package org.hadatac.console.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import org.apache.jena.sparql.function.library.print;
import org.hadatac.Constants;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;

import org.hadatac.console.providers.UserProvider;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.mvc.*;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;

import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;
//import org.hadatac.console.providers.MyUsernamePasswordAuthProvider.MyLogin;
//import org.hadatac.console.providers.MyUsernamePasswordAuthProvider.MySignup;
//import org.hadatac.console.providers.UserProvider;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.triplestore.*;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class AuthApplication extends Controller {
    @Inject
    private Application application;

    public static final String FLASH_MESSAGE_KEY = "message";
    public static final String FLASH_ERROR_KEY = "error";
    private static AuthApplication authApplication = null;
    private final MyUsernamePasswordAuthProvider provider;
    private final UserProvider userProvider;

    public static String formatTimestamp(final long t) {
        return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
    }

    @Inject
    public AuthApplication(final MyUsernamePasswordAuthProvider provider,
                           final UserProvider userProvider
            ) {
        this.provider = provider;
        this.userProvider = userProvider;
        authApplication = this;
    }

    public static AuthApplication getAuthApplication() {
        return authApplication;
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }

    public Result index(Http.Request request) {
        return ok(portal.render(application.getUserEmail(request )));
    }

    public static SysUser getLocalUser( String username) {
        return AuthApplication.getAuthApplication().getUserProvider().getUser(username);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result restricted(String username) {
        final SysUser localUser = this.userProvider.getUser(username);
        return ok(restricted.render(localUser,localUser.getEmail()));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result profile(String username) {
        final SysUser localUser = userProvider.getUser(username);
        return ok(profile.render(localUser));
    }

    //TODO: test this
    public Result jsRoutes() {
        return ok(JavaScriptReverseRouter.create("jsRoutes","","",
                routes.javascript.Signup.forgotPassword()))
                .as("text/javascript");
    }
}