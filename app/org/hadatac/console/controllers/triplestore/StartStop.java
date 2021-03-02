package org.hadatac.console.controllers.triplestore;

import com.google.inject.Inject;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import play.*;
import play.mvc.*;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.utils.Repository;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class StartStop extends Controller {

	@Inject
	Application application;

	@Restrict(@Group(Constants.DATA_MANAGER_ROLE))
	public Result index(String oper, String repository, Http.Request request) {
		String message = Repository.startStopMetadataRepository(oper, repository);
		if (message.equals("FAIL")) {
			return ok(clean.render("doneNotOk", application.getUserEmail(request)));
		} 
		return ok(clean.render("doneOk", application.getUserEmail(request)));
    }

	@Restrict(@Group(Constants.DATA_MANAGER_ROLE))
    public Result postIndex(String oper, String repository, Http.Request request) {
		String message = Repository.startStopMetadataRepository(oper, repository);
		if (message.equals("FAIL")) {
			return ok(clean.render("doneNotOk", application.getUserEmail(request)));
		}
		return ok(clean.render("doneOk", application.getUserEmail(request)));
    }

}
