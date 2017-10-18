package org.hadatac.console.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;


public class SolrUtils {
	
	public static boolean commitJsonDataToSolr(String solrCollection, String content) {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
		    HttpPost post = new HttpPost(solrCollection + "/update?commit=true");
		    StringEntity entity  = new StringEntity(content, "UTF-8");
		    entity.setContentType("application/json");
		    post.setEntity(entity);
		    HttpResponse response = httpClient.execute(post);
		    System.out.println(post.toString());
		    System.out.println("Content: " + content);
		    System.out.println("Status: " + response.getStatusLine().getStatusCode());
		    if (200 == response.getStatusLine().getStatusCode()) {
		    	return true;
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		return false;
	}
	
	public static String getFieldValue(SolrDocument doc, String field) {
		Object value = doc.getFieldValue(field);
		if (null != value) {
			return value.toString();
		}
		
		return "";
	}
	
	public static boolean clearCollection(String solrCollection) {
		try {
			SolrClient solr = new HttpSolrClient.Builder(solrCollection).build();
			solr.deleteByQuery("*:*");
			solr.commit();
			solr.close();
			
			return true;
		} catch (SolrServerException e) {
			System.out.println("[ERROR] SolrUtils.clearCollection() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] SolrUtils.clearCollection() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] SolrUtils.clearCollection() - Exception message: " + e.getMessage());
		}
		
		return false;
	}
}
