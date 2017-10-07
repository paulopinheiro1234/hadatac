package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.ArrayList;

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

public class EditDASO extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String daso_uri, String das_uri) {

		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					routes.EditDASO.index(daso_uri, das_uri).url()));
		}

		DataAcquisitionSchemaObject daso = null;
		try {
			if (daso_uri != null) {
				daso_uri = URLDecoder.decode(daso_uri, "UTF-8");
			} else {
				daso_uri = "";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
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

		if (!daso_uri.equals("")) {
			daso = DataAcquisitionSchemaObject.find(daso_uri);
			if (!das_uri.equals("")) {
				das = DataAcquisitionSchema.find(das_uri);
			} else {
				return badRequest("No URI is provided to retrieve DAS");
			}
		} else {
			return badRequest("No URI is provided to retrieve DASO");
		}
		return ok(editDASO.render(daso, EditingOptions.getEntities(), das));

	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String daso_uri, String das_uri) {
		return index(daso_uri, das_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm(String daso_uri, String das_uri) {
		
		if (das_uri == null || das_uri.equals("")) {
			return badRequest("Empty of null URI for DAS inside NewDASO's processForm.");
		} 

		DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);

		if (das == null) {
			return badRequest("Empty DAS provided to NewDASO.");
		} 

		Form<DASOForm> form = Form.form(DASOForm.class).bindFromRequest();
		DASOForm data = form.get();
		List<String> changedInfos = new ArrayList<String>();

		if (form.hasErrors()) {
			return badRequest("The submitted form has errors!");
		}

		// store new values
		String newURI = data.getNewUri();
		if (newURI == null || newURI.equals("")) {
			return badRequest("[ERROR] New URI cannot be empty.");
		}
		String newLabel = data.getNewLabel();
		String newRole = data.getNewRole();
		String newEntity = getUriFromNew(data.getNewEntity());
		String newInRelationTo = getUriFromNew(data.getNewInRelationTo());
		String newRelation = getUriFromNew(data.getNewRelation());

		// retrieve old DASO
		DataAcquisitionSchemaObject olddaso = DataAcquisitionSchemaObject.find(daso_uri);

		// set changes
		if (olddaso != null) {

			if (olddaso.getUri() != null && !olddaso.getUri().equals(newURI)) {
				changedInfos.add(newURI);
			}
			if (olddaso.getLabel() != null && !olddaso.getLabel().equals(newLabel)) {
				changedInfos.add(newLabel);
			}
			if (olddaso.getRole() != null && !olddaso.getRole().equals(newRole)) {
				changedInfos.add(newRole);
			}
			if (olddaso.getEntity() != null && !olddaso.getEntity().equals(newEntity)) {
				changedInfos.add(newEntity);
			}
			if (olddaso.getInRelationTo() == null || !olddaso.getInRelationTo().equals(newInRelationTo)) {
				changedInfos.add(newInRelationTo);
			}
			if (olddaso.getRelation() == null || !olddaso.getRelation().equals(newRelation)) {
				changedInfos.add(newRelation);
			}

			// delete previous state of the DASO in the triplestore
			if (olddaso != null) {
				olddaso.delete();
			}
		} else {
			return badRequest("[ERRO] Failed locating existing DASO.\n");
		}

		String localName = ValueCellProcessing.replacePrefix(newURI);
		localName = localName.substring(localName.indexOf(":") + 1);

		// insert current state of the DASO
		olddaso.setUri(newURI);
		olddaso.setLabel(newLabel);
		olddaso.setRole(newRole);
		olddaso.setEntity(newEntity);
		olddaso.setInRelationTo(DataAcquisitionSchemaObject.findUriFromRole(data.getNewInRelationTo(),das.getObjects()));
		olddaso.setRelation(newRelation);

		// insert the new DASO content inside of the triplestore regardless of any change -- the previous content has already been deleted
		olddaso.save();

		// update/create new DASO in LabKey
		int nRowsAffected = olddaso.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
			return badRequest("Failed to insert new DASO to LabKey!\n");
		}
		return ok(editDASOConfirm.render(olddaso, changedInfos));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postProcessForm(String daso_uri, String das_uri) {
		return processForm(daso_uri, das_uri);
	}

	private static String getUriFromNew(String newStr) {
		if (newStr == null) {
			return "";
		}
		String response = newStr.substring(newStr.indexOf("[") + 1).replace("]","");
		return response;
	}

}
