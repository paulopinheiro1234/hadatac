package org.hadatac.console.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;

import play.Routes;
import play.data.Form;
import play.mvc.*;
import play.mvc.Http.Session;
import play.mvc.Result;

import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider.MyLogin;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider.MySignup;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.triplestore.*;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.user.AuthUser;

public class AuthApplication extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";
	public static final String DATA_OWNER_ROLE = "data_owner";
	public static final String DATA_MANAGER_ROLE = "data_manager";
	
	public static Result index() {
		return ok(portal.render());
	}

	public static SysUser getLocalUser(final Session session) {
		final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
		final SysUser localUser = SysUser.findByAuthUserIdentity(currentAuthUser);
		return localUser;
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result restricted() {
		final SysUser localUser = getLocalUser(session());
		return ok(restricted.render(localUser));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result profile() {
		final SysUser localUser = getLocalUser(session());
		return ok(profile.render(localUser));
	}

	public static Result login() {
		return ok(login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
	}

	public static Result doLogin() {
	        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(login.render(filledForm));
		} else {
			return UsernamePasswordAuthProvider.handleLogin(ctx());
		}
	}

	public static Result signup() {
		return ok(signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
	}

	public static Result jsRoutes() {
		return ok(
				Routes.javascriptRouter("jsRoutes",
						org.hadatac.console.controllers.routes.javascript.Signup.forgotPassword()))
				.as("text/javascript");
	}

	public static Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MySignup> filledForm = 
				MyUsernamePasswordAuthProvider.SIGNUP_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
			return badRequest(signup.render(filledForm));
		} else {
			if (SysUser.existsSolr()) { // only check for pre-registration if it is not the first user signing up
				if (!UserManagement.isPreRegistered(filledForm.get().email)) {
					return ok(notRegistered.render());
				}
			}
			
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return UsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}
	
	public static Result doSignout() {
		session().remove("LabKeyUserName");
		session().remove("LabKeyPassword");
		return com.feth.play.module.pa.controllers.Authenticate.logout();
	}

	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}
}