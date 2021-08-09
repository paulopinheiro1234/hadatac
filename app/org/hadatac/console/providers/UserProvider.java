package org.hadatac.console.providers;

import org.hadatac.console.models.SysUser;
import org.ldaptive.Credential;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import play.mvc.Http.Session;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Service layer for User DB entity
 */
public class UserProvider {

    @Nullable
    public SysUser getUser(String username) {
        SysUser localUser = SysUser.findByEmail(username);
        return localUser;
    }

}
