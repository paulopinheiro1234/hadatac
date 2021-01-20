package org.hadatac.console.controllers.triplestore;

import org.hadatac.Constants;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.ForceFieldQuery;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.triplestore.userNetwork;
import org.hadatac.utils.CollectionUtil;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class UserNetwork extends Controller {

	@Restrict(@Group(Constants.DATA_MANAGER_ROLE))
    public Result index() {
    	ForceFieldQuery ffq = new ForceFieldQuery(CollectionUtil.Collection.PERMISSIONS_SPARQL);
        return ok(userNetwork.render(ffq.getQueryResult().replace("\n", " ")));
    }

	@Restrict(@Group(Constants.DATA_MANAGER_ROLE))
    public Result postIndex() {
        return index();
    }
}
