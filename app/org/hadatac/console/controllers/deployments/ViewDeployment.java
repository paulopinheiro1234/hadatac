package org.hadatac.console.controllers.deployments;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.deployments.*;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class ViewDeployment extends Controller {
	
	private static State allState = new State(State.ALL);
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String deployment_uri) {

    	//DeploymentForm dep = new DeploymentForm();
    	Deployment deployment = null;
    	List<DataAcquisition> dataCollections = null;
    	
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
        	dataCollections = DataAcquisition.find(deployment, false);    		
    	}
    	return ok(viewDeployment.render(deployment, dataCollections));
    
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String deployment_uri) {

    	//DeploymentForm dep = new DeploymentForm();
    	Deployment deployment = null;
    	List<DataAcquisition> dataCollections = null;
    	
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
        	dataCollections = DataAcquisition.find(deployment, false);    		
    	}
    	return ok(viewDeployment.render(deployment, dataCollections));
        
    }// /postIndex()

}
