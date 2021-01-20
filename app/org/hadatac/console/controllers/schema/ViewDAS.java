package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;

//import org.hadatac.console.views.schema.*;
//import controllers.AuthApplication;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.PossibleValue;


public class ViewDAS extends Controller {

//    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String das_uri) {
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

        return ok(org.hadatac.console.views.html.schema.viewDAS.render(das));
    }

//    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String das_uri) {
        return index(das_uri);
    }

//    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result codebook(String schemaUri) {
        DataAcquisitionSchema sdd = DataAcquisitionSchema.find(schemaUri);
        if (schemaUri != null) {
            List<PossibleValue> codes = PossibleValue.findBySchema(schemaUri);
            return ok(org.hadatac.console.views.html.schema.viewCodeBook.render(sdd, codes));
        }
        return badRequest("Could not retrieve schema from provided uri");
    }

//    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postCodebook(String schemaUri) {
        return codebook(schemaUri);
    }

}
