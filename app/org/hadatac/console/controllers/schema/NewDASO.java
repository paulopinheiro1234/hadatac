package org.hadatac.console.controllers.schema;

import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;

import org.hadatac.console.views.html.schema.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.DASOForm;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.metadata.loader.ValueCellProcessing;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class NewDASO extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String das_uri) {
		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					org.hadatac.console.controllers.schema.routes.NewDASO.index(das_uri).url()));
		}

		if (das_uri == null || das_uri.equals("")) {
			return badRequest("Empty of null URI for DAS inside NewDASO.");
		} 

		DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);

		if (das == null) {
			return badRequest("Empty DAS provided to NewDASO.");
		} 

		return ok(newDASO.render(das, EditingOptions.getEntities()));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String das_uri) {
		return index(das_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm(String das_uri) {
		if (das_uri == null || das_uri.equals("")) {
			return badRequest("Empty of null URI for DAS inside NewDASO's processForm.");
		} 

		DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);

		if (das == null) {
			return badRequest("Empty DAS provided to NewDASO.");
		} 

		Form<DASOForm> form = Form.form(DASOForm.class).bindFromRequest();
		DASOForm data = form.get();

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
		String newRole = data.getNewRole();
		String newInRelationTo = getUriFromInRelationTo(data.getNewInRelationTo(),das.getObjects());
		String newRelation = data.getNewRelation();
		String newPosition = "-1";

		// insert current state of the DASO
		DataAcquisitionSchemaObject daso = new DataAcquisitionSchemaObject(newURI,
				newLabel,
				das_uri,
				newPosition,
				newEntity,
				newRole,
				newInRelationTo,
				newRelation);

		// insert the new DASO content inside of the triplestore regardless of any change -- the previous content has already been deleted
		daso.save();

		// update/create new DASO in LabKey
		int nRowsAffected = daso.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
			return badRequest("Failed to insert new DASO to LabKey!\n");
		}
		return ok(newDASOConfirm.render(daso));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postProcessForm(String dasoUri) {
		return processForm(dasoUri);
	}

	private static String getUriFromInRelationTo(String newInRelationTo, List<DataAcquisitionSchemaObject> objects) {
		if (newInRelationTo == null) {
			return "";
		}
		if (newInRelationTo.equals("DefaultObject")) {
			return ValueCellProcessing.replacePrefix("hasco:DefaultObject");
		}
		for (DataAcquisitionSchemaObject daso : objects) {
			if (daso.getRole().equals(newInRelationTo)) {
				return ValueCellProcessing.replacePrefix(daso.getUri());
			}
		} 
		return "";
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
