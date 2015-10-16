package org.hadatac.console.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadata.metadata;


public class Metadata extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

       return ok(metadata.render());
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return ok(metadata.render());
        
    }// /postIndex()

}
