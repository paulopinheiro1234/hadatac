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

public class PermissionQueries {

    public static final String PERMISSION_BY_EMAIL       = "PermissionByEmail";
    public static final String PERMISSIONS_BY_URI        = "PermissionsByURI";
    
    public static String querySelector(String concept, String uri){
        // default query?
        String q = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        switch (concept){
            case PERMISSION_BY_EMAIL : 
                q = "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
            		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                    "SELECT * WHERE { " + 
                    "   ?uri a foaf:Person . " + 
                    "   ?uri foaf:mbox \"" + uri + "\" . " +
                    "}";
                break;
            case PERMISSIONS_BY_URI : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?deturi ?detModel ?sp ?ec ?ecName WHERE { " + 
                    "   <" + uri + "> a vstoi:Deployment . " + 
                    "   <" + uri + "> hasneto:hasDetector ?deturi .  " +
                    "   ?deturi a ?detModel . " +
                    "   ?sp vstoi:perspectiveOf ?detModel . " +
                    "   ?sp hasneto:hasPerspectiveCharacteristic ?ec ." +
                    "   ?ec rdfs:label ?ecName .  " + 
                    "}";
                break;
            default :
            	q = "";
            	System.out.println("WARNING: no query for tab " + concept);
        }// /switch
        return q;
    } // /querySelector

    public static String exec(String concept, String uri) {
	    String collection = Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL);
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
