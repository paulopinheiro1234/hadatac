package org.hadatac.console.controllers.metadataacquisition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.net.URLEncoder;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewSubject extends Controller {

	public static Map<String, List<String>> findSubjectIndicators(String study_uri, String subject_uri) {
		String indicatorQuery = "";
                if (study_uri.startsWith("http")) {
			study_uri = "<" + study_uri + ">";
		}
                if (subject_uri.startsWith("http")) {
			subject_uri = "<" + subject_uri + ">";
		}
		indicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
		indicatorQuery += "SELECT ?subjectIndicator ?label ?comment WHERE { "
				+ "?subjectIndicator rdfs:subClassOf chear:StudyIndicator . "
				+ "?subjectIndicator rdfs:label ?label . "
				+ "?subjectIndicator rdfs:comment ?comment . }";
		QueryExecution qexecInd = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
		ResultSet indicatorResults = qexecInd.execSelect();
		ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
		qexecInd.close();
		
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		while (resultsrwIndc.hasNext()) {
			QuerySolution soln = resultsrwIndc.next();
			indicatorLabel = soln.get("label").toString();
			indicatorMap.put(soln.get("subjectIndicator").toString(), indicatorLabel);		
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		Map<String, List<String>> indicatorValues = new HashMap<String, List<String>>();
		
		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
			String parentIndicatorUri = entry.getKey();
			String indvIndicatorQuery = "";
			indvIndicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
//			indvIndicatorQuery += "SELECT DISTINCT ?uri ?label ?comment WHERE { "
//  					+ "?schemaUri	hasco:isSchemaOf " + study_uri + " ."
//					+ "?uri	hasneto:partOfSchema	?schemaUri . "
//					+ "?uri	hasneto:hasEntity		sio:Human . "
//					+ "?subIndicator rdfs:subClassOf* <" + parentIndicatorUri + "> . "
//					+ "?uri 	hasneto:hasAttribute	?subIndicator ."
//					+ "?uri rdfs:label		?label . "
//					+ "?uri rdfs:comment	?comment ."
//					+ "}";
			
			indvIndicatorQuery += "SELECT DISTINCT ?label ?uri WHERE { "
					+ "?schemaUri hasco:isSchemaOf " + study_uri + " . "
					+ "?schemaAttribute hasneto:partOfSchema ?schemaUri . "
					+ "?schemaAttribute hasneto:hasAttribute ?uri . " 
					+ "?uri rdfs:subClassOf* <" + parentIndicatorUri + "> . "
					+ "?uri rdfs:label ?label . "
					+ "}";
			
			
			try {
				QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(
						Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
				ResultSet indvIndResults = qexecIndvInd.execSelect();
				ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
				qexecIndvInd.close();
				List<String> listIndicatorLabel = new ArrayList<String>();
				while (resultsrwIndvInd.hasNext()) {
					QuerySolution soln = resultsrwIndvInd.next();
					ValueCellProcessing cellProc = new ValueCellProcessing();
					if(Measurement.find(findUser(), study_uri, cellProc.convertToWholeURI(subject_uri), soln.get("uri").toString()).documents.size() > 0){
						listIndicatorLabel.add(soln.get("label").toString());
						System.out.println("HEREHERE" + Measurement.find(findUser(), study_uri, cellProc.convertToWholeURI(subject_uri), soln.get("uri").toString()).documents.get(0).getObjectUri().toString() 
								+ Measurement.find(findUser(), study_uri, cellProc.convertToWholeURI(subject_uri), soln.get("uri").toString()).documents.get(0).getCharacteristic().toString()
								+ Measurement.find(findUser(), study_uri, cellProc.convertToWholeURI(subject_uri), soln.get("uri").toString()).documents.get(0).getValue().toString());
					}
//					listIndicatorLabel.add(soln.get("comment").toString());
				}
				indicatorValues.put(entry.getValue().toString(), listIndicatorLabel);
			} catch (QueryExceptionHTTP e) {
				e.printStackTrace();
			}
		}
		return indicatorValues;
	}
	
    /* public static String findBasicHTML(String subject_uri) {
	        String str = "";
		System.out.println("findBasicHTML (input): <" + subject_uri + ">");
   	        Map<String, List<String>> subjObj = findBasic(subject_uri);
                if (subjObj != null) {
        	    System.out.println("findBasicHTML (1)");
		    for (String key : subjObj.keySet()) {
		      System.out.println("findBasicHTML (key): <" + key + ">");
		    }
         	    List<String> values = new ArrayList<String>();
                    values = subjObj.get(subject_uri);
                    if (values != null) {
                    	System.out.println("findBasicHTML (2)");
		        for (String v : values) {
			    str += v + "<br/>";
		        } 
		    }
                }
		System.out.println("findBasicHTML (output): <" + str + ">");
                return str;
		} */

	public static String findBasicHTML(String subject_uri) {
		System.out.println("findBasicHTML (subject_uri): '" + subject_uri + "'" );
		if (subject_uri == null || subject_uri.equals("")) {
			return null;
		}
	 	ResultSetRewindable resultsrw = findSubjectBasic(subject_uri); 
	 	if (resultsrw == null) {
	 		return null;
	 	}
	 	String html = "";
	
	 	if (resultsrw.hasNext()) {
	 		QuerySolution soln = resultsrw.next();
	 		html += "<table>";
	 		html += "<tr> <td><b>Original ID &nbsp; &nbsp;</b></td> <td>" + soln.get("pid").toString() + "</td></tr>";
	 		html += "<tr> <td><b>Internal ID</b></td> <td>" + soln.get("subjectLabel").toString() + "</td></tr>";
	 		html += "<tr> <td><b>Type</b></td> <td>" + soln.get("subjectTypeLabel").toString() + "</td></tr>";
	 		html += "<tr> <td><b>Cohort</b></td> <td>" + soln.get("cohortLabel").toString() + "</td></tr>";
	 		html += "<tr> <td><b>Study</b></td> <td>" + soln.get("studyLabel").toString() + "</td></tr>";
	 		html += "<tr> <td> &nbsp;</td> <td> &nbsp;</td></tr>";
	 		html += "<tr> <td></td> <td><a href='/hadatac/metadataacquisitions/viewSubject?study_uri=" 
	 				+ URLEncoder.encode(soln.get("studyUri").toString()) 
	 				+ "&subject_uri=" + URLEncoder.encode(subject_uri) 
	 				+ "'>(More info about object)</a></td></tr>";
	 		html += "</table>";
	 	}
	 	
	 	return html;
	}

	public static ResultSetRewindable findSubjectBasic(String subject_uri) {
                System.out.println("in findSubjectBasic (1): '" + subject_uri + "'");
		String subjectQueryString = "";
                if (subject_uri == null || subject_uri.equals("")) {
		    return null;
		}
		if (subject_uri.indexOf("http") != -1) {
		    subject_uri = "<" + subject_uri + ">";
		}
                System.out.println("in findSubjectBasic (2): '" + subject_uri + "'");
           	subjectQueryString += NameSpaces.getInstance().printSparqlNameSpaceList();
    	        subjectQueryString += "SELECT ?pid ?subjectTypeLabel ?subjectLabel ?cohortLabel ?studyUri ?studyLabel WHERE { "
    			+ subject_uri + " hasco:originalID ?pid . "
    			+ "?subjectUri hasco:isSubjectOf* ?cohort . "
    			+ "?studyUri rdfs:label ?studyLabel . "
    			+ "?cohort hasco:isCohortOf ?studyUri . "
    			+ "?cohort rdfs:label ?cohortLabel . "
    			+ "OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . "
    			+ "OPTIONAL { ?subjectUri a ?subjectType . "
    			+ "			  ?subjectType rdfs:label ?subjectTypeLabel } . "
    			+ "FILTER ( ?subjectUri = " + subject_uri + " ) . "
    			+ "}";
		
    	        Query basicQuery = QueryFactory.create(subjectQueryString);
    	
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		return resultsrw;
	}		

	public static Map<String, List<String>> findBasic(String subject_uri) {
        	System.out.println("in findBasic (subject_uri): '" + subject_uri + "'" );
                if (subject_uri == null || subject_uri.equals("")) {
		    return null;
		}
         	ResultSetRewindable resultsrw = findSubjectBasic(subject_uri); 
		if (resultsrw == null) {
		    return null;
		}
		Map<String, List<String>> subjectResult = new HashMap<String, List<String>>();
		List<String> values; // = new ArrayList<String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			//System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values = new ArrayList<String>();
			values.add("Pid: " + soln.get("pid").toString());
			values.add("Label: " + soln.get("subjectLabel").toString());
			values.add("Type: " + soln.get("subjectTypeLabel").toString());
			values.add("Cohort: " + soln.get("cohortLabel").toString());
			values.add("Study: " + soln.get("studyLabel").toString());
			subjectResult.put(subject_uri, values);
			//System.out.println("THIS IS SUBROW*********" + subjectResult);	
		}
		
		return subjectResult;
	}
	
	public static Map<String, String> findSubjectIndicatorsUri(String study_uri) {
		String indicatorQuery = "";
                if (study_uri.startsWith("http")) {
			study_uri = "<" + study_uri + ">";
		}
		indicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
		indicatorQuery += "SELECT ?subjectIndicator ?label ?comment WHERE { "
				+ "?subjectIndicator rdfs:subClassOf chear:StudyIndicator . "
				+ "?subjectIndicator rdfs:label ?label . "
				+ "?subjectIndicator rdfs:comment ?comment . }";
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		try {
			QueryExecution qexecInd = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
			ResultSet indicatorResults = qexecInd.execSelect();
			ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
			qexecInd.close();
			while (resultsrwIndc.hasNext()) {
				QuerySolution soln = resultsrwIndc.next();
				indicatorLabel = soln.get("label").toString();
				indicatorMap.put(soln.get("subjectIndicator").toString(),indicatorLabel);		
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		Map<String, String> indicatorUris = new HashMap<String, String>();
		
		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
			String parentIndicatorUri = entry.getKey();
			String indvIndicatorQuery = "";
			indvIndicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
//			indvIndicatorQuery += "SELECT DISTINCT ?uri ?label ?comment WHERE { "
//  					+ "?schemaUri	hasco:isSchemaOf " + study_uri + " ."
//					+ "?uri	hasneto:partOfSchema	?schemaUri . "
//					+ "?uri	hasneto:hasEntity		sio:Human . "
//					+ "?subIndicator rdfs:subClassOf* <" + parentIndicatorUri + "> . "
//					+ "?uri 	hasneto:hasAttribute	?subIndicator ."
//					+ "?uri rdfs:label		?label . "
//					+ "?uri rdfs:comment	?comment ."
//					+ "}";
			
			indvIndicatorQuery += "SELECT DISTINCT ?label ?uri WHERE { "
					+ "?schemaUri hasco:isSchemaOf " + study_uri + " . "
					+ "?schemaAttribute hasneto:partOfSchema ?schemaUri . "
					+ "?schemaAttribute hasneto:hasAttribute ?uri . " 
					+ "?uri rdfs:subClassOf* <" + parentIndicatorUri + "> . "
					+ "?uri rdfs:label ?label . "
					+ "}";
			
			try {
				QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(
						Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
				ResultSet indvIndResults = qexecIndvInd.execSelect();
				ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
				qexecIndvInd.close();
				while (resultsrwIndvInd.hasNext()) {
					QuerySolution soln = resultsrwIndvInd.next();
					System.out.println("Solution: " + soln);
					indicatorUris.put(soln.get("label").toString(), soln.get("uri").toString());
				}
			} catch (QueryExceptionHTTP e) {
				e.printStackTrace();
			}
		}
		
		return indicatorUris;
	}
	
	public static Map<String, List<String>> findSampleMap(String subject_uri) {
		String sampleQueryString = "";
                if (subject_uri.startsWith("http")) {
			subject_uri = "<" + subject_uri + ">";
		}
    	        sampleQueryString += NameSpaces.getInstance().printSparqlNameSpaceList();
    	        sampleQueryString += "SELECT ?sampleUri ?subjectUri ?subjectLabel ?sampleType ?sampleLabel ?cohortLabel ?comment WHERE { "
		    	+ "?subjectUri hasco:isSubjectOf* ?cohort . "
		    	+ "?sampleUri hasco:isSampleOf ?subjectUri . "
		    	+ "?sampleUri rdfs:comment ?comment . "
		    	+ "?cohort rdfs:label ?cohortLabel . "
		    	+ "OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . "
		    	+ "OPTIONAL { ?sampleUri rdfs:label ?sampleLabel } . "
		    	+ "OPTIONAL { ?sampleUri a ?sampleType  } . "
		    	+ "FILTER ( ?subjectUri = " + subject_uri + " ) . "
		    	+ "}";
    	        Query basicQuery = QueryFactory.create(sampleQueryString);
    	
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Map<String, List<String>> sampleResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		
		ValueCellProcessing cellProc = new ValueCellProcessing();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values = new ArrayList<String>();
			values.add("Label: " + soln.get("sampleLabel").toString());
			values.add("Type: " + cellProc.replaceNameSpaceEx(soln.get("sampleType").toString()));
			values.add("Sample Of: " + cellProc.replaceNameSpaceEx(soln.get("subjectLabel").toString()));
			sampleResult.put(cellProc.replaceNameSpaceEx(soln.get("sampleUri").toString()), values);
			System.out.println("THIS IS SUBROW*********" + sampleResult);	
		}

		return sampleResult;
	}
	
	public static List<String> findSample(String subject_uri) {
		String sampleQueryString = "";
    	sampleQueryString += NameSpaces.getInstance().printSparqlNameSpaceList();
    	sampleQueryString += "SELECT * WHERE { "
    			+ "?s <http://hadatac.org/ont/hasco/isSampleOf> " + subject_uri + " . "
    			+ "}";
        
		Query sampleQuery = QueryFactory.create(sampleQueryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), sampleQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		List<String> sampleResult = new ArrayList<String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE SAMPLES*********" + soln.toString());
			sampleResult.add(soln.get("s").toString());
			System.out.println("THIS IS SUBROW*********" + sampleResult);
		}
		return sampleResult;
	}
	
	public static String findUser() {
		String results = null;
	    final SysUser user = AuthApplication.getLocalUser(session());
	    if(null == user){
	        results = null;
	    }
	    else{
	    	results = UserManagement.getUriByEmail(user.getEmail());
	    }
	    System.out.println("This is the current user's uri:" + results);
	    
	    return results;
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String study_uri, String subject_uri) {
		Map<String, List<String>> indicatorValues = findSubjectIndicators(study_uri, subject_uri);
    	        Map<String, List<String>> subjectResult = findBasic(subject_uri);
    	        Map<String, List<String>> sampleResult = findSampleMap(subject_uri);

		Map<String, String> indicatorUris = findSubjectIndicatorsUri(study_uri);
		
		Map<String, String> showValues = new HashMap<String, String>();
		ValueCellProcessing cellProc = new ValueCellProcessing();
		showValues.put("subject", cellProc.convertToWholeURI(subject_uri));
		showValues.put("user", findUser());
		showValues.put("study", study_uri);		
    	
    	return ok(viewSubject.render(subjectResult, sampleResult, indicatorValues, indicatorUris, showValues));    
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String study_uri, String subject_uri) {
		return index(study_uri, subject_uri);
	}
}