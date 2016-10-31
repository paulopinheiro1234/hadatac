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
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.utils.Collections;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewSubject extends Controller {
	
	public static Map<String, String> findBasic(String subject_uri) {

		String basicQueryString = "";

		basicQueryString = 
    	
    	"PREFIX prov: <http://www.w3.org/ns/prov#> "
        + " PREFIX chear-kb: <http://hadatac.org/kb/chear#> "
        + "SELECT * "
        + "WHERE  {	" + subject_uri + " ?p ?o }";
    	
    	
        
		Query basicQuery = QueryFactory.create(basicQueryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Map<String, String> poResult = new HashMap<String, String>();
		System.out.println("HERE IS THE RAW resultsrw*********" + resultsrw);
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			poResult.put(soln.get("p").toString(), soln.get("o").toString());
//			System.out.println("THIS IS SUBROW*********" + poResult);

		}
		return poResult;
	}
	
	public static List<String> findSample(String subject_uri) {

		String sampleQueryString = "";
		
    	sampleQueryString = 
    	    	
    	"PREFIX prov: <http://www.w3.org/ns/prov#> "
        + " PREFIX chear-kb: <http://hadatac.org/kb/chear#> "
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
//			System.out.println("THIS IS SUBROW*********" + poResult);

		}
		return sampleResult;
	}
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String subject_uri) {

    	Map<String, String> poResult = findBasic(subject_uri);
    	List<String> sampleResult = findSample(subject_uri);
        
    	return ok(viewSubject.render(poResult,sampleResult));
    
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String subject_uri) {

		return index(subject_uri);
	}

}
