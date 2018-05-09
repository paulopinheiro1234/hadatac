package org.hadatac.data.loader;

import java.lang.String;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;


public class SubjectGenerator extends BasicGenerator {

    String file_name;
    String study_id;

    final String kbPrefix = ConfigProp.getKbPrefix();

    public SubjectGenerator(RecordFile file) {
        super(file);
        file_name = file.getFile().getName();
        study_id = file.getFile().getName().replaceAll("PID-", "").replaceAll("SSD-", "").replaceAll(".xlsx", "").replaceAll(".csv", "");
    }

    @Override
    void initMapping() {
        mapCol.clear();
        mapCol.put("subjectID", "CHEAR PID");
        mapCol.put("pilotNum", "CHEAR Project ID");
    }

    private String getUri(Record rec) {
        return kbPrefix + "SBJ-" + getOriginalID(rec) + "-" + getStudyId(rec);
    }

    private String getLabel(Record rec) {
        return "Subject ID " + getOriginalID(rec) + " - " + getStudyId(rec);
    }

    private String getOriginalID(Record rec) {
        return rec.getValueByColumnName(mapCol.get("subjectID")).replaceAll("(?<=^\\d+)\\.0*$", "");
    }

    private String getPilotNum(Record rec) {
        return rec.getValueByColumnName(mapCol.get("pilotNum"));
    }

    private String getStudyId(Record rec) {
        if (file_name.startsWith("PID-")) {
            return getPilotNum(rec);
        } else if (file_name.startsWith("SSD-")) {
            return study_id;
        }
        return null;
    }

    private String getSSDCohortUri(Record rec) {
        return kbPrefix + "SOC-" + getStudyId(rec) + "-SUBJECTS";
    }


    private String getCohortLabel(Record rec) {
        return "Study Population of " + getStudyId(rec);
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
                kbPrefix + "STD-" + getStudyId(record));

        setStudyUri(URIUtils.replacePrefixEx(kbPrefix + "STD-" + getStudyId(record)));

        return oc;
    }

    @Override
    HADatAcThing createObject(Record rec, int row_number) throws Exception {
        return createStudyObject(rec);
    }

    @Override
    public void preprocess() throws Exception {
        if (!records.isEmpty()) {
            if (file_name.startsWith("PID-")) {
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
