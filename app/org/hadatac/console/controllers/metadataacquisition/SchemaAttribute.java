package org.hadatac.console.controllers.metadataacquisition;

import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONObject;

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class SchemaAttribute extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index() {
		final SysUser user = AuthApplication.getLocalUser(session());
    	String collection = ConfigFactory.load().getString("hadatac.console.host_deploy") + 
    			request().path() + "/solrsearch";
    	List<String> indicators = getIndicators();
    	
    	return ok(schema_attributes.render(collection, indicators, user.isDataManager()));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex() {
        return index();
    }
	
    public static List<String> getIndicators() {
    	List<String> results = new ArrayList<String>();
    	String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT DISTINCT ?DASAttributeUri ?DASAttributeLabel ?Comment ?Entity ?Attribute ?AttributeLabel ?DataAcquisitionSchema ?Position ?Unit ?Source ?Object ?PIConfirmed WHERE {  "
				+ " ?DASAttributeUri a hasco:DASchemaAttribute . "
				+ " OPTIONAL { ?DASAttributeUri rdfs:label ?DASAttributeLabel . }"
				+ " OPTIONAL {?DASAttributeUri rdfs:comment ?Comment . } "
				+ " OPTIONAL {?DASAttributeUri hasco:partOfSchema ?DataAcquisitionSchema . }"
				+ " OPTIONAL {?DASAttributeUri hasco:hasEntity ?Entity . } "
				+ " OPTIONAL {?DASAttributeUri hasco:hasAssociatedObject ?Object . } "
				+ " OPTIONAL {?DASAttributeUri hasco:hasAttribute ?Attribute . "
                + "         ?Attribute rdfs:label ?AttributeLabel . } "
				+ " OPTIONAL {?DASAttributeUri hasco:hasUnit ?Unit .  }"
				+ " OPTIONAL {?DASAttributeUri hasco:hasSource ?Source . }"
				+ " OPTIONAL {?DASAttributeUri hasco:isPIConfirmed ?PIConfirmed . }"
				+ " }";
		
		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), strQuery);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
//			System.out.println("Solution: " + soln.toString());	
			if (soln.contains("DataAcquisitionSchema") && !results.contains("Data Acquisition Schema")) {
				results.add("Data Acquisition Schema");
			}
			if (soln.contains("Entity") && !results.contains("Entity")){
				results.add("Entity");
			}
			if (soln.contains("Attribute") && !results.contains("Attribute")){
				results.add("Attribute");
			}
			if (soln.contains("Position") && !results.contains("Position")){
				results.add("Position");
			}
			if (soln.contains("Unit") && !results.contains("Unit")){
				results.add("Unit");
			}
			if (soln.contains("Source") && !results.contains("Source")){
				results.add("Source");
			}
			if (soln.contains("Object") && !results.contains("Object")){
				results.add("Object");
			}
			if (soln.contains("PIConfirmed") && !results.contains("PI Confirmed")){
				results.add("PI Confirmed");
			}
		}
		java.util.Collections.sort(results);
		
		return results; 
    }
	
	public static boolean updateDASchemaAttributes() {
		String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT DISTINCT ?DASAttributeUri ?DASAttributeLabel ?Comment ?Entity ?Attribute ?AttributeLabel ?DataAcquisitionSchema ?Position ?Unit ?Source ?Object ?PIConfirmed WHERE {  "
				+ " ?DASAttributeUri a hasco:DASchemaAttribute . "
				+ " OPTIONAL { ?DASAttributeUri rdfs:label ?DASAttributeLabel . }"
				+ " OPTIONAL {?DASAttributeUri rdfs:comment ?Comment . } "
				+ " OPTIONAL {?DASAttributeUri hasco:partOfSchema ?DataAcquisitionSchema . }"
				+ " OPTIONAL {?DASAttributeUri hasco:hasEntity ?Entity . } "
				+ " OPTIONAL {?DASAttributeUri hasco:hasAssociatedObject ?Object . } "
				+ " OPTIONAL {?DASAttributeUri hasco:hasAttribute ?Attribute . "
                + "         ?Attribute rdfs:label ?AttributeLabel . } "
				+ " OPTIONAL {?DASAttributeUri hasco:hasUnit ?Unit . }"
				+ " OPTIONAL {?DASAttributeUri hasco:hasSource ?Source . }"
				+ " OPTIONAL {?DASAttributeUri hasco:isPIConfirmed ?PIConfirmed . }"
				+ " }";
		
		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), strQuery);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		
		HashMap<String, HashMap<String, Object>> mapDAInfo = new HashMap<String, HashMap<String, Object>>();
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			System.out.println("SchemaAttribute Solution: " + soln.toString());
			String attributeUri = DynamicFunctions.replaceURLWithPrefix(soln.get("DASAttributeUri").toString());
			HashMap<String, Object> DAInfo = null;
			String key = "";
			String value = "";
			
			if (!mapDAInfo.containsKey(attributeUri)) {
				DAInfo = new HashMap<String, Object>();
				DAInfo.put("DASAttributeUri", attributeUri);
				mapDAInfo.put(attributeUri, DAInfo);
			}
			else {
				DAInfo = mapDAInfo.get(attributeUri);
			}
			
			if (soln.contains("DASAttributeLabel") && !DAInfo.containsKey("DASAttributeLabel_str")) {
//				DAInfo.put("DASAttributeLabel_str", "<a href=\""
//						+ ConfigFactory.load().getString("hadatac.console.host_deploy") 
//						+ "/hadatac/metadataacquisitions/viewDASA?da_uri=" 
//						+ URIUtils.replaceNameSpaceEx(DAInfo.get("DASAttributeUri").toString()) + "\">"
//						+ soln.get("DASAttributeLabel").toString() + "</a>");
				DAInfo.put("DASAttributeLabel_str", "<a href=\"#\">" + soln.get("DASAttributeLabel").toString() + "</a>");
			}
			if (soln.contains("DataAcquisitionSchema") && !DAInfo.containsKey("DataAcquisitionSchema_str")) {
				key = "DataAcquisitionSchema_str";
				value = DynamicFunctions.replaceURLWithPrefix(soln.get("DataAcquisitionSchema").toString());
				DAInfo.put(key, value);
			}
			if (soln.contains("Comment") && !DAInfo.containsKey("Comment_str")){
				key = "Comment_str";
				value = soln.get("Comment").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("Entity") && !DAInfo.containsKey("Entity_str")){
				key = "Entity_str";
				value = DynamicFunctions.replaceURLWithPrefix(soln.get("Entity").toString());
				DAInfo.put(key, value);
			}
			if (soln.contains("Attribute") && !DAInfo.containsKey("Attribute_str")){
				key = "Attribute_str";
				value = DynamicFunctions.replaceURLWithPrefix(soln.get("Attribute").toString());
				DAInfo.put(key, value);
			}
			if (soln.contains("AttributeLabel") && !DAInfo.containsKey("AttributeLabel_str")){
				key = "AttributeLabel_str";
				value = soln.get("AttributeLabel").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("Position") && !DAInfo.containsKey("Position_str")){
				key = "Position_str";
				value = soln.get("Position").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("Unit") && !DAInfo.containsKey("Unit_str")){
				key = "Unit_str";
				value = DynamicFunctions.replaceURLWithPrefix(soln.get("Unit").toString());
				DAInfo.put(key, value);
			}
			if (soln.contains("Source") && !DAInfo.containsKey("Source_str")){
				key = "Source_str";
				value = soln.get("Source").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("Object") && !DAInfo.containsKey("Object_str")){
				key = "Object_str";
				value = DynamicFunctions.replaceURLWithPrefix(soln.get("Object").toString());
				DAInfo.put(key, value);
			}
			if (soln.contains("PIConfirmed") && !DAInfo.containsKey("PIConfirmed_str")){
				key = "PIConfirmed_str";
				value = soln.get("PIConfirmed").toString();
				DAInfo.put(key, value);
			}
		}
		
		deleteFromSolr();
		
		ArrayList<JSONObject> results = new ArrayList<JSONObject>();
		for (HashMap<String, Object> info : mapDAInfo.values()) {
			results.add(new JSONObject(info));
		}
		
		return SolrUtils.commitJsonDataToSolr(
				ConfigFactory.load().getString("hadatac.solr.data") 
				+ Collections.SA_ACQUISITION, results.toString());
	}
	
	public static int deleteFromSolr() {
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					ConfigFactory.load().getString("hadatac.solr.data") 
					+ Collections.SA_ACQUISITION).build();
			UpdateResponse response = solr.deleteByQuery("*:*");
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] SchemaAttribute.deleteFromSolr() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] SchemaAttribute.deleteFromSolr() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] SchemaAttribute.deleteFromSolr() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result update() {
		updateDASchemaAttributes();
		return redirect(routes.SchemaAttribute.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postUpdate() {
    	return update();
    }
}
