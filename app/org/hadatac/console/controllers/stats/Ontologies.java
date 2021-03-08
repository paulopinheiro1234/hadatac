package org.hadatac.console.controllers.stats;

import java.util.List;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.stats.*;
import org.pac4j.play.java.Secure;
import org.w3c.dom.html.HTMLTableCaptionElement;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Controller;

import javax.inject.Inject;

public class Ontologies extends Controller {
    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(Http.Request request) {
    	
        return ok(ontologies.render(application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(Http.Request request) {
        return index(request);
    }
}