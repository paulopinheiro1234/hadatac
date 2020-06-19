package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class MainEntity extends Controller {
	
	@Inject
	private FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String das_uri) {

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
		} else {
			return badRequest("No URI is provided to retrieve DAS");
		}
		return ok(mainEntity.render(das, EditingOptions.getEntities()));

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

		// store new Main Entity
		String newEntity = getUriFromNew(data.getNewEntity());

		// retrieve DAS
		DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);

		DataAcquisitionSchemaAttribute olddasa = DataAcquisitionSchemaAttribute.find(das_uri);

		// set changes
		if (olddasa != null) {

			// delete previous state of the DASA in the triplestore
			if (olddasa != null) {
				olddasa.delete();
			}
		} else {
			return badRequest("[ERRO] Failed locating existing DASA.\n");
		}

		// insert current state of the DASA
		olddasa.setEntity(newEntity);

		// insert the new DASA content inside of the triplestore regardless of any change -- the previous content has already been deleted
		olddasa.save();

		return ok("");
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postProcessForm(String das_uri) {
		return processForm(das_uri);
	}

	private static String getUriFromNew(String newStr) {
		if (newStr == null) {
			return "";
		}
		String response = newStr.substring(newStr.indexOf("[") + 1).replace("]","");
		//response = URIUtils.replacePrefix(response);
		return response;
	}
}
