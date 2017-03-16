package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class SubjectGenerator {
	final String kbPrefix = "chear-kb:";
	String dataAcquisition = "";
	Iterable<CSVRecord> records = null;
	CSVRecord rec = null;
	int counter = 0;
	
	HashMap<String, Integer> mapCol = new HashMap<String, Integer>();
	
	public SubjectGenerator(Iterable<CSVRecord> records) {
		initMapping();
	}
	
	private void initMapping() {
		mapCol.clear();
        mapCol.put("subjectID", 0);
        mapCol.put("pilotNum", 3);
	}
	
	private String getUri() { 
		return kbPrefix + "SBJ-" + String.format("%04d", counter) 
			+ "-Pilot-" + rec.get(mapCol.get("pilotNum")); 
	}
	private String getType() {
		return "sio:Human";
	}
	private String getLabel() {
		return "ID " + String.format("%04d", counter) + " - Pilot " 
			+ rec.get(mapCol.get("pilotNum"));
	}
    private String getOriginalID() {
    	return rec.get(mapCol.get("subjectID"));
    }
    private String getStudyUri() {
    	return kbPrefix + "CH-Pilot-" + rec.get(mapCol.get("pilotNum"));
    }
    
    public Map<String, Object> createRow() {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri());
    	row.put("a", getType());
    	row.put("rdfs:label", getLabel());
    	row.put("hasco:originalID", getOriginalID());
    	row.put("hasco:isSubjectOf", getStudyUri());
    	counter++;
    	
    	return row;
    }
    
    public List< Map<String, Object> > createRows() {
    	List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
    	for (CSVRecord record : records) {
    		rec = record;
    		rows.add(createRow());
    	}
    	
    	return rows;
    }
}