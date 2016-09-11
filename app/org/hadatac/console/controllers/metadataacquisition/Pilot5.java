package org.hadatac.console.controllers.metadataacquisition;

import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.utils.Collections;


public class Pilot5 extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {
       String collection = Collections.getCollectionsName(Collections.SUBJECTS_ACQUISITION);
       return ok(pilot5.render(collection));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
    	String collection = Collections.getCollectionsName(Collections.SUBJECTS_ACQUISITION);
        return ok(pilot5.render(collection));
        
    }// /postIndex()

}
