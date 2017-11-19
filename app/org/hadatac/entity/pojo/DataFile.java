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
import org.hadatac.utils.State;

import play.Play;

public class DataFile {
	@Field("owner_email")
	private String ownerEmail;
	@Field("acquisition_uri")
	private String dataAcquisitionUri;
	@Field("dataset_uri")
	private String datasetUri;
	@Field("file_name")
	private String fileName;
	@Field("processed")
	private boolean processed;
	@Field("upload_time")
	private String uploadTime;
	@Field("process_time")
	private String processTime;
	
	public DataFile() {
		ownerEmail = "";
		dataAcquisitionUri = "";
		datasetUri = "";
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
	
	public String getDataAcquisitionUri() {
		return dataAcquisitionUri;
	}
	public void setDataAcquisitionUri(String dataAcquisitionUri) {
		//System.out.println("inside DataFile's POJO DA: <" + dataAcquisitionUri + ">");
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
	        //System.out.println("inside DataFile's POJO: " + fileName);
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
			SolrClient client = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
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
					Play.application().configuration().getString("hadatac.solr.data") 
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
		DataFile object = new DataFile();
		
		object.setOwnerEmail(doc.getFieldValue("owner_email").toString());
		object.setDataAcquisitionUri(ValueCellProcessing.replaceNameSpaceEx(doc.getFieldValue("acquisition_uri").toString()));
		object.setDatasetUri(doc.getFieldValue("dataset_uri").toString());
		object.setFileName(doc.getFieldValue("file_name").toString());
		object.setProcessStatus(Boolean.parseBoolean(doc.getFieldValue("processed").toString()));
		object.setUploadTime(doc.getFieldValue("upload_time").toString());
		object.setProcessTime(doc.getFieldValue("process_time").toString());
		
		return object;
	}
	
	public static List<DataFile> find(String ownerEmail, int state) {
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
	
	public static List<DataFile> findByQuery(SolrQuery query) {
		List<DataFile> list = new ArrayList<DataFile>();
		
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
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
	
	public static List<DataFile> findAll(int state) {
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
	
	public static DataFile findByName(String ownerEmail, String fileName) {		
		SolrQuery query = new SolrQuery();
		if (null == ownerEmail) {
			query.set("q", "file_name:\"" + fileName + "\"");
		}
		else {
			query.set("q", "owner_email:\"" + ownerEmail + "\"" + " AND " + "file_name:\"" + fileName + "\"");
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
					DataFile newFile = new DataFile();
					newFile.setFileName(listOfFiles[i].getName());
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
