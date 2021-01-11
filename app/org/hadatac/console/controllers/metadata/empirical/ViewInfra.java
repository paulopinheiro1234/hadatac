package org.hadatac.console.controllers.metadata.empirical;

import java.util.List;

import org.hadatac.Constants;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.Platform;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.metadata.empirical.*;
import play.mvc.Result;
import play.mvc.Controller;

public class ViewInfra extends Controller {

    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
    public Result viewPlatform(String dir, String filename, String da_uri, String platform_uri) {
    	try {
    	    platform_uri = java.net.URLDecoder.decode(platform_uri,"UTF8");
    	} catch (Exception e) {
    	}    	
    	System.out.println("platform URI: [" + platform_uri + "]");
    	Platform platform = Platform.find(platform_uri);
        return ok(viewPlatform.render(dir, filename, da_uri, platform));
    }
    
    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
	public Result postViewPlatform(String dir, String filename, String da_uri, String platform_uri) {
        return viewPlatform(dir, filename, da_uri, platform_uri);
    }

    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
    public Result viewInstrument(String dir, String filename, String da_uri, String instrument_uri) {
    	try {
    	    instrument_uri = java.net.URLDecoder.decode(instrument_uri,"UTF8");
    	} catch (Exception e) {
    	}    	
    	Instrument instrument = Instrument.find(instrument_uri);
        return ok(viewInstrument.render(dir, filename, da_uri, instrument));
    }
    
    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
	public Result postViewInstrument(String dir, String filename, String da_uri, String instrument_uri) {
        return viewInstrument(dir, filename, da_uri, instrument_uri);
    }

    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
    public Result viewDetector(String dir, String filename, String da_uri, String detector_uri) {
    	try {
    	    detector_uri = java.net.URLDecoder.decode(detector_uri,"UTF8");
    	} catch (Exception e) {
    	}    	
    	System.out.println("Detector URI: [" + detector_uri + "]");
    	Detector detector = Detector.find(detector_uri);
        return ok(viewDetector.render(dir, filename, da_uri, detector));
    }
    
    @Restrict(@Group(Constants.DATA_OWNER_ROLE))
	public Result postViewDetector(String dir, String filename, String da_uri, String detector_uri) {
        return viewDetector(dir, filename, da_uri, detector_uri);
    }

}