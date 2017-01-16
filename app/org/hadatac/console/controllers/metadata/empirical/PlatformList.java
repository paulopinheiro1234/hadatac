package org.hadatac.console.controllers.metadata.empirical;

import org.hadatac.console.http.GetSparqlQuery;
import org.hadatac.console.http.JsonHandler;

import java.io.IOException;
import java.util.TreeMap;

import org.hadatac.console.models.FacetsWithCategories;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadata.hierarchy_browser;
import org.hadatac.console.views.html.error_page;


public class PlatformList extends Controller {

    // for /metadata HTTP GET requests
    public static Result index() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
	    String tabName = "Platforms";
    	String query_json = null;
        System.out.println("PlatformList.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            theResults = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("PlatformList index() was called!");
        return ok(hierarchy_browser.render(theResults, tabName));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
	    String tabName = "Platforms";
    	String query_json = null;
        System.out.println("PlatformList.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            theResults = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("PlatformList postIndex() was called!");
        return ok(hierarchy_browser.render(theResults, tabName));
	}// /postIndex()

}
