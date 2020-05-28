package org.hadatac.data.loader;

import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.Templates;

import java.lang.Exception;

public class STRTopicGenerator extends BaseGenerator {

    private String startTime = "";
    private RecordFile specRecordFile = null;
    private String errorMessage = "";
    private String errorArgument = "";
    private Map<String,String> objMap = null;

    public STRTopicGenerator(DataFile dataFile, RecordFile specRecordFile, String startTime) {
        super(dataFile);
    	System.out.println("...inside STRTopicGenerator.");
		this.file = specRecordFile;
		this.records = file.getRecords();
        this.specRecordFile = specRecordFile;
        this.startTime = startTime;
        this.objMap = new HashMap<String,String>();
        dataFile.getLogger().println("STRTopicGenerator: End of constructor -> Number of records: " + specRecordFile.getNumberOfRows());
    }

    @Override
    public void initMapping() {
        mapCol.clear();
        mapCol.put("StreamURI", Templates.MESSAGESTREAMURI);
        mapCol.put("TopicName", Templates.TOPICNAME);
        mapCol.put("DeploymentUri", Templates.DEPLOYMENTURI);
        mapCol.put("CellScope", Templates.CELLSCOPE);
    }

    public boolean isValid() {
    	for (Record rec: specRecordFile.getRecords()) {
            String dplUri = URIUtils.replacePrefixEx(getDeploymentUri(rec));
            if (dplUri.isEmpty()) {
            	errorMessage = "STR_00014";
                errorArgument = "";
                return false;
            }
            Deployment dpl = Deployment.find(dplUri);
            if (dpl == null) {
            	errorMessage = "STR_00014";
                errorArgument = getDeploymentUri(rec);
                return false;
            } 
            String scopeUri = getCellScope(rec);
            // process single object's scope
            if (scopeUri.startsWith("<<*, ")) {
            	String objUriPref = scopeUri.substring(5,scopeUri.length() - 2);
            	String objUri = URIUtils.replacePrefixEx(objUriPref);
            	StudyObject obj = StudyObject.find(objUri);
            	if (obj == null) {
            		errorMessage = "STR_00015";
            		errorArgument = objUriPref;
            		return false;
            	}
            	objMap.put(getDeploymentUri(rec), objUri);
            } else {
            // TODO: implement other kinds of scope
            }
    	}
    	return true;
    }
    
    public String getErrorMessage() {
    	return errorMessage;
    }
    
    public String getErrorArgument() {
    	return errorArgument;
    }
    
    private String getMessageStreamUri(Record rec) {
        String messageStreamUri = rec.getValueByColumnName(mapCol.get("StreamURI")).equalsIgnoreCase("NULL")? 
                "" : rec.getValueByColumnName(mapCol.get("StreamURI"));
        return messageStreamUri;
    }

    private String getUri(Record rec) {
    	String uri = getMessageStreamUri(rec).replace("MS-","MT-") + getTopicName(rec).replaceAll("/", "");
    	return uri;
    }

    private String getTopicName(Record rec) {
        String topicName = rec.getValueByColumnName(mapCol.get("TopicName")).equalsIgnoreCase("NULL")? 
                "" : rec.getValueByColumnName(mapCol.get("TopicName"));
        return topicName;
    }

    public String getDeploymentUri(Record rec) {
        String dplUri = rec.getValueByColumnName(mapCol.get("DeploymentUri"));
        if (dplUri == null) {
        	return "";
        }
        return dplUri;    	
    }
    
    private String getCellScope(Record rec) {
        return rec.getValueByColumnName(mapCol.get("CellScope"));
    }

    @Override
    public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {
        Map<String, Object> row = new HashMap<String, Object>();
		dataFile.getLogger().println("STRTopicGenerator: At createRow. Topic is [" + getTopicName(rec) + "].   URI is [" + getUri(rec) + "]");
		row.put("hasURI", getUri(rec));
		row.put("a", "hasco:MessageTopic");
		row.put("rdfs:label", getTopicName(rec));
		row.put("hasco:hasDeployment", URIUtils.replacePrefixEx(getDeploymentUri(rec)));
		row.put("hasco:hasStudyObject", objMap.get(getDeploymentUri(rec)));
		row.put("hasco:hasCellScope", getCellScope(rec));
		row.put("hasco:isMemberOf", getMessageStreamUri(rec));
		if (startTime.isEmpty()) {
			row.put("prov:startedAtTime", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).format(new Date()));
		} else {
			row.put("prov:startedAtTime", startTime);
		}
    	return row;
    }
        
    @Override
    public String getTableName() {
        return "MessageTopic";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in STRTopicGenerator: " + e.getMessage();
    }

}

