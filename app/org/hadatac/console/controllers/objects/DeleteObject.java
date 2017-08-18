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

public class DeleteObject extends Controller {
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String filename, String da_uri, String std_uri, String oc_uri, String obj_id) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
	    return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			    org.hadatac.console.controllers.objects.routes.DeleteObject.index(filename, da_uri, std_uri, oc_uri, obj_id).url()));
    	}
	std_uri = URLDecoder.decode(std_uri);
	oc_uri = URLDecoder.decode(oc_uri);
	//System.out.println("In DeleteOC: std_uri = [" + std_uri + "]");
	//System.out.println("In DeleteOC: oc_uri = [" + oc_uri + "]");

	Study study = Study.find(std_uri);
	if (study == null) {
	    return badRequest(objectConfirm.render("Error editing object: Study URI did not return valid URI", filename, da_uri, std_uri, oc_uri));
	} 

	ObjectCollection oc = ObjectCollection.find(oc_uri);
	if (oc == null) {
	    return badRequest(objectConfirm.render("Error editing object: ObjectCollection URI did not return valid object", filename, da_uri, std_uri, oc_uri));
	} 

	List<StudyObject> objects = StudyObject.findByCollection(oc);

    	//return ok(editObject.render(study, oc, objects));
	return badRequest(objectConfirm.render("PLACEHOLDER", filename, da_uri, std_uri, oc_uri));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String filename, String da_uri, String std_uri, String oc_uri, String obj_id) {
    	return index(filename, da_uri, std_uri, oc_uri, obj_id);
    }
    
}
