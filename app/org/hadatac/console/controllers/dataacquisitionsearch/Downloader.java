package org.hadatac.console.controllers.dataacquisitionsearch;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionsearch.routes;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.dataacquisitionsearch.*;
import org.hadatac.console.views.html.annotator.annotation_log;
import org.hadatac.console.views.html.annotator.assignOption;
import org.hadatac.entity.pojo.*;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.utils.FileManager;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;


public class Downloader extends Controller {

    public static final String ALIGNMENT_SUBJECT = "SUBJECT";
    public static final String ALIGNMENT_TIME = "TIME";

    @Inject
    FormFactory formFactory;
    @Inject
    Application application;


    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        List<DataFile> files = null;

        String path = ConfigProp.getPathWorking();

        if (user.isDataManager()) {
            files = DataFile.findByStatus(DataFile.CREATED);
            files.addAll(DataFile.findByStatus(DataFile.CREATING));
        } else {
            files = DataFile.find(user.getEmail(), DataFile.CREATED);
            files.addAll(DataFile.find(user.getEmail(), DataFile.CREATING));
        }

        DataFile.filterNonexistedFiles(path, files);

        return ok(downloader.render(files, user.isDataManager(),application.getUserEmail(request)));
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(Http.Request request) {
        return index(request);
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result downloadDataFile(String fileId,Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());

        if (null == dataFile) {
            return badRequest("You do NOT have the permission to download this file! fileId: " + fileId + "   user.email: " + user.getEmail());
        }

        return ok(new File(dataFile.getAbsolutePath()));
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result deleteDataFile(String fileId,Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
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

    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result assignFileOwner(String ownerEmail, String fileId, Http.Request request) {
        return ok(assignOption.render(User.getUserEmails(),
                routes.Downloader.processOwnerForm(ownerEmail, fileId),
                "Owner",
                "Selected File",
                fileId,application.getUserEmail(request)));
    }

    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postAssignFileOwner(String ownerEmail, String fileId,Http.Request request) {
        return assignFileOwner(ownerEmail, fileId,request);
    }

    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result processOwnerForm(String ownerEmail, String fileId,Http.Request request) {
        Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest(request);
        AssignOptionForm data = form.get();

        if (form.hasErrors()) {
            System.out.println("HAS ERRORS");
            return badRequest(assignOption.render(User.getUserEmails(),
                    routes.Downloader.processOwnerForm(ownerEmail, fileId),
                    "Owner",
                    "Selected File",
                    fileId,application.getUserEmail(request)));
        } else {
            DataFile file = DataFile.findByIdAndEmail(fileId, ownerEmail);
            if (file != null) {
                file.setOwnerEmail(data.getOption());
                file.save();
            }

            return redirect(routes.Downloader.index());
        }
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result checkAnnotationLog(String fileId,Http.Request request) {
        DataFile dataFile = DataFile.findById(fileId);
        if (DataFile.findById(fileId) == null) {
            return ok(annotation_log.render(Feedback.print(Feedback.WEB,""),
                    routes.Downloader.index().url(),application.getUserEmail(request)));
        }
        return ok(annotation_log.render(Feedback.print(Feedback.WEB,
                DataFile.findById(fileId).getLog()),
                routes.Downloader.index().url(),application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result checkCompletion(String fileId) {

        String ALIGNMENT = "object_alignment";
        String SUMMARIZATION = "summary";
        String SUBGROUP_SUMMARIZATION = "subgroup";

        //System.out.println("checkCompletion: [" + fileId + "]");

        Map<String, Object> result = new HashMap<String, Object>();

        if ( fileId == null ) {
            return ok(Json.toJson(result));
        }
        if (fileId.indexOf(ALIGNMENT) < 0 && fileId.indexOf(SUMMARIZATION) < 0 && fileId.indexOf(SUBGROUP_SUMMARIZATION) < 0) {
            return ok(Json.toJson(result));
        }

        if (fileId.indexOf(ALIGNMENT) >= 0 && fileId.startsWith(ALIGNMENT) == false) {
            fileId = fileId.substring(fileId.indexOf(ALIGNMENT));
        }

        if (fileId.indexOf(SUMMARIZATION) >= 0 && fileId.startsWith(SUMMARIZATION) == false) {
            fileId = fileId.substring(fileId.indexOf(SUMMARIZATION));
        }

        if (fileId.indexOf(SUBGROUP_SUMMARIZATION) >= 0 && fileId.startsWith(SUBGROUP_SUMMARIZATION) == false) {
            fileId = fileId.substring(fileId.indexOf(SUBGROUP_SUMMARIZATION));
        }

        //System.out.println("checkCompletion after adjustment: [" + fileId + "]");

        DataFile dataFile = DataFile.findByNameAndStatus(fileId, DataFile.CREATING);
        if ( dataFile == null ) {
            dataFile = DataFile.findByNameAndStatus(fileId, DataFile.CREATED);
        }

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

    public static int generateCSVFileBySubjectAlignment(String ownerUri, String facets, String ownerEmail,
                                                        String summaryType, String categoricalOption, boolean renameFiles,
                                                        boolean keepSameValue, ColumnMapping columnMapping) {

        System.out.println("generateCSVFileBySubjectAlignment: facets=[" + facets + "]");

        System.out.println("Invoked CSV generation with object alignment ...");
        System.out.println("Categorical option: [" + categoricalOption + "]");
        Date date = new Date();
        String fileName = null;
        if (summaryType.equals(Measurement.SUMMARY_TYPE_NONE)) {
            if(renameFiles) {
                //fileName = "DataSet_object_alignment_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + ".csv";
                fileName = "object_alignment_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + ".csv";            }
            else {
                fileName = "object_alignment_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + ".csv";
            }
        } else {
            fileName = "subgroup_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + ".csv";
        }

        // will use the user email address as the directory
        DataFile dataFile = DataFile.create(fileName, ConfigProp.getPathWorking()+ "/" + DataFile.DS_GENERATION + "/"+ ownerEmail, ownerEmail, DataFile.CREATING);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
        dataFile.getLogger().addLine(Feedback.println(Feedback.WEB, "Facets: " + facets));
        dataFile.save();
        System.out.println("Created download " + fileName);

        String absolutePath = dataFile.getAbsolutePath();
        System.out.println("downloaded file... absolute path = " + absolutePath);
        File file = new File(absolutePath);

        Measurement.outputAsCSVBySubjectAlignment(ownerUri, facets, file, dataFile.getId(), summaryType, categoricalOption, renameFiles, keepSameValue, columnMapping);
        System.out.println("download finished, CSV files are generated...");

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

    public static int generateCSVFileBySummarization(String ownerUri, String facets, String ownerEmail,
                                                        String summaryType, String categoricalOption,
                                                        ColumnMapping columnMapping) {

        System.out.println("generateCSVFileBySummarization: facets=[" + facets + "]");


        System.out.println("Invoked CSV generation with study summarization ...");
        System.out.println("Summary type: [" + summaryType + "]");
        System.out.println("Categorical option: [" + categoricalOption + "]");
        Date date = new Date();
        String fileName = "summary_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + ".csv";

        // will use the user email address as the directory
        DataFile dataFile = DataFile.create(fileName, ConfigProp.getPathWorking()+ "/" + DataFile.DS_GENERATION + "/"+ ownerEmail, ownerEmail, DataFile.CREATING);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
        dataFile.getLogger().addLine(Feedback.println(Feedback.WEB, "Facets: " + facets));
        dataFile.save();
        System.out.println("Created summary " + fileName);

        String absolutePath = dataFile.getAbsolutePath();
        System.out.println("Generated summary file... absolute path = " + absolutePath);
        File file = new File(absolutePath);

        System.out.println("Calling summarization...");
        Measurement.outputAsCSVBySummarization(ownerUri, facets, file, dataFile.getId(), summaryType, categoricalOption, columnMapping);
        System.out.println("download finished, CSV files are generated...");

        return 0;
    }

}
