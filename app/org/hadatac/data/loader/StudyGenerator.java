package org.hadatac.data.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class StudyGenerator {
	final String kbPrefix = "chear-kb:";
	private Iterable<CSVRecord> records = null;
	private CSVRecord rec = null;
	private int counter = 1; //starting index number
	private List< Map<String, Object> > rows = new ArrayList<Map<String, Object>>();
	private HashMap<String, String> mapCol = new HashMap<String, String>();
	
	public StudyGenerator(File file) {
		try {
			records = CSVFormat.DEFAULT.withHeader().parse(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initMapping();
	}
	
	private void initMapping() {
		mapCol.clear();
        mapCol.put("studyID", "CHEAR Project ID");
        mapCol.put("studyTitle", "Title");
        mapCol.put("studyAims", "Specific Aims");
        mapCol.put("studySignificance", "Significance");
        mapCol.put("numSubjects", "Number of Participants");
        mapCol.put("numSamples", "Number of Sample IDs");
        mapCol.put("institution", "Institution");
        mapCol.put("PI", "Principal Investigator");
        mapCol.put("PIAddress", "PI Address");
        mapCol.put("PICity", "PI City");
        mapCol.put("PIState", "PI State");
        mapCol.put("PIZipCode", "PI Zip Code");
        mapCol.put("PIEmail", "Email");
        mapCol.put("PIPhone", "PI Phone");
        mapCol.put("DCAccessBool", "DC Access?");
	}
	
	private String getUri() { 
		return kbPrefix + "STD-" + rec.get(mapCol.get("studyID")); 
	}
	
	private String getTitle() { 
		return rec.get(mapCol.get("studyTitle")); 
	}
	
	private String getAims() { 
		return rec.get(mapCol.get("studyAims")) ; 
	}
	
	private String getSignificance() { 
		return rec.get(mapCol.get("studySignificance")) ; 
	}
	
	private String getInstitution() {
		return kbPrefix + "ORG-" + rec.get(mapCol.get("institution")).replaceAll(" ", "-"); 
	}
	
	private String getAgent() {
		return kbPrefix + "PER-" + rec.get(mapCol.get("PI")).replaceAll(" ", "-"); 
	}
	
	private String getType() {
		return "hasco:Study";
	}
	
    
    public Map<String, Object> createRow() {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri());
    	row.put("a", getType());
    	row.put("rdfs:label", getTitle());
    	row.put("skos:definition", getAims());
    	row.put("rdfs:comment", getSignificance());
    	row.put("hasco:hasAgent", getAgent());
    	row.put("hasco:hasInstitution", getInstitution());
    	counter++;
    	
    	return row;
    }
    
    public List< Map<String, Object> > createRows() {
    	for (CSVRecord record : records) {
    		rec = record;
    		rows.add(createRow());
    	}

    	return rows;
    }
    
    public String toString() {
    	if(rows.isEmpty()){
    		return "";
    	}
    	
    	String result = "";
    	result = String.join(",", rows.get(0).keySet());
    	for (Map<String, Object> row : rows) {
    		List<String> values = new ArrayList<String>();
    		for (String colName : rows.get(0).keySet()) {
    			if (row.containsKey(colName)) {
    				values.add((String)row.get(colName));
    			}
    			else {
    				values.add("");
    			}
    		}
    		result += "\n";
    		result += String.join(",", values);
    	}
    	
    	return result;
    }
}