package org.hadatac.console.controllers.metadata;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
		
		String indicatorQuery="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#>SELECT ?studyIndicator ?label ?comment WHERE { ?studyIndicator rdfs:subClassOf chear:StudyIndicator . ?studyIndicator rdfs:label ?label . ?studyIndicator rdfs:comment ?comment . }";
		QueryExecution qexec0 = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
		ResultSet indicatorResults = qexec0.execSelect();
		ResultSetRewindable resultsrw0 = ResultSetFactory.copyResults(indicatorResults);
		qexec0.close();
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		while (resultsrw0.hasNext()) {
			QuerySolution soln = resultsrw0.next();
	//		System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			indicatorLabel = soln.get("label").toString();
//			indicatorValues.add("Comment: " + soln.get("comment").toString());
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
							"<schema version=\"1.5\">\n" + 
							"  <fields>\n" + 
						    "    <field name=\"studyUri\" type=\"string\" indexed=\"true\" stored=\"true\"/>\n" +
						    "    <field name=\"studyLabel\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n" +
						    "    <field name=\"proj\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n" +
						    "    <field name=\"studyTitle\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n" +
						    "    <field name=\"studyComment\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n" +
						    "    <field name=\"agentName\" type=\"string\" indexed=\"true\" docValues=\"true\" multiValued=\"true\"  />\n" +
						    "    <field name=\"institutionName\" type=\"string\" indexed=\"true\" docValues=\"true\" />\n";

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
		String selectString="SELECT ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment ?agentName ?institutionName ";
		String whereString=" 	WHERE { ?subUri rdfs:subClassOf hasco:Study . " +
		                      "?studyUri a ?subUri .  " +
		                      "?studyUri rdfs:label ?studyLabel . " +
		                      "OPTIONAL { ?studyUri chear-kb:project ?proj . " +
		                      " ?studyUri skos:definition ?studyTitle . " +
		                      " ?studyUri rdfs:comment ?studyComment . " +
		                      " ?studyUri hasco:hasAgent ?agent .  " +
		                      " ?agent foaf:name ?agentName . " +
		                      " ?studyUri hasco:hasInstitution ?institution . " +
                              " ?institution foaf:name ?institutionName } . " +
						 	  "?schemaUri hasco:isSchemaOf ?studyUri . " +
                			  "?schemaAttribute hasneto:partOfSchema ?schemaUri . ";
		String groupByString="GROUP BY ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment ?agentName ?institutionName ";
		/*
		SelectRowsResponse response;
		
		try {
			Connection cn = new Connection("http://chear.tw.rpi.edu/labkey", "rashidsabbir@gmail.com", "");
			SelectRowsCommand cmd = new SelectRowsCommand("lists", "LocalStudyIndicatorType");
			cmd.setRequiredVersion(9.1);
			cmd.setColumns(Arrays.asList("hasURI", "rdfs:subClassOf", "rdfs:label", "rdfs:comment", "prov:wasGeneratedBy", "skos:editorialNote", "hasco:needsApproval", "hasco:approvedBy"));
			response = cmd.execute(cn, "/CHEAR Production");
			System.out.println("Number of rows: " + response.getRowCount());

			for (Map<String, Object> row : response.getRows())
			{
				System.out.println(row);
				//indicatorValues = new ArrayList<String>();
				JSONObject uri = (JSONObject)row.get("hasURI");
				JSONObject superClass = (JSONObject)row.get("rdfs:subClassOf");
				//indicatorValues.add(superClass.get("value").toString());
				JSONObject label = (JSONObject)row.get("rdfs:label");
				//indicatorValues.add(label.get("value").toString());
				facetPageString=facetPageString + "        {'field': '" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") + "Label', 'display': '" + label.get("value").toString() + "'},\n";
				facetSearchSortString=facetSearchSortString + ",{'display':'" + label.get("value").toString() + "','field':'" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") + "Label.exact'}" ;
				//schemaString=schemaString + "    <field name=\"" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") + "\" type=\"string\" indexed=\"true\" docValues=\"true\" multiValued=\"true\"  />\n" ;
				schemaString=schemaString + "    <field name=\"" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") + "Label\" type=\"string\" indexed=\"true\" docValues=\"true\" multiValued=\"true\"  />\n" ;
				//selectString = selectString + "?" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") + " ";
				selectString = selectString + "?" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") + "Label ";
				
				groupByString = groupByString + "?" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") + "Label ";
				whereString = whereString + "OPTIONAL { ?schemaAttribute hasneto:hasAttribute ?" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") +
						" . ?" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:subClassOf* " + uri.get("value").toString() + 
						" . ?" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:label ?" + label.get("value").toString().replaceAll(" ", "").replaceAll(",", "") +"Label } . ";
				JSONObject comment = (JSONObject)row.get("rdfs:comment");
				//indicatorValues.add(comment.get("value").toString());
				//indicatorMap.put(uri.get("value").toString(),indicatorValues);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
		    System.out.println("Key : " + entry.getKey() + " and Value: " + entry.getValue());
		    facetPageString=facetPageString + "        {'field': '" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + "Label', 'display': '" + entry.getValue().toString() + "'},\n";
			facetSearchSortString=facetSearchSortString + ",{'display':'" + entry.getValue().toString() + "','field':'" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + "Label.exact'}" ;
			schemaString=schemaString + "    <field name=\"" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + "Label\" type=\"string\" indexed=\"true\" docValues=\"true\" multiValued=\"true\"  />\n" ;
			selectString = selectString + "?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + "Label ";
			
			groupByString = groupByString + "?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + "Label ";
			whereString = whereString + "OPTIONAL { ?schemaAttribute hasneto:hasAttribute ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") +
					" . ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:subClassOf* " + entry.getKey().toString().replaceAll("http://hadatac.org/ont/chear#","chear:") + 
					" . ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") + " rdfs:label ?" + entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "") +"Label } . ";
			
		}
		
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
				"            \"field\" : \"studyTitle\",\n" +
				"            \"post\" : \" - \"\n" +
				"          },\n" +
				"          {\n" +
				"            \"pre\" : \"(\",\n" +
				"            \"field\" : \"studyLabel\",\n" +
				"            \"post\" : \")</h3>\"\n" +
/*
//				"            \"pre\" : \"<h3><a href=\\\"./metadataacquisitions/viewStudy?study_uri=\\\"\",\n" +
				"            \"pre\" : \"<h3>\",\n" +
				"            \"field\" : \"studyUri\",\n" +
//				"            \"post\" : \">\"\n" +
				"            \"post\" : \" - \"\n" +
				"          },\n" +
				"          {\n" +
				"            \"pre\" : \"\"\n" +
				"            \"field\" : \"studyTitle\",\n" +
//				"            \"post\" : \"</a> - \"\n" +
				"            \"post\" : \" - \"\n" +
				"          },\n" +
				"          {\n" +
				"            \"pre\" : \"(\",\n" +
				"            \"field\" : \"studyLabel\",\n" +
				"            \"post\" : \")</h3>\"\n" +
*/
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
				"            \"pre\" : \"<p>\",\n" + 
				"            \"field\" : \"studyComment\",\n" +
				"            \"post\" : \"</p>\"\n" +
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
		
		System.out.println(facetPageString);
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
		System.out.println(schemaString);
		
		String studyQueryString = prefixString + selectString + whereString + " } " + groupByString;;
		System.out.println(studyQueryString);
		
		Query studyQuery = QueryFactory.create(studyQueryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), studyQuery);
//		Model results = qexec.execConstruct();
//		ResultSetRewindable resultsrw = ResultSetFactory.makeRewindable(results);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		Map<String, List<String>> studyResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
//		Map<String, String> poResult = new HashMap<String, String>();
	//	System.out.println("HERE IS THE RAW resultsrw*********" + resultsrw);
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
	//		System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values.add("Label: " + soln.get("studyLabel").toString());
			values.add("Title: " + soln.get("studyTitle").toString());
			values.add("Project: " + soln.get("proj").toString());
			values.add("Comment: " + soln.get("studyComment").toString());
			values.add("Agent(s): " + soln.get("agentName").toString());
			values.add("Institution: " + soln.get("institutionName").toString());
			studyResult.put(soln.get("studyUri").toString(),values);
			
		}
		return studyResult;
//		return indicatorMap;
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
