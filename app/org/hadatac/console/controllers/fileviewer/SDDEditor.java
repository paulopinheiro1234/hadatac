package org.hadatac.console.controllers.fileviewer;

import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.FileNotFoundException;

//import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.fileviewer.*;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.data.loader.GeneratorChain;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.data.loader.SpreadsheetRecordFile;
import org.hadatac.entity.pojo.DataFile;

import play.mvc.Http;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class SDDEditor extends Controller {
    @Inject
    Application application;
    
    public Result index(Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        
        List<DataFile> files = null;

        String path = ConfigProp.getPathWorking();

        if (user.isDataManager()) {
            files = DataFile.findByStatus(DataFile.DD_UNPROCESSED);
            files.addAll(DataFile.findByStatus(DataFile.DD_PROCESSED));
        } else {
            files = DataFile.find(user.getEmail(), DataFile.DD_UNPROCESSED);
            files.addAll(DataFile.find(user.getEmail(), DataFile.DD_PROCESSED));
        }

        DataFile.filterNonexistedFiles(path, files);
        
        return ok(sdd_editor.render(files, user.isDataManager(),application.getUserEmail(request)));
    }
    
    public Result postIndex(Http.Request request) {
        return index(request);
    }
    
    public Result uploadSDDFile(Http.Request request) {
        System.out.println("uploadSDDFile CALLED!");
        
        FilePart uploadedfile = request.body().asMultipartFormData().getFile("file");
        
        if (uploadedfile != null) {
            if (uploadedfile.getFilename().endsWith(".xlsx")) {
                File file = (File)uploadedfile.getRef();
                String newFileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()) + ".xlsx";
                
                // Ingest the uploaded SDD file
                RecordFile recordFile = new SpreadsheetRecordFile(file, newFileName, "InfoSheet");
                
                DataFile dataFile = DataFile.create(
                        newFileName, "", AuthApplication.getLocalUser(application.getUserEmail(request)).getEmail(),
                        DataFile.WORKING);
                
                dataFile.setRecordFile(recordFile);
                
                GeneratorChain chain = AnnotationWorker.annotateSDDFile(dataFile);
                if (null != chain) {
                    chain.generate(false);
                }
                
                String strLog = dataFile.getLog();
                dataFile.delete();
                
                return ok(strLog);
            }
            
            return ok("File uploaded successfully.");
        } else {
            System.out.println("uploadedfile is null");
            return badRequest("Error uploading file. Please try again.");
        }
    }
}

