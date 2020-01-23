package org.hadatac.data.loader;

import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.data.loader.DASOInstanceGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Templates;

import java.lang.Exception;


public class STRGenerator extends BaseGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();
    String startTime = "";

    public STRGenerator(DataFile dataFile) {
        super(dataFile);
    }

    public STRGenerator(DataFile dataFile, String startTime) {
        super(dataFile);
        this.startTime = startTime;
    }

    @Override
    public void initMapping() {}

    private String getStudy(Record rec) {
        return rec.getValueByColumnName(Templates.DASTUDYID).equalsIgnoreCase("NULL")? 
                "" : rec.getValueByColumnName(Templates.DASTUDYID);
    }

    private String getSTRName(Record rec) {
        return rec.getValueByColumnName(Templates.DATAACQUISITIONNAME);
    }

    private String getSDDName(Record rec) {
        String SDDName = rec.getValueByColumnName(Templates.DATADICTIONARYNAME).equalsIgnoreCase("NULL")? 
                "" : rec.getValueByColumnName(Templates.DATADICTIONARYNAME);
        return SDDName.replace("SDD-","");
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
        //System.out.println("STRGenerator: owner email's label is [" + Templates.OWNEREMAIL + "]");
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
        row.put("hasURI", kbPrefix + "DA-" + getSTRName(rec));
        row.put("a", "hasco:DataAcquisition");
        row.put("rdfs:label", getSTRName(rec));
        row.put("hasco:hasDeployment", getDeployment(rec));
        //row.put("hasco:hasMethod", getMethod(rec));
        row.put("hasco:isDataAcquisitionOf", kbPrefix + "STD-" + getStudy(rec));
        if (startTime.isEmpty()) {
            row.put("prov:startedAtTime", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).format(new Date()));
        } else {
            row.put("prov:startedAtTime", startTime);
        }
        row.put("hasco:hasSchema", kbPrefix + "DAS-" + getSDDName(rec));

        return row;
    }

    @Override
    public HADatAcThing createObject(Record rec, int rowNumber) throws Exception {
        Map<String, Object> row = createRow(rec, rowNumber);

        String ownerEmail = getOwnerEmail(rec);
        if (ownerEmail.isEmpty()) {
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

        return createSTR(row, ownerEmail, permissionUri, deploymentUri, /*rowScopeStr,*/ cellScopeStr);
    }

    private STR createSTR(
            Map<String, Object> row, 
            String ownerEmail, 
            String permissionUri, 
            String deploymentUri,
            //String rowScopeStr,
            String cellScopeStr) throws Exception {

        STR str = new STR();

        str.setUri(URIUtils.replacePrefixEx((String)row.get("hasURI")));
        System.out.println("STRGenerator: creating STR with URI=" + str.getUri());
        str.setLabel(URIUtils.replacePrefixEx((String)row.get("rdfs:label")));
        str.setDeploymentUri(URIUtils.replacePrefixEx((String)row.get("hasco:hasDeployment")));
        str.setStudyUri(URIUtils.replacePrefixEx((String)row.get("hasco:isDataAcquisitionOf")));
        str.setSchemaUri(URIUtils.replacePrefixEx((String)row.get("hasco:hasSchema")));
        str.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
        str.setNumberDataPoints(Measurement.getNumByDataAcquisition(str));

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
        System.out.println("STRGenerator: Specified CellScope: [" + cellScopeStr + "]");
        String[] cellList = null;
        String[] elementList = null;
        if (cellScopeStr != null && !cellScopeStr.equals("")) {
            if (!cellScopeStr.startsWith("<")) {
                System.out.println("[ERROR] STR Generator: CellScope ill-formed: should start with <");
            } else if (!cellScopeStr.endsWith(">")) {
                System.out.println("[ERROR] STR Generator: CellScope ill-formed: should end with >");
            } else {
                cellScopeStr = cellScopeStr.substring(1, cellScopeStr.length()-1);
                cellList = cellScopeStr.split(";");
                for (String cellSpec : cellList) {
                    cellSpec = cellSpec.trim();
                    if (!cellSpec.startsWith("<")) {
                        System.out.println("[ERROR] STR Generator: CellScope ill-formed: cell spec " + cellSpec + " should start with <");
                        break;
                    } else if (!cellSpec.endsWith(">")) {
                        System.out.println("[ERROR] STR Generator: CellScope ill-formed: cell spec " + cellSpec + " should end with >");
                        break;
                    } else {
                        cellSpec = cellSpec.substring(1, cellSpec.length()-1);
                        elementList = cellSpec.split(",");
                        if (elementList.length != 2) { 
                            System.out.println("[ERROR] STR Generator: CellScope ill-formed: cell spec " + cellSpec + " should have a name and an URI");
                            break;
                        }
                        str.addCellScopeName(elementList[0]);
                        str.addCellScopeUri(URIUtils.replacePrefixEx((String)elementList[1]));
                    }
                }
            }
        }		

        System.out.println("STRGenerator: Specified owner email: [" + ownerEmail + "]");
        SysUser user = SysUser.findByEmail(ownerEmail);
        if (null == user) {
            throw new Exception(String.format("The specified owner email %s is not a valid user!", ownerEmail));
        } else {
            str.setOwnerUri(user.getUri());
            str.setPermissionUri(permissionUri);
        }

        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        if (startTime.isEmpty()) {
            str.setStartedAt(new DateTime(new Date()));
        } else {
            str.setStartedAt(DateTimeFormat.forPattern(pattern).parseDateTime(startTime));
        }

        System.out.println("STRGenerator: Specified deployment: [" + deploymentUri + "]");
        Deployment deployment = Deployment.find(deploymentUri);
        if (deployment != null) {
            str.setDeploymentUri(deploymentUri);
            if (deployment.getPlatform() != null) {
                str.setPlatformUri(deployment.getPlatform().getUri());
                str.setPlatformName(deployment.getPlatform().getLabel());
            } else {
                throw new Exception(String.format("No platform of Deployment <%s> is specified!", deploymentUri));
            }
            if (deployment.getInstrument() != null) {
                str.setInstrumentUri(deployment.getInstrument().getUri());
                str.setInstrumentModel(deployment.getInstrument().getLabel());
            } else {
                throw new Exception(String.format("No instrument of Deployment <%s> is specified!", deploymentUri));
            }
            str.setStartedAtXsdWithMillis(deployment.getStartedAt());
        } else {
            throw new Exception(String.format("Deployment <%s> cannot be found!", deploymentUri));
        }

        System.out.println("STRGenerator: Specified SDD: [" + str.getSchemaUri() + "]");
        DataAcquisitionSchema schema = DataAcquisitionSchema.find(str.getSchemaUri());
        if (schema != null) {
            str.setStatus(9999);
        } else {
            throw new Exception(String.format("SDD <%s> cannot be found. Please ingest proper SDD file first. ", str.getSchemaUri()));
        }

        return str;
    }

    @Override
    public String getTableName() {
        return "DataAcquisition";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in STRGenerator: " + e.getMessage();
    }

}

