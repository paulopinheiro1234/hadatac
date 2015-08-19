package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import play.Play;

import com.feth.play.module.pa.user.AuthUser;

public class LinkedAccount extends AppModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Long id;
	
	@Field("id")
	public String id_s;

	public User user;

	@Field("provider_user_id")
	public String providerUserId;
	
	@Field("provider_key")
	public String providerKey;
	
	public String getUserId() {
		return user.id_s;
	}
	
	@Field("user_id")
	public void setUserId(String id) {
		user = User.findByIdSolr(id);
	}

	public static LinkedAccount findByProviderKey(final User user, String key) {
		return findByProviderKeySolr(user, key);
	}
	
	public static LinkedAccount findByProviderKeySolr(final User user, String key) {
		LinkedAccount account = null;
		SolrClient solrClient = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.users") + "/linked_account");
    	SolrQuery solrQuery = new SolrQuery("user_id:" + user.id_s + " AND provider_key:" + key);
    	
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
	
	public static List<LinkedAccount> findByIdSolr(final User user) {
		List<LinkedAccount> accounts = new ArrayList<LinkedAccount>(); 
		SolrClient solrClient = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.users") + "/linked_account");
    	SolrQuery solrQuery = new SolrQuery("user_id:" + user.id_s);
    	
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
		SolrClient solrClient = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.users") + "/linked_account");
		
        try {
        	solrClient.addBean(this);
			solrClient.commit();
			solrClient.close();
		} catch (Exception e) {
			System.out.println("[ERROR] LinkedAccount.save - Exception message: " + e.getMessage());
		}
	}
	
	private static LinkedAccount convertSolrDocumentToLinkedAccount(SolrDocument doc) {
		LinkedAccount account = new LinkedAccount();
		account.id_s = doc.getFieldValue("id").toString();
		account.providerUserId = doc.getFieldValue("provider_user_id").toString();
		account.providerKey = doc.getFieldValue("provider_key").toString();
		
		return account;
	}
}