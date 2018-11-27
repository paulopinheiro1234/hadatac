package org.hadatac.console.controllers.fileviewer;

import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileNotFoundException;

import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.views.html.fileviewer.*;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.data.loader.GeneratorChain;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.data.loader.SpreadsheetRecordFile;

import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class SDDEditor extends Controller {
    
    public Result index() {
        return ok(sdd_editor.render());
    }
    
    public Result postIndex() {
        return index();
    }
    
    public Result uploadSDDFile() {
        System.out.println("uploadSDDFile CALLED!");
        
        FilePart uploadedfile = request().body().asMultipartFormData().getFile("file");
        
        if (uploadedfile != null) {
            if (uploadedfile.getFilename().endsWith(".xlsx")) {
                File file = (File)uploadedfile.getFile();
                String newFileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()) + ".xlsx";
                
                // Ingest the uploaded SDD file
                RecordFile recordFile = new SpreadsheetRecordFile(file, newFileName, "InfoSheet");
                if (!recordFile.isValid()) {
                    return ok(Feedback.println(Feedback.WEB, "[ERROR] The Info sheet is missing in this SDD file. "));
                }
                
                GeneratorChain chain = AnnotationWorker.annotateSDDFile(recordFile);
                if (null != chain) {
                    chain.generate(false);
                }
                
                AnnotationLog log = AnnotationLog.find(newFileName);
                if (null != log) {
                    String strLog = log.getLog();
                    log.delete();
                    return ok(strLog);
                }
            }
            
            return ok("File uploaded successfully.");
        } else {
            System.out.println("uploadedfile is null");
            return badRequest("Error uploading file. Please try again.");
        }
    }
}

