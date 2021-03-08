package org.hadatac.console.controllers.dataacquisitionmanagement;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.xml.security.configuration.HandlerType;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

//import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.routes;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Measurement;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import javax.inject.Inject;

public class DeleteDataAcquisition extends Controller {
    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String oper, String uri, Http.Request request) {
        STR dc = new STR();
        try {
            if (uri != null) {
                uri = URLDecoder.decode(uri, "UTF-8");
            } else {
                uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!uri.equals("")) {

            /*
             *  Add deployment information into handler
             */

            dc = STR.findByUri(uri);
            if (dc == null) {
                return badRequest("Incorrect data acquisition uri detected!");
            }
            final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

            return ok(deleteDataAcquisition.render(oper, dc, user.isDataManager(),user.getEmail()));
        }

        return badRequest("No data acquisition uri specified!");
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String oper, String uri,Http.Request request) {
        return index(oper, uri, request);
    }

    public static String delete(String uri) {
        STR dc = new STR();
        try {
            if (uri != null) {
                uri = URLDecoder.decode(uri, "UTF-8");
            } else {
                uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!uri.equals("")) {

            /*
             *  Add deployment information into handler
             */

            dc = STR.findByUri(uri);
            dc.deleteFromSolr();

            return "Stream Specification deleted.";
        }

        return "Stream Specification failed to be deleted.";
    }

    public Result deleteDataPoints(String uri, int state) {
        STR dc = new STR();
        try {
            if (uri != null) {
                uri = URLDecoder.decode(uri, "UTF-8");
            } else {
                uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!uri.equals("")) {

            /*
             *  Add deployment information into handler
             */

            dc = STR.findByUri(uri);
            if (!dc.deleteMeasurementData()) {
                dc.setNumberDataPoints(Measurement.getNumByDataAcquisition(dc));
                dc.save();
                return badRequest("Measurement data in this data acquisition failed to be deleted.");
            }
            dc.setNumberDataPoints(0);
            dc.save();

            return redirect(routes.DataAcquisitionManagement.index(state));
        }

        return badRequest("Measurement data in this data acquisition failed to be deleted.");
    }

    public Result postDeleteDataPoints(String uri, int state) {
        return deleteDataPoints(uri, state);
    }
}
