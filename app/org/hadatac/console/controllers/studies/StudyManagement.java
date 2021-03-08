package org.hadatac.console.controllers.studies;

import java.util.List;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.entity.pojo.Study;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.studies.*;
import org.pac4j.play.java.Secure;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Controller;

import javax.inject.Inject;


public class StudyManagement extends Controller {

    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(Http.Request request) {
        List<Study> theResults = Study.find();

        return ok(studyManagement.render(theResults,application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(Http.Request request) {
        return index(request);
    }
}
