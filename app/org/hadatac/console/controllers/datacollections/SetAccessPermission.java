package org.hadatac.console.controllers.datacollections;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.console.controllers.deployments.*;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;


public class SetAccessPermission extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index(String uri) {

    	DeploymentForm dep = new DeploymentForm();
    	
    	try {
    		if (uri != null) {
			    uri = URLDecoder.decode(uri, "UTF-8");
    		} else {
    			uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

    	if (!uri.equals("")) {

    		/*
    		 *  Add deployment information into handler
    		 */
    		String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, uri);
    		SparqlQueryResults results = new SparqlQueryResults(json, false);
    		TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
    		dep.setPlatform(docDeployment.get("platform"));
    		dep.setInstrument(docDeployment.get("instrument"));
    		dep.setDetector(docDeployment.get("detector"));
    		dep.setStartDateTime(docDeployment.get("date"));
 
            System.out.println("closing deployment");
            return ok(viewDeployment.render(dep));
    	}
    	return ok(viewDeployment.render(dep));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex(String uri) {

    	DeploymentForm dep = new DeploymentForm();
    	
    	try {
    		if (uri != null) {
			    uri = URLDecoder.decode(uri, "UTF-8");
    		} else {
    			uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

    	if (!uri.equals("")) {

    		/*
    		 *  Add deployment information into handler
    		 */
    		String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, uri);
    		SparqlQueryResults results = new SparqlQueryResults(json, false);
    		TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
    		dep.setPlatform(docDeployment.get("platform"));
    		dep.setInstrument(docDeployment.get("instrument"));
    		dep.setDetector(docDeployment.get("detector"));
    		dep.setStartDateTime(docDeployment.get("date"));
 
            return ok(viewDeployment.render(dep));
    	}
    	return ok(viewDeployment.render(dep));
        
    }// /postIndex()

}
