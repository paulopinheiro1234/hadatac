package org.hadatac.console.http;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import play.Play;
    
public class GenericSparqlQuery {

    //Inputs: None. Executes query based on the member string sparql_query.
    //Output: Returns JSON in the form of a string. Currently does not handle http errors
    //		  very gracefully. Need to change this.
    //Postconditions: None
    public static String execute(String query, boolean isUpdate) throws IllegalStateException, IOException{
    	String encodedQuery = URLEncoder.encode(query,"UTF-8");
    	System.out.println(query);
    	String final_query = Play.application().configuration().getString("hadatac.solr.triplestore") + "/store/sparql?q=" + encodedQuery;
    	System.out.println(final_query);
    	CloseableHttpClient httpClient = HttpClients.createDefault();
        Scanner in = null;
        try {
        	HttpClient client = new DefaultHttpClient(); 
        	HttpPost request = new HttpPost(final_query);
        	if (isUpdate) {
            	request.setHeader("Content-Type", "application/x-www-form-urlencoded");
        	}
            request.setHeader("Accept", "text/plain,*/*;q=0.9");
        	HttpResponse response = client.execute(request);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
            //System.out.println("response: " + response);    
            return writer.toString();
        } finally {
            //in.close();
            //request.close();
        }
    } // /executeQuery()
}
