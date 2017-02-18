package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.h2.tools.Csv;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionsearch.LoadCCSV;
import org.hadatac.console.controllers.annotator.routes;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.http.DataAcquisitionSchemaQueries;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.AssignOwnerForm;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.data.model.DatasetParsingResult;
import org.hadatac.entity.pojo.CSVFile;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser;
import play.mvc.Http.MultipartFormData.FilePart;

public class AutoAnnotator extends Controller {
	
	private static boolean search(String fileName, List<CSVFile> pool) {
		for (CSVFile file : pool) {
			if (file.getFileName().equals(fileName)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void includeUnrecognizedFiles(String path, List<CSVFile> ownedFiles) {		
 		File folder = new File(path);
 		if (!folder.exists()){
 			folder.mkdirs();
 	    }

 		File[] listOfFiles = folder.listFiles();
 		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if (!search(listOfFiles[i].getName(), ownedFiles)) {
					CSVFile newFile = new CSVFile();
 					newFile.setFileName(listOfFiles[i].getName());
 					ownedFiles.add(newFile);
				}
 			}
 		}
	}

	private static void filterNonexistedFiles(String path, List<CSVFile> files) {
		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
	    }
		
		File[] listOfFiles = folder.listFiles();
		Iterator<CSVFile> iterFile = files.iterator();
		while(iterFile.hasNext()) {
			CSVFile file = iterFile.next();
			boolean isExisted = false;
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					if(file.getFileName().equals(listOfFiles[i].getName())) {
						isExisted = true;
						break;
					}
				}
			}
			if (!isExisted) {
				iterFile.remove();
			}
		}
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {		
		final SysUser user = AuthApplication.getLocalUser(session());
		
		List<CSVFile> proc_files = null;
		List<CSVFile> unproc_files = null;
		
		String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		
		if (user.isDataManager()) {
			proc_files = CSVFile.findAll(State.PROCESSED);
			unproc_files = CSVFile.findAll(State.UNPROCESSED);
			includeUnrecognizedFiles(path_unproc, unproc_files);
		}
		else {
			proc_files = CSVFile.find(user.getEmail(), State.PROCESSED);
			unproc_files = CSVFile.find(user.getEmail(), State.UNPROCESSED);
		}
		
		filterNonexistedFiles(path_proc, proc_files);
		filterNonexistedFiles(path_unproc, unproc_files);
		
		boolean bStarted = false;
		if(ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("on")){
			bStarted = true;
		}

		return ok(auto_ccsv.render(unproc_files, proc_files, bStarted, user.isDataManager()));
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
		return index();
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result assignFileOwner(String ownerEmail, String selectedFile) {	
    	return ok(assignOwner.render(Form.form(AssignOwnerForm.class),
    								 User.getUserEmails(),
    								 routes.AutoAnnotator.processForm(ownerEmail, selectedFile),
    								 "Selected File",
    								 selectedFile));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postAssignFileOwner(String ownerEmail, String selectedFile) {
		return assignFileOwner(ownerEmail, selectedFile);
	}
	
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result processForm(String ownerEmail, String selectedFile) {
        Form<AssignOwnerForm> form = Form.form(AssignOwnerForm.class).bindFromRequest();
        AssignOwnerForm data = form.get();
        
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(assignOwner.render(Form.form(AssignOwnerForm.class),
					 User.getUserEmails(),
					 routes.AutoAnnotator.processForm(ownerEmail, selectedFile),
					 "Selected File",
					 selectedFile));
        } else {
            CSVFile newCSV = CSVFile.findByName(ownerEmail, selectedFile);
            if (newCSV == null) {
            	newCSV = new CSVFile();
            	newCSV.setFileName(selectedFile);
            	newCSV.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
            	newCSV.setProcessStatus(false);
            	newCSV.setUploadTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            }
            newCSV.setOwnerEmail(data.getUser());
            newCSV.save();
    		return redirect(routes.AutoAnnotator.index());
        }
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result toggleAutoAnnotator() {
		System.out.println("Toggling...");
		
		if (ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("on")) {
			ConfigProp.setPropertyValue("autoccsv.config", "auto", "off");
			System.out.println("Turning auto-annotation off");
		}
		else {
			ConfigProp.setPropertyValue("autoccsv.config", "auto", "on");
			System.out.println("Turning auto-annotation on");
		}
		
		return redirect(routes.AutoAnnotator.index());
	}
	
	public static void autoAnnotate() {
		if(ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("off")){
			return;
		}
		
		String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		List<CSVFile> unproc_files = CSVFile.findAll(State.UNPROCESSED);
		filterNonexistedFiles(path_unproc, unproc_files);
		
		for (CSVFile file : unproc_files) {
			String file_name = file.getFileName();
			if(annotateCSVFile(file_name)){
				//Move the file to the folder for processed files
				File destFolder = new File(path_proc);
				if (!destFolder.exists()){
					destFolder.mkdirs();
			    }
				
				file.delete();

				file.setProcessStatus(true);
				file.setProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
				file.save();
				File f = new File(path_unproc + "/" + file_name);
				f.renameTo(new File(destFolder + "/" + file_name));
				f.delete();
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
		if(schema_uri == null){
    		System.out.println("Cannot find schema of the data acquisition");
    		log.addline("Cannot find schema of the data acquisition");
    		log.save();
    		return false;
    	}
		if(deployment_uri == null){
    		System.out.println("Cannot find deployment of the data acquisition");
    		log.addline("Cannot find deployment of the data acquisition");
    		log.save();
    		return false;
    	}
		
    	try {
    		deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return false;
		}
    	System.out.println("deployment_uri is " + deployment_uri);
    	
    	CSVAnnotationHandler handler = null;
    	if (!deployment_uri.equals("")) {
    		/*
    		 *  Add deployment information into handler
    		 */
    		String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, deployment_uri);
    		SparqlQueryResults results = new SparqlQueryResults(json, false);
    		TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
    		handler = new CSVAnnotationHandler(deployment_uri, 
    				docDeployment.get("platform"), 
    				docDeployment.get("instrument"));
    		
    		/*
    		 * Add possible detector's characteristics into handler
    		 */
    		String dep_json = DeploymentQueries.exec(
    				DeploymentQueries.DEPLOYMENT_CHARACTERISTICS_BY_URI, deployment_uri);
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
    		 * Add URI of active data acquisition in handler
    		 */
    		DataAcquisition dc = DataFactory.getActiveDataAcquisition(deployment_uri);
    		if (dc != null && dc.getUri() != null) {
    			handler.setDataAcquisitionUri(dc.getUri());
    		}
    	}

		String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		File newFile = new File(path_unproc + file_name);
	    try {
			FileUtils.writeStringToFile(new File(LoadCCSV.UPLOAD_NAME), 
										createPreamble(handler, schema_uri) + 
										FileUtils.readFileToString(newFile, "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	    
	    // Parse and load the generated CCSV file
	    DatasetParsingResult result = LoadCCSV.playLoadCCSV();
	    log.addline(result.getMessage());
		log.save();
		if(result.getStatus() == 0){
			return true;
		}
	    
	    return false;
	}
    
    private static String createPreamble(CSVAnnotationHandler handler, 
    									 String schema_uri) {
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
			String json = DataAcquisitionSchemaQueries.exec(
					DataAcquisitionSchemaQueries.ATTRIBUTE_BY_SCHEMA_URI, schema_uri);
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
			return "";
		}

		preamble += Downloads.FRAG_END_PREAMBLE;
		
		return preamble;
    }
    
    public static Result moveCSVFile(String ownerEmail, String file_name) {		
		String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		
		CSVFile csvFile = CSVFile.findByName(ownerEmail, file_name);
		csvFile.delete();
		csvFile.setProcessStatus(false);
		csvFile.save();
		
		File destFolder = new File(path_unproc);
		if (!destFolder.exists()){
			destFolder.mkdirs();
	    }
		File file = new File(path_proc + "/" + file_name);
		file.renameTo(new File(destFolder + "/" + file_name));
		
		return redirect(routes.AutoAnnotator.index());
    }
    
    public static Result downloadCSVFile(String file_name, boolean isProcessed) {		
		String path = ""; 
		if(isProcessed){
			path = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		}
		else{
			path = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		}
		
		return ok(new File(path + "/" + file_name));
    }
    
    public static Result checkAnnotationLog(String file_name) {
    	return ok(annotation_log.render(file_name));
    }
    
    public static Result deleteCSVFile(String ownerEmail, String file_name, boolean isProcessed) {
    	AnnotationLog.delete(file_name);
    	
		CSVFile csvFile = CSVFile.findByName(ownerEmail, file_name);
		csvFile.delete();

		String path = "";
		if(isProcessed){
			path = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		}
		else{
			path = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		}
		
		File file = new File(path + "/" + file_name);
		file.delete();
		
		return redirect(routes.AutoAnnotator.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 500 * 1024 * 1024)
    public static Result uploadCSVFile(String oper) {
    	System.out.println("uploadCSVFile CALLED!");
    	
		String path = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		
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
        			
        			CSVFile csvFile = new CSVFile();
        			csvFile.setFileName(filePart.getFilename());
        			csvFile.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
        			csvFile.setProcessStatus(false);
        			csvFile.setUploadTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        			csvFile.save();
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
 		}
    	
    	return redirect(routes.AutoAnnotator.index());
    }
}

