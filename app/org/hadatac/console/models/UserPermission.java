package org.hadatac.console.models;

import java.util.UUID;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.utils.Collections;

import play.Play;
import be.objectify.deadbolt.core.models.Permission;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
public class UserPermission implements Permission {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Long id;
	
	@Field("id")
	public String id_s;

	@Field("value")
	public String value;

	public String getValue() {
		return value;
	}

	public static UserPermission findByValue(String value) {
		return findByValueSolr(value);
	}
	
	public static UserPermission findByValueSolr(String value) {
		UserPermission permission = null;
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_PERMISSIONS).build();
    	SolrQuery solrQuery = new SolrQuery("value:" + value);
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				permission = convertSolrDocumentToUserPermission(list.get(0));
			}
		} catch (Exception e) {
			System.out.println("[ERROR] UserPermission.findByValueSolr - Exception message: " + e.getMessage());
		}
    	
    	return permission;
	}
	
	public static UserPermission findByIdSolr(String id) {
		UserPermission permission = null;
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_PERMISSIONS).build();
    	SolrQuery solrQuery = new SolrQuery("id:" + id);
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				permission = convertSolrDocumentToUserPermission(list.get(0));
			}
		} catch (Exception e) {
			System.out.println("[ERROR] UserPermission.findByIdSolr - Exception message: " + e.getMessage());
		}
    	
    	return permission;
	}
	
	public void save() {
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_PERMISSIONS).build();
		
		if (this.id_s == null) {
			this.id_s = UUID.randomUUID().toString();
		}
        
        try {
        	solrClient.addBean(this);
			solrClient.commit();
			solrClient.close();
		} catch (Exception e) {
			System.out.println("[ERROR] UserPermission.save - Exception message: " + e.getMessage());
		}
	}
	
	private static UserPermission convertSolrDocumentToUserPermission(SolrDocument doc) {
		UserPermission permission = new UserPermission();
		permission.id_s = doc.getFieldValue("id").toString();
		permission.value = doc.getFieldValue("value").toString();
		
		return permission;
	}
}
