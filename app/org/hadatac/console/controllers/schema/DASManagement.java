package org.hadatac.console.controllers.schema;

import java.util.List;

import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.PossibleValue;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.controllers.AuthApplication;

import play.mvc.Result;
import play.mvc.Controller;

public class DASManagement extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index() {
	    System.out.println("----------->>>>>> Inside DASManagement ");
	    List<DataAcquisitionSchema> sdds = DataAcquisitionSchema.findAll();
	    System.out.println("Size of SDD list: " + sdds.size());
	    return ok(org.hadatac.console.views.html.schema.DASManagement.render(sdds));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex() {
	    return index();
	}


}