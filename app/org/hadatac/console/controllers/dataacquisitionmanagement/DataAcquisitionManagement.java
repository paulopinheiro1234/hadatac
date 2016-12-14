package org.hadatac.console.controllers.dataacquisitionmanagement;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;

import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class DataAcquisitionManagement extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(int stateId) {
    	State state = new State(stateId);
    	final SysUser user = AuthApplication.getLocalUser(session());
		String ownerUri = UserManagement.getUriByEmail(user.email);
		System.out.println(user.email);
		System.out.println(ownerUri);
    	List<DataAcquisition> theResults = DataAcquisition.find(ownerUri, state);    		
    	
        return ok(dataAcquisitionManagement.render(state, theResults));   
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(int stateId) {
    	State state = new State(stateId);
    	final SysUser user = AuthApplication.getLocalUser(Controller.session());
		String ownerUri = UserManagement.getUriByEmail(user.email);
    	List<DataAcquisition> theResults = DataAcquisition.find(ownerUri, state);    		
    	
        return ok(dataAcquisitionManagement.render(state, theResults));
    }
}