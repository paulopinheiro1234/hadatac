package org.hadatac.console.controllers;

import com.typesafe.config.ConfigFactory;
import org.hadatac.console.views.html.landingPage;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;
import org.hadatac.console.views.html.main;
import org.hadatac.console.views.html.portal;
import org.hadatac.console.views.html.dashboard;
import org.hadatac.console.models.SysUser;
import org.hadatac.utils.Repository;

import javax.inject.Inject;

public class Portal extends Controller {
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
        if (!Repository.checkNamespaceWithQuads()) {
            return ok(main.render("Results", "","",
                    new Html("<div class=\"container-fluid\"><h4>"
                            + "The namespace store is set to be without quads. Ask Administrator for further information. "
                            + "</h4></div>")));
        }
        SysUser user = AuthApplication.getAuthApplication().getUserProvider().getUser(application.getUserEmail(request));

        System.out.println("Portal user= " + user);
        if (user == null) {
            if("true".equalsIgnoreCase(ConfigFactory.load().getString("hadatac.ThirdPartyUser.userRedirection"))){
                return ok(landingPage.render(application.getUserEmail(request)));
            } else
                return ok(portal.render(application.getUserEmail(request)));
        } else {
            return ok(portal.render(application.getUserEmail(request)));
        }
    }

    public Result postIndex(Http.Request request){
        return ok(portal.render(application.getUserEmail(request)));}
}
