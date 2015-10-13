package org.hadatac.console.controllers.deployments;

import org.hadatac.console.models.URIGeneratorForm;

import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;

public class ResourceAvailability extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

       return ok(resourceAvailability.render(Instrument.findAvailable(), Instrument.findDeployed(), Detector.findAvailable(), Detector.findDeployed()));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return ok(resourceAvailability.render(Instrument.findAvailable(), Instrument.findDeployed(), Detector.findAvailable(), Detector.findDeployed()));
        
    }// /postIndex()

}
