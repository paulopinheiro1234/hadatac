package org.hadatac.console.controllers.lookup;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.FormFactory;

import java.util.List;

import javax.inject.Inject;

import org.hadatac.console.models.ObjectCollectionForm;
import org.hadatac.console.models.OneStringFieldForm;
import org.hadatac.console.views.html.lookup.*;
import org.hadatac.console.views.html.metadata.empirical.*;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.GenericInstance;
import org.hadatac.entity.pojo.HADatAcClass;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class URILookup extends Controller {

	@Inject
	private FormFactory formFactory;
	
	public Result index(String mode, String message) {
        return ok(uriLookup.render(mode, message)); 
    }

    public Result postIndex(String mode, String message) {
        return index(mode, message);
    }

    public Result processForm(String mode) {

    	Form<OneStringFieldForm> form = formFactory.form(OneStringFieldForm.class).bindFromRequest();
    	OneStringFieldForm data = form.get();

    	if (form.hasErrors()) {
    		return ok(uriLookup.render(mode, "Input value has errors"));
    	}

    	String newURI = null;
    	if (data.getField() == null || data.getField().equals("")) {
    		return ok(uriLookup.render(mode, "Input value cannot be empty"));
    	} else {
    		newURI = data.getField();
    	}

    	if (!URIUtils.isValidURI(newURI)) {
    		return ok(uriLookup.render(mode, "Input value is not a valid URI or is not based on a registered namespace"));
    	}
    	
		newURI = URIUtils.replacePrefixEx(newURI);

		//System.out.println("Input value: [" + data.getField() + "]   " + newURI);
    	
    	GenericInstance thingInstance = GenericInstance.find(newURI);
    	
    	if (thingInstance != null) {
    		Platform pt = Platform.find(thingInstance.getUri());
    		if (pt != null) {
    			return ok(viewPlatform.render("","","",pt));
    		} else {
    			Instrument it = Instrument.find(thingInstance.getUri());
    			if (it != null) {
        			return ok(viewInstrument.render("","","",it));
    			} else {
    				Detector dt = Detector.find(thingInstance.getUri());
    				if (dt != null) {
    	    			return ok(viewDetector.render("","","",dt));
    				} else {
    		    		return ok(uriLookupInstanceResponse.render(mode, thingInstance));
    				}
    			}
    		}
    	}

    	HADatAcClass thingClass = new HADatAcClass(HADatAcThing.OWL_THING);
    	thingClass = thingClass.findGeneric(newURI);

    	if (thingClass != null) {
        	return ok(uriLookupClassResponse.render(mode, thingClass));
    	}

    	return ok(uriLookup.render(mode, "Could not find such URI in the knowledge graph"));

    }


}
