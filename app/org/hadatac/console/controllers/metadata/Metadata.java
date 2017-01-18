package org.hadatac.console.controllers.metadata;

import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import java.util.Map;

import org.hadatac.console.views.html.metadata.metadata;


public class Metadata extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {
    	Map<String,String> indicators = DynamicFunctions.getIndicatorTypes();
        Map<String,List<String>> values = DynamicFunctions.getIndicatorValuesJustLabels(indicators);
        return ok(metadata.render(values));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
         return index();
        
    }// /postIndex()

}
