package org.hadatac.data.loader;

import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.URIUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Templates;

import java.lang.Exception;


public class OASGenerator extends BaseGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();
    String startTime = "";

    public OASGenerator(RecordFile file) {
        super(file);
    }

    public OASGenerator(RecordFile file, String startTime) {
        super(file);
        this.startTime = startTime;
    }

    @Override
    public void initMapping() {}

    private String getStudy(Record rec) {
        return rec.getValueByColumnName(Templates.DASTUDYID).equalsIgnoreCase("NULL")? 
                "" : rec.getValueByColumnName(Templates.DASTUDYID);
    }

    private String getDataAcquisitionName(Record rec) {
        return rec.getValueByColumnName(Templates.DATAACQUISITIONNAME);
    }

    private String getDataDictionaryName(Record rec) {
        String DDName = rec.getValueByColumnName(Templates.DATADICTIONARYNAME).equalsIgnoreCase("NULL")? 
                "" : rec.getValueByColumnName(Templates.DATADICTIONARYNAME);
        return DDName.replace("SDD-","");
    }

    private String getDeployment(Record rec) { 
        return rec.getValueByColumnName(Templates.DEPLOYMENTURI);
    }

    //private String getRowScope(Record rec) {
    //    return rec.getValueByColumnName(Templates.ROWSCOPE);
    //}

    private String getCellScope(Record rec) {
        return rec.getValueByColumnName(Templates.CELLSCOPE);
    }

    private String getOwnerEmail(Record rec) {
        System.out.println("OASGenerator: owner email's label is [" + Templates.OWNEREMAIL + "]");
        String ownerEmail = rec.getValueByColumnName(Templates.OWNEREMAIL);
        if(ownerEmail.equalsIgnoreCase("NULL") || ownerEmail.isEmpty()) {
            return "";
        } else {
            return ownerEmail;
        }
    }

    private String getPermissionUri(Record rec) {
        return rec.getValueByColumnName(Templates.PERMISSIONURI);
    }

    @Override
    public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", kbPrefix + "DA-" + getDataAcquisitionName(rec));
        row.put("a", "hasco:DataAcquisition");
        row.put("rdfs:label", getDataAcquisitionName(rec));
        row.put("hasco:hasDeployment", getDeployment(rec));
        //row.put("hasco:hasMethod", getMethod(rec));
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
    public HADatAcThing createObject(Record rec, int rowNumber) throws Exception {
        Map<String, Object> row = createRow(rec, rowNumber);

        String ownerEmail = getOwnerEmail(rec);
        if (ownerEmail.isEmpty()) {
            DataFile dataFile = DataFile.findByName(getRelativePath());
            if (null != dataFile) {
                ownerEmail = dataFile.getOwnerEmail();
                if (ownerEmail.isEmpty()) {
                    throw new Exception(String.format("Owner Email is empty from records for the uploaded file!"));
                }
            } else {
                throw new Exception(String.format("Owner Email is not specified for Row %s!", rowNumber));
            }
        }

        String permissionUri = getPermissionUri(rec);
        if (permissionUri.isEmpty()) {
            SysUser user = SysUser.findByEmail(ownerEmail);
            if (null != user) {
                permissionUri = user.getUri();
                if (permissionUri.isEmpty()) {
                    throw new Exception(String.format("URI is empty for the user with email %s", ownerEmail));
                }
            } else {
                throw new Exception(String.format("Permission URI is not specified for Row %s!", rowNumber));
            }
        }

        String deploymentUri = URIUtils.replacePrefixEx(getDeployment(rec));

        //String rowScopeStr = getRowScope(rec);

        String cellScopeStr = getCellScope(rec);

        return createDataAcquisition(row, ownerEmail, permissionUri, deploymentUri, /*rowScopeStr,*/ cellScopeStr);
    }

    private ObjectAccessSpec createDataAcquisition(
            Map<String, Object> row, 
            String ownerEmail, 
            String permissionUri, 
            String deploymentUri,
            //String rowScopeStr,
            String cellScopeStr) throws Exception {

        ObjectAccessSpec da = new ObjectAccessSpec();

        da.setUri(URIUtils.replacePrefixEx((String)row.get("hasURI")));
        da.setLabel(URIUtils.replacePrefixEx((String)row.get("rdfs:label")));
        da.setDeploymentUri(URIUtils.replacePrefixEx((String)row.get("hasco:hasDeployment")));
        da.setStudyUri(URIUtils.replacePrefixEx((String)row.get("hasco:isDataAcquisitionOf")));
        da.setSchemaUri(URIUtils.replacePrefixEx((String)row.get("hasco:hasSchema")));
        da.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
        da.setNumberDataPoints(Measurement.getNumByDataAcquisition(da));

        setStudyUri(URIUtils.replacePrefixEx((String)row.get("hasco:isDataAcquisitionOf")));

        // process row scope (NEEDS TO BE FIXED -- USE SSD INSTEAD OF HARD-CODED)
        /*
	    for (ObjectCollection oc : ObjectCollection.findByStudyUri(da.getStudyUri())) {
		if ((isEpiData && oc.getTypeUri().equals(URIUtils.replacePrefixEx("hasco:SubjectGroup")))
		    || (!isEpiData && oc.getTypeUri().equals(URIUtils.replacePrefixEx("hasco:SampleCollection")))) {
		    da.setRowScopeUri(oc.getUri());
		    System.out.println("Set GlobalScopeUri to: " + oc.getUri());
		    break;
		}
		} */

        // process cell scope
        System.out.println("Showing returned CellScope: [" + cellScopeStr + "]");
        String[] cellList = null;
        String[] elementList = null;
        if (cellScopeStr != null && !cellScopeStr.equals("")) {
            if (!cellScopeStr.startsWith("<")) {
                System.out.println("[ERROR] OAS Generator: CellScope ill-formed: should start with <");
            } else if (!cellScopeStr.endsWith(">")) {
                System.out.println("[ERROR] OAS Generator: CellScope ill-formed: should end with >");
            } else {
                cellScopeStr = cellScopeStr.substring(1, cellScopeStr.length()-1);
                cellList = cellScopeStr.split(";");
                for (String cellSpec : cellList) {
                    cellSpec = cellSpec.trim();
                    if (!cellSpec.startsWith("<")) {
                        System.out.println("[ERROR] OAS Generator: CellScope ill-formed: cell spec " + cellSpec + " should start with <");
                        break;
                    } else if (!cellSpec.endsWith(">")) {
                        System.out.println("[ERROR] OAS Generator: CellScope ill-formed: cell spec " + cellSpec + " should end with >");
                        break;
                    } else {
                        cellSpec = cellSpec.substring(1, cellSpec.length()-1);
                        elementList = cellSpec.split(",");
                        if (elementList.length != 2) { 
                            System.out.println("[ERROR] OAS Generator: CellScope ill-formed: cell spec " + cellSpec + " should have a name and an URI");
                            break;
                        }
                        da.addCellScopeName(elementList[0]);
                        da.addCellScopeUri(URIUtils.replacePrefixEx((String)elementList[1]));
                    }
                }
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
                throw new Exception(String.format("No platform of Deployment <%s> is specified!", deploymentUri));
            }
            if (deployment.getInstrument() != null) {
                da.setInstrumentUri(deployment.getInstrument().getUri());
                da.setInstrumentModel(deployment.getInstrument().getLabel());
            } else {
                throw new Exception(String.format("No instrument of Deployment <%s> is specified!", deploymentUri));
            }
            da.setStartedAtXsdWithMillis(deployment.getStartedAt());
        } else {
            throw new Exception(String.format("Deployment <%s> cannot be found!", deploymentUri));
        }

        DataAcquisitionSchema schema = DataAcquisitionSchema.find(da.getSchemaUri());
        if (schema != null) {
            da.setStatus(9999);
        } else {
            throw new Exception(String.format("SDD <%s> cannot be found. Please ingest proper SDD file first. ", da.getSchemaUri()));
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

