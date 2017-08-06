package org.hadatac.console.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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
import org.noggit.JSONUtil;

import play.Play;

import com.feth.play.module.pa.user.AuthUser;

public class LinkedAccount {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Long id;
	
	@Field("id")
	public String id_s;

	@Field("provider_user_id")
	public String providerUserId;
	
	@Field("provider_key")
	public String providerKey;
	
	public SysUser user;
	
	public String getUserId() {
		return user.getId();
	}
	
	@Field("user_id")
	public void setUserId(String id) {
		user = SysUser.findByIdSolr(id);
	}

	public static LinkedAccount findByProviderKey(final SysUser user, String key) {
		return findByProviderKeySolr(user, key);
	}
	
	public static LinkedAccount findByProviderKeySolr(final SysUser user, String key) {
		LinkedAccount account = null;
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_ACCOUNTS).build();
    	SolrQuery solrQuery = new SolrQuery("user_id:" + user.getId() + " AND provider_key:" + key);
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				account = convertSolrDocumentToLinkedAccount(list.get(0));
				account.user = user;
			}
		} catch (Exception e) {
			System.out.println("[ERROR] LinkedAccount.findByProviderKeySolr - Exception message: " + e.getMessage());
		}
    	
    	return account;
	}
	
	public static List<LinkedAccount> findByIdSolr(final SysUser user) {
		List<LinkedAccount> accounts = new ArrayList<LinkedAccount>(); 
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users")
				+ Collections.AUTHENTICATE_ACCOUNTS).build();
    	SolrQuery solrQuery = new SolrQuery("user_id:" + user.getId());
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			Iterator<SolrDocument> i = list.iterator();
			
			while (i.hasNext()) {
				LinkedAccount account = convertSolrDocumentToLinkedAccount(i.next());
				account.user = user;
				accounts.add(account);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] LinkedAccount.findByIdSolr - Exception message: " + e.getMessage());
		}
    	
    	return accounts;
	}
	
	public static String outputAsJson() {
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users")
				+ Collections.AUTHENTICATE_ACCOUNTS).build();
		String query = "*:*";
    	SolrQuery solrQuery = new SolrQuery(query);
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList docs = queryResponse.getResults();
			return JSONUtil.toJSON(docs);
		} catch (Exception e) {
			System.out.println("[ERROR] LinkedAccount.outputAsJson - Exception message: " + e.getMessage());
		}
    	
    	return "";
	}

	public static LinkedAccount create(final AuthUser authUser) {
		final LinkedAccount ret = new LinkedAccount();
		ret.id_s = UUID.randomUUID().toString();
		ret.update(authUser);
		return ret;
	}
	
	public void update(final AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}

	public static LinkedAccount create(final LinkedAccount acc) {
		final LinkedAccount ret = new LinkedAccount();
		ret.id_s = UUID.randomUUID().toString();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;

		return ret;
	}
	
	public void save() {
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_ACCOUNTS).build();
        try {
        	solrClient.addBean(this);
			solrClient.commit();
			solrClient.close();
		} catch (Exception e) {
			System.out.println("[ERROR] LinkedAccount.save - Exception message: " + e.getMessage());
		}
	}
	
	public int delete() {
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.users") 
					+ Collections.AUTHENTICATE_ACCOUNTS).build();
			UpdateResponse response = solr.deleteById(this.id_s);
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] LinkedAccount.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] LinkedAccount.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] LinkedAccount.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	private static LinkedAccount convertSolrDocumentToLinkedAccount(SolrDocument doc) {
		LinkedAccount account = new LinkedAccount();
		account.id_s = doc.getFieldValue("id").toString();
		account.providerUserId = doc.getFieldValue("provider_user_id").toString();
		account.providerKey = doc.getFieldValue("provider_key").toString();
		
		return account;
	}
}