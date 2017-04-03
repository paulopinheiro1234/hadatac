package org.hadatac.console.controllers.metadataacquisition;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.SysUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONObject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class MetadataAcquisition extends Controller {
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
    	final SysUser user = AuthApplication.getLocalUser(session());
    	String collection = Play.application().configuration().getString("hadatac.console.host_deploy") 
    			+ request().path() + "/solrsearch";
    	List<String> indicators = getIndicators();
    	
    	return ok(metadataacquisition.render(collection, indicators, user.isDataManager()));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
    	return index();
    }
    
    public static List<String> getIndicators() {
		String initStudyQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT DISTINCT ?indicatorLabel WHERE { "
				+ " ?subTypeUri rdfs:subClassOf hasco:Study . "
				+ " ?studyUri a ?subTypeUri . "
				+ " ?schemaUri hasco:isSchemaOf ?studyUri . "
				+ " ?schemaAttribute hasneto:partOfSchema ?schemaUri . "
				+ " ?schemaAttribute hasneto:hasAttribute ?attribute . "
				+ " ?indicator rdfs:subClassOf chear:StudyIndicator . "
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
    
	public static boolean updateStudy() {
		String initStudyQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT DISTINCT ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment "
				+ " ?indicatorLabel ?attributeLabel ?agentName ?institutionName WHERE { "
				+ " ?subTypeUri rdfs:subClassOf hasco:Study . "
				+ " ?studyUri a ?subTypeUri . "
				+ " ?schemaUri hasco:isSchemaOf ?studyUri . "
				+ " ?schemaAttribute hasneto:partOfSchema ?schemaUri . "
				+ " ?schemaAttribute hasneto:hasAttribute ?attribute . "
				+ " ?indicator rdfs:subClassOf chear:StudyIndicator . "
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
				Collections.getCollectionsName(Collections.METADATA_SPARQL), initStudyQuery);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		
		HashMap<String, HashMap<String, String>> mapStudyInfo = new HashMap<String, HashMap<String, String>>();
		ValueCellProcessing cellProc = new ValueCellProcessing();
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			String studyUri = soln.get("studyUri").toString();
			HashMap<String, String> studyInfo = null;
			if (!mapStudyInfo.containsKey(studyUri)) {
				studyInfo = new HashMap<String, String>();
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
						+ cellProc.replaceNameSpaceEx(studyInfo.get("studyUri").toString()) + "\">"
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
			if (soln.contains("indicatorLabel")){
				String key = soln.get("indicatorLabel").toString().
						replace(",", "").replace(" ", "") + "_i";
				if (!studyInfo.containsKey(key)) {
					studyInfo.put(key, soln.get("attributeLabel").toString());
				}
			}
		}
		
		ArrayList<JSONObject> results = new ArrayList<JSONObject>();
		for (HashMap<String, String> info : mapStudyInfo.values()) {
			results.add(new JSONObject(info));
		}
		
		return SolrUtils.commitJsonDataToSolr(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.STUDIES, results.toString());
	}
	
	public static void updateAnalyteIndicator() {
		String analyteQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT ?analyteIndicator ?label ?comment WHERE { "
				+ " ?analyteIndicator rdfs:subClassOf chear:TargetedAnalyte . "
				+ " OPTIONAL { ?analyteIndicator rdfs:label ?label } . "
				+ " OPTIONAL { ?analyteIndicator rdfs:comment ?comment } . }";
		QueryExecution qexecAnalyte = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), analyteQuery);
		ResultSet analyteResults = qexecAnalyte.execSelect();
		ResultSetRewindable resultsrwAnalyte = ResultSetFactory.copyResults(analyteResults);
		qexecAnalyte.close();
		
		Map<String, String> analyteMap = new TreeMap<String, String>();
		while (resultsrwAnalyte.hasNext()) {
			QuerySolution soln = resultsrwAnalyte.next();
			analyteMap.put(soln.get("analyteIndicator").toString(), 
						   soln.get("label").toString());
		}
		
		SolrUtils.commitJsonDataToSolr(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.ANALYTES, getAnalyteJson(analyteMap));
	}
	
	public static String getAnalyteJson(Map<String, String> analyteMap) {
		ArrayList<HashMap<String, String>> arrAnalyteInfo = new ArrayList<HashMap<String, String>>();
		for(Map.Entry<String, String> entry : analyteMap.entrySet()) {
			String analyteUriString = entry.getKey().toString();
		    String label = entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + "Label_i";
			String indvAnalyteQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
					+ "SELECT DISTINCT ?indvIndicatorUri ?label WHERE {  "
					+ "?schemaUri hasco:isSchemaOf ?indvIndicatorUri . "
					+ "?schemaAttribute hasneto:partOfSchema ?schemaUri . "
					+ "?schemaAttribute hasneto:hasAttribute ?analyteIndicator . "
					+ "?analyteIndicator rdfs:subClassOf+ <" + analyteUriString + "> . "
                    + "?analyteIndicator rdfs:label ?label . " 
					+ "}";
			QueryExecution qexecIndvAnalyte = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), indvAnalyteQuery);
			ResultSet indvAnalyteResults = qexecIndvAnalyte.execSelect();
			ResultSetRewindable resultsrwIndvAnalyte = ResultSetFactory.copyResults(indvAnalyteResults);
			qexecIndvAnalyte.close();
			while (resultsrwIndvAnalyte.hasNext()) {
				QuerySolution soln = resultsrwIndvAnalyte.next();
				String studyUri = soln.get("indvIndicatorUri").toString();
				boolean bContain = false;
				for (HashMap<String, String> info : arrAnalyteInfo) {
					if (info.containsValue(studyUri)) {
						info.put(label, soln.get("label").toString());
						bContain = true;
						break;
					}
				}
				if (!bContain) {
					HashMap<String, String> newInfo = new HashMap<String, String>();
					newInfo.put("studyUri", studyUri);
					newInfo.put(label, soln.get("label").toString());
					arrAnalyteInfo.add(newInfo);
				}
			}
		}
		
		ArrayList<JSONObject> results = new ArrayList<JSONObject>();
		for (HashMap<String, String> info : arrAnalyteInfo) {
			results.add(new JSONObject(info));
		}

		return results.toString();
	}
	
	public static Map<String, List<String>> findSubject() {
		String subjectQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT ?subjectUri ?subjectType ?subjectLabel ?cohort ?study WHERE { "
    	        + " ?subjectUri hasco:isSubjectOf* ?cohort . "
    	        + " ?cohort hasco:isCohortOf ?study . "
    	        + " OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . "
    	        + " OPTIONAL { ?subjectUri a ?subjectType } . "
    	        + " }";		
        
		Query subjectQuery = QueryFactory.create(subjectQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		Map<String, List<String>> subjectResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			values = new ArrayList<String>();
			values.add("Label: " + soln.get("subjectLabel").toString());
			values.add("Type: " + soln.get("subjectType").toString());
			values.add("Cohort: " + soln.get("cohort").toString());
			values.add("Study: " + soln.get("study").toString());
			subjectResult.put(soln.get("subjectUri").toString(), values);
		}
		
		return subjectResult;
	}
		
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result update() {
		updateAnalyteIndicator();
		updateStudy();
		
		return redirect(routes.MetadataAcquisition.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postUpdate() {
    	return update();
    }
}