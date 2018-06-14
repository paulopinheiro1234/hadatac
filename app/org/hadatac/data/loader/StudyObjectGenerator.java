package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.entity.pojo.HADatAcThing;


public class StudyObjectGenerator extends BasicGenerator {

    String study_id;
    String file_name;
    String oc_uri;
    String oc_type;
    String oc_scope;
    String oc_timescope;
    String role;
    final String kbPrefix = ConfigProp.getKbPrefix();
    private List<String> listCache = new ArrayList<String>();
    private Map<String, String> uriMap = new HashMap<String, String>();
    private Map<String, List<String>> mapContent = new HashMap<String, List<String>>();

    public StudyObjectGenerator(RecordFile file, List<String> listContent, Map<String, List<String>> mapContent) {
        super(file);
        file_name = file.getFile() .getName();
        System.out.println(file_name);
        study_id = file.getFile().getName().replaceAll("SSD-", "").replaceAll(".xlsx", "");
        
        setStudyUri(URIUtils.replacePrefixEx(kbPrefix + "STD-" + study_id));
        
        this.listCache = listContent;
        this.mapContent = mapContent;
        this.oc_uri = listContent.get(0);
//        System.out.println("oc_uri : " + oc_uri);
        this.oc_type = listContent.get(1);
//        System.out.println("oc_type : " + oc_type);
        this.oc_scope = listContent.get(2);
//        System.out.println("oc_scope : " + oc_scope);
        this.oc_timescope = listContent.get(3);
//        System.out.println("oc_timescope : " + oc_timescope);
        this.role = listContent.get(4);
//        System.out.println("role : " + role);
        uriMap.put("hasco:SubjectGroup", "SBJ-");
        uriMap.put("hasco:SampleCollection", "SPL-");
        uriMap.put("hasco:TimeCollection", "TIME-");   
    }

    @Override
    public void initMapping() {
        mapCol.clear();
        mapCol.put("originalID", "originalID");
        mapCol.put("rdf:type", "rdf:type");
        mapCol.put("scopeID", "scopeID");
        mapCol.put("timeScopeID", "timeScopeID");
    }

    private String getUri(Record rec) {
        return kbPrefix + uriMap.get(oc_type) + getOriginalID(rec) + "-" + study_id;
    }

    private String getType(Record rec) {
        return rec.getValueByColumnName(mapCol.get("rdf:type"));
    }
    
    private String getLabel(Record rec) {
        return "Study Object ID " + getOriginalID(rec) + " - " + study_id;
    }

    private String getOriginalID(Record rec) {
        return rec.getValueByColumnName(mapCol.get("originalID")).replaceAll("(?<=^\\d+)\\.0*$", "");
    }
    
    private String getCohortUri(Record rec) {
        return oc_uri;
    }
    
    private String getScopeUri(Record rec) {
    	
    	if (oc_scope.length() > 0){
        	String scopeOCtype = mapContent.get(oc_scope).get(1);
            return kbPrefix + uriMap.get(scopeOCtype) + rec.getValueByColumnName(mapCol.get("scopeID")).replaceAll("(?<=^\\d+)\\.0*$", "") + "-" + study_id;
    	} else {
    		return "";
    	}
    }
//
    public StudyObject createStudyObject(Record record) throws Exception {
        StudyObject obj = new StudyObject(getUri(record), getType(record), 
                getOriginalID(record), getLabel(record), 
                getCohortUri(record), getLabel(record));
        obj.setRoleUri(URIUtils.replacePrefixEx(role));
        obj.addScopeUri(getScopeUri(record));
        return obj;
    }
    
    @Override
    HADatAcThing createObject(Record rec, int row_number) throws Exception {
        return createStudyObject(rec);
    }
	
    @Override
    Map<String, Object> createRow(Record rec, int row_number) throws Exception {
        if (getOriginalID(rec).length() > 0) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("hasURI", getUri(rec));
            if (role.length() > 0){
            	row.put("a", role);	
            }
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
        return "Error in StudyObjectGenerator: " + e.getMessage();
    }
}
