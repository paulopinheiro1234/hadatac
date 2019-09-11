package org.hadatac.console.controllers.streams;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.streams.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class StreamVisualization extends Controller {
	
	//private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private Date minDate;
    private Date maxDate;
    
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri) {

		try {
			da_uri = URLDecoder.decode(da_uri, "UTF-8");
		} catch (Exception e) {
		}
		if (da_uri == null || da_uri.isEmpty()) {
	    	return ok(streamVisualization.render(dir, filename, da_uri, "[]", "", ""));
		}
		List<Measurement> measurements = Measurement.findByDataAcquisitionUri(da_uri);
		//sdf.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
		System.out.println("Stream Visualization: da_uri=" + da_uri);
		System.out.println("Stream Visualization: size measurements =" + measurements.size());
		for (Measurement m : measurements) {
			System.out.println("Stream Visualization: " +  sdf.format(m.getTimestamp()) + " " + m.getCharacteristicUris().get(0) + "  " + m.getValue());			
		}
		if (measurements == null) {
	    	return ok(streamVisualization.render(dir, filename, da_uri, "[]", "", ""));
		}
		return ok(streamVisualization.render(dir, filename, da_uri, generateJSON(measurements), sdf.format(minDate), sdf.format(maxDate)));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri) {
    	return index(dir, filename, da_uri);
    }

	private String generateJSON(List<Measurement> measurements) {
		//sdf.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
		minDate = new Date(Long.MAX_VALUE);
	    maxDate = new Date(0);
		String json = "[";
		for (int i = 0; i < measurements.size(); i++) {
			if (measurements.get(i).getCharacteristicUris().get(0).equals("http://hadatac.org/ont/lesa#DetectorDistance9Pixel15DegreeImage")) {
				Date tmpDate = measurements.get(i).getTimestamp();
				if (tmpDate.before(minDate)) {
					minDate = tmpDate;
				}
				if (tmpDate.after(maxDate)) {
					maxDate = tmpDate;
				}
				json = json + "{";
				json = json + "\"date\":\"" + sdf.format(tmpDate) + "\",";
				json = json + "\"value\":\"" + measurements.get(i).getValue() + "\"";
				json = json + "}";
				if (i < measurements.size() - 1) {
					json = json + ",";
				}
			}	
		}
		json = json + "]";
		return json;
	}
	
}
