package org.hadatac.console.controllers.deployments;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.views.html.deployments.*;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;


public class CloseDeployment extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index(String deployment_uri) {

    	DeploymentForm depForm = new DeploymentForm();
    	Deployment dep = null;
    	
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

    		dep = Deployment.find(deployment_uri);
    		/*
    		 *  Add deployment information into handler
    		 */
    		depForm.setPlatform(dep.platform.getLabel());
    		depForm.setInstrument(dep.instrument.getLabel());
    		if (dep.detectors != null) {
    			Iterator detectors = dep.detectors.iterator();
    			while (detectors.hasNext()) {
    				depForm.addDetector(((Detector)detectors.next()).getLabel());
    			}
    		}
    		depForm.setStartDateTime(dep.getStartedAt());
 
            System.out.println("closing deployment");
            return ok(closeDeployment.render(deployment_uri, depForm));
    	}
    	return ok(closeDeployment.render(deployment_uri, depForm));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex(String deployment_uri) {
    	DeploymentForm depForm = new DeploymentForm();
    	Deployment dep = null;
    	
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

    		dep = Deployment.find(deployment_uri);
    		/*
    		 *  Add deployment information into handler
    		 */
    		depForm.setPlatform(dep.platform.getLabel());
    		depForm.setInstrument(dep.instrument.getLabel());
    		if (dep.detectors != null) {
    			Iterator detectors = dep.detectors.iterator();
    			while (detectors.hasNext()) {
    				depForm.addDetector(((Detector)detectors.next()).getLabel());
    			}
    		}
    		depForm.setStartDateTime(dep.getStartedAt());
 
            System.out.println("closing deployment");
            return ok(closeDeployment.render(deployment_uri, depForm));
    	}
    	return ok(closeDeployment.render(deployment_uri, depForm));
        
    }// /postIndex()

    public static Result processForm(String deployment_uri) {
    	Deployment dep = null;
    	
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
    		dep = Deployment.find(deployment_uri);
    	}
    	
    	Form<DeploymentForm> form = Form.form(DeploymentForm.class).bindFromRequest();
        DeploymentForm data = form.get();

        String dateStringFromJs = data.getEndDateTime();
        String endDateString = "";
        DateFormat jsFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
        Date dateFromJs;
		try {
			dateFromJs = jsFormat.parse(dateStringFromJs);
	        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	        endDateString = isoFormat.format(dateFromJs);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		dep.close(endDateString);
		
		data.setPlatform(dep.platform.getLabel());
		data.setInstrument(dep.instrument.getLabel());
		if (dep.detectors != null) {
			Iterator detectors = dep.detectors.iterator();
			while (detectors.hasNext()) {
				data.addDetector(((Detector)detectors.next()).getLabel());
			}
		}
		data.setStartDateTime(dep.getStartedAt());
		data.setEndDateTime(dep.getEndedAt());

		//Deployment deployment = DataFactory.closeDeployment(deploymentUri, endDateString);
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(closeDeployment.render(deployment_uri, data));
        } else {
            return ok(deploymentConfirm.render("Close Deployment", data));
        }
    }
}
