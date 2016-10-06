package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisition.LoadCCSV;
import org.hadatac.console.controllers.triplestore.LoadKB;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.DataAcquisitionSchemaQueries;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.views.html.annotator.auto_ccsv;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;

public class AutoAnnotator extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {
		Properties prop = new Properties();
		try {
			InputStream is = LoadKB.class.getClassLoader().getResourceAsStream("autoccsv.config");
			prop.load(is);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> proc_files = new ArrayList<String>();
		ArrayList<String> unproc_files = new ArrayList<String>();
		String path_proc = prop.getProperty("path_proc");
		String path_unproc = prop.getProperty("path_unproc");
		
		File folder = new File(path_proc);
		if (!folder.exists()){
			folder.mkdirs();
	    }
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				proc_files.add(listOfFiles[i].getName());
			}
		}
		folder = new File(path_unproc);
		if (!folder.exists()){
			folder.mkdirs();
	    }
		listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				unproc_files.add(listOfFiles[i].getName());
			}
		}
		
		boolean bStarted = false;
		if(prop.getProperty("auto").equals("on")){
			bStarted = true;
		}
		System.out.println(bStarted);

		return ok(auto_ccsv.render(unproc_files, proc_files, bStarted));
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
		return index();
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result toggleAutoAnnotator(List<String> unproc_files, 
    		                                 List<String> proc_files) {
		System.out.println("Toggling...");
		boolean bStarted = false;
		
		Properties prop = new Properties();
		try {
			InputStream is = LoadKB.class.getClassLoader().getResourceAsStream("autoccsv.config");
			prop.load(is);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(prop.getProperty("auto").equals("on")){
			prop.setProperty("auto", "off");
			bStarted = false;
			System.out.println("off");
		}
		else if(prop.getProperty("auto").equals("off")){
			prop.setProperty("auto", "on");
			bStarted = true;
			System.out.println("on");
		}
		URL url = LoadKB.class.getClassLoader().getResource("autoccsv.config");
		try {
			prop.store(new FileOutputStream(new File(url.toURI())), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return ok(auto_ccsv.render(unproc_files, proc_files, bStarted));
	}
	
	public static void autoAnnotate() {
		Properties prop = new Properties();
		try {
			InputStream is = LoadKB.class.getClassLoader().getResourceAsStream("autoccsv.config");
			prop.load(is);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(prop.getProperty("auto").equals("off")){
			return;
		}
		
		String path_proc = prop.getProperty("path_proc");
		String path_unproc = prop.getProperty("path_unproc");
		
		File folder = new File(path_unproc);
		if (!folder.exists()){
			folder.mkdirs();
	    }
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String file_name = listOfFiles[i].getName();
				if(annotateCSVFile(file_name)){
					//Move the file to the folder for processed files
					File destFolder = new File(path_proc);
					if (!destFolder.exists()){
						destFolder.mkdirs();
				    }
					listOfFiles[i].renameTo(new File(destFolder + "/" + file_name));
					listOfFiles[i].delete();
				}
			}
		}
	}
	
    public static boolean annotateCSVFile(String file_name) {
    	System.out.println("Annotating " + file_name);
    	
		State state = new State(State.ALL);
		String ownerUri = UserManagement.getUriByEmail("gychant@qq.com");
    	List<DataCollection> da_list = DataCollection.find(ownerUri, state);
		
		String dc_uri = null;
		String deployment_uri = null;
		String schema_uri = null;
		for(DataCollection dc : da_list){
			System.out.println(file_name);
			String base_name = FilenameUtils.getBaseName(file_name);
			System.out.println(base_name);
			ValueCellProcessing cellProc = new ValueCellProcessing();
			System.out.println(dc.getUri());
			String qname = cellProc.replaceNameSpaceEx(dc.getUri()).split(":")[1];
			System.out.println(qname);
			if(qname.equals(base_name)){
				dc_uri = dc.getUri();
				System.out.println("=================================" + dc_uri);
				deployment_uri = dc.getDeploymentUri();
				System.out.println("=================================" + deployment_uri);
				schema_uri = dc.getSchemaUri();
				System.out.println("=================================" + schema_uri);
				break;
			}
		}
		if(dc_uri == null){
			System.out.println(String.format("Cannot find the target data acquisition: %s", file_name));
			return false;
		}
		
		CSVAnnotationHandler handler;
    	try {
    		if (deployment_uri != null) {
    			deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
    		} else {
    			deployment_uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
    	System.out.println("uploadCSV: uri is " + deployment_uri);
    	if (!deployment_uri.equals("")) {

    		/*
    		 *  Add deployment information into handler
    		 */
    		String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, deployment_uri);
    		//System.out.println(json);
    		SparqlQueryResults results = new SparqlQueryResults(json, false);
    		TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
    		handler = new CSVAnnotationHandler(deployment_uri, docDeployment.get("platform"), docDeployment.get("instrument"));
    		    		
    		/*
    		 * Add possible detector's characterisitcs into handler
    		 */
    		String dep_json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_CHARACTERISTICS_BY_URI, deployment_uri);
    		System.out.println(dep_json);
    		SparqlQueryResults results2 = new SparqlQueryResults(dep_json, false);
    		Iterator<TripleDocument> it = results2.sparqlResults.values().iterator();
    		Map<String,String> deploymentChars = new HashMap<String,String>();
    		TripleDocument docChar;
    		while (it.hasNext()) {
    			docChar = (TripleDocument) it.next();
    			if (docChar != null && docChar.get("char") != null && docChar.get("charName") != null) {
    				deploymentChars.put((String)docChar.get("char"),(String)docChar.get("charName"));
    				System.out.println("EC: " + docChar.get("char") + "   ecName: " + docChar.get("charName"));
    			}
    		}
    		handler.setDeploymentCharacteristics(deploymentChars);

    		/*
    		 * Add URI of active datacollection in handler
    		 */
    		DataCollection dc = DataFactory.getActiveDataCollection(deployment_uri);
    		if (dc != null && dc.getUri() != null) {
    			handler.setDataCollectionUri(dc.getUri());
    		}
    	} else {
    		handler = new CSVAnnotationHandler(deployment_uri, "", "");
    	}
    	
    	if(schema_uri == null){
    		System.out.println("Cannot find schema of the data acquisition");
    		return false;
    	}

		NameSpaces ns = NameSpaces.getInstance();
		String preamble = Downloads.FRAG_START_PREAMBLE;
		preamble += ns.printNameSpaceList();
		preamble += "\n";

		//Insert KB    	
		preamble += Downloads.FRAG_KB_PART1;
		preamble += Play.application().configuration().getString("hadatac.console.kb"); 
		preamble += Downloads.FRAG_KB_PART2;

		try {
			//Insert Data Set
			preamble += "<" + DataFactory.getNextURI(DataFactory.DATASET_ABBREV) + ">";
			preamble += Downloads.FRAG_DATASET;
			preamble += handler.getDataCollectionUri() + ">; ";

			int timeStampIndex = -1;
			int aux = 0;
			ArrayList<Integer> mt = new ArrayList<Integer>();
			ArrayList<String> mt_preamble = new ArrayList<String>();
			String json = DataAcquisitionSchemaQueries.exec(DataAcquisitionSchemaQueries.ATTRIBUTE_BY_SCHEMA_URI, schema_uri);
			System.out.println(json);
			SparqlQueryResults results = new SparqlQueryResults(json, false);
			Iterator<TripleDocument> iterDoc = results.sparqlResults.values().iterator();
			while(iterDoc.hasNext()){
				TripleDocument doc = iterDoc.next();
				int i = Integer.parseInt(doc.get("hasPosition"));
				String entity = doc.get("hasEntity");
				String attrib = doc.get("hasAttribute");
				String unit = doc.get("hasUnit");
				System.out.println("get " + i + "-entity: [" + entity + "]");
				System.out.println("get " + i + "-attribute: [" + attrib + "]");
				System.out.println("get " + i + "-unit: [" + unit + "]");

//				if (entity != null && !entity.equals("") &&
//						attrib != null && !attrib.equals("") && 
//						unit != null && !unit.equals("")) {	
					if (unit.equals(Downloads.FRAG_IN_DATE_TIME)) {
						timeStampIndex = i; 
					} else {
						String p = "";
						p += Downloads.FRAG_MT + aux;
						p += Downloads.FRAG_MEASUREMENT_TYPE_PART1;
						if (timeStampIndex != -1) {
							p += Downloads.FRAG_IN_DATE_TIME;
							p += Downloads.FRAG_IN_DATE_TIME_SUFFIX;
						}
						p += Downloads.FRAG_MEASUREMENT_TYPE_PART2;
						p += i;
						p += Downloads.FRAG_MEASUREMENT_TYPE_PART3;
						p += "<" + entity + ">";
						p += Downloads.FRAG_MEASUREMENT_TYPE_PART4;
						p += "<" + attrib + ">"; 
						p += Downloads.FRAG_MEASUREMENT_TYPE_PART5;
						p += "<" + unit + ">";
						p += " .\n";
						aux++;
						mt.add(i);
						mt_preamble.add(p);
					}
//				}
			}
			
			preamble += Downloads.FRAG_HAS_MEASUREMENT_TYPE;	
			for (int i = 0; i < mt.size(); i++) {
				preamble += Downloads.FRAG_MT + i + "> ";
				if(i != (mt.size() - 1)){
					preamble += ", ";
				}
			}
			preamble += ".\n\n";

			//Insert measurement types
			for (String mt_str : mt_preamble) {
				preamble += mt_str;
			}

			if (timeStampIndex != -1) {
				preamble += "\n";
				preamble += Downloads.FRAG_IN_DATE_TIME_STATEMENT + " " + timeStampIndex + "  . \n";  
			}
			
			System.out.println(preamble);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		preamble += Downloads.FRAG_END_PREAMBLE;

		Properties prop = new Properties();
		try {
			InputStream is = LoadKB.class.getClassLoader().getResourceAsStream("autoccsv.config");
			prop.load(is);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String path_unproc = prop.getProperty("path_unproc");
		File newFile = new File(path_unproc + file_name);
	    try {
			preamble += FileUtils.readFileToString(newFile, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	    try {
			FileUtils.writeStringToFile(new File(LoadCCSV.UPLOAD_NAME), preamble);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	    LoadCCSV.playLoadCCSV();
	    
	    return true;
	}
}