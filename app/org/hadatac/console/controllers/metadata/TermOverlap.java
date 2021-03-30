package org.hadatac.console.controllers.metadata;

import org.hadatac.console.controllers.Application;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.Map;

import org.hadatac.console.views.html.metadata.*;
import org.hadatac.console.controllers.schema.EditingOptions;
import org.hadatac.console.models.ForceFieldQuery;
import org.hadatac.console.models.OtMSparqlQueryResults;
import org.hadatac.utils.Hierarchy;
import org.hadatac.utils.OntologyTerm;
import org.hadatac.utils.TermClusteror;

import javax.inject.Inject;

public class TermOverlap extends Controller {
    @Inject
    Application application;

	// for /metadata HTTP GET requests
    public Result index(Http.Request request) {
    
    	Map<String, List<OntologyTerm>> t = new TermClusteror().getTermClusters();
        return ok(searchTermOverlap.render(t, application.getUserEmail(request)));
        
    }// /index()
}
