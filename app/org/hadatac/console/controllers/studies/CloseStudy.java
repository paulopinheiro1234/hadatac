package org.hadatac.console.controllers.studies;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.StudyForm;
import org.hadatac.console.views.html.studies.*;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Study;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class CloseStudy extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String study_uri) {

    	StudyForm studyForm = new StudyForm();
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
    		/*
    		 *  Add deployment information into handler
    		 */
    		if (study.getDataAcquisitions() != null) {
    			Iterator<DataAcquisition> iterDataAcquisitions = study.getDataAcquisitions().iterator();
    			while (iterDataAcquisitions.hasNext()) {
    				studyForm.addDataAcquisition(((DataAcquisition)iterDataAcquisitions.next()).getLabel());
    			}
    		}
    		studyForm.setStartDateTime(study.getStartedAt());
 
            System.out.println("closing deployment");
            return ok(closeStudy.render(study_uri, studyForm));
    	}
    	
    	return ok(closeStudy.render(study_uri, studyForm));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String study_uri) {
    	StudyForm studyForm = new StudyForm();
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
    		/*
    		 *  Add study da information into handler
    		 */
    		if (study.getDataAcquisitions() != null) {
    			Iterator<DataAcquisition> iterDataAcquisitions = study.getDataAcquisitions().iterator();
    			while (iterDataAcquisitions.hasNext()) {
    				studyForm.addDataAcquisition(((DataAcquisition)iterDataAcquisitions.next()).getLabel());
    			}
    		}
    		studyForm.setStartDateTime(study.getStartedAt());
 
            System.out.println("closing study");
            return ok(closeStudy.render(study_uri, studyForm));
    	}
    	return ok(closeStudy.render(study_uri, studyForm));
        
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
    	
    	Form<StudyForm> form = Form.form(StudyForm.class).bindFromRequest();
    	StudyForm data = form.get();

        String dateStringFromJs = data.getEndDateTime();
        String endDateString = "";
        DateFormat jsFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
        Date dateFromJs;
		try {
			dateFromJs = jsFormat.parse(dateStringFromJs);
	        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	        endDateString = isoFormat.format(dateFromJs);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		study.close(endDateString);
		
		if (study.getDataAcquisitions() != null) {
			Iterator<DataAcquisition> iterDataAcquisitions = study.getDataAcquisitions().iterator();
			while (iterDataAcquisitions.hasNext()) {
				data.addDataAcquisition(((DataAcquisition)iterDataAcquisitions.next()).getLabel());
			}
		}
		data.setStartDateTime(study.getStartedAt());
		data.setEndDateTime(study.getEndedAt());

        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(closeStudy.render(study_uri, data));
        } else {
            return ok(closeStudy.render("Close Study", data));
        }
    }
}
