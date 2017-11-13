/*
 * Copyright 2012 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import be.objectify.deadbolt.core.models.Role;

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

	@Field("role_name")
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
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_ROLES).build();
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
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_ROLES).build();
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
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_ROLES).build();
    	SolrQuery solrQuery = new SolrQuery("role_name:" + roleName);
    	
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
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_ROLES).build();
		
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
		role.roleName = doc.getFieldValue("role_name").toString();
		
		return role;
	}
}
