package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import play.Play;

public class Credential {
	@Field("username")
	private String userName;
	@Field("password")
	private String password;
	
	public Credential() {
		userName = "";
		password = "";
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public int save() {
		try {
			SolrClient client = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.LABKEY_CREDENTIAL).build();
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] Credential.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public int delete() {
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.LABKEY_CREDENTIAL).build();
			UpdateResponse response = solr.deleteById(this.getUserName());
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Credential.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Credential.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Credential.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public static Credential convertFromSolr(SolrDocument doc) {
		Credential object = new Credential();
		object.setUserName(doc.getFieldValue("username").toString());
		object.setPassword(doc.getFieldValue("password").toString());
		
		return object;
	}
	
	public static List<Credential> findByQuery(SolrQuery query) {
		List<Credential> list = new ArrayList<Credential>();
		
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.LABKEY_CREDENTIAL).build();

		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			Iterator<SolrDocument> i = results.iterator();
			while (i.hasNext()) {
				list.add(convertFromSolr(i.next()));
			}
		} catch (Exception e) {
			list.clear();
			System.out.println("[ERROR] Credential.find(SolrQuery) - Exception message: " + e.getMessage());
		}
		
		return list;
	}
	
	public static Credential find() {
		SolrQuery query = new SolrQuery();
		query.set("q", "*:*");
		query.set("rows", "10000000");
		List<Credential> credentials = findByQuery(query);
		if (credentials.isEmpty()) {
			return null;
		}
		
		return credentials.get(0);
	}
}
