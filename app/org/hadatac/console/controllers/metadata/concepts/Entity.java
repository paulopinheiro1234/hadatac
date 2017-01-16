package org.hadatac.console.controllers.metadata.concepts;

import org.hadatac.console.http.GetSparqlQuery;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.OtMSparqlQueryResults;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadata.concepts.entity_browser;
import org.hadatac.console.views.html.error_page;


public class Entity extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

    	SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        OtMSparqlQueryResults theResults;
        String tabName = "Entities";
        String query_json = null;
        System.out.println("Entity.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            theResults = new OtMSparqlQueryResults(query_json, true);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("Entity index() was called!");
    	
    	//List<org.hadatac.entity.pojo.Entity> entities = org.hadatac.entity.pojo.Entity.find();
    	
        return ok(entity_browser.render(theResults, "Entities"));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {

    	SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        OtMSparqlQueryResults theResults;
        String tabName = "Entities";
        String query_json = null;
        System.out.println("Entity.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            theResults = new OtMSparqlQueryResults(query_json, true);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("Entity index() was called!");
    	
    	//List<org.hadatac.entity.pojo.Entity> entities = org.hadatac.entity.pojo.Entity.find();
    	
        return ok(entity_browser.render(theResults, "Entities"));
    }// /postIndex()

}
