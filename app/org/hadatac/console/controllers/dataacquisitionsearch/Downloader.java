package org.hadatac.console.controllers.dataacquisitionsearch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionsearch.routes;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.dataacquisitionsearch.*;
import org.hadatac.console.views.html.annotator.annotation_log;
import org.hadatac.console.views.html.annotator.assignOption;
import org.hadatac.entity.pojo.Alignment;
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

	public static final String ALIGNMENT_SUBJECT = "SUBJECT";
	public static final String ALIGNMENT_TIME = "TIME";
	
    @Inject
    FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index() {		
        final SysUser user = AuthApplication.getLocalUser(session());

        List<DataFile> files = null;

        String path = ConfigProp.getPathDownload();

        if (user.isDataManager()) {
            files = DataFile.findByStatus(DataFile.CREATED);
            files.addAll(DataFile.findByStatus(DataFile.CREATING));
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
    public Result downloadDataFile(String fileId) {
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        
        if (null == dataFile) {
            return badRequest("You do NOT have the permission to download this file! fileId: " + fileId + "   user.email: " + user.getEmail());
        }
        
        return ok(new File(dataFile.getAbsolutePath()));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result deleteDataFile(String fileId) {
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findById(fileId);
        } else {
            dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        }
        
        if (null == dataFile) {
            return badRequest("You do NOT have the permission to operate this file!");
        }

        dataFile.setStatus(DataFile.DELETED);
        dataFile.delete();

        File file = new File(dataFile.getAbsolutePath());
        file.delete();

        return redirect(routes.Downloader.index());
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result assignFileOwner(String ownerEmail, String fileId) {	
        return ok(assignOption.render(User.getUserEmails(),
                routes.Downloader.processOwnerForm(ownerEmail, fileId),
                "Owner", 
                "Selected File", 
                fileId));
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postAssignFileOwner(String ownerEmail, String fileId) {
        return assignFileOwner(ownerEmail, fileId);
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result processOwnerForm(String ownerEmail, String fileId) {
        Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest();
        AssignOptionForm data = form.get();

        if (form.hasErrors()) {
            System.out.println("HAS ERRORS");
            return badRequest(assignOption.render(User.getUserEmails(),
                    routes.Downloader.processOwnerForm(ownerEmail, fileId),
                    "Owner",
                    "Selected File",
                    fileId));
        } else {
            DataFile file = DataFile.findByIdAndEmail(fileId, ownerEmail);
            if (file != null) {
                file.setOwnerEmail(data.getOption());
                file.save();
            }
            
            return redirect(routes.Downloader.index());
        }
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result checkAnnotationLog(String fileId) {
        DataFile dataFile = DataFile.findById(fileId);
    	if (DataFile.findById(fileId) == null) {
        	return ok(annotation_log.render(Feedback.print(Feedback.WEB,""), 
                    routes.Downloader.index().url()));
    	}
    	return ok(annotation_log.render(Feedback.print(Feedback.WEB, 
                DataFile.findById(fileId).getLog()), 
                routes.Downloader.index().url()));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result checkCompletion(String fileId) {
        Map<String, Object> result = new HashMap<String, Object>();

        DataFile dataFile = DataFile.findById(fileId);
        if (dataFile != null) {
            result.put("CompletionPercentage", dataFile.getCompletionPercentage());
            result.put("Status", dataFile.getStatus());
            result.put("CompletionTime", dataFile.getCompletionTime());
        } else {
            result.put("CompletionPercentage", "");
            result.put("Status", "");
            result.put("CompletionTime", "");
        }

        return ok(Json.toJson(result));
    }

    public static int generateCSVFile(List<Measurement> measurements, 
            String facets, List<String> selectedFields, String ownerEmail) {
        Date date = new Date();
        String fileName = "download_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + ".csv";

        DataFile dataFile = DataFile.create(fileName, "", ownerEmail, DataFile.CREATING);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
        dataFile.getLogger().addLine(Feedback.println(Feedback.WEB, "Facets: " + facets));
        dataFile.getLogger().addLine(Feedback.println(Feedback.WEB, "Selected Fields: " + selectedFields));
        dataFile.save();
        
        File file = new File(dataFile.getAbsolutePath());

        Measurement.outputAsCSV(measurements, selectedFields, file, dataFile.getId());
        System.out.println("Generated CSV files ...");

        return 0;
    }

    public static int generateCSVFileBySubjectAlignment(List<Measurement> measurements, 
		  String facets, String ownerEmail, String categoricalOption) {
        System.out.println("Invoked CSV generation with object alignment ...");
        System.out.println("Categorical option: [" + categoricalOption + "]");
        Date date = new Date();
        String fileName = "object_alignment_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + ".csv";

        DataFile dataFile = DataFile.create(fileName, "", ownerEmail, DataFile.CREATING);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
        dataFile.getLogger().addLine(Feedback.println(Feedback.WEB, "Facets: " + facets));
        dataFile.save();
        System.out.println("Created download " + fileName);
        
        File file = new File(dataFile.getAbsolutePath());

        Measurement.outputAsCSVBySubjectAlignment(measurements, file, dataFile.getId(), categoricalOption);
        System.out.println("Generated CSV files ...");

        return 0;
    }

    public static int generateCSVFileByTimeAlignment(List<Measurement> measurements, 
		  String facets, String ownerEmail, String categoricalOption, String timeResolution) {
        System.out.println("Invoked CSV generation with timestamp alignment ...");
        System.out.println("Categorical option: [" + categoricalOption + "]");
        System.out.println("TimeResolution option: [" + timeResolution + "]");
        Date date = new Date();
        String fileName = "time_alignment" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + ".csv";

        DataFile dataFile = DataFile.create(fileName, "", ownerEmail, DataFile.CREATING);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
        dataFile.getLogger().addLine(Feedback.println(Feedback.WEB, "Facets: " + facets));
        dataFile.save();
        System.out.println("Created download " + fileName);
        
        File file = new File(dataFile.getAbsolutePath());

        Measurement.outputAsCSVByTimeAlignment(measurements, file, dataFile.getId(), categoricalOption, timeResolution);
        System.out.println("Generated CSV files ...");

        return 0;
    }
}

