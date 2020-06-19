package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.console.controllers.triplestore.routes;
import org.hadatac.console.models.SysUser;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.metadata.loader.TripleProcessing;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

public class LoadKB extends Controller {

	private static final String UPLOAD_NAME = "tmp/uploads/hasneto-spreadsheet.xls";
	private static final String UPLOAD_TURTLE_NAME = "tmp/uploads/turtle.ttl";
	
	@Inject
	private FormFactory formFactory;
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result loadKB(String oper) {
		return ok(loadKB.render(oper, ""));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postLoadKB(String oper) {
		return ok(loadKB.render(oper, ""));
    }

    public static String playLoadKB(String oper) {
	     NameSpaces.getInstance();
	     MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         ConfigFactory.load().getString("hadatac.solr.triplestore"), 
	    				         false);
	     String message = "";
	     if(oper.equals("turtle")){
	    	 message = TripleProcessing.processTTL(Feedback.WEB, oper, metadata, UPLOAD_TURTLE_NAME);
	     }
	     else{
	    	 message = SpreadsheetProcessing.generateTTL(Feedback.WEB, oper, metadata, UPLOAD_NAME); 
	     }
	     return message;
	}
    
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    @BodyParser.Of(value = BodyParser.MultipartFormData.class)
    public Result uploadFile(String oper) {
    	System.out.println("uploadFile CALLED!");
    	FilePart uploadedfile = request().body().asMultipartFormData().getFile("pic");
    	if (uploadedfile != null) {
    		File file = (File)uploadedfile.getFile();
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
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    @BodyParser.Of(value = BodyParser.MultipartFormData.class)
    public Result uploadTurtleFile(String oper) {
    	System.out.println("uploadTurtleFile CALLED!");
    	FilePart uploadedfile = request().body().asMultipartFormData().getFile("pic");
    	if (uploadedfile != null) {
    		File file = (File)uploadedfile.getFile();
    		File newFile = new File(UPLOAD_TURTLE_NAME);
    		InputStream isFile;
    		try {
    			isFile = new FileInputStream(file);
    			byte[] byteFile;
    			try {
    				byteFile = IOUtils.toByteArray(isFile);
    				try {
    					FileUtils.writeByteArrayToFile(newFile, byteFile);
    				} catch (Exception e) {
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
    		return ok(loadKB.render("turtle", "File uploaded successfully."));
    	} else {
    		return ok (loadKB.render("fail", "Error uploading file. Please try again."));
    	}
    }
}
