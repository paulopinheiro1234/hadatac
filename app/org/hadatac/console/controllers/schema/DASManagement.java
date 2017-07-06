package org.hadatac.console.controllers.schema;

import java.util.List;

import org.hadatac.entity.pojo.DataAcquisitionSchema;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.schema.*;

import play.mvc.Result;
import play.mvc.Controller;

public class DASManagement extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index() {
	    List<DataAcquisitionSchema> theResults = DataAcquisitionSchema.findAll();
	    return ok(org.hadatac.console.views.html.schema.DASManagement.render(theResults));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex() {
	    List<DataAcquisitionSchema> theResults = DataAcquisitionSchema.findAll();
	    return ok(org.hadatac.console.views.html.schema.DASManagement.render(theResults));
	}
}