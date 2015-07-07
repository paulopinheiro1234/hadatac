package controllers;

import http.GetSparqlQuery;
import java.io.IOException;
import models.SparqlQuery;
import models.TreeQuery;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.hierarchy_browser;


public class Detector extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

    	String category = "DetectorsH";
    	
    	TreeQuery tq = new TreeQuery(category, false);
    	
        System.out.println("Detectors index() was called!");
        return ok(hierarchy_browser.render(tq.getQueryResult().replace("\n", " "), category));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
    	String category = "DetectorsH";
    	
    	TreeQuery tq = new TreeQuery(category, false);
    	
        System.out.println("Detectors index() was called!");
        return ok(hierarchy_browser.render(tq.getQueryResult().replace("\n", " "), category));
        
    }// /postIndex()

}
