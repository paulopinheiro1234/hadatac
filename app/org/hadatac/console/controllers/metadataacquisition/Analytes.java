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


public class Analytes extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
		final SysUser user = AuthApplication.getLocalUser(session());
    	String collection = Play.application().configuration().getString("hadatac.console.host_deploy") + 
    			request().path() + "/solrsearch";
    	List<String> indicators = getIndicators();
    	
    	return ok(analytes.render(collection, indicators, user.isDataManager()));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
        return index();
    }
	
    public static List<String> getIndicators() {
		String initStudyQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT DISTINCT ?indicatorLabel WHERE { "
				+ " ?subTypeUri rdfs:subClassOf* hasco:Study . "
				+ " ?studyUri a ?subTypeUri . "
				+ " ?dataAcq hasco:isDataAcquisitionOf ?studyUri ."
				+ " ?dataAcq hasco:hasSchema ?schemaUri ."
//				+ " ?schemaUri hasco:isSchemaOf ?studyUri . "
				+ " ?schemaAttribute hasco:partOfSchema ?schemaUri . "
				+ " ?schemaAttribute hasco:hasAttribute|hasco:hasEntity ?attribute . "
				+ " ?indicator rdfs:subClassOf chear:TargetedAnalyte . "
				+ " ?indicator rdfs:label ?indicatorLabel . " 
				+ " ?attribute rdfs:subClassOf+ ?indicator . " 
				+ " ?attribute rdfs:label ?attributeLabel . "
				+ " }";
		
		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), initStudyQuery);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		
		List<String> results = new ArrayList<String>();
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			results.add(soln.get("indicatorLabel").toString());
		}
		java.util.Collections.sort(results);
		
		return results; 
    }
	
	public static boolean updateAnalytes() {
		String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT DISTINCT ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment "
				+ " ?indicatorLabel ?attributeLabel ?agentName ?institutionName WHERE { "
				+ " ?subTypeUri rdfs:subClassOf* hasco:Study . "
				+ " ?studyUri a ?subTypeUri . "
				+ " ?dataAcq hasco:isDataAcquisitionOf ?studyUri ."
				+ " ?dataAcq hasco:hasSchema ?schemaUri ."
//				+ " ?schemaUri hasco:isSchemaOf ?studyUri . "
				+ " ?schemaAttribute hasco:partOfSchema ?schemaUri . "
				+ " ?schemaAttribute hasco:hasAttribute|hasco:hasEntity ?attribute . "
				+ " ?indicator rdfs:subClassOf chear:TargetedAnalyte . "
				+ " ?indicator rdfs:label ?indicatorLabel . " 
				+ " ?attribute rdfs:subClassOf+ ?indicator . " 
				+ " ?attribute rdfs:label ?attributeLabel . " 
				+ " OPTIONAL{ ?studyUri rdfs:label ?studyLabel } . "
				+ " OPTIONAL{ ?studyUri hasco:hasProject ?proj } . "
				+ " OPTIONAL{ ?studyUri skos:definition ?studyTitle } . "
				+ " OPTIONAL{ ?studyUri rdfs:comment ?studyComment } . "
				+ " OPTIONAL{ ?studyUri hasco:hasAgent ?agent . "
				+ "           ?agent foaf:name ?agentName } . "
				+ " OPTIONAL{ ?studyUri hasco:hasInstitution ?institution . "
				+ "           ?institution foaf:name ?institutionName } . "
				+ " } ";
		
		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), strQuery);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		
		HashMap<String, HashMap<String, Object>> mapStudyInfo = new HashMap<String, HashMap<String, Object>>();
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			String studyUri = soln.get("studyUri").toString();
			HashMap<String, Object> studyInfo = null;
			if (!mapStudyInfo.containsKey(studyUri)) {
				studyInfo = new HashMap<String, Object>();
				studyInfo.put("studyUri", studyUri);
				mapStudyInfo.put(studyUri, studyInfo);
			}
			else {
				studyInfo = mapStudyInfo.get(studyUri);
			}
			
			if (soln.contains("studyLabel") && !studyInfo.containsKey("studyLabel_i")) {
				studyInfo.put("studyLabel_i", "<a href=\""
						+ Play.application().configuration().getString("hadatac.console.host_deploy") 
						+ "/hadatac/metadataacquisitions/viewStudy?study_uri=" 
						+ ValueCellProcessing.replaceNameSpaceEx(studyInfo.get("studyUri").toString()) + "\">"
						+ soln.get("studyLabel").toString() + "</a>");
			}
			if (soln.contains("studyTitle") && !studyInfo.containsKey("studyTitle_i")) {
				studyInfo.put("studyTitle_i", soln.get("studyTitle").toString());
			}
			if (soln.contains("proj") && !studyInfo.containsKey("proj_i")){
				studyInfo.put("proj_i", soln.get("proj").toString());
			}
			if (soln.contains("studyComment") && !studyInfo.containsKey("studyComment_i")){
				studyInfo.put("studyComment_i", soln.get("studyComment").toString());
			}
			if (soln.contains("agentName") && !studyInfo.containsKey("agentName_i")){
				studyInfo.put("agentName_i", soln.get("agentName").toString());
			}
			if (soln.contains("institutionName") && !studyInfo.containsKey("institutionName_i")){
				studyInfo.put("institutionName_i", soln.get("institutionName").toString());
			}
			if (soln.contains("indicatorLabel")) {
				String key = soln.get("indicatorLabel").toString().
						replace(",","").replace(" ", "") + "_m";
				String value = soln.get("attributeLabel").toString();
				ArrayList<String> arrValues = null;
				if (!studyInfo.containsKey(key)) {
					arrValues = new ArrayList<String>();
					studyInfo.put(key, arrValues);
				}
				else if (studyInfo.get(key) instanceof ArrayList<?>) {
					arrValues = (ArrayList<String>)studyInfo.get(key);
				}
				
				if (!arrValues.contains(value)) {
					arrValues.add(value);
				}
			}
		}
		deleteFromSolr();
		
		ArrayList<JSONObject> results = new ArrayList<JSONObject>();
		for (HashMap<String, Object> info : mapStudyInfo.values()) {
			results.add(new JSONObject(info));
		}
		
		return SolrUtils.commitJsonDataToSolr(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.ANALYTES, results.toString());
	}
	
	public static int deleteFromSolr() {
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.ANALYTES).build();
			UpdateResponse response = solr.deleteByQuery("*:*");
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Analytes.deleteFromSolr() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Analytes.deleteFromSolr() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Analytes.deleteFromSolr() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result update() {
		updateAnalytes();
		
		return redirect(routes.Analytes.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postUpdate() {
    	return update();
    }
}
