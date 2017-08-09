package org.hadatac.console.controllers.deployments;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.GetSparqlQuery;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.hadatac.console.models.URIGeneratorForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.TreeQuery;

import play.Play;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.deployments.*;
import org.hadatac.utils.Collections;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class URIGenerator extends Controller {
	
	// for /metadata HTTP GET requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result index() {

       return ok(uriGenerator.render(Form.form(URIGeneratorForm.class)));
        
    }// /index()


    // for /metadata HTTP POST requests
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postIndex() {
        
        return ok(uriGenerator.render(Form.form(URIGeneratorForm.class)));
        
    }// /postIndex()

    public static long getNextId() {
        SolrClient client = new HttpSolrClient.Builder(
        		Play.application().configuration().getString("hadatac.solr.data")
        		+ Collections.URI_GENERATOR).build();
        SolrQuery parameters = new SolrQuery();
        parameters.set("q", "*:*");
        parameters.set("sort", "generated_id desc");
        parameters.set("start", "0");
        parameters.set("rows", "1");
        parameters.set("fl", "generated_id");
        QueryResponse response;
        try {
            response = client.query(parameters);
            SolrDocumentList list = response.getResults();
            Iterator<SolrDocument> i = list.iterator();
            if (i.hasNext()) {
                long id = Long.parseLong(i.next().getFieldValue("generated_id").toString());
                return ++id;
            }
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    } 
    
    /**
     * Handles the form submission.
     */
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result processForm() {
        Form<URIGeneratorForm> form = Form.form(URIGeneratorForm.class).bindFromRequest();
        URIGeneratorForm data = form.get();
    	System.out.println("Owner URI " + data.getOwnerURI());
    	System.out.println("Description " + data.getDescription());
    	
    	// Capture the date and time
    	Date date = new Date();
    	data.setDateTime(new Date());
    	
    	// Generate the next URI Generator's ID
    	data.setGeneratedID(getNextId());
    	
    	http://localhost:8983/solr/uri_generator/select?q=*%3A*&sort=generated_id+desc&start=0&rows=1&fl=generated_id&wt=json     	
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(uriGenerator.render(form));        
        } else {
            return ok(uriGeneratorConfirm.render(data));
        }
    }

}
