package org.hadatac.console.providers;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.google.inject.Inject;

import org.hadatac.console.controllers.routes;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.LinkedAccount;
import org.hadatac.console.models.TokenAction;
import org.hadatac.console.models.TokenAction.Type;
import org.hadatac.console.models.SysUser;

import play.Application;
import play.Logger;
import play.Play;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Call;
import play.mvc.Http.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static play.data.Form.form;

public class MyUsernamePasswordAuthProvider
		extends
		UsernamePasswordAuthProvider<String, MyLoginUsernamePasswordAuthUser, MyUsernamePasswordAuthUser, MyUsernamePasswordAuthProvider.MyLogin, MyUsernamePasswordAuthProvider.MySignup> {

	private static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "verificationLink.secure";
	private static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL
			+ "." + "passwordResetLink.secure";
	private static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";

	private static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";

	@Override
	protected List<String> neededSettingKeys() {
		final List<String> needed = new ArrayList<String>(
				super.neededSettingKeys());
		needed.add(SETTING_KEY_VERIFICATION_LINK_SECURE);
		needed.add(SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		needed.add(SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
		return needed;
	}

	public static MyUsernamePasswordAuthProvider getProvider() {
		return (MyUsernamePasswordAuthProvider) PlayAuthenticate
				.getProvider(UsernamePasswordAuthProvider.PROVIDER_KEY);
	}

	public static class MyIdentity {

		public MyIdentity() {
		}

		public MyIdentity(final String email) {
			this.email = email;
		}

		@Required
		@Email
		public String email;

	}

	public static class MyLogin extends MyIdentity
			implements
			com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {

		@Required
		@MinLength(5)
		public String password;

		@Override
		public String getEmail() {
			return email;
		}

		@Override
		public String getPassword() {
			return password;
		}
	}

	public static class MySignup extends MyLogin {

		@Required
		@MinLength(5)
		public String repeatPassword;

		@Required
		public String name;

		public String validate() {
			if (password == null || !password.equals(repeatPassword)) {
				return Messages
						.get("playauthenticate.password.signup.error.passwords_not_same");
			}
			return null;
		}
	}

	public static final Form<MySignup> SIGNUP_FORM = form(MySignup.class);
	public static final Form<MyLogin> LOGIN_FORM = form(MyLogin.class);

	@Inject
	public MyUsernamePasswordAuthProvider(Application app) {
		super(app);
	}

	protected Form<MySignup> getSignupForm() {
		return SIGNUP_FORM;
	}

	protected Form<MyLogin> getLoginForm() {
		return LOGIN_FORM;
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.SignupResult 
		signupUser(final MyUsernamePasswordAuthUser authUser) {
		
		final SysUser u = SysUser.findByUsernamePasswordIdentity(authUser);
		if (u != null) {
			if (u.getEmailValidated()) {
				// This user exists, has its email validated and is active
				return SignupResult.USER_EXISTS;
			} else {
				// this user exists, is active but has not yet validated its
				// email
				return SignupResult.USER_EXISTS_UNVERIFIED;
			}
		}
		// The user either does not exist or is inactive - create a new one
		String userUri = UserManagement.getUriByEmail(authUser.getEmail());
		
		final SysUser newUser = SysUser.create(authUser, userUri);
		// Usually the email should be verified before allowing login, however
		// if you return
		// return SignupResult.USER_CREATED;
		// then the user gets logged in directly
		if (newUser.getEmailValidated() == true) {
			return SignupResult.USER_CREATED;
		}
		return SignupResult.USER_CREATED_UNVERIFIED;
	}

	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.LoginResult loginUser(
			final MyLoginUsernamePasswordAuthUser authUser) {
		final SysUser u = SysUser.findByUsernamePasswordIdentity(authUser);
		if (u == null) {
			System.out.println("User not found!");
			return LoginResult.NOT_FOUND;
		} else {
			if (!u.getEmailValidated()) {
				System.out.println("User unverified!");
				return LoginResult.USER_UNVERIFIED;
			} else {
				for (final LinkedAccount acc : u.getLinkedAccounts()) {
					if (getKey().equals(acc.providerKey)) {
						if (authUser.checkPassword(acc.providerUserId,
								authUser.getPassword())) {
							// Password was correct
							System.out.println("User logged in!");
							return LoginResult.USER_LOGGED_IN;
						} else {
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							System.out.println("User password invalid!");
							return LoginResult.WRONG_PASSWORD;
						}
					}
				}
				return LoginResult.WRONG_PASSWORD;
			}
		}
	}

	@Override
	protected Call userExists(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.exists();
	}

	@Override
	protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
		return routes.Signup.unverified();
	}

	@Override
	protected MyUsernamePasswordAuthUser buildSignupAuthUser(
			final MySignup signup, final Context ctx) {
		return new MyUsernamePasswordAuthUser(signup);
	}

	@Override
	protected MyLoginUsernamePasswordAuthUser buildLoginAuthUser(
			final MyLogin login, final Context ctx) {
		return new MyLoginUsernamePasswordAuthUser(login.getPassword(),
				login.getEmail());
	}
	
	@Override
	protected MyLoginUsernamePasswordAuthUser transformAuthUser(final MyUsernamePasswordAuthUser authUser, final Context context) {
		return new MyLoginUsernamePasswordAuthUser(authUser.getEmail());
	}

	@Override
	protected String getVerifyEmailMailingSubject(
			final MyUsernamePasswordAuthUser user, final Context ctx) {
		return Messages.get("playauthenticate.password.verify_signup.subject");
	}

	@Override
	protected String onLoginUserNotFound(final Context context) {
		context.flash()
				.put(org.hadatac.console.controllers.AuthApplication.FLASH_ERROR_KEY,
						Messages.get("playauthenticate.password.login.unknown_user_or_pw"));
		return super.onLoginUserNotFound(context);
	}

	@Override
	protected Body getVerifyEmailMailingBody(final String token,
			final MyUsernamePasswordAuthUser user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		final String url = routes.Signup.verify(token).absoluteURL(
				isSecure, Play.application().configuration().getString("hadatac.console.base_url"));

		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"org.hadatac.console.views.html.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());
		final String text = getEmailTemplate(
				"org.hadatac.console.views.txt.account.signup.email.verify_email", langCode, url,
				token, user.getName(), user.getEmail());

		return new Body(text, html);
	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

	@Override
	protected String generateVerificationRecord(
			final MyUsernamePasswordAuthUser user) {
		return generateVerificationRecordSolr(user);
	}
	
	protected String generateVerificationRecordSolr(
			final MyUsernamePasswordAuthUser user) {
		return generateVerificationRecord(SysUser.findByAuthUserIdentitySolr(user));
	}

	protected String generateVerificationRecord(final SysUser user) {
		final String token = generateToken();
		// Do database actions, etc.
		TokenAction.create(Type.EMAIL_VERIFICATION, token, user);
		return token;
	}

	protected String generatePasswordResetRecord(final SysUser u) {
		final String token = generateToken();
		TokenAction.create(Type.PASSWORD_RESET, token, u);
		return token;
	}

	protected String getPasswordResetMailingSubject(final SysUser user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.reset_email.subject");
	}

	protected Body getPasswordResetMailingBody(final String token,
			final SysUser user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_PASSWORD_RESET_LINK_SECURE);
		final String url = routes.Signup.resetPassword(token).absoluteURL(
				isSecure, Play.application().configuration().getString("hadatac.console.base_url"));

		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"org.hadatac.console.views.html.account.email.password_reset", langCode, url,
				token, user.getName(), user.getEmail());
		final String text = getEmailTemplate(
				"org.hadatac.console.views.txt.account.email.password_reset", langCode, url, token,
				user.getName(), user.getEmail());

		return new Body(text, html);
	}
	
	protected Body getInvitationMailingBody(String user_name, String user_email, final Context ctx) {
		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		final String url = routes.AuthApplication.signup().absoluteURL(
				isSecure, Play.application().configuration().getString("hadatac.console.base_url"));

		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"org.hadatac.console.views.html.account.signup.email.invitation_email", langCode, url,
				"", user_name, user_email);
		final String text = getEmailTemplate(
				"org.hadatac.console.views.txt.account.signup.email.invitation_email", langCode, url, 
				"", user_name, user_email);

		return new Body(text, html);
	}

	public void sendPasswordResetMailing(final SysUser user, final Context ctx) {
		final String token = generatePasswordResetRecord(user);
		final String subject = getPasswordResetMailingSubject(user, ctx);
		final Body body = getPasswordResetMailingBody(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}
	
	public void sendInvitationMailing(String user_name, String user_email, final Context ctx) {
		final String subject = "Invitation from HADatAc Team";
		final Body body = getInvitationMailingBody(user_name, user_email, ctx);
		sendMail(subject, body, user_email);
	}

	public boolean isLoginAfterPasswordReset() {
		return getConfiguration().getBoolean(
				SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET);
	}

	protected String getVerifyEmailMailingSubjectAfterSignup(final SysUser user,
			final Context ctx) {
		return Messages.get("playauthenticate.password.verify_email.subject");
	}

	protected String getEmailTemplate(final String template,
			final String langCode, final String url, final String token,
			final String name, final String email) {
		Class<?> cls = null;
		String ret = null;
		try {
			cls = Class.forName(template + "_" + langCode);
		} catch (ClassNotFoundException e) {
			Logger.warn("Template: '"
					+ template
					+ "_"
					+ langCode
					+ "' was not found! Trying to use English fallback template instead.");
		}
		if (cls == null) {
			try {
				cls = Class.forName(template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE);
			} catch (ClassNotFoundException e) {
				Logger.error("Fallback template: '" + template + "_"
						+ EMAIL_TEMPLATE_FALLBACK_LANGUAGE
						+ "' was not found either!");
			}
		}
		if (cls != null) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod("render", String.class,
						String.class, String.class, String.class);
				ret = htmlRender.invoke(null, url, token, name, email)
						.toString();

			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	protected Body getVerifyEmailMailingBodyAfterSignup(final String token,
			final SysUser user, final Context ctx) {

		final boolean isSecure = getConfiguration().getBoolean(
				SETTING_KEY_VERIFICATION_LINK_SECURE);
		final String url = routes.Signup.verify(token).absoluteURL(
				isSecure, Play.application().configuration().getString("hadatac.console.base_url"));

		final Lang lang = Lang.preferred(ctx.request().acceptLanguages());
		final String langCode = lang.code();

		final String html = getEmailTemplate(
				"org.hadatac.console.views.html.account.email.verify_email", langCode, url, token,
				user.getName(), user.getEmail());
		final String text = getEmailTemplate(
				"org.hadatac.console.views.txt.account.email.verify_email", langCode, url, token,
				user.getName(), user.getEmail());

		return new Body(text, html);
	}

	public void sendVerifyEmailMailingAfterSignup(final SysUser user,
			final Context ctx) {

		final String subject = getVerifyEmailMailingSubjectAfterSignup(user, ctx);
		final String token = generateVerificationRecord(user);
		final Body body = getVerifyEmailMailingBodyAfterSignup(token, user, ctx);
		sendMail(subject, body, getEmailName(user));
	}

	private String getEmailName(final SysUser user) {
		return getEmailName(user.getEmail(), user.getName());
	}
}
