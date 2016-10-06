package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.libs.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.PermissionQueries;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.PermissionsContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.utils.Feedback;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ChangePermission extends Controller {

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result index(String user_uri) {
    	return ok(users.render("init", "", User.find(), ""));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postIndex(String user_rui) {
    	return ok(users.render("init", "", User.find(), ""));
    }


}
