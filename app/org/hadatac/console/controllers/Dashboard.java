package org.hadatac.console.controllers;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;

import org.hadatac.console.views.html.main;
import org.hadatac.console.views.html.dashboard;
import org.hadatac.utils.Repository;
import scala.concurrent.stm.skel.HashTrieTMap;

import javax.inject.Inject;

public class Dashboard extends Controller {
    @Inject Application application;

    public Result index(Http.Request request) {
        if (!Repository.operational(Repository.METADATA)) {
            return ok(main.render("Results", "","",
                    new Html("<div class=\"container-fluid\"><h4>"
                            + "The triple store is NOT properly connected. "
                            + "Please restart it or check the hadatac configuration file!"
                            + "</h4></div>")));
        }
        if (!Repository.operational(Repository.DATA)) {
            return ok(main.render("Results", "","",
                    new Html("<div class=\"container-fluid\"><h4>"
                            + "HADatAC Solr is down now. Ask Administrator for further information. "
                            + "</h4></div>")));
        }

        return ok(dashboard.render(application.getUserEmail(request)));
    }

    public Result postIndex(Http.Request request) {
        return index(request);
    }
}
