package org.hadatac.console.models;

import java.util.UUID;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.utils.CollectionUtil;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.inject.Inject;

import be.objectify.deadbolt.java.models.Role;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SecurityRole implements Role {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Long id;
	
	@Field("id")
	public String id_s;

	@Field("role_name_str")
	public String roleName;

	@Override
	public String getName() {
		return roleName;
	}
	
	public static void initialize() {
		if (SecurityRole.existsSolr() == false) {
			System.out.println("SecurityRole.existsSolr() == false");
			addSecurityRole(
					org.hadatac.console.controllers.AuthApplication.DATA_OWNER_ROLE, 
					"f4251649-751e-4190-b0ed-e824f3cdd6fc");
			addSecurityRole(
					org.hadatac.console.controllers.AuthApplication.DATA_MANAGER_ROLE,
					"fdeff289-daee-4ecc-8c9c-3ef111cf7a06");			
		}
	}
	
	private static void addSecurityRole(String roleName, String id) {
		final SecurityRole role = new SecurityRole();
		role.roleName = roleName;
		role.id_s = id;
		role.save();
	}

	public static SecurityRole findByRoleName(String roleName) {
		return findByRoleNameSolr(roleName);
	}
	
	public static boolean existsSolr() {
		SolrClient solrClient = new HttpSolrClient.Builder(
				ConfigFactory.load().getString("hadatac.solr.users") 
				+ CollectionUtil.AUTHENTICATE_ROLES).build();
    	SolrQuery solrQuery = new SolrQuery("*:*");
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() > 0) {
				return true;
			}
		} catch (Exception e) {
			System.out.println("[ERROR] SecurityRole.findByIdSolr - Exception message: " + e.getMessage());
		}
    	
    	return false;
	}
	
	public static SecurityRole findByIdSolr(String id) {
		SecurityRole role = null;
		SolrClient solrClient = new HttpSolrClient.Builder(
				ConfigFactory.load().getString("hadatac.solr.users") 
				+ CollectionUtil.AUTHENTICATE_ROLES).build();
    	SolrQuery solrQuery = new SolrQuery("id:" + id);
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				role = convertSolrDocumentToSecurityRole(list.get(0));
			}
		} catch (Exception e) {
			System.out.println("[ERROR] SecurityRole.findByIdSolr - Exception message: " + e.getMessage());
		}
    	
    	return role;
	}
	
	public static SecurityRole findByRoleNameSolr(String roleName) {
		SecurityRole role = null;
		SolrClient solrClient = new HttpSolrClient.Builder(
				ConfigFactory.load().getString("hadatac.solr.users") 
				+ CollectionUtil.AUTHENTICATE_ROLES).build();
    	SolrQuery solrQuery = new SolrQuery("role_name_str:" + roleName);
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				role = convertSolrDocumentToSecurityRole(list.get(0));
			}
		} catch (Exception e) {
			System.out.println("[ERROR] SecurityRole.findByRoleNameSolr - Exception message: " + e.getMessage());
		}
    	
    	return role;
	}
	
	public void save() {
		SolrClient solrClient = new HttpSolrClient.Builder(
				ConfigFactory.load().getString("hadatac.solr.users") 
				+ CollectionUtil.AUTHENTICATE_ROLES).build();
		
		if (this.id_s == null) {
			this.id_s = UUID.randomUUID().toString();
		}
        
        try {
        	solrClient.addBean(this);
			solrClient.commit();
			solrClient.close();
		} catch (Exception e) {
			System.out.println("[ERROR] SecurityRole.save - Exception message: " + e.getMessage());
		}
	}
	
	private static SecurityRole convertSolrDocumentToSecurityRole(SolrDocument doc) {
		SecurityRole role = new SecurityRole();
		role.id_s = doc.getFieldValue("id").toString();
		role.roleName = doc.getFieldValue("role_name_str").toString();
		
		return role;
	}
}
