package org.hadatac.console.controllers.objects;

import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.utils.ConfigProp;
import org.hadatac.console.views.html.objects.*;
import org.hadatac.console.models.ObjectsForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ObjectManagement extends Controller {

    @Inject
    private FormFactory formFactory;
    
    public static int PAGESIZE = 20;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result indexNomsg(String dir, String filename, String da_uri, String std_uri, String oc_uri, int page) {
        return index(dir, filename, da_uri, std_uri, oc_uri, page, "");
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri, String std_uri, String oc_uri, int page, String message) {

    	try {
            std_uri = URLDecoder.decode(std_uri, "utf-8");
            oc_uri = URLDecoder.decode(oc_uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            std_uri = "";
            oc_uri = "";
        }

        Study study = Study.find(std_uri);
        if (study == null) {
            return badRequest(objectConfirm.render("Error listing object collection: Study URI did not return valid URI", dir, filename, da_uri, std_uri, oc_uri, page));
        } 

        ObjectCollection oc = ObjectCollection.find(oc_uri);
        if (oc == null) {
            return badRequest(objectConfirm.render("Error listing objectn: ObjectCollection URI did not return valid object", dir, filename, da_uri, std_uri, oc_uri, page));
        } 

        List<String> objUriList = new ArrayList<String>(); 
        List<StudyObject> objects = StudyObject.findByCollectionWithPages(oc, PAGESIZE, page * PAGESIZE);
        int total = StudyObject.getNumberStudyObjectsByCollection(oc_uri);
        
        for (StudyObject obj : objects) {
        	if (obj != null && obj.getUri() != null) {
        		objUriList.add(obj.getUri());
        	}
        }

        return ok(objectManagement.render(dir, filename, da_uri, study, oc, objUriList, objects, page, total, message));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri, String std_uri, String oc_uri, int page, String message) {
        return index(dir, filename, da_uri, std_uri, oc_uri, page, message);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result listURIs(String dir, String filename, String da_uri, String std_uri, String oc_uri, int page) {
        
        try {
            std_uri = URLDecoder.decode(std_uri, "utf-8");
            oc_uri = URLDecoder.decode(oc_uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            std_uri = "";
            oc_uri = "";
        }

        Study study = Study.find(std_uri);
        if (study == null) {
            return badRequest(objectConfirm.render("Error listing object collection: Study URI did not return valid URI", dir, filename, da_uri, std_uri, oc_uri, page));
        } 

        ObjectCollection oc = ObjectCollection.find(oc_uri);
        if (oc == null) {
            return badRequest(objectConfirm.render("Error listing objectn: ObjectCollection URI did not return valid object", dir, filename, da_uri, std_uri, oc_uri, page));
        } 

        List<String> objUriList = new ArrayList<String>(); 
        List<StudyObject> objects = StudyObject.findByCollectionWithPages(oc, PAGESIZE, page * PAGESIZE);
        int total = StudyObject.getNumberStudyObjectsByCollection(oc_uri);
        
        for (StudyObject obj : objects) {
            objUriList.add(obj.getUri());
        }

        return ok(listURIs.render(dir, filename, da_uri, study, oc, objUriList, objects, page, total));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postListURIs(String dir, String filename, String da_uri, String std_uri, String oc_uri, int page) {
        return listURIs(dir, filename, da_uri, std_uri, oc_uri, page);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result updateCollectionObjects(String dir, String filename, String da_uri, String std_uri, String oc_uri, List<String> objUriList, int page, int total) {
        final SysUser sysUser = AuthApplication.getLocalUser(session());

        try {
            std_uri = URLDecoder.decode(std_uri, "utf-8");
            oc_uri = URLDecoder.decode(oc_uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            std_uri = "";
            oc_uri = "";
        }

        System.out.println("Study URI entering 1 EditObject: [" + std_uri + "]");
        if (std_uri == null || std_uri.equals("")) {
            return badRequest("Cannot edit objects: empty study URI");
        }
        Study study = Study.find(std_uri);
        if (study == null) {
            return badRequest("Cannot edit objects: invalid study URI");
        }
        System.out.println("Study URI leaving EditObject's Study.find(): " + study.getUri());


        if (oc_uri == null || oc_uri.equals("")) {
            return badRequest("Cannot edit objects: empty object collection URI");
        }
        ObjectCollection oc = ObjectCollection.find(oc_uri);
        if (oc == null) {
            return badRequest("Cannot edit objects: invalid object collection URI");
        }	

        if (objUriList == null || objUriList.size() == 0) {
            return badRequest("Cannot edit objects: empty list of objects");
        }

        // old and new object lists
        List<StudyObject> oldObjList = new ArrayList<StudyObject>();
        List<StudyObject> newObjList = new ArrayList<StudyObject>();
        for (String oldUri : objUriList) {
            try {
                oldUri = URLDecoder.decode(oldUri, "utf-8");
            } catch (UnsupportedEncodingException e) {
                oldUri = "";
            }
            StudyObject obj = StudyObject.find(oldUri);
            oldObjList.add(obj);
        }

        // get new values
        Form<ObjectsForm> form = formFactory.form(ObjectsForm.class).bindFromRequest();
        ObjectsForm data = form.get();
        List<String> newLabels = data.getNewLabel();
        List<String> newOriginalIds = data.getNewOriginalId();
        System.out.println("Total entries: " + newLabels.size());

        // compare and update accordingly 
        int totUpdates = 0;
        int nRowsAffected = 0;
        String message = "";
        StudyObject newObj;
        StudyObject oldObj;
        for (int i = 0; i < oldObjList.size(); i++) {
            System.out.println("New Label: [" + newLabels.get(i).trim() + "]     " + 
                    "Old Label: [" + oldObjList.get(i).getLabel().trim() + "]     " + 
                    "OriginalId: [" + newOriginalIds.get(i) + "]");
            oldObj = oldObjList.get(i);
            if (!oldObj.getLabel().trim().equals(newLabels.get(i).trim()) || 
                    !oldObj.getOriginalId().trim().equals(newOriginalIds.get(i).trim())) {

                // update objects and add to new list
                newObj = new StudyObject(oldObj.getUri(),
                        oldObj.getTypeUri(),
                        newOriginalIds.get(i),
                        newLabels.get(i),
                        oldObj.getIsMemberOf(),
                        oldObj.getComment(),
                        oldObj.getScopeUris(),
                        oldObj.getTimeScopeUris(),
                        oldObj.getSpaceScopeUris()
                        );

                newObj.saveToTripleStore();

                newObjList.add(newObj);
                totUpdates++;

                // add unchanged objects to new list 
            } else {
                newObjList.add(oldObj);
            }
        }
        if (totUpdates > 0) {
            message = " " + totUpdates + " object(s) was/were updated.";
        } else {
            message = " no object was updated";
        }

        if (form.hasErrors()) {
            message = "The submitted form has errors!";
        }

        System.out.println("Study URI leaving EditObject: " + study.getUri());

        return ok(objectManagement.render(dir, filename, da_uri, study, oc, objUriList, newObjList, page, total, message));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result deleteCollectionObjects(String dir, String filename, String da_uri, String std_uri, String oc_uri, List<String> objUriList, int page) {

        final SysUser sysUser = AuthApplication.getLocalUser(session());

        try {
            std_uri = URLDecoder.decode(std_uri, "utf-8");
            oc_uri = URLDecoder.decode(oc_uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            std_uri = "";
            oc_uri = "";
        }

        System.out.println("Study URI entering 1 EditObject: [" + std_uri + "]");
        if (std_uri == null || std_uri.equals("")) {
            return badRequest("Cannot edit objects: empty study URI");
        }
        Study study = Study.find(std_uri);
        if (study == null) {
            return badRequest("Cannot edit objects: invalid study URI");
        }
        System.out.println("Study URI leaving EditObject's Study.find(): " + study.getUri());


        if (oc_uri == null || oc_uri.equals("")) {
            return badRequest("Cannot edit objects: empty object collection URI");
        }
        ObjectCollection oc = ObjectCollection.find(oc_uri);
        if (oc == null) {
            return badRequest("Cannot edit objects: invalid object collection URI");
        }	

        if (objUriList == null || objUriList.size() == 0) {
            return badRequest("Cannot edit objects: empty list of objects");
        }

        // old and new object lists
        List<StudyObject> oldObjList = new ArrayList<StudyObject>();
        for (String oldUri : objUriList) {
            try {
                oldUri = URLDecoder.decode(oldUri, "utf-8");
            } catch (UnsupportedEncodingException e) {
                oldUri = "";
            }
            
            StudyObject obj = StudyObject.find(oldUri);
            oldObjList.add(obj);
        }

        // compare and update accordingly 
        int totDeletes = 0;
        int nRowsAffected = 0;
        String message = "";
        StudyObject oldObj;
        for (int i = 0; i < oldObjList.size(); i++) {
            oldObj = oldObjList.get(i);
            if (oldObj != null) {
            	oldObj.deleteFromTripleStore();
            	totDeletes++;
            }
        }
        if (totDeletes > 0) {
            message = " " + totDeletes + " object(s) was/were deleted.";
        } else {
            message = " no object was deleted";
        }

        return index(dir, filename, da_uri, study.getUri(), oc.getUri(), page, message);
    }
}
