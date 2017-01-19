package org.hadatac.console.controllers.metadata.analytes;

import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.http.GetSparqlQuery;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.OtMSparqlQueryResults;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadata.analytes.analytes_browser;
import org.hadatac.console.views.html.metadata.metadata_browser;
import org.hadatac.console.views.html.error_page;


public class Analytes extends Controller {
	
    public static Result index() {
    	SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        OtMSparqlQueryResults theResults;
        String tabName = "TargetedAnalyte";
        String query_json = null;
        System.out.println("Analytes.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            theResults = new OtMSparqlQueryResults(query_json, true);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
        }
        System.out.println("Analytes index() was called!");
        Map<String,String> indicators = DynamicFunctions.getIndicatorTypes();
        Map<String,List<String>> values = DynamicFunctions.getIndicatorValuesJustLabels(indicators);
    	//List<org.hadatac.entity.pojo.Entity> entities = org.hadatac.entity.pojo.Entity.find();
    	
        return ok(metadata_browser.render(theResults, "TargetedAnalyte", values));
    }

    public static Result postIndex() {    	
        return index();
    }
}
