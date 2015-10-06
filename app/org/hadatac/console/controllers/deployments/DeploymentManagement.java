package org.hadatac.console.controllers.deployments;

import java.util.List;

import org.hadatac.entity.pojo.Deployment;
import org.hadatac.utils.State;
import org.hadatac.console.views.html.deployments.*;
import play.mvc.Result;
import play.mvc.Controller;

public class DeploymentManagement extends Controller {

	// for /metadata HTTP GET requests
    public static Result index(int option) {
    	State state = new State(option);
    	List<Deployment> theResults = Deployment.find(state);
    	
        return ok(deploymentManagement.render(state, theResults));
        
    }// /index()

    // for /metadata HTTP POST requests
    public static Result postIndex(int option) {
        State state = new State(option);
        List<Deployment> theResults = Deployment.find(state);
        	
        return ok(deploymentManagement.render(state, theResults));
            
    }// /index()

}