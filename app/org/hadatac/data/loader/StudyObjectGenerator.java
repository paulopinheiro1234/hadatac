package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.utils.ConfigProp;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;


public class StudyObjectGenerator extends BaseGenerator {

    String study_id;
    String file_name;
    String oc_uri;
    String oc_type;
    String oc_scope;
    String oc_timescope;
    private Map<String, ObjectCollection> socMap = new HashMap<String, ObjectCollection>();
    String role;
    final String kbPrefix = ConfigProp.getKbPrefix();
    private List<String> listCache = new ArrayList<String>();
    private Map<String, String> uriMap = new HashMap<String, String>();
    private Map<String, List<String>> mapContent = new HashMap<String, List<String>>();

    public StudyObjectGenerator(DataFile dataFile, List<String> listContent, 
            Map<String, List<String>> mapContent, String study_uri, String study_id) {
        super(dataFile);
        //this.study_id = study_id; 
        //file_name = file.getFile().getName();
        file_name = fileName;
        //System.out.println("We are in StudyObject Generator!");
        //System.out.println("Study URI: " + study_uri);
        
        //study_id = file.getFile().getName().replaceAll("SSD-", "").replaceAll(".xlsx", "");
        //study_id = file_name.replaceAll("SSD-", "").replaceAll(".xlsx", "");
        this.study_id = study_id;
        
        setStudyUri(study_uri);       
        this.listCache = listContent;
        //System.out.println(listContent);
        this.mapContent = mapContent;
        this.oc_uri = listContent.get(0);
        //System.out.println("oc_uri : " + oc_uri);
        this.oc_type = listContent.get(1);
        //System.out.println("oc_type : " + oc_type);
        this.oc_scope = listContent.get(2);
        //System.out.println("oc_scope : " + oc_scope);
        this.oc_timescope = listContent.get(3);
        //System.out.println("oc_timescope : " + oc_timescope);
        this.role = listContent.get(4);
        //System.out.println("role : " + role);
        uriMap.put("hasco:SubjectGroup", "SBJ-");
        uriMap.put("hasco:SampleCollection", "SPL-");
        uriMap.put("hasco:TimeCollection", "TIME-");
        uriMap.put("hasco:LocationCollection", "LOC-");
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
        String originalID = rec.getValueByColumnName(mapCol.get("originalID"));
        //System.out.println("StudyObjectGenerator: " + originalID);
        if (URIUtils.isValidURI(originalID)) {
            //System.out.println("StudyObjectGenerator: VALID URI");
            return URIUtils.replaceNameSpaceEx(originalID);
        }

        //System.out.println("StudyObjectGenerator: " + kbPrefix + uriMap.get(oc_type) + originalID + "-" + study_id);
        return kbPrefix + uriMap.get(oc_type) + originalID + "-" + study_id;
    }

    private String getType(Record rec) {
        return rec.getValueByColumnName(mapCol.get("rdf:type"));
    }

    private String getLabel(Record rec) {
        String originalID = rec.getValueByColumnName(mapCol.get("originalID"));
        if (URIUtils.isValidURI(originalID)) {
            return URIUtils.getBaseName(originalID);
        }
        
        if (getSoc() != null && getSoc().getRoleLabel() != null && !getSoc().getRoleLabel().equals("")) {
    		return getSoc().getRoleLabel() + " " + originalID;
    	}
        
        String auxstr = uriMap.get(oc_type);
        if (auxstr == null) {
            auxstr = "";
        } else {
            auxstr = auxstr.replaceAll("-","");
        }
        
        return auxstr + " " + originalID + " - " + study_id;
    }

    private String getOriginalID(Record rec) {
        String auxstr = rec.getValueByColumnName(mapCol.get("originalID"));
        //System.out.println("StudyObjectGenerator: getOriginalID(1) = [" + auxstr + "]");
        if (auxstr == null) {
            return "";
        } 
        if (URIUtils.isValidURI(auxstr)) {
            return "";
        }
        auxstr = auxstr.replaceAll("\\s+","");
        //System.out.println("StudyObjectGenerator: getOriginalID(2) = [" + auxstr + "]");
        
        //auxstr = auxstr.replaceAll("(?<=^\\d+)\\.0*$", "");
        //System.out.println("StudyObjectGenerator: getOriginalID(3) = [" + auxstr + "]");
        return auxstr;
    }

    private String getSocUri() {
        return oc_uri;
    }

    
    private ObjectCollection getSoc() {
    	if (oc_uri == null || oc_uri.equals("")) {
    		return null;
    	}
    	if (socMap.containsKey(oc_uri)) {
    		return socMap.get(oc_uri);
    	}
    	ObjectCollection soc = ObjectCollection.find(URIUtils.replacePrefixEx(oc_uri));
    	socMap.put(oc_uri, soc);
    	return soc;
    }
    
    private String getScopeUri(Record rec) {
        if (oc_scope != null && oc_scope.length() > 0){
        	if (mapContent.get(oc_scope) != null) {
        		String scopeOCtype = mapContent.get(oc_scope).get(1);
        		return kbPrefix + uriMap.get(scopeOCtype) + rec.getValueByColumnName(mapCol.get("scopeID")).replaceAll("(?<=^\\d+)\\.0*$", "") + "-" + study_id;
        	} else {
        		System.out.println("[ERROR] StudyObjectGenerator: no mapping for [" + oc_scope + "] in getScopeUri()");
        		return "";
        	}
        } else {
        	return "";
        }
    }

    private String getTimeScopeUri(Record rec) {
        if (oc_timescope != null && oc_timescope.length() > 0){
        	if (mapContent.get(oc_timescope) != null) {
        		String timeScopeOCtype = mapContent.get(oc_timescope).get(1);
        		String returnedValue = rec.getValueByColumnName(mapCol.get("timeScopeID"));
        		// the value returned by getValueByColumnName may be an URI or an original.
        		if (URIUtils.isValidURI(returnedValue)) {
        			// if returned value is an URI, this function returns the URI with expanded namespace 
        			return URIUtils.replacePrefixEx(returnedValue);
        		} else {
        			// if returned value is not an URI, this function composes an URI according to SDD convention 
        			return kbPrefix + uriMap.get(timeScopeOCtype) + returnedValue.replaceAll("(?<=^\\d+)\\.0*$", "") + "-" + study_id;
        		}
        	} else {
        		System.out.println("[ERROR] StudyObjectGenerator: no mapContent for [" + oc_timescope + "] in getTimeScopeUri(). Record is " + rec);
        		return "";
        	}
        } else {
            return "";
        }
    }
    
    public StudyObject createStudyObject(Record record) throws Exception {
    	if (getOriginalID(record) == null || getOriginalID(record).isEmpty()) {
    		return null;
    	}
    	StudyObject obj = new StudyObject(getUri(record), getType(record), 
					  getOriginalID(record), getLabel(record), 
					  getSocUri(), getLabel(record));
        obj.setRoleUri(URIUtils.replacePrefixEx(role));
        obj.addScopeUri(getScopeUri(record));
        //System.out.println("StudyObjectGenerator: createStudyObject calling getTimeScopeUri. original ID [" + getOriginalID(record) + "]");
        //System.out.println("StudyObjectGenerator: value of time scope [" + record.getValueByColumnName(mapCol.get("timeScopeID")).replaceAll("(?<=^\\d+)\\.0*$", "") + "]");
        //System.out.println("StudyObjectGenerator: value of oc_timescope [" + oc_timescope + "]");
        //System.out.println("StudyObjectGenerator: value of mapContent.get(oc_timescope) [" + mapContent.get(oc_timescope) + "]");
        List<String> l = new ArrayList<String>(mapContent.keySet());
        //for (String str : l) {
        //	System.out.println("StudyObjectGenerator: mapContent's key [" + str + "]");
        //}
        obj.addTimeScopeUri(getTimeScopeUri(record));
        
        return obj;
    }

    @Override
    public HADatAcThing createObject(Record rec, int rowNumber, String selector) throws Exception {
        return createStudyObject(rec);
    }

    @Override
    public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {
        if (getOriginalID(rec).length() > 0) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("hasURI", getUri(rec));
            return row;
        }
        
        return null;
    }

    @Override
    public void preprocess() throws Exception {}

    @Override
    public String getTableName() {
        return "StudyObject";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in StudyObjectGenerator: " + e.getMessage();
    }
}
