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
	

	public static String GEODEPLOYMENT = "GEODEPLOYMENT";
	private static State allState = new State(State.ALL);
	
	private String geoCoordList = "[]";
	private String platformNameList = "[]";
	private String platformUriList = "[]";
	private String dimensionsList = "[]";
	//private int totDeployments = -1;

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri, String plat_uri, String previous_plt_uri) {

		// Looking for georeferenced platforms 
		if (plat_uri == null || plat_uri.isEmpty()) {
			geoDeployments();
	    	return ok(deploymentBrowser.render(dir, filename, da_uri, geoCoordList, platformNameList, platformUriList));
		}
		Platform platform = Platform.find(plat_uri);
		if (platform == null) {
	    	return ok(deploymentBrowser.render(dir, filename, da_uri, "[]", "[]", "[]"));
		}
		System.out.println("Platform URI: " + platform.getUri());
		System.out.println("Platform Layout: " + platform.getLayout());
		if (platform.getLayout() == null || platform.getLayout().isEmpty()) {
			List<Deployment> dpls = Deployment.findByPlatformAndStatus(plat_uri, allState);
			System.out.println("# Deployments: " + dpls.size());
			if (dpls.size() <= 0) {
		    	return ok(deploymentBrowser.render(dir, filename, da_uri, "[]", "[]", "[]"));
			}
			System.out.println("Deployment URI: " + dpls.get(0).getUri());
			ViewDeployment viewDpl = new ViewDeployment();
			if (previous_plt_uri == null || previous_plt_uri.isEmpty()) {
				previous_plt_uri = GEODEPLOYMENT;
			}
 			return viewDpl.index(dpls.get(0).getUri(), previous_plt_uri);
		}
		platDeployments(platform);
		return ok(deploymentBrowserWithPlatform.render(dir, filename, da_uri, geoCoordList, platformNameList, platformUriList, platform, dimensionsList, plat_uri));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri, String plat_uri, String previous_plt_uri) {
    	return index(dir, filename, da_uri, plat_uri, previous_plt_uri);
    }

	private void geoDeployments() {
		List<Deployment> deployments = Deployment.find(allState);
		List<Platform> platforms = new ArrayList<Platform>();
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
				while (plt != null && !plt.getFirstCoordinateCharacteristic().equals(Platform.LAT) && !plt.getSecondCoordinateCharacteristic().equals(Platform.LONG)) {
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
					//totDeployments++;
					if (!platforms.contains(plt)) {
						platforms.add(plt);
					}
				}
			}
		}
		generateJSON(platforms);
	}
	
	private void platDeployments(Platform platform) {
		List<Deployment> deployments = Deployment.findByReferenceLayoutAndStatus(platform.getUri(), allState);
		List<Platform> platforms = new ArrayList<Platform>();
		dimensionsList = "[";
		if (platform.getWidth() == null) {
			dimensionsList = dimensionsList + " ,";
		} else {
			dimensionsList = dimensionsList + platform.getWidth() + " ,";
		}
		if (platform.getDepth() == null) {
			dimensionsList = dimensionsList + " ,";
		} else {
			dimensionsList = dimensionsList + platform.getDepth() + " ,";
		}
		if (platform.getWidth() == null) {
			dimensionsList = dimensionsList + " ]";
		} else {
			dimensionsList = dimensionsList + platform.getHeight() + " ]";
		}
		for (Deployment dpl: deployments) {
			Platform plt = dpl.getPlatform();
			System.out.println(plt.getLabel() + ":  " + plt.getFirstCoordinate() + " , " + plt.getSecondCoordinate());
			if (plt.getFirstCoordinate() != null && plt.getSecondCoordinate() != null) {
				platforms.add(plt);
			}
		}
		generateJSON(platforms);
	}
	
	private void generateJSON(List<Platform> platforms) {
		geoCoordList = "[";
		platformNameList = "[";
		platformUriList = "[";
		for (int i = 0; i < platforms.size(); i++) {
			Platform plt = platforms.get(i);
			geoCoordList = geoCoordList + plt.getFirstCoordinate() + "," + plt.getSecondCoordinate();
			platformNameList = platformNameList + "\"" + plt.getLabel() + "\"";
			platformUriList = platformUriList + "\"" + plt.getUri() + "\"";
			if (i < platforms.size() - 1) {
				geoCoordList = geoCoordList + ",";
				platformNameList = platformNameList + ",";
				platformUriList = platformUriList + ",";
			}
		}
		geoCoordList = geoCoordList + "]";
		platformNameList = platformNameList + "]";
		platformUriList = platformUriList + "]";
	}
	
}
