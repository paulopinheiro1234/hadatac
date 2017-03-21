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
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.utils.Collections;

public class SubjectGenerator {
	final String kbPrefix = "chear-kb:";
	String dataAcquisition = "";
	Iterable<CSVRecord> records = null;
	CSVRecord rec = null;
	int counter = 0;
	
	HashMap<String, Integer> mapCol = new HashMap<String, Integer>();
	
	public SubjectGenerator(File file) {
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
        mapCol.put("subjectID", 0);
        mapCol.put("pilotNum", 3);
	}
	
	private int getSubjectCount(String pilotNum){
		int count=0;
		String subjectCountQuery = DynamicFunctions.getPrefixes() + "SELECT (count(DISTINCT ?subjectURI) as ?subjectCount) WHERE {?sampleURI hasco:isSubjectOf chear-kb:CH-Pilot-" + pilotNum + " . }";
		QueryExecution qexecSubject = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectCountQuery);
		ResultSet subjectResults = qexecSubject.execSelect();
		ResultSetRewindable resultsrwSubject = ResultSetFactory.copyResults(subjectResults);
		QuerySolution soln = resultsrwSubject.next();
		int value = Integer.parseInt(soln.get("subjectCount").toString());
		qexecSubject.close();
		count += value;
		return count;
	}
	
	private String getUri() { 
		return kbPrefix + "SBJ-" + String.format("%04d", counter + getSubjectCount(rec.get(mapCol.get("pilotNum"))))
			+ "-Pilot-" + rec.get(mapCol.get("pilotNum")); 
	}
	private String getType() {
		return "sio:Human";
	}
	private String getLabel() {
		return "ID " + String.format("%04d", counter + getSubjectCount(rec.get(mapCol.get("pilotNum")))) + " - Pilot " 
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