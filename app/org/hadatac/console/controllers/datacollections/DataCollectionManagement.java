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
    	
        return ok(dataCollectionManagement.render(option, theResults));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex(String option) {

    	List<DataCollection> theResults = DataCollection.find(AuthApplication.getLocalUser(session()).uri);
    	
        return ok(dataCollectionManagement.render(option, theResults));
        
    }// /postIndex()

}