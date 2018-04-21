package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.ObjectCollection;


public class SSDGenerator extends BasicGenerator {
    static final long MAX_OBJECTS = 1000;
    static final long LENGTH_CODE = 6;

    final String kbPrefix = ConfigProp.getKbPrefix();
    
    public SSDGenerator(RecordFile file) {
        super(file);
    }

    @Override
    void initMapping() {
        mapCol.clear();
        mapCol.put("sheet", "sheet");
        mapCol.put("uri", "hasURI");
        mapCol.put("typeUri", "type");
        mapCol.put("label", "label");
        mapCol.put("studyUri", "isMemberOf");
        mapCol.put("hasScopeUri", "hasScope");
        mapCol.put("spaceScopeUris", "hasSpaceScope");
        mapCol.put("timeScopeUris", "hasTimeScope");
    }

    private String getUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("uri"));
    }

    private String getTypeUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("typeUri"));
    }
    
    private String getLabel(Record rec) {
        return rec.getValueByColumnName(mapCol.get("label"));
    }

    private String getStudyUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("studyUri"));
    }

    private String gethasScopeUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("hasScopeUri"));
    }

    private List<String> getSpaceScopeUris(Record rec) {
    	List<String> ans = new ArrayList<String>(Arrays.asList(rec.getValueByColumnName(mapCol.get("spaceScopeUris")).split(",")));
    	return ans;
    }
    
    private List<String> getTimeScopeUris(Record rec) {
    	List<String> ans = new ArrayList<String>(Arrays.asList(rec.getValueByColumnName(mapCol.get("timeScopeUris")).split(",")));
    	return ans;
    }

    public ObjectCollection createObjectCollection(Record record) throws Exception {
    	ObjectCollection oc = new ObjectCollection( getUri(record),
    			getTypeUri(record),
    			getLabel(record),
    			getLabel(record),
    			getStudyUri(record),
    			gethasScopeUri(record),
                getSpaceScopeUris(record),
                getTimeScopeUris(record)
                );
//    	oc.save();
    	oc.saveToTripleStore();
        return oc;
    }

    @Override
    public void preprocess() throws Exception {
        System.out.println("records: " + records);
    	System.out.println(getUri(records.get(0)));
    	String studyUri = getUri(records.get(0));
        if (!records.isEmpty()) {
        	Iterator<Record> iter = records.iterator();
        	while(iter.hasNext()){
        		Record rec = iter.next();
        		if(getUri(rec) != studyUri){
            		createObjectCollection(rec);
        		}
        	}
        }
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in SSDGenerator: " + e.getMessage();
    }

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return null;
	}
}
