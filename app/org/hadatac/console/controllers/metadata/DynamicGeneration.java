package org.hadatac.console.controllers.metadata;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.mvc.Controller;
import play.mvc.Result;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.utils.Collections;


public class DynamicGeneration extends Controller {
	
//	public static Map<String, String> findBasic(String study_uri) {
	public static Map<String, List<String>> findStudy(String study_uri) {
		String studyQueryString = "";

		studyQueryString = 
		"PREFIX sio: <http://semanticscience.org/resource/>" + 
		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
		"PREFIX chear-kb: <http://hadatac.org/kb/chear#>" + 
		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + 
		"SELECT ?studyUri ?studyLabel ?proj ?studyDef ?studyComment ?agentName ?institutionName " + 
		" WHERE {        ?subUri rdfs:subClassOf* hasco:Study . " + 
		"                       ?studyUri a ?subUri . " + 
		"           ?studyUri rdfs:label ?studyLabel  . " + 
		"			FILTER ( ?studyUri = " + study_uri + " ) . " +
		"        OPTIONAL { ?studyUri chear-kb:project ?proj } . " + 
		"        OPTIONAL { ?studyUri skos:definition ?studyDef } . " + 
		"        OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " + 
		"        OPTIONAL { ?studyUri hasco:hasAgent ?agent . " + 
		"                                   ?agent foaf:name ?agentName } . " + 
		"        OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " + 
		"                                 ?institution foaf:name ?institutionName} . " + 
		"                             }" ;
     
		Query studyQuery = QueryFactory.create(studyQueryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), studyQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		Map<String, List<String>> studyResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
//		Map<String, String> poResult = new HashMap<String, String>();
		System.out.println("HERE IS THE RAW resultsrw*********" + resultsrw);
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values.add("Label: " + soln.get("studyLabel").toString());
			values.add("Title: " + soln.get("studyDef").toString());
			values.add("Project: " + soln.get("proj").toString());
			values.add("Comment: " + soln.get("studyComment").toString());
			values.add("Agent(s): " + soln.get("agentName").toString());
			values.add("Institution: " + soln.get("institutionName").toString());
			studyResult.put(soln.get("studyUri").toString(),values);
			
		}
		return studyResult;
	}
	
	public static Map<String, List<String>> findSubject(String study_uri) {

		String subjectQueryString = "";
		
    	subjectQueryString = 
    	"PREFIX sio: <http://semanticscience.org/resource/>" + 
    	"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
    	"PREFIX chear-kb: <http://hadatac.org/kb/chear#>" + 
    	"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
    	"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
    	"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
    	"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
    	"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
    	"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
    	"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + 
    	"SELECT ?subjectUri ?subjectType ?subjectLabel ?cohort ?study " +
    	"			 WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort . " +
    	"			        		?cohort hasco:isCohortOf ?study . " +
    	"			        		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . " +
    	"			        		OPTIONAL { ?subjectUri a ?subjectType } . " +
    	"			        		FILTER (?study = " + study_uri + ") . " +
    	"			                             }";		
        
		Query subjectQuery = QueryFactory.create(subjectQueryString);
		
		QueryExecution qexec2 = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectQuery);
		ResultSet results = qexec2.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec2.close();
		Map<String, List<String>> subjectResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values.add("Label: " + soln.get("subjectLabel").toString());
			values.add("Type: " + soln.get("subjectType").toString());
			values.add("Cohort: " + soln.get("cohort").toString());
			values.add("Study: " + soln.get("study").toString());
			subjectResult.put(soln.get("subjectUri").toString(),values);
			
		}
		
		return subjectResult;
	}
	
	// for /metadata HTTP GET requests
    public static Result index(String study_uri) {
    	
		Map<String, List<String>> studyResult = findStudy(study_uri);
		Map<String, List<String>> subjectResult = findSubject(study_uri);
        
        return ok(viewStudy.render(studyResult,subjectResult));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex(String study_uri) {
        
        return index(study_uri);
        
    }// /postIndex()

}
