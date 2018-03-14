package org.hadatac.console.controllers.objectcollections;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.objectcollections.*;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;

public class DeleteOC extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String filename, String da_uri, String std_uri, String oc_uri) {
        if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
            return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
                    org.hadatac.console.controllers.objectcollections.routes.DeleteOC.index(filename, da_uri, std_uri, oc_uri).url()));
        }

        try {
            std_uri = URLDecoder.decode(std_uri, "UTF-8");
            oc_uri = URLDecoder.decode(oc_uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            std_uri = "";
            oc_uri = "";
        }

        Study study = Study.find(std_uri);
        if (study == null) {
            return badRequest(objectCollectionConfirm.render("Error deleting object collection: Study URI did not return valid URI", filename, da_uri, std_uri, null));
        } 

        ObjectCollection oc = ObjectCollection.find(oc_uri);
        if (oc == null) {
            return badRequest(objectCollectionConfirm.render("Error deleting object collection: ObjectCollection URI did not return valid object", filename, da_uri, std_uri, oc));
        } 

        return ok(deleteObjectCollection.render(filename, da_uri, std_uri, oc));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String filename, String da_uri, String std_uri, String oc_uri) {
        return index(filename, da_uri, std_uri, oc_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm(String filename, String da_uri, String std_uri, String oc_uri) {
        final SysUser sysUser = AuthApplication.getLocalUser(session());

        Study std = Study.find(std_uri);

        ObjectCollection oc = ObjectCollection.find(oc_uri);

        int deletedRows = -1;
        if (oc != null) {
            System.out.println("calling oc.deleteFromLabKey() from DeleteObjectCollection"); 
            deletedRows = oc.deleteFromLabKey(session().get("LabKeyUserName"),session().get("LabKeyPassword"));
            if (deletedRows > 0) {
                oc.delete();
            } else {
                String message = "Number of deleted rows: " + deletedRows;
                return badRequest(objectCollectionConfirm.render("Error deleting object collection: zero deleted rows", filename, da_uri, std_uri, oc));
            }
        }

        return ok(objectCollectionConfirm.render("Object Collection has been Deleted", filename, da_uri, std_uri, oc));
    }
}
