package org.hadatac.console.controllers.studies;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.studies.*;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.Agent;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.objectcollections.OCForceFieldGraph;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.SysUser;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class StudyViewGraph extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String study_uri, boolean autoRefresh, int maxObjPerSOC) {

        StudyForceFieldGraph graph = new StudyForceFieldGraph(study_uri, maxObjPerSOC);        
        
        return ok(studyViewGraph.render(study_uri, graph, autoRefresh, maxObjPerSOC));
    }

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String study_uri, boolean autoRefresh, int maxObjPerSOC) {
		return index(study_uri, autoRefresh, maxObjPerSOC);
	}
}
