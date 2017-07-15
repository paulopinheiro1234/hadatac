package org.hadatac.console.controllers.annotator;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.console.views.html.schema.*;
import org.hadatac.console.controllers.annotator.FileProcessing;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;
import play.mvc.*;
import play.mvc.Http.*;
import play.mvc.Result;
import play.twirl.api.Html;

public class SchemaFromFile extends Controller {

    public static Result create(String file_name) {

	String path = "";
	String labels = "";

	try {
	    file_name = URLEncoder.encode(file_name, "UTF-8");
	} catch (Exception e) {
	    System.out.println("[ERROR] encoding file name");
	}
	    
	System.out.println("file <" + file_name + ">");

	//String[] fields;

	path = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
	
	System.out.println("Path: " + path + "  Name: " + file_name);
	
	//List<DataAcquisitionSchemaAttribute> attributes = new ArrayList<DataAcquisitionSchemaAttribute>();

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(path + "/" + file_name));
	    StringBuilder builder = new StringBuilder();
	    String line = reader.readLine();
	
	    while (line != null) {
		builder.append(line);
		break;
	    }
	    if(!builder.toString().trim().equals("")) {
		labels = builder.toString();
	    }
	
	} catch (Exception e) {
	    System.out.println("Could not process uploaded file.");
	}

	System.out.println("SchemaFromFile: labels = <" + labels + ">");

	return ok(newDASFromFile.render(file_name, labels));
    }
    
}

