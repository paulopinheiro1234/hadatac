package org.hadatac.console.controllers.metadataacquisition;

import java.io.UnsupportedEncodingException;

import org.hadatac.entity.pojo.Subject;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.utils.Collections;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewStudy extends Controller {
	
//	public static Map<String, String> findBasic(String study_uri) {
	public static Map<String, List<String>> findBasic(String study_uri) {
		String basicQueryString = "";

		basicQueryString = 
		"PREFIX sio: <http://semanticscience.org/resource/>PREFIX chear: <http://hadatac.org/ont/chear#>PREFIX chear-kb: <http://hadatac.org/kb/chear#>PREFIX prov: <http://www.w3.org/ns/prov#>PREFIX hasco: <http://hadatac.org/ont/hasco/>PREFIX hasneto: <http://hadatac.org/ont/hasneto#>PREFIX dcterms: <http://purl.org/dc/terms/>PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>PREFIX skos: <http://www.w3.org/2004/02/skos/core#>PREFIX foaf: <http://xmlns.com/foaf/0.1/>SELECT ?studyUri ?studyLabel ?proj ?studyDef ?studyComment ?agentName ?institutionName  WHERE {        ?subUri rdfs:subClassOf* hasco:Study .                        ?studyUri a ?subUri .             ?studyUri rdfs:label ?studyLabel .            ?studyUri rdfs:label " + study_uri + " .         OPTIONAL { ?studyUri chear-kb:project ?proj } .         OPTIONAL { ?studyUri skos:definition ?studyDef } .         OPTIONAL { ?studyUri rdfs:comment ?studyComment } .         OPTIONAL { ?studyUri hasco:hasAgent ?agent .                                    ?agent foaf:name ?agentName } .         OPTIONAL { ?studyUri hasco:hasInstitution ?institution .                                  ?institution foaf:name ?institutionName} .                              }";
/*		"PREFIX sio: <http://semanticscience.org/resource/> "
			+ "PREFIX chear: <http://hadatac.org/ont/chear#> "
			+ "PREFIX chear-kb: <http://hadatac.org/kb/chear#> "
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> "
			+ "PREFIX hasco: <http://hadatac.org/ont/hasco/> "
			+ "PREFIX hasneto: <http://hadatac.org/ont/hasneto#> "
			+ "PREFIX dcterms: <http://purl.org/dc/terms/> "
			+ "	PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
			+ "SELECT * "
			+ "WHERE  {	?subUri rdfs:subClassOf* hasco:Study . "
            + "	?studyUri	a ?subUri . }";
*/        
		Query basicQuery = QueryFactory.create(basicQueryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		Map<String, List<String>> poResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
//		Map<String, String> poResult = new HashMap<String, String>();
		System.out.println("HERE IS THE RAW resultsrw*********" + resultsrw);
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values.add(soln.get("studyLabel").toString());
			values.add(soln.get("studyDef").toString());
			values.add(soln.get("proj").toString());
			values.add(soln.get("studyComment").toString());
			values.add(soln.get("agentName").toString());
			values.add(soln.get("institutionName").toString());
			poResult.put(soln.get("studyUri").toString(),values);
			
			
			//			poResult.put(soln.get("studyUri").toString(),soln.get("studyLabel").toString(),soln.get("proj").toString(),soln.get("studyDef").toString(),soln.get("studyComment").toString(),soln.get("agentName").toString(),soln.get("institutionName").toString(),soln.get("demographicLabel").toString());
//			poResult.put(soln.get("studyUri").toString(),soln.get("studyLabel").toString());
//			poResult.put(soln.get("studyUri").toString(),soln.get("studyDef").toString());
//			System.out.println("THIS IS SUBROW*********" + poResult);

		}
		return poResult;
	}
	
	public static List<String> findSubject(String study_uri) {

		String subjectQueryString = "";
		
    	subjectQueryString = 
    	    	
    	"PREFIX prov: <http://www.w3.org/ns/prov#> "
        + " PREFIX chear-kb: <http://hadatac.org/kb/chear#> "
        + "SELECT * "
        + "WHERE { ?s <http://hadatac.org/ont/hasco/isSampleOf> " + study_uri + " }";
        
		Query sampleQuery = QueryFactory.create(subjectQueryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), sampleQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		List<String> subjectResult = new ArrayList<String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE SAMPLES*********" + soln.toString());
			subjectResult.add(soln.get("s").toString());
//			System.out.println("THIS IS SUBROW*********" + poResult);

		}
		return subjectResult;
	}
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String study_uri) {

 //   	Map<String, String> poResult = findBasic(study_uri);
		Map<String, List<String>> poResult = findBasic(study_uri);
    	List<String> subjectResult = findSubject(study_uri);
        
    	return ok(viewStudy.render(poResult,subjectResult));
    
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String study_uri) {

		return index(study_uri);
	}

}
