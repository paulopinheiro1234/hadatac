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
import models.TreeQueryResults;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.formdata.FacetFormData;
import views.html.hierarchy_faceting;
import views.html.error_page;


public class Hierarchy extends Controller {

    public static SparqlQueryResults query_results = new SparqlQueryResults();
    
    // for /metadata HTTP GET requests
    public static Result index() {
    	//Get query using http.GetSparqlQuery
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        // query_submit contains 7 queries: one for each thingType right now
        TreeMap<String, SparqlQueryResults> query_results_list = new TreeMap<String, SparqlQueryResults>();
        TreeMap<String, String> hierarchy_results_list = new TreeMap<String, String>();
    	for (String tabName : query_submit.thingTypes){
        	if (tabName.endsWith("H")) {
        		System.out.println("Hierarchy.java is requesting: " + tabName);
        		TreeQuery tq;
        		if (tabName.equals("InstrumentModelsH")) 
        			tq = new TreeQuery(tabName, false);
        		else 
        			tq = new TreeQuery(tabName, true);
                hierarchy_results_list.put(tabName, tq.getQueryResult().replace("\n", " "));
        	} else {
        		String query_json = null;
        		try {
        			query_json = query_submit.executeQuery(tabName);
        		} catch (IllegalStateException | IOException e1) {
        			e1.printStackTrace();
        		}
        		SparqlQueryResults query_results = new SparqlQueryResults(query_json, tabName);
        		query_results_list.put(tabName, query_results);
        	}
        }// /for tabname in types of entities
        
        //Get the facets
        //getFacets(jh);

        System.out.println("hierarchy index() was called!");
        System.out.println("mapping 1 size is " + query_results_list.size());
        System.out.println("mapping 2 size is " + hierarchy_results_list.size());
        
        return ok(hierarchy_faceting.render(query_results_list, hierarchy_results_list, "All Documents"));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
    	//Get query using http.GetSparqlQuery
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        // query_submit contains 7 queries: one for each thingType right now
        TreeMap<String, SparqlQueryResults> query_results_list = new TreeMap<String, SparqlQueryResults>();
        TreeMap<String, String> hierarchy_results_list = new TreeMap<String, String>();
    	for (String tabName : query_submit.thingTypes){
        	if (tabName.endsWith("H")) {
        		System.out.println("Hierarchy.java is requesting: " + tabName);
        		TreeQuery tq;
        		if (tabName.equals("InstrumentModelsH")) 
        			tq = new TreeQuery(tabName, false);
        		else 
        			tq = new TreeQuery(tabName, true);
                hierarchy_results_list.put(tabName, tq.getQueryResult().replace("\n", " "));
        	} else {
        		String query_json = null;
        		try {
        			query_json = query_submit.executeQuery(tabName);
        		} catch (IllegalStateException | IOException e1) {
        			e1.printStackTrace();
        		}
        		SparqlQueryResults query_results = new SparqlQueryResults(query_json, tabName);
        		query_results_list.put(tabName, query_results);
        	}
        }// /for tabname in types of entities
        
        //Get the facets
        //getFacets(jh);

        System.out.println("hierarchy index() was called!");
        System.out.println("mapping 1 size is " + query_results_list.size());
        System.out.println("mapping 2 size is " + hierarchy_results_list.size());
        
        return ok(hierarchy_faceting.render(query_results_list, hierarchy_results_list, "All Documents"));
        
    }// /postIndex()

}
