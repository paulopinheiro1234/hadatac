package module;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hadatac.console.models.SecurityRole;
import org.hadatac.data.loader.mqtt.MqttMessageWorker;
import org.hadatac.console.http.SPARQLUtilsFacetSearch;
import org.hadatac.utils.CollectionUtil;

import org.hadatac.utils.ConfigProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OnStart {

	private static final Logger log = LoggerFactory.getLogger(OnStart.class);

    @Inject
    public OnStart() {
    	initDirectoryStructure();
    	 
    	// check existence/availability of security role
    	SecurityRole.initialize();
    	
    	CollectionUtil.getInstance();

		// check if default user still have default password. If so, ask to change.
		// TODO: implement this code

		// check if there is at least one user is pre-registered. If not, ask to pre-register at least the main user
		// TODO: implement this code

		// (NOT SURE THIS FUNCTION SHOULD BE CALLED ON ONSTART) check if ontologies are loaded. If not ask to upload them  
		// TODO: implement this code

		// (NOT SURE THIS FUNCTION SHOULD BE CALLED ON ONSTART) check if instances are loaded. 
		//       If not, show how to upload some default instances  
		// TODO: implement this code

    	MqttMessageWorker.getInstance();

		// also populate the in-memory model
		log.warn("building in-memory model ... : ");
		SPARQLUtilsFacetSearch.createInMemoryModel();
		log.warn("finished building in-memory model.");
    
    }
    
    private void initDirectoryStructure(){
		List<String> listFolderPaths = new LinkedList<String>();
		/*listFolderPaths.add("tmp");
		listFolderPaths.add("logs");
		listFolderPaths.add("processed_csv");
		listFolderPaths.add("unprocessed_csv");
		listFolderPaths.add("downloaded_csv");
		listFolderPaths.add("working_csv");
		listFolderPaths.add("tmp/ttl");
		listFolderPaths.add("tmp/cache");
		listFolderPaths.add("tmp/uploads");*/
		
		listFolderPaths.add(ConfigProp.getTmp());
		listFolderPaths.add(ConfigProp.getLogs());
		listFolderPaths.add(ConfigProp.getPathProc());
		listFolderPaths.add(ConfigProp.getPathUnproc());
		listFolderPaths.add(ConfigProp.getPathDownload());
		listFolderPaths.add(ConfigProp.getPathWorking());
		listFolderPaths.add(ConfigProp.getTmp() + "ttl");
		listFolderPaths.add(ConfigProp.getTmp() + "cache");
		listFolderPaths.add(ConfigProp.getTmp() + "uploads");
		
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

