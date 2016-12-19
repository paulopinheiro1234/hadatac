package org.hadatac.entity.pojo;

import java.io.IOException;
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import play.Play;

public class ConsoleStore {
	@Field("id")
	private int id;
	
	@Field("last_dynamic_metadata_id")
	private long lastDynamicMetadataId;
	
	@Field("timestamp")
	private DateTime timestamp;
	
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
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(timestamp);
	}
	public void setTimestamp(String timestamp) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.timestamp = formatter.parseDateTime(timestamp);
	}
	
	public int save() {
		try {
			SolrClient client = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/console_store");
			this.timestamp = DateTime.now();
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
		
		SolrClient client = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/console_store");
        SolrQuery parameters = new SolrQuery();
        parameters.set("q", "*:*");
        parameters.set("sort", "last_dynamic_metadata_id desc");
        parameters.set("start", "0");
        parameters.set("rows", "1");
        QueryResponse response;
        try {
            response = client.query(parameters);
            client.close();
            SolrDocumentList list = response.getResults();
            Iterator<SolrDocument> i = list.iterator();
            if (i.hasNext()) {
            	DateTime date;
            	SolrDocument document = i.next();
            	consoleStore = new ConsoleStore();
            	consoleStore.setId(Integer.parseInt(document.getFieldValue("id").toString()));
            	consoleStore.setLastDynamicMetadataId(Long.parseLong(document.getFieldValue("last_dynamic_metadata_id").toString()));
            	date = new DateTime((Date)document.getFieldValue("timestamp"));
            	consoleStore.setTimestamp(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
            }
        } catch (SolrServerException | IOException e) {
        	System.out.println("[ERROR] ConsoleStore.find() - e.Message: " + e.getMessage());
        }
		
		return consoleStore;
	}
}
