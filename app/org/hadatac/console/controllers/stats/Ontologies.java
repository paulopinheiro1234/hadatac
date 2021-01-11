package org.hadatac.console.controllers.stats;

import java.util.List;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.Constants;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.stats.*;
import play.mvc.Result;
import play.mvc.Controller;

public class Ontologies extends Controller {

    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
    public Result index() {
    	
        return ok(ontologies.render());
    }

    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
    public Result postIndex() {
        return index();
    }
}