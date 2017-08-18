package org.hadatac.console.controllers.objects;

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
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.Sample;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.objects.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.models.ObjectsForm;
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

public class ObjectManagement extends Controller {
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result indexNomsg(String filename, String da_uri, String std_uri, String oc_uri) {
    	return index(filename, da_uri, std_uri, oc_uri, "");
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String filename, String da_uri, String std_uri, String oc_uri, String message) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
	    return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			    org.hadatac.console.controllers.objects.routes.ObjectManagement.index(filename, da_uri, std_uri, oc_uri, message).url()));
    	}
	std_uri = URLDecoder.decode(std_uri);
	oc_uri = URLDecoder.decode(oc_uri);
	//System.out.println("In DeleteOC: std_uri = [" + std_uri + "]");
	//System.out.println("In DeleteOC: oc_uri = [" + oc_uri + "]");

	Study study = Study.find(std_uri);
	if (study == null) {
	    return badRequest(objectConfirm.render("Error listing object collection: Study URI did not return valid URI", filename, da_uri, std_uri, oc_uri));
	} 

	ObjectCollection oc = ObjectCollection.find(oc_uri);
	if (oc == null) {
	    return badRequest(objectConfirm.render("Error listing objectn: ObjectCollection URI did not return valid object", filename, da_uri, std_uri, oc_uri));
	} 

	List<String> objUriList = new ArrayList<String>(); 
	List<StudyObject> objects = StudyObject.findByCollection(oc);
	for (StudyObject obj : objects) {
	    objUriList.add(obj.getUri());
	}

    	return ok(objectManagement.render(filename, da_uri, study, oc, objUriList, objects, message));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String filename, String da_uri, String std_uri, String oc_uri, String message) {
    	return index(filename, da_uri, std_uri, oc_uri, message);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result updateCollectionObjects(String filename, String da_uri, String std_uri, String oc_uri, List<String> objUriList) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
	std_uri = URLDecoder.decode(std_uri);
	oc_uri = URLDecoder.decode(oc_uri);
	
 	System.out.println("Study URI entering 1 EditObject: [" + std_uri + "]");
	if (std_uri == null || std_uri.equals("")) {
            return badRequest("Cannot edit objects: empty study URI");
	}
	Study study = Study.find(std_uri);
	if (study == null) {
            return badRequest("Cannot edit objects: invalid study URI");
	}
	System.out.println("Study URI leaving EditObject's Study.find(): " + study.getUri());


	if (oc_uri == null || oc_uri.equals("")) {
            return badRequest("Cannot edit objects: empty object collection URI");
	}
	ObjectCollection oc = ObjectCollection.find(oc_uri);
	if (oc == null) {
            return badRequest("Cannot edit objects: invalid object collection URI");
	}	

	if (objUriList == null || objUriList.size() == 0) {
            return badRequest("Cannot edit objects: empty list of objects");
	}
	
	// old and new object lists
	List<StudyObject> oldObjList = new ArrayList<StudyObject>();
	List<StudyObject> newObjList = new ArrayList<StudyObject>();
	for (String oldUri : objUriList) {
	    oldUri = URLDecoder.decode(oldUri);
	    StudyObject obj = StudyObject.find(oldUri);
	    oldObjList.add(obj);
	}

	// get new values
        Form<ObjectsForm> form = Form.form(ObjectsForm.class).bindFromRequest();
        ObjectsForm data = form.get();
	List<String> newLabels = data.getNewLabel();
	List<String> newOriginalIds = data.getNewOriginalId();
	System.out.println("Total entries: " + newLabels.size());
	
	// compare and update accordingly 
	int totUpdates = 0;
	int nRowsAffected = 0;
	String message = "";
	StudyObject newObj;
	StudyObject oldObj;
	for (int i = 0; i < oldObjList.size(); i++) {
	    System.out.println("New Label: [" + newLabels.get(i).trim() + "]     " + 
			       "Old Label: [" + oldObjList.get(i).getLabel().trim() + "]     " + 
			       "OriginalId: [" + newOriginalIds.get(i) + "]");
	    oldObj = oldObjList.get(i);
	    if (!oldObj.getLabel().trim().equals(newLabels.get(i).trim()) || 
		!oldObj.getOriginalId().trim().equals(newOriginalIds.get(i).trim())) {

		// update objects and add to new list
		newObj = new StudyObject(oldObj.getUri(),
					 oldObj.getTypeUri(),
					 newOriginalIds.get(i),
					 newLabels.get(i),
					 oldObj.getIsMemberOf(),
					 oldObj.getComment(),
					 oldObj.getScopeUris() 
					 );
		try {
		    nRowsAffected = newObj.deleteFromLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		    if (nRowsAffected <= 0) {
			message = "Failed to delete object from LabKey";
		    }
		} catch (CommandException e) {
		    message = "ERROR Deleting object from LabKey ";
		}
		newObj.save();
		nRowsAffected = newObj.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
		    message = "Failed to insert object into LabKey";
		}
		newObjList.add(newObj);
		totUpdates++;

		// add unchanged objects to new list 
	    } else {
		newObjList.add(oldObj);
	    }
	}
	if (totUpdates > 0) {
	    message = " " + totUpdates + " object(s) was/were updated.";
	} else {
	    message = " no object was updated";
	}
	
        if (form.hasErrors()) {
            message = "The submitted form has errors!";
        }
        
	System.out.println("Study URI leaving EditObject: " + study.getUri());

    	return ok(objectManagement.render(filename, da_uri, study, oc, objUriList, newObjList, message));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result deleteCollectionObjects(String filename, String da_uri, String std_uri, String oc_uri, List<String> objUriList) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
	    return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			    org.hadatac.console.controllers.objects.routes.ObjectManagement.deleteCollectionObjects(filename, da_uri, std_uri, oc_uri, objUriList).url()));
    	}

    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
	std_uri = URLDecoder.decode(std_uri);
	oc_uri = URLDecoder.decode(oc_uri);
	
 	System.out.println("Study URI entering 1 EditObject: [" + std_uri + "]");
	if (std_uri == null || std_uri.equals("")) {
            return badRequest("Cannot edit objects: empty study URI");
	}
	Study study = Study.find(std_uri);
	if (study == null) {
            return badRequest("Cannot edit objects: invalid study URI");
	}
	System.out.println("Study URI leaving EditObject's Study.find(): " + study.getUri());


	if (oc_uri == null || oc_uri.equals("")) {
            return badRequest("Cannot edit objects: empty object collection URI");
	}
	ObjectCollection oc = ObjectCollection.find(oc_uri);
	if (oc == null) {
            return badRequest("Cannot edit objects: invalid object collection URI");
	}	

	if (objUriList == null || objUriList.size() == 0) {
            return badRequest("Cannot edit objects: empty list of objects");
	}
	
	// old and new object lists
	List<StudyObject> oldObjList = new ArrayList<StudyObject>();
	for (String oldUri : objUriList) {
	    oldUri = URLDecoder.decode(oldUri);
	    StudyObject obj = StudyObject.find(oldUri);
	    oldObjList.add(obj);
	}

	// compare and update accordingly 
	int totDeletes = 0;
	int nRowsAffected = 0;
	String message = "";
	StudyObject oldObj;
	for (int i = 0; i < oldObjList.size(); i++) {
	    oldObj = oldObjList.get(i);
	    try {
		nRowsAffected = oldObj.deleteFromLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
		    message = "Failed to delete object from LabKey";
		}
	    } catch (CommandException e) {
		message = "ERROR Deleting object from LabKey ";
	    }
	    oldObj.delete();
	    totDeletes++;
	}
	if (totDeletes > 0) {
	    message = " " + totDeletes + " object(s) was/were deleted.";
	} else {
	    message = " no object was deleted";
	}
	
    	return index(filename, da_uri, study.getUri(), oc.getUri(), message);
    }

}
