package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.console.models.LabKeyLoginForm;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.metadata.loader.TripleProcessing;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

public class LoadKB extends Controller {

	private static final String UPLOAD_NAME = "tmp/uploads/hasneto-spreadsheet.xls";
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result loadKB(String oper) {
		return ok(loadKB.render(oper, ""));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postLoadKB(String oper) {
		return ok(loadKB.render(oper, ""));
    }

    public static String playLoadKB(String oper) {
	     NameSpaces.getInstance();
	     MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    				         false);
	     String message = SpreadsheetProcessing.generateTTL(Feedback.WEB, oper, metadata, UPLOAD_NAME);
	     return message;
	}
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyKB(String oper) {
    	System.out.println("Labkey called!");
    	
    	Form<LabKeyLoginForm> form = Form.form(LabKeyLoginForm.class).bindFromRequest();
        LabKeyLoginForm data = form.get();
        String user_name = data.getUserName();
        String password = data.getPassword();
        
        Properties prop = new Properties();
        String fileName = "conf/labkey.config";
		try {
			InputStream is = new FileInputStream(fileName);
			prop.load(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        String path = prop.getProperty("folder");
        String site = prop.getProperty("site");
    	
    	NameSpaces.getInstance();
    	MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    				         false);
    	
    	String message = "";
    	try {
    		message = TripleProcessing.generateTTL(Feedback.WEB, oper, metadata, site, user_name, password, path);
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", ""));
    		}
    	}
    	
    	return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), oper, message));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result loadLabkeyKB(String oper) {
    	return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), oper, ""));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postLoadLabkeyKB(String oper) {
    	return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), oper, ""));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result uploadFile(String oper) {
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
	     	   return ok(loadKB.render(oper, "File uploaded successfully."));
		   } else {
			   return ok (loadKB.render("fail", "Error uploading file. Please try again."));
		}
    }
}
