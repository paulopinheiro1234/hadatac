package org.hadatac.console.controllers.schema;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.schema.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.OtMSparqlQueryResults;
import org.hadatac.entity.pojo.Entity;
import org.hadatac.entity.pojo.Attribute;
import org.hadatac.entity.pojo.Unit;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class EditingOptions extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result entityOptions() {
		return ok(entityOptions.render(getEntities()));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postEntityOptions() {
		return ok(entityOptions.render(getEntities()));
	}

	public static OtMSparqlQueryResults getEntities() {
		Entity entity = new Entity();
		String json = entity.getHierarchyJson();
		OtMSparqlQueryResults entities = new OtMSparqlQueryResults(json);
		return entities;
	}

	public static OtMSparqlQueryResults getAttributes() {
		Attribute attribute = new Attribute();
		String json = attribute.getHierarchyJson();
		OtMSparqlQueryResults attributes = new OtMSparqlQueryResults(json);
		return attributes;
	}

	public static OtMSparqlQueryResults getUnits() {
		Unit unit = new Unit();
		String json = unit.getHierarchyJson();
		OtMSparqlQueryResults units = new OtMSparqlQueryResults(json);
		return units;
	}

}
