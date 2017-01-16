package org.hadatac.console.controllers.metadata.empirical;

import org.hadatac.console.http.GetSparqlQuery;

import java.io.IOException;
import java.util.TreeMap;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadata.empirical.detector_browser;
import org.hadatac.console.views.html.error_page;


public class Detector extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        SparqlQueryResults perspectives;
        String tabName = "DetectorModels";
        String query_json = null;
        String perspective_json = null;
        System.out.println("Detector.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            perspective_json = query_submit.executeQuery("SensingPerspectives");
            //System.out.println("query_json = " + query_json);
            //System.out.println("perspective_json = " + perspective_json);
            theResults = new SparqlQueryResults(query_json, false);
            perspectives = new SparqlQueryResults(perspective_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("Detector index() was called!");
        return ok(detector_browser.render(theResults, perspectives, tabName));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        SparqlQueryResults perspectives;
        String tabName = "DetectorModels";
        String query_json = null;
        String perspective_json = null;
        System.out.println("Detector.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            perspective_json = query_submit.executeQuery("SensingPerspectives");
            //System.out.println("query_json = " + query_json);
            theResults = new SparqlQueryResults(query_json, false);
            perspectives = new SparqlQueryResults(perspective_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("Detector postIndex() was called!");
        return ok(detector_browser.render(theResults, perspectives, tabName));
    }// /postIndex()

}
