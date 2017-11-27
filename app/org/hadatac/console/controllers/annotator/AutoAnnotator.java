package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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
import org.hadatac.console.views.html.*;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.State;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.twirl.api.Html;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser;
import play.mvc.Http.MultipartFormData.FilePart;

public class AutoAnnotator extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index() {		
		final SysUser user = AuthApplication.getLocalUser(session());

		List<DataFile> proc_files = null;
		List<DataFile> unproc_files = null;

		String path_proc = ConfigProp.getPathProc();
		String path_unproc = ConfigProp.getPathUnproc();

		if (user.isDataManager()) {
			proc_files = DataFile.findAll(State.PROCESSED);
			unproc_files = DataFile.findAll(State.UNPROCESSED);
			DataFile.includeUnrecognizedFiles(path_unproc, unproc_files);
			DataFile.includeUnrecognizedFiles(path_proc, proc_files);
		} else {
			proc_files = DataFile.find(user.getEmail(), State.PROCESSED);
			unproc_files = DataFile.find(user.getEmail(), State.UNPROCESSED);
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

		return ok(main.render("Results", "", 
				new Html("<h3>Your provided credentials are valid and saved!</h3>")));
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

		String path_proc = ConfigProp.getPathProc();
		String path_unproc = ConfigProp.getPathUnproc();
		File destFolder = new File(path_unproc);
		if (!destFolder.exists()){
			destFolder.mkdirs();
		}
		File file = new File(path_proc + "/" + file_name);
		file.renameTo(new File(destFolder + "/" + file_name));
		deleteAddedTriples(file);

		return redirect(routes.AutoAnnotator.index());
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static void deleteAddedTriples(File file){
		System.out.println("Deleting the added triples from the moving file ...");
		// use the new function to reverse the triple generation
		/**
		 * Model model = createModel(rows);
            Model defaultModel = accessor.getModel();
            defaultModel.remove(model);
		 */
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result downloadDataFile(String file_name, boolean isProcessed) {		
		String path = ""; 
		if(isProcessed){
			path = ConfigProp.getPathProc();
		} else {
			path = ConfigProp.getPathUnproc();
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
			path = ConfigProp.getPathProc();
		}
		else{
			path = ConfigProp.getPathUnproc();
		}

		File file = new File(path + "/" + file_name);
		file.delete();

		return redirect(routes.AutoAnnotator.index());
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	@BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 500 * 1024 * 1024)
	public static Result uploadDataFile(String oper) {
		String path = ConfigProp.getPathUnproc();

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
				ConfigProp.getPathUnproc())) {
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
				ConfigProp.getPathUnproc())) {
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

