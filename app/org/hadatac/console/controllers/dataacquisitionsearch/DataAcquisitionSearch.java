package org.hadatac.console.controllers.dataacquisitionsearch;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.JsonHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.FacetsWithCategories;
import org.hadatac.console.models.SpatialQueryResults;
import org.hadatac.console.models.SysUser;

import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.formdata.FacetFormData;
import org.hadatac.console.views.html.dataacquisitionsearch.dataacquisition_browser;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.entity.pojo.Measurement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataAcquisitionSearch extends Controller {

    public static FacetFormData facet_form = new FacetFormData();
    public static FacetsWithCategories field_facets = new FacetsWithCategories();
    public static FacetsWithCategories query_facets = new FacetsWithCategories();
    public static FacetsWithCategories pivot_facets = new FacetsWithCategories();
    public static FacetsWithCategories range_facets = new FacetsWithCategories();
    public static FacetsWithCategories cluster_facets = new FacetsWithCategories();
    public static SpatialQueryResults query_results = new SpatialQueryResults();
    
    //Postconditions: field_facets will be modified if there are facets in the JsonHandler
    public static void getFacets(JsonHandler jh){
    	//Get the facets
        try {
            if (jh.getFieldCountJson()) {
                for (String key : jh.categories_and_facets.keySet()) {
                    for (String facet : jh.categories_and_facets.get(key)){
                        //HashMap<String, String> temp_map = new HashMap<String, String>();
                        //temp_map.put(facet, jh.categories_facets_and_counts.get(key).get(facet));
                        if (facet.equals("null")) {field_facets.addFacet(key, "missing"); continue;}
                        field_facets.addFacet(key, facet);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public static Result login(){
    	DynamicForm formData = Form.form().bindFromRequest();
    	String username = formData.data().get("username");
    	String password = formData.data().get("password");
    	
    	session("username", username);
    	session("password", password);
    	    	
    	return redirect("/");
    }
    
    public static List<String> getPermissions(String permissions) {
    	List<String> result = new ArrayList<String>();
    	
    	if (permissions != null) {
	    	StringTokenizer tokens = new StringTokenizer(permissions, ",");
	    	while (tokens.hasMoreTokens()) {
	    		result.add(tokens.nextToken());
	    	}
    	}
    	
    	return result;
    }
    
    public static Result facetSearchProxy(int page, int rows, String facets){
    	ObjectMapper mapper = new ObjectMapper();    	
    	FacetHandler handler = null;
    	try {
    		handler = mapper.readValue(facets, FacetHandler.class);
    	} catch (Exception e) {
    		handler = new FacetHandler();
    		System.out.println("mapper.readValue: " + e.getMessage());
    	}
    	
    	return ok(Json.toJson(handler));
    }
    
    public static Result querySearchProxy(int page, int rows, String facets){
    	ObjectMapper mapper = new ObjectMapper();    	
    	FacetHandler handler = null;
    	try {
    		handler = mapper.readValue(facets, FacetHandler.class);
    	} catch (Exception e) {
    		handler = new FacetHandler();
    		System.out.println("mapper.readValue: " + e.getMessage());
    	}
    	
    	AcquisitionQueryResult results = null;
    	final SysUser user = AuthApplication.getLocalUser(session());
    	if(null == user){
    		results = Measurement.find("Public", page, rows, handler);
    	}
    	else{
    		String ownerUri = UserManagement.getUriByEmail(user.email);
    		results = Measurement.find(ownerUri, page, rows, handler);
    	}
    	
    	return ok(results.toJSON());
    }
    
    public static Result index() {
    	return ok(dataacquisition_browser.render());
    }

    public static Result postIndex() {
    	return index();
    }
}
