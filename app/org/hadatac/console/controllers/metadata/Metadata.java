package org.hadatac.console.controllers.metadata;

import org.hadatac.console.controllers.Application;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;
import java.util.Map;

import org.hadatac.console.views.html.metadata.metadata;
import org.hadatac.entity.pojo.Indicator;
import org.hadatac.utils.NameSpaces;

import javax.inject.Inject;


public class Metadata extends Controller {

    @Inject
    Application application;

    // for /metadata HTTP GET requests
    public Result index(Http.Request request) {
        Map<String,String> indicators = DynamicFunctions.getIndicatorTypes();
        Map<String,List<String>> values = Indicator.getValuesJustLabels(indicators);
        return ok(metadata.render(values, getLoadedList(), application.getUserEmail(request)));

    }// /index()


    // for /metadata HTTP POST requests
    public Result postIndex(Http.Request request) {

        return index(request);

    }// /postIndex()

    private List<String> getLoadedList() {
        NameSpaces ns = NameSpaces.getInstance();
        return ns.listLoadedOntologies();
    }

}
