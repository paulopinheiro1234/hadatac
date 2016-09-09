package org.hadatac.console.controllers.annotator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.LoadKB;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.GetSparqlQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.models.User;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.RequestBody;
import play.mvc.Http.MultipartFormData.FilePart;

import org.hadatac.console.views.html.error_page;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class Annotator extends Controller {

	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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
    	System.out.println("uploadCSV: uri is " + uri);
    	if (!uri.equals("")) {

    		/*
    		 *  Add deployment information into handler
    		 */
    		String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, uri);
    		//System.out.println(json);
    		SparqlQueryResults results = new SparqlQueryResults(json, false);
    		TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
    		handler = new CSVAnnotationHandler(uri, docDeployment.get("platform"), docDeployment.get("instrument"));
    		    		
    		/*
    		 * Add possible detector's characterisitcs into handler
    		 */
    		String dep_json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_CHARACTERISTICS_BY_URI, uri);
    		System.out.println(dep_json);
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

    		/*
    		 * Add URI of active datacollection in handler
    		 */
    		DataCollection dc = DataFactory.getActiveDataCollection(uri);
    		if (dc != null && dc.getUri() != null) {
    			handler.setDataCollectionUri(dc.getUri());
    		}
    		
    		//System.out.println("uploadCSV: dep_json is " + dep_json);
    		//System.out.println("uploadCSV: ec_json is " + ec_json);
    	} else 
    	{
    		handler = new CSVAnnotationHandler(uri, "", "");
    	}

    	return ok(uploadCSV.render(handler, "init",""));
        
    }// /postIndex()

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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

    		/*
    		 *  Add deployment information into handler
    		 */
    		String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, uri);
    		SparqlQueryResults results = new SparqlQueryResults(json, false);
    		TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
    		handler = new CSVAnnotationHandler(uri, docDeployment.get("platform"), docDeployment.get("instrument"));
    		
    		/*
    		 * Add possible detector's characterisitcs into handler
    		 */
    		String dep_json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_CHARACTERISTICS_BY_URI, uri);
    		System.out.println(dep_json);
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

    		/*
    		 * Add URI of active datacollection in handler
    		 */
    		DataCollection dc = DataFactory.getActiveDataCollection(uri);
    		if (dc != null && dc.getUri() != null) {
    			handler.setDataCollectionUri(dc.getUri());
    		}
    		
    		//System.out.println("uploadCSV: dep_json is " + dep_json);
    		//System.out.println("uploadCSV: ec_json is " + ec_json);
    	} else 
    	{
    		handler = new CSVAnnotationHandler(uri, "", "");
    	}

    	return ok(uploadCSV.render(handler, "init",""));
        
    }// /postIndex()
	
//	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
//    public static Result toggleAutoAnnotator() {
//		Properties prop = new Properties();
//		try {
//			InputStream is = LoadKB.class.getClassLoader().getResourceAsStream("autoccsv.config");
//			prop.load(is);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		String folder_path = prop.getProperty("path");
//		File folder = new File(folder_path);
//		File[] listOfFiles = folder.listFiles();
//
//		for (int i = 0; i < listOfFiles.length; i++) {
//			if (listOfFiles[i].isFile()) {
//				System.out.println("File " + listOfFiles[i].getName());
//			} else if (listOfFiles[i].isDirectory()) {
//				System.out.println("Directory " + listOfFiles[i].getName());
//			}
//		}
//
//		return "";
//	}
//	
//	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
//    public static Result annotateCSVFile(String file_name) {
//		State state = new State(State.ALL);
//    	final User user = AuthApplication.getLocalUser(Controller.session());
//		String ownerUri = UserManagement.getUriByEmail(user.email);
//    	List<DataCollection> theResults = DataCollection.find(ownerUri, state);
//		File newFile = new File(file_name);
//		
//		
//
//		NameSpaces ns = NameSpaces.getInstance();
//		String preamble = Downloads.FRAG_START_PREAMBLE;
//		preamble += ns.printNameSpaceList();
//		preamble += "\n";
//
//		//Insert KB    	
//		preamble += Downloads.FRAG_KB_PART1;
//		preamble += Play.application().configuration().getString("hadatac.console.kb"); 
//		preamble += Downloads.FRAG_KB_PART2;
//
//		try {
//			//Insert Data Set
//			preamble += "<" + DataFactory.getNextURI(DataFactory.DATASET_ABBREV) + ">";
//			preamble += Downloads.FRAG_DATASET;
//			preamble += handler.getDataCollectionUri() + ">; ";
//
//			int i = 0;
//			int timeStampIndex = -1;
//			ArrayList<Integer> mt = new ArrayList<Integer>();
//			for (String str : handler.getFields()) {
//				System.out.println(str);
//				System.out.println("get " + i + "-entity: [" + p.getProperty(i + "-entity") + "]");
//				System.out.println("get " + i + "-characteristic: [" + p.getProperty(i + "-characteristic") + "]");
//				System.out.println("get " + i + "-unit:           [" + p.getProperty(i + "-unit") + "]");
//				if ((p.getProperty(i + "-entity") != null) && 
//						(!p.getProperty(i + "-entity").equals("")) &&
//						(p.getProperty(i + "-characteristic") != null) && 
//						(!p.getProperty(i + "-characteristic").equals("")) && 
//						(p.getProperty(i + "-unit") != null) && 
//						(!p.getProperty(i + "-unit").equals(""))) {
//
//					if (p.getProperty(i + "-unit").equals(Downloads.FRAG_IN_DATE_TIME)) {
//						timeStampIndex = i; 
//					} else {
//						mt.add(i);
//					}
//				}
//				i++;
//			}
//
//			preamble += Downloads.FRAG_HAS_MEASUREMENT_TYPE;	
//			int aux = 0;
//			for (Integer mt_count : mt) {
//				preamble += Downloads.FRAG_MT + aux + "> ";
//				if(aux != (mt.size() - 1)){
//					preamble += ", ";
//				}
//				aux++;
//			}
//			preamble += ".\n\n";
//			System.out.println(preamble);
//
//			//Insert measurement types
//			aux = 0;
//			for (Integer mt_count : mt) {
//				preamble += Downloads.FRAG_MT + aux;
//				preamble += Downloads.FRAG_MEASUREMENT_TYPE_PART1;
//				if (timeStampIndex != -1) {
//					preamble += Downloads.FRAG_IN_DATE_TIME;
//					preamble += Downloads.FRAG_IN_DATE_TIME_SUFFIX;
//				}
//				preamble += Downloads.FRAG_MEASUREMENT_TYPE_PART2;
//				preamble += mt_count;
//				preamble += Downloads.FRAG_MEASUREMENT_TYPE_PART3;
//				preamble += "<" + p.getProperty(mt_count + "-entity") + ">"; 
//				preamble += Downloads.FRAG_MEASUREMENT_TYPE_PART4;
//				preamble += "<" + p.getProperty(mt_count + "-characteristic") + ">"; 
//				preamble += Downloads.FRAG_MEASUREMENT_TYPE_PART5;
//				preamble += "<" + p.getProperty(mt_count + "-unit") + ">"; 
//				preamble += " .\n";
//				aux++;
//			}
//
//			if (timeStampIndex != -1) {
//				preamble += "\n";
//				preamble += Downloads.FRAG_IN_DATE_TIME_STATEMENT + " " + timeStampIndex + "  . \n";  
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return ok (completeAnnotation.render("Error processing form. Please restart form."));
//		} 
//
//		preamble += Downloads.FRAG_END_PREAMBLE;
//	}
}