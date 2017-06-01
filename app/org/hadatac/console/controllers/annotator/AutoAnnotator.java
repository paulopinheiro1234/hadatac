package org.hadatac.console.controllers.annotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionsearch.LoadCCSV;
import org.hadatac.console.controllers.annotator.routes;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.ResumableUpload;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.LabKeyLoginForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.console.views.html.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.data.loader.AgentGenerator;
import org.hadatac.data.loader.DASchemaAttrGenerator;
import org.hadatac.data.loader.DASchemaGenerator;
import org.hadatac.data.loader.DASchemaObjectGenerator;
import org.hadatac.data.loader.DataAcquisitionGenerator;
import org.hadatac.data.loader.DeploymentGenerator;
import org.hadatac.data.loader.GeneralGenerator;
import org.hadatac.data.loader.SampleGenerator;
import org.hadatac.data.loader.SampleSubjectMapper;
import org.hadatac.data.loader.StudyGenerator;
import org.hadatac.data.loader.SubjectGenerator;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
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
	
	public static HashMap<String, String> codeMappings = new HashMap<String, String>();
	public static HashMap<String, String> entityMappings = new HashMap<String, String>();
	public static String study_id;
	
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
		while (iterFile.hasNext()) {
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
			includeUnrecognizedFiles(path_proc, proc_files);
		}
		else {
			proc_files = DataFile.find(user.getEmail(), State.PROCESSED);
			unproc_files = DataFile.find(user.getEmail(), State.UNPROCESSED);
		}
		
		filterNonexistedFiles(path_proc, proc_files);
		filterNonexistedFiles(path_unproc, unproc_files);
		
		boolean bStarted = false;
		if (ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("on")) {
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
    	return ok(assignOption.render(User.getUserEmails(),
    			routes.AutoAnnotator.processOwnerForm(ownerEmail, selectedFile),
    			"Owner", 
    			"Selected File", 
    			selectedFile));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postAssignFileOwner(String ownerEmail, String selectedFile) {
		return assignFileOwner(ownerEmail, selectedFile);
	}
	
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result processOwnerForm(String ownerEmail, String selectedFile) {
        Form<AssignOptionForm> form = Form.form(AssignOptionForm.class).bindFromRequest();
        AssignOptionForm data = form.get();
        
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(assignOption.render(User.getUserEmails(),
            		routes.AutoAnnotator.processOwnerForm(ownerEmail, selectedFile),
            		"Owner",
            		"Selected File",
            		selectedFile));
        } else {
        	DataFile file = DataFile.findByName(ownerEmail, selectedFile);
            if (file == null) {
            	file = new DataFile();
            	file.setFileName(selectedFile);
            	file.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
            	file.setProcessStatus(false);
            	file.setUploadTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            }
            file.setOwnerEmail(data.getOption());
            file.save();
    		return redirect(routes.AutoAnnotator.index());
        }
    }
    
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result assignDataAcquisition(String dataAcquisitionUri, String selectedFile) {
		List<String> dataAcquisitionURIs = new ArrayList<String>();
		DataAcquisition.findAll().forEach((da) -> dataAcquisitionURIs.add(
				ValueCellProcessing.replaceNameSpaceEx(da.getUri())));
		
    	return ok(assignOption.render(dataAcquisitionURIs,
    			routes.AutoAnnotator.processDataAcquisitionForm(dataAcquisitionUri, selectedFile),
    			"Data Acquisition",
    			"Selected File",
    			selectedFile));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postAssignDataAcquisition(String dataAcquisitionUri, String selectedFile) {
		return assignDataAcquisition(dataAcquisitionUri, selectedFile);
	}
	
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result processDataAcquisitionForm(String dataAcquisitionUri, String selectedFile) {
        Form<AssignOptionForm> form = Form.form(AssignOptionForm.class).bindFromRequest();
        AssignOptionForm data = form.get();
        
		List<String> dataAcquisitionURIs = new ArrayList<String>();
		DataAcquisition.findAll().forEach((da) -> dataAcquisitionURIs.add(
				ValueCellProcessing.replaceNameSpaceEx(da.getUri())));
        
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(assignOption.render(dataAcquisitionURIs,
					 routes.AutoAnnotator.processDataAcquisitionForm(dataAcquisitionUri, selectedFile),
					 "Data Acquisition",
					 "Selected File",
					 selectedFile));
        } else {
        	DataFile file = DataFile.findByName(dataAcquisitionUri, selectedFile);
            if (file == null) {
            	file = new DataFile();
            	file.setFileName(selectedFile);
            	file.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
            	file.setProcessStatus(false);
            	file.setUploadTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            }
            file.setDataAcquisitionUri(ValueCellProcessing.replacePrefixEx(data.getOption()));
            file.save();
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
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result downloadTemplates() {
		return ok(download_templates.render());
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postDownloadTemplates() {
		return postDownloadTemplates();
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result setLabKeyCredentials() {
		return ok(syncLabkey.render("init", routes.AutoAnnotator.
				postSetLabKeyCredentials().url(), ""));
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
    			return ok(syncLabkey.render("login_failed", "", ""));
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
			AnnotationLog log = new AnnotationLog(file_name);
			log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Processing file: %s", file_name)));
			log.save();
			boolean bSucceed = false;
			if (file_name.startsWith("DA")) {
				bSucceed = annotateCSVFile(file);
			}
			else if (file_name.startsWith("SID")) {
				bSucceed = annotateSampleIdFile(new File(path_unproc + "/" + file_name));
			}
			else if (file_name.startsWith("PID")) {
				bSucceed = annotateSubjectIdFile(new File(path_unproc + "/" + file_name));
			}
			else if (file_name.startsWith("STD")) {
				bSucceed = annotateStudyIdFile(new File(path_unproc + "/" + file_name));
			}
			else if (file_name.startsWith("MAP")) {
				bSucceed = annotateMapFile(new File(path_unproc + "/" + file_name));
			}
			else if (file_name.startsWith("ACQ")) {
				bSucceed = annotateDataAcquisitionFile(new File(path_unproc + "/" + file_name));
			}
			else if (file_name.startsWith("SDD")) {
				bSucceed = annotateDataAcquisitionSchemaFile(new File(path_unproc + "/" + file_name));
			}
			if (bSucceed) {
			        //System.out.println("Succeed: Entering");
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
		   	        //System.out.println("Succeed: Leaving");
   			}
		}
	}
	
	public static Model createModel(List<Map<String, Object>> rows) {
    	Model model = ModelFactory.createDefaultModel();
    	for (Map<String, Object> row : rows) {
    		Resource sub = model.createResource(ValueCellProcessing.replacePrefixEx((String)row.get("hasURI")));
    		for (String key : row.keySet()) {
    			if (!key.equals("hasURI")) {
    				Property pred = null;
    				if (key.equals("a")) {
    					pred = model.createProperty(ValueCellProcessing.replacePrefixEx("rdf:type"));
    				}
    				else {
    					pred = model.createProperty(ValueCellProcessing.replacePrefixEx(key));
    				}
    				
    				String cellValue = (String)row.get(key);
					if (ValueCellProcessing.isAbbreviatedURI(cellValue)) {
						Resource obj = model.createResource(ValueCellProcessing.replacePrefixEx(cellValue));
						model.add(sub, pred, obj);
					}
					else {
						Literal obj = model.createLiteral(
								cellValue.replace("\n", " ").replace("\r", " ").replace("\"", "''"));
						model.add(sub, pred, obj);
					}
    			}
    		}
    	}
    	
    	return model;
    }
	
	public static boolean annotateMapFile(File file) {
		boolean bSuccess = true;
    	try {
    		SampleSubjectMapper mapper = new SampleSubjectMapper(file);
        	bSuccess = commitRows(mapper.createRows(), mapper.toString(), 
        			file.getName(), "Sample", true);
    	} catch (Exception e) {
	        e.printStackTrace();
    		AnnotationLog.printException(e, file.getName());
    		return false;
		}
		
		return bSuccess;
	}
	
	public static boolean annotateStudyIdFile(File file) {
		boolean bSuccess = true;
    	try {

    		StudyGenerator studyGenerator = new StudyGenerator(file);
        	bSuccess = commitRows(studyGenerator.createRows(), studyGenerator.toString(), 
        			file.getName(), "Study", true);        	
        	
/*        	studyGenerator = new StudyGenerator(file);
        	bSuccess = commitRows(studyGenerator.createAgentRows(), studyGenerator.toString(), 
        			file.getName(), "Agent", true);
        	
        	studyGenerator = new StudyGenerator(file);
        	bSuccess = commitRows(studyGenerator.createInstitutionRows(), studyGenerator.toString(), 
        			file.getName(), "Agent", true);
        			*/
    	} catch (Exception e) {
    		AnnotationLog.printException(e, file.getName());
    		return false;
		}
    	
    	try {
        	AgentGenerator agentGenerator = new AgentGenerator(file);
        	bSuccess = commitRows(agentGenerator.createRows(), agentGenerator.toString(), 
        			file.getName(), "Agent", true);
        	
    	} catch (Exception e) {
    		AnnotationLog.printException(e, file.getName());
    		return false;
		}
		
		return bSuccess;
	}
	
	public static boolean annotateSampleIdFile(File file) {
		boolean bSuccess = true;
    	try {
    		SampleGenerator sampleGenerator = new SampleGenerator(file);
        	bSuccess = commitRows(sampleGenerator.createRows(), sampleGenerator.toString(), 
        			file.getName(), "Sample", true);
        	sampleGenerator = new SampleGenerator(file);
        	bSuccess = commitRows(sampleGenerator.createCollectionRows(), sampleGenerator.toString(), 
        			file.getName(), "SampleCollection", true);
    	} catch (Exception e) {
	    e.printStackTrace();
    		AnnotationLog.printException(e, file.getName());
    		return false;
		}
		
		return bSuccess;
	}
	
	public static boolean annotateSubjectIdFile(File file) {
		boolean bSuccess = true;
    	try {
    		SubjectGenerator subjectGenerator = new SubjectGenerator(file);
        	bSuccess = commitRows(subjectGenerator.createRows(), subjectGenerator.toString(), 
        			file.getName(), "Subject", true);
        	
        	subjectGenerator = new SubjectGenerator(file);
        	bSuccess = commitRows(subjectGenerator.createCohortRows(), subjectGenerator.toString(), 
        			file.getName(), "Cohort", true);
    	} catch (Exception e) {
    		AnnotationLog.printException(e, file.getName());
    		return false;
		}
		
		return bSuccess;
	}
	
	private static boolean commitRows(List<Map<String, Object>> rows, String contentInCSV,
			String fileName, String tableName, boolean toTripleStore) {
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
		Credential cred = Credential.find();
		
		AnnotationLog log = AnnotationLog.find(fileName);
		if (null == log) {
			log = new AnnotationLog();
			log.setFileName(fileName);
		}
        
		if (null == cred) {
			log.resetLog();
			log.addline(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
			log.save();
			
			return false;
		}
		
		LabkeyDataHandler labkeyDataHandler = new LabkeyDataHandler(
				site, cred.getUserName(), cred.getPassword(), path);
		try {
			log.addline(Feedback.println(Feedback.WEB, "The first Row is " + rows.get(0).toString()));
			int nRows = labkeyDataHandler.insertRows(tableName, rows);
			log.addline(Feedback.println(Feedback.WEB, String.format(
					"[OK] %d row(s) have been inserted into the %s table", nRows, tableName)));
		} catch (CommandException e1) {
          	try {
          		labkeyDataHandler.deleteRows(tableName, rows);
				int nRows = labkeyDataHandler.updateRows(tableName, rows);
				log.addline(Feedback.println(Feedback.WEB, String.format(
						"[OK] %d row(s) have been updated into the %s table", nRows, tableName)));
			} catch (CommandException e) {
				log.addline(Feedback.println(Feedback.WEB, "[ERROR] CommitRows inside AutoAnnotator: " + e));
				log.save();
				return false;
			}
		}
		
		if (toTripleStore) {
			DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(
					Collections.getCollectionsName(Collections.METADATA_GRAPH));
			Model model = createModel(rows);
	    	accessor.add(model);
	    	log.addline(Feedback.println(Feedback.WEB, String.format(
					"[OK] %d triple(s) have been committed to triple store", model.size())));
		}
		
		// THIS LINE IS EXCEEDING THE STORAGE CAPABILITY OF ONE CELL IN THE SOLR DATA COLLECTION FOR ANNOTATION LOG
		//log.addline(Feedback.println(Feedback.WEB, String.format(contentInCSV)));
		log.save();
		
		return true;
	}
	
	public static boolean annotateDataAcquisitionFile(File file) {
    	boolean bSuccess = true;
    	try {
        	GeneralGenerator generalGenerator = new GeneralGenerator();
        	Map<String, Object> row = new HashMap<String, Object>();
        	row.put("hasURI", "chear-kb:INS-GENERIC-PHYSICAL-INSTRUMENT");
        	row.put("a", "vstoi:PhysicalInstrument");
        	row.put("rdfs:label", "Generic Physical Instrument");
        	generalGenerator.addRow(row);
        	
        	row = new HashMap<String, Object>();
        	row.put("hasURI", "chear-kb:INS-GENERIC-QUESTIONNAIRE");
        	row.put("a", "chear:Questionnaire");
        	row.put("rdfs:label", "Generic Questionnaire");
        	generalGenerator.addRow(row);
        	bSuccess = commitRows(generalGenerator.getRows(), generalGenerator.toString(), file.getName(), 
        			"Instrument", true);
    		
    		DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        	String startTime = isoFormat.format(new Date());
        	
        	DeploymentGenerator deploymentGenerator = new DeploymentGenerator(file, startTime);
        	bSuccess = commitRows(deploymentGenerator.createRows(), deploymentGenerator.toString(), file.getName(), 
        			"Deployment", true);
        	
        	DataAcquisitionGenerator daGenerator = new DataAcquisitionGenerator(file, startTime);
        	bSuccess = commitRows(daGenerator.createRows(), daGenerator.toString(), file.getName(), 
        			"DataAcquisition", true);
    	} catch (Exception e) {
    		AnnotationLog.printException(e, file.getName());
    		return false;
		}

		return bSuccess;
	}
	
	public static boolean annotateDataAcquisitionSchemaFile(File file) {
        
    	boolean bSuccess = true;
    	try{
	        BufferedReader bufRdr;
	        bufRdr = new BufferedReader(new FileReader(file));
	        String line = null;
	        HashMap<String, String> hm = new HashMap<String, String>();
	        while((line = bufRdr.readLine()) != null) {
	        	hm.put(line.split(",")[0], line.split(",")[1]);
	        }
	        URL url = new URL(hm.get("Data_Dictionary"));
	        System.out.println(url.toString());
	        File dd = new File(file.getName());
	        System.out.println(dd.getAbsoluteFile());
	        FileUtils.copyURLToFile(url, dd); 
	        
	        URL url2 = new URL(hm.get("Code_Mappings"));
	        System.out.println(url2.toString());
	        File cm = new File(file.getName().replace(".csv", "")+"-code-mappings.csv");
	        System.out.println(cm.getAbsoluteFile());
	        FileUtils.copyURLToFile(url2, cm);
	        
	        BufferedReader br = new BufferedReader(new FileReader(cm));
	        String line2 =  null;
	        
	        study_id = hm.get("Study_ID");

	        while((line2 = br.readLine()) != null){
	            String str[] = line2.split(",");
	            codeMappings.put(str[0], str[1]);
	        	}
	        
	        bufRdr.close();
	        br.close();
	//        System.out.println(hm.keySet());
	    	
		    	try {
		    		
		    		DASchemaObjectGenerator dasoGenerator = new DASchemaObjectGenerator(dd);
		    		System.out.println("Calling DASchemaObjectGenerator");
		    		bSuccess = commitRows(dasoGenerator.createRows(), dasoGenerator.toString(), 
		    				file.getName(), "DASchemaObject", true);
		    		
		    		
		    		DASchemaAttrGenerator dasaGenerator = new DASchemaAttrGenerator(dd);
		    		System.out.println("Calling DASchemaAttrGenerator");
		    		bSuccess = commitRows(dasaGenerator.createRows(), dasaGenerator.toString(), 
		    				file.getName(), "DASchemaAttribute", true);
		        	
		        	GeneralGenerator generalGenerator = new GeneralGenerator();

		        	Map<String, Object> row = new HashMap<String, Object>();
		        	row.put("hasURI", "chear-kb:DAS-" + file.getName().replace(".csv",""));
		        	row.put("a", "hasco:DASchema");
		        	row.put("rdfs:label", "Schema for Study " + study_id + " EPI Data Acquisitions");
		        	row.put("rdfs:comment", "");
		        	row.put("hasco:isSchemaOf", "chear-kb:STD-" + study_id);
		        	generalGenerator.addRow(row);
		    		
		        	bSuccess = commitRows(generalGenerator.getRows(), generalGenerator.toString(), file.getName(), 
		        			"DASchema", true);
		    	} catch (Exception e) {
		    		AnnotationLog.printException(e, file.getName());
		    		return false;
				}
	    	} catch (Exception e) {
	    		AnnotationLog.printException(e, file.getName());
	    		return false;
	    	}
    	
		return bSuccess;
	}
	
	private static String getProperDataAcquisitionUri(String fileName) {
		String base_name = FilenameUtils.getBaseName(fileName);
		List<DataAcquisition> da_list = DataAcquisition.findAll();
		for(DataAcquisition dc : da_list){
			String abbrevUri = ValueCellProcessing.replaceNameSpaceEx(dc.getUri());
			String qname = abbrevUri.split(":")[1];
			if(base_name.startsWith(qname)){
				return dc.getUri();
			}
		}
		
		return null;
	}
	
    public static boolean annotateCSVFile(DataFile dataFile) {
    	String file_name = dataFile.getFileName();    	
    	AnnotationLog log = new AnnotationLog();
    	log.setFileName(file_name);
	
		String dc_uri = null;
		String deployment_uri = null;
		String schema_uri = null;
		
		if (null != dataFile) {
			DataAcquisition dataAcquisition = DataAcquisition.findByUri(
					ValueCellProcessing.replacePrefixEx(dataFile.getDataAcquisitionUri()));
			if (null != dataAcquisition) {
				dc_uri = dataAcquisition.getUri();
				deployment_uri = dataAcquisition.getDeploymentUri();
				schema_uri = dataAcquisition.getSchemaUri();
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
    		Iterator<TripleDocument> iterator = results.sparqlResults.values().iterator();
    		if (iterator.hasNext()) {
    			TripleDocument docDeployment = iterator.next();
        		handler = new CSVAnnotationHandler(deployment_uri, 
        				docDeployment.get("platform"), 
        				docDeployment.get("instrument"));
    		} else {
    			log.addline(Feedback.println(Feedback.WEB, String.format(
    					"[ERROR] Could not find the deployment: %s", deployment_uri)));
        		log.save();
        		return false;
    		}
    		
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
    			handler.setDatasetUri(DataFactory.getNextDatasetURI(handler.getDataAcquisitionUri()));
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
	    ParsingResult result = LoadCCSV.playLoadCCSV();
	    log.addline(result.getMessage());
		log.save();
		if(result.getStatus() == 0){
			dataFile.setDatasetUri(handler.getDatasetUri());
			return true;
		}
	    
	    return false;
	}
    
    private static String createPreamble(CSVAnnotationHandler handler, 
    									 String schema_uri) throws Exception {
	        //System.out.println("CREATING PREAMBLE FOR " + schema_uri);
		String preamble = Downloads.FRAG_START_PREAMBLE;
		preamble += NameSpaces.getInstance().printTurtleNameSpaceList();
		preamble += "\n";

		//Insert KB    	
		preamble += Downloads.FRAG_KB_PART1;
		preamble += Play.application().configuration().getString("hadatac.console.kb"); 
		preamble += Downloads.FRAG_KB_PART2;

		try {
			//Insert Data Set
			preamble += "<" + handler.getDatasetUri() + ">";
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
                                //System.out.println("ATTRIBUTE: pos:" + i + " entity:" + entity + " attribute:" + attrib + " unit:" + unit);  

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
			throw new Exception(preamble + "\n" + e.getMessage());
		}

		preamble += Downloads.FRAG_END_PREAMBLE;
		
		return preamble;
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result checkAnnotationLog(String file_name) {
    	AnnotationLog log = AnnotationLog.find(file_name);
    	if (null == log) {
    		return ok(annotation_log.render(Feedback.print(Feedback.WEB, "")));
    	}
    	else {
    		return ok(annotation_log.render(Feedback.print(Feedback.WEB, log.getLog())));
    	}
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result moveDataFile(String file_name) {			
		final SysUser user = AuthApplication.getLocalUser(session());
		DataFile dataFile = null;
		if (user.isDataManager()) {
			dataFile = DataFile.findByName(null, file_name);
		}
		else {
			dataFile = DataFile.findByName(user.getEmail(), file_name);
		}
		if (null == dataFile) {
			return badRequest("You do NOT have the permission to operate this file!");
		}
		
		AnnotationLog log = new AnnotationLog(file_name);
		log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Moved file %s to unprocessed folder", file_name)));
		log.save();
		
		Measurement.delete(dataFile.getDatasetUri());
		List<DataAcquisition> dataAcquisitions = DataAcquisition.findAll();
		for (DataAcquisition da : dataAcquisitions) {
			if (da.containsDataset(dataFile.getDatasetUri())) {
				da.setNumberDataPoints(Measurement.getNumByDataAcquisition(da));
				da.save();
			}
		}
		dataFile.delete();
		dataFile.setProcessStatus(false);
		dataFile.save();
		
		String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		File destFolder = new File(path_unproc);
		if (!destFolder.exists()){
			destFolder.mkdirs();
	    }
		File file = new File(path_proc + "/" + file_name);
		file.renameTo(new File(destFolder + "/" + file_name));
		
		return redirect(routes.AutoAnnotator.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result deleteDataFile(String file_name, boolean isProcessed) {
		final SysUser user = AuthApplication.getLocalUser(session());
		DataFile dataFile = null;
		if (user.isDataManager()) {
			dataFile = DataFile.findByName(null, file_name);
		}
		else {
			dataFile = DataFile.findByName(user.getEmail(), file_name);
		}
		if (null == dataFile) {
			return badRequest("You do NOT have the permission to operate this file!");
		}
		
    	AnnotationLog.delete(file_name);
    	Measurement.delete(dataFile.getDatasetUri());
		List<DataAcquisition> dataAcquisitions = DataAcquisition.findAll();
		for (DataAcquisition da : dataAcquisitions) {
			if (da.containsDataset(dataFile.getDatasetUri())) {
				da.setNumberDataPoints(Measurement.getNumByDataAcquisition(da));
				da.save();
			}
		}
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
    		String resumableType,
    		String resumableIdentifier,
    		String resumableFilename,
    		String resumableRelativePath) {
    	if (ResumableUpload.uploadFileByChunking(request(), 
    			ConfigProp.getPropertyValue("autoccsv.config", "path_unproc"))) {
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
    		String resumableType,
    		String resumableIdentifier,
    		String resumableFilename,
    		String resumableRelativePath) {
    	if (ResumableUpload.postUploadFileByChunking(request(), 
    			ConfigProp.getPropertyValue("autoccsv.config", "path_unproc"))) {
    		DataFile dataFile = new DataFile();
    		dataFile.setFileName(resumableFilename);
    		dataFile.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
    		dataFile.setProcessStatus(false);
    		dataFile.setUploadTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
    		String dataAcquisitionUri = getProperDataAcquisitionUri(resumableFilename);
    		dataFile.setDataAcquisitionUri(dataAcquisitionUri == null ? "" : dataAcquisitionUri);
    		dataFile.save();
    		return(ok("Upload finished"));
        } else {
            return(ok("Upload"));
        }
    }
}

