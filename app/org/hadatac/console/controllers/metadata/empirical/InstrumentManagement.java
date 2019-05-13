package org.hadatac.console.controllers.metadata.empirical;

import java.util.List;

import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.Platform;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.metadata.empirical.*;
import play.mvc.Result;
import play.mvc.Controller;

public class InstrumentManagement extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String dir, String filename, String da_uri) {
    	List<Instrument> instruments = Instrument.find();
        return ok(instrumentManagement.render(dir, filename, da_uri, instruments));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri) {
        return index(dir, filename, da_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result viewInstrument(String dir, String filename, String da_uri, String instrument_uri) {
    	try {
    	    instrument_uri = java.net.URLDecoder.decode(instrument_uri,"UTF8");
    	} catch (Exception e) {
    	}    	
    	Instrument instrument = Instrument.find(instrument_uri);
        return ok(viewInstrument.render(dir, filename, da_uri, instrument));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postViewInstrument(String dir, String filename, String da_uri, String instrument_uri) {
        return viewInstrument(dir, filename, da_uri, instrument_uri);
    }

}