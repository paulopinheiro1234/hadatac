package org.hadatac.console.controllers.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.utils.NameSpaces;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.hadatac.console.controllers.metadataacquisition.ViewStudy;
import org.hadatac.console.views.html.metadata.*;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.metadata.loader.*;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpace;
import org.json.simple.JSONObject;
import org.labkey.remoteapi.query.*;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;

public class DynamicFunctions extends Controller {
	
	public static String getPrefixes(){
		String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList().replaceAll("\n", " ");
		return prefixString;
	}
	
	public static Map<String,String> getPrefixMap(){
		NameSpaces.getInstance();
		Map<String,String> prefixMap = new HashMap<String,String>();
		for (Map.Entry<String, NameSpace> entry : NameSpaces.table.entrySet()) {
			String abbrev = entry.getKey().toString();
	        NameSpace ns = entry.getValue();
	        prefixMap.put(abbrev, ns.getName());
	    }
		return prefixMap;
	}
	
	public static String replaceURLWithPrefix(String label){
		Map<String,String> prefixMap = getPrefixMap();
		for (Map.Entry<String, String> prefixes : prefixMap.entrySet()){
//			System.out.println("Prefix Values: " + prefixes.getValue() + "\n");
			if (label.contains(prefixes.getValue())){
				label = label.replaceAll(prefixes.getValue(), prefixes.getKey() + ":");
			}
		}
		return label;
	}
	
	public static String replacePrefixWithURL(String label){
		Map<String,String> prefixMap = getPrefixMap();
		for (Map.Entry<String, String> prefixes : prefixMap.entrySet()){
			if (label.contains(prefixes.getKey() + ":")){
				label = label.replaceAll(prefixes.getKey() + ":", prefixes.getValue());
			}
		}
		return label;
	}
	
	public static Map<String, String> getIndicatorTypes(){
		String indicatorQuery= getPrefixes() + "SELECT DISTINCT ?indicatorType ?label ?comment WHERE { ?indicatorType rdfs:subClassOf chear:Indicator . ?indicatorType rdfs:label ?label . }";
		QueryExecution qexecInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
		ResultSet indicatorResults = qexecInd.execSelect();
		ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
		qexecInd.close();
		
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		while (resultsrwIndc.hasNext()) {
			QuerySolution soln = resultsrwIndc.next();
			indicatorLabel = "";
			if (soln.contains("label"))
				indicatorLabel = soln.get("label").toString();
			if (soln.contains("indicatorType")){
				String indicatorType = replaceURLWithPrefix(soln.get("indicatorType").toString());
				indicatorMap.put(indicatorType, indicatorLabel);
			}
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		//System.out.println("Indicator Types: " + indicatorMapSorted);
		return indicatorMapSorted;
	}
	
	public static Map<String, Map<String,String>> getIndicatorValuesAndLabels(Map<String, String> indicatorMap){
		Map<String, Map<String,String>> indicatorValueMap = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		String indicatorValue = "";
		String indicatorValueLabel = "";
		for(Map.Entry<String, String> entry : indicatorMap.entrySet()){
			values = new HashMap<String, String>();
		    String indicatorType = entry.getKey().toString();
		    String indvIndicatorQuery = getPrefixes() + "SELECT DISTINCT ?indicator " +
					"(MIN(?label_) AS ?label)" +
					"WHERE { ?indicator rdfs:subClassOf " + indicatorType + " . " +
					"?indicator rdfs:label ?label_ . " + 
					"} GROUP BY ?indicator ?label";

		    QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
			ResultSet indvIndResults = qexecIndvInd.execSelect();
			ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
			qexecIndvInd.close();
			while (resultsrwIndvInd.hasNext()) {
				QuerySolution soln = resultsrwIndvInd.next();
				indicatorValueLabel = "";
				if (soln.contains("label")){
					indicatorValueLabel = soln.get("label").toString();
				}
				else {
					System.out.println("getIndicatorValues() No Label: " + soln.toString() + "\n");
				}
				if (soln.contains("indicator")){
					indicatorValue = replaceURLWithPrefix(soln.get("indicator").toString());
					values.put(indicatorValue,indicatorValueLabel);
				}
			}
			indicatorValueMap.put(indicatorType,values);
		}
		return indicatorValueMap;
	}
	
	public static Map<String, List<String>> getIndicatorValues(Map<String, String> indicatorMap){
		Map<String, List<String>> indicatorValueMap = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		String indicatorValueLabel = "";
		for(Map.Entry<String, String> entry : indicatorMap.entrySet()){
			values = new ArrayList<String>();
		    String indicatorType = entry.getKey().toString();
		    String indvIndicatorQuery = getPrefixes() + "SELECT DISTINCT ?indicator " +
					"(MIN(?label_) AS ?label)" +
					"WHERE { ?indicator rdfs:subClassOf " + indicatorType + " . " +
					"?indicator rdfs:label ?label_ . " + 
					"} GROUP BY ?indicator ?label";

		    QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
			ResultSet indvIndResults = qexecIndvInd.execSelect();
			ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
			qexecIndvInd.close();
			while (resultsrwIndvInd.hasNext()) {
				QuerySolution soln = resultsrwIndvInd.next();
				if (soln.contains("label")){
					indicatorValueLabel = replaceURLWithPrefix(soln.get("indicator").toString());
					values.add(indicatorValueLabel);
				}
				else {
					System.out.println("getIndicatorValues() No Label: " + soln.toString() + "\n");
				}
			}
			indicatorValueMap.put(indicatorType,values);
		}
		return indicatorValueMap;
	}	
	
	public static Map<String, Map<String,String>> findSample(String subject_uri) {
		String sampleQueryString = "";		
    	sampleQueryString = getPrefixes() +
    	"SELECT ?sampleUri ?subjectUri ?subjectLabel ?sampleType ?sampleLabel ?cohortLabel ?comment" +
		 "WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort ." +
		 "       		?sampleUri hasco:isSampleOf ?subjectUri ." +
		 "				?sampleUri rdfs:comment ?comment . " +
		 "				?cohort rdfs:label ?cohortLabel . " +
		 "       		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } .  " + 
		 "       		OPTIONAL { ?sampleUri rdfs:label ?sampleLabel } .  " + 
		 "       		OPTIONAL { ?sampleUri a ?sampleType  } .  " +
         "      FILTER (?subjectUri = " + subject_uri + " ) .  " +
		 "                            }";
    	Query basicQuery = QueryFactory.create(sampleQueryString);
    	
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Map<String, Map<String,String>> sampleResult = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			values = new HashMap<String, String>();
			if (soln.contains("sampleLabel"))
				values.put("Label", soln.get("sampleLabel").toString());
			if (soln.contains("sampleType"))
				values.put("Type", replaceURLWithPrefix(soln.get("sampleType").toString()));
			if (soln.contains("subjectLabel"))
				values.put("Subject", soln.get("subjectLabel").toString());
			if (soln.contains("subjectUri"))
				values.put("SubjectURI", replaceURLWithPrefix(soln.get("subjectUri").toString()));
			if (soln.contains("comment"))
				values.put("Comment", soln.get("comment").toString());
			if (soln.contains("cohortLabel"))
				values.put("Cohort", soln.get("cohortLabel").toString());
			sampleResult.put(replaceURLWithPrefix(soln.get("sampleUri").toString()),values);	
//			System.out.println("Samples: " + sampleResult);	
		}

		return sampleResult;
	}

	public static Map<String, Map<String,String>> findSamples() {
		String sampleQueryString = "";		
    	sampleQueryString = getPrefixes() +
    	"SELECT ?sampleUri ?subjectUri ?subjectLabel ?sampleType ?sampleLabel ?cohortLabel ?comment" +
		 "WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort ." +
		 "       		?sampleUri hasco:isSampleOf ?subjectUri ." +
		 "				?sampleUri rdfs:comment ?comment . " +
		 "				?cohort rdfs:label ?cohortLabel . " +
		 "       		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } .  " + 
		 "       		OPTIONAL { ?sampleUri rdfs:label ?sampleLabel } .  " + 
		 "       		OPTIONAL { ?sampleUri a ?sampleType  } .  " +
		 "                            }";
    	Query basicQuery = QueryFactory.create(sampleQueryString);
    	
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Map<String, Map<String,String>> sampleResult = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			values = new HashMap<String, String>();
			if (soln.contains("sampleLabel"))
				values.put("Label", soln.get("sampleLabel").toString());
			if (soln.contains("sampleType"))
				values.put("Type", replaceURLWithPrefix(soln.get("sampleType").toString()));
			if (soln.contains("subjectLabel"))
				values.put("Subject", soln.get("subjectLabel").toString());
			if (soln.contains("subjectUri"))
				values.put("SubjectURI", replaceURLWithPrefix(soln.get("subjectUri").toString()));
			if (soln.contains("comment"))
				values.put("Comment", soln.get("comment").toString());
			if (soln.contains("cohortLabel"))
				values.put("Cohort", soln.get("cohortLabel").toString());
			sampleResult.put(replaceURLWithPrefix(soln.get("sampleUri").toString()),values);	
//			System.out.println("Samples: " + sampleResult);	
		}
		return sampleResult;
	}
	
	// for /metadata HTTP GET requests
    public static Result index() {
    	Map<String,String> indicators = getIndicatorTypes();
    	System.out.println("Indicators: " + indicators + "\n");
    	Map<String,List<String>> valueMap = getIndicatorValues(indicators);
    	System.out.println("Indicator Values: " + valueMap + "\n");
    	Map<String,Map<String,String>> valueMapWithLabels = getIndicatorValuesAndLabels(indicators);
    	System.out.println("Indicator Values and Labels: " + valueMapWithLabels + "\n");
    	System.out.println(findSample("chear-kb:SBJ-0032-Pilot-6"));
    	System.out.println(replaceURLWithPrefix("http://hadatac.org/ont/chear#BloodPlasma"));
    	System.out.println(replacePrefixWithURL("chear-kb:SBJ-0032-Pilot-6"));
    	System.out.println(findSamples());
        return ok();        
    }// /index()

    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return index();
        
    }// /postIndex()

}
