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
import org.hadatac.console.models.DASAForm;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.metadata.loader.ValueCellProcessing;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class EditDASA extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String dasa_uri) {

		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					routes.EditDASA.index(dasa_uri).url()));
		}

		DataAcquisitionSchema das = null;
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
			if (dasa != null && dasa.getPartOfSchema() != null) {
				das = DataAcquisitionSchema.find(dasa.getPartOfSchema());
			} else {
				return badRequest("No URI is provided inside of DASA to retrieve DAS");
			}
		} else {
			return badRequest("No URI is provided to retrieve DASA");
		}
		return ok(editDASA.render(das, dasa, EditingOptions.getEntities(), EditingOptions.getAttributes(), EditingOptions.getUnits()));

	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String dasa_uri) {
		return index(dasa_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm(String dasaUri) {
		Form<DASAForm> form = Form.form(DASAForm.class).bindFromRequest();
		DASAForm data = form.get();
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
		String newPosition = data.getNewPosition();
		String newEntity = getUriFromNew(data.getNewEntity());
		String newAttribute = getUriFromNew(data.getNewAttribute());
		String newUnit = getUriFromNew(data.getNewUnit());
		String newObject = data.getNewObject();
		String newEvent = data.getNewEvent();

		// retrieve old DASA and corresponding DAS
		DataAcquisitionSchemaAttribute olddasa = DataAcquisitionSchemaAttribute.find(dasaUri);
		DataAcquisitionSchema das = DataAcquisitionSchema.find(olddasa.getPartOfSchema());

		// set changes
		if (olddasa != null) {

			if (olddasa.getUri() != null && !olddasa.getUri().equals(newURI)) {
				changedInfos.add(newURI);
			}
			if (olddasa.getLabel() != null && !olddasa.getLabel().equals(newLabel)) {
				changedInfos.add(newLabel);
			}
			if (olddasa.getPosition() != null && !olddasa.getPosition().equals(newPosition)) {
				changedInfos.add(newPosition);
			}
			if (olddasa.getEntity() != null && !olddasa.getEntity().equals(newEntity)) {
				changedInfos.add(newEntity);
			}
			if (olddasa.getAttribute() == null || !olddasa.getAttribute().equals(newAttribute)) {
				changedInfos.add(newAttribute);
			}
			if (olddasa.getUnit() == null || !olddasa.getUnit().equals(newUnit)) {
				changedInfos.add(newUnit);
			}
			if (olddasa.getObject() == null || !olddasa.getObject().equals(newObject)) {
				changedInfos.add(newObject);
			}
			if (olddasa.getEvent() == null || !olddasa.getEvent().equals(newEvent)) {
				changedInfos.add(newEvent);
			}

			// delete previous state of the DASA in the triplestore
			if (olddasa != null) {
				olddasa.delete();
			}
		} else {
			return badRequest("[ERRO] Failed locating existing DASA.\n");
		}

		String localName = ValueCellProcessing.replacePrefix(newURI);
		localName = localName.substring(localName.indexOf(":") + 1);

		// insert current state of the DASA
		olddasa.setUri(newURI);
		olddasa.setLocalName(localName);
		olddasa.setLabel(newLabel);
		olddasa.setPosition(newPosition);
		olddasa.setEntity(newEntity);
		olddasa.setAttribute(newAttribute);
		olddasa.setUnit(newUnit);
		olddasa.setObjectUri(DataAcquisitionSchemaObject.findUriFromRole(data.getNewObject(),das.getObjects()));
		olddasa.setEventUri(newEvent);

		// insert the new DASA content inside of the triplestore regardless of any change -- the previous content has already been deleted
		olddasa.save();

		// update/create new DASA in LabKey
		int nRowsAffected = olddasa.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
			return badRequest("Failed to insert new DASA to LabKey!\n");
		}
		return ok(editDASAConfirm.render(olddasa, changedInfos));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postProcessForm(String dasaUri) {
		return processForm(dasaUri);
	}

	private static String getUriFromNew(String newStr) {
		if (newStr == null) {
			return "";
		}
		String response = newStr.substring(newStr.indexOf("[") + 1).replace("]","");
		return response;
	}

}
