package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.io.FileInputStream;
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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionsearch.LoadCCSV;
import org.hadatac.console.controllers.annotator.routes;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.ResumableUpload;
import org.hadatac.console.models.AssignOwnerForm;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.LabKeyLoginForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.console.views.html.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.data.loader.SampleGenerator;
import org.hadatac.data.loader.SubjectGenerator;
import org.hadatac.data.model.DatasetParsingResult;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser;
import play.mvc.Http.MultipartFormData.FilePart;
import play.twirl.api.Html;

public class AutoAnnotator extends Controller {
	
	private static boolean search(String fileName, List<DataFile> pool) {
		for (DataFile file : pool) {
			if (file.getFileName().equals(fileName)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void includeUnrecognizedFiles(String path, List<DataFile> ownedFiles) {		
 		File folder = new File(path);
 		if (!folder.exists()){
 			folder.mkdirs();
 	    }

 		File[] listOfFiles = folder.listFiles();
 		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".csv")) {
				if (!search(listOfFiles[i].getName(), ownedFiles)) {
					DataFile newFile = new DataFile();
 					newFile.setFileName(listOfFiles[i].getName());
 					newFile.save();
 					ownedFiles.add(newFile);
				}
 			}
 		}
	}

	private static void filterNonexistedFiles(String path, List<DataFile> files) {
		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
	    }
		
		File[] listOfFiles = folder.listFiles();
		Iterator<DataFile> iterFile = files.iterator();
		while(iterFile.hasNext()) {
			DataFile file = iterFile.next();
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
	
	private static List<File> findFilesByExtension(String path, String ext) {
		List<File> results = new ArrayList<File>();
		
		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
	    }
		
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() 
				&& FilenameUtils.getExtension(listOfFiles[i].getName()).equals(ext)) {
					results.add(listOfFiles[i]);
			}
		}
		
		return results;
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {		
		final SysUser user = AuthApplication.getLocalUser(session());
		
		List<DataFile> proc_files = null;
		List<DataFile> unproc_files = null;
		
		String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		
		if (user.isDataManager()) {
			proc_files = DataFile.findAll(State.PROCESSED);
			unproc_files = DataFile.findAll(State.UNPROCESSED);
			includeUnrecognizedFiles(path_unproc, unproc_files);
		}
		else {
			proc_files = DataFile.find(user.getEmail(), State.PROCESSED);
			unproc_files = DataFile.find(user.getEmail(), State.UNPROCESSED);
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
        	DataFile newCSV = DataFile.findByName(ownerEmail, selectedFile);
            if (newCSV == null) {
            	newCSV = new DataFile();
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
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result setLabKeyCredentials() {
		return ok(syncLabkey.render("init", routes.AutoAnnotator.postSetLabKeyCredentials().url(), "", false));
	}
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postSetLabKeyCredentials() {
    	Form<LabKeyLoginForm> form = Form.form(LabKeyLoginForm.class).bindFromRequest();
    	String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/";
        String user_name = form.get().getUserName();
        String password = form.get().getPassword();
    	LabkeyDataHandler loader = new LabkeyDataHandler(
    			site, user_name, password, path);
    	try {
    		loader.checkAuthentication();
    		Credential cred = new Credential();
    		cred.setUserName(user_name);
    		cred.setPassword(password);
    		cred.save();
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render("login_failed", "", "", false));
    		}
    	}
    	
    	return ok(main.render("Results,", "", 
    			new Html("<h3>Your provided credentials are valid and saved!</h3>")));
    }
	
	public static void autoAnnotate() {
		if(ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("off")){
			return;
		}
		
		String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		List<DataFile> unproc_files = DataFile.findAll(State.UNPROCESSED);
		filterNonexistedFiles(path_unproc, unproc_files);
		
		for (DataFile file : unproc_files) {
			String file_name = file.getFileName();
			boolean bSucceed = false;
			if (file_name.startsWith("DA")) {
				bSucceed = annotateCSVFile(file_name);
			}
			else if (file_name.startsWith("SID")) {
				bSucceed = annotateSampleIdFile(new File(path_unproc + "/" + file_name));
			}
			else if (file_name.startsWith("PID")) {
				bSucceed = annotateSubjectIdFile(new File(path_unproc + "/" + file_name));
			}
			if (bSucceed) {
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
	
	public static boolean annotateSampleIdFile(File file) {
		SampleGenerator sampleGenerator = new SampleGenerator(file);
		List<Map<String, Object>> rows = sampleGenerator.createRows();
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
        Credential cred = Credential.find();
        AnnotationLog log = new AnnotationLog();
    	log.setFileName(file.getName());
        if (null != cred) {
        	log.addline(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
        	log.addline(sampleGenerator.toString());
        	System.out.println(sampleGenerator.toString());
    		log.save();
    		return false;
        }
    	LabkeyDataHandler labkeyDataHandler = new LabkeyDataHandler(
    			site, cred.getUserName(), cred.getPassword(), path);
		try {
			int nRows = labkeyDataHandler.insertRows("Sample", rows);
			log.addline(Feedback.println(Feedback.WEB, String.format(
					"[OK] %d row(s) have been inserted into Sample table", nRows)));
			log.save();
		} catch (CommandException e) {
			log.addline(Feedback.println(Feedback.WEB, "[ERROR] " + e.getMessage()));
    		log.save();
    		return false;
		}
		
		return true;
	}
	
	public static boolean annotateSubjectIdFile(File file){
		SubjectGenerator subjectGenerator = new SubjectGenerator(file);
		List<Map<String, Object>> rows = subjectGenerator.createRows();
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
        Credential cred = Credential.find();
        AnnotationLog log = new AnnotationLog();
    	log.setFileName(file.getName());
        if (null == cred) {
        	log.addline(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
    		log.save();
    		return false;
        }
    	LabkeyDataHandler labkeyDataHandler = new LabkeyDataHandler(
    			site, cred.getUserName(), cred.getPassword(), path);
		try {
			int nRows = labkeyDataHandler.insertRows("Subject", rows);
			log.addline(Feedback.println(Feedback.WEB, String.format(
					"[OK] %d row(s) have been inserted into Subject table", nRows)));
			log.save();
		} catch (CommandException e) {
			log.addline(Feedback.println(Feedback.WEB, "[ERROR] " + e.getMessage()));
    		log.save();
    		return false;
		}
		
		return true;
	}
	
    public static boolean annotateCSVFile(String file_name) {
    	String base_name = FilenameUtils.getBaseName(file_name);
    	
    	AnnotationLog log = new AnnotationLog();
    	log.setFileName(file_name);
	
		String dc_uri = null;
		String deployment_uri = null;
		String schema_uri = null;
		List<DataAcquisition> da_list = DataAcquisition.findAll();
		for(DataAcquisition dc : da_list){
			ValueCellProcessing cellProc = new ValueCellProcessing();
			String qname = cellProc.replaceNameSpaceEx(dc.getUri()).split(":")[1];
			if(base_name.startsWith(qname)){
				dc_uri = dc.getUri();
				deployment_uri = dc.getDeploymentUri();
				schema_uri = dc.getSchemaUri();
				break;
			}
		}
		if (dc_uri == null) {
			log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] Cannot find the target data acquisition: %s", file_name)));
			log.save();
			return false;
		} else {
			log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Found the target data acquisition: %s", file_name)));
		}
		if (schema_uri == null) {
    		log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] No schemas specified for the data acquisition: %s", file_name)));
    		log.save();
    		return false;
    	} else {
    		log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Schema %s specified for the data acquisition: %s", schema_uri, file_name)));
    	}
		if (deployment_uri == null) {
    		log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] No deployments specified for the data acquisition: %s", file_name)));
    		log.save();
    		return false;
    	} else {
    		try {
        		deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
    		} catch (UnsupportedEncodingException e) {
    			log.addline(Feedback.println(Feedback.WEB, String.format("URL decoding error for deployment uri %s", deployment_uri)));
    			log.save();
    			return false;
    		}
    		log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Deployment %s specified for the data acquisition %s", deployment_uri, file_name)));
    	}
		
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
    			}
    		}
    		handler.setDeploymentCharacteristics(deploymentChars);

    		DataAcquisition dc = DataAcquisition.findByUri(dc_uri);
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
		} catch (Exception e) {
			log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] %s", e.getMessage())));
			log.save();
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
    									 String schema_uri) throws Exception {
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
			
			DataAcquisitionSchema schema = DataAcquisitionSchema.find(schema_uri);
			if (null == schema) {
				throw new Exception(String.format("Can not find schema %s", schema_uri));
			}
			
			for (DataAcquisitionSchema.SchemaAttribute attribute : schema.getAttributes()) {
				int i = Integer.parseInt(attribute.getPosition());
				String entity = attribute.getEntity();
				String attrib = attribute.getAttribute();
				String unit = attribute.getUnit();

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
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		preamble += Downloads.FRAG_END_PREAMBLE;
		
		return preamble;
    }
    
    public static Result moveDataFile(String ownerEmail, String file_name) {		
		String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		
		DataFile dataFile = DataFile.findByName(ownerEmail, file_name);
		dataFile.delete();
		dataFile.setProcessStatus(false);
		dataFile.save();
		
		File destFolder = new File(path_unproc);
		if (!destFolder.exists()){
			destFolder.mkdirs();
	    }
		File file = new File(path_proc + "/" + file_name);
		file.renameTo(new File(destFolder + "/" + file_name));
		
		return redirect(routes.AutoAnnotator.index());
    }
    
    public static Result downloadDataFile(String file_name, boolean isProcessed) {		
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
    
    public static Result deleteDataFile(String ownerEmail, String file_name, boolean isProcessed) {
    	AnnotationLog.delete(file_name);
    	
    	DataFile dataFile = DataFile.findByName(ownerEmail, file_name);
		dataFile.delete();

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
    public static Result uploadDataFile(String oper) {
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
        			
        			DataFile dataFile = new DataFile();
        			dataFile.setFileName(filePart.getFilename());
        			dataFile.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
        			dataFile.setProcessStatus(false);
        			dataFile.setUploadTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        			dataFile.save();
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
 		}
    	
    	return redirect(routes.AutoAnnotator.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result uploadDataFileByChunking(
    		String resumableChunkNumber,
    		String resumableChunkSize, 
    		String resumableCurrentChunkSize,
    		String resumableTotalSize,
    		String resumableIdentifier,
    		String resumableFilename,
    		String resumableRelativePath) {
    	if (ResumableUpload.uploadFileByChunking(request(), ConfigProp.getPropertyValue("autoccsv.config", "path_unproc"))) {
            return ok("Uploaded."); //This Chunk has been Uploaded.
        } else {
        	return status(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postUploadDataFileByChunking(
    		String resumableChunkNumber, 
    		String resumableChunkSize, 
    		String resumableCurrentChunkSize,
    		String resumableTotalSize,
    		String resumableIdentifier,
    		String resumableFilename,
    		String resumableRelativePath) {
    	if (ResumableUpload.postUploadFileByChunking(request(), ConfigProp.getPropertyValue("autoccsv.config", "path_unproc"))) {
    		DataFile dataFile = new DataFile();
    		dataFile.setFileName(resumableFilename);
    		dataFile.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
    		dataFile.setProcessStatus(false);
    		dataFile.setUploadTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
    		dataFile.save();
    		return(ok("Upload finished"));
        } else {
            return(ok("Upload"));
        }
    }
}

