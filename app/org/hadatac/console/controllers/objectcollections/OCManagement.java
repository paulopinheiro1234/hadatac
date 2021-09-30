package org.hadatac.console.controllers.objectcollections;

import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.VirtualColumn;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.objectcollections.*;
import org.pac4j.play.java.Secure;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Controller;

import javax.inject.Inject;

public class OCManagement extends Controller {

    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String dir, String filename, String da_uri, String std_uri, Http.Request request) {

        try {
            std_uri = URLDecoder.decode(std_uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //System.out.println("SOC management for " + std_uri);
        Study std = Study.find(std_uri);

        List<ObjectCollection> ocList = ObjectCollection.findByStudyUri(std_uri);
        List<VirtualColumn> vcList = VirtualColumn.findByStudyUri(std_uri);

		/*for (ObjectCollection oc : ocList) {
		    System.out.println("SOC in " + std_uri + " [" + oc.getUri() + "]  with reference [" + oc.getVirtualColumnUri() + "]");
		}
        for (VirtualColumn vc : vcList) {
            System.out.println("VC in " + std_uri + " [" + vc.getUri() + "]");
        }*/


        return ok(objectCollectionManagement.render(dir, filename, da_uri, std, ocList, vcList, application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String dir, String filename, String da_uri, String std_uri, Http.Request request) {
        return index(dir, filename, da_uri, std_uri, request);
    }
}