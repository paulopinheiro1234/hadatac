package org.hadatac.console.controllers.deployments;

import org.hadatac.console.http.GetSparqlQuery;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import play.data.*;

import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.deployments.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.controllers.deployments.routes;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.DataAcquisitionManagement;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.controllers.deployments.NewDeployment;

public class NewDeployment extends Controller {
	
    public static SparqlQueryResults getQueryResults(String tabName) {
	    SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults thePlatforms = null;
    	String query_json = null;
        try {
            query_json = query_submit.executeQuery(tabName);
            thePlatforms = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            e1.printStackTrace();
        }
		return thePlatforms;
	}

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String type, String filename, String da_uri ) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
				routes.NewDeployment.index(type, filename, da_uri).url()));
    	}
    	
    	if (type.equalsIgnoreCase("regular")) {
    		return ok(newDeployment.render(
      			  Platform.find(),
      			  Instrument.findAvailable(),
      			  Detector.findAvailable(),
      			  type,
                          filename, 
                          da_uri));
    	}
    	else if (type.equalsIgnoreCase("legacy")) {
    		return ok(newDeployment.render(
      			  Platform.find(),
      			  Instrument.find(),
      			  Detector.find(),
      			  type,
                          filename,
                          da_uri));
    	}
    	
    	return badRequest("Invalid deployment type!");
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String type, String filename, String da_uri) {
    	return index(type, filename, da_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String filename, String da_uri) {
    	final SysUser user = AuthApplication.getLocalUser(session());
        Form<DeploymentForm> form = Form.form(DeploymentForm.class).bindFromRequest();
        if (form.hasErrors()) {
        	return badRequest("The submitted form has errors!");
        }
        
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
			return badRequest("Cannot parse data " + dateStringFromJs);
		}

        String deploymentUri = data.getUri();
        Deployment deployment = DataFactory.createDeployment(deploymentUri, data.getPlatform(), 
        		data.getInstrument(), data.getDetectors(), dateString, data.getType());
        
        int triggeringEvent;
        if (data.getType().equalsIgnoreCase("LEGACY")) {
        	triggeringEvent = TriggeringEvent.LEGACY_DEPLOYMENT;
        } else {
        	triggeringEvent = TriggeringEvent.INITIAL_DEPLOYMENT;
        }
        String dataAcquisitionUri = data.getDataAcquisitionUri();
        String param = data.getInitialParameter();
        DataAcquisition dataAcquisition = DataFactory.createDataAcquisition(
        		triggeringEvent, dataAcquisitionUri, deploymentUri, 
        		param, UserManagement.getUriByEmail(user.getEmail()));
        
        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name != null && password != null) {
        	try {
        		int nRowsOfDeployment = deployment.saveToLabKey(
        				session().get("LabKeyUserName"), session().get("LabKeyPassword"));
        		int nRowsOfDA = dataAcquisition.saveToLabKey(
        				session().get("LabKeyUserName"), session().get("LabKeyPassword"));
        		deployment.save();
        		dataAcquisition.save();
		    	return ok(main.render("Results,", "", new Html("<h3>" 
		    			+ String.format("%d row(s) have been inserted in Table \"Deployment\" \n", nRowsOfDeployment) 
		    			+ String.format("%d row(s) have been inserted in Table \"DataAcquisition\"", nRowsOfDA)
		    			+ "</h3>")));
        	} catch (CommandException e) {
        		return badRequest("Failed to insert Deployment to LabKey!\n"
						+ "Error Message: " + e.getMessage());
			}
        }
        
        return ok(deploymentConfirm.render("New Deployment", data, filename, da_uri));
    }
}
