package org.hadatac.console.controllers.dataanalysis;

import java.util.List;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.dataanalysis.*;
import org.hadatac.entity.pojo.Indicator;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

public class FunctionManagement extends Controller {

    @Inject
    Application application;
    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result index(Http.Request request) {
    	List<Indicator> theResults = Indicator.find();
        return ok(functionManagement.render(theResults,application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postIndex(Http.Request request) {
        return index(request);
    }
}
