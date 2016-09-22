import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hadatac.console.controllers.annotator.AutoAnnotator;
import org.hadatac.console.models.SecurityRole;
import org.hadatac.utils.Repository;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import play.Application;
import play.GlobalSettings;
import play.libs.Akka;
import play.mvc.Call;
import scala.concurrent.duration.FiniteDuration;

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
		// TODO: implement this code
		
		// check if SOLR instances are up. If not, start them up
		solrFirstVerification();
		
		// check if SOLR instances are still down, If so, show an error message
		// TODO: implement this code

        // check existence/availability of security role
		initialData();
		initDirectoryStructure();
		
		// check if default user still have default password. If so, ask to change.
		// TODO: implement this code
		
	    // check if there is at least one user is pre-registered. If not, ask to pre-register at least the main user
		// TODO: implement this code
		
		// (NOT SURE THIS FUNCTION SHOULD BE CALLED ON ONSTART) check if ontologies are loaded. If not ask to upload them  
		// TODO: implement this code

		// (NOT SURE THIS FUNCTION SHOULD BE CALLED ON ONSTART) check if instances are loaded. If not, show how to upload some default instances  
		// TODO: implement this code
		
		// Create thread for auto ccsv annotation
		FiniteDuration delay = FiniteDuration.create(0, TimeUnit.SECONDS);
		FiniteDuration frequency = FiniteDuration.create(30, TimeUnit.SECONDS);

		Runnable annotation = new Runnable() {
		   @Override
		   public void run() {
			   AutoAnnotator.autoAnnotate();
		   }
		};
		
		Akka.system().scheduler().schedule(delay, frequency, annotation, Akka.system().dispatcher());
	}

	private void initialData() {
		if (SecurityRole.existsSolr() == false) {
			for (final String roleName : Arrays
					.asList(org.hadatac.console.controllers.AuthApplication.DATA_OWNER_ROLE, org.hadatac.console.controllers.AuthApplication.DATA_MANAGER_ROLE)) {
				final SecurityRole role = new SecurityRole();
				role.roleName = roleName;
				role.save();
			}
		}
	}
	
	private void initDirectoryStructure(){
		List<String> listFolderPaths = new LinkedList<String>();
		listFolderPaths.add("tmp");
		listFolderPaths.add("logs");
		listFolderPaths.add("processed_csv");
		listFolderPaths.add("unprocessed_csv");
		listFolderPaths.add("tmp/ttl");
		listFolderPaths.add("tmp/cache");
		listFolderPaths.add("tmp/uploads");
    	
		for(String path : listFolderPaths){
			File folder = new File(path);
			// if the directory does not exist, create it
	    	if (!folder.exists()) {
	    	    System.out.println("creating directory: " + path);
	    	    try{
	    	    	folder.mkdir();
	    	    } 
	    	    catch(SecurityException se){
	    	    	System.out.println("Failed to create directory.");
	    	    }
	    	    System.out.println("DIR created");
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