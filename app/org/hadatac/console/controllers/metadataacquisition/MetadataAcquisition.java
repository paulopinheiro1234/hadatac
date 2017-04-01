package org.hadatac.console.controllers.metadataacquisition;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadata.DynamicFunctions;

import java.util.List;
import java.util.Map;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadataacquisition.*;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class MetadataAcquisition extends Controller {
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
    	String collection = Play.application().configuration().getString("hadatac.console.host_deploy") + 
    			request().path() + "/solrsearch";
    	Map<String,String> indicators = DynamicFunctions.getIndicatorTypes();
        Map<String,List<String>> values = DynamicFunctions.getIndicatorValuesJustLabels(indicators);
    	return ok(metadataacquisition.render(collection, values));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
    	return index();
    }
}