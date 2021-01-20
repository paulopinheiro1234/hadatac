package org.hadatac.console.controllers.deployments;

import org.hadatac.Constants;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.URIGeneratorForm;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ResourceAvailability extends Controller {

    // for /metadata HTTP GET requests
    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
    public Result index() {

        return ok(resourceAvailability.render(Instrument.findAvailable(), Instrument.findDeployed(), Detector.findAvailable(), Detector.findDeployed()));

    }// /index()


    // for /metadata HTTP POST requests
    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
    public Result postIndex() {

        return ok(resourceAvailability.render(Instrument.findAvailable(), Instrument.findDeployed(), Detector.findAvailable(), Detector.findDeployed()));

    }// /postIndex()

}
