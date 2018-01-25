package org.hadatac.console.controllers.dataacquisitionsearch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionsearch.routes;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.dataacquisitionsearch.*;
import org.hadatac.console.views.html.annotator.annotation_log;
import org.hadatac.console.views.html.annotator.assignOption;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.User;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;


public class Downloader extends Controller {
	
	@Inject
	FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index() {		
		final SysUser user = AuthApplication.getLocalUser(session());

		List<DataFile> files = null;

		String path = ConfigProp.getPathDownload();

		if (user.isDataManager()) {
			files = DataFile.findAll(DataFile.CREATED);
			files.addAll(DataFile.findAll(DataFile.CREATING));
		} else {
			files = DataFile.find(user.getEmail(), DataFile.CREATED);
			files.addAll(DataFile.find(user.getEmail(), DataFile.CREATING));
		}

		DataFile.filterNonexistedFiles(path, files);

		return ok(downloader.render(files, user.isDataManager()));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex() {
		return index();
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result downloadDataFile(String file_name) {
		String path = ConfigProp.getPathDownload();
		return ok(new File(path + "/" + file_name));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result deleteDataFile(String file_name) {
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
		dataFile.delete();

		String path = ConfigProp.getPathDownload();
		File file = new File(path + "/" + file_name);
		file.delete();

		return redirect(routes.Downloader.index());
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public Result assignFileOwner(String ownerEmail, String selectedFile) {	
		return ok(assignOption.render(User.getUserEmails(),
				routes.Downloader.processOwnerForm(ownerEmail, selectedFile),
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
					routes.Downloader.processOwnerForm(ownerEmail, selectedFile),
					"Owner",
					"Selected File",
					selectedFile));
		} else {
			DataFile file = DataFile.findByName(ownerEmail, selectedFile);
			if (file == null) {
				file = new DataFile(selectedFile);
				file.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
				file.setStatus(DataFile.CREATING);
				file.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
			}
			file.setOwnerEmail(data.getOption());
			file.save();
			return redirect(routes.Downloader.index());
		}
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result checkAnnotationLog(String file_name) {
		AnnotationLog log = AnnotationLog.find(file_name);
		if (null == log) {
			return ok(annotation_log.render(Feedback.print(Feedback.WEB, "")));
		}
		else {
			return ok(annotation_log.render(Feedback.print(Feedback.WEB, log.getLog())));
		}
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result checkCompletion(String file_name) {
		DataFile dataFile = DataFile.findByName(null, file_name);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("CompletionPercentage", dataFile.getCompletionPercentage());
		result.put("Status", dataFile.getStatus());
		result.put("CompletionTime", dataFile.getCompletionTime());
		
		return ok(Json.toJson(result));
	}
	
	public static int generateCSVFile(List<Measurement> measurements, 
			String facets, List<String> selectedFields, String ownerEmail) {
		Date date = new Date();
		String fileName = "download_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + ".csv";
		File file = new File(ConfigProp.getPathDownload() + "/" + fileName);
		
		AnnotationLog log = new AnnotationLog(fileName);
		log.addline(Feedback.println(Feedback.WEB, "Facets: " + facets));
		log.addline(Feedback.println(Feedback.WEB, "Selected Fields: " + selectedFields));
		log.save();
		
		DataFile dataFile = new DataFile(fileName);
		dataFile.setOwnerEmail(ownerEmail);
		dataFile.setStatus(DataFile.CREATING);
		dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
		dataFile.save();
    	
    	Measurement.outputAsCSV(measurements, selectedFields, file, dataFile);
    	System.out.println("Generated CSV files ...");
		
		return 0;
    }
}

