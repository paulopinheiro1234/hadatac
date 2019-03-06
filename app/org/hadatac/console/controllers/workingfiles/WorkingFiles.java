package org.hadatac.console.controllers.workingfiles;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;

import org.apache.http.HttpStatus;

import org.hadatac.entity.pojo.Credential;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.http.ResumableUpload;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.LabKeyLoginForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.console.views.html.workingfiles.*;
import org.hadatac.data.loader.CSVRecordFile;
import org.hadatac.data.loader.GeneratorChain;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.data.loader.SpreadsheetRecordFile;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.twirl.api.Html;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class WorkingFiles extends Controller {

    @Inject
    FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir) {        
        final SysUser user = AuthApplication.getLocalUser(session());

        List<String> folders = null;
        List<DataFile> wkFiles = null;

        String pathWorking = ConfigProp.getPathWorking();

        if (user.isDataManager()) {
            folders = DataFile.findAllFolders(dir, DataFile.WORKING);
        	wkFiles = DataFile.findInDir(dir, DataFile.WORKING);
            if (dir.equals("/")) {
            	DataFile.includeUnrecognizedFiles(pathWorking, wkFiles);
            }
        	DataFile.includeUnrecognizedFiles(pathWorking, wkFiles);
        } else {
            folders = DataFile.findFolders(dir, user.getEmail());
            wkFiles = DataFile.findInDir(dir, user.getEmail(), DataFile.WORKING);
        }

        DataFile.filterNonexistedFiles(pathWorking, wkFiles);

        wkFiles.sort(new Comparator<DataFile>() {
            @Override
            public int compare(DataFile d1, DataFile d2) {
                return d1.getFileName().compareTo(d2.getFileName());
            }
        });

        return ok(workingFiles.render(dir, folders, wkFiles, user.isDataManager()));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir) {
        return index(dir);
    }
    
    /*
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result assignFileOwner(String dir, String ownerEmail, String selectedFile) {	
        return ok(workingFiles.render(User.getUserEmails(), routes.WorkingFiles.processOwnerForm(dir, ownerEmail, selectedFile), "Owner", "Selected File", selectedFile));
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postAssignFileOwner(String dir, String ownerEmail, String selectedFile) {
        return assignFileOwner(dir, ownerEmail, selectedFile);
    } 

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result processOwnerForm(String dir, String ownerEmail, String selectedFile) {
        Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest();
        AssignOptionForm data = form.get();

        if (form.hasErrors()) {
            System.out.println("HAS ERRORS");
            return badRequest(assignOption.render(User.getUserEmails(),
                    routes.WorkingFiles.processOwnerForm(dir, ownerEmail, selectedFile),
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
            return redirect(routes.WorkingFiles.index(dir));
        }
    } 
    */

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result downloadTemplates(String dir) {
        return ok(download_templates.render(dir));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postDownloadTemplates(String dir) {
        return postDownloadTemplates(dir);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result checkAnnotationLog(String dir, String file_name) {
        AnnotationLog log = AnnotationLog.find(file_name);
        if (null == log) {
            return ok(annotation_log.render(Feedback.print(Feedback.WEB, ""), routes.WorkingFiles.index(dir).url()));
        }
        else {
            return ok(annotation_log.render(Feedback.print(Feedback.WEB, log.getLog()), routes.WorkingFiles.index(dir).url()));
        }
    }

    public Result getAnnotationStatus(String fileName) {
        DataFile dataFile = DataFile.findByName(fileName);
        Map<String, Object> result = new HashMap<String, Object>();

        if (dataFile == null) {
            result.put("File Name", fileName);
            result.put("Status", "Unknown");
            result.put("Error", "The file with the specified name cannot be retrieved. "
                    + "Please provide a valid file name.");
        } else {
            result.put("File Name", dataFile.getFileName());
            result.put("Status", dataFile.getStatus());
            result.put("Submission Time", dataFile.getSubmissionTime());
            result.put("Completion Time", dataFile.getCompletionTime());
            result.put("Owner Email", dataFile.getOwnerEmail());
            result.put("Log", AnnotationLog.create(fileName).getLog());
        }

        return ok(Json.toJson(result));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result deleteDataFile(String dir, String fileName) {
        final SysUser user = AuthApplication.getLocalUser(session());
        
        DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findByName(null, fileName);
        } else {
            dataFile = DataFile.findByName(user.getEmail(), fileName);
        }
        if (null == dataFile) {
            return badRequest("You do NOT have the permission to operate this file!");
        }

        String path = ConfigProp.getPathWorking();

        File file = new File(path + "/" + fileName);

        String pureFileName = Paths.get(fileName).getFileName().toString();
        file.delete();
        dataFile.delete();
        AnnotationLog.delete(fileName);
        AnnotationLog.delete(pureFileName);

        return redirect(routes.WorkingFiles.index(dir));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result downloadDataFile(String file_name) {
        String path = ConfigProp.getPathWorking();
        return ok(new File(path + "/" + file_name));
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
                ConfigProp.getPathWorking())) {
        	System.out.println("resumableRelativePath (uploadDataFileByChunking): " + resumableRelativePath);
            //This Chunk has been Uploaded.
            return ok("Uploaded.");
        } else {
            return status(HttpStatus.SC_NOT_FOUND);
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

        Path path = Paths.get(resumableFilename);
        if (path == null) {
            return badRequest("<a style=\"color:#cc3300; font-size: x-large;\">Could not get file path!</a>");
        }

        String filename = path.getFileName().toString();
        DataFile file = DataFile.findByName(filename);
        if (file != null && file.existsInFileSystem(ConfigProp.getPathWorking())) {
            return badRequest("<a style=\"color:#cc3300; font-size: x-large;\">A file with this name already exists!</a>");
        }

        if (ResumableUpload.postUploadFileByChunking(request(), ConfigProp.getPathWorking())) {
            DataFile.create(filename, AuthApplication.getLocalUser(session()).getEmail(), DataFile.WORKING);
            return(ok("Upload finished"));
        } else {
            return(ok("Upload"));
        }
    }
}

