package org.hadatac.console.controllers.metadataacquisition;

import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadataacquisition.*;


public class SchemaAttribute extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

       return ok(schema_attributes.render());
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return ok(schema_attributes.render());
        
    }// /postIndex()

}
