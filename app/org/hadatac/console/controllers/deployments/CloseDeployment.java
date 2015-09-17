package org.hadatac.console.controllers.deployments;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.views.html.deployments.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.entity.pojo.Deployment;


public class CloseDeployment extends Controller {
	
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
 
    		//DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SSS'Z'");
    		//Date startDate;
			//try {
			//	startDate = df.parse(docDeployment.get("date"));
	    	//	dep.setStartDateTime(startDate);
			//} catch (ParseException e) {
			//	e.printStackTrace();
			//}

            System.out.println("closing deployment");
            return ok(closeDeployment.render(dep));
    	}
    	return ok(closeDeployment.render(dep));
        
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
 
    		//DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    		//Date startDate;
			//try {
			//	startDate = df.parse(docDeployment.get("date"));
	    	//	dep.setStartDateTime(startDate);
			//} catch (ParseException e) {
			//	e.printStackTrace();
			//}

            System.out.println("closing deployment");
            return ok(closeDeployment.render(dep));
    	}
    	return ok(closeDeployment.render(dep));
        
    }// /postIndex()

    public static Result processForm() {
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
		
        //Deployment deployment = DataFactory.closeDeployment(deploymentUri, endDateString);
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(closeDeployment.render(data));
        } else {
            return ok(deploymentConfirm.render("Close Deployment", data));
        }
    }
}
