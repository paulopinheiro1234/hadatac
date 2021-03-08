package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

//import controllers.AuthApplication;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;

import javax.inject.Inject;

public class DeleteDASA extends Controller {
	@Inject
	Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
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
			System.out.println("delete org.hadatac.data acquisition schema attribute");
			return ok(org.hadatac.console.views.html.schema.deleteDASA.render(dasa));
		}
		return ok(org.hadatac.console.views.html.schema.deleteDASA.render(dasa));
	}

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
	public Result postIndex(String dasa_uri) {
		return index(dasa_uri);
	}

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
	public Result processForm(String dasa_uri, Http.Request request) {

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

		return ok(org.hadatac.console.views.html.schema.DASAConfirm.render("Deleted Data Acquisition Schema Attribute", "Deleted " + deletedRows + " tuples", dasa,application.getUserEmail(request)));
	}
}
