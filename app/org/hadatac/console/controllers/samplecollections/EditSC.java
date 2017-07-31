package org.hadatac.console.controllers.samplecollections;

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
import org.hadatac.utils.ConfigProp;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.SampleCollection;
import org.hadatac.entity.pojo.SampleCollectionType;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.samplecollections.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.models.SampleCollectionForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.http.GetSparqlQuery;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.controllers.samples.routes;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class EditSC extends Controller {
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String std_uri, String sc_uri) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
	    return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			    org.hadatac.console.controllers.samplecollections.routes.EditSC.index(std_uri, sc_uri).url()));
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

	List<SampleCollectionType> typeList = SampleCollectionType.find();

    	return ok(editSampleCollection.render(study, sc, typeList));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String std_uri, String sc_uri) {
    	return index(std_uri, sc_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String std_uri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
	Study std = Study.find(std_uri);
	
        Form<SampleCollectionForm> form = Form.form(SampleCollectionForm.class).bindFromRequest();
        SampleCollectionForm data = form.get();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        
	// store new values
	System.out.println("uri: " + data.getNewUri());
	System.out.println("type: " + data.getNewType());
	
	String newURI = null;
	if (data.getNewUri() == null || data.getNewUri().equals("")) {
            return badRequest("[ERROR] New URI cannot be empty.");
	} else {
	    newURI = ValueCellProcessing.replacePrefixEx(data.getNewUri());
	}
	String newType = null;
	if (data.getNewType() == null || data.getNewType().equals("")) {
            return badRequest("[ERROR] New type cannot be empty.");
	} else {
	    newType = ValueCellProcessing.replacePrefixEx(data.getNewType());
	}
	String newStudyUri = ValueCellProcessing.replacePrefixEx(std_uri);
	String newLabel = data.getNewLabel();
	String newComment = data.getNewComment();
	
        // insert current state of the SC
	SampleCollection sc = new SampleCollection(newURI,
						   newType,
						   newLabel,
						   newComment,
						   newStudyUri);
	
	// insert the new SC content inside of the triplestore regardless of any change -- the previous content has already been deleted
	sc.save();
	
	// update/create new SC in LabKey
	int nRowsAffected = sc.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
	if (nRowsAffected <= 0) {
	    return badRequest("Failed to edit SC into LabKey!\n");
	}
	return ok(sampleCollectionConfirm.render("New Sample Collection has been Edited", std_uri, sc));
    }

}
