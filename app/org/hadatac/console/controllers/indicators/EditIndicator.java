package org.hadatac.console.controllers.indicators;

import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;

import org.hadatac.console.views.html.indicators.*;
import org.hadatac.console.controllers.indicators.routes;
import org.hadatac.entity.pojo.Indicator;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.IndicatorForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;

public class EditIndicator extends Controller {

	@Inject
	private FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String ind_uri) {

		Indicator indicator = null;

		try {
			if (ind_uri != null) {
				ind_uri = URLDecoder.decode(ind_uri, "UTF-8");
			} else {
				ind_uri = "";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (!ind_uri.equals("")) {
			indicator = Indicator.find(ind_uri);
		} else {
			return badRequest("No URI is provided to retrieve Indicator");
		}

		return ok(editIndicator.render(indicator));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String ind_uri) {
		return index(ind_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result processForm(String ind_uri) {
		final SysUser sysUser = AuthApplication.getLocalUser(session());

		Form<IndicatorForm> form = formFactory.form(IndicatorForm.class).bindFromRequest();
		IndicatorForm data = form.get();
		List<String> changedInfos = new ArrayList<String>();

		if (form.hasErrors()) {
			return badRequest("The submitted form has errors!");
		}

		// store new values
		String newURI = URIUtils.replacePrefixEx(data.getNewUri());
		if (newURI == null || newURI.equals("")) {
			return badRequest("[ERROR] New URI cannot be empty.");
		}
		String newLabel = data.getNewLabel();
		String newComment = data.getNewComment();

		// retrieve old Indicator and corresponding DAS
		Indicator oldIndicator = Indicator.find(ind_uri);

		// set changes
		if (oldIndicator != null) {

			if (oldIndicator.getUri() != null && !oldIndicator.getUri().equals(newURI)) {
				changedInfos.add(newURI);
			}
			if (oldIndicator.getLabel() != null && !oldIndicator.getLabel().equals(newLabel)) {
				changedInfos.add(newLabel);
			}
			if (oldIndicator.getComment() == null || !oldIndicator.getComment().equals(newComment)) {
				changedInfos.add(newComment);
			}
			// delete previous state of the Indicator in the triplestore
			if (oldIndicator != null) {
				oldIndicator.delete();
			}
		} else {
			return badRequest("[ERRO] Failed locating existing Indicator.\n");
		}

		// insert current state of the Indicator
		oldIndicator.setUri(newURI);
		oldIndicator.setLabel(newLabel);
		oldIndicator.setComment(newComment);
		// insert the new Indicator content inside of the triplestore regardless of any change -- the previous content has already been deleted
		oldIndicator.save();
		

		return ok(indicatorConfirm.render("Edit Indicator", oldIndicator));
	}
}
