package org.hadatac.console.controllers.objectcollections;

import org.hadatac.console.http.GetSparqlQuery;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import play.data.*;

import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.objectcollections.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.controllers.studies.routes;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.ObjectCollectionForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.DataAcquisitionManagement;
import org.hadatac.console.controllers.triplestore.UserManagement;

public class DeleteOC extends Controller {
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String filename, String da_uri, String std_uri, String oc_uri) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
				org.hadatac.console.controllers.objectcollections.routes.DeleteOC.index(filename, da_uri, std_uri, oc_uri).url()));
    	}

	std_uri = URLDecoder.decode(std_uri);
	oc_uri = URLDecoder.decode(oc_uri);
	//System.out.println("In DeleteOC: std_uri = [" + std_uri + "]");
	//System.out.println("In DeleteOC: oc_uri = [" + oc_uri + "]");

	Study study = Study.find(std_uri);
	if (study == null) {
	    return badRequest(objectCollectionConfirm.render("Error deleting object collection: Study URI did not return valid URI", filename, da_uri, std_uri, null));
	} 

	ObjectCollection oc = ObjectCollection.find(oc_uri);
	if (oc == null) {
	    return badRequest(objectCollectionConfirm.render("Error deleting object collection: ObjectCollection URI did not return valid object", filename, da_uri, std_uri, oc));
	} 

    	return ok(deleteObjectCollection.render(filename, da_uri, std_uri, oc));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String filename, String da_uri, String std_uri, String oc_uri) {
    	return index(filename, da_uri, std_uri, oc_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm(String filename, String da_uri, String std_uri, String oc_uri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
	Study std = Study.find(std_uri);

	ObjectCollection oc = ObjectCollection.find(oc_uri);

	int deletedRows = -1;
	if (oc != null) {
	    try {
		System.out.println("calling oc.deleteFromLabKey() from DeleteObjectCollection"); 
		deletedRows = oc.deleteFromLabKey(session().get("LabKeyUserName"),session().get("LabKeyPassword"));
		if (deletedRows > 0) {
		    oc.delete();
		} else {
		    String message = "Number of deleted rows: " + deletedRows;
		    return badRequest(objectCollectionConfirm.render("Error deleting object collection: zero deleted rows", filename, da_uri, std_uri, oc));
		}
	    } catch (CommandException e) {
		return badRequest(objectCollectionConfirm.render("Error deleting object collection: LabKey", filename, da_uri, std_uri, oc));
	    }
	}
	
	return ok(objectCollectionConfirm.render("Object Collection has been Deleted", filename, da_uri, std_uri, oc));
    }
}
