package org.hadatac.console.controllers;

import org.hadatac.Constants;
//import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;
import org.hadatac.console.providers.MyUsernamePasswordAuthUser;
import org.hadatac.console.providers.UserProvider;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import org.hadatac.console.views.html.account.*;

import java.util.Locale;

import javax.inject.Inject;

public class Account extends Controller {
    @Inject
    private Application application;

    public static class Accept {

        @Required
        @NonEmpty
        public Boolean accept;

        public Boolean getAccept() {
            return accept;
        }

        public void setAccept(Boolean accept) {
            this.accept = accept;
        }

    }

    public static class PasswordChange {
        @MinLength(5)
        @Required
        public String password;

        @MinLength(5)
        @Required
        public String repeatPassword;

        private MessagesApi messagesApi;

        public PasswordChange() {}

        @Inject
        public PasswordChange(MessagesApi messagesApi) {
            this.messagesApi = messagesApi;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRepeatPassword() {
            return repeatPassword;
        }

        public void setRepeatPassword(String repeatPassword) {
            this.repeatPassword = repeatPassword;
        }

        public String validate(Http.Request request) {
            if (password == null || !password.equals(repeatPassword)) {
                return this.messagesApi.preferred(request).at("Passwords do not match.");
            }
            return null;
        }
    }

    private final Form<Accept> ACCEPT_FORM;
    private final Form<Account.PasswordChange> PASSWORD_CHANGE_FORM;

//    private final PlayAuthenticate auth;
    private final UserProvider userProvider;
    private final MyUsernamePasswordAuthProvider myUsrPaswProvider;

    private final MessagesApi msg;

    //TODO : fix this
    @Inject
    public Account(final MyUsernamePasswordAuthProvider myUsrPaswProvider,
                   final FormFactory formFactory, final UserProvider userProvider, final MessagesApi msg) {
//        this.auth = auth;
        this.userProvider = userProvider;
        this.myUsrPaswProvider = myUsrPaswProvider;

        this.ACCEPT_FORM = formFactory.form(Accept.class);
        this.PASSWORD_CHANGE_FORM = formFactory.form(Account.PasswordChange.class);

        this.msg = msg;
    }
//todo : fix this
//    @SubjectPresent
//    public Result link() {
//        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
//        return ok(link.render(this.auth));
//    }
//
    @Secure(authorizers =Constants.DATA_OWNER_ROLE)
    public Result verifyEmail(Http.Request request) {
        final SysUser user = this.userProvider.getUser(application.getUserEmail(request));
        if (user.getEmailValidated()) {
            // E-Mail has been validated already
            redirect(routes.AuthApplication.profile(user.getEmail())).flashing(Constants.FLASH_MESSAGE_KEY,"Your e-mail has already been validated.");
        } else if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            this.myUsrPaswProvider.sendVerifyEmailMailingAfterSignup(user, request);
            redirect(routes.AuthApplication.profile(user.getEmail())).flashing(Constants.FLASH_MESSAGE_KEY,"Instructions on how to verify your e-mail address have been sent to"+ user.getEmail());

        } else {
            redirect(routes.AuthApplication.profile(user.getEmail())).flashing(Constants.FLASH_MESSAGE_KEY,"You need to set an e-mail address first."+ user.getEmail());
        }
        return redirect(routes.AuthApplication.profile(user.getEmail()));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result changePassword(Http.Request request) {
        final SysUser u = this.userProvider.getUser(application.getUserEmail(request));

        if (!u.getEmailValidated()) {
            return ok(unverified.render(application.getUserEmail(request))).withHeader("Cache-Control", "no-cache");
        } else {
            return ok(password_change.render(PASSWORD_CHANGE_FORM,application.getUserEmail(request),msg.preferred(request))).withHeader("Cache-Control", "no-cache");
        }
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result doChangePassword(Http.Request request) {
        final Form<Account.PasswordChange> filledForm = PASSWORD_CHANGE_FORM.bindFromRequest(request);
        if (filledForm.hasErrors()) {
            // User did not select whether to link or not linkx
            return badRequest(password_change.render(filledForm,application.getUserEmail(request),msg.preferred(request)));
        } else {
            final SysUser user = this.userProvider.getUser(application.getUserEmail(request));
            final String newPassword = filledForm.get().password;
            /* - This code sets the URI of the user after change password as a way to set a uri
             * that is missing from a previous registration. - */
            user.setUri(UserManagement.getUriByEmail(user.getEmail()));
            user.save();
            /* ---- */
            user.changePassword(new MyUsernamePasswordAuthUser(newPassword), true);
            return redirect(routes.AuthApplication.profile(user.getEmail()))
                    .withHeader("Cache-Control", "no-cache")
                    .flashing(Constants.FLASH_MESSAGE_KEY,"Password has been changed successfully.");
        }
    }
//
//    @SubjectPresent
//    public Result askLink() {
//        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
//        final AuthUser u = this.auth.getLinkUser(session());
//        if (u == null) {
//            // account to link could not be found, silently redirect to login
//            return redirect(routes.AuthApplication.index());
//        }
//        return ok(ask_link.render(ACCEPT_FORM, u));
//    }
//
//    @SubjectPresent
//    public Result doLink() {
//        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
//        final AuthUser u = this.auth.getLinkUser(session());
//        if (u == null) {
//            // account to link could not be found, silently redirect to login
//            return redirect(routes.AuthApplication.index());
//        }
//
//        final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
//        if (filledForm.hasErrors()) {
//            // User did not select whether to link or not link
//            return badRequest(ask_link.render(filledForm, u));
//        } else {
//            // User made a choice :)
//            final boolean link = filledForm.get().accept;
//            if (link) {
//                flash(AuthApplication.FLASH_MESSAGE_KEY,
//                        this.msg.preferred(request()).at("playauthenticate.accounts.link.success"));
//            }
//            return this.auth.link(ctx(), link);
//        }
//    }
//
//    @SubjectPresent
//    public Result askMerge() {
//        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
//        // this is the currently logged in user
//        final AuthUser aUser = this.auth.getUser(session());
//
//        // this is the user that was selected for a login
//        final AuthUser bUser = this.auth.getMergeUser(session());
//        if (bUser == null) {
//            // user to merge with could not be found, silently redirect to login
//            return redirect(routes.AuthApplication.index());
//        }
//
//        // You could also get the local user object here via
//        // User.findByAuthUserIdentity(newUser)
//        return ok(ask_merge.render(ACCEPT_FORM, aUser, bUser));
//    }
//
//    @SubjectPresent
//    public Result doMerge() {
//        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
//        // this is the currently logged in user
//        final AuthUser aUser = this.auth.getUser(session());
//
//        // this is the user that was selected for a login
//        final AuthUser bUser = this.auth.getMergeUser(session());
//        if (bUser == null) {
//            // user to merge with could not be found, silently redirect to login
//            return redirect(routes.AuthApplication.index());
//        }
//
//        final Form<Accept> filledForm = ACCEPT_FORM.bindFromRequest();
//        if (filledForm.hasErrors()) {
//            // User did not select whether to merge or not merge
//            return badRequest(ask_merge.render(filledForm, aUser, bUser));
//        } else {
//            // User made a choice :)
//            final boolean merge = filledForm.get().accept;
//            if (merge) {
//                flash(AuthApplication.FLASH_MESSAGE_KEY,
//                        this.msg.preferred(request()).at("playauthenticate.accounts.merge.success"));
//            }
//            return this.auth.merge(ctx(), merge);
//        }
//    }

}
