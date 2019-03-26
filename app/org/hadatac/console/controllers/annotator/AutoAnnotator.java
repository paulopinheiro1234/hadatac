package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

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
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.FileManager;
import org.hadatac.utils.NameSpace;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.twirl.api.Html;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData.FilePart;


public class AutoAnnotator extends Controller {

    @Inject
    FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String dest) {        
        final SysUser user = AuthApplication.getLocalUser(session());

    	String newDir = "";
        if (dest.equals("..")) {
        	String[] tokens = dir.split("/");
        	for (int i=0; i < tokens.length - 1; i++) {
        		//System.out.println("[" + tokens[i] + "]");
        		if (tokens[i].equals("")) {
        			newDir = newDir + "/";
        		} else {
        			newDir = newDir + tokens[i] + "/";
        			dest	 = ".";
        		}
        	}
        } else if (dest.equals(".")) {
        	newDir = dir;
        } else {
        	if (dir.equals("/") && dest.equals("/")) {
        		newDir = "/";
        	} else  {
        		newDir = dir + dest;
        	}
        } 
        
        List<String> folders = null;
        List<DataFile> procFiles = null;
        List<DataFile> unprocFiles = null;
        List<String> studyURIs = new ArrayList<String>();

        String pathProc = ConfigProp.getPathProc();
        String pathUnproc = ConfigProp.getPathUnproc();

        if (user.isDataManager()) {
            folders = DataFile.findAllFolders(newDir);
        	procFiles = DataFile.findInDir(newDir, DataFile.PROCESSED);
            unprocFiles = DataFile.findInDir("/", DataFile.UNPROCESSED);
            unprocFiles.addAll(DataFile.findInDir("/", DataFile.FREEZED));
            if (dir.equals("/")) {
            	DataFile.includeUnrecognizedFiles(pathProc, newDir, procFiles);
            }
        	DataFile.includeUnrecognizedFiles(pathUnproc, newDir, unprocFiles);
        } else {
            folders = DataFile.findFolders(newDir, user.getEmail());
            procFiles = DataFile.findInDir(newDir, user.getEmail(), DataFile.PROCESSED);
            unprocFiles = DataFile.findInDir("/", user.getEmail(), DataFile.UNPROCESSED);
            unprocFiles.addAll(DataFile.findInDir("/", user.getEmail(), DataFile.FREEZED));
        }

        DataFile.filterNonexistedFiles(pathProc, procFiles);
        DataFile.filterNonexistedFiles(pathUnproc, unprocFiles);

        for (DataFile dataFile : procFiles) {
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
        
        unprocFiles.sort(new Comparator<DataFile>() {
            @Override
            public int compare(DataFile d1, DataFile d2) {
                return d1.getFileName().compareTo(d2.getFileName());
            }
        });

        procFiles.sort(new Comparator<DataFile>() {
            @Override
            public int compare(DataFile d1, DataFile d2) {
                return d1.getFileName().compareTo(d2.getFileName());
            }
        });

        return ok(autoAnnotator.render(newDir, folders, unprocFiles, procFiles, studyURIs, bStarted, user.isDataManager()));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String dest) {
        return index(dir, dest);
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result assignFileOwner(String dir, String ownerEmail, String selectedFile) {	
        return ok(assignOption.render(User.getUserEmails(),
                routes.AutoAnnotator.processOwnerForm(dir, ownerEmail, selectedFile),
                "Owner", 
                "Selected File", 
                selectedFile));
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
                    routes.AutoAnnotator.processOwnerForm(dir, ownerEmail, selectedFile),
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
            return redirect(routes.AutoAnnotator.index(dir, "."));
        }
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result assignDataAcquisition(String dir, String dataAcquisitionUri, String selectedFile) {
        List<String> dataAcquisitionURIs = new ArrayList<String>();
        ObjectAccessSpec.findAll().forEach((da) -> dataAcquisitionURIs.add(
                URIUtils.replaceNameSpaceEx(da.getUri())));

        return ok(assignOption.render(dataAcquisitionURIs,
                routes.AutoAnnotator.processDataAcquisitionForm(dir, dataAcquisitionUri, selectedFile),
                "Object Access Specification",
                "Selected File",
                selectedFile));
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postAssignDataAcquisition(String dir, String dataAcquisitionUri, String selectedFile) {
        return assignDataAcquisition(dir, dataAcquisitionUri, selectedFile);
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result processDataAcquisitionForm(String dir, String dataAcquisitionUri, String selectedFile) {
        Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest();
        AssignOptionForm data = form.get();

        List<String> dataAcquisitionURIs = new ArrayList<String>();
        ObjectAccessSpec.findAll().forEach((da) -> dataAcquisitionURIs.add(
                URIUtils.replaceNameSpaceEx(da.getUri())));

        if (form.hasErrors()) {
            System.out.println("HAS ERRORS");
            return badRequest(assignOption.render(dataAcquisitionURIs,
                    routes.AutoAnnotator.processDataAcquisitionForm(dir, dataAcquisitionUri, selectedFile),
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
            return redirect(routes.AutoAnnotator.index(dir, "."));
        }
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result toggleAutoAnnotator(String dir) {
        if (ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("on")) {
            ConfigProp.setPropertyValue("autoccsv.config", "auto", "off");
            System.out.println("Turning auto-annotation off");
        }
        else {
            ConfigProp.setPropertyValue("autoccsv.config", "auto", "on");
            System.out.println("Turning auto-annotation on");
        }

        return redirect(routes.AutoAnnotator.index(dir, "."));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result downloadTemplates(String dir) {
        return ok(download_templates.render(dir));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postDownloadTemplates(String dir) {
        return postDownloadTemplates(dir);
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
            if("Unauthorized".equals(e.getMessage())){
                return ok(syncLabkey.render("login_failed", "", ""));
            }
        }

        return ok(main.render("Results", "", 
                new Html("<h3>Your provided credentials are valid and saved!</h3>")));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result checkAnnotationLog(String dir, String file_name) {
        AnnotationLog log = AnnotationLog.find(file_name);
        if (null == log) {
            return ok(annotation_log.render(Feedback.print(Feedback.WEB, ""), routes.AutoAnnotator.index(dir, ".").url()));
        }
        else {
            return ok(annotation_log.render(Feedback.print(Feedback.WEB, log.getLog()), routes.AutoAnnotator.index(dir, ".").url()));
        }
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result checkErrorDictionary() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("error_dictionary.json");
        String jsonText = "";
        try {
            jsonText = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return ok(error_dictionary.render(jsonText, routes.AutoAnnotator.index("/", ".").url()));
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
    public Result moveDataFile(String dir, String fileName) {        
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

        String pathProc = ConfigProp.getPathProc();
        String pathUnproc = ConfigProp.getPathUnproc();
        File file = new File(pathProc + "/" + fileName);

        String pureFileName = Paths.get(fileName).getFileName().toString();
        if (pureFileName.startsWith("DA-")) {
            Measurement.deleteFromSolr(dataFile.getDatasetUri());
            NameSpace.deleteTriplesByNamedGraph(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
        } else {
            deleteAddedTriples(file, dataFile);
        }

        dataFile.setStatus(DataFile.UNPROCESSED);
        dataFile.setFileName(pureFileName);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        dataFile.setCompletionTime("");
        dataFile.save();

        File destFolder = new File(pathUnproc);
        if (!destFolder.exists()){
            destFolder.mkdirs();
        }
        file.renameTo(new File(destFolder + "/" + pureFileName));
        file.delete();

        AnnotationLog log = AnnotationLog.find(fileName);
        if (null != log) {
            log.delete();
        }

        AnnotationLog new_log = new AnnotationLog(pureFileName);
        new_log.addline(Feedback.println(Feedback.WEB, 
                String.format("[OK] Moved file %s to unprocessed folder", pureFileName)));

        return redirect(routes.AutoAnnotator.index(dir, "."));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result activateDataFile(String dir, String fileName) {           
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
        
        AnnotationLog.delete(fileName);

        return redirect(routes.AutoAnnotator.index(dir, "."));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result deleteDataFile(String dir, String fileName, boolean isProcessed) {
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
        if (isProcessed) {
            path = ConfigProp.getPathProc();
        } else{
            path = ConfigProp.getPathUnproc();
        }

        File file = new File(path + "/" + fileName);

        String pureFileName = Paths.get(fileName).getFileName().toString();
        if (pureFileName.startsWith("DA-")) {
            Measurement.deleteFromSolr(dataFile.getDatasetUri());
            NameSpace.deleteTriplesByNamedGraph(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
        } else {
            try {
                deleteAddedTriples(file, dataFile);
            } catch (Exception e) {
                System.out.print("Can not delete triples ingested by " + fileName + " ..");
                file.delete();
                dataFile.delete();
                AnnotationLog.delete(fileName);
                AnnotationLog.delete(pureFileName);
                return redirect(routes.AutoAnnotator.index(dir, "."));
            }
        }
        file.delete();
        dataFile.delete();
        AnnotationLog.delete(fileName);
        AnnotationLog.delete(pureFileName);

        return redirect(routes.AutoAnnotator.index(dir, "."));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static void deleteAddedTriples(File file, DataFile dataFile) {
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

        String fileName = file.getName();
        GeneratorChain chain = AnnotationWorker.getGeneratorChain(fileName, dataFile, recordFile);
        
        if (chain != null) {
            chain.delete();
        }
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result downloadDataFile(String folder, String file_name) {
        String path = FileManager.getInstance().getPathByLabel(folder);
        
        /* TO DO : add permission control here */
        
        return ok(new File(path + "/" + file_name));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result saveDataFile() {        
        FilePart uploadedfile = request().body().asMultipartFormData().getFile("file");
        
        JSONParser parser = new JSONParser();
        JSONObject params = null;
        try {
            String metadata = URLDecoder.decode(uploadedfile.getFilename(), "utf-8");
            params = (JSONObject)parser.parse(metadata);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String folder = (String)params.get("folder");
        String filePath = (String)params.get("filePath");
        Boolean bSavable = Boolean.parseBoolean((String)params.get("bSavable"));
        
        if (uploadedfile != null) {
            String fileName = uploadedfile.getFilename();
            String path = FileManager.getInstance().getPathByLabel(folder);
            File file = new File(path + "/" + filePath);
            
            final SysUser user = AuthApplication.getLocalUser(session());
            if (null == DataFile.findByName(user.getEmail(), filePath)) {
                return ok("<a style=\"color:#cc3300; font-size: large;\">This file can be modified only by its owner!</a>");
            }
            
            if (!file.exists()) {
                return ok("<a style=\"color:#cc3300; font-size: large;\">Could not find this file on records!</a>");
            }
            
            InputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream((File)uploadedfile.getFile());
                byte[] byteFile = IOUtils.toByteArray(fileInputStream);
                FileUtils.writeByteArrayToFile(file, byteFile);
                fileInputStream.close();
            } catch (Exception e) {
                return ok("<a style=\"color:#cc3300; font-size: large;\">Error uploading file. Please try again.</a>");
            }
            
            return ok("<a style=\"color:#008000; font-size: large;\">File successfully saved!</a>");
        } else {
            return ok("<a style=\"color:#cc3300; font-size: large;\">Error uploading file. Please try again.</a>");
        }
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
        if (file != null && file.existsInFileSystem(ConfigProp.getPathUnproc())) {
            return badRequest("<a style=\"color:#cc3300; font-size: x-large;\">A file with this name already exists!</a>");
        }

        if (ResumableUpload.postUploadFileByChunking(request(), ConfigProp.getPathUnproc())) {
            DataFile.create(filename, AuthApplication.getLocalUser(session()).getEmail());
            return(ok("Upload finished"));
        } else {
            return(ok("Upload"));
        }
    }
}

