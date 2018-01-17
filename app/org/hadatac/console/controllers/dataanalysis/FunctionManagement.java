package org.hadatac.console.controllers.dataanalysis;

import java.util.List;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.dataanalysis.*;
import org.hadatac.entity.pojo.Indicator;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.mvc.Controller;
import play.mvc.Result;

public class FunctionManagement extends Controller {
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result index() {
    	List<Indicator> theResults = Indicator.find();
        return ok(functionManagement.render(theResults));
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postIndex() {
        return index();
    }
}
