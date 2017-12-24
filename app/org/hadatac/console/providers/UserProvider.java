package org.hadatac.console.providers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import org.hadatac.console.models.SysUser;
import play.mvc.Http.Session;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Service layer for User DB entity
 */
public class UserProvider {

    private final PlayAuthenticate auth;

    @Inject
    public UserProvider(final PlayAuthenticate auth) {
        this.auth = auth;
    }

    @Nullable
    public SysUser getUser(Session session) {
        final AuthUser currentAuthUser = this.auth.getUser(session);
        final SysUser localUser = SysUser.findByAuthUserIdentity(currentAuthUser);
        return localUser;
    }
}
