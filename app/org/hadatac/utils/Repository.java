package org.hadatac.utils;

import org.hadatac.data.loader.DataContext;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.PermissionsContext;

import play.Play;

public class Repository {

	public static final String METADATA = "metadata";
	public static final String DATA     = "data";	
	
	public static final String START = "start";
	public static final String STOP  = "stop";
	
	private static final String METADATA_SCRIPT = "run_solr4.sh";
	private static final String DATA_SCRIPT     = "run_solr5.sh";
	
    public static boolean operational(String repository) {
    	if (repository.equals(METADATA)) {
    		return ((MetadataContext.playTotalTriples() != -1) && 
    				(PermissionsContext.playTotalTriples() != -1));
    	} else if (repository.equals(DATA)) {
        	 return ((DataContext.playTotalDataCollections() != -1)&&
                     (DataContext.playTotalMeasurements() != -1));
    		
    	}
    	return false;
    }

    public static String startStopMetadataRepository(String oper, String repository) {
    	String message = "";
    	String script = "";
        if (!oper.equals(START) && !oper.equals(STOP)) {
        	message = Feedback.println(Feedback.WEB, "Invalid operation. It should be either " + START + " or " + STOP);
        	return message;
        }
    	String home = Play.application().configuration().getString("hadatac.solr.home");
    	System.out.println(home);
    	if (!home.endsWith("/")) {
    		home = home + "/";
    	}
    	if (repository.equals(METADATA)) {
    		script = home + METADATA_SCRIPT;
    	} else if (repository.equals(DATA)) {
    	    script = home + DATA_SCRIPT;	
    	} else {
    		message = "FAIL";
    		return message;
    	}
	    String[] cmd = {script, oper};
		message += Feedback.print(Feedback.WEB, "Requested " + oper + " " + repository + " repository.");                
	    message += Command.exec(Feedback.WEB, false, cmd);
    	return message;
    }
	
}	
	
