package org.hadatac.console.controllers.schema;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;

import org.hadatac.console.views.html.schema.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.DASEForm;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class NewDASE extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String das_uri) {

		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					org.hadatac.console.controllers.schema.routes.NewDASE.index(das_uri).url()));
		}

		if (das_uri == null || das_uri.equals("")) {
			return badRequest("Empty of null URI for DAS inside NewDASE.");
		} 

		DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);

		if (das == null) {
			return badRequest("Empty DAS provided to NewDASE.");
		} 

		return ok(newDASE.render(das, EditingOptions.getEntities(), EditingOptions.getUnits()));

	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String das_uri) {
		return index(das_uri);	  
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm(String das_uri) {
		if (das_uri == null || das_uri.equals("")) {
			return badRequest("Empty of null URI for DAS inside NewDASE's processForm.");
		} 

		DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);

		if (das == null) {
			return badRequest("Empty DAS provided to NewDASE.");
		} 

		Form<DASEForm> form = Form.form(DASEForm.class).bindFromRequest();
		DASEForm data = form.get();

		if (form.hasErrors()) {
			return badRequest("The submitted form has errors!");
		}

		// store new values
		String newURI = data.getNewUri();
		if (newURI == null || newURI.equals("")) {
			return badRequest("[ERROR] New URI cannot be empty.");
		}
		String newLabel = data.getNewLabel();
		String newEntity = getUriFromNew(data.getNewEntity());
		String newUnit = getUriFromNew(data.getNewUnit());

		// insert current state of the DASE
		DataAcquisitionSchemaEvent dase = new DataAcquisitionSchemaEvent(newURI,
				newLabel,
				das_uri,
				newEntity,
				newUnit);

		// insert the new DASE content inside of the triplestore regardless of any change -- the previous content has already been deleted
		dase.save();

		// update/create new DASE in LabKey
		int nRowsAffected = dase.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
			return badRequest("Failed to insert new DASE to LabKey!\n");
		}
		return ok(newDASEConfirm.render(dase));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postProcessForm(String daseUri) {
		return processForm(daseUri);
	}

	private static String getUriFromNew(String newStr) {
		if (newStr == null) {
			return "";
		}
		String response = newStr.substring(newStr.indexOf("[") + 1).replace("]","");
		//response = ValueCellProcessing.replacePrefix(response);
		return response;
	}
}
