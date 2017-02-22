package org.hadatac.console.controllers.triplestore;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.ForceFieldQuery;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.triplestore.userNetwork;
import org.hadatac.utils.Collections;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class UserNetwork extends Controller {

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result index() {
    	ForceFieldQuery ffq = new ForceFieldQuery(Collections.PERMISSIONS_SPARQL);
        return ok(userNetwork.render(ffq.getQueryResult().replace("\n", " ")));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postIndex() {
        return index();
    }
}
