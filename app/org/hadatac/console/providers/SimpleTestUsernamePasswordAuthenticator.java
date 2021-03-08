package org.hadatac.console.providers;

import be.objectify.deadbolt.java.models.Role;
import controllers.routes;
import org.hadatac.console.models.LinkedAccount;
import org.hadatac.console.models.SysUser;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
//import org.hadatac.console.views.html.error_page;
import org.hadatac.utils.CollectionUtil;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.security.auth.login.CredentialException;
import java.util.*;
//import util.SecurityPasswordEncoder;
//import util.SecurityPasswordEncoder;
import static play.mvc.Results.*;


public class SimpleTestUsernamePasswordAuthenticator implements Authenticator<UsernamePasswordCredentials> {

    //    SecurityPasswordEncoder securityPasswordEncoder;
    PasswordEncoder passwordEncoder;

    @Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context) {
        if (credentials == null) {
            throw new CredentialsException("No credential");
        }
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        if (CommonHelper.isBlank(username)) {
            throw new CredentialsException("Username cannot be blank");
        }
        if (CommonHelper.isBlank(password)) {
            throw new CredentialsException("Password cannot be blank");
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
        try{
            System.out.println(context);
            final SysUser u = SysUser.findByEmailSolr(username);
            System.out.println(context.getSessionStore());
            if (u == null) {
                System.out.println("User not found!");
                redirect(org.hadatac.console.controllers.routes.Application.loginForm())
                        .flashing("error", "user does not exist");
            } else {
                //TODO : email validation implementation
//                if (!u.getEmailValidated()) {
//                    System.out.println("User unverified!");
//                    throw new CredentialException("User is not verified");
//                } else {
                    for (final LinkedAccount acc : u.getLinkedAccounts()) {
                        if (checkPassword(acc.providerUserId, password)) {
                            System.out.println("User logged in!:");// +acc.providerUserId+"...."+ password);
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
//                            System.out.println("User password invalid!"+acc.providerUserId+"...."+ password);
                            throw new CredentialException("User password is invalid");
                        }
                    }
//                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] SimpleTestUsernamePasswordAuthenticator - Exception message: " + e.getMessage());
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
