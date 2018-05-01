package org.hadatac.data.loader;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.entity.pojo.HADatAcThing;


public class MotherGenerator extends BasicGenerator {

    String study_id;
    String file_name;
    final String kbPrefix = ConfigProp.getKbPrefix();
    public MotherGenerator(RecordFile file) {
        super(file);
        file_name = file.getFile().getName();
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

    public StudyObject createStudyObject(Record record) throws Exception {
        StudyObject obj = new StudyObject(getUri(record), "sio:Human", 
                getOriginalID(record), getLabel(record), 
                getCohortUri(record), getLabel(record));
        obj.setRoleUri(URIUtils.replacePrefixEx("chear:Mother"));
        obj.addScopeUri(kbPrefix + "SBJ-" + getOriginalSID(record) + "-" + study_id);
        return obj;
    }
    
    @Override
    HADatAcThing createObject(Record rec, int row_number) throws Exception {
        return createStudyObject(rec);
    }
	
    @Override
    Map<String, Object> createRow(Record rec, int row_number) throws Exception {
        if (getOriginalID(rec).length() > 0 && getOriginalSID(rec).length() > 0) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("hasURI", getUri(rec));
            row.put("a", "chear:Mother");
            row.put("chear:Mother", kbPrefix + "SBJ-" + getOriginalSID(rec) + "-" + study_id);
            return row;
        }
        
        return null;
    }

    @Override
    public void preprocess() throws Exception {
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
