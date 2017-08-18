package org.hadatac.console.controllers.metadataacquisition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.apache.commons.lang3.StringUtils;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewValue extends Controller {
	public static List<Measurement> findValue(String user_uri, String study_uri, 
										      String subject_uri, String char_uri) {
		AcquisitionQueryResult result = new AcquisitionQueryResult();
		result = Measurement.findForViews(user_uri, study_uri, subject_uri, char_uri, false);
		
		return result.documents;
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String user_uri, String study_uri, 
    						   String subject_uri, String char_uri) {
		List<Measurement> indicatorValueResults = findValue(user_uri, study_uri, subject_uri, char_uri);
		if (indicatorValueResults == null){
			List<Measurement> indicatorValueResults2 = new ArrayList<Measurement>();
			File indicator_detail_csv = new File("/data/indicatorDetails.csv");
			try {
	            FileUtils.writeStringToFile(indicator_detail_csv, "", false);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        try {
	            FileUtils.writeStringToFile(indicator_detail_csv, "NULL");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
//	        return ok(indicator_detail_csv);
			return ok(viewValue.render(indicatorValueResults2));
		}
		else{
			File indicator_detail_csv = new File("/data/indicatorDetails.csv");
			try {
	            FileUtils.writeStringToFile(indicator_detail_csv, "", false);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			for(int i=0; i<indicatorValueResults.size(); i++){
				List<String> temp_sentence = new ArrayList<String>();
				temp_sentence.add(indicatorValueResults.get(i).getObjectUri().toString());
				temp_sentence.add(indicatorValueResults.get(i).getCharacteristic().toString());
				temp_sentence.add(indicatorValueResults.get(i).getValue().toString());
				String sentence = StringUtils.join(temp_sentence, ',');
				try {
		            FileUtils.writeStringToFile(indicator_detail_csv, sentence+"\n", true);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			}				
	    	return ok(viewValue.render(indicatorValueResults));	
		}
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String user_uri, String study_uri, 
    							   String subject_uri, String char_uri) {
		return index(user_uri, study_uri, subject_uri, char_uri);
	}
}