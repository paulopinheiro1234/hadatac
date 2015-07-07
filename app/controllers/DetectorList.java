package controllers;

import http.GetSparqlQuery;
import http.JsonHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import models.FacetsWithCategories;
import models.SparqlQuery;
import models.SparqlQueryResults;
import models.TreeQuery;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.metadata_list;


public class DetectorList extends Controller {

    public static SparqlQueryResults query_results = new SparqlQueryResults();
    
    // for /metadata HTTP GET requests
    public static Result index() {
    	String category = "Detectors";
    	//Get query using http.GetSparqlQuery
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        // query_submit contains 7 queries: one for each thingType right now
        String query_json = null;
        try {
        	query_json = query_submit.executeQuery(category);
        } catch (IllegalStateException | IOException e1) {
        	e1.printStackTrace();
        }
        SparqlQueryResults query_results = new SparqlQueryResults(query_json);
        
        System.out.println("instrumentlist index() was called!");
        
        return ok(metadata_list.render(query_results, category));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
    	String category = "Detectors";
    	//Get query using http.GetSparqlQuery
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        // query_submit contains 7 queries: one for each thingType right now
        String query_json = null;
        try {
        	query_json = query_submit.executeQuery(category);
        } catch (IllegalStateException | IOException e1) {
        	e1.printStackTrace();
        }
        SparqlQueryResults query_results = new SparqlQueryResults(query_json);
        
        System.out.println("instrumentlist index() was called!");
        
        return ok(metadata_list.render(query_results, category));
        
    }// /postIndex()

}
