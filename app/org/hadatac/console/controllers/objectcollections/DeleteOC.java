package org.hadatac.console.controllers.objectcollections;

import java.awt.desktop.AppForegroundListener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.google.inject.Inject;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.views.html.objectcollections.*;
import org.hadatac.entity.pojo.Study;
import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.ObjectCollection;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;

public class DeleteOC extends Controller {

    @Inject
    private Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String dir, String filename, String da_uri, String std_uri, String oc_uri, Http.Request request) {

        try {
            std_uri = URLDecoder.decode(std_uri, "UTF-8");
            oc_uri = URLDecoder.decode(oc_uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            std_uri = "";
            oc_uri = "";
        }

        Study study = Study.find(std_uri);
        if (study == null) {
            return badRequest(objectCollectionConfirm.render("Error deleting object collection: Study URI did not return valid URI", dir, filename, da_uri, std_uri, null, application.getUserEmail(request)));
        }

        ObjectCollection oc = ObjectCollection.find(oc_uri);
        if (oc == null) {
            return badRequest(objectCollectionConfirm.render("Error deleting object collection: ObjectCollection URI did not return valid object", dir, filename, da_uri, std_uri, oc, application.getUserEmail(request)));
        }

        return ok(deleteObjectCollection.render(dir, filename, da_uri, std_uri, oc, application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String dir, String filename, String da_uri, String std_uri, String oc_uri, Http.Request request) {
        return index(dir, filename, da_uri, std_uri, oc_uri, request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result processForm(String dir, String filename, String da_uri, String std_uri, String oc_uri, Http.Request request) {
        final SysUser sysUser = AuthApplication.getLocalUser(application.getUserEmail(request));

        Study std = Study.find(std_uri);

        ObjectCollection oc = ObjectCollection.find(oc_uri);

        int deletedRows = -1;
        if (oc != null) {
            oc.deleteFromTripleStore();;
        }

        return ok(objectCollectionConfirm.render("Object Collection has been Deleted", dir, filename, da_uri, std_uri, oc, sysUser.getEmail()));
    }
}
