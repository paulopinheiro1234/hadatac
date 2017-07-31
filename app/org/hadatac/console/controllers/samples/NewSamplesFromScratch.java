package org.hadatac.console.controllers.samples;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import play.data.*;
import org.hadatac.utils.ConfigProp;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.Sample;
import org.hadatac.entity.pojo.SampleCollection;
import org.hadatac.entity.pojo.SampleType;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.samples.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.models.SamplesForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.http.GetSparqlQuery;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.controllers.samples.routes;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class NewSamplesFromScratch extends Controller {
    
    static final long MAX_SAMPLES = 1000;
    static final long LENGTH_CODE = 6;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String std_uri, String sc_uri) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
	    return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			    org.hadatac.console.controllers.samples.routes.NewSamplesFromScratch.index(std_uri, sc_uri).url()));
    	}
	
	std_uri = URLDecoder.decode(std_uri);
	sc_uri = URLDecoder.decode(sc_uri);

	Study study = Study.find(std_uri);
	SampleCollection sc = SampleCollection.find(sc_uri);

	List<SampleType> typeList = SampleType.find();

	if (typeList == null) {
	    System.out.println("new samples: type list is null");
	} else {
	    for (SampleType type : typeList) {
		System.out.println("new samples: type = [" + type.getUri() + "]");
	    }
	}
	
    	return ok(newSamplesFromScratch.render(study, sc, typeList));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String std_uri, String sc_uri) {
    	return index(std_uri, sc_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String std_uri, String sc_uri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
	Study std = Study.find(std_uri);
	SampleCollection sc = SampleCollection.find(sc_uri);
	
        Form<SamplesForm> form = Form.form(SamplesForm.class).bindFromRequest();
        SamplesForm data = form.get();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        
	// store new values
	System.out.println("============================ NEW SAMPLES =======================");
	System.out.println("Study: [" + std.getUri() + "]");
	System.out.println("SC: [" + sc.getUri() + "]");
	System.out.println("type: [" + data.getNewType() + "]");
	System.out.println("Quantity : " + data.getNewQuantity());
	
	long quantity = Long.parseLong(data.getNewQuantity());
	if ((quantity > 0) && (quantity <= MAX_SAMPLES)) {
	    std.requestId(quantity);
	}
	long nextId = std.getLastId() + 1;
	System.out.println("nextId : " + nextId);
    
	// Fixed values
	String newType = null;
	if (data.getNewType() == null || data.getNewType().equals("")) {
	    return badRequest("[ERROR] New type cannot be empty.");
	} else {
	    newType = ValueCellProcessing.replacePrefixEx(data.getNewType());
	}
	String newSampleCollectionUri = ValueCellProcessing.replacePrefixEx(sc_uri);
	
	// Variable values
	String newURI = null;
	String newLabel = null;
	String newComment = null;

	// Sample object
	Sample sp = null;
	
	if (quantity > 0) {
	    for (int i=0; i <= quantity; i++) {
		
		newURI = sc.getUri().replace("SC-","SUBJ-") + "-" + formattedCounter(nextId);
		System.out.println("newUri : [" + newURI + "]");
		newLabel = "Subject " + nextId;
		newComment = newLabel;
		
		// insert current state of the SP
		sp = new Sample(newURI,
				newType,
				"", // Original ID
				newLabel,
				newSampleCollectionUri,
				newComment,
				"" // IsSampleOf 
				);
		
		// insert the new SC content inside of the triplestore regardless of any change -- the previous content has already been deleted
		sp.save();
		
		// update/create new SP in LabKey
		int nRowsAffected = sp.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
		    return badRequest("Failed to insert new SP to LabKey!\n");
		}
		sc.getSamples().add(sp);
		nextId++;
		
	    }
	}
	String message = "A total of " + quantity + " new sample(s) have been Generated";
	return ok(sampleConfirm.render(message, std_uri, sc));
    }
    
    static private String formattedCounter (long value) {
	String strLong = Long.toString(value).trim();
	for (int i=strLong.length(); i < LENGTH_CODE; i++) {
	    strLong = "0" + strLong;
	}
	return strLong;
    }
    
}
