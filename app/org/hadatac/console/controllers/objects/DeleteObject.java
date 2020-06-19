package org.hadatac.console.controllers.objects;

import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.console.views.html.objects.*;
import org.hadatac.console.controllers.AuthApplication;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DeleteObject extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri, String std_uri, String oc_uri, String obj_id, int page) {
        
        try {
            std_uri = URLDecoder.decode(std_uri, "utf-8");
            oc_uri = URLDecoder.decode(oc_uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            std_uri = "";
            oc_uri = "";
        }

        Study study = Study.find(std_uri);
        if (study == null) {
            return badRequest(objectConfirm.render("Error editing object: Study URI did not return valid URI", dir, filename, da_uri, std_uri, oc_uri, page));
        } 

        ObjectCollection oc = ObjectCollection.find(oc_uri);
        if (oc == null) {
            return badRequest(objectConfirm.render("Error editing object: ObjectCollection URI did not return valid object", dir, filename, da_uri, std_uri, oc_uri, page));
        } 

        List<StudyObject> objects = StudyObject.findByCollectionWithPages(oc, ObjectManagement.PAGESIZE, page);

        //return ok(editObject.render(study, oc, objects));
        return badRequest(objectConfirm.render("PLACEHOLDER", dir, filename, da_uri, std_uri, oc_uri, page));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri, String std_uri, String oc_uri, String obj_id, int page) {
        return index(dir, filename, da_uri, std_uri, oc_uri, obj_id, page);
    }

}
