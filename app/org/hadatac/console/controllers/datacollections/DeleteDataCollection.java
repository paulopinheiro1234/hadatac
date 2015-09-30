package org.hadatac.console.controllers.datacollections;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.datacollections.*;
import org.hadatac.entity.pojo.DataCollection;

public class DeleteDataCollection extends Controller {
	
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
    		
            return ok(deleteDataCollection.render(oper, dc));
    	}
    	
    	return ok(deleteDataCollection.render(oper, dc));
        
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
    		
            return ok(deleteDataCollection.render(oper, dc));
    	}
    	
    	return ok(deleteDataCollection.render(oper, dc));
        
    }// /postIndex()

    public static String delete(String uri) {
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
    		dc.delete();
    		
            return "Data Collection deleted.";
    	}
    	return "Data Collection failed to be deleted.";
        
    }
}
