package org.hadatac.console.controllers.deployments;

import java.util.List;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.deployments.*;
import org.pac4j.play.java.Secure;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Controller;

import javax.inject.Inject;

public class DeploymentManagement extends Controller {
    @Inject
    Application application;

    final static int PAGESIZE = 12;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(int option, String dir, String filename, String da_uri, int offset, int details, Http.Request request) {
        State state = new State(option);
        List<Deployment> deployments = Deployment.findWithPages(state, PAGESIZE, offset * PAGESIZE);
        int total = Deployment.getNumberDeployments(state);
        return ok(deploymentManagement.render(state, dir, filename, da_uri, total, PAGESIZE, offset, deployments, details, application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(int option, String dir, String filename, String da_uri, int offset, int details, Http.Request request) {
        return index(option, dir, filename, da_uri, offset, details, request);
    }

}