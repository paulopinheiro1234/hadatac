package org.hadatac.console.controllers.studies;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.StudyForm;
import org.hadatac.console.views.html.studies.*;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Study;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DeleteStudy extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String study_uri) {

        Study study = null;
        String result = "";
        try {
            if (study_uri != null) {
            	study_uri = URLDecoder.decode(study_uri, "UTF-8");
            } else {
            	study_uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	
        if (!study_uri.equals("")) {
	    
            study = Study.find(study_uri);
	    
            study.delete();

            result = deleteStudy(DynamicFunctions.replaceURLWithPrefix(study_uri));
            
            return ok(deleteStudy.render(study, result));
        }
        
        return ok(deleteStudy.render(study, result));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String study_uri) {
        return index(study_uri);
	}

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static String deleteStudy(String studyUri) {
	String result = "";
	NameSpaces.getInstance();
	MetadataContext metadata = new MetadataContext("user", "password", 
						       Play.application().configuration().getString("hadatac.solr.triplestore"), false);
	result = metadata.cleanStudy(Feedback.WEB, studyUri);
	
	return result;
    } 
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String study_uri) {
        Study study = null;
	
        try {
            if (study_uri != null) {
            	study_uri = URLDecoder.decode(study_uri, "UTF-8");
            } else {
            	study_uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	
        if (!study_uri.equals("")) {
            study = Study.find(study_uri);
        }
	
        study.delete();
	
	return ok(studyConfirm.render("Delete Study", study));
    }
}
