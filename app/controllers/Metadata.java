package controllers;

import http.GetSparqlQuery;
import java.io.IOException;
import models.SparqlQuery;
import models.TreeQuery;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.metadata;


public class Metadata extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

       return ok(metadata.render());
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return ok(metadata.render());
        
    }// /postIndex()

}
