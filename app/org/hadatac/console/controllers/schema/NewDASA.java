package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;

import org.hadatac.console.views.html.schema.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.DASAForm;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.metadata.loader.ValueCellProcessing;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class NewDASA extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String das_uri) {

		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					org.hadatac.console.controllers.schema.routes.NewDASA.index(das_uri).url()));
		}

		DataAcquisitionSchema das = null;

		try {
			if (das_uri != null) {
				das_uri = URLDecoder.decode(das_uri, "UTF-8");
				das = DataAcquisitionSchema.find(das_uri);
			} else {
				das_uri = "";
			}
		} catch (UnsupportedEncodingException e) {
			return badRequest("Could not retrieve DAS fro provided URI.");
		}

		return ok(newDASA.render(das, EditingOptions.getEntities(), EditingOptions.getAttributes(), EditingOptions.getUnits()));

	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String das_uri) {
		return index(das_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm(String das_uri) {
		Form<DASAForm> form = Form.form(DASAForm.class).bindFromRequest();
		DASAForm data = form.get();

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

		String localName = ValueCellProcessing.replacePrefix(newURI);
		localName = localName.substring(localName.indexOf(":") + 1);

		// insert current state of the DASA
		DataAcquisitionSchemaAttribute dasa = new DataAcquisitionSchemaAttribute(newURI,
				localName,
				newLabel,
				das_uri,
				newPosition,
				newEntity,
				newAttribute,
				newUnit,
				newEvent,
				newObject);

		// insert the new DASA content inside of the triplestore regardless of any change -- the previous content has already been deleted
		dasa.save();

		// update/create new DASA in LabKey
		int nRowsAffected = dasa.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
			return badRequest("Failed to insert new DASA to LabKey!\n");
		}
		return ok(newDASAConfirm.render(dasa));
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
		//response = ValueCellProcessing.replacePrefix(response);
		return response;
	}

}
