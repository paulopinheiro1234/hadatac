package org.hadatac.console.controllers.studies;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.views.html.studies.*;
import org.hadatac.entity.pojo.Study;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import javax.inject.Inject;

public class DeleteStudy extends Controller {

    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String study_uri, Http.Request request) {
        Study study = null;
        String result = "";
        try {
            if (study_uri != null) {
                study_uri = URLDecoder.decode(study_uri, "UTF-8");
            } else {
                study_uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!study_uri.equals("")) {
            study = Study.find(study_uri);
            return ok(deleteStudy.render(study, result,application.getUserEmail(request)));
        }

        return ok(deleteStudy.render(study, result,application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String study_uri, Http.Request request) {
        return index(study_uri,request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public static String deleteStudy(String studyUri) {
        NameSpaces.getInstance();
        MetadataContext metadata = new MetadataContext("user", "password",
                ConfigFactory.load().getString("hadatac.solr.triplestore"), false);

        return metadata.cleanStudy(Feedback.WEB, studyUri);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result processForm(String study_uri,Http.Request request) {
        Study study = null;

        try {
            if (study_uri != null) {
                study_uri = URLDecoder.decode(study_uri, "UTF-8");
            } else {
                study_uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!study_uri.equals("")) {
            study = Study.find(study_uri);
        }

        study.delete();

        return ok(studyConfirm.render("Deleted Study", study, application.getUserEmail(request)));
    }
}
