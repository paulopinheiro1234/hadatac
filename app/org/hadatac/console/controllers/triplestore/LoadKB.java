package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.libs.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

public class LoadKB extends Controller {

	private static final String UPLOAD_NAME = "tmp/uploads/hasneto-spreadsheet.xls";
	
    public static Result loadKB(String oper) {
	return ok(loadKB.render(oper, ""));
    }

    public static Result postLoadKB(String oper) {
	return ok(loadKB.render(oper, ""));
    }

    public static String playLoadKB() {
	     NameSpaces.getInstance();
	     MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    				         false);
	     String message = SpreadsheetProcessing.generateTTL(Feedback.WEB, metadata, UPLOAD_NAME);
	     return message;
   }
    
    public static Result uploadFile() {
    	//System.out.println("uploadFile CALLED!");
           MultipartFormData body = request().body().asMultipartFormData();
		   FilePart uploadedfile = body.getFile("pic");
		   if (uploadedfile != null) {
		       File file = uploadedfile.getFile();
		       File newFile = new File(UPLOAD_NAME);
		       InputStream isFile;
		       try {
					isFile = new FileInputStream(file);
		            byte[] byteFile;
				    try {
		     			byteFile = IOUtils.toByteArray(isFile);
		     			try {
		     				FileUtils.writeByteArrayToFile(newFile, byteFile);
		     			} catch (Exception e) {
		     				// TODO Auto-generated catch block
		     				e.printStackTrace();
		     			}
		     			try {
		     				isFile.close();
		     			} catch (Exception e) {
		     				 return ok (loadKB.render("fail", "Could not save uploaded file."));
		     			}
			    	} catch (Exception e) {
						 return ok (loadKB.render("fail", "Could not process uploaded file."));
				    }
			   } catch (FileNotFoundException e1) {
			       return ok (loadKB.render("fail", "Could not find uploaded file"));
			   }
	     	   return ok(loadKB.render("loaded", "File uploaded successfully."));
		   } else {
			 return ok (loadKB.render("fail", "Error uploading file. Please try again."));
		   } 
    } 
    
}
