package org.hadatac.entity.pojo;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.utils.CollectionUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.typesafe.config.ConfigFactory;

public class ConsoleStore {
	@Field("id")
	private int id;
	
	@Field("last_dynamic_metadata_id_long")
	private long lastDynamicMetadataId;
	
	@Field("timestamp_str")
	private String timestamp;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getLastDynamicMetadataId() {
		return lastDynamicMetadataId;
	}
	public void setLastDynamicMetadataId(long lastDynamicMetadataId) {
		this.lastDynamicMetadataId = lastDynamicMetadataId;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public int save() {
		try {
			SolrClient client = new HttpSolrClient.Builder(
					ConfigFactory.load().getString("hadatac.solr.data") 
					+ CollectionUtil.CONSOLE_STORE).build();
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] ConsoleStore.save(SolrClient) - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public static ConsoleStore find() {
		ConsoleStore consoleStore = null;
		
		SolrClient client = new HttpSolrClient.Builder(
				ConfigFactory.load().getString("hadatac.solr.data") 
				+ CollectionUtil.CONSOLE_STORE).build();
        SolrQuery query = new SolrQuery();
        query.set("q", "*:*");
        query.set("sort", "last_dynamic_metadata_id_long desc");
        query.set("start", "0");
        query.set("rows", "1");
        try {
        	QueryResponse response = client.query(query);
            client.close();
            SolrDocumentList list = response.getResults();
            Iterator<SolrDocument> iter = list.iterator();
            if (iter.hasNext()) {
            	SolrDocument document = iter.next();
            	consoleStore = new ConsoleStore();
            	consoleStore.setId(Integer.parseInt(document.getFieldValue("id").toString()));
            	consoleStore.setLastDynamicMetadataId(Long.parseLong(document.getFieldValue("last_dynamic_metadata_id_long").toString()));
            	consoleStore.setTimestamp(document.getFieldValue("timestamp_str").toString());
            }
        } catch (SolrServerException | IOException e) {
        	System.out.println("[ERROR] ConsoleStore.find() - e.Message: " + e.getMessage());
        }
		
		return consoleStore;
	}
}
