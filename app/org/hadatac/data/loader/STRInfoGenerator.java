package org.hadatac.data.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.MessageStream;
import org.hadatac.entity.pojo.Study;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import io.jsonwebtoken.lang.Objects;

public class STRInfoGenerator extends BaseGenerator{

	final String kbPrefix = ConfigProp.getKbPrefix();
	public static final String FILESTREAM = "FileStream";
    public static final String MESSAGESTREAM = "MessageStream";
    public static final String MESSAGETOPIC = "MessageTopic";

    private Map<String, String> mapCatalog = new HashMap<String, String>();
    private Map<String, Map<String, String>> fileStreamSpec = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> messageStreamSpec = new HashMap<String, Map<String, String>>();
    private String studyId;
    private String version;
    private Study study;

    public STRInfoGenerator(DataFile dataFile) {
    	super(dataFile);
    	System.out.println("...inside STRInfoGenerator.");
    	readCatalog(dataFile.getRecordFile());
    	study = null;
    }

    public Map<String, String> getCatalog() {
        return mapCatalog;
    }

    public Map<String, Map<String, String>> getFileStreamSpec() {
        return fileStreamSpec;
    }

    public Map<String, Map<String, String>> getMessageStreamSpec() {
        return messageStreamSpec;
    }

    private void readCatalog(RecordFile file) {
        if (!file.isValid()) {
            return;
        }
        // This is on the infosheet
        for (Record record : file.getRecords()) {
            mapCatalog.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
            System.out.println("STR's mapCatalog: [" + record.getValueByColumnIndex(0) + "]  [" + record.getValueByColumnIndex(1) + "]");
        }
    }

    public String getStudyId() {
        studyId = mapCatalog.get("Study_ID");
    	//System.out.println("studyID in getStudyId: [" + studyId + "]");
        if (studyId == null || studyId.isEmpty()) {
        	study = null;
        	return "";            
        }
        study = Study.findById(studyId);
        return studyId;
    }

    public Study getStudy() {
    	// this will load the study, if available
    	getStudyId();
    	return study;
    }
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
