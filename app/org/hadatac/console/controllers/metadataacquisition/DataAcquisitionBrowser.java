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
		/*String initStudyQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT DISTINCT ?indicator ?indicatorLabel ?attributeUri ?attributeLabel ?daSchema ?method ?deployment ?study ?comment ?startTime ?endTime WHERE { "
				+ " ?indicator rdfs:subClassOf chear:DataAcquisitionIndicator. "
				+ " ?indicator rdfs:label ?indicatorLabel .  "
				+ " ?attributeSuper rdfs:subClassOf* ?indicator . " 
				+ " ?attributeUri a ?attributeSuper . " 
				+ " ?attributeUri rdfs:label ?attributeLabel ."
				+ " ?attributeUri hasco:isDataAcquisitionOf ?study ."
				+ " OPTIONAL {?attributeUri hasco:hasSchema ?daSchema . }"
				+ " OPTIONAL {?attributeUri hasco:hasMethod ?method . }"
				+ " OPTIONAL {?attributeUri hasneto:hasDeployment ?deployment . }"
				+ " OPTIONAL {?attributeUri rdfs:comment ?comment . }"
				+ " OPTIONAL {?attributeUri prov:wasAssociatedWith ?agent . }"
				+ " OPTIONAL {?attributeUri prov:startedAtTime ?startTime . }"
				+ " OPTIONAL {?attributeUri prov:endedAtTime ?endTime . }"
				+ " }";
		
		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), initStudyQuery);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		*/
		List<String> results = new ArrayList<String>();
/*		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			if(soln.contains("indicator")){
				results.add(soln.get("indicator").toString());
			}
		}*/
		
		results.add("daSchema");
		results.add("method");
		results.add("deployment");
		results.add("agent");
		results.add("startTime");
		results.add("endTime");
		//java.util.Collections.sort(results);
		
		return results; 
    }
	
	public static boolean updateDataAcquisitions() {
		String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT DISTINCT ?indicator ?indicatorLabel ?attributeUri ?attributeLabel ?daSchema ?method ?deployment ?study ?comment ?startTime ?endTime WHERE { "
				+ " ?indicator rdfs:subClassOf chear:DataAcquisitionIndicator. "
				+ " ?indicator rdfs:label ?indicatorLabel .  "
				+ " ?attributeSuper rdfs:subClassOf* ?indicator . " 
				+ " ?attributeUri a ?attributeSuper . " 
				+ " ?attributeUri rdfs:label ?attributeLabel ."
				+ " ?attributeUri hasco:isDataAcquisitionOf ?study ."
				+ " OPTIONAL {?attributeUri hasco:hasSchema ?daSchema . }"
				+ " OPTIONAL {?attributeUri hasco:hasMethod ?method . }"
				+ " OPTIONAL {?attributeUri hasneto:hasDeployment ?deployment . }"
				+ " OPTIONAL {?attributeUri rdfs:comment ?comment . }"
				+ " OPTIONAL {?attributeUri prov:wasAssociatedWith ?agent . }"
				+ " OPTIONAL {?attributeUri prov:startedAtTime ?startTime . }"
				+ " OPTIONAL {?attributeUri prov:endedAtTime ?endTime . }"
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
			String attributeUri = soln.get("attributeUri").toString();
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
			
			if (soln.contains("attributeLabel") && !DAInfo.containsKey("attributeLabel_i")) {
				DAInfo.put("attributeLabel_i", "<a href=\""
						+ Play.application().configuration().getString("hadatac.console.host_deploy") 
						+ "/hadatac/metadataacquisitions/viewDA?da_uri=" 
						+ cellProc.replaceNameSpaceEx(DAInfo.get("attributeUri").toString()) + "\">"
						+ soln.get("attributeLabel").toString() + "</a>");
			}
			if (soln.contains("daSchema") && !DAInfo.containsKey("daSchema_i")) {
				key = "daSchema_i";
				value = soln.get("daSchema").toString();
				DAInfo.put(key, value);
/*				arrValues = null;
				if (!DAInfo.containsKey(key)) {
					arrValues = new ArrayList<String>();
					DAInfo.put(key, arrValues);
				}
				else if (DAInfo.get(key) instanceof ArrayList<?>) {
					arrValues = (ArrayList<String>)DAInfo.get(key);
				}
				
				if (!arrValues.contains(value)) {
					arrValues.add(value);
				}*/
			}
			if (soln.contains("method") && !DAInfo.containsKey("method_i")){
				key = "method_i";
				value = soln.get("method").toString();
				DAInfo.put(key, value);
/*				if (!DAInfo.containsKey(key)) {
					arrValues = new ArrayList<String>();
					DAInfo.put(key, arrValues);
				}
				else if (DAInfo.get(key) instanceof ArrayList<?>) {
					arrValues = (ArrayList<String>)DAInfo.get(key);
				}
				
				if (!arrValues.contains(value)) {
					arrValues.add(value);
				}*/
			}
			if (soln.contains("deployment") && !DAInfo.containsKey("deployment_i")){
				key = "deployment_i";
				value = soln.get("deployment").toString();
				DAInfo.put(key, value);
/*				if (!DAInfo.containsKey(key)) {
					arrValues = new ArrayList<String>();
					DAInfo.put(key, arrValues);
				}
				else if (DAInfo.get(key) instanceof ArrayList<?>) {
					arrValues = (ArrayList<String>)DAInfo.get(key);
				}
				
				if (!arrValues.contains(value)) {
					arrValues.add(value);
				}*/
			}
			if (soln.contains("comment") && !DAInfo.containsKey("comment_i")){
				key = "comment_i";
				value = soln.get("comment").toString();
				DAInfo.put(key, value);
			}
			if (soln.contains("agent") && !DAInfo.containsKey("agent_i")){
				key = "agent_i";
				value = soln.get("agent").toString();
				DAInfo.put(key, value);
/*				if (!DAInfo.containsKey(key)) {
					arrValues = new ArrayList<String>();
					DAInfo.put(key, arrValues);
				}
				else if (DAInfo.get(key) instanceof ArrayList<?>) {
					arrValues = (ArrayList<String>)DAInfo.get(key);
				}
				
				if (!arrValues.contains(value)) {
					arrValues.add(value);
				}*/
			}
			if (soln.contains("startTime") && !DAInfo.containsKey("startTime_i")){
				key = "startTime_i";
				value = soln.get("startTime").toString();
				DAInfo.put(key, value);
/*				if (!DAInfo.containsKey(key)) {
					arrValues = new ArrayList<String>();
					DAInfo.put(key, arrValues);
				}
				else if (DAInfo.get(key) instanceof ArrayList<?>) {
					arrValues = (ArrayList<String>)DAInfo.get(key);
				}
				
				if (!arrValues.contains(value)) {
					arrValues.add(value);
				}*/
			}
			if (soln.contains("endTime") && !DAInfo.containsKey("endTime_i")){
				key = "endTime_i";
				value = soln.get("endTime").toString();
				DAInfo.put(key, value);
/*				if (!DAInfo.containsKey(key)) {
					arrValues = new ArrayList<String>();
					DAInfo.put(key, arrValues);
				}
				else if (DAInfo.get(key) instanceof ArrayList<?>) {
					arrValues = (ArrayList<String>)DAInfo.get(key);
				}
				
				if (!arrValues.contains(value)) {
					arrValues.add(value);
				}*/
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
			SolrClient solr = new HttpSolrClient(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.METADATA_AQUISITION);
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
