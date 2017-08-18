package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import play.*;
import play.data.Form;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;
import org.hadatac.console.http.PermissionQueries;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.UserPreRegistrationForm;
import org.hadatac.console.models.GroupRegistrationForm;
import org.hadatac.console.models.LinkedAccount;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.TokenAction;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;
import org.hadatac.metadata.loader.PermissionsContext;
import org.hadatac.metadata.loader.RDFContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class UserManagement extends Controller {

	private static final String UPLOAD_NAME = "tmp/uploads/users-spreadsheet.xls";
	private static final String UPLOAD_NAME_TTL = "tmp/uploads/user-graph.ttl";
	private static final String UPLOAD_NAME_JSON = "tmp/uploads/user-auth.json";
	
	public static String getSpreadSheetPath(){
		return UPLOAD_NAME;
	}
	
	public static String getTurtlePath(){
		return UPLOAD_NAME_TTL;
	}
	
	public static String getJsonPath(){
		return UPLOAD_NAME_JSON;
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result index(String oper) {
		return ok(users.render(oper, "", User.find(), UserGroup.find(), ""));
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result postIndex(String oper) {
		return index(oper);
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result onLinePreRegistration(String oper) {
		return ok(preregister.render(oper, UserGroup.find()));
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result postOnLinePreRegistration(String oper) {
		return onLinePreRegistration(oper);
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result onLineGroupRegistration(String oper) {
		return ok(preregisterGroup.render(oper, UserGroup.find()));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result postOnLineGroupRegistration(String oper) {
		return onLineGroupRegistration(oper);
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result grantAdminPermission(String user_uri) {
		try {
			user_uri = URLDecoder.decode(user_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		SysUser su = SysUser.findByEmail(User.find(user_uri).getEmail());
		if(su != null){
			su.addSecurityRole(AuthApplication.DATA_MANAGER_ROLE);
			su.save();
		}
		
		return redirect(routes.UserManagement.index("init"));
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postGrantAdminPermission(String user_uri) {
		return grantAdminPermission(user_uri);
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result revokeAdminPermission(String user_uri) {
		try {
			user_uri = URLDecoder.decode(user_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		SysUser su = SysUser.findByEmail(User.find(user_uri).getEmail());
		if(su != null){
			su.removeSecurityRole(AuthApplication.DATA_MANAGER_ROLE);
			su.save();
		}
		
		return redirect(routes.UserManagement.index("init"));
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postRevokeAdminPermission(String user_uri) {
		return revokeAdminPermission(user_uri);
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result assignUserAccessLevel(String user_uri, String group_uri) {
		User.changeAccessLevel(user_uri, group_uri);
		return ok(users.render("init", "", User.find(), UserGroup.find(), ""));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postAssignUserAccessLevel(String user_uri, String group_uri) {
		User.changeAccessLevel(user_uri, group_uri);
    	return ok(users.render("init", "", User.find(), UserGroup.find(), ""));
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result editGroup(String group_uri) {
		try {
			group_uri = URLDecoder.decode(group_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println(UserGroup.find().size());
		return ok(editregisterGroup.render("edit", UserGroup.find(), UserGroup.find(group_uri)));
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postEditGroup(String group_uri) {
		try {
			group_uri = URLDecoder.decode(group_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ok(editregisterGroup.render("edit", UserGroup.find(), UserGroup.find(group_uri)));
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result editUser(String user_uri) {
		try {
			user_uri = URLDecoder.decode(user_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ok(editregister.render("edit", UserGroup.find(), User.find(user_uri)));
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postEditUser(String user_uri) {
		try {
			user_uri = URLDecoder.decode(user_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ok(editregister.render("edit", UserGroup.find(), User.find(user_uri)));
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result deleteUser(String user_uri, boolean deleteAuth, boolean deleteMember) {
		try {
			user_uri = URLDecoder.decode(user_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		User.deleteUser(user_uri, deleteAuth, deleteMember);
		return redirect(routes.UserManagement.index("init"));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postDeleteUser(String user_uri, boolean deleteAuth, boolean deleteMember) {
    	return deleteUser(user_uri, deleteAuth, deleteMember);
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result backupUserAuthentication() {
		JSONArray sys_user = (JSONArray) JSONValue.parse(SysUser.outputAsJson());
		for (int i = 0; i < sys_user.size(); i++) {
			((JSONObject)sys_user.get(i)).remove("_version_");
		}
		JSONArray linked_account = (JSONArray) JSONValue.parse(LinkedAccount.outputAsJson());
		for (int i = 0; i < linked_account.size(); i++) {
			((JSONObject)linked_account.get(i)).remove("_version_");
		}
		JSONArray token = (JSONArray) JSONValue.parse(TokenAction.outputAsJson());
		for (int i = 0; i < token.size(); i++) {
			((JSONObject)token.get(i)).remove("_version_");
		}
		HashMap<String,Object> combined = new HashMap<String,Object>();
		combined.put("sys_user", sys_user);
		combined.put("linked_account", linked_account);
		combined.put("token", token);
		File ret_file = new File(getJsonPath());
		try {
			FileUtils.writeStringToFile(ret_file, new JSONObject(combined).toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ok(ret_file);
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static String recoverUserAuthentication() {
		System.out.println("Recovering User Authentication ...");
		try {
			if (!SolrUtils.clearCollection(Play.application().configuration().getString("hadatac.solr.users") 
					+ Collections.AUTHENTICATE_USERS)) {
				return "Failed to clear original \"users\" collection! ";
			}
			if (!SolrUtils.clearCollection(Play.application().configuration().getString("hadatac.solr.users") 
					+ Collections.AUTHENTICATE_ACCOUNTS)) {
				return "Failed to clear original \"linked_account\" collection! ";
			}
			if (!SolrUtils.clearCollection(Play.application().configuration().getString("hadatac.solr.users") 
					+ Collections.AUTHENTICATE_TOKENS)) {
				return "Failed to clear original \"token_action\" collection! ";
			}
			
			JSONObject combined = (JSONObject) JSONValue.parse(new FileReader(UPLOAD_NAME_JSON));
			
			if (!SolrUtils.commitJsonDataToSolr(Play.application().configuration().getString("hadatac.solr.users") 
					+ Collections.AUTHENTICATE_USERS, combined.get("sys_user").toString())) {
				return "Failed to recover \"users\" collection! ";
			}
			if (!SolrUtils.commitJsonDataToSolr(Play.application().configuration().getString("hadatac.solr.users") 
					+ Collections.AUTHENTICATE_ACCOUNTS, combined.get("linked_account").toString())) {
				return "Failed to recover \"linked_account\" collection! ";
			}
			if (!SolrUtils.commitJsonDataToSolr(Play.application().configuration().getString("hadatac.solr.users") 
					+ Collections.AUTHENTICATE_TOKENS, combined.get("token").toString())) {
				return "Failed to recover \"token_action\" collection! ";
			}
		} catch (FileNotFoundException e) {
			return "Failed to find uploaded json file! ";
		}
		
		return "Successfully recovered user authentications! ";
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postBackupUserAuthentication() {
    	return backupUserAuthentication();
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result backupUserGraph() {
		return ok(User.outputAsTurtle());
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postBackupUserGraph() {
    	return backupUserGraph();
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result sendInvitationEmail(String user_name, String user_email) {
		MyUsernamePasswordAuthProvider.getProvider().sendInvitationMailing(
				user_name, user_email, ctx());
		return redirect(routes.UserManagement.index("init"));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postSendInvitationEmail(String user_name, String user_email) {
    	return sendInvitationEmail(user_name, user_email);
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result submitPreRegistrationForm(String oper) {
		return ok(users.render(oper, "", User.find(), UserGroup.find(), "form"));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static String commitUserPreRegistration(String source) {
		System.out.println("Adding pre-registered user...");
		int mode = Feedback.WEB;
		String oper = "load";
		String message = "";
		PermissionsContext rdf = new PermissionsContext(
						"user", 
						"password", 
						Play.application().configuration().getString("hadatac.solr.permissions"), 
						false);
		
		if(source.equals("batch")){
			message = SpreadsheetProcessing.generateTTL(mode, oper, rdf, UPLOAD_NAME);
		}
		else if(source.equals("form")){
			Form<UserPreRegistrationForm> form = Form.form(UserPreRegistrationForm.class).bindFromRequest();
			UserPreRegistrationForm data = form.get();
			String usr_uri = data.getUserUri();
		    String given_name = data.getGivenName();
		    String family_name = data.getFamilyName();
		    String comment = data.getComment();
		    String email = data.getEmail();
		    String homepage = data.getHomepage();
		    String group_uri = data.getGroupUri();
		    
		    Map<String, String> pred_value_map = new HashMap<String, String>();
			pred_value_map.put("a", "foaf:Person, prov:Person");
			pred_value_map.put("foaf:name", given_name + " " + family_name);
			pred_value_map.put("foaf:familyName", family_name);
			pred_value_map.put("foaf:givenName", given_name);
			pred_value_map.put("rdfs:comment", comment);
			pred_value_map.put("foaf:mbox", email);
			pred_value_map.put("foaf:homepage", "<" + homepage + ">");
			pred_value_map.put("hadatac:isMemberOfGroup", group_uri);
			
			deleteUser(usr_uri, false, false);
			message = generateTTL(mode, oper, rdf, usr_uri, pred_value_map);
		}
		return message;
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static String commitGroupRegistration(String source) {
		System.out.println("Adding registered group...");
		int mode = Feedback.WEB;
		String oper = "load";
		String message = "";
		PermissionsContext rdf = new PermissionsContext(
						"user", 
						"password", 
						Play.application().configuration().getString("hadatac.solr.permissions"), 
						false);
		
		if(source.equals("batch")){
			message = SpreadsheetProcessing.generateTTL(mode, oper, rdf, UPLOAD_NAME);
		}
		else if(source.equals("form")){
			Form<GroupRegistrationForm> form = Form.form(GroupRegistrationForm.class).bindFromRequest();
			GroupRegistrationForm data = form.get();
			String group_uri = data.getGroupUri();
		    String group_name = data.getGroupName();
		    String comment = data.getComment();
		    String homepage = data.getHomepage();
		    String parent_group_uri = data.getParentGroupUri();
		    
		    Map<String, String> pred_value_map = new HashMap<String, String>();
			pred_value_map.put("a", "foaf:Group, prov:Group");
			pred_value_map.put("foaf:name", group_name);
			pred_value_map.put("rdfs:comment", comment);
			pred_value_map.put("foaf:homepage", "<" + homepage + ">");
			pred_value_map.put("hadatac:isMemberOfGroup", parent_group_uri);
			
			deleteUser(group_uri, false, false);
			
			message = generateTTL(mode, oper, rdf, group_uri, pred_value_map);
		}
		return message;
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static String commitUserGraphRegistration() {
		System.out.println("Adding pre-registered user...");
		int mode = Feedback.WEB;
		String message = "";
		PermissionsContext rdf = new PermissionsContext(
						"user", 
						"password", 
						Play.application().configuration().getString("hadatac.solr.permissions"), 
						false);
		try {
			Model model = RDFDataMgr.loadModel(UPLOAD_NAME_TTL);
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "SUCCESS parsing the document!");
			message += Feedback.println(mode, " ");
		} catch (Exception e) {
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "ERROR parsing the document!");
			message += Feedback.println(mode, " ");
			message += e.getMessage();
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, "==== TURTLE (TTL) UPLOADED ====");
			
			return message;
		}
		
		message += Feedback.print(mode, "   Uploading generated file.");
		rdf.loadLocalFile(mode, UPLOAD_NAME_TTL, SpreadsheetProcessing.KB_FORMAT);
		message += Feedback.println(mode, "");
		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, "   Triples after [preregistration]: " + rdf.totalTriples());
		    
		return message;
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static String generateTTL(int mode, String oper, RDFContext rdf, 
			                  String uri, Map<String, String> pred_value_map){
		String message = "";
		message += Feedback.println(mode, "   Triples before [preregistration]: " + rdf.totalTriples());
		message += Feedback.println(mode, " ");
		
		String ttl = NameSpaces.getInstance().printTurtleNameSpaceList();
		ttl = ttl + "# properties: ";
		for(String key : pred_value_map.keySet()){
			ttl = ttl + "[" + key + "] ";
		}
		ttl = ttl + "\n\n";
		
		ValueCellProcessing.validateNameSpace("hasURI");
		ttl = ttl + ValueCellProcessing.execCellValue(uri, "hasURI");
		for(String key : pred_value_map.keySet()){
			String value = pred_value_map.get(key);
			ValueCellProcessing.validateNameSpace(key);
			ttl = ttl + ValueCellProcessing.execCellValue(value, key);
			ttl = ttl + "\n";
		}
		System.out.println(ttl);
		
		String fileName = "";
		try {
			String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
			fileName = SpreadsheetProcessing.TTL_DIR + "HASNetO-" + timeStamp + ".ttl";
			FileUtils.writeStringToFile(new File(fileName), ttl);
		} catch (IOException e) {
			message += e.getMessage();
			return message;
		}
		
		String listing = "";
		try {
			listing = URLEncoder.encode(SpreadsheetProcessing.printFileWithLineNumber(mode, fileName), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, "   Generated " + fileName + " and stored locally.");
		try {
			Model model = RDFDataMgr.loadModel(fileName);
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "SUCCESS parsing the document!");
			message += Feedback.println(mode, " ");
		} catch (Exception e) {
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "ERROR parsing the document!");
			message += Feedback.println(mode, " ");
			message += e.getMessage();
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, "==== TURTLE (TTL) CODE GENERATED FROM THE SPREADSHEET ====");
			message += listing;
			return message;
		}

		if (oper.equals("load")) {
		    message += Feedback.print(mode, "   Uploading generated file.");
		    rdf.loadLocalFile(mode, fileName, SpreadsheetProcessing.KB_FORMAT);
		    message += Feedback.println(mode, "");
		    message += Feedback.println(mode, " ");
		    message += Feedback.println(mode, "   Triples after [preregistration]: " + rdf.totalTriples());
		}
		return message;
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	@BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 500 * 1024 * 1024)
	public static Result uploadFile(String file_type) {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedfile = body.getFile("pic");
		if (uploadedfile != null) {
			File file = uploadedfile.getFile();
			String file_name = "";
			if(file_type.equals("xls")){
				file_name = UPLOAD_NAME;
			}
			else if(file_type.equals("ttl")){
				file_name = UPLOAD_NAME_TTL;
			}
			else if(file_type.equals("json")){
				file_name = UPLOAD_NAME_JSON;
			}
			File newFile = new File(file_name);
			InputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(file);
				byte[] byteFile = IOUtils.toByteArray(fileInputStream);
				FileUtils.writeByteArrayToFile(newFile, byteFile);
				fileInputStream.close();
			} catch (Exception e) {
				return ok (users.render("fail", "Could not find uploaded file", User.find(), UserGroup.find(), ""));
			}
			if(file_type.equals("ttl")) {
				System.out.println("Uploaded turtle file!");
				return ok(users.render("loaded", "File uploaded successfully.", User.find(), UserGroup.find(), "turtle"));
			}
			else if(file_type.equals("json")) {
				System.out.println("Uploaded json file!");
				return ok(users.render("loaded", "File uploaded successfully.", User.find(), UserGroup.find(), "json"));
			}
			else {
				return ok(users.render("loaded", "File uploaded successfully.", User.find(), UserGroup.find(), "batch"));
			}
		}
		else {
			return ok (users.render("fail", "Error uploading file. Please try again.", User.find(), UserGroup.find(), ""));
		} 
	} 

	public static boolean isPreRegistered(String email) {
		String json = PermissionQueries.exec(PermissionQueries.PERMISSION_BY_EMAIL, email);
		SparqlQueryResults results = new SparqlQueryResults(json, false);
		if (results == null || !results.sparqlResults.values().iterator().hasNext()) 
			return false;
		TripleDocument docPermission = results.sparqlResults.values().iterator().next();
		String uri = docPermission.get("uri");
		if (uri != null && !uri.equals("")) {
			return true; 
		}
		return false;
	}

	public static String getUriByEmail(String email) {
		String json = PermissionQueries.exec(PermissionQueries.PERMISSION_BY_EMAIL, email);
		SparqlQueryResults results = new SparqlQueryResults(json, false);
		if (results == null
			|| results.sparqlResults == null
			|| !results.sparqlResults.values().iterator().hasNext()){
			return null;
		}
		TripleDocument docPermission = results.sparqlResults.values().iterator().next();
		String uri = docPermission.get("uri");
		
		return uri;
	}
	
	public static String getCurrentUserUri() {
		String uri = null;
	    final SysUser user = AuthApplication.getLocalUser(session());
	    if(null == user){
	    	uri = null;
	    }
	    else{
	    	uri = UserManagement.getUriByEmail(user.getEmail());
	    }	    
	    return uri;
	}
}
