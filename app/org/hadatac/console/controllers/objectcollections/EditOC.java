package org.hadatac.console.controllers.objectcollections;

import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.ObjectCollectionType;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.views.html.objectcollections.*;
import org.hadatac.console.models.ObjectCollectionForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class EditOC extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String filename, String da_uri, String std_uri, String oc_uri) {
		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					org.hadatac.console.controllers.objectcollections.routes.EditOC.index(filename, da_uri, std_uri, oc_uri).url()));
		}
		try {
			std_uri = URLDecoder.decode(std_uri, "UTF-8");
			oc_uri = URLDecoder.decode(oc_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Study study = Study.find(std_uri);
		if (study == null) {
			return badRequest(objectCollectionConfirm.render("Error deleting object collection: Study URI did not return valid URI", filename, da_uri, std_uri, null));
		} 

		ObjectCollection oc = ObjectCollection.find(oc_uri);
		if (oc == null) {
			return badRequest(objectCollectionConfirm.render("Error deleting object collection: ObjectCollection URI did not return valid object", filename, da_uri, std_uri, oc));
		} 

		List<ObjectCollectionType> typeList = ObjectCollectionType.find();

		List<ObjectCollection> domainList = new ArrayList<ObjectCollection>();
		List<ObjectCollection> locationList = new ArrayList<ObjectCollection>();
		List<ObjectCollection> timeList = new ArrayList<ObjectCollection>();
		List<ObjectCollection> objList = ObjectCollection.findByStudy(study);
		for (ObjectCollection objc : objList) {
			if (objc.isDomainCollection() && !objc.getUri().equals(oc.getUri())) {
				domainList.add(objc);
			} else if (objc.isLocationCollection() && !objc.getUri().equals(oc.getUri())) {
				locationList.add(objc);
			} else if (objc.isTimeCollection() && !objc.getUri().equals(oc.getUri())) {
				timeList.add(objc);
			}
		}

		return ok(editObjectCollection.render(filename, da_uri, study, oc, domainList, locationList, timeList, typeList));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postIndex(String filename, String da_uri, String std_uri, String oc_uri) {
		return index(filename, da_uri, std_uri, oc_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processForm(String filename, String da_uri, String std_uri, String oc_uri) {
		final SysUser sysUser = AuthApplication.getLocalUser(session());
		List<String> changedInfos = new ArrayList<String>();

		// Retrieve form information
		Form<ObjectCollectionForm> form = Form.form(ObjectCollectionForm.class).bindFromRequest();
		ObjectCollectionForm data = form.get();

		if (form.hasErrors()) {
			return badRequest("The submitted form has errors!");
		}

		//System.out.println("uri: " + data.getNewUri());
		//System.out.println("type: " + data.getNewType());
		String newStudyUri = ValueCellProcessing.replacePrefixEx(std_uri);
		String newLabel = data.getNewLabel();
		String newComment = data.getNewComment();
		String newHasScopeUri = data.getNewHasScopeUri();
		List<String> newSpaceScopeUris = data.getSpaceUri();
		List<String> newTimeScopeUris = data.getTimeUri();

		// Verify Study and ObjectCollection information is valid
		String newURI = null;
		if (data.getNewUri() == null || data.getNewUri().equals("")) {
			return badRequest("[ERROR] New URI cannot be empty.");
		} else {
			newURI = ValueCellProcessing.replacePrefixEx(data.getNewUri());
		}
		String newType = null;
		if (data.getNewType() == null || data.getNewType().equals("")) {
			return badRequest("[ERROR] New type cannot be empty.");
		} else {
			newType = ValueCellProcessing.replacePrefixEx(data.getNewType());
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
		//if (oldOc.getSpaceScopeUris() == null || !oldOc.getSpaceScopeUris().equals(newSpaceScopeUris)) {
		//    changedInfos.add(newSpaceScopeUri);
		//}
		//if (oldOc.getTimeScopeUris() == null || !oldOc.getTimeScopeUris().equals(newTimeScopeUris)) {
		//    changedInfos.add(newTimeScopeUri);
		//}

		// insert current state of the OC
		ObjectCollection newOc = new ObjectCollection(newURI,
				newType,
				newLabel,
				newComment,
				newStudyUri,
				newHasScopeUri,
				newSpaceScopeUris,
				newTimeScopeUris);
		newOc.setObjectUris(oldOc.getObjectUris());

		// delete previous content and insert new OC content inside of triplestore
		oldOc.delete();
		newOc.save();

		// update/create new OC in LabKey
		int nRowsAffected = newOc.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
			return badRequest("Failed to edit OC into LabKey!\n");
		}
		return ok(objectCollectionConfirm.render("New Object Collection has been Edited", filename, da_uri, std_uri, newOc));
	}

}
