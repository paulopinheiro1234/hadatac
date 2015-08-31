package org.hadatac.console.controllers.deployments;


import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.deployments.*;


public class CloseDeployment extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

       return ok(deploymentManagement.render());
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return ok(deploymentManagement.render());
        
    }// /postIndex()

}
