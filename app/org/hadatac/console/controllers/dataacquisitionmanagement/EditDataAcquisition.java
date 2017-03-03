package org.hadatac.console.controllers.dataacquisitionmanagement;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.DataAcquisitionForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class EditDataAcquisition extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String uri) {
		DataAcquisition dataAcquisition = new DataAcquisition();
		final SysUser sysUser = AuthApplication.getLocalUser(session());
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
    		dataAcquisition = DataAcquisition.findByUri(uri);
    		
    		Map<String, String> nameList = new HashMap<String, String>();
    		User user = User.find(dataAcquisition.getOwnerUri());
    		if(null != user){
    			if(user.getUri() != dataAcquisition.getPermissionUri()){
        			nameList.put(user.getUri(), user.getName());
        		}
    			List<User> groups = UserGroup.find();
    			for (User group : groups) {
    				nameList.put(group.getUri(), group.getName());
    			}
    		}
    		
            return ok(editDataAcquisition.render(dataAcquisition, nameList, 
            		User.getUserURIs(), sysUser.isDataManager()));
    	}
    	
    	return ok(editDataAcquisition.render(dataAcquisition, null, 
    			User.getUserURIs(), sysUser.isDataManager()));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String uri) {
    	return index(uri);
    }
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String acquisitionUri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
    	
        Form<DataAcquisitionForm> form = Form.form(DataAcquisitionForm.class).bindFromRequest();
        DataAcquisitionForm data = form.get();
        List<String> changedInfos = new ArrayList<String>();
        
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest();
        } else {
            DataAcquisition da = DataAcquisition.findByUri(acquisitionUri);
            if (sysUser.isDataManager()) {
            	if (da.getOwnerUri() == null || !da.getOwnerUri().equals(data.getNewOwner())) {
                	da.setOwnerUri(data.getNewOwner());
                	changedInfos.add(data.getNewOwner());
                }
            }
            if (da.getPermissionUri() == null || !da.getPermissionUri().equals(data.getNewPermission())) {
            	da.setPermissionUri(data.getNewPermission());
            	changedInfos.add(data.getNewPermission());
            }
            if (da.getParameter() == null || !da.getParameter().equals(data.getNewParameter())) {
            	da.setParameter(data.getNewParameter());
            	changedInfos.add(data.getNewParameter());
            }
            
            if (!changedInfos.isEmpty()) {
            	da.save();
            }
            
            return ok(editDataAcquisitionConfirm.render(da, changedInfos, sysUser.isDataManager()));
        }
    }
}
