package org.hadatac.console.controllers.triplestore;

import org.hadatac.console.models.ForceFieldQuery;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.triplestore.userNetwork;
import org.hadatac.utils.Collections;


public class UserNetwork extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

    	ForceFieldQuery ffq = new ForceFieldQuery(Collections.PERMISSIONS_SPARQL);
    	
        System.out.println("Agent index() was called!");
        return ok(userNetwork.render(ffq.getQueryResult().replace("\n", " ")));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
    	ForceFieldQuery ffq = new ForceFieldQuery(Collections.PERMISSIONS_SPARQL);
    	
        System.out.println("Agent index() was called!");
        return ok(userNetwork.render(ffq.getQueryResult().replace("\n", " ")));
        
    }// /postIndex()

}
