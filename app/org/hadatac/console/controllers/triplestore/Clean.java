package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;
import play.libs.*;

import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

public class Clean extends Controller {

    public static Result clean(String oper) {
	return ok(clean.render(oper));
    }

    public static Result postClean(String oper) {
	return ok(clean.render(oper));
    }

    public static String playClean() {
	     NameSpaces.getInstance();
	     MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    		                 "password",
	    		                  Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    		                  false);
	     return metadata.clean(Feedback.WEB);
   }

}
