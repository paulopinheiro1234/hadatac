package org.hadatac.console.controllers.indicators;

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
import org.hadatac.console.views.html.indicators.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.controllers.indicators.routes;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.Agent;
import org.hadatac.entity.pojo.Indicator;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.IndicatorForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.DataAcquisitionManagement;
import org.hadatac.console.controllers.triplestore.UserManagement;

public class NewIndicator extends Controller {
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
    				routes.NewIndicator.index().url()));
    	}
    	// may need addressing
    	Indicator indicator = new Indicator();
    	return ok(newIndicator.render(indicator));
    	
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
    	return index();
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm() {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
        Form<IndicatorForm> form = Form.form(IndicatorForm.class).bindFromRequest();
        IndicatorForm data = form.get();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        
	// store new values
	String newURI = ValueCellProcessing.replacePrefixEx(data.getNewUri());
	if (newURI == null || newURI.equals("")) {
            return badRequest("[ERROR] New URI cannot be empty.");
	}
	String newLabel = data.getNewLabel();
	String newComment = data.getNewComment();

        // insert current state of the STD
	Indicator ind = new Indicator(DynamicFunctions.replacePrefixWithURL(newURI),
			      newLabel,
			      newComment);
	
	// insert the new indicator content inside of the triplestore
	ind.save();
	
	// update/create new indicator in LabKey
	int nRowsAffected = ind.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
	if (nRowsAffected <= 0) {
	    return badRequest("Failed to insert new indicator to LabKey!\n");
	}
	
	
	return ok(newIndicatorConfirm.render(ind));
    }
}
