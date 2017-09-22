package org.hadatac.console.controllers.annotator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.GetSparqlQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.TripleDocument;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import org.hadatac.console.views.html.error_page;
import org.hadatac.console.views.html.annotator.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class FileProcessing extends Controller {

    private static final String UPLOAD_PATH = "tmp/uploads/";

    public static String[] extractFields(String str) {
	if (str == null || str.equals("")) {
	    return null;
	}
	String line = str;
	if (str.indexOf('\n') >= 0) {
	    line = str.substring(0, str.indexOf('\n'));
	}
	System.out.println("Line: [" + line + "]");
	StringTokenizer st = new StringTokenizer(line, ",");
	String[] fields = new String[st.countTokens()];
	int pos = 0;
	while (st.hasMoreElements()) {
	    fields[pos++] = (String)(st.nextElement());
	}
	return fields;
    }

    public static SparqlQueryResults getQueryResults(String tabName) {
	    SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults = null;
    	String query_json = null;
        try {
            query_json = query_submit.executeQuery(tabName);
            System.out.println("query_json = " + query_json);
            theResults = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            e1.printStackTrace();
        }
		return theResults;
	}

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 500 * 1024 * 1024)
    public static Result uploadFile(String handler_json) {
    	try {
			handler_json = URLDecoder.decode(handler_json, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	System.out.println(handler_json);

    	ObjectMapper mapper = new ObjectMapper();
    	CSVAnnotationHandler handler = null;
    	try {
			handler = mapper.readValue(handler_json, CSVAnnotationHandler.class);
		} catch (Exception e) {
			e.printStackTrace();
			return ok (uploadCSV.render(null, "fail", "Lost deployment information."));
		}

        MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedfile = body.getFile("pic");
		if (uploadedfile != null) {
		       File file = uploadedfile.getFile();
		       handler.setDatasetName(UPLOAD_PATH + uploadedfile.getFilename());
		       File newFile = new File(handler.getDatasetName());
		       InputStream isFile;
		       InputStream isFile2;
		       try {
					isFile = new FileInputStream(file);
					isFile2 = new FileInputStream(file);
		            byte[] byteFile;
				    try {
		     			byteFile = IOUtils.toByteArray(isFile);
		     			String str = IOUtils.toString(isFile2, "UTF-8");
		     			//System.out.println("File Processing: [" + str + "]");
		     			handler.setFields(extractFields(str));
		     			try {
		     				FileUtils.writeByteArrayToFile(newFile, byteFile);
		     			} catch (Exception e) {
		     				// TODO Auto-generated catch block
		     				e.printStackTrace();
		     			}
		     			try {
		     				isFile.close();
		     			} catch (Exception e) {
		     				 return ok (uploadCSV.render(null, "fail", "Could not save uploaded file."));
		     			}
			    	} catch (Exception e) {
						 return ok (uploadCSV.render(null, "fail", "Could not process uploaded file."));
				    }
			   } catch (FileNotFoundException e1) {
			       return ok (uploadCSV.render(null, "fail", "Could not find uploaded file"));
			   }
	     	   return ok(measurementsSpec.render(handler, getQueryResults("Entities"), getQueryResults("Units")));
		} else {
		   return ok (uploadCSV.render(null, "fail", "Error uploading file. Please try again."));
		}
    }

}
