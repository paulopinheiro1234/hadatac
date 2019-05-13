package org.hadatac.console.controllers.metadata.empirical;

import java.util.List;
import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadata.empirical.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.entity.pojo.InstrumentType;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class InstrumentTypeManagement extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri) {
    	List<InstrumentType> instrumentTypes = InstrumentType.find();
    	//System.out.println("Number of platforms: " + platforms.size());
        return ok(instrumentTypeManagement.render(dir, filename, da_uri, instrumentTypes));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String dir, String filename, String da_uri) {
        return index(dir, filename, da_uri);
    }


}
