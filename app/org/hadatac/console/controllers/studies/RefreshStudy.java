package org.hadatac.console.controllers.studies;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.riot.resultset.ResultSetReaderFactory;
import org.apache.jena.shared.NotFoundException;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.studies.routes;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.StudyForm;
import org.hadatac.console.views.html.studies.*;
import org.hadatac.console.views.html.triplestore.syncLabkey;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Study;
import org.hadatac.metadata.loader.TripleProcessing;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.State;
import org.labkey.remoteapi.CommandException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
    		
    		ValueCellProcessing cellProc = new ValueCellProcessing();
    		Model refModel = Study.findModel(cellProc.replaceNameSpaceEx(study_uri));
    		
    		results += Feedback.println(Feedback.WEB, "Imported Triples: ");
    		StmtIterator iter = model.listStatements();
    		while (iter.hasNext()) {
    			String stmt = iter.nextStatement().toString();
    			results += Feedback.println(Feedback.WEB, stmt);
    			nTriples++;
    		}
    		
    		String ref_results = "";
    		StmtIterator iterref = refModel.listStatements();
    		int nRefTriples = 0;
    		while (iterref.hasNext()) {
    			String stmt = iterref.nextStatement().toString();
    			ref_results += stmt;
    			nRefTriples++;
    		}
    		System.out.println(nRefTriples + " Ref Triples!");
    		
    		try {
    			File file = new File("/Users/jason/Desktop/ref_results.txt");
    			BufferedWriter output = new BufferedWriter(new FileWriter(file));
    			output.write(ref_results);
    		} catch (IOException e) {
    			System.out.println("Invalid file path!re");
			}
    		
    		if (refModel.containsAll(model)) {
    			System.out.println("refModel.containsAll(model)!");
    		}
    		else if (model.containsAll(refModel)) {
    			System.out.println("model.containsAll(refModel)!");
    		}
    		
    	} catch (CommandException e) {
    		if (e.getMessage().equals("Unauthorized")) {
    			return ok(syncLabkey.render("login_failed",
    					org.hadatac.console.controllers.studies.routes.StudyManagement.index(
    							State.ACTIVE).url(), ""));
    		}
    	}
    	
    	return ok(refreshStudy.render(results, nTriples));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String study_uri) {
        return index(study_uri);
    }
}
