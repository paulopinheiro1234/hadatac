package org.hadatac.console.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.utils.Collections;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.mvc.Controller;
import play.mvc.Result;

public class SolrSearchProxy extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getSolrSearch(String path) {
		InputStream is = null;
		try {
			URL url = new URL(path);
			is = url.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return ok(is);
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result getStudyAcquisitionDownload(){
        String path = Collections.getCollectionsName(Collections.STUDY_ACQUISITION) + 
                request().toString().split((request().path()))[1];
        System.out.println(path);
        response().setContentType("text/csv");
        return getSolrSearch(path);
    }
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getStudyAcquisition(){
		String path = Collections.getCollectionsName(Collections.STUDY_ACQUISITION) + 
				request().toString().split((request().path()))[1];
		System.out.println(path);
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
		String path = Collections.getCollectionsName(Collections.METADATA_DA) + 
				request().toString().split((request().path()))[1];
		return getSolrSearch(path);
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result getSchemaAttributes(){
		String path = Collections.getCollectionsName(Collections.SCHEMA_ATTRIBUTES) + 
				request().toString().split((request().path()))[1];
		return getSolrSearch(path);
	}
}
