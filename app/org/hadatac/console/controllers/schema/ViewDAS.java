package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

//import org.hadatac.console.views.schema.*;
//import controllers.AuthApplication;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.PossibleValue;

import javax.inject.Inject;


public class ViewDAS extends Controller {
    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String das_uri, Http.Request request) {
        DataAcquisitionSchema das = null;
        try {
            if (das_uri != null) {
                das_uri = URLDecoder.decode(das_uri, "UTF-8");
            } else {
                das_uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!das_uri.equals("")) {
            das = DataAcquisitionSchema.find(das_uri);
        }

        return ok(org.hadatac.console.views.html.schema.viewDAS.render(das,application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String das_uri,Http.Request request) {
        return index(das_uri,request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result codebook(String schemaUri,Http.Request request) {
        DataAcquisitionSchema sdd = DataAcquisitionSchema.find(schemaUri);
        if (schemaUri != null) {
            List<PossibleValue> codes = PossibleValue.findBySchema(schemaUri);
            return ok(org.hadatac.console.views.html.schema.viewCodeBook.render(sdd, codes, application.getUserEmail(request)));
        }
        return badRequest("Could not retrieve schema from provided uri");
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postCodebook(String schemaUri, Http.Request request) {
        return codebook(schemaUri,request);
    }

}
