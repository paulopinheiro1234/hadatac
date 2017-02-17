package org.hadatac.console.controllers.dataacquisitionmanagement;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.routes;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.AssignOwnerForm;
import org.hadatac.console.models.SysUser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.annotator.assignOwner;
import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.User;
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
    		System.out.println("Email: " + user.getEmail());
    		System.out.println("Owner URI: " + ownerUri);
    		results = DataAcquisition.find(ownerUri, state);
    	}
    	
        return ok(dataAcquisitionManagement.render(state, results, user.isDataManager()));   
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(int stateId) {
    	return index(stateId);
    }
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result assignOwner(String acquisitionUri) {
		try {
    		if (acquisitionUri != null) {
    			acquisitionUri = URLDecoder.decode(acquisitionUri, "UTF-8");
    		} else {
    			acquisitionUri = "";
    		}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return ok(assignOwner.render(Form.form(AssignOwnerForm.class),
    								 User.getUserURIs(),
    								 routes.DataAcquisitionManagement.processForm(acquisitionUri),
    								 "Data Acquisition URI",
    								 acquisitionUri));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postAssignOwner(String acquisitionUri) {
		return assignOwner(acquisitionUri);
	}
	
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result processForm(String acquisitionUri) {
        Form<AssignOwnerForm> form = Form.form(AssignOwnerForm.class).bindFromRequest();
        AssignOwnerForm data = form.get();
        
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(assignOwner.render(Form.form(AssignOwnerForm.class),
					 User.getUserURIs(),
					 routes.DataAcquisitionManagement.processForm(acquisitionUri),
					 "Data Acquisition URI",
					 acquisitionUri));
        } else {
            DataAcquisition da = DataAcquisition.findByUri(acquisitionUri);
            da.setOwnerUri(data.getUser());
            da.save();
    		return redirect(routes.DataAcquisitionManagement.index(State.ACTIVE));
        }
    }
}