package org.hadatac.console.controllers.metadata.empirical;

import org.hadatac.console.http.GetSparqlQuery;

import java.io.IOException;
import java.util.TreeMap;

import org.hadatac.console.models.FacetsWithCategories;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadata.hierarchy_browser;
import org.hadatac.console.views.html.error_page;


public class InstrumentList extends Controller {

    public Result index() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        String tabName = "Instruments";
        String query_json = null;
        System.out.println("InstrumentList.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            theResults = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
        }
        System.out.println("InstrumentList index() was called!");
        return ok(hierarchy_browser.render(theResults, tabName));
    }

    public Result postIndex() {
        return index();
    }
}