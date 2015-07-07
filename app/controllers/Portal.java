package controllers;

import http.GetSparqlQuery;
import java.io.IOException;
import models.SparqlQuery;
import models.TreeQuery;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.portal;


public class Portal extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {

       return ok(portal.render());
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return ok(portal.render());
        
    }// /postIndex()

}
