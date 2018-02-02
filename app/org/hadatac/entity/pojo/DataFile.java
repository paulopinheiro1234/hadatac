package org.hadatac.entity.pojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;

import com.typesafe.config.ConfigFactory;

public class DataFile {
	
	// Process status for auto-annotator
	public static final String UNPROCESSED = "UNPROCESSED";
	public static final String PROCESSED = "PROCESSED";
	
	// Process status for downloader
	public static final String CREATING = "CREATING";
	public static final String CREATED 	= "CREATED";
	
	@Field("file_name")
	private String fileName;
	@Field("owner_email_str")
	private String ownerEmail;
	@Field("acquisition_uri_str")
	private String dataAcquisitionUri;
	@Field("dataset_uri_str")
	private String datasetUri;
	@Field("status_str")
	private String status;
	@Field("completion_percentage_int")
	private int completionPercentage;
	@Field("submission_time_str")
	private String submissionTime;
	@Field("completion_time_str")
	private String completionTime;
	
	public DataFile(String fileName) {
		this.fileName = fileName;
		ownerEmail = "";
		dataAcquisitionUri = "";
		datasetUri = "";
		submissionTime = "";
		completionTime = "";
		status = "";
		completionPercentage = 0;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}
	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}
	
	public String getDataAcquisitionUri() {
		return dataAcquisitionUri;
	}
	public void setDataAcquisitionUri(String dataAcquisitionUri) {
		this.dataAcquisitionUri = dataAcquisitionUri;
	}
	
	public String getDatasetUri() {
		return datasetUri;
	}
	public void setDatasetUri(String datasetUri) {
		this.datasetUri = datasetUri;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public int getCompletionPercentage() {
		return completionPercentage;
	}
	public void setCompletionPercentage(int completionPercentage) {
		this.completionPercentage = completionPercentage;
	}
	
	public String getSubmissionTime() {
		return submissionTime;
	}
	public void setSubmissionTime(String submissionTime) {
		this.submissionTime = submissionTime;
	}
	
	public String getCompletionTime() {
		return completionTime;
	}
	public void setCompletionTime(String completionTime) {
		this.completionTime = completionTime;
	}
	
	public int save() {
		try {
			SolrClient client = new HttpSolrClient.Builder(
					ConfigFactory.load().getString("hadatac.solr.data") 
					+ Collections.CSV_DATASET).build();
			
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] DataFile.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public int delete() {
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					ConfigFactory.load().getString("hadatac.solr.data") 
					+ Collections.CSV_DATASET).build();
			UpdateResponse response = solr.deleteById(this.getFileName());
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] DataFile.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] DataFile.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] DataFile.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public static DataFile convertFromSolr(SolrDocument doc) {
		DataFile object = new DataFile(doc.getFieldValue("file_name").toString());
		
		object.setOwnerEmail(doc.getFieldValue("owner_email_str").toString());
		object.setDataAcquisitionUri(ValueCellProcessing.replaceNameSpaceEx(doc.getFieldValue("acquisition_uri_str").toString()));
		object.setDatasetUri(doc.getFieldValue("dataset_uri_str").toString());
		object.setStatus(doc.getFieldValue("status_str").toString());
		object.setCompletionPercentage(Integer.parseInt(doc.getFieldValue("completion_percentage_int").toString()));
		object.setSubmissionTime(doc.getFieldValue("submission_time_str").toString());
		object.setCompletionTime(doc.getFieldValue("completion_time_str").toString());
		
		return object;
	}
	
	public static List<DataFile> findByQuery(SolrQuery query) {
		List<DataFile> list = new ArrayList<DataFile>();
		
		SolrClient solr = new HttpSolrClient.Builder(
				ConfigFactory.load().getString("hadatac.solr.data") 
				+ Collections.CSV_DATASET).build();

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
			System.out.println("[ERROR] DataFile.find(SolrQuery) - Exception message: " + e.getMessage());
		}
		
		return list;
	}
	
	public static List<DataFile> find(String ownerEmail, String status) {
		if (status == UNPROCESSED || status == PROCESSED || status == CREATING || status == CREATED) {
			SolrQuery query = new SolrQuery();
			query.set("q", "owner_email_str:\"" + ownerEmail + "\"" + " AND " + "status_str:\"" + status + "\"");
			query.set("rows", "10000000");
			return findByQuery(query);
		}
		else {
			return new ArrayList<DataFile>();
		}
	}
	
	public static List<DataFile> findAll(String status) {
		if (status == UNPROCESSED || status == PROCESSED || status == CREATING || status == CREATED) {
			SolrQuery query = new SolrQuery();
			query.set("q", "status_str:\"" + status + "\"");
			query.set("rows", "10000000");
			return findByQuery(query);
		}
		else {
			return new ArrayList<DataFile>();
		}
	}
	
	public static DataFile findByName(String ownerEmail, String fileName) {		
		SolrQuery query = new SolrQuery();
		if (null == ownerEmail) {
			query.set("q", "file_name:\"" + fileName + "\"");
		}
		else {
			query.set("q", "owner_email_str:\"" + ownerEmail + "\"" + " AND " + "file_name:\"" + fileName + "\"");
		}
		query.set("rows", "10000000");
		
		List<DataFile> results = findByQuery(query);
		if (!results.isEmpty()) {
			return results.get(0);
		}
		
		return null;
	}
	
	public static boolean search(String fileName, List<DataFile> pool) {
		for (DataFile file : pool) {
			if (file.getFileName().equals(fileName)) {
				return true;
			}
		}
		return false;
	}

	public static void includeUnrecognizedFiles(String path, List<DataFile> ownedFiles) {		
		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
		}

		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".csv")) {
				if (!search(listOfFiles[i].getName(), ownedFiles)) {
					DataFile newFile = new DataFile(listOfFiles[i].getName());
					newFile.save();
					ownedFiles.add(newFile);
				}
			}
		}
	}

	public static void filterNonexistedFiles(String path, List<DataFile> files) {
		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
		}

		File[] listOfFiles = folder.listFiles();
		Iterator<DataFile> iterFile = files.iterator();
		while (iterFile.hasNext()) {
			DataFile file = iterFile.next();
			boolean isExisted = false;
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					if(file.getFileName().equals(listOfFiles[i].getName())) {
						isExisted = true;
						break;
					}
				}
			}
			if (!isExisted) {
				iterFile.remove();
			}
		}
	}

	public static List<File> findFilesByExtension(String path, String ext) {
		List<File> results = new ArrayList<File>();

		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
		}

		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() 
					&& FilenameUtils.getExtension(listOfFiles[i].getName()).equals(ext)) {
				results.add(listOfFiles[i]);
			}
		}
		return results;
	}
}
