package org.hadatac.console.controllers.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.views.html.metadata.*;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.metadata.loader.*;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONObject;
import org.labkey.remoteapi.query.*;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;

public class DynamicMetadataGeneration extends Controller {
	
	/*public static void renderNavigationHTML(Map<String,String> indicatorMap){
		String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList().replaceAll("\n", " ");
		for(Map.Entry<String, String> entry : indicatorMap.entrySet()){
			String fileName = Play.application().configuration().getString("hadatac.console.host_deploy_location") + "/app/org/hadatac/console/views/metadata/" + entry.getValue().toString().toLowerCase().replaceAll(" ", "_") + "_navigation.scala.html";
			String metadataNavigationString = "@(selection : String)\n\n" +
					"@import org.hadatac.console.controllers._\n\n" +
					"    <div class=\"navbar-collapse collapse navbar-secondary hidden-print\">\n" +
					"        <div class=\"col-md-1.5\">\n" +
					"            <button type=\"button\" class=\"btn btn-link\"><b>" + entry.getValue().toString() + "</b></button>\n" +
					"        </div>\n" ;
		    String indicatorType = entry.getKey().toString();
			Map<String,String> prefixMap = DynamicFunctions.getPrefixMap();
			for (Map.Entry<String, String> prefixes : prefixMap.entrySet()){
				if (indicatorType.contains(prefixes.getValue())){
					indicatorType = indicatorType.replaceAll(prefixes.getValue(), prefixes.getKey() + ":");
				}
			}
		    //String indvIndicatorQuery = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX case: <http://hadatac.org/ont/case#>PREFIX hasco: <http://hadatac.org/ont/hasco/>PREFIX hasneto: <http://hadatac.org/ont/hasneto#>SELECT DISTINCT ?indicator " +
		    String indvIndicatorQuery = prefixString + "SELECT DISTINCT ?indicator " +
					"(MIN(?label_) AS ?label)" +
					"WHERE { ?indicator rdfs:subClassOf " + indicatorType + " . " +
					"?indicator rdfs:label ?label_ . " + 
					"} GROUP BY ?indicator ?label";
			QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
			ResultSet indvIndResults = qexecIndvInd.execSelect();
			ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
			qexecIndvInd.close();
			int resultCount = 0;
			while (resultsrwIndvInd.hasNext()) {
				resultCount += 1;
				QuerySolution soln = resultsrwIndvInd.next();
				int maxLength = 30;
				if (soln.contains("label")){
					int buttonTextLength = soln.get("label").toString().length();
					if (buttonTextLength > maxLength) {
						buttonTextLength = maxLength;
					}
					metadataNavigationString = metadataNavigationString + 
							"        @if(selection == \"" + soln.get("label").toString().replaceAll(" ", "").replaceAll(",", "") + "\") {\n" +
							"            <div class=\"col-md-1\">\n" +
							"                <a href=\"@org.hadatac.console.controllers.metadata.routes.MetadataEntry.index(selection)\" class=\"btn-xs btn-block btn-warning\" role=\"button\">" + soln.get("label").toString().substring(0, buttonTextLength) + "<br>&nbsp;<br>&nbsp;</a>\n" +
							"            </div>\n" +
							"        } else {\n" +
							"            <div class=\"col-md-1\">\n" +
							"                <a href=\"@org.hadatac.console.controllers.metadata.routes.MetadataEntry.index(\"" + soln.get("label").toString().replaceAll(" ", "").replaceAll(",", "") + "\")\" class=\"btn-xs btn-block btn-primary\" role=\"button\">" + soln.get("label").toString().substring(0, buttonTextLength) + "<br>&nbsp;<br>&nbsp;</a>\n" +
							"            </div>\n" + 
							"        }\n" ;
					int multiple = resultCount % 7;
					if (multiple==0){
						metadataNavigationString = metadataNavigationString + "        <br /><br /><br /><br /><br />\n";
					}
				}
				else {
					System.out.println("renderNavigationHTML() No Label: " + soln.toString() + "\n"); 
				}
			}
			metadataNavigationString = metadataNavigationString +
						"    </div>\n";
			//System.out.println(metadataNavigationString);
			try {
				File metadataNavigationPage = new File(fileName);
				FileWriter metadataNavigationPageStream = new FileWriter(metadataNavigationPage,false);
				metadataNavigationPageStream.write(metadataNavigationString);
				metadataNavigationPageStream.close();
				System.out.println("Wrote " + fileName + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	*/
	/*
	public static void renderMetadataHTML(Map<String,String> indicatorMap){
		String metadataBrowserString = "@()\n\n" +
				"@import helper._\n" + 
				"@import org.hadatac.console.views.html._\n\n" + 
				"@main(\"Browse Metadata\") {\n" +
				"    <div class=\"container-fluid\">\n" +
				"        <h2>Browse Metadata</h2>\n" ;
		for(Map.Entry<String, String> entry : indicatorMap.entrySet()){
			metadataBrowserString = metadataBrowserString + "        <div class=\"row\">\n            @" +
				entry.getValue().toString().toLowerCase().replaceAll(" ", "_") + 
				"_navigation(\"\")\n        </div>\n        <hr>\n\n";
		}
		metadataBrowserString = metadataBrowserString + "        <br><br><br>\n        @mainButton(false)\n    </div>\n}";
		//System.out.println(metadataBrowserString);
		try {
			File metadataBrowserPage = new File(Play.application().configuration().getString("hadatac.console.host_deploy_location") + "/app/org/hadatac/console/views/metadata/metadata.scala.html");
			FileWriter metadataBrowserPageStream = new FileWriter(metadataBrowserPage,false);
			metadataBrowserPageStream.write(metadataBrowserString);
			metadataBrowserPageStream.close();
			System.out.println("Wrote metadata.scala.html\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
/*	public static void renderMetadataEntryHTML(){
		String metadataEntry = "@(triple : org.hadatac.console.models.OtMTripleDocument)\n\n" +
				"@*****************************\n" +
				"  hasURI/label    | id (array[0])\n" +
				"*****************************@\n\n" +
				"@defining(triple.get(\"id\").get(0)) {itemID =>\n" +
				"    @defining(itemID.replaceAll(\"[\\\\W]|_\", \"\")) {cleanName =>\n" +
				"    <div class=\"panel-heading\" role=\"tab\" id=\"heading@itemID\">\n" +
				"        <h4 class=\"panel-title\">\n" +
				"            <a class=\"collapsed\" role=\"button\" data-toggle=\"collapse\" data-parent=\"#accordion\" href=\"#@cleanName\" aria-expanded=\"false\" aria-controls=\"@cleanName\">\n" +
				"                @itemID</a></h4>\n" +
				"	 </div>\n" +
				"    <div id=\"@cleanName\" class=\"panel-collapse collapse\" role=\"tabpanel\" aria-labelledby=\"heading@cleanName\">\n" +
				"        <div class=\"panel-body\">\n" +
				"            <h3>@itemID</h3>\n" +
				"            @defining(triple.get(\"label\").get(0)) {theuri =>\n" +
				"            <form>\n" +
				"                <input type=\"text\" size=\"80\" value=\"@theuri\">\n" +
				"            </form>\n" +
				"            }\n" +
				"        </div>\n" +
				"        <h4>ID:</h4>\n" +
				"            @for(c <- triple.get(\"iden\")) {\n" +
				"                <p>@c</p>\n" +
				"            }\n" +
				"        <h4>Comment:</h4>\n" +
				"            @for(c <- triple.get(\"comment\")) {\n" +
				"                <p>@c</p>\n" +
				"            }\n" +
				"            @for(n <- triple.get(\"note\")) {\n" +
				"                <p>@n</p>\n" +
				"            }\n" +
				"        <h4>Note:</h4>\n" +
				"            @for(c <- triple.get(\"note\")) {\n" +
				"                <p>@c</p>\n" +
				"            }\n" +
				"    </div>\n" +
				"    }\n" +
				"}" ;
		//System.out.println(metadataEntry);
		try {
			File metadataEntryPage = new File(Play.application().configuration().getString("hadatac.console.host_deploy_location") + "/app/org/hadatac/console/views/metadata/metadata_entry.scala.html");
			FileWriter metadataEntryPageStream = new FileWriter(metadataEntryPage,false);
			metadataEntryPageStream.write(metadataEntry);
			metadataEntryPageStream.close();
			System.out.println("Wrote metadata_entry.scala.html\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}*/
	
	public static Map<String, String> getIndicatorTypes(){
		String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList().replaceAll("\n", " ");
		//String indicatorQuery="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#>SELECT DISTINCT ?indicatorType ?label ?comment WHERE { ?indicatorType rdfs:subClassOf hasco:Indicator . ?indicatorType rdfs:label ?label . }";
		String indicatorQuery=prefixString + "SELECT DISTINCT ?indicatorType ?label ?comment WHERE { ?indicatorType rdfs:subClassOf hasco:Indicator . ?indicatorType rdfs:label ?label . }";
		QueryExecution qexecInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indicatorQuery);
		ResultSet indicatorResults = qexecInd.execSelect();
		ResultSetRewindable resultsrwIndc = ResultSetFactory.copyResults(indicatorResults);
		qexecInd.close();
		
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		while (resultsrwIndc.hasNext()) {
			QuerySolution soln = resultsrwIndc.next();
			indicatorLabel = soln.get("label").toString();
			indicatorMap.put(soln.get("indicatorType").toString(),indicatorLabel);		
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		//System.out.println("Indicator Types: " + indicatorMapSorted);
		return indicatorMapSorted;
	}
	
	public static Map<String, String> getIndicatorValues(Map<String, String> indicatorMap){
		String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList().replaceAll("\n", " ");
		Map<String, String> indicatorValueMap = new HashMap<String, String>();
		String indicatorValueLabel = "";
		for(Map.Entry<String, String> entry : indicatorMap.entrySet()){
		    //System.out.println("Key : " + entry.getKey() + " and Value: " + entry.getValue() + "\n");
		    String indicatorType = entry.getKey().toString();
			Map<String,String> prefixMap = DynamicFunctions.getPrefixMap();
			for (Map.Entry<String, String> prefixes : prefixMap.entrySet()){
				if (indicatorType.contains(prefixes.getValue())){
					indicatorType = indicatorType.replaceAll(prefixes.getValue(), prefixes.getKey() + ":");
				}
			}
		    //System.out.println("Indicator Type: " + indicatorType + "\n");
		    //String indvIndicatorQuery = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX case: <http://hadatac.org/ont/case#>PREFIX hasco: <http://hadatac.org/ont/hasco/>PREFIX hasneto: <http://hadatac.org/ont/hasneto#>SELECT DISTINCT ?indicator " +
		    String indvIndicatorQuery = prefixString + "SELECT DISTINCT ?indicator " +
					"(MIN(?label_) AS ?label)" +
					"WHERE { ?indicator rdfs:subClassOf " + indicatorType + " . " +
					"?indicator rdfs:label ?label_ . " + 
					"} GROUP BY ?indicator ?label";
			//System.out.println(indvIndicatorQuery + "\n");
			QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
			ResultSet indvIndResults = qexecIndvInd.execSelect();
			ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
			qexecIndvInd.close();
			while (resultsrwIndvInd.hasNext()) {
				QuerySolution soln = resultsrwIndvInd.next();
				if (soln.contains("label")){
					indicatorValueLabel = soln.get("label").toString();
					String indicatorUrl = soln.get("indicator").toString();
					for (Map.Entry<String, String> prefixes : prefixMap.entrySet()){
						if (indicatorUrl.contains(prefixes.getValue())){
							indicatorUrl = indicatorUrl.replaceAll(prefixes.getValue(), prefixes.getKey() + ":");
						}
					}
					indicatorValueMap.put(indicatorUrl,indicatorValueLabel);
				}
				else {
					System.out.println("getIndicatorValues() No Label: " + soln.toString() + "\n");
				}
			}
		}
		return indicatorValueMap;
	}
	/*
	public static void renderMetadataBrowserHTML(Map<String, String> indicatorMap){
		String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList().replaceAll("\n", " ");
		Map<String,String> prefixMap = DynamicFunctions.getPrefixMap();
		String metadataBrowserHTMLString="@( results : org.hadatac.console.models.OtMSparqlQueryResults, category : String)\n\n" +
				"@*****************************\n" +
				"    public TreeMap<String,OtMTripleDocument> results.sparqlResults\n;" +
				"    public TreeMap<String,ArrayList<String>> results.sparqlResults.item;\n" +
				"    public String results.treeResults;\n" +
				"    public String json;\n" +
				"*****************************@\n\n" +
				"@import helper._\n" +
				"@import org.hadatac.console.views.html._\n\n" +
				"@main(\"Hierarchies\") {\n" ;
		for(Map.Entry<String, String> entry : indicatorMap.entrySet()){
		    //System.out.println("Key : " + entry.getKey() + " and Value: " + entry.getValue() + "\n");
			String indicatorType = entry.getKey().toString();
			for (Map.Entry<String, String> prefixes : prefixMap.entrySet()){
				if (indicatorType.contains(prefixes.getValue())){
					indicatorType = indicatorType.replaceAll(prefixes.getValue(), prefixes.getKey() + ":");
				}
			}
		    //System.out.println("Indicator Type: " + indicatorType + "\n");
		    //String indvIndicatorQuery = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX case: <http://hadatac.org/ont/case#>PREFIX hasco: <http://hadatac.org/ont/hasco/>PREFIX hasneto: <http://hadatac.org/ont/hasneto#>SELECT DISTINCT ?indicator " +
		    String indvIndicatorQuery = prefixString + "SELECT DISTINCT ?indicator " +
					"(MIN(?label_) AS ?label)" +
					"WHERE { ?indicator rdfs:subClassOf " + indicatorType + " . " +
					"?indicator rdfs:label ?label_ . " + 
					"} GROUP BY ?indicator ?label";
			//System.out.println(indvIndicatorQuery + "\n");
			QueryExecution qexecIndvInd = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), indvIndicatorQuery);
			ResultSet indvIndResults = qexecIndvInd.execSelect();
			ResultSetRewindable resultsrwIndvInd = ResultSetFactory.copyResults(indvIndResults);
			qexecIndvInd.close();
			metadataBrowserHTMLString = metadataBrowserHTMLString + "    @if(";
			while (resultsrwIndvInd.hasNext()) {
				QuerySolution soln = resultsrwIndvInd.next();
				if (soln.contains("label")){
					metadataBrowserHTMLString = metadataBrowserHTMLString + "(category == \"" + soln.get("label").toString().replaceAll(" ", "").replaceAll(",", "") + "\")";
				}
				else {
					System.out.println("renderMetadataBrowserHTML() No Label: " + soln.toString() + "\n");
					metadataBrowserHTMLString = metadataBrowserHTMLString + "(category == \"\")";
				}
				if (resultsrwIndvInd.hasNext()){
					metadataBrowserHTMLString = metadataBrowserHTMLString + " || ";
				} else {
					metadataBrowserHTMLString = metadataBrowserHTMLString + "){\n";
				}
			}
			metadataBrowserHTMLString = metadataBrowserHTMLString + "        @" + entry.getValue().toString().toLowerCase().replaceAll(" ", "_") + "_navigation(category)\n    }\n";
		}
		metadataBrowserHTMLString = metadataBrowserHTMLString +
				"    <div class=\"container-fluid container-spaced\">\n" +
				"      <div class=\"row\">\n" +
				"        <div class=\"col-sm-9 col-md-9 main\">\n" +
				"            <ul class=\"nav nav-tabs hidden-print\">\n" +
				"                @if(results.treeResults != \"\"){\n" +
				"                    <li class=\"active\" onclick=\"info_on('info')\"><a data-toggle=\"tab\" href=\"#tree\">\n" +
				"                        <span class=\"glyphicon glyphicon-tree-conifer\" aria-hidden=\"true\"></span> View as Hierarchy</a></li>\n"+
				"                    <li onclick=\"info_off('info')\"><a data-toggle=\"tab\" href=\"#table\">\n"+
				"                        <span class=\"glyphicon glyphicon-th-list\" aria-hidden=\"true\"></span> View as Table</a></li>\n"+
				"                } else {\n"+
				"                    <li class=\"active\"><a data-toggle=\"tab\" href=\"#table\">\n"+
				"                        <span class=\"glyphicon glyphicon-th-list\" aria-hidden=\"true\"></span> View as Table</a></li>\n"+
				"                }\n"+
				"            </ul>\n"+
				"            <div class = \"tab-content\">\n"+
				"            <div id=\"table\" class=\"hidden-print tab-pane fade in\">\n"+
				"                <div><p>(click for more information)</p></div>\n"+
				"                    <div class=\"panel-group\" id=\"accordion\" role=\"tablist\" aria-multiselectable=\"true\">\n" +
				"                        <div class=\"panel panel-default\">\n" +
				"                        @for((k, triple) <- results.sparqlResults){\n" +
				"        	            	@metadata_entry(triple)\n" +
				"            	        }\n" +
				"                        </div>\n" +
				"                    </div>\n" +
				"            </div>\n\n" +
				"            @if(results.treeResults != \"\"){\n" +
				"                <div id=\"tree\" class=\"hidden-print tab-pane fade in active\">\n" +
				"                    <div id=\"query\" data-results=\"@results.treeResults\"></div>\n" +
				"                    <div id=\"body\"></div>\n" +
				"                        <script type=\"text/javascript\" src=\"/hadatac/assets/javascripts/d3.js\"></script>\n" +
				"                        <script type=\"text/javascript\" src=\"/hadatac/assets/javascripts/d3.layout.js\"></script>\n" +
				"                        <script type=\"text/javascript\" src=\"/hadatac/assets/javascripts/treeRenderer.js\"></script>\n" +
				"                        <script type=\"text/javascript\">\n" +
				"                        	d3.selectAll(\".node\")\n" +
				"                                .on('mouseover', function (d,i){\n" +
				"                                    var item = d.name.replace(/\\W/g, '');\n" +
				"                                    var stuff = document.getElementById(item).innerHTML;\n" +
				"                                    var infobox = document.getElementById('info');\n" +
				"                                    infobox.innerHTML = stuff;\n" +
				"                                });\n" +
				"			            </script>\n" +
				"                    <link type=\"text/css\" rel=\"stylesheet\" href=\"/hadatac/assets/stylesheets/treeRenderer.css\"/>\n" +
				"                </div>\n" +
				"            }\n" +
				"          </div>\n" +
				"    </div>\n" +
				"    <div class=\"col-md-3 col-sm-3 hidden-print\">\n" +
				"        <div class=\"tab-spacer\"></div>\n" +
				"        <div id=\"info\" class=\"infobox\" style=\"display:block\"></div>\n" +
				"    </div>\n" +
				"    <script type=\"text/javascript\" src=\"/hadatac/assets/javascripts/extra.js\"></script> \n\n" +
				"    <div class=\"visible-print-block\">\n" +
				"        <table>\n" +
				"        	@for((k, triple) <- results.sparqlResults){\n" +
				"            	@print_metadata_entry(triple)\n" +
				"            }\n" +
				"        </table>\n" +
				"    </div>\n" +
				"}";
		//System.out.println(metadataBrowserHTMLString);
		try {
			File metadataBrowserHTMLPage = new File(Play.application().configuration().getString("hadatac.console.host_deploy_location") + "/app/org/hadatac/console/views/metadata/metadata_browser.scala.html");
			FileWriter metadataBrowserHTMLPageStream = new FileWriter(metadataBrowserHTMLPage,false);
			metadataBrowserHTMLPageStream.write(metadataBrowserHTMLString);
			metadataBrowserHTMLPageStream.close();
			System.out.println("Wrote metadata_browser.scala.html\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
	public static void renderSPARQLPage(){
		String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList().replaceAll("\n", " ");
		Map<String,String> prefixMap = DynamicFunctions.getPrefixMap();
//		String prefixString="PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>";
		String importString="import java.io.IOException;\n" +
				"import java.io.ByteArrayOutputStream;\n" + 
				"import java.io.StringWriter;\n" +
				"import java.io.UnsupportedEncodingException;\n" +
				"import java.net.URLEncoder;\n" +
				"import java.util.Scanner;\n" +
				"import java.util.TreeMap;\n" +
				"import org.hadatac.console.models.SparqlQuery;\n" +
				"import org.hadatac.utils.Collections;\n" +
				"import org.hadatac.utils.NameSpaces;\n" +
				"import org.apache.commons.io.IOUtils;\n" +
				"import org.apache.http.HttpResponse;\n" +
				"import org.apache.http.client.HttpClient;\n" +
				"import org.apache.http.client.methods.HttpGet;\n" +
				"import org.apache.http.impl.client.CloseableHttpClient;\n" +
				"import org.apache.http.impl.client.DefaultHttpClient;\n" +
				"import org.apache.http.impl.client.HttpClients;\n" +
				"import org.apache.jena.query.Query;\n" +
				"import org.apache.jena.query.QueryExecution;\n" +
				"import org.apache.jena.query.QueryExecutionFactory;\n" +
				"import org.apache.jena.query.QueryFactory\n;" +
				"import org.apache.jena.query.ResultSet;\n" +
				"import org.apache.jena.query.ResultSetFormatter;\n" +
				"import play.Play;\n";
		
		Map<String, String> indicatorMapSorted = getIndicatorTypes();
		Map<String, String> indicatorValueMap = getIndicatorValues(indicatorMapSorted);

		String getSPARQLClassString = "public class GetSparqlQueryDynamic { \n" +
				"    public String collection;\n" +
				"    public GetSparqlQueryDynamic () {}\n\n" +
				"    public GetSparqlQueryDynamic (SparqlQuery query) {\n" +
				"    	this(Collections.METADATA_SPARQL, query);\n" +
				"    }\n\n" +
				"    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query) {\n" +
				"        this.collection = Collections.getCollectionsName(collectionSource);\n" +
				"        System.out.println(\"Collection: \" + collection);\n" +
				"    }\n\n" +
				"    public GetSparqlQueryDynamic (SparqlQuery query, String tabName) {\n" +
				"    	this(Collections.METADATA_SPARQL, query, tabName);\n" +
				"    }\n\n" +
				"    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query, String tabName) {\n" +
				"    	this.collection = Collections.getCollectionsName(collectionSource);\n" +
				"        System.out.println(\"Collection: \" + collection);\n" +
				"    }\n\n" +
/*				"    public StringBuffer sparql_query = new StringBuffer();\n" +
				"    public TreeMap<String, StringBuffer> list_of_queries = new TreeMap<String, StringBuffer>();\n" +
				"    public String collection;\n" +
				"    private int numThings = " + indicatorValueMap.size() + ";\n" +
				"    public String[] thingTypes = new String[numThings];\n\n" +
				"    public GetSparqlQueryDynamic () {} \n\n" + 
				"    public GetSparqlQueryDynamic (SparqlQuery query) {\n" +
			    "        this(Collections.METADATA_SPARQL, query);\n" +
			    "    }\n\n" + 
			    "    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query) {\n" +
			    "        addThingTypes();\n" +
			    "        this.collection = Collections.getCollectionsName(collectionSource);\n" +
			    "        System.out.println(\"Collection: \" + collection);\n\n" +
			    "        for (String tabName : thingTypes ){\n" +
			    "            this.sparql_query = new StringBuffer();\n" +
			    "            this.sparql_query.append(collection);\n" +
			    "            this.sparql_query.append(\"?q=\");\n" +
			    "            String q = querySelector(tabName);\n\n" +
			    "            @SuppressWarnings(\"unused\")\n" +
			    "            String quote = new String();\n" +
			    "            try {\n" +
			    "                this.sparql_query.append(URLEncoder.encode(q, \"UTF-8\"));\n" +
			    "                quote = URLEncoder.encode(\"\\\"\", \"UTF-8\");\n" +
			    "            } catch (UnsupportedEncodingException e) {\n" +
			    "                e.printStackTrace();\n" +
			    "            }\n\n" +
			    "            this.list_of_queries.put(tabName, this.sparql_query);\n" +
			    "        }\n" +
			    "    }\n\n" + 
			    "    public GetSparqlQueryDynamic (SparqlQuery query, String tabName) {\n" +
			    "        this(Collections.METADATA_SPARQL, query, tabName);\n" +
			    "    }\n\n" +
			    "    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query, String tabName) {\n" +
				"        this.collection = Collections.getCollectionsName(collectionSource);\n" +
			    "        System.out.println(\"Collection: \" + collection);\n" +
			    "        this.sparql_query = new StringBuffer();\n" +
			    "        this.sparql_query.append(collection);\n" +
			    "        this.sparql_query.append(\"?q=\");\n" +
			    "        String q = querySelector(tabName);\n\n" +
			    "        @SuppressWarnings(\"unused\")\n" +
			    "        String quote = new String();\n" +
			    "        try {\n" +
			    "            this.sparql_query.append(URLEncoder.encode(q, \"UTF-8\"));\n" +
			    "            quote = URLEncoder.encode(\"\\\"\", \"UTF-8\");\n" +
			    "        } catch (UnsupportedEncodingException e) {\n" +
			    "            e.printStackTrace();\n" +
			    "        }\n\n" +
			    "        this.list_of_queries.put(tabName, this.sparql_query);\n" +
			    "    }\n\n" +
			    "    public void addThingTypes(){\n" ;
		Map<String, String> indicatorValueMapSorted = new TreeMap<String, String>(indicatorValueMap);
		List<String> allIndicatorLabels = new ArrayList<String>(indicatorValueMapSorted.values());
		for (int i=0;i<indicatorValueMapSorted.size();i++){
			getSPARQLClassString = getSPARQLClassString + "        thingTypes[" + i + "]  = \"" + allIndicatorLabels.get(i).replaceAll(" ", "").replaceAll(",", "") + "\";\n" ;
		}
		getSPARQLClassString = getSPARQLClassString + "    }\n\n" + */
				"    public String querySelector(String tabName){\n" +
				"        String q = \"SELECT * WHERE { ?s ?p ?o } LIMIT 10\";\n" +
				"        switch (tabName){\n" ;
		
		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
			//String indicatorValue = entry.getValue().toString().replaceAll(" ", "").replaceAll(",", "");
			String indicatorType = entry.getKey().toString();
			for (Map.Entry<String, String> prefixes : prefixMap.entrySet()){
				if (indicatorType.contains(prefixes.getValue())){
					indicatorType = indicatorType.replaceAll(prefixes.getValue(), prefixes.getKey() + ":");
				}
			}
			//String indicatorType = entry.getKey().toString().replaceAll("http://hadatac.org/ont/chear#","chear:").replaceAll("http://hadatac.org/ont/case#","case:").replaceAll("http://hadatac.org/kb/chear#","chear-kb:");
			//System.out.println("Value: " + indicatorValue);
			//System.out.println("Type: " + indicatorType);
			//String indvIndicatorQuery = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX case: <http://hadatac.org/ont/case#>PREFIX hasco: <http://hadatac.org/ont/hasco/>PREFIX hasneto: <http://hadatac.org/ont/hasneto#>SELECT DISTINCT ?indicator " +
			String indvIndicatorQuery = prefixString + "SELECT DISTINCT ?indicator " +
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
				if (soln.contains("label")) {
					String indicatorString=soln.get("indicator").toString();
					for (Map.Entry<String, String> prefixes : prefixMap.entrySet()){
						if (indicatorString.contains(prefixes.getValue())){
							indicatorString = indicatorString.replaceAll(prefixes.getValue(), prefixes.getKey() + ":");
						}
					}
					getSPARQLClassString = getSPARQLClassString + "            case \"" + soln.get("label").toString().replaceAll(" ", "").replaceAll(",", "") + "\":\n" +
						"               q= \"" + prefixString + "\" + \n" +
						"                   \"SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith \" + \n" +
						"                   \"WHERE { \" + \n" +
						"                   \"  ?id rdfs:subClassOf* " + indicatorString + " . \" + \n" +
						"                   \"  ?id rdfs:subClassOf ?superId . \" + \n" +
						"                   \"  ?id rdfs:label ?label .\" + \n" +
						"                   \"  OPTIONAL {?id dcterms:identifier ?iden} . \" + \n" +
						"                   \"  OPTIONAL {?id rdfs:comment ?comment} . \" + \n" +
						"                   \"  OPTIONAL {?id skos:definition ?def} . \" + \n" +
						"                   \"  OPTIONAL {?id hasco:hasUnit ?unit} . \" + \n" +
						"                   \"  OPTIONAL {?id skos:editorialNote ?note} . \" + \n" +
						"                   \"  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . \" + \n" +
						"                   \"  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . \" + \n" +
						"                   \"} \";\n " +
						"               break;\n" ;
				}
				else {
					System.out.println("renderSPARQLPage() No Label: " + soln.toString() + "\n");
				}
		    }
		}
		
		getSPARQLClassString = getSPARQLClassString + "            default :\n" +
				"                q = \"\";\n" +
				"                System.out.println(\"WARNING: no query for tab \" + tabName);\n" +
				"        }\n" + 
				"    return q; \n" +
				"    }\n\n" +
/*				"    public String executeQuery(String tab) throws IllegalStateException, IOException{\n" +
				"        try {\n" +
				"            HttpClient client = new DefaultHttpClient();\n" +
				"            HttpGet request = new HttpGet(list_of_queries.get(tab).toString().replace(\" \", \"%20\"));\n" +
				"            System.out.println(tab + \" : \" + list_of_queries.get(tab));\n" +
				"            request.setHeader(\"Accept\", \"application/sparql-results+json\");\n" +
				"            HttpResponse response = client.execute(request);\n" +
				"            StringWriter writer = new StringWriter();\n" +
				"            IOUtils.copy(response.getEntity().getContent(), writer, \"utf-8\");\n" +
				"            System.out.println(\"Response: \" + response);\n" + 
				"            return writer.toString();\n" + 
				"        } finally {}\n" + 
				"    }\n" + */
				"    public String executeQuery(String tab) throws IllegalStateException, IOException{\n" +
				"        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();\n" +
				"        try {\n" +
				"            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + querySelector(tab);\n" +
				"            Query query = QueryFactory.create(queryString);\n" +
				"            QueryExecution qexec = QueryExecutionFactory.sparqlService(collection, query);\n" +
				"            ResultSet results = qexec.execSelect();\n" +
				"            ResultSetFormatter.outputAsJSON(outputStream, results);\n" +
				"            qexec.close();\n" +
				"            return outputStream.toString(\"UTF-8\");\n" +
				"        } catch (Exception e) {\n" +
				"			 e.printStackTrace();\n" + 
				"    	 }\n" + 
				"	return \"\";\n" +
				"   }\n" +
				"}" ;
		String getSPARQLJavaString="package org.hadatac.console.http;\n"				+ 
				"//This Java Class was Dynamically Generated\n" + 
				 importString + getSPARQLClassString;
		//System.out.println(getSPARQLJavaString);// Generate facet view html.scala file
		try {
			File getSPARQLPage = new File(Play.application().configuration().getString("hadatac.console.host_deploy_location") + "/app/org/hadatac/console/http/GetSparqlQueryDynamic.java");
			FileWriter getSPARQLPageStream = new FileWriter(getSPARQLPage,false);
			getSPARQLPageStream.write(getSPARQLJavaString);
			getSPARQLPageStream.close();
			System.out.println("Wrote GetSparqlQueryDynamic.java\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	// for /metadata HTTP GET requests
    public static Result index() {
    	Map<String,String> indicatorMap = getIndicatorTypes();
    	//renderSPARQLPage();
		//renderNavigationHTML(indicatorMap);
		//renderMetadataHTML(indicatorMap);
    	//renderMetadataBrowserHTML(indicatorMap);

    	return ok(dynamicMetadataPage.render());
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return index();
        
    }// /postIndex()

}
