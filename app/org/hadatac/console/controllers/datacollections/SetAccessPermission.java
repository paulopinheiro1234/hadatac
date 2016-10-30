package org.hadatac.console.controllers.datacollections;

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
import org.hadatac.console.models.SetPermissionForm;
import org.hadatac.console.views.html.datacollections.*;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.entity.pojo.HADataC;
import org.hadatac.entity.pojo.User;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class SetAccessPermission extends Controller {
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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
    		
    		User user = User.find(dc.getOwnerUri());
    		System.out.println("DC OWNER URI: " + dc.getOwnerUri());
    		
            return ok(setAccessPermission.render(dc, user.getGroupNames()));
    	}
    	return ok(setAccessPermission.render(dc, null));
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
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
    		
    		User user = User.find(dc.getOwnerUri());
    		
            return ok(setAccessPermission.render(dc, user.getGroupNames()));
    	}
    	return ok(setAccessPermission.render(dc, null));
        
    }// /postIndex()
    
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result newPermission(String dc_uri) {
       	DataCollection dc = new DataCollection();
       	System.out.println("New Permission for " + dc_uri);
    	
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
    	
    	if (dc != null) {
    		Form<SetPermissionForm> form = Form.form(SetPermissionForm.class).bindFromRequest();
    		System.out.println(form.toString());
    		SetPermissionForm data = form.get();

    		String newPermUri = data.getNewPermission();
    		System.out.println("New Perm: " + newPermUri);
    		dc.setPermissionUri(newPermUri);
    		dc.save();
    	}
    	
    	DataCollection dc2 = DataCollection.findByUri(dc_uri);
    	
        return ok(setAccessPermissionConfirm.render(dc2));
    }
}
