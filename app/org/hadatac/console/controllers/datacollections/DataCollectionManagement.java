package org.hadatac.console.controllers.datacollections;

import org.hadatac.console.controllers.AuthApplication;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.datacollections.*;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class DataCollectionManagement extends Controller {

	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(int stateId) {

    	State state = new State(stateId);
    	
    	List<DataCollection> theResults = DataCollection.find(AuthApplication.getLocalUser(session()).uri, state);    		
    	
        return ok(dataCollectionManagement.render(state, theResults));
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(int stateId) {

    	State state = new State(stateId);
    	
    	List<DataCollection> theResults = DataCollection.find(AuthApplication.getLocalUser(session()).uri, state);    		
    	
        return ok(dataCollectionManagement.render(state, theResults));
        
    }// /postIndex()

}