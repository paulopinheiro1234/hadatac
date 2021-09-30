package org.hadatac.console.controllers.schema;

import java.util.List;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.PossibleValue;
//import controllers.AuthApplication;

import org.pac4j.play.java.Secure;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Controller;

import javax.inject.Inject;

public class DASManagement extends Controller {
    @Inject
    Application application;

  @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    @SubjectPresent(forceBeforeAuthCheck = true)
    public Result index(Http.Request request) {
        List<DataAcquisitionSchema> sdds = DataAcquisitionSchema.findAll();
        return ok(org.hadatac.console.views.html.schema.DASManagement.render(sdds,application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    @SubjectPresent(forceBeforeAuthCheck = true)
    public Result postIndex(Http.Request request) {
        return index(request);
    }


}