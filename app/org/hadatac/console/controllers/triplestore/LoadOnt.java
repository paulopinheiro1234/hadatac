package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;
import play.libs.*;

import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.Feedback;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.NameSpaces;

public class LoadOnt extends Controller {

    public static Result loadOnt(String oper) {
	return ok(loadOnt.render(oper));
    }

    public static Result postLoadOnt(String oper) {
	return ok(loadOnt.render(oper));
    }

    public static String playLoadOntologies() {
	     NameSpaces.getInstance();
	     MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    				         false);
	     return metadata.loadOntologies(Feedback.WEB);
    }

}
