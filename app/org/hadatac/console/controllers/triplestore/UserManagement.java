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
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.PermissionQueries;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.PermissionsContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.utils.Feedback;

public class UserManagement extends Controller {

	private static final String UPLOAD_NAME = "tmp/uploads/users-spreadsheet.xls";
	
    public static Result preRegistration(String oper) {
    	return ok(users.render(oper, "", User.find()));
    }

    public static Result postPreRegistration(String oper) {
    	return ok(users.render(oper, "", User.find()));
    }

    public static String commitPreRegistration() {
	     PermissionsContext permissions = new 
	    		 PermissionsContext("user", 
	    				         "password", 
	    				         Play.application().configuration().getString("hadatac.solr.permissions"), 
	    				         false);
	     String message = SpreadsheetProcessing.generateTTL(Feedback.WEB, "load", permissions, UPLOAD_NAME);
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
		     				 return ok (users.render("fail", "Could not save uploaded file.",User.find()));
		     			}
			    	} catch (Exception e) {
						 return ok (users.render("fail", "Could not process uploaded file.",User.find()));
				    }
			   } catch (FileNotFoundException e1) {
			       return ok (users.render("fail", "Could not find uploaded file",User.find()));
			   }
	     	   return ok(users.render("loaded", "File uploaded successfully.",User.find()));
		   } else {
			 return ok (users.render("fail", "Error uploading file. Please try again.",User.find()));
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
        //System.out.println("Email: " + email);
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
