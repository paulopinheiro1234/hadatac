package org.hadatac.console.controllers.metadata;

import org.hadatac.entity.pojo.Indicator;
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

    public Result index(String tabName) {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQueryDynamic query_submit = new GetSparqlQueryDynamic(query);
        OtMSparqlQueryResults theResults;
        String query_json = null;
        System.out.println("MetadataEntry.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            if (query_json.isEmpty()) {
                return badRequest();
            }
            theResults = new OtMSparqlQueryResults(query_json, true, tabName);
        } catch (IllegalStateException | NullPointerException | IOException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
        }

        Map<String, String> indicators = DynamicFunctions.getIndicatorTypes();
        Map<String, List<String>> values = DynamicFunctions.getIndicatorValuesJustLabels(indicators);

        return ok(metadata_browser.render(theResults, tabName, values));
    }

    public Result postIndex(String tabName) {
        return index(tabName);
    }
    
    public Result indexByUri(String uri) {
        System.out.println("Request indicator URI: " + uri);
        
        Indicator indicator = Indicator.find(uri);
        if (indicator == null) {
            return badRequest();
        }
        
        String tabName = indicator.getLabel().replace(" ", "");
        
        return index(tabName);
    }

    public Result postIndexByUri(String uri) {
        return indexByUri(uri);
    }
}
