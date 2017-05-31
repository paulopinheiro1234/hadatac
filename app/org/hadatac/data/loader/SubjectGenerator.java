package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.google.common.collect.Iterables;

public class SubjectGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	private int counter = 1; //starting index number
	
	public SubjectGenerator(File file) {
		super(file);
	}
	
	@Override
	void initMapping() {
		mapCol.clear();
        mapCol.put("subjectID", "patient_id");
        mapCol.put("pilotNum", "project_id");
	}
	
	private int getSubjectCount(String pilotNum){
		int count = 0;
		String subjectCountQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT (count(DISTINCT ?subjectURI) as ?subjectCount) WHERE { "
				+ " ?subjectURI hasco:isSubjectOf chear-kb:CH-" + pilotNum + " . "
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
	
	private String getUri(CSVRecord rec) { 
		return kbPrefix + "SBJ-" + String.format("%04d", counter + getSubjectCount(rec.get(mapCol.get("pilotNum"))))
			+ "-" + rec.get(mapCol.get("pilotNum")); 
	}
	private String getType() {
		return "sio:Human";
	}
	private String getLabel(CSVRecord rec) {
		return "ID " + String.format("%04d", counter + getSubjectCount(rec.get(mapCol.get("pilotNum")))) + " - " 
			+ rec.get(mapCol.get("pilotNum"));
	}
    private String getOriginalID(CSVRecord rec) {
    	return rec.get(mapCol.get("subjectID"));
    }
    
    private String getStudyUri(CSVRecord rec) {
    	return kbPrefix + "STD-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCohortUri(CSVRecord rec) {
    	return kbPrefix + "CH-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCohortLabel(CSVRecord rec) {
    	return "Cohort of Study " + rec.get(mapCol.get("pilotNum"));
    }
    
    @Override
    Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri(rec));
    	row.put("a", getType());
    	row.put("rdfs:label", getLabel(rec));
    	row.put("hasco:originalID", getOriginalID(rec));
    	row.put("hasco:isSubjectOf", getCohortUri(rec));
    	counter++;
    	
    	return row;
    }
    
    public Map<String, Object> createCohortRow(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getCohortUri(rec));
    	row.put("a", "hasco:Cohort");
    	row.put("rdfs:label", getCohortLabel(rec));
    	row.put("hasco:hasSize", Integer.toString(Iterables.size(records)+1));
    	row.put("hasco:isCohortOf", getStudyUri(rec));
    	counter++;
    	
    	return row;
    }
    
    public List< Map<String, Object> > createCohortRows() {
    	rows.clear();
    	for (CSVRecord record : records) {
    		rows.add(createCohortRow(record));
    	}
    	return rows;
    }
}