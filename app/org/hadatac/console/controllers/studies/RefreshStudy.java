package org.hadatac.console.controllers.studies;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import play.mvc.Controller;
import play.mvc.Result;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.controllers.studies.routes;
import org.hadatac.console.controllers.studies.DeleteStudy;
import org.hadatac.console.views.html.studies.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.entity.pojo.Study;
import org.hadatac.metadata.loader.TripleProcessing;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.State;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class RefreshStudy extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String study_uri) {
        try {
            if (study_uri != null) {
            	study_uri = URLDecoder.decode(study_uri, "UTF-8");
            } else {
            	study_uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (study_uri.equals("")) {
        	return badRequest("Invalid study URI!");
        }
        
        DeleteStudy.deleteStudy(DynamicFunctions.replaceURLWithPrefix(study_uri));
        
        String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = ConfigProp.getPropertyValue("labkey.config", "folder");
        
    	if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
    		return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
    				routes.RefreshStudy.index(study_uri).url()));
    	}
        
    	String results = "";
    	int nTriples = 0;
    	try {
    		Model model = TripleProcessing.importStudy(site, session().get("LabKeyUserName"), 
    				session().get("LabKeyPassword"), path, study_uri);
    		DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(
    				Collections.getCollectionsName(Collections.METADATA_GRAPH));
    		accessor.add(model);
    		
    		Model refModel = Study.findModel(ValueCellProcessing.replaceNameSpaceEx(study_uri));
    		
    		results += Feedback.println(Feedback.WEB, "Imported Triples: ");
    		StmtIterator iter = model.listStatements();
    		while (iter.hasNext()) {
    			Statement stmt = iter.nextStatement();
    			if (!refModel.contains(stmt)) {
    				System.out.println(stmt.toString());
    			}
    			results += Feedback.println(Feedback.WEB, stmt.toString());
    			nTriples++;
    		}
    		
    		TripleProcessing.importDataAcquisition(site, session().get("LabKeyUserName"), 
    				session().get("LabKeyPassword"), path, ValueCellProcessing.replacePrefixEx(study_uri));
    		
    	} catch (CommandException e) {
    		if (e.getMessage().equals("Unauthorized")) {
    			return ok(syncLabkey.render("login_failed",
    					org.hadatac.console.controllers.studies.routes.StudyManagement.index().url(), ""));
    		}
    	}
    	
    	return ok(refreshStudy.render(results, nTriples));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String study_uri) {
        return index(study_uri);
    }
}
