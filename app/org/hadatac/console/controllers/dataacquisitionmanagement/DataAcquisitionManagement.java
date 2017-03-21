package org.hadatac.console.controllers.dataacquisitionmanagement;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.routes;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.DataAcquisitionForm;
import org.hadatac.console.models.SysUser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DataAcquisitionManagement extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(int stateId) {
		List<DataAcquisition> results = null;
    	State state = new State(stateId);
    	final SysUser user = AuthApplication.getLocalUser(session());
    	if (user.isDataManager()) {
    		results = DataAcquisition.findAll(state);
    	}
    	else {
    		String ownerUri = UserManagement.getUriByEmail(user.getEmail());
    		results = DataAcquisition.find(ownerUri, state);
    	}
    	
    	ValueCellProcessing cellProc = new ValueCellProcessing();
		for (DataAcquisition dataAcquisition : results) {
			dataAcquisition.setSchemaUri(cellProc.replaceNameSpaceEx(
					dataAcquisition.getSchemaUri()));
		}
		results.sort(new Comparator<DataAcquisition>() {
            @Override
            public int compare(DataAcquisition lhs, DataAcquisition rhs) {
                return lhs.getUri().compareTo(rhs.getUri());
            }
        });
    	
        return ok(dataAcquisitionManagement.render(state, results, user.isDataManager()));   
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(int stateId) {
    	return index(stateId);
    }
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result newDataAcquisition() {
		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
    				routes.DataAcquisitionManagement.newDataAcquisition().url()));
    	}
		
		final SysUser sysUser = AuthApplication.getLocalUser(session());
		
		ValueCellProcessing cellProc = new ValueCellProcessing();
		List<DataAcquisitionSchema> schemas = DataAcquisitionSchema.findAll();
		for (DataAcquisitionSchema schema : schemas) {
			schema.setUri(cellProc.replaceNameSpaceEx(schema.getUri()));
		}
		
		Map<String, String> nameList = new HashMap<String, String>();
		List<User> groups = UserGroup.find();
		for (User group : groups) {
			nameList.put(group.getUri(), group.getName());
		}
		
    	return ok(newDataAcquisition.render(Deployment.find(new State(State.ACTIVE)),
    			schemas, nameList, User.getUserURIs(), sysUser.isDataManager()));
    }
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postNewDataAcquisition() {
		return newDataAcquisition();
	}
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm() {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
    	
        Form<DataAcquisitionForm> form = Form.form(DataAcquisitionForm.class).bindFromRequest();
        DataAcquisitionForm data = form.get();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        if (null != DataAcquisition.findByUri(data.getNewDataAcquisitionUri())) {
        	return badRequest("Data acquisition with this uri already exists!");
        }
        
        DataAcquisition da = new DataAcquisition();
        da.setUri(data.getNewDataAcquisitionUri());
    	da.setNumberDataPoints(0);
    	da.setSchemaUri(data.getNewSchema());
    	da.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
    	da.setParameter(data.getNewParameter());
        if (sysUser.isDataManager()) {
        	da.setOwnerUri(data.getNewOwner());
        }
        da.setPermissionUri(data.getNewPermission());
        String dateString = "";
        DateFormat jsFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
		try {
			Date dateFromJs = jsFormat.parse(data.getNewStartDate());
	        DateFormat isoFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
	        dateString = isoFormat.format(dateFromJs);
		} catch (ParseException e) {
			return badRequest("Cannot parse data " + data.getNewStartDate());
		}
    	da.setStartedAt(dateString);
    	da.save();
        
        return redirect(routes.DataAcquisitionManagement.index(State.ACTIVE));
    }
}

