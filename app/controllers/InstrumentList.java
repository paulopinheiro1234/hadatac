package controllers;

import http.GetSparqlQuery;
import http.JsonHandler;

import java.io.IOException;
import java.util.TreeMap;

import models.FacetsWithCategories;
import models.SparqlQuery;
import models.BundledResults;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.hierarchy_browser;
import views.html.error_page;


public class InstrumentList extends Controller {

    // for /metadata HTTP GET requests
    public static Result index() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        BundledResults theResults;
        String tabName = "Instruments";
        String query_json = null;
        System.out.println("InstrumentList.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            theResults = new BundledResults(query_json, false);
        } catch (IllegalStateException | IOException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("InstrumentList index() was called!");
        return ok(hierarchy_browser.render(theResults, tabName));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        BundledResults theResults;
        String tabName = "Instruments";
        String query_json = null;
        System.out.println("InstrumentList.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            theResults = new BundledResults(query_json, false);
        } catch (IllegalStateException | IOException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("InstrumentList index() was called!");
        return ok(hierarchy_browser.render(theResults, tabName));
    }// /postIndex()

}