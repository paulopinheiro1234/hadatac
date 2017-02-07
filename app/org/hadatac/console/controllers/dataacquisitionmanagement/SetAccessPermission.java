package org.hadatac.console.controllers.dataacquisitionmanagement;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SetPermissionForm;
import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.console.views.html.dataacquisitionsearch.measurement_details;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.HADataC;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class SetAccessPermission extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String uri) {
    	
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
    		System.out.println("DC OWNER URI: " + dc.getOwnerUri());
    		
    		Map<String, String> nameList = new HashMap<String, String>();
    		User user = User.find(dc.getOwnerUri());
    		if(null != user){
    			if(user.getUri() != dc.getPermissionUri()){
        			nameList.put(user.getUri(), user.getName());
        		}
    			List<User> groups = UserGroup.find();
    			for (User group : groups) {
    				nameList.put(group.getUri(), group.getName());
    			}
    		}
    		System.out.println("name list: " + nameList);
    		
            return ok(setAccessPermission.render(dc, nameList));
    	}
    	
    	return ok(setAccessPermission.render(dc, null));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String uri) {
    	return index(uri);
    }
    
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result newPermission(String dc_uri) {
		DataAcquisition dc = new DataAcquisition();
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
    		dc = DataAcquisition.findByUri(dc_uri);
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
    	
    	DataAcquisition dc2 = DataAcquisition.findByUri(dc_uri);
    	
        return ok(setAccessPermissionConfirm.render(dc2));
    }
}
