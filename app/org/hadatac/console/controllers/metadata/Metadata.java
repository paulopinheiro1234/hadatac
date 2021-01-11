package org.hadatac.console.controllers.metadata;

import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import java.util.Map;

import org.hadatac.console.views.html.metadata.metadata;
import org.hadatac.entity.pojo.Indicator;
import org.hadatac.utils.NameSpaces;


public class Metadata extends Controller {

    // for /metadata HTTP GET requests
    public Result index() {
        Map<String,String> indicators = DynamicFunctions.getIndicatorTypes();
        Map<String,List<String>> values = Indicator.getValuesJustLabels(indicators);
        return ok(metadata.render(values, getLoadedList()));

    }// /index()


    // for /metadata HTTP POST requests
    public Result postIndex() {

        return index();

    }// /postIndex()

    private List<String> getLoadedList() {
        NameSpaces ns = NameSpaces.getInstance();
        return ns.listLoadedOntologies();
    }

}
