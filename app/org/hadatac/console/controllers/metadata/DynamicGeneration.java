package org.hadatac.console.controllers.metadata;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.jena.rdf.model.Model;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.utils.Collections;


public class DynamicGeneration extends Controller {
	
//	public static Map<String, String> findBasic(String study_uri) {
	public static Map<String, List<String>> findStudy() {
		String studyQueryString = "";

		studyQueryString = 
		"PREFIX sio: <http://semanticscience.org/resource/>" + 
		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
		"PREFIX chear-kb: <http://hadatac.org/kb/chear#>" + 
		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + 
	/*	
"SELECT DISTINCT ?studyUri ?studyLabel ?studyTitle ?proj ?studyComment " +
"(group_concat(distinct   ?agentName_ ; separator = ' & ') as ?agentName) " +
"?institutionName " +
"(group_concat(distinct ?demographicLabel_ ; separator = ' & ') as ?demographicLabel)" +
"(group_concat(distinct ?acculturationLabel_ ; separator = ' & ') as ?acculturationLabel)" +
"(group_concat(distinct ?birthOutcomeLabel_ ; separator = ' & ') as ?birthOutcomeLabel)" +
"(group_concat(distinct ?anthropometryLabel_ ; separator = ' & ') as ?anthropometryLabel)" +
"(group_concat(distinct ?assessmentLabel_ ; separator = ' & ') as ?assessmentLabel)" +
"(group_concat(distinct ?ATIDULabel_ ; separator = ' & ') as ?ATIDULabel)" +
"(group_concat(distinct ?dietLabel_ ; separator = ' & ') as ?dietLabel)" +
"(group_concat(distinct ?envExposureLabel_ ; separator = ' & ') as ?envExposureLabel)" +
"(group_concat(distinct ?housingCharLabel_ ; separator = ' & ') as ?housingCharLabel)" +
"(group_concat(distinct ?medHistLabel_ ; separator = ' & ') as ?medHistLabel)" +
"(group_concat(distinct ?mentalHealthLabel_ ; separator = ' & ') as ?mentalHealthLabel)" +
"(group_concat(distinct ?PPULabel_ ; separator = ' & ') as ?PPULabel)" +
"(group_concat(distinct ?pregnancyCharLabel_ ; separator = ' & ') as ?pregnancyCharLabel)" +
"WHERE {        ?subUri rdfs:subClassOf hasco:Study . " +
"		                      ?studyUri a ?subUri . " +
"	           ?studyUri rdfs:label ?studyLabel  .  " +
"					 	OPTIONAL { ?studyUri chear-kb:project ?proj } . " +
"					 	OPTIONAL { ?studyUri skos:definition ?studyTitle } . " +
"					 	OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " +
"					 	OPTIONAL { ?studyUri hasco:hasAgent ?agent . " +
"                                 	?agent foaf:name ?agentName_ } . " +
"					 	OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " +
"                                 ?institution foaf:name ?institutionName} . " +
"       OPTIONAL {?schemaUri_ hasco:isSchemaOf ?studyUri ." +
"                 ?schemaAttribute_ hasneto:partOfSchema ?schemaUri_ } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?demographic_ . " +
"                                 ?demographic_ rdfs:subClassOf* chear:Demographic . " +
"                			?demographic_ rdfs:label ?demographicLabel_ " +
"                } ." +
"      OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?acculturation_ . " +
"                                 ?acculturation_ rdfs:subClassOf* chear:Acculturation . " +
"                			?acculturation_ rdfs:label ?acculturationLabel_ " +
"                } . " +
"      OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?birthOutcome_ .  " +
"                                 ?birthOutcome_ rdfs:subClassOf* chear:BirthOutcome . " +
"                			?birthOutcome_ rdfs:label ?birthOutcomeLabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?anthropometry_ .  " +
"                                 ?anthropometry_ rdfs:subClassOf* chear:Anthropometry . " +
"                			?anthropometry_ rdfs:label ?anthropometryLabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?assessment_ .  " +
"                                 ?assessment_ rdfs:subClassOf* chear:Assessment . " +
"                			?assessment_ rdfs:label ?assessmentLabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?ATIDU_ .  " +
"                                 ?ATIDU_ rdfs:subClassOf* chear:ATIDU . " +
"                			?ATIDU_ rdfs:label ?ATIDULabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?diet_ .  " +
"                                 ?diet_ rdfs:subClassOf* chear:DietAndNutrition . " +
"                			?diet_ rdfs:label ?dietLabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?envExposure_ .  " +
"                                 ?envExposure_ rdfs:subClassOf* chear:EnvironmentalExposure . " +
"                			?envExposure_ rdfs:label ?envExposureLabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?housingChar_ .  " +
"                                 ?housingChar_ rdfs:subClassOf* chear:HousingCharacteristic . " +
"                			?housingChar_ rdfs:label ?housingCharLabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?medHist_ .  " +
"                                 ?medHist_ rdfs:subClassOf* chear:MedicalHistory . " +
"                			?medHist_ rdfs:label ?medHistLabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?mentalHealth_ .  " +
"                                 ?mentalHealth_ rdfs:subClassOf* chear:MentalHealth . " +
"                			?mentalHealth_ rdfs:label ?mentalHealthLabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?PPU_ .  " +
"                                 ?PPU_ rdfs:subClassOf* chear:PersonalProductUse . " +
"                			?PPU_ rdfs:label ?PPULabel_ " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?pregnancyChar_ .  " +
"                                 ?pregnancyChar_ rdfs:subClassOf* chear:PregnancyCharacteristic . " +
"                			?pregnancyChar_ rdfs:label ?pregnancyCharLabel_ " +
"                } .       " +
"      } " +
"GROUP BY ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment ?agentName ?institutionName ?schemaUri ?schemaAttributeLabel ?schemaLabel ?schemaAttribute ?demographic ?demographicLabel  ?acculturationLabel ?pregnancyCharLabel ?PPULabel ?mentalHealthLabel ?medHistLabel ?housingCharLabel ?envExposureLabel ?dietLabel ?ATIDULabel ?assessmentLabel ?anthropometryLabel ?birthOutcomeLabel ";
	*/		

"SELECT ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment  ?agentName " +
"?institutionName ?demographic ?acculturation ?birthOutcome ?anthropometry ?assessment " + 
"?ATIDU ?diet ?envExposure ?housingChar ?medHist ?mentalHealth ?PPU ?pregnancyChar " + 
"WHERE {        ?subUri rdfs:subClassOf hasco:Study .  " +
"		                      ?studyUri a ?subUri .  " +
"	           ?studyUri rdfs:label ?studyLabel  .   " +
"					 	OPTIONAL { ?studyUri chear-kb:project ?proj } .  " +
"					 	OPTIONAL { ?studyUri skos:definition ?studyTitle } .  " +
"					 	OPTIONAL { ?studyUri rdfs:comment ?studyComment } .  " +
"					 	OPTIONAL { ?studyUri hasco:hasAgent ?agent .  " +
"                                 	?agent foaf:name ?agentName } .  " +
"					 	OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " +
"                                 ?institution foaf:name ?institutionName} . " +
"       OPTIONAL {?schemaUri_ hasco:isSchemaOf ?studyUri . " +
"                 ?schemaAttribute_ hasneto:partOfSchema ?schemaUri_ } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?demographic_ .  " +
"                                 ?demographic_ rdfs:subClassOf* chear:Demographic  " +
"                } . " +
"      OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?acculturation .  " +
"                                 ?acculturation rdfs:subClassOf* chear:Acculturation  " +
"                } . " +
"      OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?birthOutcome .  " +
"                                 ?birthOutcome rdfs:subClassOf* chear:BirthOutcome " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?anthropometry .  " +
"                                 ?anthropometry rdfs:subClassOf* chear:Anthropometry  " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?assessment .  " +
"                                 ?assessment rdfs:subClassOf* chear:Assessment  " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?ATIDU .  " +
"                                 ?ATIDU rdfs:subClassOf* chear:ATIDU  " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?diet .  " +
"                                 ?diet rdfs:subClassOf* chear:DietAndNutrition  " +
"                }  . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?envExposure .  " +
"                                 ?envExposure rdfs:subClassOf* chear:EnvironmentalExposure  " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?housingChar .  " +
"                                 ?housingChar rdfs:subClassOf* chear:HousingCharacteristic  " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?medHist . " + 
"                                 ?medHist rdfs:subClassOf* chear:MedicalHistory " + 
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?mentalHealth . " + 
"                                 ?mentalHealth rdfs:subClassOf* chear:MentalHealth  " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?PPU .  " +
"                                 ?PPU rdfs:subClassOf* chear:PersonalProductUse  " +
"                } . " +
"       OPTIONAL { " +
"         		?schemaAttribute_ hasneto:hasAttribute ?pregnancyChar .  " +
"                                 ?pregnancyChar rdfs:subClassOf* chear:PregnancyCharacteristic  " +
"                } . " +
"      } " ;		
		
//		"SELECT ?studyUri ?studyLabel ?proj ?studyDef ?studyComment ?agentName ?institutionName " + 
//		" WHERE {        ?subUri rdfs:subClassOf hasco:Study . " +
//		//"CONSTRUCT {        ?subUri rdfs:subClassOf hasco:Study . " +
//		"                       ?studyUri a ?subUri . " + 
//		"           ?studyUri rdfs:label ?studyLabel  .  " +
//		//"WHERE {        OPTIONAL { ?studyUri chear-kb:project ?proj } . " + 
//		"        OPTIONAL { ?studyUri chear-kb:project ?proj } . " + 
//		"        OPTIONAL { ?studyUri skos:definition ?studyTitle } . " + 
//		"        OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " + 
//		"        OPTIONAL { ?studyUri hasco:hasAgent ?agent . " + 
//		"                                   ?agent foaf:name ?agentName } . " + 
//		"        OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " + 
//		"                                 ?institution foaf:name ?institutionName} . " + 
//		"                             }" ;
		System.out.println(studyQueryString);
		Query studyQuery = QueryFactory.create(studyQueryString);
		
//		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), studyQuery);
//		Model results = qexec.execConstruct();
//		ResultSetRewindable resultsrw = ResultSetFactory.makeRewindable(results);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), studyQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		Map<String, List<String>> studyResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
//		Map<String, String> poResult = new HashMap<String, String>();
		System.out.println("HERE IS THE RAW resultsrw*********" + resultsrw);
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values.add("Label: " + soln.get("studyLabel").toString());
			values.add("Title: " + soln.get("studyTitle").toString());
			values.add("Project: " + soln.get("proj").toString());
			values.add("Comment: " + soln.get("studyComment").toString());
			values.add("Agent(s): " + soln.get("agentName").toString());
			values.add("Institution: " + soln.get("institutionName").toString());
			studyResult.put(soln.get("studyUri").toString(),values);
			
		}
		return studyResult;
	}
	
	
	public static Map<String, List<String>> findSubject() {

		String subjectQueryString = "";
		
    	subjectQueryString = 
    	"PREFIX sio: <http://semanticscience.org/resource/>" + 
    	"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
    	"PREFIX chear-kb: <http://hadatac.org/kb/chear#>" + 
    	"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
    	"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
    	"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
    	"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
    	"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
    	"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
    	"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" + 
    	"SELECT ?subjectUri ?subjectType ?subjectLabel ?cohort ?study " +
    	"			 WHERE {        ?subjectUri hasco:isSubjectOf* ?cohort . " +
    	"			        		?cohort hasco:isCohortOf ?study . " +
    	"			        		OPTIONAL { ?subjectUri rdfs:label ?subjectLabel } . " +
    	"			        		OPTIONAL { ?subjectUri a ?subjectType } . " +
    	"			                             }";		
        
		Query subjectQuery = QueryFactory.create(subjectQueryString);
		
		QueryExecution qexec2 = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectQuery);
		ResultSet results = qexec2.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec2.close();
		Map<String, List<String>> subjectResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values.add("Label: " + soln.get("subjectLabel").toString());
			values.add("Type: " + soln.get("subjectType").toString());
			values.add("Cohort: " + soln.get("cohort").toString());
			values.add("Study: " + soln.get("study").toString());
			subjectResult.put(soln.get("subjectUri").toString(),values);
			
		}
		
		return subjectResult;
	}
	
	// for /metadata HTTP GET requests
    public static Result index() {
    	
		Map<String, List<String>> studyResult = findStudy();
		Map<String, List<String>> subjectResult = findSubject();
        
        return ok(viewStudy.render(studyResult,subjectResult));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return index();
        
    }// /postIndex()

}
