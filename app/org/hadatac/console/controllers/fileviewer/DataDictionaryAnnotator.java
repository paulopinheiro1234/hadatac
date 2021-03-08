package org.hadatac.console.controllers.fileviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
//import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.controllers.fileviewer.routes;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.dataacquisitionsearch.*;
import org.hadatac.console.views.html.fileviewer.*;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.data.loader.GeneratorChain;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.data.loader.SpreadsheetRecordFile;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.entity.pojo.Alignment;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData.FilePart;


public class DataDictionaryAnnotator extends Controller {

    @Inject
    FormFactory formFactory;

    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

        List<DataFile> files = null;

        String path = ConfigProp.getPathDownload();

        if (user.isDataManager()) {
            files = DataFile.findByStatus(DataFile.DD_UNPROCESSED);
            files.addAll(DataFile.findByStatus(DataFile.DD_PROCESSED));
        } else {
            files = DataFile.find(user.getEmail(), DataFile.DD_UNPROCESSED);
            files.addAll(DataFile.find(user.getEmail(), DataFile.DD_PROCESSED));
        }

        DataFile.filterNonexistedFiles(path, files);

        return ok(sdd_editor.render(files, user.isDataManager(),user.getEmail()));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(Http.Request request) {
        return index(request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result downloadDataFile(String file_name) {
        String path = ConfigProp.getPathDownload();
        return ok(new File(path + "/" + file_name));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result deleteDataFile(String fileId, Http.Request request) {
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

        return redirect(routes.SDDEditor.index());
    }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result checkAnnotationLog(String fileId, Http.Request request) {
        DataFile dataFile = DataFile.findById(fileId);
        return ok(annotation_log.render(Feedback.print(Feedback.WEB, 
                DataFile.findById(fileId).getLog()), 
                routes.SDDEditor.index().url(),application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
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
    
    public Result uploadDataDictionaryFile(Http.Request request) {
        System.out.println("uploadDataDictionaryFile is called!");
        
        FilePart uploadedfile = request.body().asMultipartFormData().getFile("file");
        
        if (uploadedfile != null) {
            String fileName = uploadedfile.getFilename();
            
            if (fileName.endsWith(".xlsx")) {
                File file = (File)uploadedfile.getRef();
                File newFile = new File(fileName);
                InputStream fileInputStream;
                try {
                    fileInputStream = new FileInputStream(file);
                    byte[] byteFile = IOUtils.toByteArray(fileInputStream);
                    FileUtils.writeByteArrayToFile(newFile, byteFile);
                    fileInputStream.close();
                } catch (Exception e) {
                    return badRequest("Error uploading file. Please try again.");
                }
            }
            
            String ownerEmail = AuthApplication.getLocalUser(application.getUserEmail(request)).getEmail();
            DataFile.create(fileName, "", ownerEmail, DataFile.DD_UNPROCESSED);
            
            return redirect(routes.DataDictionaryAnnotator.index());
        } else {
            System.out.println("uploadedfile is null");
            return badRequest("Error uploading file. Please try again.");
        }
    }
}

