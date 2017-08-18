package org.hadatac.console.controllers.indicators;

import java.util.List;

import org.hadatac.entity.pojo.Indicator;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.indicators.*;
import play.mvc.Result;
import play.mvc.Controller;

public class IndicatorManagement extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
    	List<Indicator> theResults = Indicator.find();
        return ok(indicatorManagement.render(theResults));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
        return index();
    }
}