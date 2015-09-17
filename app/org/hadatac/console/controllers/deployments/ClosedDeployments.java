package org.hadatac.console.controllers.deployments;

import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.GetSparqlQuery;
import org.hadatac.console.http.JsonHandler;

import java.io.IOException;
import java.util.TreeMap;

import org.hadatac.console.models.FacetsWithCategories;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.hierarchy_browser;
import org.hadatac.console.views.html.error_page;
import org.hadatac.console.views.html.deployments.*;


public class ClosedDeployments extends Controller {

	// for /metadata HTTP GET requests
    public static Result index() {
		String json = DeploymentQueries.exec(DeploymentQueries.ACTIVE_DEPLOYMENTS, "");
		SparqlQueryResults results = new SparqlQueryResults(json, false);
        return ok(deploymentManagement.render(results));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
		String json = DeploymentQueries.exec(DeploymentQueries.ACTIVE_DEPLOYMENTS, "");
		SparqlQueryResults results = new SparqlQueryResults(json, false);
        return ok(deploymentManagement.render(results));
        
    }// /postIndex()

}