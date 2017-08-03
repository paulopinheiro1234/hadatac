package org.hadatac.console.controllers.indicators;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.StudyForm;
import org.hadatac.console.views.html.indicators.*;
import org.hadatac.entity.pojo.Indicator;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DeleteIndicator extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String ind_uri) {

        Indicator indicator = null;
        String result = "";
        try {
            if (ind_uri != null) {
            	ind_uri = URLDecoder.decode(ind_uri, "UTF-8");
            } else {
            	ind_uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	
        if (!ind_uri.equals("")) {
	    
            indicator = Indicator.find(ind_uri);
	    
            indicator.delete();

            result = deleteIndicator(DynamicFunctions.replaceURLWithPrefix(ind_uri));
            
            return ok(deleteIndicator.render(indicator, result));
        }
        
        return ok(deleteIndicator.render(indicator, result));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String ind_uri) {
        return index(ind_uri);
	}

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static String deleteIndicator(String ind_uri) {
    	String result = "";
		NameSpaces.getInstance();
//	MetadataContext metadata = new MetadataContext("user", "password", 
//						       Play.application().configuration().getString("hadatac.solr.triplestore"), false);
//	result = metadata.cleanIndicator(Feedback.WEB, indicatorUri);
		Indicator newIndicator = new Indicator(ind_uri);
		newIndicator.delete();
		return result;
    } 
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String ind_uri) {
        Indicator indicator = null;
	
        try {
            if (ind_uri != null) {
            	ind_uri = URLDecoder.decode(ind_uri, "UTF-8");
            } else {
            	ind_uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
	
        if (!ind_uri.equals("")) {
            indicator = Indicator.find(ind_uri);
        }
	
        indicator.delete();
	
	return ok(indicatorConfirm.render("Delete Indicator", indicator));
    }
}
