package org.hadatac.console.controllers.objectcollections;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;
import play.data.FormFactory;

import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.ObjectCollectionType;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.console.views.html.objectcollections.*;
import org.hadatac.console.models.ObjectCollectionForm;
import org.hadatac.console.controllers.AuthApplication;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class NewOC extends Controller {
	
	@Inject
	private FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String dir, String filename, String da_uri, String std_uri) {
		Study study = Study.find(std_uri);
		List<ObjectCollectionType> typeList = ObjectCollectionType.find();

		List<ObjectCollection> domainList = new ArrayList<ObjectCollection>();
		List<ObjectCollection> locationList = new ArrayList<ObjectCollection>();
		List<ObjectCollection> timeList = new ArrayList<ObjectCollection>();
		List<ObjectCollection> objList = ObjectCollection.findByStudyUri(std_uri);
		for (ObjectCollection oc : objList) {
			if (oc.isDomainCollection()) {
				domainList.add(oc);
			} else if (oc.isLocationCollection()) {
				locationList.add(oc);
			} else if (oc.isTimeCollection()) {
				timeList.add(oc);
			}
		}

		return ok(newObjectCollection.render(dir, filename, da_uri, study, domainList, locationList, timeList, typeList));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String dir, String filename, String da_uri, String std_uri) {
		return index(dir, filename, da_uri, std_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result processForm(String dir, String filename, String da_uri, String std_uri) {
		Form<ObjectCollectionForm> form = formFactory.form(ObjectCollectionForm.class).bindFromRequest();
		ObjectCollectionForm data = form.get();

		if (form.hasErrors()) {
			return badRequest("The submitted form has errors!");
		}

		// store new values
		System.out.println("uri: " + data.getNewUri());
		System.out.println("type: " + data.getNewType());

		String newURI = null;
		if (data.getNewUri() == null || data.getNewUri().equals("")) {
			return badRequest("[ERROR] New URI cannot be empty.");
		} else {
			newURI = URIUtils.replacePrefixEx(data.getNewUri());
		}
		String newType = null;
		if (data.getNewType() == null || data.getNewType().equals("")) {
			return badRequest("[ERROR] New type cannot be empty.");
		} else {
			newType = URIUtils.replacePrefixEx(data.getNewType());
		}
		String newStudyUri = URIUtils.replacePrefixEx(std_uri);
		String newLabel = data.getNewLabel();
		String newComment = data.getNewComment();
		String newHasScopeUri = data.getNewHasScopeUri();
		String newHasGroundingLabel = data.getNewHasGroundingLabel();
		String newHasRoleLabel = data.getNewHasRoleLabel();
		String newHasSOCReferenceLabel = data.getNewHasSOCReference();
		List<String> newSpaceScopeUris = data.getSpaceUri();
		List<String> newTimeScopeUris = data.getTimeUri();
		List<String> newGroupUris = data.getGroupUri();

		// insert current state of the OC
		ObjectCollection oc = new ObjectCollection(newURI,
				newType,
				newLabel,
				newComment,
				newStudyUri,
				"", // HACK!
                newHasRoleLabel,
				newHasScopeUri,
				newSpaceScopeUris,
				newTimeScopeUris,
				newGroupUris,
				"0");

		// insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
		oc.save();

		return ok(objectCollectionConfirm.render("New Object Collection has been Generated", dir, filename, da_uri, std_uri, oc));
	}

}
