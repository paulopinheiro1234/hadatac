package org.hadatac.console.controllers.metadata.empirical;

import java.util.List;

import org.hadatac.entity.pojo.Platform;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.metadata.empirical.*;
import play.mvc.Result;
import play.mvc.Controller;

public class PlatformManagement extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String filename, String da_uri) {
    	List<Platform> platforms = Platform.find();
	System.out.println("Number of platforms: " + platforms.size());
        return ok(platformManagement.render(filename, da_uri, platforms));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String filename, String da_uri) {
        return index(filename, da_uri);
    }

}