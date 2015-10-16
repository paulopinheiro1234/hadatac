package org.hadatac.console.controllers;

import org.hadatac.console.models.ForceFieldQuery;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadata.concepts.agent_browser;


public class Agent extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

    	String category = "Agents";
    	
    	ForceFieldQuery ffq = new ForceFieldQuery();
    	
        System.out.println("Agent index() was called!");
        return ok(agent_browser.render(ffq.getQueryResult().replace("\n", " "), category));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
    	String category = "Agents";
    	
    	ForceFieldQuery ffq = new ForceFieldQuery();
    	
        System.out.println("Agent index() was called!");
        return ok(agent_browser.render(ffq.getQueryResult().replace("\n", " "), category));
        
    }// /postIndex()

}
