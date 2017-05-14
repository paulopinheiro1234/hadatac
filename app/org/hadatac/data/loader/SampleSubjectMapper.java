package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class SampleSubjectMapper extends BasicGenerator {
	
	public SampleSubjectMapper(File file) {
		super(file);
	}
	
	@Override
	void initMapping() {
		mapCol.clear();
        mapCol.put("originalPID", "patient_id");
        mapCol.put("originalSID", "specimen_id");
	}
	
	private String getSampleUri(CSVRecord rec) {
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
	
	private String getSubjectUri(CSVRecord rec) {
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
	
	@Override
	Map<String, Object> createRow(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getSampleUri(rec));
    	row.put("hasco:isSampleOf", getSubjectUri(rec));
    	return row;
    }
}
