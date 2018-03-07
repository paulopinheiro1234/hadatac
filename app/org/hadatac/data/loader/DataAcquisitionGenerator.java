package org.hadatac.data.loader;

import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.URIUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Templates;

import java.lang.Exception;

public class DataAcquisitionGenerator extends BasicGenerator {
	
	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";

	public DataAcquisitionGenerator(RecordFile file) {
		super(file);
	}

	public DataAcquisitionGenerator(RecordFile file, String startTime) {
		super(file);
		this.startTime = startTime;
	}

	@Override
	void initMapping() {}

	private String getDataAcquisitionName(Record rec) {
		return rec.getValueByColumnName(Templates.DATAACQUISITIONNAME);
	}

	private String getOwnerEmail(Record rec) {
		String ownerEmail = rec.getValueByColumnName(Templates.OWNEREMAIL);
		if(ownerEmail.equalsIgnoreCase("NULL") || ownerEmail.isEmpty()) {
			return "";
		}
		else {
			return ownerEmail;
		}
	}
	
	private String getPermissionUri(Record rec) {
		return rec.getValueByColumnName(Templates.PERMISSIONURI);
	}

	private String getMethod(Record rec) {
		String method = rec.getValueByColumnName(Templates.METHOD);
		if(method.equalsIgnoreCase("NULL") || method.isEmpty()) {
			return "";
		}
		else {
			return "hasco:" + method;
		}
	}

	private String getStudy(Record rec) {
		return rec.getValueByColumnName(Templates.DASTUDYID).equalsIgnoreCase("NULL")? 
				"" : rec.getValueByColumnName(Templates.DASTUDYID);
	}

	private String getDataDictionaryName(Record rec) {
		String DDName = rec.getValueByColumnName(Templates.DATADICTIONARYNAME).equalsIgnoreCase("NULL")? 
				"" : rec.getValueByColumnName(Templates.DATADICTIONARYNAME);
		return DDName.replace("SDD-","");
	}

	private Boolean isEpiData(Record rec) {
		return rec.getValueByColumnName(Templates.EPILAB).equalsIgnoreCase("EPI");
	}

	@Override
	Map<String, Object> createRow(Record rec, int row_number) throws Exception {
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
		row.put("hasco:hasSchema", kbPrefix + "DAS-" + getDataDictionaryName(rec));

		return row;
	}
	
	@Override
	HADatAcThing createObject(Record rec, int row_number) throws Exception {
		Map<String, Object> row = createRow(rec, row_number);
		
		String ownerEmail = getOwnerEmail(rec);
		if (ownerEmail.isEmpty()) {
			throw new Exception(String.format("Owner Email is not specified for Row %s!", row_number));
		}
		
		String permissionUri = getPermissionUri(rec);
		if (permissionUri.isEmpty()) {
			throw new Exception(String.format("Permission URI is not specified for Row %s!", row_number));
		}

		String deploymentUri = URIUtils.replacePrefixEx(kbPrefix + "DPL-" + getDataAcquisitionName(rec));
		
		return createDataAcquisition(row, ownerEmail, permissionUri, deploymentUri, isEpiData(rec));
	}

	DataAcquisition createDataAcquisition(Map<String, Object> row, 
			String ownerEmail, 
			String permissionUri, 
			String deploymentUri,
			boolean isEpiData) throws Exception {
		DataAcquisition da = new DataAcquisition();
		da.setUri(URIUtils.replacePrefixEx((String)row.get("hasURI")));
		da.setLabel(URIUtils.replacePrefixEx((String)row.get("rdfs:label")));
		da.setDeploymentUri(URIUtils.replacePrefixEx((String)row.get("hasco:hasDeployment")));
		da.setMethodUri(URIUtils.replacePrefixEx((String)row.get("hasco:hasMethod")));
		da.setStudyUri(URIUtils.replacePrefixEx((String)row.get("hasco:isDataAcquisitionOf")));
		da.setSchemaUri(URIUtils.replacePrefixEx((String)row.get("hasco:hasSchema")));
		da.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
		da.setNumberDataPoints(Measurement.getNumByDataAcquisition(da));
		
		for (ObjectCollection oc : ObjectCollection.findByStudyUri(da.getStudyUri())) {
			if ((isEpiData && oc.getTypeUri().equals(URIUtils.replacePrefixEx("hasco:SubjectGroup")))
					|| (!isEpiData && oc.getTypeUri().equals(URIUtils.replacePrefixEx("hasco:SampleCollection")))) {
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
		
		DataAcquisitionSchema schema = DataAcquisitionSchema.find(da.getSchemaUri());
		if (schema != null) {
			da.setStatus(9999);
		}
		
		return da;
	}

	@Override
	public String getTableName() {
		return "DataAcquisition";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "Error in DataAcquisitionGenerator: " + e.getMessage();
	}
}
