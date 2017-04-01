package org.hadatac.console.controllers.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.views.html.metadata.*;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONObject;

public class DynamicGeneration extends Controller {

	public static Map<String, List<String>> queryStudy() {
		String initStudyQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT DISTINCT ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment "
				+ " (group_concat( ?agentName_ ; separator = ' & ') as ?agentName) ?institutionName WHERE { "
				+ " ?subUri rdfs:subClassOf hasco:Study . "
				+ " ?studyUri a ?subUri . "
				+ " OPTIONAL{ ?studyUri rdfs:label ?studyLabel } . "
				+ " OPTIONAL{ ?studyUri hasco:hasProject ?proj } . "
				+ " OPTIONAL{ ?studyUri skos:definition ?studyTitle } . "
				+ " OPTIONAL{ ?studyUri rdfs:comment ?studyComment } . "
				+ " OPTIONAL{ ?studyUri hasco:hasAgent ?agent . "
				+ "           ?agent foaf:name ?agentName_ } . "
				+ " OPTIONAL{ ?studyUri hasco:hasInstitution ?institution . "
				+ "           ?institution foaf:name ?institutionName } . } "
				+ " GROUP BY ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment ?agentName ?institutionName ";
		
		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), initStudyQuery);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		
		Map<String, List<String>> initStudyMap = new HashMap<String, List<String>>();
		
		ArrayList<HashMap<String, String>> arrStudyInfo = new ArrayList<HashMap<String, String>>();
		ValueCellProcessing cellProc = new ValueCellProcessing();
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			HashMap<String, String> studyInfo = new HashMap<String, String>();
			List<String> initStudyValues = new ArrayList<String>();
			if (soln.contains("studyUri")){
				initStudyMap.put(soln.get("studyUri").toString(), initStudyValues);
				studyInfo.put("studyUri", soln.get("studyUri").toString());
			}
			if (soln.contains("studyLabel")){
				initStudyValues.add("Label: " + soln.get("studyLabel").toString());
				studyInfo.put("studyLabel_i", "<a href=\""
						+ Play.application().configuration().getString("hadatac.console.host_deploy") 
						+ "/hadatac/metadataacquisitions/viewStudy?study_uri=" 
						+ cellProc.replaceNameSpaceEx(studyInfo.get("studyUri").toString()) + "\">"
						+ soln.get("studyLabel").toString() + "</a>");
			}
			if (soln.contains("studyTitle")){
				initStudyValues.add("Title: " + soln.get("studyTitle").toString());
				studyInfo.put("studyTitle_i", soln.get("studyTitle").toString());
			}
			if (soln.contains("proj")){
				initStudyValues.add("Project: " + cellProc.replaceNameSpaceEx(soln.get("proj").toString()));
				studyInfo.put("proj_i", soln.get("proj").toString());
			}
			if (soln.contains("studyComment")){
				initStudyValues.add("Comment: " + soln.get("studyComment").toString());
				studyInfo.put("studyComment_i", soln.get("studyComment").toString());
			}
			if (soln.contains("agentName")){
				initStudyValues.add("Agent(s): " + soln.get("agentName").toString());
				studyInfo.put("agentName_i", soln.get("agentName").toString());
			}
			if (soln.contains("institutionName")){
				initStudyValues.add("Institution: " + soln.get("institutionName").toString());
				studyInfo.put("institutionName_i", soln.get("institutionName").toString());
			}
			arrStudyInfo.add(studyInfo);
		}
		
		updateStudyIndicator(arrStudyInfo);
		
		return initStudyMap;
	}
		
	public static void updateStudyIndicator(ArrayList<HashMap<String, String>> arrStudyInfo) {
		String indicatorQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT ?studyIndicator ?label ?comment WHERE { "
				+ " ?studyIndicator rdfs:subClassOf chear:StudyIndicator . "
				+ " ?studyIndicator rdfs:label ?label . "
				+ " OPTIONAL { ?studyIndicator rdfs:comment ?comment } . "
				+ " }";
		QueryExecution qexecInd = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
		ResultSet indicatorResults = qexecInd.execSelect();
		ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
		qexecInd.close();
		
		Map<String, String> indicatorMap = new TreeMap<String, String>();
		while (resultsrwIndc.hasNext()) {
			QuerySolution soln = resultsrwIndc.next();
			indicatorMap.put(soln.get("studyIndicator").toString(), 
					         soln.get("label").toString());
		}
		
		SolrUtils.commitJsonDataToSolr(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.STUDIES, getIndicatorJson(indicatorMap, arrStudyInfo));
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
	
	public static String getIndicatorJson(Map<String, String> indicatorMap,
			                              ArrayList<HashMap<String, String>>  arrStudyInfo) {
		for(Map.Entry<String, String> entry : indicatorMap.entrySet()) {
			String studyIndicatorUri = entry.getKey().toString();
			String label = entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + "Label_i";
			String indvIndicatorQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
					+ " SELECT DISTINCT ?indvIndicatorUri ?label WHERE { "
					+ " ?schemaUri hasco:isSchemaOf ?indvIndicatorUri . "
					+ " ?schemaAttribute hasneto:partOfSchema ?schemaUri . "
					+ " ?schemaAttribute hasneto:hasAttribute ?attribute . "
					+ " ?attribute rdfs:subClassOf+ <" + studyIndicatorUri + "> . "
					+ " ?attribute rdfs:label ?label . "
					+ " }";
			QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
			ResultSet indvIndResults = qexecIndvInd.execSelect();
			ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
			qexecIndvInd.close();
			
			while (resultsrwIndvInd.hasNext()) {
				QuerySolution soln = resultsrwIndvInd.next();
				String studyUri = soln.get("indvIndicatorUri").toString();
				boolean bContain = false;
				for (HashMap<String, String> info : arrStudyInfo) {
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
					arrStudyInfo.add(newInfo);
				}
			}
		}
		
		ArrayList<JSONObject> results = new ArrayList<JSONObject>();
		for (HashMap<String, String> info : arrStudyInfo) {
			results.add(new JSONObject(info));
		}

		return results.toString();
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
		
    public static Result index() {
		updateAnalyteIndicator();
		Map<String, List<String>> studyResult = queryStudy();
		Map<String, List<String>> subjectResult = findSubject();
		Map<String, Map<String, String>> indicatorResults = new HashMap<String, Map<String,String>>();
		
        return ok(dynamicPage.render(studyResult, subjectResult, indicatorResults));
    }

    public static Result postIndex() {
    	return index(); 
    }
}
