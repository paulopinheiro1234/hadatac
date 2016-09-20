package org.hadatac.console.controllers.metadataacquisition;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.utils.Collections;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DataAcquisitionFacet extends Controller {
    
    // for /dataacquisitions HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() { 
	    String collection = Play.application().configuration().getString("hadatac.console.host_deploy") + 
   	 		request().path() + "/solrsearch";
	    return ok(dataacquisitionfacet.render(collection));
    }// /index()    // for /dataacquisitions HTTP POST requests
   
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
        return index();
    }// /postIndex()}
   
}