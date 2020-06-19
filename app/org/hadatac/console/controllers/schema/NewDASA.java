package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;
import play.data.FormFactory;
import javax.inject.Inject;

import org.hadatac.console.views.html.schema.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.DASAForm;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class NewDASA extends Controller {
	
	@Inject
	private FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String das_uri) {

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
	public Result postIndex(String das_uri) {
		return index(das_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result processForm(String das_uri) {
		Form<DASAForm> form = formFactory.form(DASAForm.class).bindFromRequest();
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
		List<String> newAttribute = getUriListFromNew(data.getNewAttribute());
		String newUnit = getUriFromNew(data.getNewUnit());
		String newObject = data.getNewObject();
		String newEvent = data.getNewEvent();

		String localName = URIUtils.replacePrefix(newURI);
		localName = localName.substring(localName.indexOf(":") + 1);

		// insert current state of the DASA
		DataAcquisitionSchemaAttribute dasa = new DataAcquisitionSchemaAttribute(
		        newURI,
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

		return ok(newDASAConfirm.render(dasa));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postProcessForm(String dasaUri) {
		return processForm(dasaUri);
	}

	private static String getUriFromNew(String newStr) {
		if (newStr == null) {
			return "";
		}
		String response = newStr.substring(newStr.indexOf("[") + 1).replace("]","");
		//response = URIUtils.replacePrefix(response);
		return response;
	}
	
	private static List<String> getUriListFromNew(List<String> newStrList) {
		if (newStrList == null) {
			return Arrays.asList("");
		}
		List<String> response = new ArrayList<String>();
		for (String str : newStrList) {
			response.add(str.substring(str.indexOf("[") + 1).replace("]",""));
		}

		return response;
	}

}
