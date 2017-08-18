package org.hadatac.console.controllers.metadataacquisition;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
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
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
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


public class DataAcquisitionBrowser extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
		final SysUser user = AuthApplication.getLocalUser(session());
    	String collection = Play.application().configuration().getString("hadatac.console.host_deploy") + 
    			request().path() + "/solrsearch";
    	List<String> indicators = getIndicators();
    	
    	return ok(dataacquisitionbrowser.render(collection, indicators, user.isDataManager()));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
        return index();
    }
	
    public static List<String> getIndicators() {
		List<String> results = new ArrayList<String>();
		String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT DISTINCT ?attributeUri ?AttributeLabel ?DataAcquisitionSchema ?Method ?Deployment ?Study ?Comment ?StartTime ?EndTime WHERE { "
				+ " ?AttributeSuper rdfs:subClassOf* hasco:DataAcquisition . " 
				+ " ?attributeUri a ?AttributeSuper . " 
				+ " ?attributeUri rdfs:label ?AttributeLabel ."
				+ " ?attributeUri hasco:isDataAcquisitionOf ?Study ."
				+ " OPTIONAL {?attributeUri hasco:hasSchema ?DataAcquisitionSchema . }"
				+ " OPTIONAL {?attributeUri hasco:hasMethod ?Method . }"
				+ " OPTIONAL {?attributeUri hasco:hasDeployment ?Deployment . }"
				+ " OPTIONAL {?attributeUri rdfs:comment ?Comment . }"
				+ " OPTIONAL {?attributeUri prov:wasAssociatedWith ?Agent . }"
				+ " OPTIONAL {?attributeUri prov:startedAtTime ?StartTime . }"
				+ " OPTIONAL {?attributeUri prov:endedAtTime ?EndTime . }"
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
			System.out.println("DataAcquisitionBrowser Solution: " + soln.toString());
			if (soln.contains("DataAcquisitionSchema") && !results.contains("Data Acquisition Schema")) {
				results.add("Data Acquisition Schema");			}
			if (soln.contains("Method") && !results.contains("Method")){
				results.add("Method");
			}
			if (soln.contains("Deployment") && !results.contains("Deployment")){
				results.add("Deployment");
			}
			if (soln.contains("Agent") && !results.contains("Agent")){
				results.add("Agent");
			}
			if (soln.contains("StartTime") && !results.contains("Start Time")){
				results.add("Start Time");
			}
			if (soln.contains("EndTime") && !results.contains("End Time")){
				results.add("End Time");
			}
			if (soln.contains("Study") && !results.contains("Study")){
				results.add("Study");
			}
		}
		java.util.Collections.sort(results);
		return results; 
    }
	
	public static boolean updateDataAcquisitions() {
		String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT DISTINCT ?attributeUri ?AttributeLabel ?DataAcquisitionSchema ?Method ?Deployment ?Study ?Comment ?StartTime ?EndTime WHERE { "
				+ " ?AttributeSuper rdfs:subClassOf* hasco:DataAcquisition . " 
				+ " ?attributeUri a ?AttributeSuper . " 
				+ " ?attributeUri rdfs:label ?AttributeLabel ."
				+ " ?attributeUri hasco:isDataAcquisitionOf ?Study ."
				+ " OPTIONAL {?attributeUri hasco:hasSchema ?DataAcquisitionSchema . }"
				+ " OPTIONAL {?attributeUri hasco:hasMethod ?Method . }"
				+ " OPTIONAL {?attributeUri hasco:hasDeployment ?Deployment . }"
				+ " OPTIONAL {?attributeUri rdfs:comment ?Comment . }"
				+ " OPTIONAL {?attributeUri prov:wasAssociatedWith ?Agent . }"
				+ " OPTIONAL {?attributeUri prov:startedAtTime ?StartTime . }"
				+ " OPTIONAL {?attributeUri prov:endedAtTime ?EndTime . }"
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
			System.out.println("DataAcquisitionBrowser Solution: " + soln.toString());
			String attributeUri = DynamicFunctions.replaceURLWithPrefix(soln.get("attributeUri").toString());
			HashMap<String, Object> DAInfo = null;
			String key = "";
			String value = "";
			ArrayList<String> arrValues = null;
			
			if (!mapDAInfo.containsKey(attributeUri)) {
				DAInfo = new HashMap<String, Object>();
				DAInfo.put("attributeUri", attributeUri);
				mapDAInfo.put(attributeUri, DAInfo);
			}
			else {
				DAInfo = mapDAInfo.get(attributeUri);
			}
			
			if (soln.contains("AttributeLabel") && !DAInfo.containsKey("AttributeLabel_i")) {
				DAInfo.put("AttributeLabel_i", "<a href=\""
						+ Play.application().configuration().getString("hadatac.console.host_deploy") 
						+ "/hadatac/metadataacquisitions/viewDA?da_uri=" 
						+ cellProc.replaceNameSpaceEx(DAInfo.get("attributeUri").toString()) + "\">"
						+ soln.get("AttributeLabel").toString() + "</a>");
			}
			if (soln.contains("DataAcquisitionSchema") && !DAInfo.containsKey("DataAcquisitionSchema_i")) {
				key = "DataAcquisitionSchema_i";
				value = DynamicFunctions.replaceURLWithPrefix(soln.get("DataAcquisitionSchema").toString());
				DAInfo.put(key, value);
			}
			if (soln.contains("Method") && !DAInfo.containsKey("Method_i")){
				key = "Method_i";
				value = DynamicFunctions.replaceURLWithPrefix(soln.get("Method").toString());
				DAInfo.put(key, value);
			}
			if (soln.contains("Deployment") && !DAInfo.containsKey("Deployment_i")){
				key = "Deployment_i";
				value = DynamicFunctions.replaceURLWithPrefix(soln.get("Deployment").toString());
				DAInfo.put(key, value);
			}
			if (soln.contains("Comment") && !DAInfo.containsKey("Comment_i")){
				key = "Comment_i";
				value = soln.get("Comment").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("Agent") && !DAInfo.containsKey("Agent_i")){
				key = "Agent_i";
				value = DynamicFunctions.replaceURLWithPrefix(soln.get("Agent").toString());
				DAInfo.put(key, value);
			}
			if (soln.contains("StartTime") && !DAInfo.containsKey("StartTime_i")){
				key = "StartTime_i";
				value = soln.get("StartTime").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("EndTime") && !DAInfo.containsKey("EndTime_i")){
				key = "EndTime_i";
				value = soln.get("EndTime").toString();
				DAInfo.put(key, value);
			}
		}
		deleteFromSolr();
		
		ArrayList<JSONObject> results = new ArrayList<JSONObject>();
		for (HashMap<String, Object> info : mapDAInfo.values()) {
			results.add(new JSONObject(info));
		}
		
		return SolrUtils.commitJsonDataToSolr(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.METADATA_AQUISITION, results.toString());
	}
	
	public static int deleteFromSolr() {
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.METADATA_AQUISITION).build();
			UpdateResponse response = solr.deleteByQuery("*:*");
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] DataAcquisitionBroswer.deleteFromSolr() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] DataAcquisitionBroswer.deleteFromSolr() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] DataAcquisitionBroswer.deleteFromSolr() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result update() {
		updateDataAcquisitions();
		
		return redirect(routes.DataAcquisitionBrowser.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postUpdate() {
    	return update();
    }
}
