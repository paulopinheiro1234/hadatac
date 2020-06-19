package org.hadatac.console.controllers.objectcollections;

import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class EditOC extends Controller {
	
	@Inject
	private FormFactory formFactory;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String dir, String filename, String da_uri, String std_uri, String oc_uri) {
		
		try {
			std_uri = URLDecoder.decode(std_uri, "UTF-8");
			oc_uri = URLDecoder.decode(oc_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Study study = Study.find(std_uri);
		if (study == null) {
			return badRequest(objectCollectionConfirm.render("Error deleting object collection: Study URI did not return valid URI", dir, filename, da_uri, std_uri, null));
		} 

		ObjectCollection oc = ObjectCollection.find(oc_uri);
		if (oc == null) {
			return badRequest(objectCollectionConfirm.render("Error deleting object collection: ObjectCollection URI did not return valid object", dir, filename, da_uri, std_uri, oc));
		} 

		List<ObjectCollectionType> typeList = ObjectCollectionType.find();

		List<ObjectCollection> domainList = new ArrayList<ObjectCollection>();
		List<ObjectCollection> locationList = new ArrayList<ObjectCollection>();
		List<ObjectCollection> timeList = new ArrayList<ObjectCollection>();
		List<ObjectCollection> objList = ObjectCollection.findByStudyUri(std_uri);
		for (ObjectCollection objc : objList) {
			if (objc.isDomainCollection() && !objc.getUri().equals(oc.getUri())) {
				domainList.add(objc);
			} else if (objc.isLocationCollection() && !objc.getUri().equals(oc.getUri())) {
				locationList.add(objc);
			} else if (objc.isTimeCollection() && !objc.getUri().equals(oc.getUri())) {
				timeList.add(objc);
			}
		}

		return ok(editObjectCollection.render(dir, filename, da_uri, study, oc, domainList, locationList, timeList, typeList));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String dir, String filename, String da_uri, String std_uri, String oc_uri) {
		return index(dir, filename, da_uri, std_uri, oc_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result processForm(String dir, String filename, String da_uri, String std_uri, String oc_uri) {
		final SysUser sysUser = AuthApplication.getLocalUser(session());
		List<String> changedInfos = new ArrayList<String>();

		// Retrieve form information
		Form<ObjectCollectionForm> form = formFactory.form(ObjectCollectionForm.class).bindFromRequest();
		ObjectCollectionForm data = form.get();

		if (form.hasErrors()) {
			return badRequest("The submitted form has errors!");
		}

		String newStudyUri = URIUtils.replacePrefixEx(std_uri);
		String newLabel = data.getNewLabel();
		String newComment = data.getNewComment();
		String newHasScopeUri = data.getNewHasScopeUri();
		String newHasGroundingLabel = data.getNewHasGroundingLabel();
		String newHasRoleLabel = data.getNewHasRoleLabel();
		String newHasSOCReference = data.getNewHasSOCReference();
		List<String> newSpaceScopeUris = data.getSpaceUri();
		List<String> newTimeScopeUris = data.getTimeUri();
		List<String> newGroupUris = data.getGroupUri();

		// Verify Study and ObjectCollection information is valid
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
		Study std = Study.find(std_uri);
		if (std == null) {
			return badRequest("[ERROR] Cannot retriev Study from given URI: " + std_uri);
		}
		ObjectCollection oldOc = ObjectCollection.find(oc_uri);
		if (oldOc == null) {
			return badRequest("[ERROR] Cannot retriev Object Collection from given URI: " + oc_uri);
		}

		// compare old and new object collections
		if (oldOc.getUri() != null && !oldOc.getUri().equals(newURI)) {
			changedInfos.add(newURI);
		}
		if (oldOc.getTypeUri() != null && !oldOc.getTypeUri().equals(newType)) {
			changedInfos.add(newType);
		}
		if (oldOc.getLabel() != null && !oldOc.getLabel().equals(newLabel)) {
			changedInfos.add(newLabel);
		}
		if (oldOc.getComment() != null && !oldOc.getComment().equals(newComment)) {
			changedInfos.add(newComment);
		}
		if (oldOc.getStudyUri() == null || !oldOc.getStudyUri().equals(newStudyUri)) {
			changedInfos.add(newStudyUri);
		}
		if (oldOc.getHasScopeUri() == null || !oldOc.getHasScopeUri().equals(newHasScopeUri)) {
			changedInfos.add(newHasScopeUri);
		}
		if (oldOc.getGroundingLabel() == null || !oldOc.getGroundingLabel().equals(newHasGroundingLabel)) {
			changedInfos.add(newHasGroundingLabel);
		}
		if (oldOc.getRoleLabel() == null || !oldOc.getRoleLabel().equals(newHasRoleLabel)) {
			changedInfos.add(newHasRoleLabel);
		}
		if (oldOc.getSOCReference() == null || !oldOc.getSOCReference().equals(newHasSOCReference)) {
			changedInfos.add(newHasSOCReference);
		}
		//if (oldOc.getSpaceScopeUris() == null || !oldOc.getSpaceScopeUris().equals(newSpaceScopeUris)) {
		//    changedInfos.add(newSpaceScopeUri);
		//}
		//if (oldOc.getTimeScopeUris() == null || !oldOc.getTimeScopeUris().equals(newTimeScopeUris)) {
		//    changedInfos.add(newTimeScopeUri);
		//}

		// insert current state of the OC
		ObjectCollection newOc = new ObjectCollection(
		        newURI,
				newType,
				newLabel,
				newComment,
				newStudyUri,
				"",  //HACK!
                newHasRoleLabel,
				newHasScopeUri,
				newSpaceScopeUris,
				newTimeScopeUris,
				newGroupUris,
				"0");
		newOc.setObjectUris(oldOc.getObjectUris());

		// delete previous content and insert new OC content inside of triplestore
		oldOc.delete();
		newOc.save();

		return ok(objectCollectionConfirm.render("New Object Collection has been Edited", dir, filename, da_uri, std_uri, newOc));
	}

}
