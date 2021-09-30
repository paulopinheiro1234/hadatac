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
import org.hadatac.console.providers.AuthUser;
import org.hadatac.console.providers.MyAuthUserIdentity;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;
import org.hadatac.console.providers.MyUsernamePasswordAuthUser;
import org.hadatac.utils.CollectionUtil;
import org.noggit.JSONUtil;

//import org.hadatac.console.providers.AuthUser;


public class LinkedAccount {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public Long id;

	@Field("id")
	public String id_s;

	@Field("provider_user_id_str")
    public String providerUserId;

	@Field("user_id_str")
	public String userId;

	@Field("provider_key_str")
	public String providerKey;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
	    this.userId = userId;
	}

	public static LinkedAccount findByProviderKey(final SysUser user, String key) {
		return findByProviderKeySolr(user, key);
	}

	public static LinkedAccount findByProviderKeySolr(final SysUser user, String key) {
		LinkedAccount account = null;
		SolrClient solrClient = new HttpSolrClient.Builder(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.AUTHENTICATE_ACCOUNTS)).build();
    	SolrQuery solrQuery = new SolrQuery("user_id_str:" + user.getId() + " AND provider_key_str:" + key);

    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				account = convertSolrDocumentToLinkedAccount(list.get(0));
				account.setUserId(user.getId());
			}
		} catch (Exception e) {
			System.out.println("[ERROR] LinkedAccount.findByProviderKeySolr - Exception message: " + e.getMessage());
		}

    	return account;
	}

	public static List<LinkedAccount> findByIdSolr(final SysUser user) {
		List<LinkedAccount> accounts = new ArrayList<LinkedAccount>();
		SolrClient solrClient = new HttpSolrClient.Builder(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.AUTHENTICATE_ACCOUNTS)).build();
//		System.out.println("solrClient: "+solrClient);
    	SolrQuery solrQuery = new SolrQuery("user_id_str:" + user.getId());
//    	System.out.println("solrQuery: "+solrQuery);

    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
//			System.out.println("queryResponse: "+queryResponse);

			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
//			System.out.println("queryResponse: "+queryResponse);
			Iterator<SolrDocument> i = list.iterator();

			while (i.hasNext()) {
				LinkedAccount account = convertSolrDocumentToLinkedAccount(i.next());
				account.setUserId(user.getId());
				accounts.add(account);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] LinkedAccount.findByIdSolr - Exception message: " + e.getMessage());
		}

    	return accounts;
	}

	public static List<LinkedAccount> findByProviderUserIdSolr(String providerUserId) {
        List<LinkedAccount> accounts = new ArrayList<LinkedAccount>();
        SolrClient solrClient = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.AUTHENTICATE_ACCOUNTS)).build();
        SolrQuery solrQuery = new SolrQuery("provider_user_id_str:" + providerUserId);

        try {
            QueryResponse queryResponse = solrClient.query(solrQuery);
            solrClient.close();
            SolrDocumentList list = queryResponse.getResults();
            Iterator<SolrDocument> i = list.iterator();

            while (i.hasNext()) {
                LinkedAccount account = convertSolrDocumentToLinkedAccount(i.next());
                accounts.add(account);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ERROR] LinkedAccount.findByProviderUserIdSolr - Exception message: " + e.getMessage());
        }

        return accounts;
    }

	public static String outputAsJson() {
		SolrClient solrClient = new HttpSolrClient.Builder(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.AUTHENTICATE_ACCOUNTS)).build();
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
	//TODO: test
	public static LinkedAccount create(final MyUsernamePasswordAuthUser authUser) {
		final LinkedAccount ret = new LinkedAccount();
		ret.id_s = UUID.randomUUID().toString();
		ret.update(authUser);
		return ret;
	}
//TODO : original
	public static LinkedAccount create(final AuthUser authUser) {
		final LinkedAccount ret = new LinkedAccount();
		ret.id_s = UUID.randomUUID().toString();
		ret.update(authUser);
		return ret;
	}

//TODO : test
public void update(final MyUsernamePasswordAuthUser authUser) {
	this.providerKey = authUser.getProvider();
	this.providerUserId = authUser.getId();
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
		ret.userId = acc.providerUserId;

		return ret;
	}

	public void save() {
		SolrClient solrClient = new HttpSolrClient.Builder(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.AUTHENTICATE_ACCOUNTS)).build();
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
			        CollectionUtil.getCollectionPath(CollectionUtil.Collection.AUTHENTICATE_ACCOUNTS)).build();
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
		account.providerUserId = doc.getFieldValue("provider_user_id_str").toString();
		account.providerKey = doc.getFieldValue("provider_key_str").toString();
		account.userId = doc.getFieldValue("user_id_str").toString();

		return account;
	}
}