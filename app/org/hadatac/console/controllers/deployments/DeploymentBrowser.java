package org.hadatac.console.controllers.deployments;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class DeploymentBrowser extends Controller {
	
	private static String LAT = "http://semanticscience.org/resource/Latitude";
	private static String LONG = "http://semanticscience.org/resource/Longitude";
	
	private static State allState = new State(State.ALL);
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri) {
    	List<Deployment> deployments = Deployment.find(allState);
    	List<String> geoCoordinates = new ArrayList<String>();
    	String geoCoordList = "[]";
    	for (Deployment dp : deployments) {
    		Platform plt = dp.getPlatform();
    		if (plt != null) {
    			//System.out.println(plt.getLabel() + ":  " + plt.getUri() + " " + plt.getFirstCoordinate() + " " + plt.getFirstCoordinateCharacteristic() +  
    			//		" " + plt.getSecondCoordinate() + " " + plt.getSecondCoordinateCharacteristic());
    			if (plt.getFirstCoordinateCharacteristic() == null) {
    				plt.setFirstCoordinateCharacteristic("");
    			}
    			if (plt.getSecondCoordinateCharacteristic() == null) {
    				plt.setSecondCoordinateCharacteristic("");
    			}
    			while (plt != null && !plt.getFirstCoordinateCharacteristic().equals(LAT) && !plt.getSecondCoordinateCharacteristic().equals(LONG)) {
        			//System.out.println(plt.getLabel() + " (" + plt.getUri() + "): " + plt.getPartOf() + " " + plt.getFirstCoordinate() + " " + plt.getFirstCoordinateCharacteristic() +  
        			//		" " + plt.getSecondCoordinate() + " " + plt.getSecondCoordinateCharacteristic());
    				if (plt.getPartOf() == null) {
    					plt = null;
    				} else {
    	    			//System.out.println("reading " + plt.getPartOf());  
    					plt = Platform.find(plt.getPartOf());
    					if (plt != null) {
    						if (plt.getFirstCoordinateCharacteristic() == null) {
    							plt.setFirstCoordinateCharacteristic("");
    						}
    						if (plt.getSecondCoordinateCharacteristic() == null) {
    							plt.setSecondCoordinateCharacteristic("");
    						}
    					}
    				}
    			}
    			if (plt != null) {
    				String geoCoord = plt.getFirstCoordinate() + "," + plt.getSecondCoordinate();
    				if (!geoCoordinates.contains(geoCoord)) {
    					geoCoordinates.add(geoCoord);
    				}
    				//System.out.println("Final: " + plt.getLabel() + ":  " + plt.getUri() + " " + plt.getFirstCoordinate() + " " + plt.getFirstCoordinateCharacteristic() +  
    				//		" " + plt.getSecondCoordinate() + " " + plt.getSecondCoordinateCharacteristic());
    			}
    			geoCoordList = "[";
    			for (int i=0; i < geoCoordinates.size(); i++) {
    				geoCoordList = geoCoordList + geoCoordinates.get(i);
    				if (i < geoCoordinates.size() - 1) {
    					geoCoordList = geoCoordList + ",";
    				}
    			}
    			geoCoordList = geoCoordList + "]";
    		}
    	}
    	return ok(deploymentBrowser.render(dir, filename, da_uri, geoCoordList));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri) {
    	return index(dir, filename, da_uri);
    }

}
