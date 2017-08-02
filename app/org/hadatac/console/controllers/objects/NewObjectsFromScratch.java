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
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObjectType;
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

public class NewObjectsFromScratch extends Controller {
    
    static final long MAX_OBJECTS = 1000;
    static final long LENGTH_CODE = 6;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String std_uri, String oc_uri) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
	    return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			    org.hadatac.console.controllers.objects.routes.NewObjectsFromScratch.index(std_uri, oc_uri).url()));
    	}
	
	std_uri = URLDecoder.decode(std_uri);
	oc_uri = URLDecoder.decode(oc_uri);

	Study study = Study.find(std_uri);
	ObjectCollection oc = ObjectCollection.find(oc_uri);

	List<StudyObjectType> typeList = StudyObjectType.find();

	if (typeList == null) {
	    System.out.println("new objects: type list is null");
	} else {
	    for (StudyObjectType type : typeList) {
		System.out.println("new objects: type = [" + type.getUri() + "]");
	    }
	}
	
    	return ok(newObjectsFromScratch.render(study, oc, typeList));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String std_uri, String oc_uri) {
    	return index(std_uri, oc_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String std_uri, String oc_uri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
	Study std = Study.find(std_uri);
	ObjectCollection oc = ObjectCollection.find(oc_uri);
	
        Form<ObjectsForm> form = Form.form(ObjectsForm.class).bindFromRequest();
        ObjectsForm data = form.get();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        
	// store new values
	System.out.println("============================ NEW OBJECTS =======================");
	System.out.println("Study: [" + std.getUri() + "]");
	System.out.println("OC: [" + oc.getUri() + "]");
	System.out.println("type: [" + data.getNewType() + "]");
	System.out.println("Quantity : " + data.getNewQuantity());
	
	long quantity = Long.parseLong(data.getNewQuantity());
	if ((quantity > 0) && (quantity <= MAX_OBJECTS)) {
	    std.requestId(quantity);
	}
	long nextId = std.getLastId() + 1;
	System.out.println("nextId : " + nextId);
    
	// Fixed values
	String newType = null;
	if (data.getNewType() == null || data.getNewType().equals("")) {
	    return badRequest("[ERROR] New type cannot be empty.");
	} else {
	    newType = ValueCellProcessing.replacePrefixEx(data.getNewType());
	}
	String newObjectCollectionUri = ValueCellProcessing.replacePrefixEx(oc_uri);
	
	// Variable values
	String newURI = null;
	String newLabel = null;
	String newComment = null;

	// Object object
	StudyObject obj = null;
	
	if (quantity > 0) {
	    for (int i=0; i < quantity; i++) {
		
		 if (oc.getType().equals("http://hadatac.org/ont/hasco/SubjectGroup")) {
		     newURI = oc.getUri().replace("OC-","SUBJ-") + "-" + formattedCounter(nextId);
		     newLabel = "Subject " + nextId;
		 } else if(oc.getType().equals("http://hadatac.org/ont/hasco/LocationCollection")) {
		     newURI = oc.getUri().replace("OC-","LOC-") + "-" + formattedCounter(nextId);
		     newLabel = "Location " + nextId;
		 } else {
		     newURI = oc.getUri().replace("OC-","MAT-") + "-" + formattedCounter(nextId);
		     newLabel = "Material " + nextId;
                 }
		newComment = newLabel;
		
		// insert current state of the OBJ
		obj = new StudyObject(newURI,
				      newType,
				      "", // Original ID
				      newLabel,
				      newObjectCollectionUri,
				      newComment,
				      "" // IsObjectOf 
				      );
		
		// insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
		obj.save();
		
		// update/create new OBJ in LabKey
		int nRowsAffected = obj.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
		    return badRequest("Failed to insert new OBJ to LabKey!\n");
		}

		oc.getObjectUris().add(obj.getUri());
		nextId++;
		
	    }
	}
	String message = "A total of " + quantity + " new object(s) have been Generated";
	return ok(objectConfirm.render(message, std_uri, oc));
    }
    
    static private String formattedCounter (long value) {
	String strLong = Long.toString(value).trim();
	for (int i=strLong.length(); i < LENGTH_CODE; i++) {
	    strLong = "0" + strLong;
	}
	return strLong;
    }
    
}
