package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.routes;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.http.ResumableUpload;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.LabKeyLoginForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.console.views.html.*;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.twirl.api.Html;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser;
import play.mvc.Http.MultipartFormData.FilePart;

public class AutoAnnotator extends Controller {
	
	@Inject
	FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index() {		
		final SysUser user = AuthApplication.getLocalUser(session());

		List<DataFile> proc_files = null;
		List<DataFile> unproc_files = null;

		String path_proc = ConfigProp.getPathProc();
		String path_unproc = ConfigProp.getPathUnproc();

		if (user.isDataManager()) {
			proc_files = DataFile.findAll(DataFile.PROCESSED);
			unproc_files = DataFile.findAll(DataFile.UNPROCESSED);
			DataFile.includeUnrecognizedFiles(path_unproc, unproc_files);
			DataFile.includeUnrecognizedFiles(path_proc, proc_files);
		} else {
			proc_files = DataFile.find(user.getEmail(), DataFile.PROCESSED);
			unproc_files = DataFile.find(user.getEmail(), DataFile.UNPROCESSED);
		}

		DataFile.filterNonexistedFiles(path_proc, proc_files);
		DataFile.filterNonexistedFiles(path_unproc, unproc_files);

		boolean bStarted = false;
		if (ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("on")) {
			bStarted = true;
		}

		return ok(auto_ccsv.render(unproc_files, proc_files, bStarted, user.isDataManager()));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex() {
		return index();
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result assignFileOwner(String ownerEmail, String selectedFile) {	
		return ok(assignOption.render(User.getUserEmails(),
				routes.AutoAnnotator.processOwnerForm(ownerEmail, selectedFile),
				"Owner", 
				"Selected File", 
				selectedFile));
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result postAssignFileOwner(String ownerEmail, String selectedFile) {
		return assignFileOwner(ownerEmail, selectedFile);
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result processOwnerForm(String ownerEmail, String selectedFile) {
		Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest();
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
				file = new DataFile(selectedFile);
				file.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
				file.setStatus(DataFile.UNPROCESSED);
				file.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
			}
			file.setOwnerEmail(data.getOption());
			file.save();
			return redirect(routes.AutoAnnotator.index());
		}
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result assignDataAcquisition(String dataAcquisitionUri, String selectedFile) {
		List<String> dataAcquisitionURIs = new ArrayList<String>();
		DataAcquisition.findAll().forEach((da) -> dataAcquisitionURIs.add(
				URIUtils.replaceNameSpaceEx(da.getUri())));

		return ok(assignOption.render(dataAcquisitionURIs,
				routes.AutoAnnotator.processDataAcquisitionForm(dataAcquisitionUri, selectedFile),
				"Data Acquisition",
				"Selected File",
				selectedFile));
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result postAssignDataAcquisition(String dataAcquisitionUri, String selectedFile) {
		return assignDataAcquisition(dataAcquisitionUri, selectedFile);
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result processDataAcquisitionForm(String dataAcquisitionUri, String selectedFile) {
		Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest();
		AssignOptionForm data = form.get();

		List<String> dataAcquisitionURIs = new ArrayList<String>();
		DataAcquisition.findAll().forEach((da) -> dataAcquisitionURIs.add(
				URIUtils.replaceNameSpaceEx(da.getUri())));

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
				file = new DataFile(selectedFile);
				file.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
				file.setStatus(DataFile.UNPROCESSED);
				file.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
			}
			file.setDataAcquisitionUri(URIUtils.replacePrefixEx(data.getOption()));
			file.save();
			return redirect(routes.AutoAnnotator.index());
		}
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result toggleAutoAnnotator() {
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
	public Result downloadTemplates() {
		return ok(download_templates.render());
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postDownloadTemplates() {
		return postDownloadTemplates();
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result setLabKeyCredentials() {
		return ok(syncLabkey.render("init", routes.AutoAnnotator.
				postSetLabKeyCredentials().url(), ""));
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result postSetLabKeyCredentials() {
		Form<LabKeyLoginForm> form = formFactory.form(LabKeyLoginForm.class).bindFromRequest();
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

		return ok(main.render("Results", "", 
				new Html("<h3>Your provided credentials are valid and saved!</h3>")));
	}

	private static String getProperDataAcquisitionUri(String fileName) {
		String base_name = FilenameUtils.getBaseName(fileName);
		List<DataAcquisition> da_list = DataAcquisition.findAll();
		for(DataAcquisition dc : da_list){
			String abbrevUri = URIUtils.replaceNameSpaceEx(dc.getUri());
			String qname = abbrevUri.split(":")[1];
			if(base_name.startsWith(qname)){
				return dc.getUri();
			}
		}
		return null;
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result checkAnnotationLog(String file_name) {
		AnnotationLog log = AnnotationLog.find(file_name);
		if (null == log) {
			return ok(annotation_log.render(Feedback.print(Feedback.WEB, ""), routes.AutoAnnotator.index().url()));
		}
		else {
			return ok(annotation_log.render(Feedback.print(Feedback.WEB, log.getLog()), routes.AutoAnnotator.index().url()));
		}
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result moveDataFile(String file_name) {			
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
		dataFile.setStatus(DataFile.UNPROCESSED);
		dataFile.save();

		String path_proc = ConfigProp.getPathProc();
		String path_unproc = ConfigProp.getPathUnproc();
		File destFolder = new File(path_unproc);
		if (!destFolder.exists()){
			destFolder.mkdirs();
		}
		File file = new File(path_proc + "/" + file_name);
		file.renameTo(new File(destFolder + "/" + file_name));
		deleteAddedTriples(new File(destFolder + "/" + file_name));

		return redirect(routes.AutoAnnotator.index());
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static void deleteAddedTriples(File file){
		System.out.println("Deleting the added triples from the moving file ...");
		String file_name = file.getName();
		if (file_name.startsWith("SDD")) {
			
			List<String> result = AnnotationWorker.getPopulatedSDDUris(file);
			
			for (String str:result){
				try{
					String query = "";
					query += NameSpaces.getInstance().printSparqlNameSpaceList();
					query += "DELETE WHERE {  ";
					query += str + " ?p ?o . ";
					query += "}  ";				
//					System.out.println(query);
					UpdateRequest request = UpdateFactory.create(query);
					UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
					processor.execute();
				} catch (Exception e) {
					System.out.println(str + " s triple can not be deleted.");
				}

				try{
					String query2 = "";
					query2 += NameSpaces.getInstance().printSparqlNameSpaceList();
					query2 += "DELETE WHERE {  ";
					query2 += "?s ?p " + str + " . ";
					query2 += "}  ";
//					System.out.println(query2);
					UpdateRequest request2 = UpdateFactory.create(query2);
					UpdateProcessor processor2 = UpdateExecutionFactory.createRemote(request2, Collections.getCollectionsName(Collections.METADATA_UPDATE));
					processor2.execute();
				} catch (Exception e) {
					System.out.println(str + " s triple can not be deleted.");
				}
			}
		}
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result downloadDataFile(String file_name, boolean isProcessed) {		
		String path = ""; 
		if(isProcessed){
			path = ConfigProp.getPathProc();
		} else {
			path = ConfigProp.getPathUnproc();
		}
		return ok(new File(path + "/" + file_name));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result deleteDataFile(String file_name, boolean isProcessed) {
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
			path = ConfigProp.getPathProc();
		}
		else{
			path = ConfigProp.getPathUnproc();
		}

		File file = new File(path + "/" + file_name);
		deleteAddedTriples(file);
		file.delete();

		return redirect(routes.AutoAnnotator.index());
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	@BodyParser.Of(value = BodyParser.MultipartFormData.class)
	public Result uploadDataFile(String oper) {
		String path = ConfigProp.getPathUnproc();
		for(FilePart filePart : request().body().asMultipartFormData().getFiles()) {
			if (filePart != null) {
				File file = (File)filePart.getFile();
				File newFile = new File(path + "/" + filePart.getFilename());
				InputStream isFile;
				try {
					isFile = new FileInputStream(file);
					byte[] byteFile;
					byteFile = IOUtils.toByteArray(isFile);
					FileUtils.writeByteArrayToFile(newFile, byteFile);
					isFile.close();

					DataFile dataFile = new DataFile(filePart.getFilename());
					dataFile.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
					dataFile.setStatus(DataFile.UNPROCESSED);
					dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
					dataFile.save();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return redirect(routes.AutoAnnotator.index());
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result uploadDataFileByChunking(
			String resumableChunkNumber,
			String resumableChunkSize, 
			String resumableCurrentChunkSize,
			String resumableTotalSize,
			String resumableType,
			String resumableIdentifier,
			String resumableFilename,
			String resumableRelativePath) {
		if (ResumableUpload.uploadFileByChunking(request(), 
				ConfigProp.getPathUnproc())) {
			return ok("Uploaded."); //This Chunk has been Uploaded.
		} else {
			return status(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postUploadDataFileByChunking(
			String resumableChunkNumber, 
			String resumableChunkSize, 
			String resumableCurrentChunkSize,
			String resumableTotalSize,
			String resumableType,
			String resumableIdentifier,
			String resumableFilename,
			String resumableRelativePath) {
		if (ResumableUpload.postUploadFileByChunking(request(), 
				ConfigProp.getPathUnproc())) {
			DataFile dataFile = new DataFile(resumableFilename);
			dataFile.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
			dataFile.setStatus(DataFile.UNPROCESSED);
			dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
			String dataAcquisitionUri = getProperDataAcquisitionUri(resumableFilename);
			dataFile.setDataAcquisitionUri(dataAcquisitionUri == null ? "" : dataAcquisitionUri);
			dataFile.save();
			return(ok("Upload finished"));
		} else {
			return(ok("Upload"));
		}
	}
}

