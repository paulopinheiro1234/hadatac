package org.hadatac.console.controllers.metadata;

import org.hadatac.console.controllers.Application;
import org.hadatac.entity.pojo.Indicator;
import org.hadatac.console.http.GetSparqlQueryDynamic;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.OtMSparqlQueryResults;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.views.html.metadata.metadata_browser;
import org.hadatac.console.views.html.error_page;

import javax.inject.Inject;

public class MetadataEntry extends Controller {

    @Inject
    Application application;

    public Result index(String tabName, Http.Request request) {
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
        Map<String, List<String>> values = Indicator.getValuesJustLabels(indicators);

        return ok(metadata_browser.render(theResults, tabName, values,application.getUserEmail(request)));
    }

    public Result postIndex(String tabName,Http.Request request) {
        return index(tabName,request);
    }

    public Result indexByUri(String uri,Http.Request request) {
        System.out.println("Request indicator URI: " + uri);

        Indicator indicator = Indicator.find(uri);
        if (indicator == null) {
            return badRequest();
        }

        String tabName = indicator.getLabel().replace(" ", "");

        return index(tabName,request);
    }

    public Result postIndexByUri(String uri,Http.Request request) {
        return indexByUri(uri,request);
    }
}
