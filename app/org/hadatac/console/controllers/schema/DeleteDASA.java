package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.schema.*;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DeleteDASA extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String dasa_uri) {

		DataAcquisitionSchemaAttribute dasa = null;

		try {
			if (dasa_uri != null) {
				dasa_uri = URLDecoder.decode(dasa_uri, "UTF-8");
			} else {
				dasa_uri = "";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (!dasa_uri.equals("")) {
			dasa = DataAcquisitionSchemaAttribute.find(dasa_uri);
			System.out.println("delete data acquisition schema attribute");
			return ok(deleteDASA.render(dasa));
		}
		return ok(deleteDASA.render(dasa));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String dasa_uri) {
		return index(dasa_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result processForm(String dasa_uri) {

		DataAcquisitionSchemaAttribute dasa = null;

		try {
			if (dasa_uri != null) {
				dasa_uri = URLDecoder.decode(dasa_uri, "UTF-8");
			} else {
				dasa_uri = "";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (!dasa_uri.equals("")) {
			dasa = DataAcquisitionSchemaAttribute.find(dasa_uri);
		}

		int deletedRows = -1;
		if (dasa != null) {
			dasa.delete();
		}

		return ok(DASAConfirm.render("Deleted Data Acquisition Schema Attribute", "Deleted " + deletedRows + " tuples", dasa));
	}
}
