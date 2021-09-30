package org.hadatac.console.controllers.indicators;

import java.util.List;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.entity.pojo.Indicator;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.indicators.*;
import org.pac4j.play.java.Secure;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Controller;
import scala.concurrent.stm.skel.HashTrieTMap;

import javax.inject.Inject;

public class IndicatorManagement extends Controller {
    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(Http.Request request) {
        List<Indicator> theResults = Indicator.findSubClasses();
        return ok(indicatorManagement.render(theResults,application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(Http.Request request) {
        return index(request);
    }
}