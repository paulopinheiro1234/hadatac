package org.hadatac.console.controllers.dataacquisitionsearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.jena.base.Sys;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Study;
import org.pac4j.play.java.Secure;
import play.libs.Files.TemporaryFile;
import play.mvc.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http.MultipartFormData.FilePart;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.hadatac.console.views.html.dataacquisitionsearch.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class Harmonization extends Controller {

    @Inject
    private FormFactory formFactory;
    @Inject
    Application application;

    // @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result index(String oper, Http.Request request) {

        List<Study> studyList = Study.find();

        if (oper.equals("study")) {
            List<Study> queryList = new ArrayList<Study>();
            return ok(harmonizationStudy.render(studyList, queryList, application.getUserEmail(request)));
        }

        List<ObjectCollection> queryList = new ArrayList<ObjectCollection>();
        List<ObjectCollection> socList = ObjectCollection.findAll();
        return ok(harmonizationSoc.render(studyList, socList, queryList, application.getUserEmail(request)));
    }

    // @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postIndex(String oper, Http.Request request) {
        return index(oper, request);
    }

    public static String playLoadOntologies(String oper) {
        NameSpaces.getInstance();
        MetadataContext metadata = new
                MetadataContext("user",
                "password",
                ConfigFactory.load().getString("hadatac.solr.triplestore"),
                false);
        return metadata.loadOntologies(Feedback.WEB, oper);
    }

}
