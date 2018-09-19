package org.hadatac.console.controllers.triplestore;

import play.mvc.*;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.data.loader.DataContext;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.PermissionsContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class Clean extends Controller {

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result clean(String oper) {
        return ok(clean.render(oper));
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postClean(String oper) {
        return ok(clean.render(oper));
    }

    public static String playClean(String oper) {
        String result = "";
        if (oper.equals("metadata")) {
            MetadataContext metadata = new 
                    MetadataContext("user", 
                            "password",
                            ConfigFactory.load().getString("hadatac.solr.triplestore"), 
                            false);
            result = metadata.clean(Feedback.WEB);
            NameSpaces.reload();
        } else if (oper.equals("usergraph")) {
            PermissionsContext permission = new 
                    PermissionsContext("user", 
                            "password",
                            ConfigFactory.load().getString("hadatac.solr.permissions"), 
                            false);
            result = permission.clean(Feedback.WEB);

            DataContext userCollection = new 
                    DataContext("user", 
                            "password",
                            ConfigFactory.load().getString("hadatac.solr.users"), 
                            false);
            result = userCollection.cleanDataUsers(Feedback.WEB);

            DataContext linkedCollection = new 
                    DataContext("user", 
                            "password",
                            ConfigFactory.load().getString("hadatac.solr.data"), 
                            false);
            result = linkedCollection.cleanDataAccounts(Feedback.WEB);
        } else if (oper.equals("collections")) {
            DataContext collection = new 
                    DataContext("user", 
                            "password",
                            ConfigFactory.load().getString("hadatac.solr.data"), 
                            false);
            result = collection.cleanDataAcquisitions(Feedback.WEB);
        } else if (oper.equals("acquisitions")) {
            DataContext acquisition = new 
                    DataContext("user", 
                            "password",
                            ConfigFactory.load().getString("hadatac.solr.data"), 
                            false);
            result = acquisition.cleanAcquisitionData(Feedback.WEB);
        } 

        return result;
    }

}
