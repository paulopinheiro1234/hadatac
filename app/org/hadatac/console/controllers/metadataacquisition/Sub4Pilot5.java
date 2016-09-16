package org.hadatac.console.controllers.metadataacquisition;



import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.utils.Collections;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class Sub4Pilot5 extends Controller {
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
		
    	return ok(sub4Pilot5.render());
    	
    }// /index()


    // for /metadata HTTP POST requests
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
        return index();
        
    }// /postIndex()

}