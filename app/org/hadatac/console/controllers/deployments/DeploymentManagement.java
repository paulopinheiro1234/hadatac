package org.hadatac.console.controllers.deployments;

import java.util.List;

import org.hadatac.entity.pojo.Deployment;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.deployments.*;
import play.mvc.Result;
import play.mvc.Controller;

public class DeploymentManagement extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(int option) {
    	State state = new State(option);
    	List<Deployment> theResults = Deployment.find(state);
    	
        return ok(deploymentManagement.render(state, theResults));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(int option) {
        State state = new State(option);
        List<Deployment> theResults = Deployment.find(state);
        	
        return ok(deploymentManagement.render(state, theResults));
    }
}