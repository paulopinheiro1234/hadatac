package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.poi.util.SystemOutLogger;
import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Templates;

import java.lang.Exception;

public class DataAcquisitionGenerator extends BasicGenerator {
	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";

	public DataAcquisitionGenerator(File file) {
		super(file);
	}

	public DataAcquisitionGenerator(File file, String startTime) {
		super(file);
		this.startTime = startTime;
	}

	@Override
	void initMapping() {
	}

	private String getDataAcquisitionName(CSVRecord rec) {
		return getValueByColumnName(rec, Templates.DATAACQUISITIONNAME);
	}

	private String getOwnerEmail(CSVRecord rec) {
		String ownerEmail = getValueByColumnName(rec, Templates.OWNEREMAIL);
		if(ownerEmail.equalsIgnoreCase("NULL") || ownerEmail.isEmpty()) {
			return "";
		}
		else {
			return ownerEmail;
		}
	}
	
	private String getPermissionUri(CSVRecord rec) {
		return getValueByColumnName(rec, Templates.PERMISSIONURI);
	}

	private String getMethod(CSVRecord rec) {
		String method = getValueByColumnName(rec, Templates.METHOD);
		if(method.equalsIgnoreCase("NULL") || method.isEmpty()) {
			return "";
		}
		else {
			return "hasco:" + method;
		}
	}

	private String getStudy(CSVRecord rec) {
		return getValueByColumnName(rec, Templates.DASTUDYID).equalsIgnoreCase("NULL")? 
				"" : getValueByColumnName(rec, Templates.DASTUDYID);
	}

	private String getDataDictionaryName(CSVRecord rec) {
		String DDName = getValueByColumnName(rec, Templates.DATADICTIONARYNAME).equalsIgnoreCase("NULL")? 
				"" : getValueByColumnName(rec, Templates.DATADICTIONARYNAME);
		return DDName.replace("SDD-","");
	}

	private Boolean isEpiData(CSVRecord rec) {
		return getValueByColumnName(rec, Templates.EPILAB).equalsIgnoreCase("EPI");
	}

	private Boolean isLabData(CSVRecord rec) {
		return getValueByColumnName(rec, Templates.EPILAB).equalsIgnoreCase("LAB");
	}

	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DA-" + getDataAcquisitionName(rec));
		row.put("a", "hasco:DataAcquisition");
		row.put("rdfs:label", getDataAcquisitionName(rec));
		row.put("hasco:hasDeployment", kbPrefix + "DPL-" + getDataAcquisitionName(rec));
		row.put("hasco:hasMethod", getMethod(rec));
		row.put("hasco:isDataAcquisitionOf", kbPrefix + "STD-" + getStudy(rec));
		if (startTime.isEmpty()) {
			row.put("prov:startedAtTime", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).format(new Date()));
		} else {
			row.put("prov:startedAtTime", startTime);
		}
		if (isEpiData(rec)) {
			row.put("hasco:hasSchema", kbPrefix + "DAS-" + getDataDictionaryName(rec));
		} else if (isLabData(rec)) {
			row.put("hasco:hasSchema", kbPrefix + "DAS-STANDARD-LAB-SCHEMA");
		}

		String ownerEmail = getOwnerEmail(rec);
		if (ownerEmail.isEmpty()) {
			throw new Exception(String.format("Owner Email is not specified for Row %s!", row_number));
		}
		
		String permissionUri = getPermissionUri(rec);
		if (permissionUri.isEmpty()) {
			throw new Exception(String.format("Permission URI is not specified for Row %s!", row_number));
		}

		String deploymentUri = ValueCellProcessing.replacePrefixEx(kbPrefix + "DPL-" + getDataAcquisitionName(rec));
		createDataAcquisition(row, ownerEmail, permissionUri, deploymentUri, isEpiData(rec));

		return row;
	}

	void createDataAcquisition(Map<String, Object> row, 
			String ownerEmail, 
			String permissionUri, 
			String deploymentUri,
			boolean isEpiData) throws Exception {
		DataAcquisition da = new DataAcquisition();
		da.setUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasURI")));
		da.setLabel(ValueCellProcessing.replacePrefixEx((String)row.get("rdfs:label")));
		da.setDeploymentUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasco:hasDeployment")));
		da.setMethodUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasco:hasMethod")));
		da.setStudyUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasco:isDataAcquisitionOf")));
		da.setSchemaUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasco:hasSchema")));
		da.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
		da.setNumberDataPoints(Measurement.getNumByDataAcquisition(da));
		
		for (ObjectCollection oc : ObjectCollection.findByStudyUri(da.getStudyUri())) {
			if ((isEpiData && oc.getTypeUri().equals(ValueCellProcessing.replacePrefixEx("hasco:SubjectGroup")))
					|| (!isEpiData && oc.getTypeUri().equals(ValueCellProcessing.replacePrefixEx("hasco:SampleCollection")))) {
				da.setGlobalScopeUri(oc.getUri());
				System.out.println("Set GlobalScopeUri to: " + oc.getUri());
				break;
			}
		}
		
		SysUser user = SysUser.findByEmail(ownerEmail);
		if (null == user) {
			throw new Exception(String.format("The specified owner email %s is not a valid user!", ownerEmail));
		} else {
			da.setOwnerUri(user.getUri());
			da.setPermissionUri(permissionUri);
		}

		String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		if (startTime.isEmpty()) {
			da.setStartedAt(new DateTime(new Date()));
		} else {
			da.setStartedAt(DateTimeFormat.forPattern(pattern).parseDateTime(startTime));
		}

		Deployment deployment = Deployment.find(deploymentUri);
		if (deployment != null) {
			da.setDeploymentUri(deploymentUri);
			if (deployment.getPlatform() != null) {
				da.setPlatformUri(deployment.getPlatform().getUri());
				da.setPlatformName(deployment.getPlatform().getLabel());
			} else {
				throw new Exception(String.format("No platform of Deployment %s is specified!", deploymentUri));
			}
			if (deployment.getInstrument() != null) {
				da.setInstrumentUri(deployment.getInstrument().getUri());
				da.setInstrumentModel(deployment.getInstrument().getLabel());
			} else {
				throw new Exception(String.format("No instrument of Deployment %s is specified!", deploymentUri));
			}
			da.setStartedAtXsdWithMillis(deployment.getStartedAt());
		} else {
			throw new Exception(String.format("Deployment %s cannot be found!", deploymentUri));
		}

		da.save();
	}
}
