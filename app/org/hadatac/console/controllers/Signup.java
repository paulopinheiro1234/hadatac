package org.hadatac.console.controllers;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import com.typesafe.config.ConfigFactory;
import module.SecurityModule;
import org.hadatac.Constants;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.LinkedAccount;
import org.hadatac.console.models.SignUp;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.TokenAction;
import org.hadatac.console.providers.*;
import org.hadatac.console.views.html.account.errorLogin;
import org.hadatac.console.views.html.account.signup.no_token_or_invalid;
import org.hadatac.console.views.html.account.signup.password_forgot;
import org.hadatac.console.views.html.account.signup.password_reset;
import org.hadatac.console.views.html.loginForm;
import org.hadatac.console.views.html.portal;
import org.hadatac.console.views.html.protectedIndex;
import org.hadatac.console.views.html.triplestore.notRegistered;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlaySessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.libs.mailer.MailerClient;
import play.i18n.MessagesApi;
import play.data.validation.Constraints;
import play.mvc.Http;
import play.mvc.Result;
import play.data.Form;
import play.data.FormFactory;
import org.hadatac.console.models.TokenAction.Type;

import javax.inject.Inject;

import java.util.*;

import static play.libs.Scala.asScala;
import static play.mvc.Results.*;
import static play.shaded.ahc.io.netty.util.internal.SystemPropertyUtil.get;


public class Signup {
    @Inject
    private Application application;
    @Inject
    private PlaySessionStore playSessionStore;

    @Constraints.Validate
    public static class PasswordReset extends Account.PasswordChange implements Constraints.Validatable<String> {

        public PasswordReset(MessagesApi messagesApi) {
            super(messagesApi);
        }

        public PasswordReset(final String token, MessagesApi messagesApi) {
            super(messagesApi);
            this.token = token;
        }

        public String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        @Override
        public String validate() {
            return null;
        }
    }

    private final Form<PasswordReset> PASSWORD_RESET_FORM;

    private final Form<MyUsernamePasswordAuthProvider.MyIdentity> FORGOT_PASSWORD_FORM;
    private final MyUsernamePasswordAuthProvider userPaswAuthProvider;
    private final MessagesApi msg;
    private final UserProvider userProvider;
    private final Form<MyUsernamePasswordAuthProvider> form;
    MyService myService;
    private MessagesApi messagesApi;
    private final List<SignUp> signUps;

    @Inject
    MailerClient mailerClient;

    //TODO : fix this
    @Inject
    public Signup(final UserProvider userProvider,
                  final MyUsernamePasswordAuthProvider userPaswAuthProvider,
                  final FormFactory formFactory, final MessagesApi msg, MyService myService) {
//        this.auth = auth;
        this.userProvider = userProvider;
        this.userPaswAuthProvider = userPaswAuthProvider;
        this.PASSWORD_RESET_FORM = formFactory.form(PasswordReset.class);
        this.FORGOT_PASSWORD_FORM = formFactory.form(MyUsernamePasswordAuthProvider.MyIdentity.class);
        this.msg = msg;
        this.myService = myService;
        this.form = formFactory.form(MyUsernamePasswordAuthProvider.class);
        this.signUps = com.google.common.collect.Lists.newArrayList(
                new SignUp("Data 1", "a", "a", "a"),
                new SignUp("Data 2", "b", "b", "b"),
                new SignUp("Data 3", "c", "c", "c")
        );
    }

    //TODO : fix this
    public Result forgotPassword(Http.Request request) {
        Form<MyUsernamePasswordAuthProvider.MyIdentity> form = FORGOT_PASSWORD_FORM;
        return ok(password_forgot.render(form, msg.preferred(request))).withHeader("Cache-Control", "no-cache");
    }

    //TODO : fix this
    public Result doResetPassword(String token, Http.Request request) {
        final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM
                .bindFromRequest(request);
        if (filledForm.hasErrors()) {
            return badRequest(password_reset.render(filledForm, "", msg.preferred(request))).withHeader("Cache-Control", "no-cache");
        } else {
            final String newPassword = filledForm.get().password;

            final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
            if (ta == null) {
                return badRequest(no_token_or_invalid.render());
            }
            final SysUser u = ta.targetUser;
            try {
                // Pass true for the second parameter if you want to
                // automatically create a password and the exception never to
                // happen
                u.resetPassword(new MyUsernamePasswordAuthUser(newPassword, u.getEmail(), u.getName()), false);
            } catch (final RuntimeException re) {
                System.out.println(re + "\n" + re.getStackTrace());
                return redirect(routes.Application.loginForm()).flashing(AuthApplication.FLASH_MESSAGE_KEY,
                        this.msg.preferred(request).at("authenticate.reset_password.message.no_password_account"));
            }
//            final boolean login = this.userPaswAuthProvider.isLoginAfterPasswordReset();
//            if (login) {
//                // automatically log in
//                flash(AuthApplication.FLASH_MESSAGE_KEY,
//                        this.msg.preferred(request).at("playauthenticate.reset_password.message.success.auto_login"));
//
//                return this.auth.loginAndRedirect(ctx(),
//                        new MyLoginUsernamePasswordAuthUser(u.getEmail()));
//            } else {
//                // send the user to the login page
//                return redirect(routes.Application.loginForm()).flashing(AuthApplication.FLASH_MESSAGE_KEY,
//                        this.msg.preferred(request).at("playauthenticate.reset_password.message.success.manual_login"));
//            }
            return redirect(routes.Application.loginForm());
        }
    }
    /**
     * Returns a token object if valid, null if not
     *
     * @param token
     * @param type
     * @return
     */
    private static TokenAction tokenIsValid(final String token, final Type type) {
        TokenAction ret = null;
        if (token != null && !token.trim().isEmpty()) {
            final TokenAction ta = TokenAction.findByToken(token, type);
            if (ta != null && ta.isValid()) {
                ret = ta;
            }
        }

        return ret;
    }

    //todo: fix this
    public Result doForgotPassword(Http.Request request) {
        final Form<MyUsernamePasswordAuthProvider.MyIdentity> filledForm = FORGOT_PASSWORD_FORM
                .bindFromRequest(request);
        if (filledForm.hasErrors()) {
            // User did not fill in his/her email
            return badRequest(password_forgot.render(filledForm, msg.preferred(request))).withHeader("Cache-Control", "no-cache");
        } else {
            // The email address given *BY AN UNKNWON PERSON* to the form - we
            // should find out if we actually have a user with this email
            // address and whether password login is enabled for him/her. Also
            // only send if the email address of the user has been verified.
            final String email = filledForm.get().email;

            // We don't want to expose whether a given email address is signed
            // up, so just say an email has been sent, even though it might not
            // be true - that's protecting our user privacy.
//            flash(Constants.FLASH_MESSAGE_KEY,
//                    this.msg.preferred(request).at(
//                            "Instructions on how to reset your password have been sent to {0}.",
//                            email));

            final SysUser user = SysUser.findByEmail(email);
            if (user != null) {
                // yep, we have a user with this email that is active - we do
                // not know if the user owning that account has requested this
                // reset, though.
                final MyUsernamePasswordAuthProvider provider = this.userPaswAuthProvider;
                // User exists
                if (user.getEmailValidated()) {
                    provider.sendPasswordResetMailing(user, request);
                    // In case you actually want to let (the unknown person)
                    // know whether a user was found/an email was sent, use,
                    // change the flash message
                } else {
                    // We need to change the message here, otherwise the user
                    // does not understand whats going on - we should not verify
                    // with the password reset, as a "bad" user could then sign
                    // up with a fake email via OAuth and get it verified by an
                    // a unsuspecting user that clicks the link.
                    // You might want to re-send the verification email here...
                    System.out.println("I AM HERE: user not verfyied");
                    provider.sendVerifyEmailMailingAfterSignup(user, request);
                    return redirect(routes.Portal.index()).flashing(Constants.FLASH_MESSAGE_KEY,
                            "Your account has not been verified, yet. An e-mail including instructions on how to verify it has been sent out. Retry resetting your password afterwards.");

                }
            }

            return redirect(routes.Portal.index());
        }
    }

    public Result resetPassword(final String token, Http.Request request) {
        final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
        final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM.fill(new PasswordReset(token, this.msg)).bindFromRequest(request);
        if (ta == null) {
            return badRequest(no_token_or_invalid.render());
        }

        return ok(password_reset.render(filledForm,token,msg.preferred(request))).withHeader("Cache-Control", "no-cache");
    }

    public Result verify(final String token, Http.Request request) {
        final TokenAction ta = tokenIsValid(token, Type.EMAIL_VERIFICATION);
        if (ta == null) {
            return badRequest(no_token_or_invalid.render());
        }
        final String email = ta.targetUser.getEmail();
        SysUser.verify(ta.targetUser);
        if (this.userProvider.getUser(application.getUserEmail(request)) != null) {
            return redirect(routes.Application.formIndex()).withHeader("Cache-Control", "no-cache").flashing(Constants.FLASH_MESSAGE_KEY,"E-mail address "+email+" successfully verified.");
        } else {
            return redirect(routes.Application.loginForm())
                    .withHeader("Cache-Control", "no-cache").flashing(Constants.FLASH_MESSAGE_KEY,"E-mail address "+email+" successfully verified.");
        }
    }

    @SubjectNotPresent
    public Result createUser(Http.Request request) throws TechnicalException {
        System.out.println("inside create user");
        final Form<MyUsernamePasswordAuthProvider> boundForm = form.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return redirect(org.hadatac.console.controllers.routes.WidgetController.listWidgets())
                    .flashing("error", String.valueOf(boundForm.errors()));
        }
        if (SysUser.existsSolr()) { // only check for pre-registration if it is not the first user signing up
            if (!UserManagement.isPreRegistered(boundForm.get().getEmail())) {
                return ok(notRegistered.render());
                }
            }
            MyUsernamePasswordAuthProvider data = boundForm.get();
             return settingUpAccount(data, false);
    }

    public Result settingUpAccount (MyUsernamePasswordAuthProvider data, Boolean redirectedUser){
        if (data.validate()!=null){
            return redirect(org.hadatac.console.controllers.routes.WidgetController.listWidgets())
                    .flashing("error",data.validate());
        }
        signUps.add(new SignUp(data.getName(), data.getEmail(), data.getPassword(), data.getRepeatPassword()));
        try {
            LinkedAccount linkedAccount = new LinkedAccount();
            linkedAccount.providerKey ="password"; //TODO : generalize later
            linkedAccount.providerUserId=data.getHashedPassword();
            String userUri = UserManagement.getUriByEmail(data.getEmail());
            final SysUser newUser = SysUser.create(data, userUri, linkedAccount);
            System.out.println("commit done");
        } catch (Exception e) {
            System.out.println("[ERROR] User.getAuthUserFindSolr - Exception message: " + e.getMessage());
        }
        if(!redirectedUser)
            return redirect(org.hadatac.console.controllers.routes.Portal.index());
        return ok("Account set up is complete");
    }
    //For users redirected to Hadatac -
    // New users Signup and then login
    // Existing users Login
    public Result checkUserExists(Http.Request request) throws TechnicalException {
        final Form<MyUsernamePasswordAuthProvider> formData = form.bindFromRequest(request);
        if ( formData != null && !formData.hasErrors()) {
            if (SysUser.findByEmail(formData.get().getEmail()) != null && !formData.get().getEmail().isEmpty()) {
                //Login user
                System.out.println("Redirected from HHEAR Portal, user exists - logging in");
                SimpleTestUsernamePasswordAuthenticator test = new SimpleTestUsernamePasswordAuthenticator();
                final PlayWebContext context = new PlayWebContext(request, playSessionStore);
                test.validate(new UsernamePasswordCredentials(formData.get().getEmail(), formData.get().getPassword()), context);
                SysUser user = SysUser.findByEmail(formData.get().getEmail());
                application.formIndex(request,user);
                return ok ("/protected/index.html/"+user.getEmail());
            } else {
                System.out.println("Redirected from HHEAR Portal, user Does not exist, Signing up");
                MyUsernamePasswordAuthProvider data = formData.get();
                settingUpAccount(data,true);
                return ok("user does not exist"); //TODO: This needs to change to login
            }
        }
        return badRequest("what happened?");
    }
}