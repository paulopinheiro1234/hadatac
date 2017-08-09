package org.hadatac.console.controllers.objectcollections;


import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.objectcollections.OCForceFieldGraph;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.objectcollections.*;
import org.hadatac.utils.Collections;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class OCForceField extends Controller {

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result index(String mode, String std_uri) {
    	OCForceFieldGraph graph = new OCForceFieldGraph(mode, std_uri);
        return ok(ocForceField.render(graph.getQueryResult().replace("\n", " "), std_uri));
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result postIndex(String mode, String std_uri) {
        return index(mode, std_uri);
    }
}
