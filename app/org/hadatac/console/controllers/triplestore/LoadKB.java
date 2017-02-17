package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.dataacquisitionmanagement.dataAcquisitionManagement;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.console.models.LabKeyLoginForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.metadata.loader.TripleProcessing;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;
import play.data.Form;
import play.mvc.BodyParser;
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
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyDataAcquisition(String oper, String content, String folder, 
    		List<String> list_names, LabKeyLoginForm auth) {
    	System.out.println(String.format("Batch loading data acquisition from \"/%s\"...", folder));
    	
    	List<String> final_names = new LinkedList<String>();
    	String user_name = "";
        String password = "";
        LabKeyLoginForm data = null;
    	if(oper.equals("init")){
    		Form<LabKeyLoginForm> form = Form.form(LabKeyLoginForm.class).bindFromRequest();
    		data = form.get();
            user_name = data.getUserName();
            password = data.getPassword();
            final_names.addAll(list_names);
    	}
    	else if(oper.contains("load")){
    		user_name = auth.getUserName();
            password = auth.getPassword();
            data = auth;
            for(String name : list_names){
            	if(name.equals("DataAcquisition")){
            		final_names.add(name);
            	}
            }
    	}
        
        String path = String.format("/%s", folder);
        String site = ConfigProp.getPropertyValue("labkey.config", "site");
    	
        NameSpaces.getInstance();
    	try {
    		String message = TripleProcessing.importDataAcquisition(site, user_name, password, path, final_names);
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", content, "", false));
    		}
    	}
    	
    	State state = new State(State.ALL);
    	final SysUser user = AuthApplication.getLocalUser(Controller.session());
		String ownerUri = UserManagement.getUriByEmail(user.getEmail());
    	List<DataAcquisition> results = DataAcquisition.find(ownerUri, state);
    	
        return ok(dataAcquisitionManagement.render(state, results, user.isDataManager()));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyKB(String oper, String content, String folder, 
    		List<String> list_names, LabKeyLoginForm auth) {
    	System.out.println(String.format("Batch loading metadata from \"/%s\"...", folder));
    	
    	List<String> final_names = new LinkedList<String>();
    	String user_name = "";
        String password = "";
        LabKeyLoginForm data = null;
    	if(oper.equals("init")){
    		Form<LabKeyLoginForm> form = Form.form(LabKeyLoginForm.class).bindFromRequest();
    		data = form.get();
            user_name = data.getUserName();
            password = data.getPassword();
            final_names.addAll(list_names);
    	}
    	else if(oper.contains("load")){
    		user_name = auth.getUserName();
            password = auth.getPassword();
            data = auth;
            
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
        String site = ConfigProp.getPropertyValue("labkey.config", "site");
    	
    	NameSpaces.getInstance();
    	MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    				         false);
    	
    	String message = "";
    	try {
    		message = TripleProcessing.generateTTL(Feedback.WEB, oper, metadata, 
    				site, user_name, password, path, final_names);
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", content, "", false));
    		}
    	}
    	
    	boolean isLoadedStudy = false;
    	if (final_names.contains("Study")){
    		isLoadedStudy = true;
    	}
    	
    	return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), oper, content, message, isLoadedStudy));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyListContent(String oper, String content, LabKeyLoginForm auth, 
    		String folder, String list_name) {
    	System.out.println(String.format("Loading data from list \"%s\"...", list_name));
    	
        String user_name = auth.getUserName();
        String password = auth.getPassword();

        String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = String.format("/%s", folder);
    	
    	NameSpaces.getInstance();
    	MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    				         false);
    	
    	String message = "";
    	try {
    		List<String> loading_list = new LinkedList<String>();
    		loading_list.add(list_name);
    		message = TripleProcessing.generateTTL(
    				Feedback.WEB, oper, metadata, site, user_name, password, path, loading_list);
    	} catch(CommandException e) {
    		if(e.getMessage().equals("Unauthorized")){
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", content, "", false));
    		}
    	}
    	
    	return ok(labkeyLoadingResult.render(auth, folder, oper, content, message));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyFolders(String oper, LabKeyLoginForm auth, String content) {
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
    	
        String site = ConfigProp.getPropertyValue("labkey.config", "site");
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
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", content, "", false));
    		}
    	}
    	return ok(getLabkeyFolders.render(data, folders, content));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result playLoadLabkeyLists(LabKeyLoginForm auth, String folder, String content) {
    	System.out.println(String.format("Accessing LabKey lists of %s", folder));
        String user_name = auth.getUserName();
        String password = auth.getPassword();
        String site = ConfigProp.getPropertyValue("labkey.config", "site");
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
    			return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), "login_failed", content, "", false));
    		}
    	}
    	return ok(getLabkeyLists.render(auth, folder, content, retMetadataLists, retDataLists));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result loadLabkeyKB(String oper, String content) {
    	return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), oper, content, "", false));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postLoadLabkeyKB(String oper, String content) {
    	return ok(syncLabkey.render(Form.form(LabKeyLoginForm.class), oper, content, "", false));
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 500 * 1024 * 1024)
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
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 500 * 1024 * 1024)
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
