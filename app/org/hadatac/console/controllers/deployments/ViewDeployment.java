package org.hadatac.console.controllers.deployments;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.hadatac.Constants;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.deployments.*;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class ViewDeployment extends Controller {

    private static State allState = new State(State.ALL);

    // for /metadata HTTP GET requests
    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
    public Result index(String deployment_uri, String prev_plat_uri) {

        //DeploymentForm dep = new DeploymentForm();
        Deployment deployment = null;
        List<STR> dataCollections = null;

        try {
            if (deployment_uri != null) {
                deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
            } else {
                deployment_uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!deployment_uri.equals("")) {
            deployment = Deployment.find(deployment_uri);
            dataCollections = STR.find(deployment, false);
        }

        String urlReturn = null;
        if (prev_plat_uri == null || prev_plat_uri.isEmpty()) {
            urlReturn = "";
        } else {
            urlReturn = prev_plat_uri;
        }

        return ok(viewDeployment.render(deployment, dataCollections, urlReturn));


    }// /index()


    // for /metadata HTTP POST requests
    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
    public Result postIndex(String deployment_uri, String prev_plat_uri) {
        return index(deployment_uri, prev_plat_uri);
    }// /postIndex()

}
