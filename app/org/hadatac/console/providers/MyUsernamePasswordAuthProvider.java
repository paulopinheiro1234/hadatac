package org.hadatac.console.providers;

import javax.inject.Inject;

import akka.actor.Cancellable;
import com.feth.play.module.mail.IMailer;
import com.feth.play.module.mail.Mailer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.hadatac.Constants;
import org.hadatac.console.controllers.routes;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.TokenAction;
import org.springframework.security.crypto.bcrypt.BCrypt;
import play.Logger;
import play.api.i18n.Messages;
import play.api.i18n.MessagesApi;
import play.api.libs.mailer.MailerClient;
import play.api.mvc.RequestHeader;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Http;
import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.mail.Mailer.MailerFactory;
import play.i18n.Lang;
import scala.collection.Seq;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

import static org.hadatac.Constants.EMAIL_TEMPLATE_FALLBACK_LANGUAGE;

/**
 * A form processing DTO that maps to the widget form.
 *
 * Using a class specifically for form binding reduces the chances
 * of a parameter tampering attack and makes code clearer, because
 * you can define constraints against the class.
 */
public class MyUsernamePasswordAuthProvider implements MyAuthUserIdentity{

    @Constraints.Required
    private String name;
    @Constraints.Required
    @Constraints.Email
    private String email;
    @Constraints.Required
    @Constraints.MinLength(5)
    private String password;
    private String repeatPassword;

    private String source;
    private String studyPageRef;
    private String studyId;
    private String studyIds;

    private MessagesApi messagesApi;
    private Config config;
    MyService myService;

    public static final String PROVIDER_KEY = "password";

    protected static final String SETTING_KEY_MAIL = "mail";

    private static final String SETTING_KEY_MAIL_FROM_EMAIL = Mailer.SettingKeys.FROM_EMAIL;

    private static final String SETTING_KEY_MAIL_DELAY = Mailer.SettingKeys.DELAY;

    private static final String SETTING_KEY_MAIL_FROM = Mailer.SettingKeys.FROM;

    @Inject
    public MyUsernamePasswordAuthProvider(MessagesApi messagesApi, MyService myService) {
        this.messagesApi = messagesApi;
        this.myService = myService;
    }

    public MyUsernamePasswordAuthProvider() {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getProvider() {
        return "password"; //TODO : generalize this
    }

    public static class MyIdentity {

        public MyIdentity() {
        }

        public MyIdentity(final String email) {
            this.email = email;
        }

        @Constraints.Required
        @Constraints.Email
        public String email;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getSource() { return source; }

    public void setSource(String source) {this.source = source; }

    public String getStudyPageRef() { return studyPageRef; }

    public void setStudyPageRef(String studyPageRef) {this.studyPageRef = studyPageRef;}

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getStudyIds() {
        return studyIds;
    }

    public void setStudyIds(String studyIds) {
        this.studyIds = studyIds;
    }

    public String validate() {
        if (password == null || !password.equals(repeatPassword)) {
            return "Passwords do not match";
        }
        if (name ==null || email== null || password ==null|| repeatPassword==null){
            return "All fields are mandatory";
        }
        return null;
    }

    public String getHashedPassword() {
        return createPassword(this.password);
    }

    public String getHashedPassword(String password) {
        return createPassword(password);
    }

    protected String createPassword(final String clearString) {
        return BCrypt.hashpw(clearString, BCrypt.gensalt());
    }

    public void sendPasswordResetMailing(final SysUser user, final Http.Request request) {
        final String token = generatePasswordResetRecord(user);
        final String subject = getPasswordResetMailingSubject(user, request);
        final Body body = getPasswordResetMailingBody(token, user, request);
        myService.sendMail(subject, body, getEmailName(user));
    }

    public void sendInvitationMailing(String user_name, String user_email, final Http.Request request) {
        final String subject = ConfigFactory.load().getString("hadatac.community.email_subject_line");
        final Body body = getInvitationMailingBody(user_name, user_email, request);
        myService.sendMail(subject, body, user_email);
    }

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    protected String generatePasswordResetRecord(final SysUser u) {
        final String token = generateToken();
        TokenAction.create(TokenAction.Type.PASSWORD_RESET, token, u);
        return token;
    }

    //TODO : fix this
    protected String getPasswordResetMailingSubject(final SysUser user,
                                                    final Http.Request request) {
        return "How to reset your password";
//        Messages messages = this.messagesApi.preferred((RequestHeader) request.acceptLanguages());
//        messages.apply("test");
//        return this.messagesApi.preferred((Http.RequestHeader) request.acceptLanguages()).at(
//                "How to reset your password");
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

    //todo : fix it
    protected Body getPasswordResetMailingBody(final String token,
                                               final SysUser user, final Http.Request request) {

        final boolean isSecure = ConfigFactory.load().getBoolean("hadatac.https.enable");
        final String url = routes.Signup.resetPassword(token).absoluteURL(
                isSecure, ConfigFactory.load().getString("hadatac.console.base_url"));

//        final Lang lang = (Lang) this.messagesApi.preferred((Http.RequestHeader) request.acceptLanguages()).lang();
        final String langCode = "en"; //lang.code();

        final String html = getEmailTemplate(
                "org.hadatac.console.views.html.account.email.password_reset", langCode, url,
                token, user.getName(), user.getEmail());
        final String text = getEmailTemplate(
                "org.hadatac.console.views.txt.account.email.password_reset", langCode, url, token,
                user.getName(), user.getEmail());

        return new Body(text, html);
    }
    //TODO: test this
    protected Body getInvitationMailingBody(String user_name, String user_email, final Http.Request request) {
        final boolean isSecure = ConfigFactory.load().getBoolean("hadatac.https.enable");
        final String url = routes.Signup.createUser().absoluteURL(
                isSecure, ConfigFactory.load().getString("hadatac.console.base_url"));
//        final Lang lang = this.messagesApi.preferred(request.acceptLanguages()).lang();
        final String langCode = "en";//lang.code();

        final String html = getEmailTemplate(
                "org.hadatac.console.views.html.account.signup.email.invitation_email", langCode, url,
                "", user_name, user_email);
        final String text = getEmailTemplate(
                "org.hadatac.console.views.txt.account.signup.email.invitation_email", langCode, url,
                "", user_name, user_email);

        return new Body(text, html);
    }


    private String getEmailName(final SysUser user) {
        return Mailer.getEmailName(user.getEmail(), user.getName());
    }

    //TODO : fix it
    public void sendVerifyEmailMailingAfterSignup(final SysUser user,
                                                  final Http.Request request) {

        final String subject = getVerifyEmailMailingSubjectAfterSignup(user, request);
        final String token = generateVerificationRecord(user);
        final Body body = getVerifyEmailMailingBodyAfterSignup(token, user, request);
        myService.sendMail(subject, body, getEmailName(user));
    }

    protected Body getVerifyEmailMailingBodyAfterSignup(final String token,
                                                        final SysUser user, final Http.Request request) {

        final boolean isSecure = ConfigFactory.load().getBoolean("hadatac.https.enable");
        final String url = routes.Signup.verify(token).absoluteURL(
                isSecure, ConfigFactory.load().getString("hadatac.console.base_url"));

//        final Lang lang = this.messagesApi.preferred(request.acceptLanguages()).lang();
        final String langCode = "en";//lang.code();

        final String html = getEmailTemplate(
                "org.hadatac.console.views.html.account.signup.email.verify_email", langCode, url, token,
                user.getName(), user.getEmail());
        final String text = getEmailTemplate(
                "org.hadatac.console.views.txt.account.signup.email.verify_email", langCode, url, token,
                user.getName(), user.getEmail());

        return new Body(text, html);
    }

    protected String getVerifyEmailMailingSubjectAfterSignup(final SysUser user,
                                                             final Http.Request request) {
        return "Confirm your e-mail address";
    }

    //TODO: fix it

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
        TokenAction.create(TokenAction.Type.EMAIL_VERIFICATION, token, user);
        return token;
    }

    protected String getVerifyEmailMailingSubject(
            final MyUsernamePasswordAuthUser user, final Http.Request request) {
        return "Complete your signup";
    }

//    @Override
//    protected Call userUnverified(final UsernamePasswordAuthUser authUser) {
//        return routes.Signup.unverified();
//    }

}