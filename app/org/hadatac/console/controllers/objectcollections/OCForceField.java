package org.hadatac.console.controllers.objectcollections;


import org.hadatac.Constants;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.objectcollections.OCForceFieldGraph;
import org.hadatac.console.views.html.objectcollections.*;

import play.mvc.Controller;
import play.mvc.Result;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class OCForceField extends Controller {

    @Restrict(@Group(Constants.DATA_MANAGER_ROLE))
	public Result index(String mode, String dir, String filename, String da_uri, String std_uri) {
    	OCForceFieldGraph graph = new OCForceFieldGraph(mode, std_uri);
        return ok(ocForceField.render(graph.getQueryResult().replace("\n", " "), dir, filename, da_uri, std_uri));
    }

    @Restrict(@Group(Constants.DATA_MANAGER_ROLE))
	public Result postIndex(String mode, String dir, String filename, String da_uri, String std_uri) {
        return index(mode, dir, filename, da_uri, std_uri);
    }
}
