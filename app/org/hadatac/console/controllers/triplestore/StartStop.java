package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;
import play.libs.*;

import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.utils.Command;
import org.hadatac.utils.Feedback;

public class StartStop extends Controller {

	private static final String METADATA_SCRIPT = "run_solr4.sh";
	private static final String DATA_SCRIPT = "run_solr5.sh";
	
    public static Result index(String oper, String repository) {
    	String home = Play.application().configuration().getString("hadatac.solr.home");
    	if (!home.endsWith("/")) {
    		home = home + "/";
    	}
    	String message = "";
    	String script = "";
    	if (repository.equals("metadata")) {
    		script = home + METADATA_SCRIPT;
    	} else if (repository.equals("data")) {
    	    script = home + DATA_SCRIPT;	
    	} else {
    		System.out.println("StartStop: Invalid request");
    		return ok(clean.render("doneNotOk"));
    	}
	    String[] cmd = {script, oper};
		message += Feedback.print(Feedback.WEB, "Requested " + oper + " " + repository + " repository.");                
	    message += Command.exec(Feedback.WEB, false, cmd);
	    
	    System.out.println(message);
	    
	    return ok(clean.render("doneOk"));
    }

    public static Result postIndex(String oper, String repository) {
    	String home = Play.application().configuration().getString("hadatac.solr.home");
    	if (!home.endsWith("/")) {
    		home = home + "/";
    	}
    	String message = "";
    	String script = "";
    	if (repository.equals("metadata")) {
    		script = home + METADATA_SCRIPT;
    	} else if (repository.equals("data")) {
    	    script = home + DATA_SCRIPT;	
    	} else {
    		System.out.println("StartStop: Invalid request");
    		return ok(clean.render("doneNotOk"));
    	}
	    String[] cmd = {script, oper};
		message += Feedback.print(Feedback.WEB, "Requested " + oper + " " + repository + " repository.");                
	    message += Command.exec(Feedback.WEB, false, cmd);

	    System.out.println(message);

	    return ok(clean.render("doneOk"));
    }

}
