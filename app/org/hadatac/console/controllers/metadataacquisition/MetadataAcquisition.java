package org.hadatac.console.controllers.metadataacquisition;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.http.JsonHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.FacetsWithCategories;
import org.hadatac.console.models.SpatialQueryResults;

import play.Play;
//import models.SpatialQuery;
//import models.SpatialQueryResults;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.formdata.FacetFormData;
import org.hadatac.console.views.html.metadataacquisition.*;

import org.hadatac.data.model.MetadataAcquisitionQueryResult;
import org.hadatac.entity.pojo.Study;
import org.hadatac.utils.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class MetadataAcquisition extends Controller {
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    //public static Result index(int page, int rows, String facets) {
    public static Result index() {
//    	String collection = Collections.getCollectionsName(Collections.STUDY_ACQUISITION);
    	String collection = Play.application().configuration().getString("hadatac.console.host_deploy") + 
    			request().path() + "/solrsearch";
    	Map<String,String> indicators = DynamicFunctions.getIndicatorTypes();
        Map<String,List<String>> values = DynamicFunctions.getIndicatorValuesJustLabels(indicators);
    	return ok(metadataacquisition.render(collection, values));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    //public static Result postIndex(int page, int rows, String facets) {
    public static Result postIndex() {
    	return index();
    }
}