
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hadatac.data.loader.AnnotationWorker;

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

		initDirectoryStructure();

		// check if default user still have default password. If so, ask to change.
		// TODO: implement this code

		// check if there is at least one user is pre-registered. If not, ask to pre-register at least the main user
		// TODO: implement this code

		// (NOT SURE THIS FUNCTION SHOULD BE CALLED ON ONSTART) check if ontologies are loaded. If not ask to upload them  
		// TODO: implement this code

		// (NOT SURE THIS FUNCTION SHOULD BE CALLED ON ONSTART) check if instances are loaded. 
		//       If not, show how to upload some default instances  
		// TODO: implement this code

		// Create thread for auto csv annotation
		FiniteDuration delay = FiniteDuration.create(0, TimeUnit.SECONDS);
		FiniteDuration frequency = FiniteDuration.create(15, TimeUnit.SECONDS);

		Runnable annotation = new Runnable() {
			@Override
			public void run() {
				AnnotationWorker.autoAnnotate();
			}
		};

		Akka.system().scheduler().schedule(delay, frequency, annotation, Akka.system().dispatcher());
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
}