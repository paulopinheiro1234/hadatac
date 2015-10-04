package org.hadatac.data.loader;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;

import play.Play;

public class DataContext {
	
	String username;
	String password;
	String kbURL;
	boolean verbose;
	
	public DataContext(String un, String pwd, String kb, boolean ver) {
        System.out.println("Metadata management set for knowledge base at " + kb);
	    username = un;
	    password = pwd;
	    kbURL = kb;
	    verbose = ver;
    }

	public static Long playTotalMeasurements() {
		DataContext data = new DataContext( "user", 
				"password",
				Play.application().configuration().getString("hadatac.solr.data"), 
				false);
		
		return data.totalMeasurements();
	}
	
	public static Long playTotalDataCollections() {
		DataContext data = new DataContext( "user", 
				"password",
				Play.application().configuration().getString("hadatac.solr.data"), 
				false);
		
		return data.totalDataCollections();
	}
	
	public Long totalMeasurements() {
		SolrClient solr = new HttpSolrClient(kbURL + "/measurement");
		SolrQuery parameters = new SolrQuery();
		parameters.set("q", "*:*");
		parameters.set("rows", 0);
		
		try {
			QueryResponse response = solr.query(parameters);
			solr.close();
			return response.getResults().getNumFound();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return (long) -1;
	}
	
	public Long totalDataCollections() {
		SolrClient solr = new HttpSolrClient(kbURL + "/sdc");
		SolrQuery parameters = new SolrQuery();
		parameters.set("q", "*:*");
		parameters.set("rows", 0);
		
		try {
			QueryResponse response = solr.query(parameters);
			solr.close();
			return response.getResults().getNumFound();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return (long) -1;
	}
}
