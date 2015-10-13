package org.hadatac.console.controllers.deployments;

import org.hadatac.console.http.GetSparqlQuery;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.User;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;

public class NewDeployment extends Controller {
	
    public static SparqlQueryResults getQueryResults(String tabName) {
	    SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults thePlatforms = null;
    	String query_json = null;
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            thePlatforms = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | IOException | NullPointerException e1) {
            e1.printStackTrace();
        }
		return thePlatforms;
	}
    
    // for /metadata HTTP GET requests
    public static Result index(String type) {
    	return ok(newDeployment.render(Form.form(DeploymentForm.class), 
    			  Platform.find(),
    			  Instrument.findAvailable(),
    			  Detector.findAvailable(),
    			  type));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex(String type) {
    	return ok(newDeployment.render(Form.form(DeploymentForm.class), 
              Platform.find(),
    		  Instrument.findAvailable(),
			  Detector.findAvailable(),
  			  type));
        
    }// /postIndex()

    //prov:startedAtTime		"2015-02-15T19:50:55Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
        
    /**
     * Handles the form submission.
     */
    public static Result processForm() {
    	final User user = AuthApplication.getLocalUser(session());
        Form<DeploymentForm> form = Form.form(DeploymentForm.class).bindFromRequest();
        DeploymentForm data = form.get();

        String dateStringFromJs = data.getStartDateTime();
        String dateString = "";
        DateFormat jsFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
        Date dateFromJs;
		try {
			dateFromJs = jsFormat.parse(dateStringFromJs);
	        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	        dateString = isoFormat.format(dateFromJs);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		int triggeringEvent;
        String insert = "";
        String deploymentUri = DataFactory.getNextURI(DataFactory.DEPLOYMENT_ABBREV);
        String dataCollectionUri = DataFactory.getNextURI(DataFactory.DATA_COLLECTION_ABBREV);
        if (data.getType().equalsIgnoreCase("LEGACY")) {
        	triggeringEvent = TriggeringEvent.LEGACY_DEPLOYMENT;
        } else {
        	triggeringEvent = TriggeringEvent.INITIAL_DEPLOYMENT;
        }
        
        System.out.println("new deployment: size of detector's array : " + data.getDetector().size());
        if (data.getDetector().size() > 0) {
        	for (String detector : data.getDetector()) {
        		System.out.println("   -- det uri: " + detector);
        	}
        }
        
        Deployment deployment = DataFactory.createDeployment(deploymentUri, data.getPlatform(), data.getInstrument(), data.getDetector(), dateString, data.getType());
        DataCollection dataCollection = DataFactory.createDataCollection(dataCollectionUri, deploymentUri, triggeringEvent, UserManagement.getUriByEmail(user.email));
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(newDeployment.render(form,
            		  Platform.find(),
			          Instrument.find(),
	    			  Detector.find(),
			          data.getType()));        
        } else {
            return ok(deploymentConfirm.render("New Deployment", data));
        }
    }

}
