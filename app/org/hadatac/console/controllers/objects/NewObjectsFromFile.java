package org.hadatac.console.controllers.objects;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.ObjectCollectionType;
import org.hadatac.entity.pojo.StudyObjectType;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.objects.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.models.NewObjectsFromFileForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.http.GetSparqlQuery;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.controllers.objects.routes;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import java.nio.charset.StandardCharsets;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class NewObjectsFromFile extends Controller {
    
    static final long MAX_OBJECTS = 1000;
    static final long LENGTH_CODE = 6;

    private static String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String filename, String da_uri, String oc_uri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
	ObjectCollection oc = ObjectCollection.find(oc_uri);
	Study study = oc.getStudy();
	
        Form<NewObjectsFromFileForm> form = Form.form(NewObjectsFromFileForm.class).bindFromRequest();
        NewObjectsFromFileForm data = form.get();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        
	// store new values
	System.out.println("============================ NEW OBJECTS =======================");
	System.out.println("Study: [" + study.getUri() + "]");
	System.out.println("OC: [" + oc.getUri() + "]");
	System.out.println("collection: [" + data.getCollection() + "]");
	System.out.println("position: [" + data.getPosition() + "]");
	System.out.println("filename: [" + filename + "]");
	
	String newPosition = data.getPosition();
	int position = Integer.parseInt(newPosition);
	String newLabelPrefix = "";

	long nextId = study.getLastId() + 1;
	System.out.println("nextId : " + nextId);

	ObjectCollectionType ocType = ObjectCollectionType.find(oc.getTypeUri());
	File  toUse = new File(path_unproc + filename);
	int rowCount = 0;
	String newURI = "";
	String newType = "http://semanticscience.org/resource/Human";
	String newLabel = "";
	String newComment = "";
	String newObjectCollectionUri = ValueCellProcessing.replacePrefixEx(oc_uri);
	StudyObject obj = null;

	System.out.println("fileName: " + filename);
	try{
	    CSVParser parser = CSVParser.parse(toUse, StandardCharsets.UTF_8, CSVFormat.RFC4180.withHeader());
	    int recordSize;
	    Iterator it = parser.iterator();
	    CSVRecord currentRow;
	    List<String> scopeUris = new ArrayList<String>();
	    while(it.hasNext()){

		// retrieve original ID
		rowCount++;
		currentRow = (CSVRecord)it.next();

		// adjust object property values
		newURI = oc.getUri().replace("OC-",ocType.getAcronym() + "-") + "-" + formattedCounter(nextId);
		newURI = ValueCellProcessing.replacePrefixEx(newURI);
		if (newLabelPrefix == null || newLabelPrefix.equals("")) {
		    newLabel = Long.toString(nextId);
		} else {
		    newLabel = newLabelPrefix + " " + nextId;
		}
		newComment = newLabel;		

		// insert current state of the OBJ
		obj = new StudyObject(newURI,
				      newType,
				      currentRow.get(position), // Original ID
				      newLabel,
				      newObjectCollectionUri,
				      newComment,
				      scopeUris 
				      );
	    
		// insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
		obj.save();
		
		// update/create new OBJ in LabKey
		int nRowsAffected = obj.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
		    System.out.println("[ERROR] Failed to insert new OBJ to LabKey!");
		}
		oc.getObjectUris().add(obj.getUri());
	    
		nextId++;
	    }
	    parser.close();
	} catch(IOException e) {
	    e.printStackTrace();
	}
	
	String message = "Total objects created: " + rowCount;
	return ok(objectConfirm.render(message, filename, da_uri, study.getUri(), oc_uri));
    }
    
    static private String formattedCounter (long value) {
	String strLong = Long.toString(value).trim();
	for (int i=strLong.length(); i < LENGTH_CODE; i++) {
	    strLong = "0" + strLong;
	}
	return strLong;
    }
    
}
