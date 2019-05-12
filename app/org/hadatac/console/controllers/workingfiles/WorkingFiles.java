package org.hadatac.console.controllers.workingfiles;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;

import org.hadatac.entity.pojo.Credential;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.controllers.workingfiles.routes;
import org.hadatac.console.http.ResumableUpload;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.LabKeyLoginForm;
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
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.labkey.remoteapi.CommandException;

import com.google.common.io.Files;
import com.typesafe.config.ConfigFactory;

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
    public Result index(String dir, String dest) {        
        final SysUser user = AuthApplication.getLocalUser(session());

        String newDir = Paths.get(dir, dest).normalize().toString();
        
        List<DataFile> wkFiles = null;

        String pathWorking = ConfigProp.getPathWorking();
        
        List<String> folders = DataFile.findFolders(Paths.get(pathWorking, newDir).toString());
        if (!"/".equals(newDir)) {
            folders.add(0, "..");
        }
        
        if (user.isDataManager()) {
        	wkFiles = DataFile.findInDir(newDir, DataFile.WORKING);
        	
        	String basePath = newDir;
            if (basePath.startsWith("/")) {
                basePath = basePath.substring(1, basePath.length());
            }
            
        	DataFile.includeUnrecognizedFiles(Paths.get(pathWorking, newDir).toString(), 
        	        basePath, wkFiles, user.getEmail(), DataFile.WORKING);
        } else {
            wkFiles = DataFile.findInDir(newDir, user.getEmail(), DataFile.WORKING);
        }

        DataFile.filterNonexistedFiles(pathWorking, wkFiles);

        wkFiles.sort(new Comparator<DataFile>() {
            @Override
            public int compare(DataFile d1, DataFile d2) {
                return d1.getFileName().compareTo(d2.getFileName());
            }
        });

        return ok(workingFiles.render(newDir, folders, wkFiles, user.isDataManager()));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String dest) {
        return index(dir, dest);
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result renameDataFile(String dir, String fileId) {
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
        
        return ok(renameFile.render(dir, dataFile));
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postRenameDataFile(String dir, String fileId) {
        return renameDataFile(dir, fileId);
    } 

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result processRenameDataFileForm(String dir, String fileId) {
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
        
        Form<NewFileForm> form = formFactory.form(NewFileForm.class).bindFromRequest();
        NewFileForm data = form.get();

        if (form.hasErrors()) {
            System.out.println("HAS ERRORS");
            return badRequest(renameFile.render(dir, dataFile));
        } else {
            String newFileName = Paths.get(data.getNewName()).getFileName().toString();
            String newFilePath = Paths.get(Paths.get(dataFile.getAbsolutePath()).getParent().getFileName().toString(), newFileName).toString();
            
            File originalFile = new File(dataFile.getAbsolutePath());
            File newFile = new File(newFilePath);
            if (newFile.exists()) {
                return badRequest("A file with the new name already exists in the current folder!");
            } else {
                try {
                    Files.copy(originalFile, newFile);
                    
                    if (newFile.exists()) {
                        originalFile.delete();
                        
                        dataFile.setFileName(newFileName);
                        dataFile.save();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return badRequest("Failed to rename the target file!");
                }
            }
            
            return redirect(routes.WorkingFiles.index(dir, "."));
        }
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result getLinkForSharing(String dir, String fileId) {
        final SysUser user = AuthApplication.getLocalUser(session());
        
        DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findById(fileId);
        } else {
            dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        }
        
        if (null == dataFile) {
            return badRequest("You do NOT have the permission to share this file!");
        }
        
        String sharedId = dataFile.getSharedId();
        if (sharedId.isEmpty()) {
            sharedId = UUID.randomUUID().toString();
            dataFile.setSharedId(sharedId);
            dataFile.save();
        }
        
        String sharedlink = ConfigFactory.load().getString("hadatac.console.host_deploy") + 
                org.hadatac.console.controllers.fileviewer.routes.ExcelPreview.fromSharedLink(sharedId).toString();
        
        return ok("<a style=\"color:#0000FF; font-size: large;\">Please copy this link for sharing: <br/><u>" + sharedlink 
                + "</u><br/>With this link, one can preview the shared file!&#128521;</a>").as("text/html");
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

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result downloadTemplates(String dir) {
        return ok(download_templates.render(dir));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postDownloadTemplates(String dir) {
        return postDownloadTemplates(dir);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result checkAnnotationLog(String dir, String fileId) {
        return ok(annotation_log.render(Feedback.print(Feedback.WEB, 
                DataFile.findById(fileId).getLog()), 
                routes.WorkingFiles.index(dir, dir).url()));
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

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result deleteDataFile(String dir, String fileId) {
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
        
        File file = new File(dataFile.getAbsolutePath());
        file.delete();
        dataFile.delete();

        return redirect(routes.WorkingFiles.index(dir, "."));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result downloadDataFile(String fileId) {
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        if (null == dataFile) {
            return badRequest("You do NOT have the permission to download this file!");
        }
        
        return ok(new File(dataFile.getAbsolutePath()));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result ingestDataFile(String fileId) {
        final SysUser user = AuthApplication.getLocalUser(session());
        
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
        
        File file = new File(dataFile.getAbsolutePath());
        File destFolder = new File(ConfigProp.getPathUnproc());
        if (!destFolder.exists()){
            destFolder.mkdirs();
        }
        file.renameTo(new File(destFolder.getPath() + "/" + dataFile.getPureFileName()));
        file.delete();
        
        dataFile.getLogger().resetLog();
        dataFile.setDir("");
        dataFile.setStatus(DataFile.UNPROCESSED);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        dataFile.save();
        
        return redirect(org.hadatac.console.controllers.annotator.routes.AutoAnnotator.index("/", "."));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result verifyDataFile(String fileId) {
        DataFile dataFile = DataFile.findByIdAndStatus(fileId, DataFile.WORKING);
        File file = new File(dataFile.getAbsolutePath());
        
        RecordFile recordFile = null;
        if (dataFile.getFileName().endsWith(".csv")) {
            recordFile = new CSVRecordFile(file);
        } else if (dataFile.getFileName().endsWith(".xlsx")) {
            recordFile = new SpreadsheetRecordFile(file);
        }
        
        dataFile.setRecordFile(recordFile);
        dataFile.getLogger().resetLog();
        
        GeneratorChain chain = AnnotationWorker.getGeneratorChain(dataFile);
        if (null != chain) {
            chain.generate(false);
        }
        
        String strLog = dataFile.getLog();
        
        return ok(annotation_log.render(Feedback.print(Feedback.WEB, strLog), 
                routes.WorkingFiles.index("/", ".").url()));
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

        String fileName = path.getFileName().toString();
        DataFile file = DataFile.findByNameAndStatus(fileName, DataFile.WORKING);
        if (file != null && file.existsInFileSystem(ConfigProp.getPathWorking())) {
            return badRequest("<a style=\"color:#cc3300; font-size: x-large;\">A file with this name already exists!</a>");
        }

        if (ResumableUpload.postUploadFileByChunking(request(), ConfigProp.getPathWorking())) {
            DataFile.create(fileName, "", AuthApplication.getLocalUser(session()).getEmail(), DataFile.WORKING);
            return(ok("Upload finished"));
        } else {
            return(ok("Upload"));
        }
    }
}

