package org.hadatac.console.providers;

import com.feth.play.module.mail.Mailer;
import com.typesafe.config.ConfigFactory;
import org.hadatac.console.controllers.routes;
import org.hadatac.console.models.LinkedAccount;
import org.hadatac.console.models.SysUser;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
//import org.hadatac.console.views.account.signup.unverified;
import org.hadatac.console.models.TokenAction;
import org.hadatac.utils.CollectionUtil;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.pac4j.core.exception.BadCredentialsException;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.security.crypto.bcrypt.BCrypt;
import play.Logger;
import play.api.libs.mailer.Email;
import play.api.libs.mailer.MailerClient;
//import play.libs.Mail;

import javax.inject.Inject;
import javax.security.auth.login.CredentialException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
//import util.SecurityPasswordEncoder;
//import util.SecurityPasswordEncoder;
import static org.hadatac.Constants.EMAIL_TEMPLATE_FALLBACK_LANGUAGE;
import static play.mvc.Results.*;


public class SimpleTestUsernamePasswordAuthenticator implements Authenticator<UsernamePasswordCredentials> {

    PasswordEncoder passwordEncoder;

    @Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context) {
        if (credentials == null) {
            throw new CredentialsException("No credential");
        }
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (CommonHelper.isBlank(username)) {
            throw new BadCredentialsException("Username cannot be blank");
        }
        if (CommonHelper.isBlank(password)) {
            throw new BadCredentialsException("Password cannot be blank");
        }
        //Querying from DB
        SolrClient solrClient = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.AUTHENTICATE_USERS)).build();
        String query = "active_bool:true";
//        System.out.println("solrClient:"+solrClient);
        SolrQuery solrQuery = new SolrQuery(query);
//        System.out.println("solrQuery:"+solrQuery);
        List<SysUser> users = new ArrayList<SysUser>();
        //CHECK PASSWORD
            final SysUser u = SysUser.findByEmailSolr(username);
            System.out.println(context.getSessionStore());
            if (u == null) {
                System.out.println("User not found!");
                redirect(routes.Application.loginForm())
                        .flashing("error", "user does not exist");
            }
            int userValidated = u.getEmailValidated()? 1:0;
            switch (userValidated) {
                case 0:
                    throw new CredentialsException("User email is not unverified");
                case 1:
                    for (final LinkedAccount acc : u.getLinkedAccounts()) {
                        if (checkPassword(acc.providerUserId, password)) {
                            System.out.println("User logged in!:");
                            System.out.println(context.getSessionStore());
                            //TODO : customise the profile
                            final CommonProfile profile = new CommonProfile();
                            profile.setId(username);
                            profile.addAttribute(Pac4jConstants.USERNAME, username);
                            credentials.setUserProfile(profile);
                            profile.setRoles(getUserRoles(u));
                            profile.setRemembered(true);
                            System.out.println("Profile:"+profile);
                            break;
                        } else {
                            try {
                                throw new CredentialException("User password is invalid");
                            } catch (CredentialException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + userValidated);
            }
    }

    private boolean checkPassword(final String hashed, final String candidate) {
        if(hashed == null || candidate == null) {
            return false;
        }
        return BCrypt.checkpw(candidate, hashed);
    }

    private Set<String> getUserRoles(SysUser sysUser){
        int rolesSize =sysUser.getRoles().size();
        Set<String> roles = new HashSet<String> ();
        while (rolesSize > 0){
            roles.add(sysUser.getRoles().get(rolesSize-1).getName());
            rolesSize--;
        }
        return roles;
    }
}
