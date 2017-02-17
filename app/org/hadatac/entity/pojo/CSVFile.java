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
import org.hadatac.utils.State;

import play.Play;

public class CSVFile {
	@Field("owner_email")
	private String ownerEmail;
	@Field("acquisition_uri")
	private String acquisitionUri;
	@Field("file_name")
	private String fileName;
	@Field("processed")
	private boolean processed;
	@Field("upload_time")
	private String uploadTime;
	@Field("process_time")
	private String processTime;
	
	public CSVFile() {
		ownerEmail = "";
		acquisitionUri = "";
		fileName = "";
		uploadTime = "";
		processTime = "";
		processed = false;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}
	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}
	
	public String getAcquisitionUri() {
		return acquisitionUri;
	}
	public void setAcquisitionUri(String acquisitionUri) {
		this.acquisitionUri = acquisitionUri;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public boolean getProcessStatus() {
		return processed;
	}
	public void setProcessStatus(boolean processed) {
		this.processed = processed;
	}
	
	public String getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(String uploadTime) {
		this.uploadTime = uploadTime;
	}
	
	public String getProcessTime() {
		return processTime;
	}
	public void setProcessTime(String processTime) {
		this.processTime = processTime;
	}
	
	public int save() {
		try {
			SolrClient client = new HttpSolrClient(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.CSV_DATASET);
			
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] CSVFile.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public int delete() {
		try {
			SolrClient solr = new HttpSolrClient(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.CSV_DATASET);
			UpdateResponse response = solr.deleteById(this.getFileName());
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] CSVFile.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] CSVFile.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] CSVFile.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public static CSVFile convertFromSolr(SolrDocument doc) {
		CSVFile object = new CSVFile();
		object.setOwnerEmail(doc.getFieldValue("owner_email").toString());
		object.setAcquisitionUri(doc.getFieldValue("acquisition_uri").toString());
		object.setFileName(doc.getFieldValue("file_name").toString());
		object.setProcessStatus(Boolean.parseBoolean(doc.getFieldValue("processed").toString()));
		object.setUploadTime(doc.getFieldValue("upload_time").toString());
		object.setProcessTime(doc.getFieldValue("process_time").toString());
		
		return object;
	}
	
	public static List<CSVFile> find(String ownerEmail, int state) {
		SolrQuery query = new SolrQuery();
		if (state == State.PROCESSED) {
			query.set("q", "owner_email:\"" + ownerEmail + "\"" + " AND " + "processed:\"true\"");
		}
		else if (state == State.UNPROCESSED) {
			query.set("q", "owner_email:\"" + ownerEmail + "\"" + " AND " + "processed:\"false\"");
		}
		query.set("rows", "10000000");
		
		return findByQuery(query);
	}
	
	public static List<CSVFile> findByQuery(SolrQuery query) {
		List<CSVFile> list = new ArrayList<CSVFile>();
		
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.CSV_DATASET);

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
			System.out.println("[ERROR] CSVFile.find(SolrQuery) - Exception message: " + e.getMessage());
		}
		
		return list;
	}
	
	public static List<CSVFile> findAll(int state) {
		SolrQuery query = new SolrQuery();
		if (state == State.PROCESSED) {
			query.set("q", "processed:\"true\"");
		}
		else if (state == State.UNPROCESSED) {
			query.set("q", "processed:\"false\"");
		}
		query.set("rows", "10000000");
		
		return findByQuery(query);
	}
	
	public static CSVFile findByName(String ownerEmail, String fileName) {		
		SolrQuery query = new SolrQuery();
		query.set("q", "owner_email:\"" + ownerEmail + "\"" + " AND " + "file_name:\"" + fileName + "\"");
		query.set("rows", "10000000");
		
		List<CSVFile> results = findByQuery(query);
		if (!results.isEmpty()) {
			return results.get(0);
		}
		
		return null;
	}
}
