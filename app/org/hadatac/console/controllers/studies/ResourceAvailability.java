package org.hadatac.console.controllers.studies;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.URIGeneratorForm;

import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.studies.*;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ResourceAvailability extends Controller {
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {

       return ok(resourceAvailability.render(Instrument.findAvailable(), Instrument.findDeployed(), Detector.findAvailable(), Detector.findDeployed()));
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
        
        return ok(resourceAvailability.render(Instrument.findAvailable(), Instrument.findDeployed(), Detector.findAvailable(), Detector.findDeployed()));
        
    }// /postIndex()

}
