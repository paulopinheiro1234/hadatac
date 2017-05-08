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
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class SampleSubjectMapper {
	private Iterable<CSVRecord> records = null;
	private CSVRecord rec = null;
	private List< Map<String, Object> > rows = new ArrayList<Map<String, Object>>();
	private HashMap<String, String> mapCol = new HashMap<String, String>();
	
	public SampleSubjectMapper(File file) {
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
        mapCol.put("originalPID", "patient_id");
        mapCol.put("originalSID", "specimen_id");
	}
	
	private String getSampleUri() {
		String sampleUri = "";
		String sampleQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT ?s WHERE {" +
				"?s hasco:originalID \"" + rec.get(mapCol.get("originalSID")) + "\"." +
				"}";
		try {
			Query sampleQuery = QueryFactory.create(sampleQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), sampleQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
	        if(resultsrw.hasNext()) {
	            QuerySolution soln = resultsrw.next();
	            if(soln.contains("s")){
	            	sampleUri = DynamicFunctions.replaceURLWithPrefix(soln.get("s").toString());
	            }
	        }
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		System.out.println("Sample:" + sampleUri);
		return sampleUri;
	}
	
	private String getSubjectUri() {
		String subjectUri = "";
		String subjectQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT ?s WHERE {" +
				"?s hasco:originalID \"" + rec.get(mapCol.get("originalPID")) + "\"." +
				"}";
		try {
			Query subjectQuery = QueryFactory.create(subjectQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
	        if(resultsrw.hasNext()) {
	            QuerySolution soln = resultsrw.next();
	            if(soln.contains("s")){
	            	subjectUri = DynamicFunctions.replaceURLWithPrefix(soln.get("s").toString());
	            }
	        }
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		System.out.println("Subject:" + subjectUri);
		return subjectUri;
	}
	
	public Map<String, Object> createRow() {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getSampleUri());
    	row.put("hasco:isSampleOf", getSubjectUri());
    	return row;
    }
    
    public List< Map<String, Object> > createRows() {
    	for (CSVRecord record : records) {
    		rec = record;
    		rows.add(createRow());
    	}

    	return rows;
    }
}
