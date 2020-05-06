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
import org.hadatac.console.models.LabKeyLoginForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.metadata.loader.TripleProcessing;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.labkey.remoteapi.CommandException;

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
    public Result playLoadLabkeyDataAcquisition(String oper, String content, 
    		String folder, String study_uri) {
    	System.out.println(String.format("Batch loading data acquisition from \"/%s\"...", folder));
    	
        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name == null || password == null) {
        	redirect(routes.LoadKB.loadLabkeyKB("init", content));
        }
        
        String path = String.format("/%s", folder);
        String site = ConfigProp.getLabKeySite();
        
    	try {
    		final SysUser user = AuthApplication.getLocalUser(Controller.session());
    		String ownerUri = UserManagement.getUriByEmail(user.getEmail());
    		if (null == ownerUri) {
    			return badRequest("Cannot find corresponding URI for the current logged in user!");
    		}
    		ParsingResult result = TripleProcessing.importDataAcquisition(
    				site, path, user_name, password, study_uri);
    		if (result.getStatus() != 0) {
    			return ok(labkeyLoadingResult.render(folder, oper, content, result.getMessage()));
    		}
    	} catch (CommandException e) {
    		if (e.getMessage().equals("Unauthorized")) {
    			return ok(syncLabkey.render("login_failed",
    					routes.LoadKB.playLoadLabkeyFolders("init", content).url(), ""));
    		}
    	}
    	
    	return redirect(org.hadatac.console.controllers.dataacquisitionmanagement.
    			routes.DataAcquisitionManagement.index(State.ACTIVE));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result playLoadLabkeyKB(String oper, String content, String folder, 
    		List<String> list_names) {
    	System.out.println(String.format("Batch loading metadata from \"/%s\"...", folder));
    	
    	List<String> final_names = new LinkedList<String>();
    	String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name == null || password == null) {
        	redirect(routes.LoadKB.loadLabkeyKB("init", content));
        }
    	if(oper.equals("init")){
            final_names.addAll(list_names);
    	}
    	else if(oper.contains("load")){            
            if(oper.equals("load_instance_data")){
            	// get request value from submitted form
                Map<String, String[]> name_map = request().body().asFormUrlEncoded();
                final_names.addAll(name_map.keySet());
                oper = "load";
            }
            else{
            	final_names.addAll(list_names);
            	System.out.println(final_names.size());
            }
    	}

        String path = String.format("/%s", folder);
        String site = ConfigProp.getLabKeySite();
    	
    	NameSpaces.getInstance();
    	MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         ConfigFactory.load().getString("hadatac.solr.triplestore"), 
	    				         false);
    	
    	String message = "";
    	try {
    		message = TripleProcessing.generateTTL(Feedback.WEB, oper, metadata, 
    				site, user_name, password, path, final_names);
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render("login_failed",
    					routes.LoadKB.playLoadLabkeyFolders("init", content).url(), ""));
    		}
    	}
    	
    	return ok(syncLabkey.render(oper,
    			routes.LoadKB.playLoadLabkeyFolders("init", content).url(),
    			message));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result playLoadLabkeyListContent(
    		String oper, String content, String folder, String list_name) {
    	System.out.println(String.format("Loading data from list \"%s\"...", list_name));
    	
        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name == null || password == null) {
        	redirect(routes.LoadKB.loadLabkeyKB("init", content));
        }

        String site = ConfigProp.getLabKeySite();
        String path = String.format("/%s", folder);
    	
    	NameSpaces.getInstance();
    	MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         ConfigFactory.load().getString("hadatac.solr.triplestore"), 
	    				         false);
    	
    	String message = "";
    	try {
    		List<String> loading_list = new LinkedList<String>();
    		loading_list.add(list_name);
    		message = TripleProcessing.generateTTL(
    				Feedback.WEB, oper, metadata, site, user_name, password, path, loading_list);
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render("login_failed",
    					routes.LoadKB.playLoadLabkeyFolders("init", content).url(), ""));
    		}
    	}
    	
    	return ok(labkeyLoadingResult.render(folder, oper, content, message));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result playLoadLabkeyFolders(String oper, String content) {
    	System.out.println("Looking up LabKey folders...");
    	
    	String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name == null || password == null) {
        	Form<LabKeyLoginForm> form = formFactory.form(LabKeyLoginForm.class).bindFromRequest();
            user_name = form.get().getUserName();
            password = form.get().getPassword();
        }
        
        String site = ConfigProp.getLabKeySite();
        String path = "/";
    	
    	List<String> folders = null;
    	try {
    		folders = TripleProcessing.getLabKeyFolders(site, user_name, password, path);
    		if (folders != null) {
                folders.sort(new Comparator<String>() {
        			@Override
    				public int compare(String o1, String o2) {
    					return o1.compareTo(o2);
    				}
    			});
    		}
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")) {
    			return ok(syncLabkey.render("login_failed", 
    					routes.LoadKB.playLoadLabkeyFolders("init", content).url(), ""));
    		}
    	}
    	
		session().put("LabKeyUserName", user_name);
        session().put("LabKeyPassword", password);
    	
    	return ok(getLabkeyFolders.render(folders, content));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result playLoadLabkeyLists(String folder, String content) {
    	System.out.println(String.format("Accessing LabKey lists of %s", folder));
        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name == null || password == null) {
        	redirect(routes.LoadKB.loadLabkeyKB("init", "knowledge"));
        }
        String site = ConfigProp.getLabKeySite();
        String path = String.format("/%s", folder);
    	
    	List<String> retMetadataLists = null;
    	List<String> retDataLists = null;
    	try {
    		if(content.equals("ontology")){
    			retMetadataLists = TripleProcessing.getLabKeyMetadataLists(site, user_name, password, path);
    			retMetadataLists.sort(new Comparator<String>() {
        			@Override
    				public int compare(String o1, String o2) {
    					return o1.compareTo(o2);
    				}
    			});
    		}
    		else if(content.equals("knowledge")){
    			retDataLists = TripleProcessing.getLabKeyInstanceDataLists(site, user_name, password, path);
    			retDataLists.sort(new Comparator<String>() {
        			@Override
    				public int compare(String o1, String o2) {
    					return o1.compareTo(o2);
    				}
    			});
    		}
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render("login_failed",
    					routes.LoadKB.playLoadLabkeyFolders("init", content).url(), ""));
    		}
    	}
    	return ok(getLabkeyLists.render(folder, content, retMetadataLists, retDataLists));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result loadLabkeyKB(String oper, String content) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return ok(syncLabkey.render(oper,
    				routes.LoadKB.playLoadLabkeyFolders("init", content).url(), ""));
    	}
    	
    	return playLoadLabkeyFolders(oper, content);
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postLoadLabkeyKB(String oper, String content) {
    	return loadLabkeyKB(oper, content);
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result logInLabkey(String nextCall) {
        return ok(syncLabkey.render("init", routes.LoadKB.postLogInLabkey(nextCall).url(), ""));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postLogInLabkey(String nextCall) {
    	Form<LabKeyLoginForm> form = formFactory.form(LabKeyLoginForm.class).bindFromRequest();
    	String site = ConfigProp.getLabKeySite();
        String path = "/";
        String user_name = form.get().getUserName();
        String password = form.get().getPassword();
    	LabkeyDataHandler loader = new LabkeyDataHandler(
    			site, path, user_name, password);
    	try {
    		loader.checkAuthentication();
    		session().put("LabKeyUserName", user_name);
            session().put("LabKeyPassword", password);
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render("login_failed", 
    					nextCall, ""));
    		}
    	}
    	
    	return redirect(nextCall);
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
