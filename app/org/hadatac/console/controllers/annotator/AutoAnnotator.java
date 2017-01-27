package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.io.FileInputStream;
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
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionsearch.LoadCCSV;
import org.hadatac.console.controllers.triplestore.LoadKB;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.http.DataAcquisitionSchemaQueries;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.data.model.DatasetParsingResult;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;

import com.avaje.ebean.enhance.ant.AntEnhanceTask;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

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
    public static Result toggleAutoAnnotator() {
		System.out.println("Toggling...");
		
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
			System.out.println("off");
		}
		else if(prop.getProperty("auto").equals("off")){
			prop.setProperty("auto", "on");
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
		
		return redirect(routes.AutoAnnotator.index());
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
    	String base_name = FilenameUtils.getBaseName(file_name);
    	
    	AnnotationLog log = new AnnotationLog();
    	log.setFileName(file_name);
	
		String dc_uri = null;
		String deployment_uri = null;
		String schema_uri = null;
		List<DataAcquisition> da_list = DataAcquisition.findAll();
		for(DataAcquisition dc : da_list){
			System.out.println("Data Acquisition URI: " + dc.getUri());
			ValueCellProcessing cellProc = new ValueCellProcessing();
			String qname = cellProc.replaceNameSpaceEx(dc.getUri()).split(":")[1];
			System.out.println(qname);
			if(base_name.startsWith(qname)){
				dc_uri = dc.getUri();
				System.out.println("DataAcquisitionURI: " + dc_uri);
				deployment_uri = dc.getDeploymentUri();
				System.out.println("DeploymentURI: " + deployment_uri);
				schema_uri = dc.getSchemaUri();
				System.out.println("DataAcquisitionSchema:" + schema_uri);
				break;
			}
		}
		if(dc_uri == null){
			System.out.println(String.format("Cannot find the target data acquisition: %s", file_name));
			log.addline(String.format("Cannot find the target data acquisition: %s", file_name));
			log.save();
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
    	System.out.println("deployment_uri is " + deployment_uri);
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
    		SparqlQueryResults char_results = new SparqlQueryResults(dep_json, false);
    		Map<String,String> deploymentChars = new HashMap<String,String>();
    		Iterator<TripleDocument> iterDoc = char_results.sparqlResults.values().iterator();
    		while (iterDoc.hasNext()) {
    			TripleDocument docChar = (TripleDocument)iterDoc.next();
    			if (docChar != null && docChar.get("char") != null && docChar.get("charName") != null) {
    				deploymentChars.put((String)docChar.get("char"),(String)docChar.get("charName"));
    				System.out.println("EC: " + docChar.get("char") + "   ecName: " + docChar.get("charName"));
    			}
    		}
    		handler.setDeploymentCharacteristics(deploymentChars);

    		/*
    		 * Add URI of active datacollection in handler
    		 */
    		DataAcquisition dc = DataFactory.getActiveDataAcquisition(deployment_uri);
    		if (dc != null && dc.getUri() != null) {
    			handler.setDataAcquisitionUri(dc.getUri());
    		}
    	} 
    	else {
    		handler = new CSVAnnotationHandler(deployment_uri, "", "");
    	}
    	
    	if(schema_uri == null){
    		System.out.println("Cannot find schema of the data acquisition");
    		log.addline("Cannot find schema of the data acquisition");
    		log.save();
    		return false;
    	}
 
		String preamble = Downloads.FRAG_START_PREAMBLE;
		preamble += NameSpaces.getInstance().printNameSpaceList();
		preamble += "\n";

		//Insert KB    	
		preamble += Downloads.FRAG_KB_PART1;
		preamble += Play.application().configuration().getString("hadatac.console.kb"); 
		preamble += Downloads.FRAG_KB_PART2;

		try {
			//Insert Data Set
			preamble += "<" + DataFactory.getNextURI(DataFactory.DATASET_ABBREV) + ">";
			preamble += Downloads.FRAG_DATASET;
			preamble += handler.getDataAcquisitionUri() + ">; ";

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
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
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
	    DatasetParsingResult result = LoadCCSV.playLoadCCSV();
	    log.addline(result.getMessage());
		log.save();
		if(result.getStatus() == 0){
			return true;
		}
	    
	    return false;
	}
    
    public static Result moveCSVFile(String file_name) {
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
		
		String path_proc = prop.getProperty("path_proc");
		String path_unproc = prop.getProperty("path_unproc");
		
		File destFolder = new File(path_unproc);
		if (!destFolder.exists()){
			destFolder.mkdirs();
	    }
		File file = new File(path_proc + "/" + file_name);
		file.renameTo(new File(destFolder + "/" + file_name));
		
		return redirect(routes.AutoAnnotator.index());
    }
    
    public static Result downloadCSVFile(String file_name, boolean isProcessed) {
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
		
		String path = ""; 
		if(isProcessed){
			path = prop.getProperty("path_proc");
		}
		else{
			path = prop.getProperty("path_unproc");
		}
		
		return ok(new File(path + "/" + file_name));
    }
    
    public static Result checkAnnotationLog(String file_name) {
    	return ok(annotation_log.render(file_name));
    }
    
    public static Result deleteCSVFile(String file_name, boolean isProcessed) {
    	AnnotationLog.delete(file_name);
    	
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
		
		String path = "";
		if(isProcessed){
			path = prop.getProperty("path_proc");
		}
		else{
			path = prop.getProperty("path_unproc");
		}
		
		File file = new File(path + "/" + file_name);
		file.delete();
		
		return redirect(routes.AutoAnnotator.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 500 * 1024 * 1024)
    public static Result uploadCSVFile(String oper) {
    	System.out.println("uploadCSVFile CALLED!");
    	
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
		String path = prop.getProperty("path_unproc");
		
    	List<FilePart> fileParts = request().body().asMultipartFormData().getFiles();
    	for(FilePart filePart : fileParts) {
    		if (filePart != null) {
        		File file = filePart.getFile();
        		File newFile = new File(path + "/" + filePart.getFilename());
        		InputStream isFile;
        		try {
        			isFile = new FileInputStream(file);
        			byte[] byteFile;
        			byteFile = IOUtils.toByteArray(isFile);
        			FileUtils.writeByteArrayToFile(newFile, byteFile);
        			isFile.close();
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
 		}
    	
    	return redirect(routes.AutoAnnotator.index());
    }
}