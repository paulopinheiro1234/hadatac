package org.hadatac.console.controllers.metadataacquisition;

import java.util.List;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.console.controllers.AuthApplication;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewValue extends Controller {	
	public static List<Measurement> findValue(String user_uri, String study_uri, 
										      String subject_uri, String char_uri) {
		AcquisitionQueryResult result = new AcquisitionQueryResult();
		result = Measurement.find(user_uri, study_uri, subject_uri, char_uri);
		
		return result.documents;
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String user_uri, String study_uri, 
    						   String subject_uri, String char_uri) {
		List<Measurement> indicatorValueResults = findValue(user_uri, study_uri, subject_uri, char_uri);
    	return ok(viewValue.render(indicatorValueResults));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String user_uri, String study_uri, 
    							   String subject_uri, String char_uri) {
		return index(user_uri, study_uri, subject_uri, char_uri);
	}
}