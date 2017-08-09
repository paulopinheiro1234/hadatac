package org.hadatac.console.controllers.objectcollections;

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
import org.hadatac.utils.ConfigProp;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.ObjectCollectionType;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.objectcollections.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.models.ObjectCollectionForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.http.GetSparqlQuery;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.controllers.objects.routes;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class NewOC extends Controller {
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String std_uri) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
	    return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			    org.hadatac.console.controllers.objectcollections.routes.NewOC.index(std_uri).url()));
    	}
	Study study = Study.find(std_uri);
	List<ObjectCollectionType> typeList = ObjectCollectionType.find();

	List<ObjectCollection> domainList = new ArrayList<ObjectCollection>();
	List<ObjectCollection> locationList = new ArrayList<ObjectCollection>();
	List<ObjectCollection> timeList = new ArrayList<ObjectCollection>();
	List<ObjectCollection> objList = ObjectCollection.findByStudy(study);
	for (ObjectCollection oc : objList) {
	    if (oc.isDomainCollection()) {
		domainList.add(oc);
	    } else if (oc.isLocationCollection()) {
		locationList.add(oc);
	    } else if (oc.isTimeCollection()) {
		timeList.add(oc);
	    }
	}

    	return ok(newObjectCollection.render(study, domainList, locationList, timeList, typeList));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String std_uri) {
    	return index(std_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String std_uri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
	Study std = Study.find(std_uri);
	
        Form<ObjectCollectionForm> form = Form.form(ObjectCollectionForm.class).bindFromRequest();
        ObjectCollectionForm data = form.get();
        
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
	String newHasScopeUri = data.getNewHasScopeUri();
	List<String> newSpaceScopeUris = data.getSpaceUri();
	List<String> newTimeScopeUris = data.getTimeUri();
	
        // insert current state of the OC
	ObjectCollection oc = new ObjectCollection(newURI,
						   newType,
						   newLabel,
						   newComment,
						   newStudyUri,
						   newHasScopeUri,
						   newSpaceScopeUris,
						   newTimeScopeUris);
	
	// insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
	oc.save();
	
	// update/create new OC in LabKey
	int nRowsAffected = oc.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
	if (nRowsAffected <= 0) {
	    return badRequest("Failed to insert new OC to LabKey!\n");
	}
	return ok(objectCollectionConfirm.render("New Object Collection has been Generated", std_uri, oc));
    }

}
