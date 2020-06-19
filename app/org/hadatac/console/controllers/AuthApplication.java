package org.hadatac.console.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import org.apache.jena.sparql.function.library.print;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;

import play.data.Form;
import play.mvc.*;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;

import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider.MyLogin;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider.MySignup;
import org.hadatac.console.providers.UserProvider;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.triplestore.*;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.user.AuthUser;

import com.feth.play.module.pa.controllers.Authenticate;

public class AuthApplication extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";
	public static final String DATA_OWNER_ROLE = "data_owner";
	public static final String DATA_MANAGER_ROLE = "data_manager";
	public static final String FILE_VIEWER_EDITOR_ROLE = "file_viewer_editor";
	
	private static AuthApplication authApplication = null;
	private final PlayAuthenticate auth;
	private final MyUsernamePasswordAuthProvider provider;
	private final UserProvider userProvider;
	private final Authenticate authenticate;
	
	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}

	@Inject
	public AuthApplication(final PlayAuthenticate auth, 
			final MyUsernamePasswordAuthProvider provider,
			final UserProvider userProvider,
			final Authenticate authenticate) {
		this.auth = auth;
		this.provider = provider;
		this.userProvider = userProvider;
		this.authenticate = authenticate;
		authApplication = this;
	}
	
	public static AuthApplication getAuthApplication() {
		return authApplication;
	}
	
	public UserProvider getUserProvider() {
		return userProvider;
	}
	
	public Result index() {
		return ok(portal.render());
	}
	
	public static SysUser getLocalUser(final Session session) {
		return AuthApplication.getAuthApplication().getUserProvider().getUser(session);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result restricted() {
		final SysUser localUser = this.userProvider.getUser(session());
		return ok(restricted.render(localUser));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result profile() {
		final SysUser localUser = userProvider.getUser(session());
		return ok(profile.render(this.auth, localUser));
	}

	public Result login() {
		return ok(login.render(this.provider.getLoginForm()));
	}

	public Result doLogin() {
	        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MyLogin> filledForm = this.provider.getLoginForm()
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(login.render(filledForm));
		} else {
			return this.provider.handleLogin(ctx());
		}
	}

	public Result signup() {
		return ok(signup.render(this.auth, this.provider.getSignupForm()));
	}

	public Result jsRoutes() {
		return ok(JavaScriptReverseRouter.create("jsRoutes", 
				routes.javascript.Signup.forgotPassword()))
				.as("text/javascript");
	}

	public Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MySignup> filledForm = this.provider.getSignupForm().bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(signup.render(this.auth, filledForm));
		} else {
			if (SysUser.existsSolr()) { // only check for pre-registration if it is not the first user signing up
				if (!UserManagement.isPreRegistered(filledForm.get().email)) {
					return ok(notRegistered.render());
				}
			}
			
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return this.provider.handleSignup(ctx());
		}
	}
	
	public Result doSignout() {
		return this.authenticate.logout();
	}
}