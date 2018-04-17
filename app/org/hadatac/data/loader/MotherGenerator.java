package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;


public class MotherGenerator extends BasicGenerator {

    static final long MAX_OBJECTS = 1000;
    static final long LENGTH_CODE = 6;
    String study_id;
    final String kbPrefix = ConfigProp.getKbPrefix();
    public MotherGenerator(RecordFile file) {
        super(file);
        study_id = file.getFile().getName().replaceAll("SSD-", "").replaceAll(".xlsx", "");
    }

    @Override
    void initMapping() {
        mapCol.clear();
        mapCol.put("subjectID", "CHEAR PID");
        mapCol.put("motherID", "originalID");
    }

    private String getUri(Record rec) {
        return kbPrefix + "SBJ-" + getOriginalID(rec) + "-" + study_id;
    }

    private String getLabel(Record rec) {
        return "Subject ID " + getOriginalID(rec) + " - " + study_id;
    }

    private String getOriginalID(Record rec) {
        return rec.getValueByColumnName(mapCol.get("motherID")).replaceAll("(?<=^\\d+)\\.0*$", "");
    }

    private String getStudyUri(Record rec) {
        return kbPrefix + "STD-" + study_id;
    }

    private String getOriginalSID(Record rec) {
        if(!rec.getValueByColumnName(mapCol.get("subjectID")).equalsIgnoreCase("NULL")){
            return rec.getValueByColumnName(mapCol.get("subjectID")).replaceAll("(?<=^\\d+)\\.0*$", "");
        } else {
            return "";
        }
    }
    
    private String getCohortUri(Record rec) {
        return kbPrefix + "SOC-" + study_id + "-MOTHERS";
    }

    private String getCohortLabel(Record rec) {
        return "Study Population of " + study_id;
    }

    public StudyObject createStudyObject(Record record) throws Exception {
        StudyObject obj = new StudyObject(getUri(record), "sio:Human", 
                getOriginalID(record), getLabel(record), 
                getCohortUri(record), getLabel(record));
        
        return obj;
    }
    
    @Override
    HADatAcThing createObject(Record rec, int row_number) throws Exception {
        return createStudyObject(rec);
    }
    
	@Override
	public void createRows() throws Exception {
		rows.clear();
		int row_number = 0;
		for (Record record : records) {
        //System.out.println("[DASOGenerator] creating a row....");
	    	if (getOriginalID(record).length() > 0 && getOriginalSID(record).length() > 0){
	    		rows.add(createRow(record, ++row_number));
	    	}
		}
        System.out.println("[MotherGenerator] Added " + row_number + " rows!");
	}
	
    @Override
    Map<String, Object> createRow(Record rec, int row_number) throws Exception {
    	Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", getUri(rec));
    	row.put("chear:Mother", kbPrefix + "SBJ-" + getOriginalSID(rec) + "-" + study_id);
    	return row;
    }

    @Override
    public void preprocess() throws Exception {
        if (!records.isEmpty()) {
//            rows
        	System.out.println(study_id);
        }
    }

    @Override
    public String getTableName() {
        return "StudyObject";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in MotherGenerator: " + e.getMessage();
    }
}
