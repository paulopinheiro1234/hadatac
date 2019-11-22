package org.hadatac.console.service;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import play.mvc.Call;

/**
 * Concrete resolver implementation.
 */
public class MyResolver extends Resolver {
    @Override
    public Call login() {
        // Your login page
    	return org.hadatac.console.controllers.routes.AuthApplication.login();
    }

    @Override
    public Call afterAuth() {
        // The user will be redirected to this page after authentication
        // if no original URL was saved
        return org.hadatac.console.controllers.routes.AuthApplication.index();
    }

    @Override
    public Call afterLogout() {
    	return org.hadatac.console.controllers.routes.AuthApplication.index();
    }

    @Override
    public Call auth(final String provider) {
        // You can provide your own authentication implementation,
        // however the default should be sufficient for most cases
    	return com.feth.play.module.pa.controllers.routes.Authenticate
				.authenticate(provider);
    }

    @Override
    public Call askMerge() {
    	return org.hadatac.console.controllers.routes.Account.askMerge();
    }

    @Override
    public Call askLink() {
    	return org.hadatac.console.controllers.routes.Account.askLink();
    }

    @Override
    public Call onException(final AuthException e) {
    	if (e instanceof AccessDeniedException) {
			return org.hadatac.console.controllers.routes.Signup
					.oAuthDenied(((AccessDeniedException) e)
							.getProviderKey());
		}

		// more custom problem handling here...
		return super.onException(e);
    }
}