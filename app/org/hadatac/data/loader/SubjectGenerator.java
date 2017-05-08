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
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import com.google.common.collect.Iterables;

public class SubjectGenerator {
	final String kbPrefix = "chear-kb:";
	private Iterable<CSVRecord> records = null;
	private CSVRecord rec = null;
	private int counter = 1; //starting index number
	private List< Map<String, Object> > rows = new ArrayList<Map<String, Object>>();
	private HashMap<String, String> mapCol = new HashMap<String, String>();
	
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
        mapCol.put("subjectID", "patient_id");
        mapCol.put("pilotNum", "project_id");
	}
	
	private int getSubjectCount(String pilotNum){
		int count = 0;
		String subjectCountQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT (count(DISTINCT ?subjectURI) as ?subjectCount) WHERE { "
				+ " ?subjectURI hasco:isSubjectOf chear-kb:CH-Pilot-" + pilotNum + " . "
				+ " }";
		QueryExecution qexecSubject = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectCountQuery);
		ResultSet subjectResults = qexecSubject.execSelect();
		ResultSetRewindable resultsrwSubject = ResultSetFactory.copyResults(subjectResults);
		qexecSubject.close();
		
		if (resultsrwSubject.hasNext()) {
			QuerySolution soln = resultsrwSubject.next();
			Literal countLiteral = (Literal) soln.get("subjectCount");
			if(countLiteral != null){
				count += countLiteral.getInt();
			}
		}
		
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
    	return kbPrefix + "STD-Pilot-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCohortUri() {
    	return kbPrefix + "CH-Pilot-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCohortLabel() {
    	return "Cohort of Pilot Study " + rec.get(mapCol.get("pilotNum"));
    }
    
    public Map<String, Object> createRow() {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri());
    	row.put("a", getType());
    	row.put("rdfs:label", getLabel());
    	row.put("hasco:originalID", getOriginalID());
    	row.put("hasco:isSubjectOf", getCohortUri());
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
    
    public Map<String, Object> createCohortRow() {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getCohortUri());
    	row.put("a", "hasco:Cohort");
    	row.put("rdfs:label", getCohortLabel());
    	row.put("hasco:hasSize", Integer.toString(Iterables.size(records)+1));
    	row.put("hasco:isCohortOf", getStudyUri());
    	counter++;
    	
    	return row;
    }
    
    public List< Map<String, Object> > createCohortRows() {
    	for (CSVRecord record : records) {
    		rec = record;
    		rows.add(createCohortRow());
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