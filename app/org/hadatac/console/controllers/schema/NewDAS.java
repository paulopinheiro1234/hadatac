package org.hadatac.console.controllers.schema;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.views.html.schema.editDAS;
import org.pac4j.play.java.Secure;
import play.mvc.Http;
import org.hadatac.utils.ConfigProp;
import org.hadatac.console.http.GetSparqlQuery;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;
import javax.inject.Inject;

//import views.html.schema.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataAcquisitionSchema;

import org.hadatac.console.models.DataAcquisitionSchemaForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
//import controllers.AuthApplication;
import org.hadatac.console.views.html.schema.newDAS;

public class NewDAS extends Controller {

    public static final String kbPrefix = ConfigProp.getKbPrefix();

    @Inject
    private FormFactory formFactory;
    @Inject
    Application application;

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

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(Http.Request request) {
        return ok(newDAS.render(application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(Http.Request request) {
        return index(request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result processForm(Http.Request request) {
        Form<DataAcquisitionSchemaForm> form = formFactory.form(DataAcquisitionSchemaForm.class).bindFromRequest(request);
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        DataAcquisitionSchemaForm data = form.get();

        String label = data.getLabel();
        DataAcquisitionSchema das = DataFactory.createDataAcquisitionSchema(label);

        das.save();
        return ok(org.hadatac.console.views.html.schema.DASConfirm.render("New Data Acquisition Schema",
                String.format("Rows have been inserted in Table \"DataAcquisitionSchema\" \n"),data.getLabel(),application.getUserEmail(request)));
     }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result processFormFromFile(String attributes, Http.Request request) {
    	
        DataAcquisitionSchema das = new DataAcquisitionSchema();
        return ok(editDAS.render(das,application.getUserEmail(request)));
       
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result processFormFromFileLabels(String attributes,Http.Request request) {

    	DataAcquisitionSchema das = new DataAcquisitionSchema();
        return ok(editDAS.render(das,application.getUserEmail(request)));
    }
}
