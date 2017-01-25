package org.hadatac.console.controllers.metadataacquisition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewSubject extends Controller {

	public static Map<String, List<String>> findSubjectIndicators(String subject_uri) {
		String indicatorQuery = "";
		indicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
		indicatorQuery += "SELECT ?subjectIndicator ?label ?comment WHERE { "
				+ "?subjectIndicator rdfs:subClassOf chear:subjectIndicator . "
				+ "?subjectIndicator rdfs:label ?label . "
				+ "?subjectIndicator rdfs:comment ?comment . }";
		QueryExecution qexecInd = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
		ResultSet indicatorResults = qexecInd.execSelect();
		ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
		qexecInd.close();
		
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		while (resultsrwIndc.hasNext()) {
			QuerySolution soln = resultsrwIndc.next();
			indicatorLabel = soln.get("label").toString();
			indicatorMap.put(soln.get("subjectIndicator").toString(), indicatorLabel);		
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		Map<String, List<String>> indicatorValues = new HashMap<String, List<String>>();
		
		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
			String parentIndicatorUri = entry.getKey();
			String indvIndicatorQuery = "";
			indvIndicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
			indvIndicatorQuery += "SELECT DISTINCT ?subjectUri ?label WHERE { "
					+ "?subjectUri hasco:isSubjectOf* ?cohort . "
					+ "?cohort hasco:isCohortOf ?study . "
					+ "?schemaUri hasco:isSchemaOf ?study . "
					+ "?schemaAttribute hasneto:partOfSchema ?schemaUri . "
					+ "?schemaAttribute hasneto:hasAttribute ?uri . "
					+ "?uri rdfs:subClassOf* <" + parentIndicatorUri + "> . " 
					+ "?uri rdfs:label ?label . " 
					+ "FILTER ( ?subjectUri = " + subject_uri + " ) . " 
					+ "}";
			QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
			ResultSet indvIndResults = qexecIndvInd.execSelect();
			ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
			qexecIndvInd.close();
			
			List<String> listIndicatorLabel = new ArrayList<String>();
			while (resultsrwIndvInd.hasNext()) {
				QuerySolution soln = resultsrwIndvInd.next();
				listIndicatorLabel.add(soln.get("label").toString());
			}
			indicatorValues.put(entry.getValue().toString(), listIndicatorLabel);
		}
		
		return indicatorValues;
	}
	
	public static Map<String, List<String>> findBasic(String subject_uri) {
		String subjectQueryString = "";
    	subjectQueryString += NameSpaces.getInstance().printSparqlNameSpaceList();
    	subjectQueryString += "SELECT ?subjectUri ?subjectTypeLabel ?subjectLabel ?cohortLabel ?studyLabel WHERE { "
    			+ "?subjectUri hasco:isSubjectOf* ?cohort . "
    			+ "?study rdfs:label ?studyLabel . "
    			+ "?cohort hasco:isCohortOf ?study . "
    			+ "?cohort rdfs:label ?cohortLabel . "
    			+ "OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . "
    			+ "OPTIONAL { ?subjectUri a ?subjectType . "
    			+ "			  ?subjectType rdfs:label ?subjectTypeLabel } . "
    			+ "FILTER ( ?subjectUri = " + subject_uri + " ) . "
    			+ "}";
		
    	Query basicQuery = QueryFactory.create(subjectQueryString);
    	
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Map<String, List<String>> subjectResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		
		ValueCellProcessing cellProc = new ValueCellProcessing();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values = new ArrayList<String>();
			values.add("Label: " + soln.get("subjectLabel").toString());
			values.add("Type: " + soln.get("subjectTypeLabel").toString());
			values.add("Cohort: " + soln.get("cohortLabel").toString());
			values.add("Study: " + soln.get("studyLabel").toString());
			subjectResult.put(cellProc.replaceNameSpaceEx(soln.get("subjectUri").toString()), values);	
			System.out.println("THIS IS SUBROW*********" + subjectResult);	
		}
		
		return subjectResult;
	}
	
	public static Map<String, List<String>> findSampleMap(String subject_uri) {
		String sampleQueryString = "";
    	sampleQueryString += NameSpaces.getInstance().printSparqlNameSpaceList();
    	sampleQueryString += "SELECT ?sampleUri ?subjectUri ?subjectLabel ?sampleType ?sampleLabel ?cohortLabel ?comment WHERE { "
		    	+ "?subjectUri hasco:isSubjectOf* ?cohort . "
		    	+ "?sampleUri hasco:isSampleOf ?subjectUri . "
		    	+ "?sampleUri rdfs:comment ?comment . "
		    	+ "?cohort rdfs:label ?cohortLabel . "
		    	+ "OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . "
		    	+ "OPTIONAL { ?sampleUri rdfs:label ?sampleLabel } . "
		    	+ "OPTIONAL { ?sampleUri a ?sampleType  } . "
		    	+ "FILTER ( ?subjectUri = " + subject_uri + " ) . "
		    	+ "}";
    	Query basicQuery = QueryFactory.create(sampleQueryString);
    	
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Map<String, List<String>> sampleResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		
		ValueCellProcessing cellProc = new ValueCellProcessing();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values = new ArrayList<String>();
			values.add("Label: " + soln.get("sampleLabel").toString());
			values.add("Type: " + cellProc.replaceNameSpaceEx(soln.get("sampleType").toString()));
			values.add("Sample Of: " + cellProc.replaceNameSpaceEx(soln.get("subjectLabel").toString()));
			sampleResult.put(cellProc.replaceNameSpaceEx(soln.get("sampleUri").toString()), values);
			System.out.println("THIS IS SUBROW*********" + sampleResult);	
		}

		return sampleResult;
	}
	
	public static List<String> findSample(String subject_uri) {
		String sampleQueryString = "";
    	sampleQueryString += NameSpaces.getInstance().printSparqlNameSpaceList();
    	sampleQueryString += "SELECT * WHERE { "
    			+ "?s <http://hadatac.org/ont/hasco/isSampleOf> " + subject_uri + " . "
    			+ "}";
        
		Query sampleQuery = QueryFactory.create(sampleQueryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), sampleQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		List<String> sampleResult = new ArrayList<String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE SAMPLES*********" + soln.toString());
			sampleResult.add(soln.get("s").toString());
			System.out.println("THIS IS SUBROW*********" + sampleResult);
		}
		return sampleResult;
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String subject_uri) {
		Map<String, List<String>> indicatorValues = findSubjectIndicators(subject_uri);
    	Map<String, List<String>> subjectResult = findBasic(subject_uri);
    	Map<String, List<String>> sampleResult = findSampleMap(subject_uri);
    	
    	return ok(viewSubject.render(subjectResult, sampleResult, indicatorValues));    
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String subject_uri) {
		return index(subject_uri);
	}
}
