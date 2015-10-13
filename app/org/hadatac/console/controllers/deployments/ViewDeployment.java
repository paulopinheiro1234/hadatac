package org.hadatac.console.controllers.deployments;

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
import org.hadatac.entity.pojo.Deployment;


public class ViewDeployment extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index(String deployment_uri) {

    	//DeploymentForm dep = new DeploymentForm();
    	Deployment deployment = null;
    	
    	try {
    		if (deployment_uri != null) {
			    deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
    		} else {
    			deployment_uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

    	if (!deployment_uri.equals("")) {

        	deployment = Deployment.find(deployment_uri);

    	}
    	return ok(viewDeployment.render(deployment));
    
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex(String deployment_uri) {

    	Deployment deployment = null;
    	
    	try {
    		if (deployment_uri != null) {
			    deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
    		} else {
    			deployment_uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

    	if (!deployment_uri.equals("")) {

        	deployment = Deployment.find(deployment_uri);

    	}
    	return ok(viewDeployment.render(deployment));
            
    }// /postIndex()

}
