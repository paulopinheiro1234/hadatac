package org.hadatac.console.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;

import javax.xml.ws.http.HTTPException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.views.html.main;
import org.hadatac.console.views.html.portal;
import org.hadatac.utils.Collections;

public class Portal extends Controller {
	
    public static Result index() {
    	if (!checkTripleStoreConnection()) {
    		return ok(main.render("Results", "", 
    				new Html("<div><h4>The triple store is NOT properly connected. "
    						+ "Please restart it or check the hadatac configuration file!</h4></div>")));
    	}
    	return ok(portal.render()); 
    }

    public static Result postIndex() {
    	return ok(portal.render());
    }

    private static boolean checkTripleStoreConnection() {
    	String queryString = "SELECT (COUNT(*) as ?count) WHERE { ?s ?p ?o . } ";
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qe;
		try {
			qe = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
			qe.execSelect();
			
			qe = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL), query);
			qe.execSelect();
		} catch (QueryExceptionHTTP e) {
			return false;
		}
		
		return true;
    }
}
