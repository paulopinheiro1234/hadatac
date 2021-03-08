package org.hadatac.console.controllers.deployments;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.URIGeneratorForm;

import org.pac4j.play.java.Secure;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import javax.inject.Inject;

public class ResourceAvailability extends Controller {
    @Inject
    Application application;

    // for /metadata HTTP GET requests
    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(Http.Request request) {

        return ok(resourceAvailability.render(Instrument.findAvailable(), Instrument.findDeployed(), Detector.findAvailable(), Detector.findDeployed(),application.getUserEmail(request)));

    }// /index()


    // for /metadata HTTP POST requests
    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(Http.Request request) {

        return ok(resourceAvailability.render(Instrument.findAvailable(), Instrument.findDeployed(), Detector.findAvailable(), Detector.findDeployed(),application.getUserEmail(request)));

    }// /postIndex()

}
