package org.hadatac.console.controllers.metadata.empirical;

import java.util.List;
import java.util.ArrayList;
import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;
import play.data.FormFactory;

import org.hadatac.console.views.html.metadata.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.ConceptForm;
import org.hadatac.console.models.OtMSparqlQueryResults;
import org.hadatac.entity.pojo.InstrumentType;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class InstrumentTypeManagement extends Controller {
	
	@Inject
	private FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String dir, String filename, String da_uri) {

		if (ConfigProp.getLabKeyLoginRequired() && session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					org.hadatac.console.controllers.metadata.empirical.routes.InstrumentTypeManagement.index(dir, filename, da_uri).url()));
		}

		InstrumentType type = new InstrumentType();
		String json = type.getHierarchyJson();
		OtMSparqlQueryResults instrumentTypes = new OtMSparqlQueryResults(json);

		return ok(typeManagement.render("Instrument", dir, filename, da_uri, instrumentTypes));

	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String dir, String filename, String da_uri) {
		return index(dir, filename, da_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result processForm(String dir, String filename, String da_uri) {
		final SysUser sysUser = AuthApplication.getLocalUser(session());

		Form<ConceptForm> form = formFactory.form(ConceptForm.class).bindFromRequest();
		ConceptForm data = form.get();
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
		String newSuperUri = data.getNewSuperUri();
		String newComment = data.getNewComment();

		// retrieve old Instrument
		InstrumentType oldPlat = InstrumentType.find(newURI);

		// set changes
		if (oldPlat != null) {

			if (oldPlat.getUri() != null && !oldPlat.getUri().equals(newURI)) {
				changedInfos.add(newURI);
			}
			if (oldPlat.getLabel() != null && !oldPlat.getLabel().equals(newLabel)) {
				changedInfos.add(newLabel);
			}
			if (oldPlat.getSuperUri() != null && !oldPlat.getSuperUri().equals(newSuperUri)) {
				changedInfos.add(newSuperUri);
			}
			if (oldPlat.getComment() != null && !oldPlat.getComment().equals(newComment)) {
				changedInfos.add(newComment);
			}
			// delete previous state of the DASA in the triplestore
			if (oldPlat != null) {
				//oldPlat.delete();
			}
		} else {
			return badRequest("[ERRO] Failed locating existing Instrument.\n");
		}

		// insert current state of the DASA
		oldPlat.setUri(newURI);
		oldPlat.setLabel(newLabel);
		oldPlat.setSuperUri(newSuperUri);
		oldPlat.setComment(newComment);

		// insert the new DASA content inside of the triplestore regardless of any change -- the previous content has already been deleted
		//oldPlat.save();

		// update/create new DASA in LabKey
		//int nRowsAffected = oldPlat.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		//if (nRowsAffected <= 0) {
		//    return badRequest("Failed to insert new Instrument to LabKey!\n");
		//	}
		return index(dir, filename, da_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postProcessForm(String dir, String filename, String da_uri) {
		return processForm(dir, filename, da_uri);

	}

}
