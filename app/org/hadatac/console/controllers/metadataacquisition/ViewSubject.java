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
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.deployments.*;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.utils.Collections;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewSubject extends Controller {

	public static Map<String, String> findSubjectIndicators(String subject_uri) {
		String indicatorQuery="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX case: <http://hadatac.org/ont/case#>PREFIX chear: <http://hadatac.org/ont/chear#>SELECT ?subjectIndicator ?label ?comment WHERE { ?subjectIndicator rdfs:subClassOf chear:subjectIndicator . ?subjectIndicator rdfs:label ?label . ?subjectIndicator rdfs:comment ?comment . }";
		QueryExecution qexecInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
		ResultSet indicatorResults = qexecInd.execSelect();
		ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
		qexecInd.close();
		
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		while (resultsrwIndc.hasNext()) {
			QuerySolution soln = resultsrwIndc.next();
			indicatorLabel = soln.get("label").toString();
			indicatorMap.put(soln.get("subjectIndicator").toString(),indicatorLabel);		
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		
		Map<String, String> indicatorValues = new HashMap<String, String>();
		
		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
		    //System.out.println("Key : " + entry.getKey() + " and Value: " + entry.getValue() + "\n");
		    String label = entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "").toString() + "Label";

			String indvIndicatorQuery = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#>PREFIX case: <http://hadatac.org/ont/case#>PREFIX chear-kb: <http://hadatac.org/kb/chear#>PREFIX case-kb: <http://hadatac.org/kb/case#>PREFIX hasco: <http://hadatac.org/ont/hasco/>PREFIX hasneto: <http://hadatac.org/ont/hasneto#>SELECT DISTINCT ?subjectUri " +
					"?" + label + " " +
					"WHERE { ?schemaUri hasco:isSchemaOf ?subjectUri . ?schemaAttribute hasneto:partOfSchema ?schemaUri . ?schemaAttribute hasneto:hasAttribute " +
					"?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") +
					" . ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:subClassOf* " + entry.getKey().toString().replaceAll("http://hadatac.org/ont/chear#","chear:").replaceAll("http://hadatac.org/ont/case#","case:").replaceAll("http://hadatac.org/kb/chear#","chear-kb:").replaceAll("http://hadatac.org/kb/case#","case-kb:") + 
					" . ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:label ?" + label + " . " +
					"			FILTER ( ?subjectUri = " + subject_uri.replaceAll("http://hadatac.org/ont/chear#","chear:").replaceAll("http://hadatac.org/ont/case#","case:").replaceAll("http://hadatac.org/kb/chear#","chear-kb:").replaceAll("http://hadatac.org/kb/case#","case-kb:") + " ) . " +
					"}";
			//System.out.println(indvIndicatorQuery + "\n");
			QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
			ResultSet indvIndResults = qexecIndvInd.execSelect();
			ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
			qexecIndvInd.close();
			String indvIndicatorString="";
			while (resultsrwIndvInd.hasNext()) {
				QuerySolution soln = resultsrwIndvInd.next();
				//System.out.println("Solution: " + soln);
				indvIndicatorString += soln.get(label).toString() + ", ";
				//System.out.println("Indicator String: " + indvIndicatorString);
			}
			if (indvIndicatorString != ""){
				indvIndicatorString = indvIndicatorString.substring(0, indvIndicatorString.length()-2);
				indicatorValues.put(entry.getValue().toString(),indvIndicatorString);
			}
		}
		return indicatorValues;
	}
	
	public static Map<String, List<String>> findBasic(String subject_uri) {

		String subjectQueryString = "";
		
    	subjectQueryString = 
    	"PREFIX sio: <http://semanticscience.org/resource/>" + 
    	"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
    	"PREFIX chear-kb: <http://hadatac.org/kb/chear#>" + 
    	"PREFIX case: <http://hadatac.org/ont/case#>" + 
    	"PREFIX case-kb: <http://hadatac.org/kb/case#>" + 
    	"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
    	"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
    	"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
    	"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
    	"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
    	"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
    	"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + 
    	"SELECT ?subjectUri ?subjectTypeLabel ?subjectLabel ?cohortLabel ?studyLabel " +
    	"			 WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort . " +
    	"							?study rdfs:label ?studyLabel ." +
    	"			        		?cohort hasco:isCohortOf ?study . " +
    	"							?cohort rdfs:label ?cohortLabel" +
    	"			        		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . " +
    	"			        		OPTIONAL { ?subjectUri a ?subjectType . " +
    	"										?subjectType rdfs:label ?subjectTypeLabel} . " +
    	"			        		FILTER (?subjectUri = " + subject_uri + ") . " +
    	"			                             }";
		
	/*	
		String basicQueryString = "";

		basicQueryString = 
    	
    	"PREFIX prov: <http://www.w3.org/ns/prov#> "
        + " PREFIX chear-kb: <http://hadatac.org/kb/chear#> "
        + "SELECT * "
        + "WHERE  {	" + subject_uri + " ?p ?o }";
    */	
    	
        
		//Query basicQuery = QueryFactory.create(basicQueryString);
    	Query basicQuery = QueryFactory.create(subjectQueryString);
    	
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Map<String, List<String>> subjectResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values = new ArrayList<String>();
			values.add("Label: " + soln.get("subjectLabel").toString());
			values.add("Type: " + soln.get("subjectTypeLabel").toString());
			values.add("Cohort: " + soln.get("cohortLabel").toString());
			values.add("Study: " + soln.get("studyLabel").toString());
			subjectResult.put(soln.get("subjectUri").toString(),values);	
			System.out.println("THIS IS SUBROW*********" + subjectResult);	
		}
/*		
		Map<String, String> poResult = new HashMap<String, String>();
		System.out.println("HERE IS THE RAW resultsrw*********" + resultsrw);
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			poResult.put(soln.get("p").toString(), soln.get("o").toString());
//			System.out.println("THIS IS SUBROW*********" + poResult);

		}*/
		return subjectResult;
	}
	public static Map<String, List<String>> findSampleMap(String subject_uri) {

		String sampleQueryString = "";
		
    	sampleQueryString = 
    	"PREFIX sio: <http://semanticscience.org/resource/>" + 
    	"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
    	"PREFIX chear-kb: <http://hadatac.org/kb/chear#>" + 
    	"PREFIX case: <http://hadatac.org/ont/case#>" + 
    	"PREFIX case-kb: <http://hadatac.org/kb/case#>" + 
    	"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
    	"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
    	"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
    	"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
    	"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
    	"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
    	"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + 
    	"SELECT ?sampleUri ?subjectUri ?subjectLabel ?sampleType ?sampleLabel ?cohortLabel ?comment" +
		 "WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort ." +
		 "       		?sampleUri hasco:isSampleOf ?subjectUri ." +
		 "				?sampleUri rdfs:comment ?comment . " +
		 "				?cohort rdfs:label ?cohortLabel . " +
		 "       		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } .  " + 
		 "       		OPTIONAL { ?sampleUri rdfs:label ?sampleLabel } .  " + 
		 "       		OPTIONAL { ?sampleUri a ?sampleType  } .  " +
         "      FILTER (?subjectUri = " + subject_uri + " ) .  " +
		 "                            }";
	/*	
		String basicQueryString = "";

		basicQueryString = 
    	
    	"PREFIX prov: <http://www.w3.org/ns/prov#> "
        + " PREFIX chear-kb: <http://hadatac.org/kb/chear#> "
        + "SELECT * "
        + "WHERE  {	" + subject_uri + " ?p ?o }";
    */	
    	
        
		//Query basicQuery = QueryFactory.create(basicQueryString);
    	Query basicQuery = QueryFactory.create(sampleQueryString);
    	
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Map<String, List<String>> sampleResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values = new ArrayList<String>();
			values.add("Label: " + soln.get("sampleLabel").toString());
			values.add("Type: " + soln.get("sampleType").toString());
			values.add("Sample Of: " + soln.get("subjectLabel").toString());
			//values.add("Comment: " + soln.get("comment").toString());
			sampleResult.put(soln.get("sampleUri").toString(),values);	
			System.out.println("THIS IS SUBROW*********" + sampleResult);	
		}

		return sampleResult;
	}
	
	public static List<String> findSample(String subject_uri) {

		String sampleQueryString = "";
		
    	sampleQueryString = 
    	    	
    	"PREFIX prov: <http://www.w3.org/ns/prov#> "
        + " PREFIX chear-kb: <http://hadatac.org/kb/chear#> "
        + " PREFIX case-kb: <http://hadatac.org/kb/case#> "
        + "SELECT * "
        + "WHERE { ?s <http://hadatac.org/ont/hasco/isSampleOf> " + subject_uri + " }";
        
		Query sampleQuery = QueryFactory.create(sampleQueryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), sampleQuery);
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
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String subject_uri) {

		Map<String, String> indicatorValues = findSubjectIndicators(subject_uri);
    	Map<String, List<String>> subjectResult = findBasic(subject_uri);
    	//List<String> sampleResult = findSample(subject_uri);
    	Map<String, List<String>> sampleResult = findSampleMap(subject_uri);
    	
    	return ok(viewSubject.render(subjectResult,sampleResult,indicatorValues));
    
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String subject_uri) {

		return index(subject_uri);
	}

}
