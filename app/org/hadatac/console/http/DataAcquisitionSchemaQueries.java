package org.hadatac.console.http;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hadatac.utils.Collections;

import play.Play;

public class DataAcquisitionSchemaQueries {

    public static final String ATTRIBUTE_BY_SCHEMA_URI                 = "AttributeBySchemaURI";
    
    public static String querySelector(String concept, String uri){
        // default query?
        String q = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        switch (concept){
            case ATTRIBUTE_BY_SCHEMA_URI : 
            	q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + 
            		"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            		"PREFIX prov: <http://www.w3.org/ns/prov#>  " +
            		"PREFIX vstoi: <http://hadatac.org/ont/vstoi#> " +
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#> " +
        			"PREFIX hasco: <http://hadatac.org/ont/hasco/> " +
        			"SELECT ?uri ?hasPosition ?hasEntity ?hasAttribute ?hasUnit ?hasSource ?isPIConfirmed WHERE { " + 
        			"   ?uri a hasneto:DataAcquisitionSchema . " + 
        			"   ?uri hasneto:partOfSchema " + "<" + uri + "> .  " + 
        			"   ?uri hasco:hasPosition ?hasPosition .  " + 
        			"   OPTIONAL { ?uri hasneto:hasEntity ?hasEntity } ." + 
        			"   OPTIONAL { ?uri hasneto:hasAttribute ?hasAttribute } ." + 
        			"   OPTIONAL { ?uri hasneto:hasUnit ?hasUnit } ." + 
        			"   OPTIONAL { ?uri hasco:hasSource ?hasSource } ." + 
        			"   OPTIONAL { ?uri hasco:isPIConfirmed ?isPIConfirmed } ." + 
        			"}";
                break;
            default :
            	q = "";
            	System.out.println("WARNING: no query for tab " + concept);
        }
        return q;
    }

    public static String exec(String concept, String uri) {
	    String collection = Collections.getCollectionsName(Collections.METADATA_SPARQL);
        StringBuffer sparql_query = new StringBuffer();
        sparql_query.append(collection);
        sparql_query.append("?q=");

        String q = querySelector(concept, uri);
        System.out.println("Query: [" + q + "]");
        try {
            sparql_query.append(URLEncoder.encode(q, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }            

        CloseableHttpClient httpClient = HttpClients.createDefault();
        Scanner in = null;
        try {
        	HttpClient client = new DefaultHttpClient();
        	HttpGet request = new HttpGet(sparql_query.toString().replace(" ", "%20"));
        	request.setHeader("Accept", "application/sparql-results+json");
        	HttpResponse response;
            StringWriter writer = new StringWriter();			
            try {
				response = client.execute(request);

                try {
		    		IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
		    	} catch (Exception e) {
			    	// TODO Auto-generated catch block
			    	e.printStackTrace();
			    } 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
            return writer.toString();
        } finally {
        }
    } // /executeQuery()
}
