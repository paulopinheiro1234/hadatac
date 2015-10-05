import java.util.Arrays;

import org.hadatac.console.models.SecurityRole;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import controllers.routes;

import play.Application;
import play.GlobalSettings;
import play.mvc.Call;
import org.hadatac.console.controllers.*;
import org.hadatac.utils.Repository;

public class Global extends GlobalSettings {

	@Override
	public void onStart(Application app) {
		PlayAuthenticate.setResolver(new Resolver() {

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
		});
		
		// check if CURL is properly installed
		
		// check if SOLR instances are up. If not, start them up
		solrFirstVerification();
		
		// check if SOLR instances are still down, If so, show an error message

		initialData();
		
		// check if default user still have default password. If so, ask to change.
		
	    // check if there is at least one user is pre-registered. If not, ask to pre-register at least the main user
		
		// (NOT SURE THIS SHOULD BE ON ONSTART) check if ontologies are loaded. If not ask to upload them  

		// (NOT SURE THIS SHOULD BE ON ONSTART) check if instances are loaded. If not, show how to upload some default instances  
		
	}

	private void initialData() {
		if (SecurityRole.existsSolr() == false) {
			for (final String roleName : Arrays
					.asList(org.hadatac.console.controllers.AuthApplication.USER_ROLE)) {
				final SecurityRole role = new SecurityRole();
				role.roleName = roleName;
				role.save();
			}
		}
	}

	private void solrFirstVerification() {
		if (!Repository.operational(Repository.DATA)) {
			System.out.println("Repository " + Repository.DATA + " was identified as being down");
			Repository.startStopMetadataRepository(Repository.START, Repository.DATA);			
			System.out.println("A startup command has been issue to epository " + Repository.DATA + ".");
		}
		if (!Repository.operational(Repository.METADATA)) {
			System.out.println("Repository " + Repository.METADATA + " was identified as being down");
			Repository.startStopMetadataRepository(Repository.START, Repository.METADATA);
			System.out.println("A startup command has been issue to epository " + Repository.METADATA + ".");
		}
	}

}