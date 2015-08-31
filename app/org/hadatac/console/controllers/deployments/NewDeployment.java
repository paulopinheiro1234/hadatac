package org.hadatac.console.controllers.deployments;

import org.hadatac.console.http.GetSparqlQuery;
import java.io.IOException;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.TreeQuery;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.deployments.*;


public class NewDeployment extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

       return ok(deploymentManagement.render());
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return ok(deploymentManagement.render());
        
    }// /postIndex()

}
