package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;

import javax.servlet.http.HttpServletResponse;

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
import org.hadatac.data.loader.CSVRecordFile;
import org.hadatac.data.loader.GeneratorChain;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.data.loader.SpreadsheetRecordFile;
import org.hadatac.console.views.html.*;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.twirl.api.Html;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;


public class AutoAnnotator extends Controller {

    @Inject
    FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index() {		
        final SysUser user = AuthApplication.getLocalUser(session());

        List<DataFile> proc_files = null;
        List<DataFile> unproc_files = null;
        List<String> studyURIs = new ArrayList<String>();

        String path_proc = ConfigProp.getPathProc();
        String path_unproc = ConfigProp.getPathUnproc();

        if (user.isDataManager()) {
            proc_files = DataFile.findAll(DataFile.PROCESSED);
            unproc_files = DataFile.findAll(DataFile.UNPROCESSED);
            unproc_files.addAll(DataFile.findAll(DataFile.FREEZED));
            DataFile.includeUnrecognizedFiles(path_unproc, unproc_files);
            DataFile.includeUnrecognizedFiles(path_proc, proc_files);
        } else {
            proc_files = DataFile.find(user.getEmail(), DataFile.PROCESSED);
            unproc_files = DataFile.find(user.getEmail(), DataFile.UNPROCESSED);
            unproc_files.addAll(DataFile.find(user.getEmail(), DataFile.FREEZED));
        }

        DataFile.filterNonexistedFiles(path_proc, proc_files);
        DataFile.filterNonexistedFiles(path_unproc, unproc_files);
        
        for (DataFile dataFile : proc_files) {
            if (!dataFile.getStudyUri().isEmpty() && !studyURIs.contains(dataFile.getStudyUri())) {
                studyURIs.add(dataFile.getStudyUri());
            }
        }
        
        studyURIs.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        boolean bStarted = false;
        if (ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("on")) {
            bStarted = true;
        }

        return ok(autoAnnotator.render(unproc_files, proc_files, studyURIs, bStarted, user.isDataManager()));
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
        ObjectAccessSpec.findAll().forEach((da) -> dataAcquisitionURIs.add(
                URIUtils.replaceNameSpaceEx(da.getUri())));

        return ok(assignOption.render(dataAcquisitionURIs,
                routes.AutoAnnotator.processDataAcquisitionForm(dataAcquisitionUri, selectedFile),
                "Object Access Specification",
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
        ObjectAccessSpec.findAll().forEach((da) -> dataAcquisitionURIs.add(
                URIUtils.replaceNameSpaceEx(da.getUri())));

        if (form.hasErrors()) {
            System.out.println("HAS ERRORS");
            return badRequest(assignOption.render(dataAcquisitionURIs,
                    routes.AutoAnnotator.processDataAcquisitionForm(dataAcquisitionUri, selectedFile),
                    "Object Access Specification",
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
        String user_name = form.get().getUserName();
        String password = form.get().getPassword();
        LabkeyDataHandler loader = new LabkeyDataHandler(
                ConfigProp.getLabKeySite(), "/", user_name, password);
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
    public Result moveDataFile(String fileName) {			
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findByName(null, fileName);
        }
        else {
            dataFile = DataFile.findByName(user.getEmail(), fileName);
        }
        if (null == dataFile) {
            return badRequest("You do NOT have the permission to operate this file!");
        }

        String path_proc = ConfigProp.getPathProc();
        String path_unproc = ConfigProp.getPathUnproc();
        File file = new File(path_proc + "/" + fileName);

        if (fileName.startsWith("DA-")) {
            Measurement.delete(dataFile.getDatasetUri());
        } else {
            deleteAddedTriples(file);
        }

        dataFile.setStatus(DataFile.UNPROCESSED);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        dataFile.setCompletionTime("");
        dataFile.save();

        File destFolder = new File(path_unproc);
        if (!destFolder.exists()){
            destFolder.mkdirs();
        }
        file.renameTo(new File(destFolder + "/" + fileName));

        AnnotationLog log = new AnnotationLog(fileName);
        log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Moved file %s to unprocessed folder", fileName)));

        return redirect(routes.AutoAnnotator.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result activateDataFile(String fileName) {           
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findByName(null, fileName);
        }
        else {
            dataFile = DataFile.findByName(user.getEmail(), fileName);
        }
        if (null == dataFile) {
            return badRequest("You do NOT have the permission to operate this file!");
        }

        dataFile.setStatus(DataFile.UNPROCESSED);
        dataFile.save();

        return redirect(routes.AutoAnnotator.index());
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result deleteDataFile(String fileName, boolean isProcessed) {
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

        String path = "";
        if(isProcessed){
            path = ConfigProp.getPathProc();
        } else{
            path = ConfigProp.getPathUnproc();
        }

        File file = new File(path + "/" + fileName);
        if (fileName.startsWith("DA-")) {
            Measurement.delete(dataFile.getDatasetUri());
        } else {
        	try{
        		deleteAddedTriples(file);
        	} catch (Exception e) {
            	System.out.print("Can not delete triples ingested by " + fileName + " ..");
                file.delete();
                dataFile.delete();
                AnnotationLog.delete(fileName);
                return redirect(routes.AutoAnnotator.index());
            }
        }
        file.delete();
        dataFile.delete();
        AnnotationLog.delete(fileName);

        return redirect(routes.AutoAnnotator.index());
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static void deleteAddedTriples(File file){
        System.out.println("Deleting the added triples from the moving file ...");

        RecordFile recordFile = null;
        if (file.getName().endsWith(".csv")) {
            recordFile = new CSVRecordFile(file);
        } else if (file.getName().endsWith(".xlsx")) {
            recordFile = new SpreadsheetRecordFile(file);
        } else {
            AnnotationLog log = new AnnotationLog(file.getName());
            log.addline(Feedback.println(Feedback.WEB, String.format(
                    "[ERROR] Unknown file format: %s", file.getName())));
            return;
        }

        String file_name = file.getName();
        GeneratorChain chain = null;
        if (file_name.startsWith("PID")) {
            chain = AnnotationWorker.annotateSubjectIdFile(recordFile);
        } else if (file_name.startsWith("STD")) {
            chain = AnnotationWorker.annotateStudyIdFile(recordFile);
        } else if (file_name.startsWith("DPL")) {
            if (file_name.endsWith(".xlsx")) {
                recordFile = new SpreadsheetRecordFile(file, "InfoSheet");
            }
            chain = AnnotationWorker.annotateDPLFile(recordFile);
        } else if (file_name.startsWith("MAP")) {
            chain = AnnotationWorker.annotateMapFile(recordFile);
        } else if (file_name.startsWith("ACQ")) {
            chain = AnnotationWorker.annotateACQFile(recordFile, false);
        } else if (file_name.startsWith("SDD")) {
            if (file_name.endsWith(".xlsx")) {
                recordFile = new SpreadsheetRecordFile(file, "InfoSheet");
            }
            chain = AnnotationWorker.annotateDataAcquisitionSchemaFile(recordFile);
        }

        if (chain != null) {
            chain.delete();
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
        
        DataFile file = DataFile.findByName(resumableFilename);
        if (file != null && file.existsInFileSystem(ConfigProp.getPathUnproc())) {
            return badRequest("<a style=\"color:#cc3300; font-size: x-large;\">A file with this name already exists!</a>");
        }
        
        if (ResumableUpload.postUploadFileByChunking(request(), ConfigProp.getPathUnproc())) {
            DataFile.create(resumableFilename, AuthApplication.getLocalUser(session()).getEmail());
            return(ok("Upload finished"));
        } else {
            return(ok("Upload"));
        }
    }
}

