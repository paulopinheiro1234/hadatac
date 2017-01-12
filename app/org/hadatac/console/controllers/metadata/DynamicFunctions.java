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
	
	public static Map<String, String> getIndicatorTypes(){
		String prefixString = getPrefixes();
		String indicatorQuery=prefixString + "SELECT DISTINCT ?indicatorType ?label ?comment WHERE { ?indicatorType rdfs:subClassOf chear:Indicator . ?indicatorType rdfs:label ?label . }";
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
		System.out.println("Indicator Types: " + indicatorMapSorted);
		return indicatorMapSorted;
	}
		
	// for /metadata HTTP GET requests
    public static Result index() {
    	Map<String,String> indicators = getIndicatorTypes();
    	//System.out.println(getPrefixes());
    	Map<String,String> prefixMap = getPrefixMap();
    	System.out.println(prefixMap);
        return ok();        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {
        
        return index();
        
    }// /postIndex()

}
