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

public class CleanStudy extends Controller {

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result clean(String oper) {
		return ok(clean_study.render(oper));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postClean(String oper) {
		return ok(clean_study.render(oper));
    }

    public static String playClean(String oper) {
    	String result = "";
    	if (oper.equals("studies")) {
    		NameSpaces.getInstance();
    		DataContext acquisition = new 
	    		 DataContext("user", 
	    		             "password",
	    		             Play.application().configuration().getString("hadatac.solr.data"), 
	    		             false);
    		result = acquisition.cleanStudy(Feedback.WEB,"http://hadatac.org/kb/chear#STD-Pilot-3");
    		// TODO: need to pull in speified study from repository management and pass as argument, rather than hardcoded as was done for testing
    	}
	     return result;
   }

}
