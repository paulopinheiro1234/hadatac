package org.hadatac.console.controllers.schema;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;
import play.twirl.api.Html;

import org.hadatac.console.views.html.schema.*;
import org.hadatac.console.views.html.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.schema.*;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DataAcquisitionSchemaForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.DASAForm;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.Entity;
import org.hadatac.entity.pojo.Attribute;
import org.hadatac.entity.pojo.Unit;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class EditDASA extends Controller {
	
    // for /metadata HTTP GET requests
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String dasa_uri) {
	
    	DataAcquisitionSchemaAttribute dasa = null;
    	try {
	    if (dasa_uri != null) {
		dasa_uri = URLDecoder.decode(dasa_uri, "UTF-8");
	    } else {
		dasa_uri = "";
	    }
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	
    	if (!dasa_uri.equals("")) {
	    System.out.println("Requested URI: <" + dasa_uri + ">");
	    dasa = DataAcquisitionSchemaAttribute.find(dasa_uri);
	    System.out.println("Retreived URI: <" + dasa.getUri() + ">");
    	} else {
            return badRequest("No URI is provided to retrieve DASA");
    	}
    	return ok(editDASA.render(dasa, EditingOptions.getEntities(), EditingOptions.getAttributes(), EditingOptions.getUnits()));
	
    }// /index()

    
    // for /metadata HTTP POST requests
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String dasa_uri) {
	
    	DataAcquisitionSchemaAttribute dasa = null;
    	try {
	    if (dasa_uri != null) {
		dasa_uri = URLDecoder.decode(dasa_uri, "UTF-8");
	    } else {
		dasa_uri = "";
	    }
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	
    	if (!dasa_uri.equals("")) {
	    System.out.println("Requested URI: <" + dasa_uri + ">");
	    dasa = DataAcquisitionSchemaAttribute.find(dasa_uri);
	    System.out.println("Retreived URI: <" + dasa.getUri() + ">");
    	} else {
            return badRequest("No URI is provided to retrieve DASA");
	}
    	return ok(editDASA.render(dasa, EditingOptions.getEntities(), EditingOptions.getAttributes(), EditingOptions.getUnits()));
		  
    }// /postIndex()

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String dasaUri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
        Form<DASAForm> form = Form.form(DASAForm.class).bindFromRequest();
        DASAForm data = form.get();
        List<String> changedInfos = new ArrayList<String>();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        
	String newEntity = getUriFromNew(data.getNewEntity());
	String newAttribute = getUriFromNew(data.getNewAttribute());
	String newUnit = getUriFromNew(data.getNewUnit());

        DataAcquisitionSchemaAttribute dasa = DataAcquisitionSchemaAttribute.find(dasaUri);
        if (data.getNewUri() != null) {
            if (!data.getNewUri().equals("")) {
                if (DataAcquisitionSchemaAttribute.find(data.getNewUri()) != null) {
		    return badRequest("DASA with this uri already exists!");
                }
                
            	// Create new DASA
            	dasa.setUri(data.getNewUri());
            	dasa.setEntity(newEntity);
		dasa.setAttribute(newAttribute);
		dasa.setUnit(newUnit);
            	
            	try {
		    int nRowsAffected = dasa.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		    dasa.save();
		    return ok(main.render("Results,", "", new Html("<h3>" 
								   + String.format("%d row(s) have been inserted in Table \"DASA\"", nRowsAffected) 
								   + "</h3>")));
		} catch (CommandException e) {
		    return badRequest("Failed to insert new DASA to LabKey!\n"
				      + "Error Message: " + e.getMessage());
		}
            }
        }
        
        // Update current DASA
	if (dasa.getEntity() == null || !dasa.getEntity().equals(newEntity)) {
	    dasa.setEntity(newEntity);
	    changedInfos.add(newEntity);
	}

	if (dasa.getAttribute() == null || !dasa.getAttribute().equals(newAttribute)) {
	    dasa.setAttribute(newAttribute);
	    changedInfos.add(newAttribute);
	}

	if (dasa.getUnit() == null || !dasa.getUnit().equals(newUnit)) {
	    dasa.setUnit(newUnit);
	    changedInfos.add(newUnit);
	}
	
	if (!changedInfos.isEmpty()) {
	    dasa.save();
	}
	
	return ok(editDASAConfirm.render(dasa, changedInfos));
	
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postProcessForm(String dasaUri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
        Form<DASAForm> form = Form.form(DASAForm.class).bindFromRequest();
        DASAForm data = form.get();
        List<String> changedInfos = new ArrayList<String>();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        
	String newEntity = getUriFromNew(data.getNewEntity());
	String newAttribute = getUriFromNew(data.getNewAttribute());
	String newUnit = getUriFromNew(data.getNewUnit());

        DataAcquisitionSchemaAttribute dasa = DataAcquisitionSchemaAttribute.find(dasaUri);
        if (data.getNewUri() != null) {
            if (!data.getNewUri().equals("")) {
                if (DataAcquisitionSchemaAttribute.find(data.getNewUri()) != null) {
		    return badRequest("DASA with this uri already exists!");
                }
                
            	// Create new DASA
            	dasa.setUri(data.getNewUri());
            	dasa.setEntity(newEntity);
		dasa.setAttribute(newAttribute);
		dasa.setUnit(newUnit);
            	
            	try {
		    int nRowsAffected = dasa.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		    dasa.save();
		    return ok(main.render("Results,", "", new Html("<h3>" 
								   + String.format("%d row(s) have been inserted in Table \"DASA\"", nRowsAffected) 
								   + "</h3>")));
		} catch (CommandException e) {
		    return badRequest("Failed to insert new DASA to LabKey!\n"
				      + "Error Message: " + e.getMessage());
		}
            }
        }
        
        // Update current DASA
	if (dasa.getEntity() == null || !dasa.getEntity().equals(newEntity)) {
	    dasa.setEntity(newEntity);
	    changedInfos.add(newEntity);
	}

	if (dasa.getAttribute() == null || !dasa.getAttribute().equals(newAttribute)) {
	    dasa.setAttribute(newAttribute);
	    changedInfos.add(newAttribute);
	}

	if (dasa.getUnit() == null || !dasa.getUnit().equals(data.getNewUnit())) {
	    dasa.setUnit(newUnit);
	    changedInfos.add(newUnit);
	}
	
	if (!changedInfos.isEmpty()) {
	    dasa.save();
	}
	
	return ok(editDASAConfirm.render(dasa, changedInfos));
	
    }

    private static String getUriFromNew(String newStr) {
	if (newStr == null) {
	    return "";
	}
	String response = newStr.substring(newStr.indexOf("[") + 1).replace("]","");
	//response = ValueCellProcessing.replacePrefix(response);
	return response;
    }


}
