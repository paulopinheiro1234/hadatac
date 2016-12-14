package org.hadatac.console.controllers.dataacquisitionmanagement;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.DataAcquisition;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DeleteDataAcquisition extends Controller {
	
	// for /metadata HTTP GET requests
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
    		
            return ok(deleteDataAcquisition.render(oper, dc));
    	}
    	
    	return ok(deleteDataAcquisition.render(oper, dc));
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String oper, String uri) {
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
    		
            return ok(deleteDataAcquisition.render(oper, dc));
    	}
    	
    	return ok(deleteDataAcquisition.render(oper, dc));
        
    }// /postIndex()

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
}
