package org.hadatac.console.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

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
}
