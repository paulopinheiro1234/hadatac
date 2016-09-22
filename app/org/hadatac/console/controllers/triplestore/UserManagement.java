package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import play.libs.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.PermissionQueries;
import org.hadatac.console.models.UserPreRegistrationForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.PermissionsContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class UserManagement extends Controller {

	private static final String UPLOAD_NAME = "tmp/uploads/users-spreadsheet.xls";

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result preRegistration(String oper) {
		return ok(users.render(oper, "", User.find(), ""));
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result postPreRegistration(String oper) {
		return ok(users.render(oper, "", User.find(), ""));
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result onLinePreRegistration(String oper) {
		return ok(preregister.render(oper));
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result postOnLinePreRegistration(String oper) {
		return ok(preregister.render(oper));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result submitPreRegistrationForm() {
		return ok(users.render("loaded", "", User.find(), "form"));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static String commitPreRegistration(String source) {
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
		    String org_uri = data.getOrgUri();
		    
		    Map<String, String> pred_value_map = new HashMap<String, String>();
			ValueCellProcessing cellProc = new ValueCellProcessing();
			pred_value_map.put("a", "foaf:Person, prov:Person");
			pred_value_map.put("foaf:name", given_name + " " + family_name);
			pred_value_map.put("foaf:familyName", family_name);
			pred_value_map.put("foaf:givenName", given_name);
			pred_value_map.put("rdfs:comment", comment);
			pred_value_map.put("foaf:mbox", email);
			pred_value_map.put("foaf:homepage", homepage);
			pred_value_map.put("foaf:member", org_uri);
		
			message += Feedback.println(mode, "   Triples before [preregistration]: " + rdf.totalTriples());
			message += Feedback.println(mode, " ");
			
			String ttl = "";
			NameSpaces ns = NameSpaces.getInstance();
			ttl = ttl + ns.printNameSpaceList();
			
			ttl = ttl + "# properties: ";
			for(String key : pred_value_map.keySet()){
				ttl = ttl + "[" + key + "] ";
			}
			ttl = ttl + "\n\n";
			
			cellProc.validateNameSpace("hasURI");
			ttl = ttl + cellProc.execCellValue(usr_uri, "hasURI");
			for(String key : pred_value_map.keySet()){
				String value = pred_value_map.get(key);
				cellProc.validateNameSpace(key);
				ttl = ttl + cellProc.execCellValue(value, key);
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
			};

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
		}
		return message;
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
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
						return ok (users.render("fail", "Could not save uploaded file.", User.find(), ""));
					}
				} catch (Exception e) {
					return ok (users.render("fail", "Could not process uploaded file.",User.find(), ""));
				}
			} catch (FileNotFoundException e1) {
				return ok (users.render("fail", "Could not find uploaded file",User.find(), ""));
			}
			return ok(users.render("loaded", "File uploaded successfully.",User.find(), "batch"));
		} else {
			return ok (users.render("fail", "Error uploading file. Please try again.",User.find(), ""));
		} 
	} 

	public static boolean isPreRegistered(String email) {
		//System.out.println("Email: " + email);
		String json = PermissionQueries.exec(PermissionQueries.PERMISSION_BY_EMAIL, email);
		SparqlQueryResults results = new SparqlQueryResults(json, false);
		//System.out.println("results: " + results.json);
		if (results == null || !results.sparqlResults.values().iterator().hasNext()) 
			return false;
		TripleDocument docPermission = results.sparqlResults.values().iterator().next();
		String uri = docPermission.get("uri");
		if (uri != null && !uri.equals("")) {
			//System.out.println(uri);
			return true; 
		}
		return false;
	}

	public static String getUriByEmail(String email) {
		System.out.println("Email: " + email);
		String json = PermissionQueries.exec(PermissionQueries.PERMISSION_BY_EMAIL, email);
		SparqlQueryResults results = new SparqlQueryResults(json, false);
		//System.out.println("results: " + results.json);
		if (results == null || !results.sparqlResults.values().iterator().hasNext()) 
			return null;
		TripleDocument docPermission = results.sparqlResults.values().iterator().next();
		String uri = docPermission.get("uri");
		return uri;
	}
}
