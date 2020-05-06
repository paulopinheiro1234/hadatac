package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.utils.Repository;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class StartStop extends Controller {

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result index(String oper, String repository) {	    
		String message = Repository.startStopMetadataRepository(oper, repository);
		if (message.equals("FAIL")) {
			return ok(clean.render("doneNotOk"));
		} 
		return ok(clean.render("doneOk"));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postIndex(String oper, String repository) {
		String message = Repository.startStopMetadataRepository(oper, repository);
		if (message.equals("FAIL")) {
			return ok(clean.render("doneNotOk"));
		}
		return ok(clean.render("doneOk"));
    }

}
