package org.hadatac.console.controllers.datacollections;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.GetSparqlQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.hierarchy_browser;
import org.hadatac.console.views.html.error_page;
import org.hadatac.console.views.html.datacollections.*;
import org.hadatac.entity.pojo.DataCollection;


public class DataCollectionManagement extends Controller {

	// for /metadata HTTP GET requests
    public static Result index(String option) {

    	List<DataCollection> theResults = DataCollection.find(AuthApplication.getLocalUser(session()).uri);
    	
        return ok(dataCollectionManagement.render(theResults));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex(String option) {

    	List<DataCollection> theResults = DataCollection.find(AuthApplication.getLocalUser(session()).uri);
    	
        return ok(dataCollectionManagement.render(theResults));
        
    }// /postIndex()

/*    // for /metadata HTTP GET requests
    public static Result list() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        String tabName = "Instruments";
        String query_json = null;
        System.out.println("InstrumentList.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            theResults = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | IOException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("InstrumentList index() was called!");
        return ok(hierarchy_browser.render(theResults, tabName));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postList() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        String tabName = "Instruments";
        String query_json = null;
        System.out.println("InstrumentList.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            theResults = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | IOException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("InstrumentList index() was called!");
        return ok(hierarchy_browser.render(theResults, tabName));
    }// /postIndex()
    
*/
    

}