package org.hadatac.console.controllers.datacollections;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.datacollections.*;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.entity.pojo.HADataC;

public class SetAccessPermission extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index(String oper, String uri) {
    	
    	DataCollection dc = new DataCollection();
    	
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
    		
    		dc = DataCollection.findByUri(uri);
    		
            return ok(setAccessPermission.render(oper, dc));
    	}
    	return ok(setAccessPermission.render(oper, dc));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex(String oper, String uri) {
    	DataCollection dc = new DataCollection();
    	
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
    		
    		dc = DataCollection.findByUri(uri);
    		
            return ok(setAccessPermission.render(oper, dc));
    	}
    	return ok(setAccessPermission.render(oper, dc));
        
    }// /postIndex()
    
    
    public static void newPermission(String dc_uri, String permission_uri) {
    	DataCollection dc = new DataCollection();
    	
    	try {
    		if (dc_uri != null) {
			    dc_uri = URLDecoder.decode(dc_uri, "UTF-8");
    		} else {
    			dc_uri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

    	if (!dc_uri.equals("")) {

    		/*
    		 *  Add deployment information into handler
    		 */
    		
    		dc = DataCollection.findByUri(dc_uri);
    	}
    }
}
