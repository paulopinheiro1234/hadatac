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
import org.hadatac.utils.CollectionUtil;


public class OperationMode {
	@Field("user_email")
	private String userEmail = "";
	@Field("operation_mode_str")
	private String operationMode = "";
	@Field("last_enter_time_str")
    private String lastEnterTime = "";
	
	public static final String REGULAR = "regular";
	public static final String SANDBOX = "sandbox";
	
	public OperationMode() {}

	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	
	public String getOperationMode() {
		return operationMode;
	}
	public void setOperationMode(String operationMode) {
		this.operationMode = operationMode;
	}
	
	public String getLastEnterTime() {
        return lastEnterTime;
    }
    public void setLastEnterTime(String lastEnterTime) {
        this.lastEnterTime = lastEnterTime;
    }
	
	public int save() {		
		try {
			SolrClient client = new HttpSolrClient.Builder(
			        CollectionUtil.getCollectionPath(CollectionUtil.Collection.OPERATION_MODE)).build();
			
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] OperationMode.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public int delete() {
		try {
			SolrClient client = new HttpSolrClient.Builder(
			        CollectionUtil.getCollectionPath(CollectionUtil.Collection.OPERATION_MODE)).build();
			UpdateResponse response = client.deleteById(getUserEmail());
			client.commit();
			client.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] OperationMode.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] OperationMode.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] OperationMode.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public static OperationMode convertFromSolr(SolrDocument doc) {
		OperationMode object = new OperationMode();
		object.setUserEmail(doc.getFieldValue("user_email").toString());
		object.setOperationMode(doc.getFieldValue("operation_mode_str").toString());
		object.setLastEnterTime(doc.getFieldValue("last_enter_time_str").toString());
		
		return object;
	}
	
	public static List<OperationMode> findByQuery(SolrQuery query) {
		List<OperationMode> list = new ArrayList<OperationMode>();
		
		SolrClient solr = new HttpSolrClient.Builder(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.OPERATION_MODE)).build();

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
			System.out.println("[ERROR] OperationMode.find(SolrQuery) - Exception message: " + e.getMessage());
		}
		
		return list;
	}
	
	public static List<OperationMode> findAll() {
        SolrQuery query = new SolrQuery();
        query.set("q", "*:*");
        query.set("rows", "10000000");
        
        return findByQuery(query);
    }
	
	public static OperationMode findByEmail(String email) {
		SolrQuery query = new SolrQuery();
		query.set("q", "user_email:\"" + email + "\"");
		query.set("rows", "10000000");
		List<OperationMode> modes = findByQuery(query);
		if (modes.isEmpty()) {
			return null;
		}
		
		return modes.get(0);
	}
}
