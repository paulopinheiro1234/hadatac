package org.hadatac.console.controllers.indicators;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.StudyForm;
import org.hadatac.console.views.html.indicators.*;
import org.hadatac.entity.pojo.Indicator;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import scala.concurrent.stm.skel.HashTrieTMap;

import javax.inject.Inject;

public class DeleteIndicator extends Controller {
    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String ind_uri, Http.Request request) {

        Indicator indicator = null;
        String result = "";
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

            indicator.delete();

            result = deleteIndicator(DynamicFunctions.replaceURLWithPrefix(ind_uri));

            return ok(deleteIndicator.render(indicator, result,application.getUserEmail(request)));
        }

        return ok(deleteIndicator.render(indicator, result, application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String ind_uri,Http.Request request) {
        return index(ind_uri,request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public static String deleteIndicator(String ind_uri) {
        String result = "";
        NameSpaces.getInstance();
        Indicator newIndicator = new Indicator(ind_uri);
        newIndicator.delete();
        return result;
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result processForm(String ind_uri,Http.Request request) {
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
        }

        indicator.delete();

        return ok(indicatorConfirm.render("Delete Indicator", indicator,application.getUserEmail(request)));
    }
}
