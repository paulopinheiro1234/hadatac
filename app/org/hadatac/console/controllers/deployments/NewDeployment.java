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
import play.data.*;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.controllers.deployments.routes;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.LabKeyLoginForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
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
    public static Result index(String type) {        
    	boolean isLoggedInLabKey = true;
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		isLoggedInLabKey = false;

            Form<LabKeyLoginForm> form = Form.form(LabKeyLoginForm.class).bindFromRequest();
            if (!form.hasErrors() && !form.get().getUserName().isEmpty() && !form.get().getPassword().isEmpty()) {
            	String site = ConfigProp.getPropertyValue("labkey.config", "site");
                String path = "/";
                String user_name = form.get().getUserName();
                String password = form.get().getPassword();
            	LabkeyDataHandler loader = new LabkeyDataHandler(
            			site, user_name, password, path);
            	try {
            		loader.checkAuthentication();
            		session().put("LabKeyUserName", user_name);
                    session().put("LabKeyPassword", password);
                    isLoggedInLabKey = true;
            	} catch(CommandException e) {
            		if(e.getMessage().equals("Unauthorized")){
            			return ok(syncLabkey.render("login_failed", 
            					routes.NewDeployment.index(type).url(), "", false));
            		}
            	}
            }
    	}
    	return ok(newDeployment.render(Form.form(DeploymentForm.class), 
    			  Platform.find(),
    			  Instrument.findAvailable(),
    			  Detector.findAvailable(),
    			  type,
    			  isLoggedInLabKey));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String type) {
    	return index(type);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm() {
    	final SysUser user = AuthApplication.getLocalUser(session());
        Form<DeploymentForm> form = Form.form(DeploymentForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
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
			e.printStackTrace();
		}

        String deploymentUri = data.getUri();
        Deployment deployment = DataFactory.createDeployment(deploymentUri, data.getPlatform(), 
        		data.getInstrument(), data.getDetectors(), dateString, data.getType());
        
        /*
        int triggeringEvent;
        if (data.getType().equalsIgnoreCase("LEGACY")) {
        	triggeringEvent = TriggeringEvent.LEGACY_DEPLOYMENT;
        } else {
        	triggeringEvent = TriggeringEvent.INITIAL_DEPLOYMENT;
        }
        DataFactory.createDataAcquisition(dataAcquisitionUri, deploymentUri, triggeringEvent, 
        	UserManagement.getUriByEmail(user.getEmail()));
        */
        
        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name != null && password != null) {
        	try {
        		saveToLabKey(deployment);
        	} catch (CommandException e) {
        		return badRequest("Failed to insert Deployment to LabKey!");
			}
        }
        
        return ok(deploymentConfirm.render("New Deployment", data));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static void saveToLabKey(Deployment deployment) throws CommandException {
    	if (null != deployment) {
    		String site = ConfigProp.getPropertyValue("labkey.config", "site");
            String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
            
        	LabkeyDataHandler loader = new LabkeyDataHandler(
        			site, session().get("LabKeyUserName"), session().get("LabKeyPassword"), path);
        	
        	ValueCellProcessing cellProc = new ValueCellProcessing();
        	List<String> detectorURIs = new ArrayList<String>();
        	for (Detector detector : deployment.getDetectors()) {
        		detectorURIs.add(cellProc.replaceNameSpaceEx(detector.getUri()));
        	}
        	String detectors = String.join(", ", detectorURIs);
        	
        	List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        	Map<String, Object> row = new HashMap<String, Object>();
        	row.put("hasURI", cellProc.replaceNameSpaceEx(deployment.getUri()));
        	row.put("a", "vstoi:Deployment");
        	row.put("vstoi:hasPlatform", cellProc.replaceNameSpaceEx(deployment.getPlatform().getUri()));
        	row.put("hasneto:hasInstrument", cellProc.replaceNameSpaceEx(deployment.getInstrument().getUri()));
        	row.put("hasneto:hasDetector", detectors);
        	row.put("prov:startedAtTime", deployment.getStartedAt());
        	row.put("prov:endedAtTime", deployment.getEndedAt());
        	rows.add(row);
        	
        	loader.insertRows("Deployment", rows);
    	}
    }
}
