package org.hadatac.console.controllers.annotator;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.GetSparqlQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.TripleDocument;

import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.views.html.error_page;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.STR;

import javax.inject.Inject;

public class Annotator extends Controller {
    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result selectDeployment() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        String tabName = "Deployments";
        String query_json = null;

        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            if (query_json != null && !query_json.equals("")) {
                theResults = new SparqlQueryResults(query_json, false);
            } else {
                theResults = null;
            }
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
        }
        //return ok(selectDeployment.render(theResults));
        return ok();

    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postSelectDeployment() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        String tabName = "Deployments";
        String query_json = null;
        try {
            query_json = query_submit.executeQuery(tabName);
            if (query_json != null && !query_json.equals("")) {
                theResults = new SparqlQueryResults(query_json, false);
            } else {
                theResults = null;
            }
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
        }
        //return ok(selectDeployment.render(theResults));
        return ok();
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result uploadCSV(String uri,Http.Request request) {
        CSVAnnotationHandler handler;
        try {
            if (uri != null) {
                uri = URLDecoder.decode(uri, "UTF-8");
            } else {
                uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("uploadCSV: uri is " + uri);
        if (!uri.equals("")) {

            /*
             *  Add deployment information into handler
             */
            String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, uri);
            SparqlQueryResults results = new SparqlQueryResults(json, false);
            TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
            handler = new CSVAnnotationHandler(uri, docDeployment.get("platform"), docDeployment.get("instrument"));

            /*
             * Add possible detector's characterisitcs into handler
             */
            String dep_json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_CHARACTERISTICS_BY_URI, uri);
            System.out.println(dep_json);
            SparqlQueryResults results2 = new SparqlQueryResults(dep_json, false);
            Iterator<TripleDocument> it = results2.sparqlResults.values().iterator();
            Map<String,String> deploymentChars = new HashMap<String,String>();
            TripleDocument docChar;
            while (it.hasNext()) {
                docChar = (TripleDocument) it.next();
                if (docChar != null && docChar.get("char") != null && docChar.get("charName") != null) {
                    deploymentChars.put((String)docChar.get("char"),(String)docChar.get("charName"));
                }
            }
            handler.setDeploymentCharacteristics(deploymentChars);

            /*
             * Add URI of active datacollection in handler
             */
            STR dc = DataFactory.getActiveDataAcquisition(uri);
            if (dc != null && dc.getUri() != null) {
                handler.setDataAcquisitionUri(dc.getUri());
            }
        } else {
            handler = new CSVAnnotationHandler(uri, "", "");
        }

        return ok(uploadCSV.render(handler, "init","",application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postUploadCSV(String uri, Http.Request request) {

        CSVAnnotationHandler handler;
        try {
            if (uri != null) {
                uri = URLDecoder.decode(uri, "UTF-8");
            } else {
                uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!uri.equals("")) {

            /*
             *  Add deployment information into handler
             */
            String json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_BY_URI, uri);
            SparqlQueryResults results = new SparqlQueryResults(json, false);
            TripleDocument docDeployment = results.sparqlResults.values().iterator().next();
            handler = new CSVAnnotationHandler(uri, docDeployment.get("platform"), docDeployment.get("instrument"));

            /*
             * Add possible detector's characterisitcs into handler
             */
            String dep_json = DeploymentQueries.exec(DeploymentQueries.DEPLOYMENT_CHARACTERISTICS_BY_URI, uri);
            System.out.println(dep_json);
            SparqlQueryResults results2 = new SparqlQueryResults(dep_json, false);
            Iterator<TripleDocument> it = results2.sparqlResults.values().iterator();
            Map<String,String> deploymentChars = new HashMap<String,String>();
            TripleDocument docChar;
            while (it.hasNext()) {
                docChar = (TripleDocument) it.next();
                if (docChar != null && docChar.get("char") != null && docChar.get("charName") != null) {
                    deploymentChars.put((String)docChar.get("char"),(String)docChar.get("charName"));
                    System.out.println("EC: " + docChar.get("char") + "   ecName: " + docChar.get("charName"));
                }
            }
            handler.setDeploymentCharacteristics(deploymentChars);

            /*
             * Add URI of active datacollection in handler
             */
            STR dc = DataFactory.getActiveDataAcquisition(uri);
            if (dc != null && dc.getUri() != null) {
                handler.setDataAcquisitionUri(dc.getUri());
            }
        } else {
            handler = new CSVAnnotationHandler(uri, "", "");
        }

        return ok(uploadCSV.render(handler, "init","",application.getUserEmail(request)));
    }
}