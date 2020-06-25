package org.hadatac.console.controllers.schema;

import org.hadatac.utils.ConfigProp;
import org.hadatac.console.http.GetSparqlQuery;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;
import javax.inject.Inject;

import org.hadatac.console.views.html.schema.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.DataAcquisitionSchemaForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.schema.NewDAS;
import org.hadatac.console.controllers.annotator.FileProcessing;

public class NewDAS extends Controller {

    public static final String kbPrefix = ConfigProp.getKbPrefix();

    @Inject
    private FormFactory formFactory;

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
    public Result index() {
        
        return ok(newDAS.render());
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex() {
        return index();
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm() {
        Form<DataAcquisitionSchemaForm> form = formFactory.form(DataAcquisitionSchemaForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        DataAcquisitionSchemaForm data = form.get();

        String label = data.getLabel();
        DataAcquisitionSchema das = DataFactory.createDataAcquisitionSchema(label);

        das.save();
        return ok(DASConfirm.render("New Data Acquisition Schema", 
                String.format("Rows have been inserted in Table \"DataAcquisitionSchema\" \n"),data.getLabel()));
     }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processFormFromFile(String attributes) {
    	
        DataAcquisitionSchema das = new DataAcquisitionSchema();

        return ok(editDAS.render(das));
       
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processFormFromFileLabels(String attributes) {

    	DataAcquisitionSchema das = new DataAcquisitionSchema();
    	    	
        return ok(editDAS.render(das));
    }
}
