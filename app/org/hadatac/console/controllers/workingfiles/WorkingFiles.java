package org.hadatac.console.controllers.workingfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.controllers.workingfiles.routes;
import org.hadatac.console.http.ResumableUpload;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.NewFileForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.console.views.html.workingfiles.*;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.data.loader.CSVRecordFile;
import org.hadatac.data.loader.GeneratorChain;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.data.loader.SpreadsheetRecordFile;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.pac4j.play.java.Secure;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.common.io.Files;
import com.typesafe.config.ConfigFactory;
import play.libs.Json;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.mvc.Http;
import play.twirl.api.Html;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;


public class WorkingFiles extends Controller {

    @Inject
    FormFactory formFactory;
    @Inject
    Application application;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String dir, String dest, Boolean stayAtRoot, Http.Request request) {

        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        String targetDir = null;
        List<DataFile> wkFiles = null;
        String pathWorking = ConfigProp.getPathWorking();

        if ( stayAtRoot ) {
            targetDir = pathWorking;
        } else {
            if ("/".equals(dest)) {
                targetDir = pathWorking + "download/" + user.getEmail();
            } else {
                targetDir = Paths.get(dir, dest).normalize().toString();
            }
        }

        if ( targetDir.startsWith(pathWorking.substring(0, pathWorking.length()-1)) == false )  {
            targetDir = Paths.get(pathWorking, targetDir).normalize().toString();
        }

        // List<String> folders = DataFile.findFolders(Paths.get(pathWorking, newDir).toString(), false);
        List<String> folders = DataFile.findFolders(targetDir, false);
        Collections.sort(folders);
        if (!"/".equals(targetDir)) {
            folders.add(0, "..");
        }

        if (user.isDataManager()) {
            wkFiles = DataFile.findDownloadedFilesInDir(targetDir, DataFile.CREATING);

            String basePath = targetDir;
            if (basePath.startsWith("/")) {
                basePath = basePath.substring(1, basePath.length());
            }

            // DataFile.includeUnrecognizedFiles(Paths.get(pathWorking, newDir).toString(), basePath, wkFiles, user.getEmail(), DataFile.WORKING);
        } else {
            // since we can see other users' download, we don't send it email anymore
            wkFiles = DataFile.findDownloadedFilesInDir(targetDir, /*user.getEmail(), */ DataFile.CREATING);
        }

        // DataFile.filterNonexistedFiles(pathWorking, wkFiles);

        DataFile.updatePermission(wkFiles, user.getEmail());

        // once we reach the level that contains download, we don't want to allow the use to nevigate back to upper level
        if ( folders.contains("download/") ) {
            folders.remove("..");
        }
        return ok(workingFiles.render(targetDir, folders, wkFiles, user.isDataManager(),application.getUserEmail(request)));

    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String dir, String dest,Http.Request request) {
        return index(dir, dest, false,request);
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result renameDataFile(String dir, String fileId,Http.Request request) {
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

        return ok(renameFile.render(dir, dataFile,user.getEmail()));
    }

    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postRenameDataFile(String dir, String fileId,Http.Request request) {
        return renameDataFile(dir, fileId, request);
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result processRenameDataFileForm(String dir, String fileId,Http.Request request) throws Exception {
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

        Form<NewFileForm> form = formFactory.form(NewFileForm.class).bindFromRequest(request);
        NewFileForm data = form.get();

        if (form.hasErrors()) {
            System.out.println("HAS ERRORS");
            return badRequest(renameFile.render(dir, dataFile, application.getUserEmail(request)));
        } else {
            String newFileName = Paths.get(data.getNewName()).getFileName().toString();

            String dirPath = Paths.get(dataFile.getAbsolutePath()).getParent().toString();
            File originalFile = new File(dataFile.getAbsolutePath());

            dataFile.setFileName(newFileName);
            dataFile.save();

            String newFilePath = Paths.get(dirPath, dataFile.getStorageFileName()).toString();
            File newFile = new File(newFilePath);
            if (newFile.exists()) {
                return badRequest("A file with the new name already exists in the current folder!");
            } else {
                try {
                    originalFile.renameTo(newFile);
                    originalFile.delete();

                    logger.info("newFilePath: " + newFilePath);
                } catch (Exception e) {
                    return badRequest("Failed to rename the target file!");
                }
            }

            return redirect(routes.WorkingFiles.index(dir, ".", false));
        }
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result moveDataFile(String dir, String fileId, Http.Request request) {
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

        DataFile dirFile = new DataFile(dir);
        dirFile.setStatus(DataFile.WORKING);

        return ok(moveFile.render(dir, dataFile, dirFile, user.getEmail()));
    }

    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postMoveDataFile(String dir, String fileId, Http.Request request) {
        return moveDataFile(dir, fileId, request);
    }

    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result processMoveDataFileForm(String dir, String fileId, Http.Request request) throws Exception {
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

        Form<NewFileForm> form = formFactory.form(NewFileForm.class).bindFromRequest(request);
        NewFileForm data = form.get();

        if (form.hasErrors()) {
            System.out.println("HAS ERRORS");
            DataFile dirFile = new DataFile(dir);
            return badRequest(moveFile.render(dir, dataFile, dirFile, user.getEmail()));
        } else {
            String destination = data.getNewDest();

            DataFile root = new DataFile("/");
            root.setStatus(DataFile.WORKING);
            String oldFilePath = dataFile.getAbsolutePath();
            String newFilePath = Paths.get(root.getAbsolutePath(), destination, dataFile.getStorageFileName()).toString();

            System.out.println("fileName " + dataFile.getStorageFileName());
            System.out.println("oldFilePath " + oldFilePath);
            System.out.println("newFilePath " + newFilePath);
            System.out.println("destination: " + destination);

            File originalFile = new File(oldFilePath);
            File newFile = new File(newFilePath);
            if (newFile.exists()) {
                return badRequest("A file with the same name already exists in the destination folder!");
            } else {
                try {
                    originalFile.renameTo(newFile);
                    originalFile.delete();

                    if (destination.startsWith("/")) {
                        dataFile.setDir(destination.substring(1));
                    } else {
                        dataFile.setDir(destination);
                    }
                    dataFile.save();

                    logger.info("newFilePath: " + newFilePath);
                } catch (Exception e) {
                    return badRequest("Failed to move the file!");
                }
            }

            return redirect(routes.WorkingFiles.index(dir, ".", false));
        }
    }

    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result moveDataFiles(String dir,Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        Map<String, String[]> name_map = request.body().asFormUrlEncoded();
        List<String> selectedFileIds = new ArrayList<String>();
        String fileIdString = name_map.get("fileIds")[0];
        if (fileIdString.length() > 0) {
            for (String id : fileIdString.split(",")) {
                selectedFileIds.add(id);
            }
        }

        List<DataFile> dataFiles = new ArrayList<DataFile>();
        for (String fileId : selectedFileIds) {
            DataFile dataFile = null;
            if (user.isDataManager()) {
                dataFile = DataFile.findById(fileId);
            } else {
                dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
            }

            if (null == dataFile) {
                return badRequest(String.format(
                        "You do NOT have the permission to operate this file with id %s !", fileId));
            }

            dataFiles.add(dataFile);
        }

        DataFile dirFile = new DataFile(dir);
        dirFile.setStatus(DataFile.WORKING);

        return ok(moveFiles.render(dir, dataFiles, dirFile, user.getEmail()));
    }

    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postMoveDataFiles(String dir, Http.Request request) {
        return moveDataFiles(dir, request);
    }

    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result processMoveDataFilesForm(String dir, Http.Request request) throws Exception {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        Map<String, String[]> name_map = request.body().asFormUrlEncoded();
        List<String> selectedFileIds = new ArrayList<String>();
        String fileIdString = name_map.get("fileIds")[0];
        if (fileIdString.length() > 0) {
            for (String id : fileIdString.split(",")) {
                selectedFileIds.add(id);
            }
        }

        for (String fileId : selectedFileIds) {
            DataFile dataFile = null;
            if (user.isDataManager()) {
                dataFile = DataFile.findById(fileId);
            } else {
                dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
            }

            if (null == dataFile) {
                return badRequest(String.format(
                        "You do NOT have the permission to operate this file with id %s !", fileId));
            }

            Form<NewFileForm> form = formFactory.form(NewFileForm.class).bindFromRequest(request);
            NewFileForm data = form.get();

            if (form.hasErrors()) {
                return badRequest("THE FORM HAS ERRORS");
            } else {
                try {
                    String destination = data.getNewDest();
                    moveSingleDataFile(dataFile, destination);
                } catch (Exception e) {
                    return badRequest(e.getMessage());
                }
            }
        }

        return redirect(routes.WorkingFiles.index(dir, ".", false));
    }

    public void moveSingleDataFile(DataFile dataFile, String destination) throws Exception {
        DataFile root = new DataFile("/");
        root.setStatus(DataFile.WORKING);
        String oldFilePath = dataFile.getAbsolutePath();
        String newFilePath = Paths.get(root.getAbsolutePath(), destination, dataFile.getStorageFileName()).toString();

        System.out.println("fileName " + dataFile.getStorageFileName());
        System.out.println("oldFilePath " + oldFilePath);
        System.out.println("newFilePath " + newFilePath);
        System.out.println("destination: " + destination);

        File originalFile = new File(oldFilePath);
        File newFile = new File(newFilePath);
        if (newFile.exists()) {
            throw new Exception("A file with the same name already exists in the destination folder!");
        } else {
            try {
                originalFile.renameTo(newFile);
                originalFile.delete();

                if (destination.startsWith("/")) {
                    dataFile.setDir(destination.substring(1));
                } else {
                    dataFile.setDir(destination);
                }
                dataFile.save();

                logger.info("newFilePath: " + newFilePath);
            } catch (Exception e) {
                throw new Exception("Failed to move the file!");
            }
        }
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result shareDataFile(String dir, String fileId, Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findById(fileId);
        } else {
            dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        }

        if (null == dataFile) {
            return badRequest("You do NOT have the permission to share this file!");
        }

        String viewableId = dataFile.getViewableId();
        if (viewableId.isEmpty()) {
            viewableId = UUID.randomUUID().toString();
            dataFile.setViewableId(viewableId);
            dataFile.save();
        }

        String sharedlink = ConfigFactory.load().getString("hadatac.console.host_deploy") +
                org.hadatac.console.controllers.fileviewer.routes.ExcelPreview.fromViewableLink(viewableId).toString();

        return ok(shareFile.render(dir, sharedlink, dataFile, user.getEmail()));
    }

    @SuppressWarnings("unchecked")
    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result saveViewerEmails(Http.Request request) throws Exception {
        Form form = formFactory.form().bindFromRequest(request);
        Map<String, String> data = form.rawData();

        String dir = data.get("dir");
        String fileId = data.get("fileId");

        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findById(fileId);
        } else {
            dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        }

        List<String> emails = new ArrayList<String>();
        for (int i = 0; i < data.size() - 2; i++) {
            if (!data.containsKey("viewerEmail" + String.valueOf(i + 1))) {
                continue;
            }

            emails.add(data.get("viewerEmail" + String.valueOf(i + 1)));
        }

        dataFile.setViewerEmails(emails);
        dataFile.save();

        return redirect(routes.WorkingFiles.shareDataFile(dir, fileId));
    }

    @SuppressWarnings("unchecked")
    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result saveEditorEmails(Http.Request request) throws Exception {
        Form form = formFactory.form().bindFromRequest(request);
        Map<String, String> data = form.rawData();

        String dir = data.get("dir");
        String fileId = data.get("fileId");

        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findById(fileId);
        } else {
            dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        }

        List<String> emails = new ArrayList<String>();
        for (int i = 0; i < data.size() - 2; i++) {
            if (!data.containsKey("editorEmail" + String.valueOf(i + 1))) {
                continue;
            }

            emails.add(data.get("editorEmail" + String.valueOf(i + 1)));
        }

        dataFile.setEditorEmails(emails);
        dataFile.save();

        return redirect(routes.WorkingFiles.shareDataFile(dir, fileId));
    }
    
    /*
    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result assignFileOwner(String dir, String ownerEmail, String selectedFile) {	
        return ok(workingFiles.render(User.getUserEmails(), routes.WorkingFiles.processOwnerForm(dir, ownerEmail, selectedFile), "Owner", "Selected File", selectedFile));
    }
    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postAssignFileOwner(String dir, String ownerEmail, String selectedFile) {
        return assignFileOwner(dir, ownerEmail, selectedFile);
    } 
    @Secure (authorizers = Constants.DATA_MANAGER_ROLE)
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
            DataFile file = DataFile.findByNameAndEmail(ownerEmail, selectedFile);
            if (file == null) {
                file = new DataFile(selectedFile);
                file.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
                file.setStatus(DataFile.UNPROCESSED);
                file.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            }
            file.setOwnerEmail(data.getOption());
            file.save();
            return redirect(routes.WorkingFiles.index(dir, "."));
        }
    } 
    */

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result downloadTemplates(String dir, Http.Request request) {
        return ok(download_templates.render(dir, application.getUserEmail(request)));
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result postDownloadTemplates(String dir) {
        return postDownloadTemplates(dir);
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result checkAnnotationLog(String dir, String fileId, Http.Request request) {
        DataFile dataFile = DataFile.findById(fileId);
        return ok(annotation_log.render(Feedback.print(Feedback.WEB,
                DataFile.findById(fileId).getLog()),
                routes.WorkingFiles.index(dir, "/", false).url(), application.getUserEmail(request)));
    }

    public Result getAnnotationStatus(String fileId) {
        DataFile dataFile = DataFile.findById(fileId);
        Map<String, Object> result = new HashMap<String, Object>();
        if (dataFile == null) {
            result.put("File Id", fileId);
            result.put("Status", "Unknown");
            result.put("Error", "The file with the specified name cannot be retrieved. "
                    + "Please provide a valid file name.");
        } else {
            result.put("File Name", dataFile.getFileName());
            result.put("Status", dataFile.getStatus());
            result.put("Submission Time", dataFile.getSubmissionTime());
            result.put("Completion Time", dataFile.getCompletionTime());
            result.put("Owner Email", dataFile.getOwnerEmail());
            result.put("Log", dataFile.getLog());
        }

        return ok(Json.toJson(result));
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result deleteDataFile(String dir, String fileId, Http.Request request) {
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

        File file = new File(dataFile.getAbsolutePath());
        file.delete();
        dataFile.delete();

        return redirect(routes.WorkingFiles.index(dir, ".", false));
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result deleteDataFiles(String dir, Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        Map<String, String[]> name_map = request.body().asFormUrlEncoded();
        List<String> selectedFileIds = new ArrayList<String>();
        String fileIdString = name_map.get("fileIds")[0];
        if (fileIdString.length() > 0) {
            for (String id : fileIdString.split(",")) {
                selectedFileIds.add(id);
            }
        }

        System.out.println("selectedFileIds: " + selectedFileIds);
        for (String fileId : selectedFileIds) {
            DataFile dataFile = null;
            if (user.isDataManager()) {
                dataFile = DataFile.findById(fileId);
            } else {
                dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
            }

            if (null == dataFile) {
                return badRequest(String.format(
                        "You do NOT have the permission to operate the file with id %s!", fileId));
            }

            File file = new File(dataFile.getAbsolutePath());
            file.delete();
            dataFile.delete();
        }

        return redirect(routes.WorkingFiles.index(dir, ".", false));
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result downloadDataFile(String fileId, Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        DataFile dataFile = DataFile.findById(fileId);

        if (null == dataFile ||
                ( !dataFile.getOwnerEmail().equals(user.getEmail())
                        && !dataFile.getViewerEmails().contains(user.getEmail())
                        && !dataFile.getEditorEmails().contains(user.getEmail()))) {
            return badRequest("You do NOT have the permission to download this file!");
        }

        return ok(new File(dataFile.getAbsolutePath())).withHeader("Content-disposition", String.format("attachment; filename=%s", dataFile.getFileName()));
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result ingestDataFile(String fileId, Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findByIdAndStatus(fileId, DataFile.WORKING);
        } else {
            dataFile = DataFile.findByIdAndOwnerEmailAndStatus(
                    fileId, user.getEmail(), DataFile.WORKING);
        }

        if (null == dataFile) {
            return badRequest("You do NOT have the permission to operate this file!");
        }

        if (dataFile.existsInFileSystem(ConfigProp.getPathUnproc())) {
            return badRequest("<a style=\"color:#cc3300; font-size: x-large;\">A file with this name already exists!</a>");
        }

        DataFile newDataFile = DataFile.create(
                dataFile.getFileName(), "", dataFile.getOwnerEmail(), DataFile.UNPROCESSED);
        newDataFile.getLogger().resetLog();
        newDataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        newDataFile.save();

        // Send a copy of file to the auto-annotator
        File file = new File(dataFile.getAbsolutePath());
        File newFile = new File(newDataFile.getAbsolutePath());
        File destFolder = new File(ConfigProp.getPathUnproc());
        if (!destFolder.exists()){
            destFolder.mkdirs();
        }

        try {
            Files.copy(file, newFile);
        } catch (IOException e) {
            System.out.println("Failed to copy the file at " + dataFile.getAbsolutePath());
        }

        return redirect(org.hadatac.console.controllers.annotator.routes.AutoAnnotator.index("/", "."));
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result verifyDataFile(String fileId, Http.Request request) {
        DataFile dataFile = DataFile.findByIdAndStatus(fileId, DataFile.WORKING);
        File file = new File(dataFile.getAbsolutePath());

        dataFile.getLogger().resetLog();

        if (dataFile.attachFile(file)) {
            GeneratorChain chain = AnnotationWorker.getGeneratorChain(dataFile);
            if (null != chain) {
                chain.generate(false);
            }
        }

        String strLog = dataFile.getLog();
        if (strLog.isEmpty()) {
            strLog += "This file can be ingested without errors";
        }

        return ok(annotation_log.render(Feedback.print(Feedback.WEB, strLog),
                routes.WorkingFiles.index("/", ".", false).url(), application.getUserEmail(request)));
    }
    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result verifyDataFileTemp(String file_id) {
        DataFile dataFile = DataFile.findByIdAndStatus(file_id, DataFile.WORKING);
        File file = new File(dataFile.getAbsolutePath());

        dataFile.getLogger().resetLog();

        if (dataFile.attachFile(file)) {
            GeneratorChain chain = AnnotationWorker.getGeneratorChain(dataFile);
            if (null != chain) {
                chain.generate(false);
            }
        }

        String strLog = dataFile.getLog();
        if (strLog.isEmpty()) {
            strLog += "This file can be ingested without errors";
        }
        //logString=Feedback.print(Feedback.WEB, strLog);
        return ok(Json.toJson(Feedback.print(Feedback.WEB, strLog)));

    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result uploadDataFileByChunking(
            String resumableChunkNumber,
            String resumableChunkSize,
            String resumableCurrentChunkSize,
            String resumableTotalSize,
            String resumableType,
            String resumableIdentifier,
            String resumableFilename,
            String resumableRelativePath,
            Http.Request request) {

        String baseDir = Paths.get(ConfigProp.getPathWorking(), resumableRelativePath).toString();
        if (ResumableUpload.uploadFileByChunking(request, baseDir)) {
            //This Chunk has been Uploaded.
            return ok("Uploaded.");
        } else {
            return status(HttpStatus.SC_NOT_FOUND);
        }
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result postUploadDataFileByChunking(
            String resumableChunkNumber,
            String resumableChunkSize,
            String resumableCurrentChunkSize,
            String resumableTotalSize,
            String resumableType,
            String resumableIdentifier,
            String resumableFilename,
            String resumableRelativePath,
            Http.Request request) {

        String baseDir = Paths.get(ConfigProp.getPathWorking(), resumableRelativePath).toString();
        Path path = Paths.get(resumableFilename);
        if (path == null) {
            return badRequest("<a style=\"color:#cc3300; font-size: x-large;\">Could not get file path!</a>");
        }

        String fileName = path.getFileName().toString();

        if (ResumableUpload.postUploadFileByChunking(request, baseDir)) {
            DataFile dataFile = DataFile.create(
                    fileName, resumableRelativePath, AuthApplication.getLocalUser(application.getUserEmail(request)).getEmail(),
                    DataFile.WORKING);

            String originalPath = Paths.get(baseDir, dataFile.getPureFileName()).toString();
            File file = new File(originalPath);

            String newPath = originalPath.replace(
                    "/" + dataFile.getPureFileName(),
                    "/" + dataFile.getStorageFileName());
            file.renameTo(new File(newPath));
            file.delete();

            return(ok("Upload finished"));
        } else {
            return(ok("Upload"));
        }
    }
}
