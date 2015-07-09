package controllers;

import http.GetSparqlQuery;
import http.JsonHandler;

import java.io.IOException;
import java.util.TreeMap;

import models.FacetsWithCategories;
import models.SparqlQuery;
import models.SparqlQueryResults;
import models.TreeQueryResults;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.hierarchy_browser;
import views.html.error_page;


public class PlatformList extends Controller {

    public static SparqlQueryResults query_results = new SparqlQueryResults();
    
    // for /metadata HTTP GET requests
    public static Result index() {
    	//Get query using http.GetSparqlQuery
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        String[] tabsToQuery = {"Platforms"}; 

        TreeMap<String, SparqlQueryResults> query_results_list = new TreeMap<String, SparqlQueryResults>();
        TreeMap<String, String> hierarchy_results_list = new TreeMap<String, String>();
        for (String tabName : tabsToQuery){
            String query_json = null;
            if (tabName.endsWith("H")) {
                System.out.println("PlatformList.java is requesting: " + tabName);
                try {
                    query_json = query_submit.executeQuery(tabName);
                    TreeQueryResults query_results = new TreeQueryResults(query_json, false);
                    hierarchy_results_list.put(tabName, query_results.getQueryResult().replace("\n", " "));
                } catch (IllegalStateException | IOException | NullPointerException e1) {
                    return internalServerError(error_page.render(e1.toString(), "Platforms"));
                    //e1.printStackTrace();
                }
            } else {
                try {
                    query_json = query_submit.executeQuery(tabName);
                    SparqlQueryResults query_results = new SparqlQueryResults(query_json, tabName);
                    query_results_list.put(tabName, query_results);
                } catch (IllegalStateException | IOException | NullPointerException e1) {
                    return internalServerError(error_page.render(e1.toString(), "Platforms"));
                    //e1.printStackTrace();
                }
            }// /else
        }// /for tabName
        System.out.println("PlatformList index() was called!");
        //String tree_query_result = tq.getQueryResult().replace("\n", " ");
        return ok(hierarchy_browser.render(query_results_list, hierarchy_results_list, "Platforms"));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
    	//Get query using http.GetSparqlQuery
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        String[] tabsToQuery = {"Platforms"}; 

        TreeMap<String, SparqlQueryResults> query_results_list = new TreeMap<String, SparqlQueryResults>();
        TreeMap<String, String> hierarchy_results_list = new TreeMap<String, String>();
        for (String tabName : tabsToQuery){
            String query_json = null;
            if (tabName.endsWith("H")) {
                System.out.println("PlatformList.java is requesting: " + tabName);
                try {
                    query_json = query_submit.executeQuery(tabName);
                    TreeQueryResults query_results = new TreeQueryResults(query_json, false);
                    hierarchy_results_list.put(tabName, query_results.getQueryResult().replace("\n", " "));
                } catch (IllegalStateException | IOException | NullPointerException e1) {
                    return internalServerError(error_page.render(e1.toString(), "Platforms"));
                    //e1.printStackTrace();
                }
            } else {
                try {
                    query_json = query_submit.executeQuery(tabName);
                    SparqlQueryResults query_results = new SparqlQueryResults(query_json, tabName);
                    query_results_list.put(tabName, query_results);
                } catch (IllegalStateException | IOException | NullPointerException e1) {
                    return internalServerError(error_page.render(e1.toString(), "Platforms"));
                    //e1.printStackTrace();
                }
            }// /else
        }// /for tabName
        System.out.println("PlatformList postIndex() was called!");
        //String tree_query_result = tq.getQueryResult().replace("\n", " ");
        return ok(hierarchy_browser.render(query_results_list, hierarchy_results_list, "All Documents"));
	}// /postIndex()

}
