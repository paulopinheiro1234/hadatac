package org.hadatac.console.controllers.triplestore;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.ForceFieldQuery;

import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.views.html.triplestore.userNetwork;
import org.hadatac.utils.CollectionUtil;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import scala.concurrent.stm.skel.HashTrieTMap;

import javax.inject.Inject;


public class UserNetwork extends Controller {
    @Inject
    Application application;

	@Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result index(Http.Request request) {
    	ForceFieldQuery ffq = new ForceFieldQuery(CollectionUtil.Collection.PERMISSIONS_SPARQL);
        return ok(userNetwork.render(ffq.getQueryResult().replace("\n", " "),application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postIndex(Http.Request request) {
        return index(request);
    }
}
