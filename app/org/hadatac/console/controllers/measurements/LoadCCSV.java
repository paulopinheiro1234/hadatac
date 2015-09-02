package org.hadatac.console.controllers.measurements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.libs.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.views.html.measurements.*;
import org.hadatac.data.loader.ccsv.Parser;
import org.hadatac.data.loader.util.Arguments;
import org.hadatac.data.loader.util.FileFactory;
import org.hadatac.data.model.DatasetParsingResult;
import org.hadatac.utils.Feedback;
import org.kohsuke.args4j.Option;

public class LoadCCSV extends Controller {

	static FileFactory files;

	private static final String UPLOAD_NAME = "uploads/latest.ccsv";
	
    public static Result loadCCSV(String oper) {
	return ok(loadCCSV.render(oper, ""));
    }

    public static Result postLoadCCSV(String oper) {
	return ok(loadCCSV.render(oper, ""));
    }

    public static String playLoadCCSV() {
    	DatasetParsingResult result;
        String message = "";
		Arguments arguments = new Arguments();
		arguments.setInputPath(UPLOAD_NAME);
		arguments.setInputType("CCSV");
		arguments.setOutputPath("upload/");
		arguments.setVerbose(true);
		arguments.setPv(false);

		File inputFile = new File(arguments.getInputPath());
		files = new FileFactory(arguments);
		files.setFile(inputFile, inputFile.getName());
			
		try {
			files.openFile("log", "w");
			files.writeln("log", "[START] " + arguments.getInputPath() + " generating measurements.");
			
			if (arguments.getInputType().equals("CCSV")) {
				Parser parser = new Parser();
				if (arguments.isPv()) {
					result = parser.validate(Feedback.WEB, files);
					message += result.getMessage();
				} else {
					result = parser.validate(Feedback.WEB, files);
					message += result.getMessage();
					if (result.getStatus() == 0) {
						parser.index(Feedback.WEB);
					}
				}
			}
			
			files.writeln("log", "[END] " + arguments.getInputPath() + " generating measurements.");
			files.closeFile("log", "w");
		} catch (Exception e) {
			e.printStackTrace();
		}		
		message += "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] end of file "; //+ ccsvFile.getName();
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
		     				 return ok (loadCCSV.render("fail", "Could not save uploaded file."));
		     			}
			    	} catch (Exception e) {
						 return ok (loadCCSV.render("fail", "Could not process uploaded file."));
				    }
			   } catch (FileNotFoundException e1) {
			       return ok (loadCCSV.render("fail", "Could not find uploaded file"));
			   }
	     	   return ok(loadCCSV.render("loaded", "File uploaded successfully."));
		   } else {
			 return ok (loadCCSV.render("fail", "Error uploading file. Please try again."));
		   } 
    } 
    
}
