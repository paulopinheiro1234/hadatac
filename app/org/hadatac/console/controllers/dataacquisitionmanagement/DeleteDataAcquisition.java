package org.hadatac.console.controllers.dataacquisitionmanagement;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.routes;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Measurement;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DeleteDataAcquisition extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String oper, String uri) {
		DataAcquisition dc = new DataAcquisition();
    	try {
    		if (uri != null) {
			    uri = URLDecoder.decode(uri, "UTF-8");
    		} else {
    			uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

    	if (!uri.equals("")) {

    		/*
    		 *  Add deployment information into handler
    		 */
    		
    		dc = DataAcquisition.findByUri(uri);
    		if (dc == null) {
    			return badRequest("Incorrect data acquisition uri detected!");
    		}
    		final SysUser user = AuthApplication.getLocalUser(session());
    		
            return ok(deleteDataAcquisition.render(oper, dc, user.isDataManager()));
    	}
    	
    	return badRequest("No data acquisition uri specified!");
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String oper, String uri) {
		return index(oper, uri);
    }

    public static String delete(String uri) {
    	DataAcquisition dc = new DataAcquisition();
    	try {
    		if (uri != null) {
			    uri = URLDecoder.decode(uri, "UTF-8");
    		} else {
    			uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

    	if (!uri.equals("")) {

    		/*
    		 *  Add deployment information into handler
    		 */
    		
    		dc = DataAcquisition.findByUri(uri);
    		dc.delete();
    		
            return "Data Acquisition deleted.";
    	}
    	
    	return "Data Acquisition failed to be deleted.";
    }
    
    public static Result deleteDataPoints(String uri, int state) {
    	DataAcquisition dc = new DataAcquisition();
    	try {
    		if (uri != null) {
			    uri = URLDecoder.decode(uri, "UTF-8");
    		} else {
    			uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

    	if (!uri.equals("")) {

    		/*
    		 *  Add deployment information into handler
    		 */
    		
    		dc = DataAcquisition.findByUri(uri);
    		if (!dc.deleteMeasurementData()) {
    			dc.setNumberDataPoints(Measurement.getNumByDataAcquisition(dc));
    			dc.save();
    			return badRequest("Measurement data in this data acquisition failed to be deleted.");
    		}
    		dc.setNumberDataPoints(0);
    		dc.save();
    		
            return redirect(routes.DataAcquisitionManagement.index(state));
    	}
    	
    	return badRequest("Measurement data in this data acquisition failed to be deleted.");
    }
    
    public static Result postDeleteDataPoints(String uri, int state) {
    	return deleteDataPoints(uri, state);
    }
}
