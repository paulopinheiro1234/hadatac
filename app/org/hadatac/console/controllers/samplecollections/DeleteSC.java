package org.hadatac.console.controllers.samplecollections;

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
import org.hadatac.console.views.html.samplecollections.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.controllers.studies.routes;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.SampleCollection;
import org.hadatac.entity.pojo.SampleCollectionType;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.SampleCollectionForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.DataAcquisitionManagement;
import org.hadatac.console.controllers.triplestore.UserManagement;

public class DeleteSC extends Controller {
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String std_uri, String sc_uri) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			        org.hadatac.console.controllers.samplecollections.routes.DeleteSC.index(std_uri, sc_uri).url()));
    	}

	std_uri = URLDecoder.decode(std_uri);
	sc_uri = URLDecoder.decode(sc_uri);
	//System.out.println("In DeleteSC: std_uri = [" + std_uri + "]");
	//System.out.println("In DeleteSC: sc_uri = [" + sc_uri + "]");

	Study study = Study.find(std_uri);
	if (study == null) {
	    return badRequest(sampleCollectionConfirm.render("Error deleting sample collection: Study URI did not return valid URI", std_uri, null));
	} 

	SampleCollection sc = SampleCollection.find(sc_uri);
	if (sc == null) {
	    return badRequest(sampleCollectionConfirm.render("Error deleting sample collection: SampleCollection URI did not return valid object", std_uri, sc));
	} 

    	return ok(deleteSampleCollection.render(std_uri, sc));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String std_uri, String sc_uri) {
    	return index(std_uri, sc_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm(String std_uri, String sc_uri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
	Study std = Study.find(std_uri);

	SampleCollection sc = SampleCollection.find(sc_uri);

	int deletedRows = -1;
	if (sc != null) {
	    try {
		System.out.println("calling sc.deleteFromLabKey() from DeleteSampleCollection"); 
		deletedRows = sc.deleteFromLabKey(session().get("LabKeyUserName"),session().get("LabKeyPassword"));
		if (deletedRows > 0) {
		    sc.delete();
		} else {
		    String message = "Number of deleted rows: " + deletedRows;
		    return badRequest(sampleCollectionConfirm.render("Error deleting sample collection: zero deleted rows", std_uri, sc));
		}
	    } catch (CommandException e) {
		return badRequest(sampleCollectionConfirm.render("Error deleting sample collection: LabKey", std_uri, sc));
	    }
	}
	
	return ok(sampleCollectionConfirm.render("Sample Collection has been Deleted",std_uri, sc));
    }
}
