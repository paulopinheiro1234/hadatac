package org.hadatac.console.controllers.metadata;

import org.hadatac.console.http.GetSparqlQueryDynamic;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.OtMSparqlQueryResults;

import play.mvc.Controller;
import play.mvc.Result;

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
            //System.out.println("Pre Conversion: " + query_json + "\n");
            theResults = new OtMSparqlQueryResults(query_json, true, tabName);
            //System.out.println("Results: " + theResults.json + "\n");
            
        } catch (IllegalStateException | NullPointerException | IOException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
        }
        System.out.println(tabName + " index() was called!");
    	
    	//List<org.hadatac.entity.pojo.Entity> entities = org.hadatac.entity.pojo.Entity.find();
        Map<String,String> indicators = DynamicFunctions.getIndicatorTypes();
        Map<String,List<String>> values = DynamicFunctions.getIndicatorValuesJustLabels(indicators);
        /*for (String key : theResults.sparqlResults.keySet()){
        	System.out.println(theResults.sparqlResults.get(key).toString());
        }
        System.out.println(theResults.treeResults);*/
        return ok(metadata_browser.render(theResults, tabName, values));
    }

    public static Result postIndex(String tabName) {
    	return index(tabName);
    }
}
