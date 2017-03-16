package org.hadatac.console.controllers.dataacquisitionmanagement;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;
import org.hadatac.metadata.loader.ValueCellProcessing;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class EditDataAcquisition extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String uri) {
		DataAcquisition dataAcquisition = new DataAcquisition();
		
		ValueCellProcessing cellProc = new ValueCellProcessing();
		List<DataAcquisitionSchema> schemas = DataAcquisitionSchema.findAll();
		for (DataAcquisitionSchema schema : schemas) {
			schema.setUri(cellProc.replaceNameSpaceEx(schema.getUri()));
		}
		
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
            		User.getUserURIs(), schemas, sysUser.isDataManager()));
    	}
    	
    	return ok(editDataAcquisition.render(dataAcquisition, null, 
    			User.getUserURIs(), schemas, sysUser.isDataManager()));
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
                String dateString = "";
                DateFormat jsFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
        		try {
        			Date dateFromJs = jsFormat.parse(data.getNewDataAcquisitionStartDate());
        	        DateFormat isoFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        	        dateString = isoFormat.format(dateFromJs);
        		} catch (ParseException e) {
        			return badRequest("Cannot parse data " + data.getNewDataAcquisitionStartDate());
        		}
            	da.setUri(data.getNewDataAcquisitionUri());
            	da.setNumberDataPoints(0);
            	da.setTriggeringEvent(TriggeringEvent.CHANGED_CONFIGURATION);
            	da.setParameter(data.getNewParameter());
            	da.setStartedAt(dateString);
            	da.save();
            	changedInfos.add(data.getNewParameter());
            }
            if (da.getSchemaUri() == null || !da.getSchemaUri().equals(data.getNewSchema())) {
            	da.setSchemaUri(data.getNewSchema());
            	changedInfos.add(data.getNewSchema());
            }
            
            if (!changedInfos.isEmpty()) {
            	da.save();
            }
            
            return ok(editDataAcquisitionConfirm.render(da, changedInfos, sysUser.isDataManager()));
        }
    }
}
