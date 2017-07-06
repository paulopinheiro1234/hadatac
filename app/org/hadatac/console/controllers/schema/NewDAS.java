package org.hadatac.console.controllers.schema;

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
import org.hadatac.console.views.html.schema.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.controllers.deployments.routes;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.DataAcquisitionSchemaForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.controllers.schema.NewDAS;

public class NewDAS extends Controller {
	
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
	public static Result index() {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
	    return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			     org.hadatac.console.controllers.schema.routes.NewDAS.index().url()));
    	}
	return ok(newDAS.render());
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex() {
    	return index();
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm() {
    	final SysUser user = AuthApplication.getLocalUser(session());
        Form<DataAcquisitionSchemaForm> form = Form.form(DataAcquisitionSchemaForm.class).bindFromRequest();
        if (form.hasErrors()) {
	    return badRequest("The submitted form has errors!");
        }
        
        DataAcquisitionSchemaForm data = form.get();
	
        String label = data.getLabel();
        DataAcquisitionSchema das = DataFactory.createDataAcquisitionSchema(label);
        
        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name != null && password != null) {
	    try {
		int nRowsOfSchema = das.saveToLabKey(
				        session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		das.save();
		return ok(main.render("Results,", "", new Html("<h3>" 
							       + String.format("%d row(s) have been inserted in Table \"DataAcquisitionSchema\" \n", nRowsOfSchema) 
							       + "</h3>")));
	    } catch (CommandException e) {
		return badRequest("Failed to insert Deployment to LabKey!\n"
				  + "Error Message: " + e.getMessage());
	    }
        }
        
        return ok(DASConfirm.render("New Data Acquisition Schema", data));
    }
}
