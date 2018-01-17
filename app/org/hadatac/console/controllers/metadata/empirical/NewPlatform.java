package org.hadatac.console.controllers.metadata.empirical;

import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;
import play.data.FormFactory;

import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.metadata.empirical.*;
import org.hadatac.console.controllers.metadata.empirical.routes;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.entity.pojo.PlatformType;
import org.hadatac.metadata.loader.ValueCellProcessing;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.models.PlatformForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;

public class NewPlatform extends Controller {

	@Inject
	private FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String filename, String da_uri) {
		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					routes.NewPlatform.index(filename, da_uri).url()));
		}

		PlatformType platformType = new PlatformType();

		return ok(newPlatform.render(filename, da_uri, platformType));

	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String filename, String da_uri) {
		return index(filename, da_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result processForm(String filename, String da_uri) {
		final SysUser sysUser = AuthApplication.getLocalUser(session());

		Form<PlatformForm> form = formFactory.form(PlatformForm.class).bindFromRequest();
		PlatformForm data = form.get();

		if (form.hasErrors()) {
			return badRequest("The submitted form has errors!");
		}

		// store new values
		String newURI = ValueCellProcessing.replacePrefixEx(data.getNewUri());
		if (newURI == null || newURI.equals("")) {
			return badRequest("[ERROR] New URI cannot be empty.");
		}
		String newPlatformType = ValueCellProcessing.replacePrefixEx(data.getNewType());
		String newLabel = data.getNewLabel();
		String newComment = data.getNewComment();

		// insert current state of the PLT
		Platform plt = new Platform(newURI,
				newPlatformType,
				newLabel,
				newComment);	

		// insert the new PLT content inside of the triplestore regardless of any change -- the previous content has already been deleted
		plt.save();

		// update/create new PLT in LabKey
		int nRowsAffected = plt.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
			return badRequest("Failed to insert new PLT to LabKey!\n");
		}

		System.out.println("Inserting new Platform from file. filename:  " + filename + "   da : [" + ValueCellProcessing.replacePrefixEx(da_uri) + "]");
		System.out.println("Inserting new Platform from file. Study URI : [" + plt.getUri() + "]");
		// when a new study is created in the scope of a datafile, the new platform needs to be associated to the datafile's DA 
		return ok(newPlatformConfirm.render(plt, filename, da_uri));
	}
}
