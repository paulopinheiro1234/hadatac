package org.hadatac.console.controllers.studies;

import java.util.List;

import org.hadatac.entity.pojo.Study;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.studies.*;
import play.mvc.Result;
import play.mvc.Controller;

public class StudyManagement extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
    	List<Study> theResults = Study.find();
    	
        return ok(studyManagement.render(theResults));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
        return index();
    }
}