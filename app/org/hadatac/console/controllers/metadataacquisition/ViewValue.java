package org.hadatac.console.controllers.metadataacquisition;

import java.io.UnsupportedEncodingException;

import org.hadatac.entity.pojo.Subject;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.entity.pojo.Measurement;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.deployments.*;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.utils.Collections;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewValue extends Controller {

	
	public static List<Measurement> findValue(String user_uri, String study_uri, String subject_uri, String char_uri) {
		
		AcquisitionQueryResult result = new AcquisitionQueryResult();
		
		result = Measurement.find(user_uri, study_uri, subject_uri, char_uri);
		
		return result.documents;
	}

	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index(String user_uri, String study_uri, String subject_uri, String char_uri) {

		List<Measurement> indicatorValueResults = findValue(user_uri, study_uri, subject_uri, char_uri);
    	
    	
    	return ok(viewValue.render(indicatorValueResults));
    
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex(String user_uri, String study_uri, String subject_uri, String char_uri) {

		return index(user_uri, study_uri, subject_uri, char_uri);
	}

}