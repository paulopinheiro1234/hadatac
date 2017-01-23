package org.hadatac.console.controllers.metadataacquisition;

import java.io.UnsupportedEncodingException;

import org.hadatac.entity.pojo.Subject;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.deployments.*;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.utils.Collections;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewStudy extends Controller {
	
	//public static Map<String, List<String>> findStudyIndicators(String study_uri) {
	public static Map<String, List<String>> findStudyIndicators(String study_uri) {
		String indicatorQuery="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX case: <http://hadatac.org/ont/case#>PREFIX chear: <http://hadatac.org/ont/chear#>SELECT ?studyIndicator ?label ?comment WHERE { ?studyIndicator rdfs:subClassOf chear:StudyIndicator . ?studyIndicator rdfs:label ?label . ?studyIndicator rdfs:comment ?comment . }";
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		try {
			QueryExecution qexecInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
			ResultSet indicatorResults = qexecInd.execSelect();
			ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
			qexecInd.close();
			while (resultsrwIndc.hasNext()) {
				QuerySolution soln = resultsrwIndc.next();
				indicatorLabel = soln.get("label").toString();
				indicatorMap.put(soln.get("studyIndicator").toString(),indicatorLabel);		
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		Map<String, List<String>> indicatorValues = new HashMap<String, List<String>>();
		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
		    //System.out.println("Key : " + entry.getKey() + " and Value: " + entry.getValue() + "\n");
		    String label = entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "").toString() + "Label";

			String indvIndicatorQuery = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#>PREFIX case: <http://hadatac.org/ont/case#>PREFIX chear-kb: <http://hadatac.org/kb/chear#>PREFIX case-kb: <http://hadatac.org/kb/case#>PREFIX hasco: <http://hadatac.org/ont/hasco/>PREFIX hasneto: <http://hadatac.org/ont/hasneto#>SELECT DISTINCT ?studyUri " +
					"?" + label + " " +
					"WHERE { ?schemaUri hasco:isSchemaOf ?studyUri . ?schemaAttribute hasneto:partOfSchema ?schemaUri . ?schemaAttribute hasneto:hasAttribute " +
					"?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") +
					" . ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:subClassOf* " + entry.getKey().toString().replaceAll("http://hadatac.org/ont/chear#","chear:").replaceAll("http://hadatac.org/ont/case#","case:").replaceAll("http://hadatac.org/kb/chear#","chear-kb:").replaceAll("http://hadatac.org/kb/case#","case-kb:") + 
					" . ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:label ?" + label + " . " +
					"			FILTER ( ?studyUri = " + study_uri.replaceAll("http://hadatac.org/ont/chear#","chear:").replaceAll("http://hadatac.org/ont/case#","case:").replaceAll("http://hadatac.org/kb/chear#","chear-kb:").replaceAll("http://hadatac.org/kb/case#","case-kb:") + " ) . " +
					"}";
			//System.out.println(indvIndicatorQuery + "\n");
			try {
				QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
				ResultSet indvIndResults = qexecIndvInd.execSelect();
				ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
				qexecIndvInd.close();
				List<String> indvIndicatorList = new ArrayList<String>();
				while (resultsrwIndvInd.hasNext()) {
					QuerySolution soln = resultsrwIndvInd.next();
					//System.out.println("Solution: " + soln);
					indvIndicatorList.add(soln.get(label).toString());
					//System.out.println("Indicator String: " + indvIndicatorString);
				}
				indicatorValues.put(entry.getValue().toString(),indvIndicatorList);
			} catch (QueryExceptionHTTP e) {
				e.printStackTrace();
			}
		}
		return indicatorValues;
	}
	
	public static Map<String, List<String>> findBasic(String study_uri) {
		String basicQueryString = "";

		basicQueryString = 
		"PREFIX sio: <http://semanticscience.org/resource/>" + 
		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
		"PREFIX case: <http://hadatac.org/ont/case#>" + 
		"PREFIX chear-kb: <http://hadatac.org/kb/chear#>" + 
		"PREFIX case-kb: <http://hadatac.org/kb/case#>" +
		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + 
		"SELECT ?studyUri ?studyLabel ?proj ?studyDef ?studyComment ?agentName ?institutionName " + 
		" WHERE {        ?subUri rdfs:subClassOf hasco:Study . " + 
		"                       ?studyUri a ?subUri . " + 
		"           ?studyUri rdfs:label ?studyLabel  . " + 
		"			FILTER ( ?studyUri = " + study_uri + " ) . " +
//		"        OPTIONAL { ?studyUri chear-kb:project ?proj. " +
//		"					?proj rdfs:label ?projLabel} . " + 
		"		OPTIONAL {?studyUri hasco:hasProject ?proj} . " +
		"        OPTIONAL { ?studyUri skos:definition ?studyDef } . " + 
		"        OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " + 
		"        OPTIONAL { ?studyUri hasco:hasAgent ?agent . " + 
		"                                   ?agent foaf:name ?agentName } . " + 
		"        OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " + 
		"                                 ?institution foaf:name ?institutionName} . " + 
		"                             }" ;
		Map<String, List<String>> poResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		try {
			Query basicQuery = QueryFactory.create(basicQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
	//		Map<String, String> poResult = new HashMap<String, String>();
	//		System.out.println("HERE IS THE RAW resultsrw*********" + resultsrw);
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
	//			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
				values = new ArrayList<String>();
				values.add("Label: " + soln.get("studyLabel").toString());
				values.add("Title: " + soln.get("studyDef").toString());
				values.add("Project: " + soln.get("proj").toString());
				values.add("Comment: " + soln.get("studyComment").toString());
				values.add("Agent(s): " + soln.get("agentName").toString());
				values.add("Institution: " + soln.get("institutionName").toString());
				poResult.put(soln.get("studyUri").toString(),values);
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return poResult;
	}
	
	public static Map<String, List<String>> findSubject(String study_uri) {

		String subjectQueryString = "";
		
    	subjectQueryString = 
    	"PREFIX sio: <http://semanticscience.org/resource/>" + 
    	"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
		"PREFIX case: <http://hadatac.org/ont/case#>" + 
    	"PREFIX chear-kb: <http://hadatac.org/kb/chear#>" + 
    	"PREFIX case-kb: <http://hadatac.org/kb/case#>" + 
    	"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
    	"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
    	"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
    	"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
    	"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
    	"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
    	"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + 
    	"SELECT ?subjectUri ?subjectType ?subjectLabel ?cohortLabel ?studyLabel " +
    	"			 WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort . " +
    	"			        		?cohort hasco:isCohortOf ?study . " +
    	"							?study rdfs:label ?studyLabel . " +
    	"							?cohort rdfs:label ?cohortLabel ." +
    	"			        		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . " +
    	"			        		OPTIONAL { ?subjectUri a ?subjectType } . " +
    	"			        		FILTER (?study = " + study_uri + ") . " +
    	"			                             }";		
    	Map<String, List<String>> subjectResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		try {
			Query subjectQuery = QueryFactory.create(subjectQueryString);
			QueryExecution qexec2 = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectQuery);
			ResultSet results = qexec2.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec2.close();
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
	//			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
				values = new ArrayList<String>();
				values.add("Label: " + soln.get("subjectLabel").toString());
				values.add("Type: " + soln.get("subjectType").toString());
				values.add("Cohort: " + soln.get("cohortLabel").toString());
				values.add("Study: " + soln.get("studyLabel").toString());
				subjectResult.put(soln.get("subjectUri").toString(),values);		
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return subjectResult;
	}
	
	
	public static String findUser() {
	
	String results = null;
    final SysUser user = AuthApplication.getLocalUser(session());
    if(null == user){
        results = null;
    }
    else{
    	results = UserManagement.getUriByEmail(user.email);
    }
    System.out.println("This is the current user's uri:" + results);
    return results;
	
	}
	
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String study_uri) {
		
		
		Map<String, List<String>> indicatorValues = findStudyIndicators(study_uri);
 //   	Map<String, String> poResult = findBasic(study_uri);
		Map<String, List<String>> poResult = findBasic(study_uri);
		Map<String, List<String>> subjectResult = findSubject(study_uri);
		Map<String, String> showValues = new HashMap<String, String>();
		showValues.put("study", study_uri);
		showValues.put("user", findUser());
        
    	return ok(viewStudy.render(poResult,subjectResult,indicatorValues,showValues));
    
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String study_uri) {

		return index(study_uri);
	}

}
