package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.data.loader.DataContext;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.PermissionsContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class Clean extends Controller {

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result clean(String oper) {
	return ok(clean.render(oper));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postClean(String oper) {
	return ok(clean.render(oper));
    }

    public static String playClean(String oper) {
    	String result = "";
    	if (oper.equals("metadata")) {
    		NameSpaces.getInstance();
    		MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    		                 "password",
	    		                  Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    		                  false);
    		result = metadata.clean(Feedback.WEB);
    	} else if (oper.equals("usergraph")) {
    		NameSpaces.getInstance();
    		PermissionsContext permission = new 
	    		 PermissionsContext("user", 
	    		                 "password",
	    		                  Play.application().configuration().getString("hadatac.solr.permissions"), 
	    		                  false);
    		result = permission.clean(Feedback.WEB);
    	} else if (oper.equals("collections")) {
    		NameSpaces.getInstance();
    		DataContext collection = new 
	    		 DataContext("user", 
	    		             "password",
	    		             Play.application().configuration().getString("hadatac.solr.data"), 
	    		             false);
    		result = collection.cleanDataAcquisitions(Feedback.WEB);
    	} else if (oper.equals("acquisitions")) {
    		NameSpaces.getInstance();
    		DataContext acquisition = new 
	    		 DataContext("user", 
	    		             "password",
	    		             Play.application().configuration().getString("hadatac.solr.data"), 
	    		             false);
    		result = acquisition.cleanDataAcquisitions(Feedback.WEB);
    	}
	     return result;
   }

}
