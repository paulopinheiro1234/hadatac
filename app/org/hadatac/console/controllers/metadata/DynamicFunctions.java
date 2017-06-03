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
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadataacquisition.ViewStudy;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.metadata.*;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.data.loader.SampleGenerator;
import org.hadatac.data.loader.SubjectGenerator;
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
	
	public static String replaceTabNameWithConcept(String tabName){
		Map<String, String> indicatorTypes = getIndicatorTypes();
		Map<String,Map<String,String>> valueMapWithLabels = getIndicatorValuesAndLabels(indicatorTypes);
		String returnString = "";
		for (String key : valueMapWithLabels.keySet() ){
			for (String k : valueMapWithLabels.get(key).keySet()){
				/*System.out.println("k: " + k + "\n");
				System.out.println("value: " + valueMapWithLabels.get(key).get(k) + "\n");
				System.out.println("value reduced: " + valueMapWithLabels.get(key).get(k).replace(" ","").replace(",","")+ "\n");
				System.out.println("tabName: " + tabName + "\n");*/
				if (tabName.equals(valueMapWithLabels.get(key).get(k).replace(" ","").replace(",",""))){
					returnString = k;
					return returnString;
				}
			}
		}
		return returnString;
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
		String indicatorQuery= getPrefixes() 
				+ "SELECT DISTINCT ?indicatorType ?label ?comment WHERE { "
				+ "?indicatorType rdfs:subClassOf hasco:Indicator . "
				+ "?indicatorType rdfs:label ?label . "
				+ "}";
		Map<String, String> indicatorMap = new HashMap<String, String>();
		try {
			QueryExecution qexecInd = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
			ResultSet indicatorResults = qexecInd.execSelect();
			ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
			qexecInd.close();
			
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
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
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
		    try {
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
		    } catch (QueryExceptionHTTP e) {
				e.printStackTrace();
			}
		}
		return indicatorValueMap;
	}
	
	public static Map<String, List<String>> getIndicatorValuesJustLabels(
			Map<String, String> indicatorMap){
		
		Map<String, List<String>> indicatorValueMap = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		String indicatorValueLabel = "";
		for(Map.Entry<String, String> entry : indicatorMap.entrySet()){
			values = new ArrayList<String>();
		    String indicatorType = entry.getKey().toString();
		    String indvIndicatorQuery = getPrefixes() 
		    		+ " SELECT DISTINCT ?indicator (MIN(?label_) AS ?label) WHERE { "
		    		+ " ?indicator rdfs:subClassOf " + indicatorType + " . "
		    		+ " ?indicator rdfs:label ?label_ . "
		    		+ " } "
					+ " GROUP BY ?indicator ?label";
		    try {
			    QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(
			    		Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
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
						values.add(indicatorValueLabel);
					}
				}
				String indicatorTypeLabel = entry.getValue().toString();
				indicatorValueMap.put(indicatorTypeLabel,values);
		    } catch (QueryExceptionHTTP e) {
				e.printStackTrace();
			}
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
		    try {
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
		    } catch (QueryExceptionHTTP e) {
				e.printStackTrace();
			}
		}
		return indicatorValueMap;
	}	
	
	public static Map<String, Map<String,String>> findStudy(String study_uri) {
		String studyQueryString = "";
		studyQueryString = getPrefixes() +
		"SELECT ?studyUri ?studyLabel ?proj ?studyDef ?studyComment ?agentName ?institutionName " + 
		" WHERE {        ?subUri rdfs:subClassOf hasco:Study . " + 
		"                       ?studyUri a ?subUri . " + 
		"           ?studyUri rdfs:label ?studyLabel  . " + 
		"			FILTER ( ?studyUri = " + study_uri + " ) . " +
		"		 OPTIONAL {?studyUri hasco:hasProject ?proj} . " +
		"        OPTIONAL { ?studyUri skos:definition ?studyDef } . " + 
		"        OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " + 
		"        OPTIONAL { ?studyUri hasco:hasAgent ?agent . " + 
		"                                   ?agent foaf:name ?agentName } . " + 
		"        OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " + 
		"                                 ?institution foaf:name ?institutionName} . " + 
		"                             }" ;
		Map<String, Map<String,String>> studyResult = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		
		try {
			Query studyQuery = QueryFactory.create(studyQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), studyQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				values = new HashMap<String, String>();
				if (soln.contains("studyLabel"))
					values.put("Label" , soln.get("studyLabel").toString());
				if (soln.contains("studyDef"))
					values.put("Title" , soln.get("studyDef").toString());
				if (soln.contains("proj"))
					values.put("Project" , replaceURLWithPrefix(soln.get("proj").toString()));
				if (soln.contains("studyComment"))
					values.put("Comment" , soln.get("studyComment").toString());
				if (soln.contains("agentName"))
					values.put("Agents" , soln.get("agentName").toString());
				if (soln.contains("institutionName"))
					values.put("Institution" , soln.get("institutionName").toString());
				studyResult.put(replaceURLWithPrefix(soln.get("studyUri").toString()),values);
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return studyResult;
	}
	
	public static Map<String, Map<String,String>> findStudies() {
		Map<String, Map<String,String>> studyResult = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		String studyQueryString = "";
		studyQueryString = getPrefixes() +
		"SELECT ?studyUri ?studyLabel ?proj ?studyDef ?studyComment ?agentName ?institutionName " + 
		" WHERE {        ?subUri rdfs:subClassOf hasco:Study . " + 
		"                       ?studyUri a ?subUri . " + 
		"           ?studyUri rdfs:label ?studyLabel  . " + 
		"		 OPTIONAL {?studyUri hasco:hasProject ?proj} . " +
		"        OPTIONAL { ?studyUri skos:definition ?studyDef } . " + 
		"        OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " + 
		"        OPTIONAL { ?studyUri hasco:hasAgent ?agent . " + 
		"                                   ?agent foaf:name ?agentName } . " + 
		"        OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " + 
		"                                 ?institution foaf:name ?institutionName} . " + 
		"                             }" ;
		try {
			Query studyQuery = QueryFactory.create(studyQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), studyQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				values = new HashMap<String, String>();
				if (soln.contains("studyLabel"))
					values.put("Label" , soln.get("studyLabel").toString());
				if (soln.contains("studyDef"))
					values.put("Title" , soln.get("studyDef").toString());
				if (soln.contains("proj"))
					values.put("Project" , replaceURLWithPrefix(soln.get("proj").toString()));
				if (soln.contains("studyComment"))
					values.put("Comment" , soln.get("studyComment").toString());
				if (soln.contains("agentName"))
					values.put("Agents" , soln.get("agentName").toString());
				if (soln.contains("institutionName"))
					values.put("Institution" , soln.get("institutionName").toString());
				studyResult.put(replaceURLWithPrefix(soln.get("studyUri").toString()),values);
				
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return studyResult;
	}
	public static Map<String, Map<String,String>> findSubjects() {
		Map<String, Map<String,String>> subjectResult = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		String subjectQueryString = "";		
		subjectQueryString = getPrefixes() +
    			"SELECT ?subjectUri ?subjectType ?subjectTypeLabel ?subjectLabel ?cohortLabel ?studyLabel " +
    	    	"			 WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort . " +
    	    	"							?study rdfs:label ?studyLabel ." +
    	    	"			        		?cohort hasco:isCohortOf ?study . " +
    	    	"							?cohort rdfs:label ?cohortLabel" +
    	    	"			        		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . " +
    	    	"			        		OPTIONAL { ?subjectUri a ?subjectType . " +
    	    	"										?subjectType rdfs:label ?subjectTypeLabel} . " +
    	    	"			                             }";
    	try {
			Query basicQuery = QueryFactory.create(subjectQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				values = new HashMap<String, String>();
				if (soln.contains("subjectLabel"))
					values.put("Label", soln.get("subjectLabel").toString());
				if (soln.contains("subjectType"))
					values.put("Type", replaceURLWithPrefix(soln.get("subjectType").toString()));
				if (soln.contains("subjectTypeLabel"))
					values.put("TypeLabel", soln.get("subjectTypeLabel").toString());
				if (soln.contains("cohortLabel"))
					values.put("Cohort", soln.get("cohortLabel").toString());
				if (soln.contains("studyLabel"))
					values.put("Study", replaceURLWithPrefix(soln.get("studyLabel").toString()));
				subjectResult.put(replaceURLWithPrefix(soln.get("subjectUri").toString()),values);
	//			System.out.println("Samples: " + sampleResult);	
			}
    	} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return subjectResult;
	}
	
	public static Map<String, Map<String,String>> findSubject(String subject_uri) {
		Map<String, Map<String,String>> subjectResult = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		String subjectQueryString = "";		
		subjectQueryString = getPrefixes() +
    			"SELECT ?subjectUri ?subjectType ?subjectTypeLabel ?subjectLabel ?cohortLabel ?studyLabel " +
    	    	"			 WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort . " +
    	    	"							?study rdfs:label ?studyLabel ." +
    	    	"			        		?cohort hasco:isCohortOf ?study . " +
    	    	"							?cohort rdfs:label ?cohortLabel" +
    	    	"			        		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . " +
    	    	"			        		OPTIONAL { ?subjectUri a ?subjectType . " +
    	    	"										?subjectType rdfs:label ?subjectTypeLabel} . " +
    	    	"			        		FILTER (?subjectUri = " + subject_uri + ") . " +
    	    	"			                             }";
	    try {
	    	Query basicQuery = QueryFactory.create(subjectQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();			
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				values = new HashMap<String, String>();
				if (soln.contains("subjectLabel"))
					values.put("Label", soln.get("subjectLabel").toString());
				if (soln.contains("subjectType"))
					values.put("Type", replaceURLWithPrefix(soln.get("subjectType").toString()));
				if (soln.contains("subjectTypeLabel"))
					values.put("TypeLabel", soln.get("subjectTypeLabel").toString());
				if (soln.contains("cohortLabel"))
					values.put("Cohort", soln.get("cohortLabel").toString());
				if (soln.contains("studyLabel"))
					values.put("Study", replaceURLWithPrefix(soln.get("studyLabel").toString()));
				subjectResult.put(replaceURLWithPrefix(soln.get("subjectUri").toString()),values);
	//			System.out.println("Samples: " + sampleResult);	
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return subjectResult;
	}
	
	
	public static Map<String, Map<String,String>> findSample(String sample_uri) {
		Map<String, Map<String,String>> sampleResult = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		String sampleQueryString = "";		
    	sampleQueryString = getPrefixes() +
    	"SELECT ?sampleUri ?subjectUri ?subjectLabel ?sampleType ?sampleLabel ?freezeThaw ?storageTemp ?storageTempUnit ?cohortLabel ?object ?samplingVolume ?samplingVolumeUnit ?comment" +
    	"WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort ." +
    	"       		?sampleUri hasco:isSampleOf ?subjectUri ." +
    	"				?sampleUri rdfs:comment ?comment . " +
    	"				OPTIONAL { ?sampleUri hasco:isObjectOf ?object . " +
    	"				?sampleUri hasco:hasSamplingVolume ?samplingVolume . " +
    	"				?sampleUri hasco:hasSamplingVolumeUnit ?samplingVolumeUnit . " +
    	"				?sampleUri hasco:hasStorageTemperature ?storageTemp . " +
    	"				?sampleUri hasco:hasStorageTemperatureUnit ?storageTempUnit . " +
    	"				?sampleUri hasco:hasNumFreezeThaw ?freezeThaw } . " +
    	"				?sampleUri rdfs:comment ?comment . " +
    	"				?cohort rdfs:label ?cohortLabel . " +
    	"       		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } .  " + 
    	"       		OPTIONAL { ?sampleUri rdfs:label ?sampleLabel } .  " + 
    	"       		OPTIONAL { ?sampleUri a ?sampleType  } .  " +
        "      FILTER (?sampleUri = " + sample_uri + " ) .  " +
	    "                            }";
    	try {
	    	Query basicQuery = QueryFactory.create(sampleQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			
			//?freezeThaw ?storageTemp ?storageTempUnit ?cohortLabel ?object ?samplingVolume ?samplingVolumeUnit
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
				if (soln.contains("freezeThaw"))
					values.put("FreezeThaw", soln.get("freezeThaw").toString());
				if (soln.contains("storageTemp"))
					values.put("StorageTemp", replaceURLWithPrefix(soln.get("storageTemp").toString()));
				if (soln.contains("storageTempUnit"))
					values.put("StorageTempUnit", replaceURLWithPrefix(soln.get("storageTempUnit").toString()));
				if (soln.contains("object"))
					values.put("MeasuredObject", replaceURLWithPrefix(soln.get("object").toString()));
				if (soln.contains("samplingVolume"))
					values.put("Volume", replaceURLWithPrefix(soln.get("samplingVolume").toString()));
				if (soln.contains("samplingVolumeUnit"))
					values.put("VolumeUnit", replaceURLWithPrefix(soln.get("samplingVolumeUnit").toString()));
				sampleResult.put(replaceURLWithPrefix(soln.get("sampleUri").toString()),values);	
	//			System.out.println("Samples: " + sampleResult);	
			}
    	} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return sampleResult;
	}
	public static Map<String, Map<String,String>> findSampleBySubject(String subject_uri) {
		Map<String, Map<String,String>> sampleResult = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		String sampleQueryString = "";		
    	sampleQueryString = getPrefixes() +
    	"SELECT ?sampleUri ?subjectUri ?subjectLabel ?sampleType ?sampleLabel ?freezeThaw ?storageTemp ?storageTempUnit ?cohortLabel ?object ?samplingVolume ?samplingVolumeUnit ?comment" +
    	"WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort ." +
    	"       		?sampleUri hasco:isSampleOf ?subjectUri ." +
    	"				?sampleUri rdfs:comment ?comment . " +
    	"				OPTIONAL { ?sampleUri hasco:isObjectOf ?object . " +
    	"				?sampleUri hasco:hasSamplingVolume ?samplingVolume . " +
    	"				?sampleUri hasco:hasSamplingVolumeUnit ?samplingVolumeUnit . " +
    	"				?sampleUri hasco:hasStorageTemperature ?storageTemp . " +
    	"				?sampleUri hasco:hasStorageTemperatureUnit ?storageTempUnit . " +
    	"				?sampleUri hasco:hasNumFreezeThaw ?freezeThaw } . " +
    	"				?sampleUri rdfs:comment ?comment . " +
    	"				?cohort rdfs:label ?cohortLabel . " +
    	"       		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } .  " + 
    	"       		OPTIONAL { ?sampleUri rdfs:label ?sampleLabel } .  " + 
    	"       		OPTIONAL { ?sampleUri a ?sampleType  } .  " +
        "      FILTER (?subjectUri = " + subject_uri + " ) .  " +
	    "                            }";
    	try {
	    	Query basicQuery = QueryFactory.create(sampleQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			
			//?freezeThaw ?storageTemp ?storageTempUnit ?cohortLabel ?object ?samplingVolume ?samplingVolumeUnit
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
				if (soln.contains("freezeThaw"))
					values.put("FreezeThaw", soln.get("freezeThaw").toString());
				if (soln.contains("storageTemp"))
					values.put("StorageTemp", replaceURLWithPrefix(soln.get("storageTemp").toString()));
				if (soln.contains("storageTempUnit"))
					values.put("StorageTempUnit", replaceURLWithPrefix(soln.get("storageTempUnit").toString()));
				if (soln.contains("object"))
					values.put("MeasuredObject", replaceURLWithPrefix(soln.get("object").toString()));
				if (soln.contains("samplingVolume"))
					values.put("Volume", replaceURLWithPrefix(soln.get("samplingVolume").toString()));
				if (soln.contains("samplingVolumeUnit"))
					values.put("VolumeUnit", replaceURLWithPrefix(soln.get("samplingVolumeUnit").toString()));
				sampleResult.put(replaceURLWithPrefix(soln.get("sampleUri").toString()),values);	
	//			System.out.println("Samples: " + sampleResult);	
			}
    	} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return sampleResult;
	}

	public static Map<String, Map<String,String>> findSamples() {
		Map<String, Map<String,String>> sampleResult = new HashMap<String, Map<String,String>>();
		Map<String,String> values = new HashMap<String, String>();
		String sampleQueryString = "";		
    	sampleQueryString = getPrefixes() +
    			"SELECT ?sampleUri ?subjectUri ?subjectLabel ?sampleType ?sampleLabel ?freezeThaw ?storageTemp ?storageTempUnit ?cohortLabel ?object ?samplingVolume ?samplingVolumeUnit ?comment" +
    			"WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort ." +
    			"       		?sampleUri hasco:isSampleOf ?subjectUri ." +
    			"				?sampleUri rdfs:comment ?comment . " +
    			"				OPTIONAL { ?sampleUri hasco:isObjectOf ?object . " +
    			"				?sampleUri hasco:hasSamplingVolume ?samplingVolume . " +
    			"				?sampleUri hasco:hasSamplingVolumeUnit ?samplingVolumeUnit . " +
    			"				?sampleUri hasco:hasStorageTemperature ?storageTemp . " +
    			"				?sampleUri hasco:hasStorageTemperatureUnit ?storageTempUnit . " +
    			"				?sampleUri hasco:hasNumFreezeThaw ?freezeThaw } . " +
    			"				?sampleUri rdfs:comment ?comment . " +
    			"				?cohort rdfs:label ?cohortLabel . " +
    			"       		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } .  " + 
    			"       		OPTIONAL { ?sampleUri rdfs:label ?sampleLabel } .  " + 
    			"       		OPTIONAL { ?sampleUri a ?sampleType  } .  " +
    			"                            }";
    	try {
    		Query basicQuery = QueryFactory.create(sampleQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), basicQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			
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
				if (soln.contains("freezeThaw"))
					values.put("FreezeThaw", soln.get("freezeThaw").toString());
				if (soln.contains("storageTemp"))
					values.put("StorageTemp", replaceURLWithPrefix(soln.get("storageTemp").toString()));
				if (soln.contains("storageTempUnit"))
					values.put("StorageTempUnit", replaceURLWithPrefix(soln.get("storageTempUnit").toString()));
				if (soln.contains("object"))
					values.put("MeasuredObject", replaceURLWithPrefix(soln.get("object").toString()));
				if (soln.contains("samplingVolume"))
					values.put("Volume", replaceURLWithPrefix(soln.get("samplingVolume").toString()));
				if (soln.contains("samplingVolumeUnit"))
					values.put("VolumeUnit", replaceURLWithPrefix(soln.get("samplingVolumeUnit").toString()));
				sampleResult.put(replaceURLWithPrefix(soln.get("sampleUri").toString()),values);
	//			System.out.println("Samples: " + sampleResult);	
			}
    	} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return sampleResult;
	}
	
	
	// for /metadata HTTP GET requests
    public static Result index() {
    	Map<String,String> indicators = getIndicatorTypes();
    	//System.out.println("Indicators: " + indicators + "\n");
    	Map<String,List<String>> valueMap = getIndicatorValues(indicators);
    	//System.out.println("Indicator Values: " + valueMap + "\n");
    	Map<String,Map<String,String>> valueMapWithLabels = getIndicatorValuesAndLabels(indicators);
    	//System.out.println("Indicator Values and Labels: " + valueMapWithLabels + "\n");
    	Map<String,List<String>> valueMapJustLabels = getIndicatorValuesJustLabels(indicators);
    	//System.out.println("Indicator Values Just Labels: " + valueMapJustLabels + "\n");
    	//System.out.println(replaceURLWithPrefix("http://hadatac.org/ont/chear#BloodPlasma"));
    	//System.out.println(findSubject("chear-kb:SBJ-0032-6"));
    	
    	//System.out.println(findStudy("chear-kb:STD-3"));
    	//System.out.println("Studies: " + findStudies());
    	
    	//System.out.println(findSamples());
    	//System.out.println(findSampleBySubject("chear-kb:SBJ-0032-6"));
    	//System.out.println(findSample("chear-kb:SPL-0032-6-Urine-ICP-MS"));
    	
    	//System.out.println(findSubject("chear-kb:SBJ-0032-6"));
    	//System.out.println(findSubjects());
    	//System.out.println(replaceTabNameWithConcept("ReproductiveHealth"));
    	//System.out.println(getPrefixes());
    	
    	/*SubjectGenerator sg = new SubjectGenerator(new File("/home/sabbir/Programs/java_code/pid.sys"));
    	sg.createRows();
    	System.out.println("Subject String: \n" + sg.toString());*/
    	
    	/*SampleGenerator smg = new SampleGenerator(new File("/home/sabbir/Programs/java_code/SID.sys"));
    	smg.createRows();
    	System.out.println("Sample String: \n" + smg.toString());*/
    	
        return ok(); 
    }// /index()

    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return index();
        
    }// /postIndex()

}
