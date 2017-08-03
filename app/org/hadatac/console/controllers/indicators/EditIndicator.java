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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import play.data.*;

import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.indicators.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.controllers.indicators.routes;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.Indicator;
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

public class EditIndicator extends Controller {
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String ind_uri) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
    				routes.EditIndicator.index(ind_uri).url()));
    	}
    	
    	Indicator indicator = null;

    	try {
	    if (ind_uri != null) {
		ind_uri = URLDecoder.decode(ind_uri, "UTF-8");
	    } else {
		ind_uri = "";
	    }
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	
    	if (!ind_uri.equals("")) {
	    indicator = Indicator.find(ind_uri);
    	} else {
            return badRequest("No URI is provided to retrieve Indicator");
    	}

	return ok(editIndicator.render(indicator));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String ind_uri) {
    	return index(ind_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String ind_uri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
        Form<IndicatorForm> form = Form.form(IndicatorForm.class).bindFromRequest();
        IndicatorForm data = form.get();
        List<String> changedInfos = new ArrayList<String>();
        
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

	// retrieve old Indicator and corresponding DAS
    Indicator oldIndicator = Indicator.find(ind_uri);

	// set changes
	if (oldIndicator != null) {
	    
	    if (oldIndicator.getUri() != null && !oldIndicator.getUri().equals(newURI)) {
	    	changedInfos.add(newURI);
	    }
	    if (oldIndicator.getLabel() != null && !oldIndicator.getLabel().equals(newLabel)) {
	    	changedInfos.add(newLabel);
	    }
	    if (oldIndicator.getComment() == null || !oldIndicator.getComment().equals(newComment)) {
	    	changedInfos.add(newComment);
	    }
	    // delete previous state of the Indicator in the triplestore
	    if (oldIndicator != null) {
	    	oldIndicator.delete();
	    }
	} else {
	    return badRequest("[ERRO] Failed locating existing Indicator.\n");
	}

        // insert current state of the Indicator
	oldIndicator.setUri(newURI);
	oldIndicator.setLabel(newLabel);
	oldIndicator.setComment(newComment);
	// insert the new Indicator content inside of the triplestore regardless of any change -- the previous content has already been deleted
	oldIndicator.save();
	
	// update/create new Indicator in LabKey
	int nRowsAffected = oldIndicator.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
	if (nRowsAffected <= 0) {
	    return badRequest("Failed to insert edited Indicator to LabKey!\n");
	}

        return ok(indicatorConfirm.render("Edit Indicator", oldIndicator));
    }
}
