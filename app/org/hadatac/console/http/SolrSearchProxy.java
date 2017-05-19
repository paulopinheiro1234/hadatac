package org.hadatac.console.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.util.List;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.utils.Collections;
import org.hadatac.entity.pojo.Measurement;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.DynamicForm;
import play.data.Form;

public class SolrSearchProxy extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getSolrSearch(String path) {
		InputStream is = null;
		URL url = null;
		HttpURLConnection con = null;

		try {
		    url = new URL(path.substring(0, path.indexOf('?')));
		    con = (HttpURLConnection) url.openConnection();
		    con.setRequestMethod("POST");
		    con.setRequestProperty("Accept-Charset", "utf-8");
		    con.setDoOutput(true);
		    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		    wr.writeBytes(path.substring(path.indexOf('?')+1, path.length()));
		    wr.flush();
		    wr.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}

		try (OutputStream output = con.getOutputStream()) {
		    output.write(path.getBytes("utf-8"));
		} catch (IOException e) {
		    e.printStackTrace();
		}
 
		try {
		    is = con.getInputStream();
		} catch (IOException e) {
		    e.printStackTrace();
		}
 
		if (is != null) {
		    return ok(is);
		} else {
		    return ok();
		}
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getIndicatorValueDownload(String lm) {
		File file = new File(lm);
		return ok(file);
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result getDataAcquisitionDownload(){
	DynamicForm form = Form.form().bindFromRequest();
	String request_fl;
	String request_wt;
	String request_rows;
    String request_q;
    String request_encoding = "";

	if (form.data().size() == 0) {
	    return badRequest("[ERROR] getDataAcuisitionDownload expects some data");
        } else {
        request_fl = form.get("fl");
	    request_wt = form.get("wt");
	    request_rows = form.get("rows");
	    request_q = form.get("q");
	    request_encoding = "wt=" + request_wt + "&rows=" + request_rows + "&q=" + request_q;
	    if (!request_fl.equals("")) {
	        request_encoding += "&fl=" + request_fl; 
	    }
	    //System.out.println("Request: " + request_encoding);
	}
        String path = Collections.getCollectionsName(Collections.DATA_ACQUISITION) + "/select" +
	    //"?" + URLEncoder.encode(request_encoding);
	    "?" + request_encoding;
            response().setContentType("text/csv");
        return getSolrSearch(path);
    }
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result getStudyAcquisitionDownload(){
        String path = Collections.getCollectionsName(Collections.STUDY_ACQUISITION) + 
                request().toString().split((request().path()))[1];
        //System.out.println(path);
        response().setContentType("text/csv");
        return getSolrSearch(path);
    }
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getStudyAcquisition(){
		String path = Collections.getCollectionsName(Collections.STUDY_ACQUISITION) + 
				request().toString().split((request().path()))[1];
		return getSolrSearch(path);
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getSubjectAcquisition(){
		String path = Collections.getCollectionsName(Collections.SUBJECTS_ACQUISITION) + 
				request().toString().split((request().path()))[1];
		return getSolrSearch(path);
	}

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getSampleAcquisition(){
		String path = Collections.getCollectionsName(Collections.SAMPLES_ACQUISITION) + 
				request().toString().split((request().path()))[1];
		return getSolrSearch(path);
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getAnalytesAcquisition(){
		String path = Collections.getCollectionsName(Collections.ANALYTES_ACQUISITION) + 
				request().toString().split((request().path()))[1];
		return getSolrSearch(path);
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getMetadataDataAcquisition(){
//		String path = Collections.getCollectionsName(Collections.METADATA_DA) + 
//				request().toString().split((request().path()))[1];
		String path = Collections.getCollectionsName(Collections.DATA_COLLECTION) + "/select" +
				request().toString().split((request().path()))[1];
		return getSolrSearch(path);
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getDataAcquisition(){
//		String path = Collections.getCollectionsName(Collections.METADATA_DA) + 
//				request().toString().split((request().path()))[1];
		String path = Collections.getCollectionsName(Collections.METADATA_AQUISITION) + "/select" +
				request().toString().split((request().path()))[1];
		System.out.println("Solr Search Path: " + path);
		return getSolrSearch(path);
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getSchemaAttributes(){
		String path = Collections.getCollectionsName(Collections.SA_ACQUISITION) + "/select" +
				request().toString().split((request().path()))[1];
		return getSolrSearch(path);
	}
	
	public static Result getApiStudyDownload() {
		DynamicForm form = Form.form().bindFromRequest();
		String request_token;
		
		if (form.data().size() == 0) {
			return badRequest("[ERROR] getApiStudyDownload expects some data");
		} else {
	        request_token = form.get("token");
	        if (!request_token.equals("TESTTOKEN")) {
	        	return badRequest("[ERROR] getApiStudyDownload token mismatch");
	        }
	        return getStudyAcquisitionDownload();
		}
	}
	
	public static Result getApiStudyVariableDownload() {
		DynamicForm form = Form.form().bindFromRequest();
		String request_token;
		
		if (form.data().size() == 0) {
			return badRequest("[ERROR] getApiStudyVariableDownload expects some data");
		} else {
	        request_token = form.get("token");
	        if (!request_token.equals("TESTTOKEN")) {
	        	return badRequest("[ERROR] getApiStudyVariableDownload token mismatch");
	        }
	        return getStudyAcquisitionDownload();
		}
	}
	
	public static Result getApiStudyVariableDataDownload() {
		DynamicForm form = Form.form().bindFromRequest();
		String request_token;
		
		if (form.data().size() == 0) {
			return badRequest("[ERROR] getApiStudyVariableDataDownload expects some data");
		} else {
	        request_token = form.get("token");
	        if (!request_token.equals("TESTTOKEN")) {
	        	return badRequest("[ERROR] getApiStudyVariableDataDownload token mismatch");
	        }
	        return getStudyAcquisitionDownload();
		}
	}
}
