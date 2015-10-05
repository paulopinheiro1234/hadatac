package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;

import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.utils.Repository;

public class StartStop extends Controller {

	public static Result index(String oper, String repository) {	    
		String message = Repository.startStopMetadataRepository(oper, repository);
		if (message.equals("FAIL")) {
			return ok(clean.render("doneNotOk"));
		} 
		return ok(clean.render("doneOk"));
    }

    public static Result postIndex(String oper, String repository) {
		String message = Repository.startStopMetadataRepository(oper, repository);
		if (message.equals("FAIL")) {
			return ok(clean.render("doneNotOk"));
		}
		return ok(clean.render("doneOk"));
    }

}
