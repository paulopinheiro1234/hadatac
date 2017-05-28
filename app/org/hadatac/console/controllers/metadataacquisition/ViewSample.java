package org.hadatac.console.controllers.metadataacquisition;

import java.io.UnsupportedEncodingException;

import org.hadatac.entity.pojo.Subject;
import org.hadatac.metadata.loader.ValueCellProcessing;

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
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.hadatac.entity.pojo.Measurement;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewSample extends Controller {

	public static Map<String, String> findSampleIndicators(String sample_uri) {
		
		Map<String, String> indicatorValues = new HashMap<String, String>();
		
		return indicatorValues;
	}
	
	public static Map<String, List<String>> findBasic(String sample_uri, String study_uri) {

		String sampleQueryString = "";
		
    	sampleQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
    	/*"PREFIX sio: <http://semanticscience.org/resource/>" + 
    	"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
    	"PREFIX chear-kb: <http://hadatac.org/kb/chear#>" + 
    	"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
    	"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
    	"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
    	"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
    	"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
    	"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
    	"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + */
         "SELECT ?originalID ?isSampleOf ?isObjectOf ?sampleType ?sampleLabel ?storageTempUnit ?samplingVolumeUnit ?comment" +
		 "WHERE {	" + sample_uri + "rdfs:label ?sampleLabel . " +
         				sample_uri + "rdfs:comment ?comment . " +
         				sample_uri + "rdf:type	?sampleType . " +
         				sample_uri + "<http://hadatac.org/ont/hasco/isObjectOf> ?isObjectOf . " +
         				sample_uri + "<http://hadatac.org/ont/hasco/originalID> ?originalID . " +
         				sample_uri + "<http://hadatac.org/ont/hasco/isSampleOf> ?isSampleOf . " +
         				sample_uri + "<http://hadatac.org/ont/hasco/hasStorageTemperatureUnit> ?storageTempUnit . " +
         				sample_uri + "<http://hadatac.org/ont/hasco/hasSamplingVolumeUnit> ?samplingVolumeUnit . }";
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
			values.add("OriginalID: " + soln.get("originalID").toString());
			values.add("Label: " + soln.get("sampleLabel").toString());
			values.add("Type: " + soln.get("sampleType").toString());
			values.add("Object Of: " + soln.get("isObjectOf").toString());
			values.add("Sample Of: " + soln.get("isSampleOf").toString());
			values.add("Sample Volume Unit: " + soln.get("samplingVolumeUnit").toString());
			values.add("Storage Temperature Unit: " + soln.get("storageTempUnit").toString());
			values.add("Comment: " + soln.get("comment").toString());
			sampleResult.put(soln.get("sampleUri").toString(),values);	
			System.out.println("THIS IS SUBROW*********" + sampleResult);	
		}

		return sampleResult;
	}
	
	public static String findUser() {
		String results = null;
	    final SysUser user = AuthApplication.getLocalUser(session());
	    if(null == user){
	        results = null;
	    }
	    else{
	    	results = UserManagement.getUriByEmail(user.getEmail());
	    }
	    System.out.println("This is the current user's uri:" + results);
	    
	    return results;
	}
	
	public static String findValues(String sample_uri, String study_uri, String subject_uri) {
		ValueCellProcessing cellProc = new ValueCellProcessing();
		return Measurement.findForViews(findUser(), study_uri, cellProc.convertToWholeURI(subject_uri), sample_uri).documents.get(0).getValue().toString();
	}
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String sample_uri, String study_uri, String subject_uri) {

		Map<String, String> indicatorValues = findSampleIndicators(sample_uri);
    	Map<String, List<String>> sampleResult = findBasic(sample_uri, study_uri);
    	String samplevalues = findValues(sample_uri, study_uri, subject_uri);
        
    	return ok(viewSample.render(sampleResult,indicatorValues, samplevalues));   
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String sample_uri, String study_uri, String subject_uri) {

		return index(sample_uri, study_uri, subject_uri);
	}

}
