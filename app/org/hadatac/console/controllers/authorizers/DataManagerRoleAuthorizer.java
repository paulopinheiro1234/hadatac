package org.hadatac.console.controllers.authorizers;

import org.apache.commons.lang.StringUtils;
import org.hadatac.Constants;
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAllRolesAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.Set;

public class DataManagerRoleAuthorizer extends ProfileAuthorizer<CommonProfile> {

    @Override
    public boolean isAuthorized(final WebContext context, final List<CommonProfile> profiles) throws HttpAction {
        System.out.println("Is  DataManagerRoleAuthorizer authorised was called:"+isAnyAuthorized(context, profiles));
        return isAnyAuthorized(context, profiles);
    }

    @Override
    public  boolean isProfileAuthorized(final WebContext context, final CommonProfile profile) {
        if (profile == null) {
            return false;
        }
        Set<String> roles = profile.getRoles();
        System.out.println("IsDataManagerRoleAuthorizer  profile authorised was called:"+ roles.contains(Constants.DATA_MANAGER_ROLE));
        return roles.contains(Constants.DATA_MANAGER_ROLE);
    }

}
