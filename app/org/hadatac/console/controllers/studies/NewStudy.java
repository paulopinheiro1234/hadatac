package org.hadatac.console.controllers.studies;

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
import org.hadatac.console.views.html.studies.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.controllers.studies.routes;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.StudyForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.DataAcquisitionManagement;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.controllers.deployments.NewDeployment;

public class NewStudy extends Controller {
	
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
    public static Result index(String type) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
    				routes.NewStudy.index(type).url()));
    	}
    	
    	if (type.equalsIgnoreCase("regular")) {
    		return ok(newStudy.render(
      			  Platform.find(),
      			  Instrument.findAvailable(),
      			  Detector.findAvailable(),
      			  type));
    	}
    	else if (type.equalsIgnoreCase("legacy")) {
    		return ok(newStudy.render(
      			  Platform.find(),
      			  Instrument.find(),
      			  Detector.find(),
      			  type));
    	}
    	
    	return badRequest("Invalid deployment type!");
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String type) {
    	return index(type);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm() {
    	final SysUser user = AuthApplication.getLocalUser(session());
        Form<StudyForm> form = Form.form(StudyForm.class).bindFromRequest();
        if (form.hasErrors()) {
        	return badRequest("The submitted form has errors!");
        }
        
        StudyForm data = form.get();

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

        String studyUri = data.getUri();
        Study study = DataFactory.createStudy(studyUri, data.getDataAcquisitions(), dateString);
        
        int triggeringEvent = TriggeringEvent.INITIAL_DEPLOYMENT;
        
//        String dataAcquisitionUri = data.getDataAcquisitionUri();
        String param = data.getInitialParameter();
/*        DataAcquisition dataAcquisition = DataFactory.createDataAcquisition(
        		triggeringEvent, dataAcquisitionUri, deploymentUri, 
        		param, UserManagement.getUriByEmail(user.getEmail()));
  */      
        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
 /*       if (user_name != null && password != null) {
        	try {
        		int nRowsOfDeployment = study.saveToLabKey(
        				session().get("LabKeyUserName"), session().get("LabKeyPassword"));
        		int nRowsOfDA = dataAcquisition.saveToLabKey(
        				session().get("LabKeyUserName"), session().get("LabKeyPassword"));
        		study.save();
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
   */     
        return ok(studyConfirm.render("New Study", data));
    }
}
