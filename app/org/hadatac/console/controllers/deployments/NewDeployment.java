package org.hadatac.console.controllers.deployments;

import org.hadatac.console.http.GetSparqlQuery;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;

import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.deployments.*;
import org.hadatac.console.controllers.deployments.routes;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;

public class NewDeployment extends Controller {

    @Inject
    FormFactory formFactory;

    public static SparqlQueryResults getQueryResults(String tabName) {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults thePlatforms = null;
        String query_json = null;
        try {
            query_json = query_submit.executeQuery(tabName);
            thePlatforms = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            e1.printStackTrace();
        }
        return thePlatforms;
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String type, String dir, String filename, String da_uri, Integer page) {

        if (type.equalsIgnoreCase("regular")) {
            return ok(newDeployment.render(
                    Platform.find(),
                    Instrument.findAvailable(),
                    Detector.findAvailable(),
                    type,
                    dir, 
                    filename, 
                    da_uri,
                    page));
        }
        else if (type.equalsIgnoreCase("legacy")) {
            return ok(newDeployment.render(
                    Platform.find(),
                    Instrument.find(),
                    Detector.find(),
                    type,
                    dir,
                    filename,
                    da_uri,
                    page));
        }

        return badRequest("Invalid deployment type!");
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String type, String dir, String filename, String da_uri, Integer page) {
        return index(type, dir, filename, da_uri, page);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm(String dir, String filename, String da_uri, Integer page) {
        final SysUser user = AuthApplication.getLocalUser(session());
        
        Form<DeploymentForm> form = formFactory.form(DeploymentForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        DeploymentForm data = form.get();

        String dateStringFromJs = data.getStartDateTime();
        String dateString = "";
        DateFormat jsFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
        Date dateFromJs;
        try {
            dateFromJs = jsFormat.parse(dateStringFromJs);
            DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateString = isoFormat.format(dateFromJs);
        } catch (ParseException e) {
            return badRequest("Cannot parse data " + dateStringFromJs);
        }

        String deploymentUri = data.getUri();
        deploymentUri = URIUtils.replacePrefixEx(deploymentUri);
        Deployment deployment = DataFactory.createDeployment(deploymentUri, data.getPlatform(), 
                data.getInstrument(), data.getDetectors(), dateString, data.getType());

        int nRowsOfDeployment = 0;
        int nRowsOfDA = 0;

        if (da_uri != null && !da_uri.equals("")) {
            /* 
             *
             *   DEPLOYMENT INFO IS ADDED TO EXISTING AND INCOMPLETE DATA ACQUISITION 
             *
             */

            STR da = STR.findByUri(da_uri);
            if (da == null) {
                return badRequest("Data acquisition " + da_uri + " provided by unable to be loaded");
            }
            System.out.println("NewDeployment: Loading existing DA : [" + da_uri + "]");
            da.setDeploymentUri(deployment.getUri());
            deployment.saveToTripleStore();
            da.save();
        } else {
            /* 
             *
             *   NEW DATA ACQUISITION IS CREATED 
             *
             */

            int triggeringEvent;
            if (data.getType().equalsIgnoreCase("LEGACY")) {
                triggeringEvent = TriggeringEvent.LEGACY_DEPLOYMENT;
            } else {
                triggeringEvent = TriggeringEvent.INITIAL_DEPLOYMENT;
            }
            String dataAcquisitionUri = data.getDataAcquisitionUri();
            if (dataAcquisitionUri == null || dataAcquisitionUri.equals("")) {
                return badRequest("Failed to insert Deployment!\n"
                        + "Error Message: No URI for for DA");
            }
            dataAcquisitionUri = URIUtils.replacePrefixEx(dataAcquisitionUri);
            String param = data.getInitialParameter();
            System.out.println("NewDeployment: Creating new DA : [" + dataAcquisitionUri + "]");
            STR dataAcquisition = DataFactory.createDataAcquisition(
                    triggeringEvent, dataAcquisitionUri, deploymentUri, 
                    param, UserManagement.getUriByEmail(user.getEmail()));

            System.out.println("NewDeployment: Showing DA: " + dataAcquisition);
            deployment.saveToTripleStore();
            dataAcquisition.save();
        }
        
        return ok(deploymentConfirm.render("New Deployment created.", data, dir, filename, da_uri, page));
    }
}
