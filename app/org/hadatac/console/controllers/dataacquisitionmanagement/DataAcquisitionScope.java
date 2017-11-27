package org.hadatac.console.controllers.dataacquisitionmanagement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.FileProcessing;
import org.hadatac.console.controllers.dataacquisitionmanagement.routes;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Study;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DataAcquisitionScope extends Controller {
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result create(String file_name, String da_uri) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
	    return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
			    routes.DataAcquisitionScope.create(file_name, da_uri).url()));
    	}
	
	DataAcquisition da = null;
	DataFile file = null;
	String ownerEmail = "";
	
	// Load associated DA
	try {
	    file_name = URLEncoder.encode(file_name, "UTF-8");
	} catch (Exception e) {
	    System.out.println("[ERROR] encoding file name");
	}
	
	ownerEmail = AuthApplication.getLocalUser(session()).getEmail();
	file = DataFile.findByName(ownerEmail, file_name);
	if (file == null) {
	    return badRequest("[ERROR] Could not update file records with new DA information");
	}
	
	// Load associated DA
	if (da_uri != null && !da_uri.equals("")) {
	    da = DataAcquisition.findByUri(ValueCellProcessing.replacePrefixEx(da_uri));

	    if (da == null) {
		String message = "[ERROR] Could not load assigned DA from DA's URI : " + da_uri;
		return badRequest(message);
	    }
	}

	String[] fields = null;
	String globalScope = null;
	String globalScopeUri = null;
	List<String> localScope = null;
	List<String> localScopeUri = null;
	String labelsStr = "";
	String path = "";
	String labels = "";
	
	try {
	    file_name = URLEncoder.encode(file_name, "UTF-8");
	} catch (Exception e) {
	    System.out.println("[ERROR] encoding file name");
	}
	
	//System.out.println("file <" + file_name + ">");
	path = ConfigProp.getPathUnproc();
	//System.out.println("Path: " + path + "  Name: " + file_name);
	try {
	    BufferedReader reader = new BufferedReader(new FileReader(path + "/" + file_name));
	    StringBuilder builder = new StringBuilder();
	    String line = reader.readLine();
	    while (line != null) {
		builder.append(line);
		break;
	    }
	    if(!builder.toString().trim().equals("")) {
		labels = builder.toString();
	    }
	} catch (Exception e) {
	    System.out.println("Could not process uploaded file.");
	}
	System.out.println("selectScope: labels = <" + labels + ">");
	if (labels != null && !labels.equals("")) {
	    fields = FileProcessing.extractFields(labels);
	    localScope = new ArrayList<String>();
	    localScopeUri = new ArrayList<String>();
	    for (String str : fields) {
		localScope.add("no mapping");
                localScopeUri.add("");
	    }
	    //System.out.println("# of fields: " + fields.length);
	}
	
	Study study = Study.find(da.getStudyUri());
	//System.out.println("StudygetUri(): " + study.getUri());
	//System.out.println("Study name: " + study.getLabel());
	List<ObjectCollection> ocList = ObjectCollection.findDomainByStudy(study);
	//System.out.println("Collection list size: " + ocList.size());
	
	return ok(editScope.render(file_name, da_uri, ocList, Arrays.asList(fields), globalScope, globalScopeUri, localScope, localScopeUri));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postCreate(String file_name, String da_uri) {
    	return create(file_name, da_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result view(String file_name, String da_uri) {
	
	DataAcquisition da = null;
	DataFile file = null;
	String ownerEmail = "";
	
	// Load associated DA
	try {
	    file_name = URLEncoder.encode(file_name, "UTF-8");
	} catch (Exception e) {
	    System.out.println("[ERROR] encoding file name");
	}
	
	ownerEmail = AuthApplication.getLocalUser(session()).getEmail();
	file = DataFile.findByName(ownerEmail, file_name);
	if (file == null) {
	    return badRequest("[ERROR] Could not update file records with new DA information");
	}
	
	// Load associated DA
	if (da_uri != null && !da_uri.equals("")) {
	    da = DataAcquisition.findByUri(ValueCellProcessing.replacePrefixEx(da_uri));

	    if (da == null) {
		String message = "[ERROR] Could not load assigned DA from DA's URI : " + da_uri;
		return badRequest(message);
	    }
	}

	List<String> localScopeUri = da.getLocalScopeUri();
	System.out.println("Size Local Scope URI: " + localScopeUri.size());
	for (String str : localScopeUri) {
	    System.out.println("  - uri : " + str);
	}
	List<String> localScopeName = da.getLocalScopeName();
	System.out.println("Size Local Scope URI: " + localScopeName.size());
	for (String str : localScopeName) {
	    System.out.println("  - name : " + str);
	}
	
	return ok(viewScope.render(file_name, da_uri, da.getGlobalScopeName(), da.getGlobalScopeUri(), localScopeName, localScopeUri));
    }
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postView(String file_name, String da_uri) {
    	return view(file_name, da_uri);
    }
    
}
