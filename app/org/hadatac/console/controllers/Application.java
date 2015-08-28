package org.hadatac.console.controllers;

import org.hadatac.console.http.GetSolrQuery;
import org.hadatac.console.http.JsonHandler;

import java.io.IOException;
import java.util.TreeMap;

import org.hadatac.console.models.FacetsWithCategories;
import org.hadatac.console.models.SpatialQuery;
import org.hadatac.console.models.SpatialQueryResults;
//import models.SpatialQuery;
//import models.SpatialQueryResults;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.formdata.FacetFormData;
import org.hadatac.console.views.html.index_browser;
import org.hadatac.console.views.html.hadatac_message;

public class Application extends Controller {

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

    public static Result index(int p) {
    	Form<FacetFormData> formData = Form.form(FacetFormData.class).fill(facet_form);
        JsonHandler jh = new JsonHandler();
        //ArrayList<String> names = new ArrayList<String>();
        
        //Get query using http.GetSolrQuery
        SpatialQuery query = new SpatialQuery();
        GetSolrQuery query_submit = new GetSolrQuery(query);
        TreeMap<String, SpatialQueryResults> query_results_list = new TreeMap<String, SpatialQueryResults>();
    	String final_query = null;
    	
    	for (String collection : query_submit.list_of_queries.keySet()){
    		final_query = query_submit.list_of_queries.get(collection).toString();
    		String query_json = null;
            try {
    			query_json = query_submit.executeQuery(collection, p, 20);
    		} catch (IllegalStateException | IOException e1) {
    			e1.printStackTrace();
    		}
            SpatialQueryResults query_results = new SpatialQueryResults(query_json);
            query_results_list.put(collection, query_results);
    	}
    	
        //Get the facets
        getFacets(jh);
        
        //return ok("cool");
        Form<FacetFormData> fd = Form.form(FacetFormData.class).fill(facet_form);
        return ok(index_browser.render(fd, field_facets, query_facets,
                range_facets, pivot_facets, cluster_facets, 
                query_results_list, final_query, p, (int) Math.ceil(1808.0/20)));
        //return ok(hadatac_message.render("HADataC", "Your HADataC instance does not contain any measurements to be browser. Please go ahead and index some."));
    }

    public static Result postIndex(int p) {
        
    	JsonHandler jh = new JsonHandler();
    	String subject = new String();
    	String predicate = "within";
    	DynamicForm formData = Form.form().bindFromRequest();
    	
    	FacetsWithCategories field_facet_for_query = new FacetsWithCategories();
    	
    	//Searching for the index of "[" is done here, because of the way the views are set up
    	//The scala will add a number to each category so as to map
    	//The same category to more than 1 facet
    	//When creating the query, however, this number is not needed.
    	for (String category : formData.data().keySet()){
    		if (category.contains("[")) {
    			int index = category.indexOf("[");
    			field_facet_for_query.addFacet(category.substring(0,index), formData.data().get(category));
    		} else {
    			subject = formData.data().get(category);
    		}
    	}
    	
    	//TODO Change Query constructor not to accept named_geographic_location and spatial_predicate as args
    	//Don't need it since the addSpatialComponent function was created
    	SpatialQuery query = new SpatialQuery(subject, predicate, field_facet_for_query, query_facets,
    						    pivot_facets, range_facets, cluster_facets);
    	
    	GetSolrQuery query_submit = new GetSolrQuery(query);
    	
    	//TODO loop over all queries in query_submit.list_of_queries
    	TreeMap<String, SpatialQueryResults> query_results_list = new TreeMap<String, SpatialQueryResults>();
    	String final_query = null;
    	
    	for (String collection : query_submit.list_of_queries.keySet()){
    		final_query = query_submit.list_of_queries.get(collection).toString();
    		String query_json = null;
            try {
    			query_json = query_submit.executeQuery(collection, p, 20);
    		} catch (IllegalStateException | IOException e1) {
    			e1.printStackTrace();
    		}
            SpatialQueryResults query_results = new SpatialQueryResults(query_json);
            query_results_list.put(collection, query_results);
    	}
    	
        //Get the facets
        getFacets(jh);
        
        //return ok("cool");
        Form<FacetFormData> fd = Form.form(FacetFormData.class).fill(facet_form);
        return ok(index_browser.render(fd, field_facets, query_facets,
                range_facets, pivot_facets, cluster_facets, 
                query_results_list, final_query, p, (int) Math.ceil(1808.0/20))); 

    }

}
