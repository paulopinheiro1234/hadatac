package org.hadatac.console.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.portal;

public class Portal extends Controller {
	
    public static Result index() {
    	System.out.println("Portal.index() is called");
    	return ok(portal.render()); 
    }

    public static Result postIndex() {
    	return ok(portal.render());
    }

}
