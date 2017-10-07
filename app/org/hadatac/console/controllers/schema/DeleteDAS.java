package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.DataAcquisitionSchemaForm;
import org.hadatac.console.views.html.schema.*;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DeleteDAS extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String das_uri) {
		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					routes.DeleteDAS.index(das_uri).url()));
		}

		DataAcquisitionSchemaForm dasForm = new DataAcquisitionSchemaForm();
		DataAcquisitionSchema das = null;

		try {
			if (das_uri != null) {
				das_uri = URLDecoder.decode(das_uri, "UTF-8");
			} else {
				das_uri = "";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (!das_uri.equals("")) {

			das = DataAcquisitionSchema.find(das_uri);
			System.out.println("delete data acquisition schema");
			dasForm.setUri(das_uri);
			dasForm.setLabel(das.getLabel());
			return ok(deleteDAS.render(das_uri, dasForm));
		}
		return ok(deleteDAS.render(das_uri, dasForm));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String das_uri) {
		return index(das_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm(String das_uri) {
		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					routes.DeleteDAS.processForm(das_uri).url()));
		}

		DataAcquisitionSchema das = null;

		try {
			if (das_uri != null) {
				das_uri = URLDecoder.decode(das_uri, "UTF-8");
			} else {
				das_uri = "";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (!das_uri.equals("")) {
			das = DataAcquisitionSchema.find(das_uri);
		}

		Form<DataAcquisitionSchemaForm> form = Form.form(DataAcquisitionSchemaForm.class).bindFromRequest();
		DataAcquisitionSchemaForm data = form.get();
		data.setLabel(das.getLabel());

		if (das != null) {
			try {
				System.out.println("calling das.deleteFromLabKey() from DeleteDAS"); 
				das.deleteFromLabKey(session().get("LabKeyUserName"),session().get("LabKeyPassword"));
			} catch (CommandException e) {
				return badRequest(DASConfirm.render("ERROR Deleting Data Acquisition Schema ", "Error from das.deleteFromLabKey()", data.getLabel()));
			}
			das.delete();
		}

		if (form.hasErrors()) {
			return badRequest(DASConfirm.render("ERROR Deleting Data Acquisition Schema ", "Error from form", data.getLabel()));
		} else {
			return ok(DASConfirm.render("Deleted Data Acquisition Schema ", "", data.getLabel()));
		}
	}

}
