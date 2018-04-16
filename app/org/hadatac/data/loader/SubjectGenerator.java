package org.hadatac.data.loader;

import java.lang.String;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;


public class SubjectGenerator extends BasicGenerator {

    static final long MAX_OBJECTS = 1000;
    static final long LENGTH_CODE = 6;
    String file_name;

    final String kbPrefix = ConfigProp.getKbPrefix();
    
    public SubjectGenerator(RecordFile file) {
        super(file);
        file_name = file.getFile().getName();
    }

    @Override
    void initMapping() {
        mapCol.clear();
        mapCol.put("subjectID", "CHEAR PID");
        mapCol.put("pilotNum", "CHEAR Project ID");
    }

    private String getUri(Record rec) {
        return kbPrefix + "SBJ-" + getOriginalID(rec) + "-" + getPilotNum(rec);
    }

    private String getLabel(Record rec) {
        return "Subject ID " + getOriginalID(rec) + " - " + getPilotNum(rec);
    }

    private String getOriginalID(Record rec) {
        return rec.getValueByColumnName(mapCol.get("subjectID")).replaceAll("(?<=^\\d+)\\.0*$", "");
    }

    private String getPilotNum(Record rec) {
        return rec.getValueByColumnName(mapCol.get("pilotNum"));
    }

    private String getStudyUri(Record rec) {
        return kbPrefix + "STD-" + getPilotNum(rec);
    }

    private String getCohortUri(Record rec) {
        return kbPrefix + "CH-" + getPilotNum(rec);
    }
    
    private String getSSDCohortUri(Record rec) {
        return kbPrefix + "SOC-" + getPilotNum(rec) + "-SUBJECTS";
    }


    private String getCohortLabel(Record rec) {
        return "Study Population of " + getPilotNum(rec);
    }

    public StudyObject createStudyObject(Record record) throws Exception {
        StudyObject obj = new StudyObject(getUri(record), "sio:Human", 
                getOriginalID(record), getLabel(record), 
                getSSDCohortUri(record), getLabel(record));
        
        return obj;
    }

    public ObjectCollection createObjectCollection(Record record) throws Exception {
        ObjectCollection oc = new ObjectCollection(
                getSSDCohortUri(record),
                "http://hadatac.org/ont/hasco/SubjectGroup",
                getCohortLabel(record),
                getCohortLabel(record),
                getStudyUri(record));
        
        return oc;
    }
    
    @Override
    HADatAcThing createObject(Record rec, int row_number) throws Exception {
        return createStudyObject(rec);
    }

    @Override
    public void preprocess() throws Exception {
        if (!records.isEmpty()) {
        	if (file_name.startsWith("PID-")){
        		objects.add(createObjectCollection(records.get(0)));
        	}
        }
    }

    @Override
    public String getTableName() {
        return "StudyObject";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in SubjectGenerator: " + e.getMessage();
    }
}
