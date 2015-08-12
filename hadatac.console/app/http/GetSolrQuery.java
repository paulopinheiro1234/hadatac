package http;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;


import models.Query;
import models.SpatialQuery;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetSolrQuery {

	public TreeMap<String, StringBuffer> list_of_queries = new TreeMap<String, StringBuffer>();
    public StringBuffer solr_query = new StringBuffer();
    public TreeMap<String, String> collection_urls = new TreeMap<String, String>();
    
    public GetSolrQuery () {} 

    
    //list_of_queries contains all the queries to execute
    //this.solr_query will be a query to return all documents in the last collection of
    //collection_urls.
    //this.solr_query should NOT BE USED OUTSIDE OF THIS CLASS UNLESS YOU KNOW WHAT YOU'RE DOING
    //I'm mostly talking to myself here.
    public GetSolrQuery (Query query) {
    	addCollectionUrls();
    	
    	for (String collection : collection_urls.keySet()){
    		this.solr_query = new StringBuffer();
    		this.solr_query.append(collection_urls.get(collection));
            this.solr_query.append("&q=*:*");
            
            String quote = new String();
    		try {
    			quote = URLEncoder.encode("\"", "UTF-8");
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
            
            
            for (String field_facet_category : query.field_facets.facets.keySet()){
                for (String field_facet : query.field_facets.facets.get(field_facet_category).keySet()){
                    this.solr_query.append(String.format("&fq=%s:%s%s%s", field_facet_category.replace(" ", "%20"), quote,
                    		field_facet.replace(" ", "%20"), quote));
                }
            }
            
            this.list_of_queries.put(collection, this.solr_query);
    	}
        //System.out.println("Final Solr Query");
        //System.out.println(this.solr_query.toString());
    }// /getSolrQuery for NoSQL
    
    //Preconditions: None
    //Inputs: None
    //Outputs: None
    //Postconditions: The collection_urls field will be populated with all current collections
    //				  in the SOLR server, and their associated base URLs.
    public void addCollectionUrls(){
    	//TODO Replace this method with something that dynamically checks the SOLR database for all collections
    	//This will require a string formatting approach, and for a password to be asked for at the start of use (to access lidarsonar)
    	
    	collection_urls.put("collection1", "http://jeffersontest.tw.rpi.edu/solr/collection1/select?wt=json");
    	collection_urls.put("collection2", "http://jeffersontest.tw.rpi.edu/solr/collection2/select?wt=json");
    	collection_urls.put("datasets", "http://jeffersontest.tw.rpi.edu/solr/datasets/select?wt=json");
    	//collection_urls.put("metadata", "http://jeffersontest.tw.rpi.edu/solr/metadata/select?wt=json");
    	collection_urls.put("wikimapia", "http://jeffersontest.tw.rpi.edu/solr/wikimapia/select?wt=json");
    	String lidarsonar = String.format("http://%s:%s@jeffersontest.tw.rpi.edu/solr/lidarsonar/select?wt=json", 
    			                           play.mvc.Controller.session("username"), play.mvc.Controller.session("password"));
    	collection_urls.put("lidarsonar", lidarsonar);
    }// /addCollectionUrls()
    
    //Preconditions: The GetSolrQuery object has been initialized by a Query object
    //Inputs: The named location and the predicate associated with it.
    //Output: Returns this object to allow for a builder design pattern to be applied.
    //		  Currently does not handle http errors (e.g 404) very well. Need to fix.
    //		  Especially to handle permission denied responses.
    //Postconditions: The member string solr_query is modified to contain the spatial filters.
    //				  It will contain the filters of the last query. Also modified is the 
    //				  list_of_queries member variable.
    public GetSolrQuery addSpatialComponent(String named_geographic_location, String spatial_predicate) {
    	//Right now (4-24-15) the lidarsonar collection is the only collection with associate lats and longs
    	//Other collections will return no results. Eventually this will change though.
    	for (String collection : this.list_of_queries.keySet()){
    		if (named_geographic_location != null){
            	if (named_geographic_location.length() > 0){
            		solr_query = new StringBuffer(this.list_of_queries.get(collection).toString());
                	//Get the polygon associated with the name
                	StringBuffer spatial_query = new StringBuffer();
                	String json = new String();
                	String polygon_string = new String();
                	try {
                		spatial_query.append("http://jeffersontest.tw.rpi.edu/solr/wikimapia/select?q=location_name");
                		spatial_query.append(URLEncoder.encode(":\"", "UTF-8"));
                		spatial_query.append(URLEncoder.encode(named_geographic_location, "UTF-8"));
                		spatial_query.append(URLEncoder.encode("\"", "UTF-8"));
                		spatial_query.append("&wt=json");
                		//System.out.println(spatial_query.toString().charAt(72));
                		
                	} catch (Exception e){
                		e.printStackTrace();
                	}
                	try
                    {
                    	HttpClient client = new DefaultHttpClient();
                    	
                    	System.out.println(spatial_query.toString());
                    	HttpGet request = new HttpGet(spatial_query.toString().replace(" ", "%20"));
                    	HttpResponse response = client.execute(request);
              
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");

                        json = writer.toString();
                        
                    } catch (IllegalStateException e) {
        				e.printStackTrace();
        			} catch (IOException e) {
        				e.printStackTrace();
        			}
                	
                	ObjectMapper mapper = new ObjectMapper();
                	JsonNode node = null;
            		try {
            			node = mapper.readTree(json);
            		} catch (IOException e) {
            			e.printStackTrace();
            		}
                	
                	JsonNode documents = node.get("response").get("docs");
                	Iterator<JsonNode> doc_iterator = documents.iterator();
                	while (doc_iterator.hasNext()){
                		JsonNode doc = doc_iterator.next();
                		//For the SOLR query to work, the commas must be surrounded by spaces
                		polygon_string = doc.get("polygon_string").asText().replace(",", " , ");
                		String quote = new String();
                		try {
    						quote = URLEncoder.encode("\"", "UTF-8");
    					} catch (UnsupportedEncodingException e) {
    						e.printStackTrace();
    					}
                		this.solr_query.append(String.format("&fq=point:%sIsWithin(%s)%sdistErrPct=0%s", quote, polygon_string, "%20", quote));
                	}
                	this.list_of_queries.remove(collection);
                	this.list_of_queries.put(collection, this.solr_query);
                }
        	}
    	}
    	//System.out.println("The spatial query:");
    	//System.out.println(this.solr_query.toString());
    	return this;
    }// /addSpatialComponent()
    
    //Preconditions: The GetSolrQuery object has been initialized by a Query object
    //Inputs: None. Executes query based on the member string solr_query.
    //Output: Returns JSON in the form of a string. Currently does not handle http errors
    //		  very gracefully. Need to change this.
    //Postconditions: None
    public String executeQuery(String collection) throws IllegalStateException, IOException{
    	CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(this.collection_urls.get(collection));
        
        Scanner in = null;
        try
        {
        	HttpClient client = new DefaultHttpClient();
        	HttpGet request = new HttpGet(this.list_of_queries.get(collection).toString().replace(" ", "%20"));
        	HttpResponse response = client.execute(request);
            System.out.println(response);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
                        
            return writer.toString();
            
        } finally
        {
            //in.close();
            //request.close();
        }
    }// /executeQuery()
}
