package org.hadatac.console.controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import javax.inject.Inject;

import com.feth.play.module.mail.Mailer;
import com.typesafe.config.ConfigFactory;
import module.SecurityModule;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.TokenAction;
import org.hadatac.console.providers.SimpleTestUsernamePasswordAuthenticator;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.account.signup.unverified;
import org.hadatac.console.views.html.account.errorLogin;
import org.hadatac.console.views.html.loginForm;
import org.pac4j.cas.profile.CasProxyProfile;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.http.PlayHttpActionAdapter;
import org.pac4j.play.java.Secure;
import org.pac4j.play.store.PlayCacheSessionStore;
import org.pac4j.play.store.PlaySessionStore;
import play.Logger;
import play.api.libs.mailer.MailerClient;
import play.libs.mailer.Email;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
//import providers.MyUsernamePasswordAuthProvider;
import org.hadatac.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletionStage;

import static org.hadatac.Constants.EMAIL_TEMPLATE_FALLBACK_LANGUAGE;

public class Application extends Controller {

    @Inject
    private Config config;

    @Inject
    private PlaySessionStore playSessionStore;
    @Inject CentralLogoutController centralLogoutController;
    @Inject private MailerClient mailerClient;
    private static SysUser sysUser;
    private PlayWebContext playWebContext;
    private static String studyRefLink;

    private List<CommonProfile> getProfiles(Http.Request request) {
        final PlayWebContext context = new PlayWebContext(request, playSessionStore);
        final ProfileManager<CommonProfile> profileManager = new ProfileManager(context);
        return profileManager.getAll(true);
    }

    private CommonProfile getProfile(Http.Request request) {
        final CommonProfile profile = new CommonProfile();
        final PlayWebContext context = new PlayWebContext(request, playSessionStore);
        final ProfileManager<CommonProfile> profileManager = new ProfileManager(context);
        if (null != sysUser && null != sysUser.getEmail()) {
            profile.setId(sysUser.getEmail());
            profile.addAttribute(Pac4jConstants.USERNAME, sysUser.getEmail());
            profile.setRoles(getUserRoles(sysUser));
            profile.setRemembered(true);
            profileManager.save(true, profile, true);
        }
        return (profileManager.getAll(true).isEmpty() ? null : profileManager.getAll(true).get(0));
    }

    public String getSessionId (Http.Request request){
        final PlayWebContext context = new PlayWebContext(request, playSessionStore);
        final String sessionId = context.getSessionStore().getOrCreateSessionId(context);
//        System.out.println("sessionId :"+ sessionId);
        return sessionId;
    }

    public String getUserEmail(Http.Request request) {
        if("true".equalsIgnoreCase(ConfigFactory.load().getString("hadatac.ThirdPartyUser.userRedirection"))){
            final PlayWebContext context = getPlayWebContext()!=null? getPlayWebContext():new PlayWebContext(request, playSessionStore);
            final ProfileManager<CommonProfile> profileManager = new ProfileManager(context,playSessionStore);
            final String userEmail =  profileManager.get(true).isEmpty() ? "": profileManager.get(true).get().getUsername();
            System.out.println("getUserEmail:"+userEmail+", sessionId:"+playSessionStore.getOrCreateSessionId(context));
            return userEmail;
        }
        final String userEmail = (getProfile(request) == null) ? "" : getProfile(request).getUsername();
        return userEmail;
    }

    private Result protectedIndexView(Http.Request request) {
//        getUserEmail(request);
//        getProfiles(request);
        String userEmail =getUserEmail(request);
        return ok(protectedIndex.render(userEmail));
    }

    private Result notProtectedIndexView(Http.Request request) {
        return ok(notprotectedIndex.render(getProfiles(request)));
    }

    public Result facebookNotProtectedIndex(Http.Request request) {
        return notProtectedIndexView(request);
    }

    @Secure(clients = "FacebookClient", authorizers = "admin")
    public Result facebookAdminIndex(Http.Request request) {
        return protectedIndexView(request);
    }

    @Secure(clients = "FacebookClient", authorizers = "custom")
    public Result facebookCustomIndex(Http.Request request) {
        return protectedIndexView(request);
    }

    @Secure(clients = "TwitterClient,FacebookClient")
    public Result twitterIndex(Http.Request request) {
        return protectedIndexView(request);
    }

    @SubjectPresent(forceBeforeAuthCheck = true)
    public Result protectedIndex(Http.Request request) {
        return protectedIndexView(request);
    }

    @SubjectPresent(handlerKey = "FormClient", forceBeforeAuthCheck = true)
    public Result formIndex(Http.Request request) {
        SysUser user = SysUser.findByEmail(getUserEmail(request));
        System.out.println("Application->formindex:"+user.getEmail()+",studyRefLink:"+studyRefLink);
        if(null != user && studyRefLink!=null && !studyRefLink.isEmpty()){
            System.out.println("Application->formindex->studyRefLink:"+studyRefLink);
            return redirect (studyRefLink).addingToSession(request ,"userValidated", "yes");
        }
        if(null != user && user.isDataManager()){
            System.out.println("Application->formindex->DataManager:"+user.getEmail());
            return ok(protectedIndex.render(user.getEmail()));
        }
        System.out.println("Application->formindex->NormalUser:"+user.getEmail());
        return ok(portal.render(user.getEmail()));
    }

    @Secure(clients = "IndirectBasicAuthClient")
    public Result basicauthIndex(Http.Request request) {
        return protectedIndexView(request);
    }

    @Secure(clients = "DirectBasicAuthClient,ParameterClient,DirectFormClient")
    public Result dbaIndex(Http.Request request) {

        Utils.block();

        return protectedIndexView(request);
    }

    @Secure(clients = "CasClient")
    public Result casIndex(Http.Request request) {
        final CommonProfile profile = getProfiles(request).get(0);
        final String service = "http://localhost:8080/proxiedService";
        String proxyTicket = null;
        if (profile instanceof CasProxyProfile) {
            final CasProxyProfile proxyProfile = (CasProxyProfile) profile;
            proxyTicket = proxyProfile.getProxyTicketFor(service);
        }
        return ok(org.hadatac.console.views.html.casProtectedIndex.render(profile, service, proxyTicket));
    }

    @Secure(clients = "SAML2Client")
    public Result samlIndex(Http.Request request) {
        return protectedIndexView(request);
    }

    @Secure(clients = "OidcClient")
    public Result oidcIndex(Http.Request request) {
        return protectedIndexView(request);
    }

    //@Secure(clients = "ParameterClient")
    //@SubjectPresent(handlerKey = "ParameterClient")
    public Result restJwtIndex(Http.Request request) {
        return protectedIndexView(request);
    }

    //@Secure(clients = "AnonymousClient", authorizers = "csrfCheck")
    public Result csrfIndex(Http.Request request) {
        return ok(org.hadatac.console.views.html.csrf.render(getProfiles(request)));
    }

    public Result loginForm(Http.Request request) throws TechnicalException {
        //If The user has been redirected from portal to Hadatac. To login we go back to the redirected portal
        if("true".equalsIgnoreCase(ConfigFactory.load().getString("hadatac.ThirdPartyUser.userRedirection"))) {
            if(null != sysUser){
                SimpleTestUsernamePasswordAuthenticator test = new SimpleTestUsernamePasswordAuthenticator();
                final PlayWebContext playWebContext = new PlayWebContext(request, playSessionStore);
                final ProfileManager<CommonProfile> profileManager = new ProfileManager(playWebContext);
                final CommonProfile profile = new CommonProfile();
                profile.setId(sysUser.getEmail());
                profile.addAttribute(Pac4jConstants.USERNAME, sysUser.getEmail());
                profile.setRoles(test.getUserRoles(sysUser));
                profile.setRemembered(true);
                profileManager.save(true, profile, true);
                System.out.println("Application->loginForm->studyRefLink:"+studyRefLink);
                if(null != sysUser && studyRefLink!=null && !studyRefLink.isEmpty()){
                    System.out.println("Inside if Application->loginForm->studyRefLink:"+studyRefLink);
                    return redirect (studyRefLink).addingToSession(request ,"userValidated", "yes");
                }
                if(null != sysUser && sysUser.isDataManager()){
                    System.out.println("Application->loginForm->DataManager:"+sysUser.getEmail());
                    return ok(protectedIndex.render(sysUser.getEmail()));
                }
                System.out.println("Application->loginForm->NormalUser:"+sysUser.getEmail());
                return ok(portal.render(sysUser.getEmail()));
            }
            return redirect(ConfigFactory.load().getString("hadatac.ThirdPartyUser.oauth.redirectionUrl"));
        }

        final FormClient formClient = (FormClient) config.getClients().findClient("FormClient").get();
        Optional<String> username = request.queryString("username");
        Optional<String> error = request.queryString("error");

        if (!error.isEmpty() && error.get().equalsIgnoreCase("CredentialsException") && !username.isEmpty()) {
            Logger.info("credentials exception!");
            SysUser sysUser = AuthApplication.getAuthApplication().getUserProvider().getUser(username.get());
            sendVerifyEmailMailingAfterSignup(sysUser);
            return unverified();
        }
        else if(!error.isEmpty() && error.get().equalsIgnoreCase("BadCredentialsException")) {
            Logger.info("bad credentials exception!");
            return ok(errorLogin.render());
        }
        Logger.info("no credentials exception!");
        return ok(loginForm.render(formClient.getCallbackUrl()));
    }

    public Result jwt(Http.Request request) {
        final List<CommonProfile> profiles = getProfiles(request);
        final JwtGenerator generator = new JwtGenerator(new SecretSignatureConfiguration(SecurityModule.JWT_SALT));
        String token = "";
        if (CommonHelper.isNotEmpty(profiles)) {
            token = generator.generate(profiles.get(0));
        }
        return ok(org.hadatac.console.views.html.jwt.render(token));
    }

    public Result forceLogin(Http.Request request) {
        final PlayWebContext context = new PlayWebContext(request, playSessionStore);
        final Client client = config.getClients().findClient(context.getRequestParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER).get()).get();
        try {
            final HttpAction action = (HttpAction) client.getRedirectionAction(context).get();
            return (Result) PlayHttpActionAdapter.INSTANCE.adapt(action, context);
        } catch (final HttpAction e) {
            throw new TechnicalException(e);
        }
    }

    public Result unverified() throws TechnicalException {
        return (ok(unverified.render())).withHeader("Cache-Control", "no-cache");
    }

    public void sendVerifyEmailMailingAfterSignup(final SysUser sysUser) {
        final String subject = "Confirm your e-mail address";
        final String token = generateVerificationRecord(sysUser);
        final Mailer.Mail.Body body = getVerifyEmailMailingBodyAfterSignup(token, sysUser);
        String recipient = Mailer.getEmailName(sysUser.getEmail(), sysUser.getName());
        Email email = new Email()
                .setSubject(subject)
                .setBodyText(body.getText())
                .setBodyHtml(body.getHtml())
                .setFrom(ConfigFactory.load().getString("hadatac.community.contact_email"))
                .addTo(recipient);
        mailerClient.send(email);
    }

    protected String generateVerificationRecord(final SysUser user) {
        final String token = UUID.randomUUID().toString();
        TokenAction.create(TokenAction.Type.EMAIL_VERIFICATION, token, user);
        return token;
    }

    protected Mailer.Mail.Body getVerifyEmailMailingBodyAfterSignup(final String token, final SysUser user) {
        final boolean isSecure = ConfigFactory.load().getBoolean("hadatac.https.enable");
        final String url = routes.Signup.verify(token).absoluteURL(
                isSecure, ConfigFactory.load().getString("hadatac.console.base_url"));
        final String langCode = "en";//lang.code();
        final String html = getEmailTemplate(
                "org.hadatac.console.views.html.account.signup.email.verify_email", langCode, url, token,
                user.getName(), user.getEmail());
        final String text = getEmailTemplate(
                "org.hadatac.console.views.txt.account.signup.email.verify_email", langCode, url, token,
                user.getName(), user.getEmail());

        return new Mailer.Mail.Body(text, html);
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
    public Result getReceiver() {
        return ok(org.hadatac.console.views.html.receiver.render(""));
    }
    private Set<String> getUserRoles(SysUser sysUser){
        int rolesSize =sysUser.getRoles().size();
        Set<String> roles = new HashSet<String>();
        while (rolesSize > 0){
            roles.add(sysUser.getRoles().get(rolesSize-1).getName());
            rolesSize--;
        }
        return roles;
    }

    public Result protectedInd(String email) {
        SysUser user = SysUser.findByEmail(email);
        if(null != user && user.isDataManager()){
            return ok(protectedIndex.render(user.getEmail()));
        }
        return ok(portal.render(email));
    }

    public void formIndex(Http.Request request, SysUser sysUserValue, PlaySessionStore sessionStore, PlayWebContext webContext, String refLink){
        sysUser = sysUserValue;
        playSessionStore = sessionStore;
        setSessionStore(sessionStore);
        setPlayWebContext(webContext);
        studyRefLink=refLink;
//        System.out.println("studyRefLink:"+studyRefLink);

    }
    private SysUser getSysUser(){return sysUser;}
    private void setSysUser(SysUser sysUserNew){ sysUser=sysUserNew;}

    public CompletionStage<Result> logOutUser (Http.Request request){
        setSysUser(null);
        return centralLogoutController.logout(request);
    }
    public void setSessionStore ( PlaySessionStore sessionStore){
        playSessionStore =sessionStore;
    }
    public PlaySessionStore getSessionStore (){
        return  playSessionStore ;
    }
    public void setPlayWebContext ( PlayWebContext webContext){
        playWebContext =webContext;
    }
    public PlayWebContext getPlayWebContext (){
        return  playWebContext ;
    }

}
