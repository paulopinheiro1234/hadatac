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
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.models.PlatformForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;


public class NewPlatform extends Controller {

	@Inject
	private FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String dir, String fileId, String da_uri) {

		PlatformType platformType = new PlatformType();

		return ok(newPlatform.render(dir, fileId, da_uri, platformType));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String dir, String fileId, String da_uri) {
		return index(dir, fileId, da_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result processForm(String dir, String fileId, String da_uri) {
		final SysUser sysUser = AuthApplication.getLocalUser(session());

		Form<PlatformForm> form = formFactory.form(PlatformForm.class).bindFromRequest();
		PlatformForm data = form.get();

		if (form.hasErrors()) {
			return badRequest("The submitted form has errors!");
		}

		// store new values
		String newURI = URIUtils.replacePrefixEx(data.getNewUri());
		if (newURI == null || newURI.equals("")) {
			return badRequest("[ERROR] New URI cannot be empty.");
		}
		String newPlatformType = URIUtils.replacePrefixEx(data.getNewType());
		String newLabel = data.getNewLabel();
		String newComment = data.getNewComment();

		// insert current state of the PLT
		Platform plt = new Platform(newURI,
				newPlatformType,
				newLabel,
				newComment);	

		// insert the new PLT content inside of the triplestore regardless of any change -- the previous content has already been deleted
		plt.save();

		System.out.println("Inserting new Platform from file. da : [" + URIUtils.replacePrefixEx(da_uri) + "]");
		System.out.println("Inserting new Platform from file. Study URI : [" + plt.getUri() + "]");
		// when a new study is created in the scope of a datafile, the new platform needs to be associated to the datafile's DA 
		return ok(newPlatformConfirm.render(plt, dir, fileId, da_uri, 0));
	}
}
