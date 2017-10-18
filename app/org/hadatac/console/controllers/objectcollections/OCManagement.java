package org.hadatac.console.controllers.objectcollections;

import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.objectcollections.*;
import play.mvc.Result;
import play.mvc.Controller;

public class OCManagement extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String filename, String da_uri, String std_uri) {

		try {
			std_uri = URLDecoder.decode(std_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Study std = Study.find(std_uri);

		List<ObjectCollection> ocList = ObjectCollection.findByStudy(std);

		return ok(objectCollectionManagement.render(filename, da_uri, std, ocList));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String filename, String da_uri, String std_uri) {
		return index(filename, da_uri, std_uri);
	}
}