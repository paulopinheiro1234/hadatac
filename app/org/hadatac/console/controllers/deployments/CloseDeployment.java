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

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.views.html.deployments.*;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class CloseDeployment extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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
    		depForm.setPlatform(dep.getPlatform().getLabel());
    		depForm.setInstrument(dep.getInstrument().getLabel());
    		if (dep.getDetectors() != null) {
    			Iterator<Detector> iterDetectors = dep.getDetectors().iterator();
    			while (iterDetectors.hasNext()) {
    				depForm.addDetector(((Detector)iterDetectors.next()).getLabel());
    			}
    		}
    		depForm.setStartDateTime(dep.getStartedAt());
 
            System.out.println("closing deployment");
            return ok(closeDeployment.render(deployment_uri, depForm));
    	}
    	
    	return ok(closeDeployment.render(deployment_uri, depForm));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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
    		depForm.setPlatform(dep.getPlatform().getLabel());
    		depForm.setInstrument(dep.getInstrument().getLabel());
    		if (dep.getDetectors() != null) {
    			Iterator<Detector> iterDetectors = dep.getDetectors().iterator();
    			while (iterDetectors.hasNext()) {
    				depForm.addDetector(((Detector)iterDetectors.next()).getLabel());
    			}
    		}
    		depForm.setStartDateTime(dep.getStartedAt());
 
            System.out.println("closing deployment");
            return ok(closeDeployment.render(deployment_uri, depForm));
    	}
    	return ok(closeDeployment.render(deployment_uri, depForm));
        
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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
		
		data.setPlatform(dep.getPlatform().getLabel());
		data.setInstrument(dep.getInstrument().getLabel());
		if (dep.getDetectors() != null) {
			Iterator<Detector> iterDetectors = dep.getDetectors().iterator();
			while (iterDetectors.hasNext()) {
				data.addDetector(((Detector)iterDetectors.next()).getLabel());
			}
		}
		data.setStartDateTime(dep.getStartedAt());
		data.setEndDateTime(dep.getEndedAt());

        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(closeDeployment.render(deployment_uri, data));
        } else {
            return ok(deploymentConfirm.render("Close Deployment", data, "", ""));
        }
    }
}
