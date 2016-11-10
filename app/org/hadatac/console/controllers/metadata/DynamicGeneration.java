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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.hadatac.console.views.html.metadata.*;
import org.hadatac.metadata.loader.*;
import org.hadatac.utils.Collections;
import org.json.simple.JSONObject;
import org.labkey.remoteapi.query.*;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;

public class DynamicGeneration extends Controller {
	
//	public static Map<String, String> findBasic(String study_uri) {
	public static Map<String, List<String>> generateStudy() {
		String prefixString="PREFIX sio: <http://semanticscience.org/resource/> " + 
				"PREFIX chear: <http://hadatac.org/ont/chear#> " +
				"PREFIX chear-kb: <http://hadatac.org/kb/chear#> " +
				"PREFIX prov: <http://www.w3.org/ns/prov#> " +
				"PREFIX hasco: <http://hadatac.org/ont/hasco/> " +
				"PREFIX hasneto: <http://hadatac.org/ont/hasneto#> " +
				"PREFIX dcterms: <http://purl.org/dc/terms/> " +
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";
		
		String initStudyQuery="SELECT DISTINCT ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment " +
				"(group_concat( ?agentName_ ; separator = ' & ') as ?agentName) " +
				"?institutionName  " +
				"WHERE {        ?subUri rdfs:subClassOf hasco:Study .  " +
				"		                      ?studyUri a ?subUri .  " +
				"	           ?studyUri rdfs:label ?studyLabel  .   " +
				"					 	?studyUri chear-kb:project ?proj . " +
				"                                  ?studyUri skos:definition ?studyTitle . " +
				"                                   ?studyUri rdfs:comment ?studyComment . " +
				"                                  ?studyUri hasco:hasAgent ?agent .  " +
				"                                 	?agent foaf:name ?agentName_ . " +
				"                                  ?studyUri hasco:hasInstitution ?institution . " +
//				"                                 ?institution foaf:name ?institutionName} . } " +
				"                                 ?institution foaf:name ?institutionName . } " +
				"GROUP BY ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment ?agentName ?institutionName ";
		
		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), prefixString + initStudyQuery);
		ResultSet initStudyResults = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(initStudyResults);
		qexecStudy.close();
		String initStudyJson="{\n\"commit\": {}";
		Map<String, List<String>> initStudyMap = new HashMap<String, List<String>>();
		List<String> initStudyValues = new ArrayList<String>();
		
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
					
			initStudyValues= new ArrayList<String>();
			initStudyValues.add("Label: " + soln.get("studyLabel").toString());
			initStudyValues.add("Title: " + soln.get("studyTitle").toString());
			initStudyValues.add("Project: " + soln.get("proj").toString());
			initStudyValues.add("Comment: " + soln.get("studyComment").toString());
			initStudyValues.add("Agent(s): " + soln.get("agentName").toString());
			initStudyValues.add("Institution: " + soln.get("institutionName").toString());
			initStudyMap.put(soln.get("studyUri").toString(),initStudyValues);
			
			initStudyJson=initStudyJson + ",\n\"add\":\n\t{\n\t\"doc\":\n\t\t{\n";
			initStudyJson=initStudyJson + "\t\t\"studyUri\": \"" + soln.get("studyUri").toString() + "\" ,\n";
			initStudyJson=initStudyJson + "\t\t\"studyLabel\": \"<a href=\\\"./metadataacquisitions/viewStudy?study_uri=" + soln.get("studyUri").toString().replaceAll("http://hadatac.org/ont/chear#","chear:").replaceAll("http://hadatac.org/kb/chear#","chear-kb:") + "\\\">" + soln.get("studyLabel").toString() + "</a>\" ,\n";
			//initStudyJson=initStudyJson + "\t\t\"studyTitle\": \"<a href=\\\"./metadataacquisitions/viewStudy?study_uri=" + soln.get("studyUri").toString().replaceAll("http://hadatac.org/ont/chear#","chear:").replaceAll("http://hadatac.org/kb/chear#","chear-kb:") + "\\\">" + soln.get("studyTitle").toString() + "</a>\" ,\n";
			initStudyJson=initStudyJson + "\t\t\"studyTitle\": \"" + soln.get("studyTitle").toString() + "\" ,\n";
			initStudyJson=initStudyJson + "\t\t\"proj\": \"" + soln.get("proj").toString() + "\" ,\n";
			initStudyJson=initStudyJson + "\t\t\"studyComment\": \"" + soln.get("studyComment").toString() + "\" ,\n";
			initStudyJson=initStudyJson + "\t\t\"agentName\": \"" + soln.get("agentName").toString() + "\" ,\n";
			initStudyJson=initStudyJson + "\t\t\"institutionName\": \"" + soln.get("institutionName").toString() + "\"\n";
			initStudyJson=initStudyJson + "\t\t}\n\t}";
		}
		initStudyJson=initStudyJson + "\n}" ;
		System.out.println(initStudyJson);
		
		String indicatorQuery="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#>SELECT ?studyIndicator ?label ?comment WHERE { ?studyIndicator rdfs:subClassOf chear:StudyIndicator . ?studyIndicator rdfs:label ?label . ?studyIndicator rdfs:comment ?comment . }";
		QueryExecution qexecInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
		ResultSet indicatorResults = qexecInd.execSelect();
		ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
		qexecInd.close();
		
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		while (resultsrwIndc.hasNext()) {
			QuerySolution soln = resultsrwIndc.next();
			indicatorLabel = soln.get("label").toString();
			indicatorMap.put(soln.get("studyIndicator").toString(),indicatorLabel);		
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		
		String facetPageString="@(collection_url : java.lang.String)\n\n" +
							   "@import helper._\n" +
							   "@import org.hadatac.console.views.html._\n\n" +
							   "@main(\"Study Browser Home\") {\n" +
							   "	<script type=\"text/javascript\" src=\"@controllers.routes.Assets.versioned(\"javascripts/vendor/jquery/1.7.1/jquery-1.7.1.min.js\")\"></script>\n" +
							   "	<link rel=\"stylesheet\" type=\"text/css\" href=\"@controllers.routes.Assets.versioned(\"javascripts/vendor/bootstrap/css/bootstrap.min.css\")\"/>\n" +
							   "	<script type=\"text/javascript\" src=\"@controllers.routes.Assets.versioned(\"javascripts/vendor/bootstrap/js/bootstrap.min.js\")\"></script>\n" +
							   "	<script type=\"text/javascript\" src=\"@controllers.routes.Assets.versioned(\"javascripts/vendor/linkify/1.0/jquery.linkify-1.0-min.js\")\"></script>\n" +
							   "	<link rel=\"stylesheet\" href=\"@controllers.routes.Assets.versioned(\"javascripts/vendor/jquery-ui-1.8.18.custom/jquery-ui-1.8.18.custom.css\")\"/>\n" +
							   "	<script type=\"text/javascript\" src=\"@controllers.routes.Assets.versioned(\"javascripts/vendor/jquery-ui-1.8.18.custom/jquery-ui-1.8.18.custom.min.js\")\"></script>\n" +
							   "	<script type=\"text/javascript\" src=\"@controllers.routes.Assets.versioned(\"javascripts/jquery.facetview.js\")\"></script>\n" +
							   "	<link rel=\"stylesheet\" type=\"text/css\" href=\"@controllers.routes.Assets.versioned(\"css/facetview.css\")\"/>\n" +
							   " 	<link rel=\"stylesheet\" type=\"text/css\" href=\"@controllers.routes.Assets.versioned(\"css/style.css\")\"/>\n" +
							   "	<link rel=\"stylesheet\" type=\"text/css\" href=\"@controllers.routes.Assets.versioned(\"javascripts/vendor/c3-0.4.10/c3.css\")\"/>\n" +
							   "	<script type=\"text/javascript\" src=\"@controllers.routes.Assets.versioned(\"javascripts/vendor/d3-3.5.6/d3.min.js\")\"></script>\n" +
							   "	<script type=\"text/javascript\" src=\"@controllers.routes.Assets.versioned(\"javascripts/vendor/c3-0.4.10/c3.min.js\")\"></script>\n" +
							   "	<script type=\"text/javascript\">\n" +
							   "jQuery(document).ready(function($) {\n" +
							   "  $('.facet-view-simple').each(function() {\n" +
							   "  $(this).facetview({\n" +
							   "    search_url: '@collection_url',\n" +
							   "    search_index: 'solr',\n" +
							   "    datatype: 'json',\n" +
							   "    facets: [ \n" ;
		String facetSearchSortString="[{'display':'Study URI', 'field':'studyUri.exact'}";
		String schemaString="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
							"<schema version=\"1.5\"><!-- This schema was Dynamically Generated -->\n" + 
							"  <fields>\n" + 
						    "    <field name=\"studyUri\" type=\"string\" indexed=\"true\" stored=\"true\"/>\n" +
						    "    <field name=\"studyLabel\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n" +
						    "    <field name=\"proj\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n" +
						    "    <field name=\"studyTitle\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n" +
						    "    <field name=\"studyComment\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n" +
						    "    <field name=\"agentName\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n" +
						    "    <field name=\"institutionName\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n";

		String updateIndicatorJson="{\n\"commit\": {}";
		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
		    //System.out.println("Key : " + entry.getKey() + " and Value: " + entry.getValue() + "\n");
		    String label = entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "").toString() + "Label";
		    facetPageString=facetPageString + "        {'field': '" + label + "', 'display': '" + entry.getValue().toString() + "'},\n";
			facetSearchSortString=facetSearchSortString + ",{'display':'" + entry.getValue().toString() + "','field':'" + label + ".exact'}" ;
			schemaString=schemaString + "    <field name=\"" + label + "\" type=\"string\" indexed=\"true\" docValues=\"true\" multiValued=\"true\"  />\n" ;

			String indvIndicatorQuery = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#>PREFIX hasco: <http://hadatac.org/ont/hasco/>PREFIX hasneto: <http://hadatac.org/ont/hasneto#>SELECT DISTINCT ?studyUri " +
					"?" + label + " " +
					"WHERE { ?schemaUri hasco:isSchemaOf ?studyUri . ?schemaAttribute hasneto:partOfSchema ?schemaUri . ?schemaAttribute hasneto:hasAttribute " +
					"?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") +
					" . ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:subClassOf* " + entry.getKey().toString().replaceAll("http://hadatac.org/ont/chear#","chear:").replaceAll("http://hadatac.org/kb/chear#","chear-kb:") + 
					" . ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:label ?" + label + " . " +
					"}";
//			System.out.println(indvIndicatorQuery + "\n");
			QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
			ResultSet indvIndResults = qexecIndvInd.execSelect();
			ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
			qexecIndvInd.close();
			String indvIndicatorJson="";
			while (resultsrwIndvInd.hasNext()) {
				QuerySolution soln = resultsrwIndvInd.next();
				System.out.println("Solution: " + soln);
				indvIndicatorJson="";
				indvIndicatorJson=indvIndicatorJson + ",\n\"add\":\n\t{ \"doc\":\n\t\t{\n";
				indvIndicatorJson=indvIndicatorJson + "\t\t\"studyUri\": \"" + soln.get("studyUri").toString() + "\" ,\n";
				indvIndicatorJson=indvIndicatorJson + "\t\t\"" + label + "\":\n\t\t\t{ \"add\": \n\t\t\t\t[ " ;
				indvIndicatorJson=indvIndicatorJson + "\""+ soln.get(label).toString() +"\"";
				indvIndicatorJson=indvIndicatorJson + " ]\n\t\t\t}\n\t\t}\n\t}";
				System.out.println(indvIndicatorJson);
				updateIndicatorJson=updateIndicatorJson + indvIndicatorJson;
			}
		}
		updateIndicatorJson = updateIndicatorJson + "\n}";
		System.out.println(updateIndicatorJson + "\n");
		
		facetPageString =facetPageString + "    ],\n    search_sortby: " + facetSearchSortString + "],\n    searchbox_fieldselect: "+ facetSearchSortString + "],\n" +
				"    paging: {\n" + 
				"      size: 10\n" +
				"    },\n" +
				"    predefined_filters: {\n" +
				"        //'owner.exact': {'term':{'owner.exact':'test'}}\n" + 
				"    },\n" +
				"    result_display : [\n" +
				"        [\n" +
				"          {\n" + 
				"            \"pre\" : \"<h3>\",\n" +
				"            \"field\" : \"studyLabel\",\n" +
				"            \"post\" : \" - \"\n" +
				"          },\n" +
				"          {\n" +
				"            \"pre\" : \"(\",\n" +
				"            \"field\" : \"studyTitle\",\n" +
				"            \"post\" : \")</h3>\"\n" +
				"          }\n" +
				"        ],\n" +
				"        [\n" +
				"          {\n" +
				"            \"pre\" : \"<b>Institution:</b> \",\n" +
				"            \"field\" : \"institutionName\",\n" +
				"            \"post\" : \", \"\n" +
				"          }\n" +
				"        ],\n" +
				"        [\n" +
				"          {\n" +
				"            \"pre\" : \"<b>Principal Investigator(s):</b> \",\n" +
				"            \"field\" : \"agentName\",\n" +
				"            \"post\" : \" \"\n" +
				"          }\n" +
				"        ],\n" +
				"        [\n" +
				"          {\n" + 
				"            \"pre\" : \"\\n\\n\",\n" + 
				"            \"field\" : \"studyComment\",\n" +
				"            \"post\" : \"\"\n" +
				"          }\n" +
				"        ]\n" +
				"    ],\n" +
				"    default_operator: \"AND\",\n" +
				"    default_freetext_fuzzify: \"*\"\n" + 
				"    });\n" +
				"  });\n" +
				"});\n" +
				"    </script>\n" +
				"	<hr>  \n" + 
				"    <div class=\"container-fluid\">\n" + 
				"    <div class=\"page-header\">\n" + 
				"      <h2>\n" + 
				"      Study Faceted Search\n" + 
				"      </h2>\n" + 
				"   </div>\n" + 
				"   <div class=\"content\">\n" + 
				"	 <div class=\"facet-view-simple\"></div>\n" +
				"   </div> \n" +
				"   <div id=\"download_url\" style=\"visibility: hidden ;\">@org.hadatac.console.http.routes.SolrSearchProxy.getStudyAcquisitionDownload()</div>\n" + 
				"   <!-- Trigger the modal with a button -->\n" + 
				"   <button type=\"button\" class=\"btn btn-info btn-lg\" data-toggle=\"modal\" data-target=\"#myModal\">Download</button>\n" + 
				"   <!-- Modal -->\n" +
				"	<div id=\"myModal\" class=\"modal fade\" role=\"dialog\">\n" + 
				"	  <div class=\"modal-dialog\">\n" +
				"	    <!-- Modal content-->\n" +
				"	    <div class=\"modal-content\">\n" + 
				"	      <div class=\"modal-header\">\n" +
				"	        <button type=\"button\" class=\"close\" data-dismiss=\"modal\">&times;</button>\n" + 
				"	        <h4 class=\"modal-title\">Download</h4>\n" +
				"	      </div>\n" +
				"	      <div class=\"modal-body\">\n" + 
				"	        <p>Format:</p>\n" +
				"	        <select class=\"form-control\" id=\"sel_format\">\n" + 
				"              <option>CSV</option>\n" +
				"              <option>JSON</option>\n" +
				"            </select>\n" +
				"	        <a href=\"\" class=\"btn btn-info\" role=\"button\" id=\"btn_download\">Confirm</a>\n" + 
				"	      </div>\n" +
				"	      <div class=\"modal-footer\">\n" + 
				"	        <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>\n" + 
				"	      </div>\n" +
				"	    </div>\n" +
				"	  </div>\n" +
				"	</div>\n" +
				"}";
		
		schemaString = schemaString + "    <field name=\"_version_\" type=\"long\" indexed=\"true\" stored=\"true\"/>\n" +
									  "    <field name=\"_text_\" type=\"text_general\" indexed=\"true\" stored=\"false\" multiValued=\"true\"/>\n" +
									  "    <copyField source=\"studyUri\" dest=\"_text_\" />\n" +
									  "  </fields>\n" +
									  "  <uniqueKey>studyUri</uniqueKey>\n" +
									  "  <types>\n" +
									  "    <fieldType name=\"string\" class=\"solr.StrField\" />\n" +
									  "    <fieldType name=\"date\" class=\"solr.TrieDateField\" precisionStep=\"0\" positionIncrementGap=\"0\"/>\n" +
									  "    <fieldType name=\"long\" class=\"solr.TrieLongField\" precisionStep=\"0\" positionIncrementGap=\"0\"/>\n" +
									  "    <fieldType name=\"double\" class=\"solr.TrieDoubleField\"/>\n" +
									  "    <fieldType name=\"int\" class=\"solr.TrieIntField\" precisionStep=\"8\"/>\n" +
									  "    <fieldType name=\"latlong\" class=\"solr.SpatialRecursivePrefixTreeFieldType\"\n" +
									  "              spatialContextFactory=\"com.spatial4j.core.context.jts.JtsSpatialContextFactory\"\n" +
									  "              distErrPct=\"0.025\"\n" +
									  "              maxDistErr=\"0.000009\"\n" +
									  "              units=\"degrees\" />\n" +
									  "    <fieldType name=\"text_general\" class=\"solr.TextField\" positionIncrementGap=\"100\" multiValued=\"true\">\n" +
									  "      <analyzer type=\"index\">\n" +
									  "        <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n" +
									  "        <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"stopwords.txt\" />\n" +
									  "        <filter class=\"solr.LowerCaseFilterFactory\"/> \n" +
									  "      </analyzer> \n" +
									  "      <analyzer type=\"query\">\n" +
									  "        <tokenizer class=\"solr.StandardTokenizerFactory\"/>\n" +
									  "        <filter class=\"solr.StopFilterFactory\" ignoreCase=\"true\" words=\"stopwords.txt\" />\n" +
									  "        <filter class=\"solr.LowerCaseFilterFactory\"/>\n" +
									  "      </analyzer>\n" +
									  "    </fieldType>\n" +
									  "  </types>\n" +
									  "</schema> " ;
		System.out.println(facetPageString);
		System.out.println(schemaString);
		try {
			File facetPage = new File("./app/org/hadatac/console/views/metadataacquisition/metadataacquisition.scala.html");
			FileWriter facetPageStream = new FileWriter(facetPage,false);
			facetPageStream.write(facetPageString);
			facetPageStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			File schemaXML = new File(Play.application().configuration().getString("hadatac.solr.home") + "/solr-home/studies_facet/conf/schema.xml");
			FileWriter schemaXMLStream = new FileWriter(schemaXML,false);
			schemaXMLStream.write(schemaString);
			schemaXMLStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			ProcessBuilder p=new ProcessBuilder("curl","http://localhost:8983/solr/studies/update?commit=true", "-H","Content-type:application/json",
	                "--data-binary",initStudyJson );
			final Process shell = p.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			ProcessBuilder p=new ProcessBuilder("curl","http://localhost:8983/solr/studies/update?commit=true", "-H","Content-type:application/json",
	                "--data-binary",updateIndicatorJson );
			final Process shell = p.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return initStudyMap;
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
	//		System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values = new ArrayList<String>();
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
    	
		Map<String, List<String>> studyResult = generateStudy();
		Map<String, List<String>> subjectResult = findSubject();
        
        return ok(dynamicPage.render(studyResult,subjectResult));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return index();
        
    }// /postIndex()

}
