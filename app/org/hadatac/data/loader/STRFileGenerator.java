package org.hadatac.data.loader;

import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.MessageTopic;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.entity.pojo.VirtualColumn;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.data.loader.DASOInstanceGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.Templates;

import java.lang.Exception;

public class STRFileGenerator extends BaseGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();
    String startTime = "";
    Study study = null;
    RecordFile specRecordFile = null;

    public STRFileGenerator(DataFile dataFile, Study study, RecordFile specRecordFile, String startTime) {
        super(dataFile);
		this.file = specRecordFile;
		this.records = file.getRecords();
        this.study = study;
        this.specRecordFile = specRecordFile;
        this.startTime = startTime;
        dataFile.getLogger().println("STRFileGenerator: End of constructor -> Number of records: " + specRecordFile.getNumberOfRows());
    }

    @Override
    public void initMapping() {}

    private String getSTRName(Record rec) {
    	System.out.println("getSTRName: " + Templates.DATAACQUISITIONNAME + "  [" + rec.getValueByColumnName(Templates.DATAACQUISITIONNAME) + "]");
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
		dataFile.getLogger().println("STRFileGenerator: At createRow. Row Number " + rowNumber + "  record size: " + rec.size());
		row.put("hasURI", kbPrefix + "DA-" + getSTRName(rec));
		row.put("a", "hasco:DataAcquisition");
		row.put("rdfs:label", getSTRName(rec));
		row.put("hasco:hasDeployment", getDeployment(rec));
		row.put("hasco:isDataAcquisitionOf", study.getUri());
		if (startTime.isEmpty()) {
			row.put("prov:startedAtTime", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).format(new Date()));
		} else {
			row.put("prov:startedAtTime", startTime);
		}
		row.put("hasco:hasSchema", kbPrefix + "DAS-" + getSDDName(rec));
    	return row;
    }

    @Override
    public HADatAcThing createObject(Record rec, int rowNumber, String selector) throws Exception {
	    Map<String, Object> row = createRow(rec, rowNumber);
	    if (row == null) {
    		return null;
    	}
    	
        STR str = new STR();

        str.setUri(URIUtils.replacePrefixEx((String)row.get("hasURI")));
        str.setStudyUri(URIUtils.replacePrefixEx((String)row.get("hasco:isDataAcquisitionOf")));
        setStudyUri(URIUtils.replacePrefixEx((String)row.get("hasco:isDataAcquisitionOf")));
        str.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
        str.setNumberDataPoints(Measurement.getNumByDataAcquisition(str));
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        if (startTime.isEmpty()) {
            str.setStartedAt(new DateTime(new Date()));
        } else {
            str.setStartedAt(DateTimeFormat.forPattern(pattern).parseDateTime(startTime));
        }

        // process STREAM NAME (i.e., the DA NAME)
        if (getSTRName(rec) == null || getSTRName(rec).isEmpty()) {
            dataFile.getLogger().printExceptionByIdWithArgs("STR_00020");
            throw new Exception();
    	}
        str.setLabel(URIUtils.replacePrefixEx((String)row.get("rdfs:label")));
        dataFile.getLogger().println("createStr [1/6] - assigned URI: [" + str.getUri() + "]");

        // process CELL SCOPE
        String cellScopeStr = getCellScope(rec);
        String[] cellList = null;
        String[] elementList = null;
        if (cellScopeStr != null && !cellScopeStr.equals("")) {
            if (!cellScopeStr.startsWith("<")) {
                dataFile.getLogger().printExceptionById("STR_00022");
                throw new Exception();
            } else if (!cellScopeStr.endsWith(">")) {
                dataFile.getLogger().printExceptionById("STR_00023");
                throw new Exception();
            } else {
                cellScopeStr = cellScopeStr.substring(1, cellScopeStr.length()-1);
                cellList = cellScopeStr.split(";");
                for (String cellSpec : cellList) {
                    cellSpec = cellSpec.trim();
                    if (!cellSpec.startsWith("<")) {
                        dataFile.getLogger().printExceptionByIdWithArgs("STR_00024", cellSpec);
                        throw new Exception();
                    } else if (!cellSpec.endsWith(">")) {
                        dataFile.getLogger().printExceptionByIdWithArgs("STR_00025", cellSpec);
                        throw new Exception();
                    } else {
                        cellSpec = cellSpec.substring(1, cellSpec.length()-1);
                        elementList = cellSpec.split(",");
                        if (elementList.length != 2) { 
                            dataFile.getLogger().printExceptionByIdWithArgs("STR_00026", cellSpec);
                            throw new Exception();
                        }
                        str.addCellScopeName(elementList[0]);
                        str.addCellScopeUri(URIUtils.replacePrefixEx((String)elementList[1]));
                    }
                }
            }
        }		
        dataFile.getLogger().println("createStr [2/6] - Specified CellScope: [" + cellScopeStr + "]");

        // process OWNER EMAIL
        String ownerEmail = getOwnerEmail(rec);
        SysUser user = SysUser.findByEmail(ownerEmail);
        if (null == user) {
            dataFile.getLogger().printExceptionByIdWithArgs("STR_00028", ownerEmail);
            throw new Exception();
        } else {
            str.setOwnerUri(user.getUri());
        }
	    if (ownerEmail.isEmpty()) {
	        if (null != dataFile) {
	            ownerEmail = dataFile.getOwnerEmail();
	            if (ownerEmail.isEmpty()) {
	                dataFile.getLogger().printExceptionById("STR_00029");
	                throw new Exception();
	            }
	        } else {
	            dataFile.getLogger().printExceptionByIdWithArgs("STR_00030", rowNumber);
	            throw new Exception();
	        }
	    }
        dataFile.getLogger().println("createStr [3/6] - Specified owner email: [" + ownerEmail + "]");
	    
	    // process PERMISSION URI
	    String permissionUri = getPermissionUri(rec);
	    if (permissionUri.isEmpty()) {
	        user = SysUser.findByEmail(ownerEmail);
	        if (null != user) {
	            permissionUri = user.getUri();
	            if (permissionUri.isEmpty()) {
		            dataFile.getLogger().printExceptionByIdWithArgs("STR_00031", ownerEmail);
		            throw new Exception();
	            }
	        } else {
	            dataFile.getLogger().printExceptionByIdWithArgs("STR_00032", rowNumber);
	            throw new Exception();
	        }
	    }
	    str.setPermissionUri(permissionUri);
        dataFile.getLogger().println("createStr [4/6] - Specified permission: [" + permissionUri + "]");
	    
        // process DEPLOYMENT
        if (row.get("hasco:hasDeployment") == null || ((String)row.get("hasco:hasDeployment")).isEmpty()) {
            dataFile.getLogger().printExceptionByIdWithArgs("STR_00022");
            throw new Exception();
        }
        str.setDeploymentUri(URIUtils.replacePrefixEx((String)row.get("hasco:hasDeployment")));
        Deployment deployment = Deployment.find(str.getDeploymentUri());
        if (deployment != null) {
            if (deployment.getPlatform() != null) {
                str.setPlatformUri(deployment.getPlatform().getUri());
                str.setPlatformName(deployment.getPlatform().getLabel());
            } else {
	            dataFile.getLogger().printExceptionByIdWithArgs("STR_00033", str.getDeploymentUri());
	            throw new Exception();
            }
            if (deployment.getInstrument() != null) {
                str.setInstrumentUri(deployment.getInstrument().getUri());
                str.setInstrumentModel(deployment.getInstrument().getLabel());
            } else {
	            dataFile.getLogger().printExceptionByIdWithArgs("STR_00034", str.getDeploymentUri());
	            throw new Exception();
            }
            str.setStartedAtXsdWithMillis(deployment.getStartedAt());
        } else {
            dataFile.getLogger().printExceptionByIdWithArgs("STR_00022");
            throw new Exception();
        }
        dataFile.getLogger().println("createStr [5/6] - Specified deployment: [" + str.getDeploymentUri() + "]");
        
        // process SDD
	    if (getSDDName(rec) == null || getSDDName(rec).isEmpty()) {
            dataFile.getLogger().printExceptionById("STR_00021");
            throw new Exception();
	    }
        str.setSchemaUri(URIUtils.replacePrefixEx((String)row.get("hasco:hasSchema")));
        DataAcquisitionSchema schema = DataAcquisitionSchema.find(str.getSchemaUri());
        if (schema != null) {
            str.setStatus(9999);
        } else {
            dataFile.getLogger().printExceptionByIdWithArgs("STR_00035", str.getSchemaUri());
            throw new Exception();
        }
        dataFile.getLogger().println("createStr [6/6] - Specified SDD: [" + str.getSchemaUri() + "]");
        
	    if (!isFileStreamValid(str)) {
            throw new Exception();
	    }
        return str;
    }

    public boolean isFileStreamValid(STR str) {
    	boolean resp = true;
        //Record record = dataFile.getRecordFile().getRecords().get(0);
        //String studyName = record.getValueByColumnName("Study ID");
        //String studyUri = URIUtils.replacePrefixEx(ConfigProp.getKbPrefix() + "STD-" + studyName);

        //dataFile.getLogger().println("Study ID found: " + studyName);
        String studyUriForVC =(str.getStudy().getUri()).replace("STD", "SSD");
        dataFile.getLogger().println("Study URI found: " + studyUriForVC);

        List<VirtualColumn> vcList = VirtualColumn.findByStudyUri(studyUriForVC);
        // map of SOCReference and grounding label
        Map<String, String> refList = new HashMap<String, String>();
        // map of daso uri and SOCReference
        Map<String, String> tarList = new HashMap<String, String>();

        /*
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("====>>> Inside IS FILE STREAM VALID " + str.getSchema().getUri());
        System.out.println("");
        System.out.println("Target list: ");
        */
        for (VirtualColumn vc: vcList) {
            if (vc.getGroundingLabel().length() > 0) {
                refList.put(vc.getSOCReference(), vc.getGroundingLabel());
                String tarUri = URIUtils.replacePrefixEx(kbPrefix + "DASO-" + study.getId() + "-" + vc.getSOCReference().trim().replace(" ","").replace("_","-").replace("??", "")); 
                //System.out.println("  - (RefList)  [" + vc.getGroundingLabel() + "]  [" + vc.getSOCReference() + "]");
                tarList.put(tarUri,  vc.getSOCReference());
                //System.out.println("  - (TarList)  [" + vc.getGroundingLabel() + "]  [" + tarUri + "]");
            }
        }

        String queryString = null;
        ResultSetRewindable resultsrw = null;
        
        //System.out.println("DASOs requiring role assignments: ");
        Map<String, String> dasoPL = new HashMap<String, String>();
        List<DataAcquisitionSchemaObject> dasos = new ArrayList<DataAcquisitionSchemaObject>();
        List<String> roles = new ArrayList<String>();
        for (DataAcquisitionSchemaAttribute attr : str.getSchema().getAttributes()) {
            if (attr.getObjectViewLabel().length() > 0) {
                if (!roles.contains(attr.getObjectViewLabel())) {
                    roles.add(attr.getObjectViewLabel());
                    dasos.add(attr.getObject());
                    //System.out.println("  - DASO: " + attr.getObjectViewLabel() + "  " + attr.getObject().getUri());
                }
            }
        }
        dataFile.getLogger().println("DASOs requiring role assignments: " + roles.toString());
        //System.out.println("Existing mappings " + refList.toString());
        String dasUri ="";
        for (DataAcquisitionSchemaObject daso : dasos) {
        	//System.out.println("---->>> Processing DASO " + daso.getUri());
        	if (null == daso) {
                continue;
            }

            if (daso.getEntityLabel() == null || daso.getEntityLabel().length() == 0) {
                dataFile.getLogger().printExceptionByIdWithArgs("STR_00009", daso.getLabel());
            	//System.out.println("DASO with no entity/entity label" + daso.getUri());
                resp = false;
            } else if (refList.containsKey(daso.getLabel())) {
            	dataFile.getLogger().println("PATH: " + daso.getLabel() + " has role \"" + refList.get(daso.getLabel()) + "\"");
            	//System.out.println("DASO skipped");
            } else {
                dasUri = (daso!=null && daso.getPartOfSchema()!=null) ? daso.getPartOfSchema():"";
            	queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                    		"SELECT ?vc ?soc ?socRef ?vcLabel ?role WHERE { " +
                	    "   <" + daso.getUri() + "> rdfs:label ?vcLabel . " +
                	    "   ?soc hasco:hasReference ?vc . " +
                	    "   ?vc hasco:hasSOCReference ?socRef . " +
                	    "   OPTIONAL { ?soc hasco:hasRoleLabel ?role . } . " + 
                	    "   FILTER (?socRef = ?vcLabel ) . " +
                	    " }";
                
                resultsrw = SPARQLUtils.select(CollectionUtil.getCollectionPath(
                		CollectionUtil.Collection.METADATA_SPARQL), queryString);

                if (resultsrw.hasNext()) {
                    QuerySolution soln = resultsrw.next();
                    
                    if (soln.get("role").isLiteral() && soln.getLiteral("role") != null) {
                    	dataFile.getLogger().println("PATH: " + daso.getLabel() + " has role \"" + soln.getLiteral("role").toString() + "\"");
                        dasoPL.put(daso.getUri(), soln.getLiteral("role").toString() );
                        //if (refList.containsKey(soln.getLiteral("x").toString())) {
                            //answer.add(refList.get(soln.getLiteral("x").toString()));
                            //dataFile.getLogger().println("PATH: DASO: " + daso.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\"");
                            //dasoPL.put(daso.getUri(), answer.get(1) + " " + answer.get(0));
                            //found = true;
                            //break;
                        //}
                    } 
                } else {
                	//dataFile.getLogger().println(daso.getUri() + " misses a role");
                	//System.out.println(daso.getUri() + " misses a role");

                	List<String> answer = new ArrayList<String>();
                    answer.add(daso.getEntityLabel());
                    Boolean found = false;

                	//System.out.println(daso.getUri() + " size of refList: " + refList.size());
                    for (String j : refList.keySet()) {
                    	
                        //dataFile.getLogger().println("daso.getUri(): [" + daso.getUri() + "];   J is [" + j + "]");
                    	//System.out.println("daso.getUri(): [" + daso.getUri() + "];   J is [" + j + "]");
                    	

                        if (found == false) {
                            String target = kbPrefix + "DASO-" + study.getId() + "-" + j.trim().replace(" ","").replace("_","-").replace("??", "");

                            queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                                    "SELECT ?x ?o WHERE { \n" + 
                                    "<" + daso.getUri() + "> ?p ?x . \n" + 
                                    "   ?x ?p1 ?o .  \n" + 
                                    "   OPTIONAL {?o ?p2 " + target + " } " +
                                    "}";
                            //System.out.println(queryString);
                            
                            resultsrw = SPARQLUtils.select(CollectionUtil.getCollectionPath(
                                    CollectionUtil.Collection.METADATA_SPARQL), queryString);

                            //System.out.println("HERE 3");
                            if (!resultsrw.hasNext()) {
                                dataFile.getLogger().printException("STR_00009");
                                resp = false;
                            }

                            while (resultsrw.hasNext()) {
                                QuerySolution soln = resultsrw.next();                      
                                try {
                                    if (soln != null) {
                                        try {                                       
                                        	//System.out.println("HERE 4");
                                        	if (soln.get("x").isResource()){
                                                if (soln.getResource("x") != null) {
                                                    //System.out.println("Resource X: " + soln.getResource("x").toString());
                                                    if (tarList.containsKey(soln.getResource("x").toString())) {                           
	                                                    //System.out.println("IS MATCH");
                                                        answer.add(refList.get(tarList.get(soln.getResource("x").toString())));
                                                        dataFile.getLogger().println("PATH: " + daso.getLabel() + " has role \"" + answer.get(1) + " " + answer.get(0) + "\"");
                                                        dasoPL.put(daso.getUri(), answer.get(1) + " " + answer.get(0));
                                                        found = true;
                                                        break;
                                                    } else {
                                                        if (soln.get("o").isResource()){
                                                            if (soln.getResource("o") != null) {
                                                                if (tarList.containsValue(soln.getResource("o").toString())) {
                                                                    answer.add(str.getSchema().getObject(soln.getResource("o").toString()).getEntityLabel());
                                                                    dasoPL.put(daso.getUri(), answer.get(1) + " " + answer.get(0));
                                                                    found = true;
                                                                    break;
                                                                }
                                                            }
                                                        } else if (soln.get("o").isLiteral()) {
                                                            if (soln.getLiteral("o") != null) {
                                                                if (refList.containsKey(soln.getLiteral("o").toString())) {
                                                                    answer.add(refList.get(soln.getLiteral("o").toString()));
                                                                    dataFile.getLogger().println("PATH: DASO: " + daso.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\"");
                                                                    dasoPL.put(daso.getUri(), answer.get(1) + " " + answer.get(0));
                                                                    found = true;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else if (soln.get("x").isLiteral()) {
                                                if (soln.getLiteral("x") != null) {
                                                    //System.out.println("Resource X (literal): " + str.getSchema().getObject(soln.getLiteral("x").toString()));
                                                    if (refList.containsKey(soln.getLiteral("x").toString())) {
                                                        answer.add(refList.get(soln.getLiteral("x").toString()));
                                                        dataFile.getLogger().println("PATH: DASO: " + daso.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\"");
                                                        dasoPL.put(daso.getUri(), answer.get(1) + " " + answer.get(0));
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        	//System.out.println("HERE 5");

                                        } catch (Exception e1) {
                                            //return false;
                                        }
                                    }
                                } catch (Exception e) {
                                    dataFile.getLogger().printException(e.getMessage());
                                    resp = false;
                                }
                            }
                        }

                    }
                    if (found == false) {
                        dataFile.getLogger().println("PATH: DASO: " + daso.getLabel() + " Path connections can not be found ! Check for paths in SSD/SDD definition. ");
                        //System.out.println("PATH: DASO: " + daso.getLabel() + " Path connections can not be found ! check the SDD definition. ");
                        resp = false;
                    }
                }
                //System.out.println("<<<---- END OF DASO PROCESSING " + daso.getUri());
            }             
        }
        //insert the triples

        for (String uri : dasoPL.keySet()) {
            String insert = "";
            insert += NameSpaces.getInstance().printSparqlNameSpaceList();
            insert += "INSERT DATA {  ";
            insert += "graph  <"+dasUri+"> { " ;
            insert += "<" + uri + ">" + " hasco:hasRoleLabel  \"" + dasoPL.get(uri) + "\" . ";
            insert += "}} ";

            try {
                UpdateRequest request = UpdateFactory.create(insert);
                UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                        request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
                processor.execute();
            } catch (QueryParseException e) {
                System.out.println("QueryParseException due to update query: " + insert);
                resp = false;
                throw e;
            }
        }
        //System.out.println("<<<===== END OF FILE PROCESSING " + str.getSchema().getUri());
        return resp;
    }
        
    @Override
    public String getTableName() {
        return "DataAcquisition";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in STRFileGenerator: " + e.getMessage();
    }

}

