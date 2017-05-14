package org.hadatac.console.controllers.metadataacquisition;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONObject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class SchemaAttribute extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
		final SysUser user = AuthApplication.getLocalUser(session());
    	String collection = Play.application().configuration().getString("hadatac.console.host_deploy") + 
    			request().path() + "/solrsearch";
    	List<String> indicators = getIndicators();
    	
    	return ok(schema_attributes.render(collection, indicators, user.isDataManager()));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
        return index();
    }
	
    public static List<String> getIndicators() {
		List<String> results = new ArrayList<String>();
	
		results.add("daSchema");
		results.add("attLabel");
		results.add("entity");
		results.add("unit");
		results.add("object");
		results.add("position");
		results.add("source");
		results.add("piConfirmed");
		//java.util.Collections.sort(results);
		
		return results; 
    }
	
	public static boolean updateDASchemaAttributes() {
		String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT DISTINCT ?DASAttributeUri ?DASAttributeLabel ?comment ?entity ?attribute ?attLabel ?daSchema ?position ?unit ?source ?object ?piConfirmed WHERE {  "
				+ " ?DASAttributeUri a hasneto:DASchemaAttribute . "
				+ " OPTIONAL { ?DASAttributeUri rdfs:label ?DASAttributeLabel . }"
				+ " OPTIONAL {?DASAttributeUri rdfs:comment ?comment . } "
				+ " OPTIONAL {?DASAttributeUri hasneto:partOfSchema ?daSchema . }"
				+ " OPTIONAL {?DASAttributeUri hasco:hasPosition ?position . } "
				+ " OPTIONAL {?DASAttributeUri hasneto:hasEntity ?entity . } "
				+ " OPTIONAL {?DASAttributeUri hasneto:hasAssociatedObject ?object . } "
				+ " OPTIONAL {?DASAttributeUri hasneto:hasAttribute ?attribute . "
                + "         ?attribute rdfs:label ?attLabel . } "
				+ " OPTIONAL {?DASAttributeUri hasneto:hasUnit ?unit . }"
				+ " OPTIONAL {?DASAttributeUri hasco:hasSource ?source . }"
				+ " OPTIONAL {?DASAttributeUri hasco:isPIConfirmed ?piConfirmed . }"
				+ " }";
		
		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), strQuery);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		
		HashMap<String, HashMap<String, Object>> mapDAInfo = new HashMap<String, HashMap<String, Object>>();
		ValueCellProcessing cellProc = new ValueCellProcessing();
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			System.out.println("Solution: " + soln.toString());
			String attributeUri = soln.get("DASAttributeUri").toString();
			HashMap<String, Object> DAInfo = null;
			String key = "";
			String value = "";
			ArrayList<String> arrValues = null;
			
			if (!mapDAInfo.containsKey(attributeUri)) {
				DAInfo = new HashMap<String, Object>();
				DAInfo.put("DASAttributeUri", attributeUri);
				mapDAInfo.put(attributeUri, DAInfo);
			}
			else {
				DAInfo = mapDAInfo.get(attributeUri);
			}
			
			if (soln.contains("DASAttributeLabel") && !DAInfo.containsKey("DASAttributeLabel_i")) {
				DAInfo.put("DASAttributeLabel_i", "<a href=\""
						+ Play.application().configuration().getString("hadatac.console.host_deploy") 
						+ "/hadatac/metadataacquisitions/viewDASA?da_uri=" 
						+ cellProc.replaceNameSpaceEx(DAInfo.get("DASAttributeUri").toString()) + "\">"
						+ soln.get("DASAttributeLabel").toString() + "</a>");
			}
			if (soln.contains("daSchema") && !DAInfo.containsKey("daSchema_i")) {
				key = "daSchema_i";
				value = soln.get("daSchema").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("comment") && !DAInfo.containsKey("comment_i")){
				key = "comment_i";
				value = soln.get("comment").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("entity") && !DAInfo.containsKey("entity_i")){
				key = "entity_i";
				value = soln.get("entity").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("attribute") && !DAInfo.containsKey("attribute_i")){
				key = "attribute_i";
				value = soln.get("attribute").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("attLabel") && !DAInfo.containsKey("attLabel_i")){
				key = "attLabel_i";
				value = soln.get("attLabel").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("position") && !DAInfo.containsKey("position_i")){
				key = "position_i";
				value = soln.get("position").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("unit") && !DAInfo.containsKey("unit_i")){
				key = "unit_i";
				value = soln.get("unit").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("source") && !DAInfo.containsKey("source_i")){
				key = "source_i";
				value = soln.get("source").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("object") && !DAInfo.containsKey("object_i")){
				key = "object_i";
				value = soln.get("object").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("piConfirmed") && !DAInfo.containsKey("piConfirmed_i")){
				key = "piConfirmed_i";
				value = soln.get("piConfirmed").toString();
				DAInfo.put(key, value);
			}
		}
		
		ArrayList<JSONObject> results = new ArrayList<JSONObject>();
		for (HashMap<String, Object> info : mapDAInfo.values()) {
			results.add(new JSONObject(info));
		}
		
		return SolrUtils.commitJsonDataToSolr(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.SA_ACQUISITION, results.toString());
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result update() {
		updateDASchemaAttributes();
		
		return redirect(routes.SchemaAttribute.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postUpdate() {
    	return update();
    }
}
