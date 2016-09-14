package org.hadatac.console.controllers.metadataacquisition;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.utils.Collections;


public class SchemaAttribute extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {
    	String collection = Play.application().configuration().getString("hadatac.console.host_deploy") + 
    			request().path() + "/solrsearch";
       return ok(schema_attributes.render(collection));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        return index();
    }// /postIndex()

}
