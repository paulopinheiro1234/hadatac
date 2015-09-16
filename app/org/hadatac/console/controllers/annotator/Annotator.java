package org.hadatac.console.controllers.annotator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.GetSparqlQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.TripleDocument;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import org.hadatac.console.views.html.error_page;
import org.hadatac.console.views.html.annotator.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Annotator extends Controller {

	// for /metadata HTTP GET requests
    public static Result selectDeployment() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        String tabName = "Deployments";
        String query_json = null;
        //System.out.println("DeploymentManagement is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            if (query_json != null && !query_json.equals("")) {
                theResults = new SparqlQueryResults(query_json, false);
            } else {
            	theResults = null;
            }
        } catch (IllegalStateException | IOException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
       return ok(selectDeployment.render(theResults));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postSelectDeployment() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        String tabName = "Deployments";
        String query_json = null;
        //System.out.println("DeploymentManagement is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            if (query_json != null && !query_json.equals("")) {
                theResults = new SparqlQueryResults(query_json, false);
            } else {
            	theResults = null;
            }
        } catch (IllegalStateException | IOException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        return ok(selectDeployment.render(theResults));
        
    }// /postIndex()

    public static Result uploadCSV(String uri) {

    	CSVAnnotationHandler handler;
    	try {
    		if (uri != null) {
			    uri = URLDecoder.decode(uri, "UTF-8");
    		} else {
    			uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	//System.out.println("uploadCSV: uri is " + uri);
    	if (!uri.equals("")) {
    		String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, uri);
    		SparqlQueryResults results = new SparqlQueryResults(json, false);
    		TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
    		handler = new CSVAnnotationHandler(uri, docDeployment.get("platform"), docDeployment.get("instrument"));
    		
    		String dep_json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_CHARACTERISTICS_BY_URI, uri);
    		SparqlQueryResults results2 = new SparqlQueryResults(dep_json, false);
    		Iterator<TripleDocument> it = results2.sparqlResults.values().iterator();
    		Map<String,String> deploymentChars = new HashMap<String,String>();
    		TripleDocument docChar;
    		while (it.hasNext()) {
    			docChar = (TripleDocument) it.next();
    			if (docChar != null && docChar.get("ec") != null && docChar.get("ecName") != null) {
    				deploymentChars.put((String)docChar.get("ec"),(String)docChar.get("ecName"));
    				System.out.println("EC: " + docChar.get("ec") + "   ecName: " + docChar.get("ecName"));
    			}
    		}
    		handler.setDeploymentCharacteristics(deploymentChars);
    		System.out.println("uploadCSV: dep_json is " + dep_json);
    		//System.out.println("uploadCSV: ec_json is " + ec_json);
    	} else 
    	{
    		handler = new CSVAnnotationHandler(uri, "", "");
    	}

    	return ok(uploadCSV.render(handler, "init",""));
        
    }// /postIndex()

    public static Result postUploadCSV(String uri) {

		CSVAnnotationHandler handler;
    	try {
    		if (uri != null) {
			    uri = URLDecoder.decode(uri, "UTF-8");
    		} else {
    			uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	//System.out.println("uploadCSV: uri is " + uri);
    	if (!uri.equals("")) {
    		String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, uri);
    		SparqlQueryResults results = new SparqlQueryResults(json, false);
    		TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
    		handler = new CSVAnnotationHandler(uri, docDeployment.get("platform"), docDeployment.get("instrument"));
    		
    		String dep_json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_CHARACTERISTICS_BY_URI, uri);
    		SparqlQueryResults results2 = new SparqlQueryResults(dep_json, false);
    		Iterator<TripleDocument> it = results2.sparqlResults.values().iterator();
    		Map<String,String> deploymentChars = new HashMap<String,String>();
    		TripleDocument docChar;
    		while (it.hasNext()) {
    			docChar = (TripleDocument) it.next();
    			if (docChar != null && docChar.get("ec") != null && docChar.get("ecName") != null) {
    				deploymentChars.put((String)docChar.get("ec"),(String)docChar.get("ecName"));
    				System.out.println("EC: " + docChar.get("ec") + "   ecName: " + docChar.get("ecName"));
    			}
    		}
    		handler.setDeploymentCharacteristics(deploymentChars);
    		System.out.println("uploadCSV: dep_json is " + dep_json);
    		//System.out.println("uploadCSV: ec_json is " + ec_json);
    	} else 
    	{
    		handler = new CSVAnnotationHandler(uri, "", "");
    	}

    	return ok(uploadCSV.render(handler, "init",""));
        
    }// /postIndex()
    
}