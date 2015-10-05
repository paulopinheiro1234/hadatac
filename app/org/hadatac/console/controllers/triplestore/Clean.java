package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;
import play.libs.*;

import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.PermissionsContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

public class Clean extends Controller {

    public static Result clean(String oper) {
	return ok(clean.render(oper));
    }

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
    	} else if (oper.equals("acquisitions:")) {
    	}
	     return result;
   }

}
