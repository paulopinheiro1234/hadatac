package org.hadatac.console.controllers.datacollections;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.datacollections.*;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.entity.pojo.HADataC;


public class DeleteDataCollection extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index(String uri) {
    	
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
    		
            return ok(deleteDataCollection.render(dc));
    	}
    	return ok(deleteDataCollection.render(dc));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex(String uri) {
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
    		
            return ok(deleteDataCollection.render(dc));
    	}
    	return ok(deleteDataCollection.render(dc));
        
    }// /postIndex()

}
