package org.hadatac.console.controllers.metadata;

import play.mvc.Controller;
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

public class TermOverlap extends Controller {

	// for /metadata HTTP GET requests
    public Result index() {
    
    	Map<String, List<OntologyTerm>> t = new TermClusteror().getTermClusters();
        return ok(searchTermOverlap.render(t));
        
    }// /index()
}
