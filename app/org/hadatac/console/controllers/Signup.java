package org.hadatac.console.controllers;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import org.hadatac.Constants;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.LinkedAccount;
import org.hadatac.console.models.SignUp;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.TokenAction;
import org.hadatac.console.providers.MyService;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;
import org.hadatac.console.providers.UserProvider;
import org.hadatac.console.views.html.account.signup.no_token_or_invalid;
import org.hadatac.console.views.html.account.signup.password_forgot;
import org.hadatac.console.views.html.account.signup.password_reset;
import org.pac4j.core.exception.TechnicalException;
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

import java.util.List;

import static play.libs.Scala.asScala;
import static play.mvc.Results.*;


public class Signup {
    @Inject
    private Application application;

    @Constraints.Validate
    public static class PasswordReset extends Account.PasswordChange implements Constraints.Validatable<String> {

        public PasswordReset() {}

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

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;

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
                new SignUp("Data 1", "a", "a","a"),
                new SignUp("Data 2", "b","b","b"),
                new SignUp("Data 3", "c", "c","c")
        );
    }

    //TODO : fix this
    public Result forgotPassword(Http.Request request) {
        Form<MyUsernamePasswordAuthProvider.MyIdentity> form = FORGOT_PASSWORD_FORM;
        return ok(password_forgot.render(form,msg.preferred(request))).withHeader("Cache-Control", "no-cache");
    }

    //TODO : fix this
    public Result doResetPassword(Http.Request request) {
        final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM
                .bindFromRequest(request);
        if (filledForm.hasErrors()) {
            return badRequest(password_reset.render(filledForm,msg.preferred(request))).withHeader("Cache-Control", "no-cache");
        } else {
            final String token = filledForm.get().token;
            final String newPassword = filledForm.get().password;

            final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
            if (ta == null) {
                return badRequest(no_token_or_invalid.render());
            }
            final SysUser u = ta.targetUser;
//            try {
//                // Pass true for the second parameter if you want to
//                // automatically create a password and the exception never to
//                // happen
//                u.resetPassword(new MyUsernamePasswordAuthUser(newPassword),
//                        false);
//            } catch (final RuntimeException re) {
//                flash(AuthApplication.FLASH_MESSAGE_KEY,
//                        this.msg.preferred(request()).at("playauthenticate.reset_password.message.no_password_account"));
//            }
//            final boolean login = this.userPaswAuthProvider.isLoginAfterPasswordReset();
//            if (login) {
//                // automatically log in
//                flash(AuthApplication.FLASH_MESSAGE_KEY,
//                        this.msg.preferred(request()).at("playauthenticate.reset_password.message.success.auto_login"));
//
//                return this.auth.loginAndRedirect(ctx(),
//                        new MyLoginUsernamePasswordAuthUser(u.getEmail()));
//            } else {
//                // send the user to the login page
//                flash(AuthApplication.FLASH_MESSAGE_KEY,
//                        this.msg.preferred(request()).at("playauthenticate.reset_password.message.success.manual_login"));
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
                    provider.sendVerifyEmailMailingAfterSignup(user, request);
                    return redirect(routes.Portal.index()).flashing(Constants.FLASH_MESSAGE_KEY,
                           "Your account has not been verified, yet. An e-mail including instructions on how to verify it has been sent out. Retry resetting your password afterwards.");

                }
            }

            return redirect(routes.Portal.index());
        }
    }

    //TODO :test this
    public Result resetPassword(final String token, Http.Request request) {
        final TokenAction ta = tokenIsValid(token, Type.PASSWORD_RESET);
        final Form<PasswordReset> filledForm = PASSWORD_RESET_FORM.fill(new PasswordReset(token, this.msg)).bindFromRequest(request);
        if (ta == null) {
            return badRequest(no_token_or_invalid.render());
        }

        return ok(password_reset.render(filledForm,msg.preferred(request))).withHeader("Cache-Control", "no-cache");
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
        final Form<MyUsernamePasswordAuthProvider> boundForm = form.bindFromRequest(request);
//        if (SysUser.existsSolr()) { // only check for pre-registration if it is not the first user signing up
//            if (!UserManagement.isPreRegistered(boundForm.get().getEmail())) {
//                return ok(notRegistered.render());
//            }
//        }

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest(org.hadatac.console.views.html.listWidgets.render(asScala(signUps), boundForm, request, messagesApi.preferred(request)));
        } else {
            MyUsernamePasswordAuthProvider data = boundForm.get();
            if (data.validate()!=null){
//                messagesApi.preferred(request).at("Your e-mail has already been validated.");
                return redirect(org.hadatac.console.controllers.routes.WidgetController.listWidgets())
                        .flashing("error",data.validate());
            }
            signUps.add(new SignUp(data.getName(), data.getEmail(), data.getPassword(), data.getRepeatPassword()));
//            //Adding to DB
//            SolrClient solrClient = new HttpSolrClient.Builder(
//                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.AUTHENTICATE_USERS)).build();
//            String query = "active_bool:true";
//            SolrQuery solrQuery = new SolrQuery(query);
//            List<SysUser> users = new ArrayList<SysUser>();
//
            try {
//                QueryResponse queryResponse = solrClient.query(solrQuery);
////                solrClient.close();
//                SolrDocumentList list = queryResponse.getResults();
//                Iterator<SolrDocument> i = list.iterator();
//
//                while (i.hasNext()) {
//                    System.out.println("User at i :"+i.next());
//                    if(i.next().containsValue(data.getEmail()))
//                    System.out.println("Email already validated");
////				SysUser user = SysUser.convertSolrDocumentToUser(i.next());
////                System.out.println("Users:"+user);
////				users.add(user);
//	    			}
//            SolrInputDocument newUser = new SolrInputDocument();
//            newUser.addField( "id_str", UUID.randomUUID().toString());
//            newUser.addField( "email", data.getEmail());
//            newUser.addField( "name_str", data.getName());
//            newUser.addField("active_bool",true);
//            newUser.addField("email_validated_bool", true);
//            solrClient.add(newUser);
//            solrClient.commit();
                LinkedAccount linkedAccount = new LinkedAccount();
                linkedAccount.providerKey ="password"; //TODO : generalize later
                linkedAccount.providerUserId=data.getHashedPassword();
                String userUri = UserManagement.getUriByEmail(data.getEmail()); //TODO: fix it
                final SysUser newUser = SysUser.create(data, userUri, linkedAccount);
                System.out.println("commit done");
//            solrClient.close();
            } catch (Exception e) {
                System.out.println("[ERROR] User.getAuthUserFindSolr - Exception message: " + e.getMessage());
            }

            return redirect(org.hadatac.console.controllers.routes.Portal.index());
            //routes.WidgetController.listWidgets())
            //.flashing("info", "User added!");
        }
    }
}
