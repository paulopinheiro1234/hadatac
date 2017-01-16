package org.hadatac.console.controllers.metadata;

import org.hadatac.console.http.GetSparqlQueryDynamic;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.OtMSparqlQueryResults;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadata.analytes.analytes_browser;
import org.hadatac.console.views.html.metadata.metadata_browser;
import org.hadatac.console.views.html.error_page;

public class MetadataEntry extends Controller {
	
    public static Result index(String tabName) {
    	SparqlQuery query = new SparqlQuery();
        GetSparqlQueryDynamic query_submit = new GetSparqlQueryDynamic(query);
        OtMSparqlQueryResults theResults;
        String query_json = null;
        System.out.println("MetadataEntry.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            theResults = new OtMSparqlQueryResults(query_json, true);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println(tabName + " index() was called!");
    	
        return ok(metadata_browser.render(theResults, tabName));
    }

    public static Result postIndex(String tabName) {
    	return index(tabName);
    }
}
