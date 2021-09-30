package org.hadatac.console.controllers.authorizers;

import org.hadatac.Constants;
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.Set;

public class FileViewerEditorAuthorizer extends ProfileAuthorizer<CommonProfile> {
    @Override
    public boolean isAuthorized(final WebContext context, final List<CommonProfile> profiles) throws HttpAction {
//        System.out.println("Is  FileViewerEditorAuthorizer authorised was called:"+isAnyAuthorized(context, profiles));
        return isAnyAuthorized(context, profiles);
    }

    @Override
    public  boolean isProfileAuthorized(final WebContext context, final CommonProfile profile) {
        if (profile == null) {
            return false;
        }
        Set<String> roles = profile.getRoles();
//        System.out.println("FileViewerEditorAuthorizer  profile authorised was called:"+ roles.contains(Constants.FILE_VIEWER_EDITOR_ROLE));
        return roles.contains(Constants.FILE_VIEWER_EDITOR_ROLE);
    }

}
