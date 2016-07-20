package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
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
	private static final String UPLOAD_TURTLE_NAME = "tmp/uploads/turtle.ttl";
	
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
	     String message = "";
	     if(oper.equals("turtle")){
	    	 message = TripleProcessing.processTTL(Feedback.WEB, oper, metadata, UPLOAD_TURTLE_NAME);
	     }
	     else{
	    	 message = SpreadsheetProcessing.generateTTL(Feedback.WEB, oper, metadata, UPLOAD_NAME); 
	     }
	     return message;
	}
    
    public static Properties loadConfig() {
    	Properties prop = new Properties();
		try {
			InputStream is = LoadKB.class.getClassLoader().getResourceAsStream("labkey.config");
			prop.load(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyKB(String oper, String folder, LabKeyLoginForm auth) {
    	System.out.println(String.format("Batch loading data from \"/%s\"...", folder));
    	
    	String user_name = "";
        String password = "";
        LabKeyLoginForm data = null;
    	if(oper.equals("init")){
    		Form<LabKeyLoginForm> form = Form.form(LabKeyLoginForm.class).bindFromRequest();
    		data = form.get();
            user_name = data.getUserName();
            password = data.getPassword();
    	}
    	else if(oper.equals("load")){
    		user_name = auth.getUserName();
            password = auth.getPassword();
            data = auth;
    	}
        
        Properties prop = loadConfig();
        String path = String.format("/%s", folder);
        String site = prop.getProperty("site");
    	
    	NameSpaces.getInstance();
    	MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    				         false);
    	
    	String message = "";
    	try {
    		message = TripleProcessing.generateTTL(Feedback.WEB, oper, metadata, 
    				site, user_name, password, path, null);
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", ""));
    		}
    	}
    	
    	return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), oper, message));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyListContent(String oper, LabKeyLoginForm auth, 
    		String folder, String list_name) {
    	System.out.println(String.format("Loading data from list \"%s\"...", list_name));
    	
        String user_name = auth.getUserName();
        String password = auth.getPassword();
        
        Properties prop = loadConfig();
        String site = prop.getProperty("site");
        String path = String.format("/%s", folder);
    	
    	NameSpaces.getInstance();
    	MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    				         false);
    	
    	String message = "";
    	try {
    		message = TripleProcessing.generateTTL(
    				Feedback.WEB, oper, metadata, site, user_name, password, path, list_name);
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", ""));
    		}
    	}
    	
    	return ok(labkeyLoadingResult.render(auth, folder, oper, message));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyFolders(String oper, LabKeyLoginForm auth) {
    	System.out.println("Looking up LabKey folders...");
    	
    	String user_name = "";
        String password = "";
        LabKeyLoginForm data = null;
    	if(oper.equals("init")){
    		Form<LabKeyLoginForm> form = Form.form(LabKeyLoginForm.class).bindFromRequest();
    		data = form.get();
            user_name = data.getUserName();
            password = data.getPassword();
    	}
    	else if(oper.equals("load")){
    		user_name = auth.getUserName();
            password = auth.getPassword();
            data = auth;
    	}
    	
        Properties prop = loadConfig();
        String site = prop.getProperty("site");
        String path = "/";
    	
    	List<String> folders = null;
    	try {
    		folders = TripleProcessing.getLabKeyFolders(site, user_name, password, path); 		
    		folders.sort(new Comparator<String>() {
    			@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", ""));
    		}
    	}
    	return ok(getLabkeyFolders.render(data, folders));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyLists(LabKeyLoginForm auth, String folder) {
    	System.out.println(String.format("Accessing LabKey lists of %s", folder));
        String user_name = auth.getUserName();
        String password = auth.getPassword();
        
        Properties prop = loadConfig();
        String site = prop.getProperty("site");
        String path = String.format("/%s", folder);
    	
    	List<String> retLists = null;
    	try {
    		retLists = TripleProcessing.getLabKeyLists(site, user_name, password, path);
    		retLists.sort(new Comparator<String>() {
    			@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", ""));
    		}
    	}
    	return ok(getLabkeyLists.render(auth, folder, retLists));
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
    	System.out.println("uploadFile CALLED!");
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
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result uploadTurtleFile(String oper) {
    	System.out.println("uploadTurtleFile CALLED!");
    	MultipartFormData body = request().body().asMultipartFormData();
    	FilePart uploadedfile = body.getFile("pic");
    	if (uploadedfile != null) {
    		File file = uploadedfile.getFile();
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
