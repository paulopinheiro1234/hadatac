package org.hadatac.console.controllers.fileviewer;

import org.hadatac.utils.ConfigProp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;

import org.hadatac.console.views.html.fileviewer.*;

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
            File file = (File)uploadedfile.getFile();
            System.out.println("name: " + uploadedfile.getFilename());
            return ok("File uploaded successfully.");
        } else {
            System.out.println("uploadedfile is null");
            return badRequest("Error uploading file. Please try again.");
        }
    }
}

