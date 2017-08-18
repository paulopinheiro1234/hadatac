package org.hadatac.console.controllers.studies;

import org.hadatac.console.http.GetSparqlQuery;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import play.data.*;

import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.studies.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.console.controllers.studies.routes;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.Agent;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.StudyType;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.query.SaveRowsResponse;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.StudyForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;

public class EditStudy extends Controller {
	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result index(String filename, String da_uri, String std_uri) {
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
				routes.EditStudy.index(filename, da_uri, std_uri).url()));
    	}
    	
    	Study std = null;
	StudyType stdType = null;
	List<Agent> organizations = null;
	List<Agent> persons = null;

    	try {
	    if (std_uri != null) {
		std_uri = URLDecoder.decode(std_uri, "UTF-8");
	    } else {
		std_uri = "";
	    }
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	
    	if (!std_uri.equals("")) {
	    std = Study.find(std_uri);
	    organizations = Agent.findOrganizations();
	    persons = Agent.findPersons();
	    stdType = new StudyType();

    	} else {
            return badRequest("No URI is provided to retrieve Study");
    	}

	return ok(editStudy.render(filename, da_uri, std, stdType, organizations, persons));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String filename, String da_uri, String std_uri) {
    	return index(filename, da_uri, std_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm(String std_uri) {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
        Form<StudyForm> form = Form.form(StudyForm.class).bindFromRequest();
        StudyForm data = form.get();
        List<String> changedInfos = new ArrayList<String>();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        
	// store new values
	String newURI = ValueCellProcessing.replacePrefixEx(data.getNewUri());
	if (newURI == null || newURI.equals("")) {
            return badRequest("[ERROR] New URI cannot be empty.");
	}
	String newStudyType = ValueCellProcessing.replacePrefixEx(data.getNewType());
	String newLabel = data.getNewLabel();
	String newTitle = data.getNewTitle();
	String newProject = data.getNewProject();
	String newComment = data.getNewComment();
	String newExternalSource = data.getNewExternalSource();
	String newInstitution = data.getNewInstitution();
	String newAgent = data.getNewAgent();
	String newStartDateTime = data.getNewStartDateTime();
	String newEndDateTime = data.getNewEndDateTime();

	// retrieve old Study and corresponding DAS
        Study oldStudy = Study.find(std_uri);

	// set changes
	if (oldStudy != null) {
	    
	    if (oldStudy.getUri() != null && !oldStudy.getUri().equals(newURI)) {
		changedInfos.add(newURI);
	    }
	    if (oldStudy.getType() != null && !oldStudy.getType().equals(newStudyType)) {
		changedInfos.add(newStudyType);
	    }
	    if (oldStudy.getLabel() != null && !oldStudy.getLabel().equals(newLabel)) {
		changedInfos.add(newLabel);
	    }
	    if (oldStudy.getTitle() != null && !oldStudy.getTitle().equals(newTitle)) {
		changedInfos.add(newTitle);
	    }
	    if (oldStudy.getProject() != null && !oldStudy.getProject().equals(newProject)) {
		changedInfos.add(newProject);
	    }
	    if (oldStudy.getComment() == null || !oldStudy.getComment().equals(newComment)) {
		changedInfos.add(newComment);
	    }
	    if (oldStudy.getExternalSource() == null || !oldStudy.getExternalSource().equals(newExternalSource)) {
		changedInfos.add(newExternalSource);
	    }
	    if (oldStudy.getInstitution() == null || !oldStudy.getInstitution().equals(newInstitution)) {
		changedInfos.add(newInstitution);
	    }
	    if (oldStudy.getAgent() == null || !oldStudy.getAgent().equals(newAgent)) {
		changedInfos.add(newAgent);
	    }
	    if (oldStudy.getStartedAt() == null || !oldStudy.getStartedAt().equals(newStartDateTime)) {
		changedInfos.add(newStartDateTime);
	    }
	    if (oldStudy.getEndedAt() == null || !oldStudy.getEndedAt().equals(newEndDateTime)) {
		changedInfos.add(newEndDateTime);
	    }

	    // delete previous state of the Study in the triplestore
	    if (oldStudy != null) {
		oldStudy.delete();
	    }
	} else {
	    return badRequest("[ERRO] Failed locating existing Study.\n");
	}

        // insert current state of the Study
	oldStudy.setUri(newURI);
	oldStudy.setType(newStudyType);
	oldStudy.setLabel(newLabel);
	oldStudy.setTitle(newTitle);
	oldStudy.setProject(newProject);
	oldStudy.setComment(newComment);
	oldStudy.setExternalSource(newExternalSource);
	oldStudy.setInstitutionUri(newInstitution);
	oldStudy.setAgentUri(newAgent);
	oldStudy.setStartedAt(newStartDateTime);
	oldStudy.setEndedAt(newEndDateTime);
	
	// insert the new Study content inside of the triplestore regardless of any change -- the previous content has already been deleted
	oldStudy.save();
	
	// update/create new Study in LabKey
	int nRowsAffected = oldStudy.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
	if (nRowsAffected <= 0) {
	    return badRequest("Failed to insert edited Study to LabKey!\n");
	}

        return ok(studyConfirm.render("Edit Study", oldStudy));
    }
}
