package org.hadatac.console.controllers.annotator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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
import org.hadatac.utils.Feedback;

import play.Play;

public class AnnotationLog {
	@Field("file_name")
	private String file_name = "";
	@Field("log")
	private String log = "";
	
	public AnnotationLog() {}
	
	public AnnotationLog(String file_name) {
		this.file_name = file_name;
	}

	public String getFileName() {
		return file_name;
	}
	public void setFileName(String file_name) {
		this.file_name = file_name;
	}
	
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}
	
	public void resetLog() {
		this.log = "";
	}
	
	public void addline(String new_line) {
		this.log += (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + " " + new_line;
	}
	
	public int save() {
		try {
			SolrClient client = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data")
					+ Collections.ANNOTATION_LOG).build();
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] AnnotationLog.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public int save(SolrClient solr) {
		try {
			int status = solr.addBean(this).getStatus();
			solr.commit();
			solr.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] AnnotationLog.save(SolrClient) - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public static void printException(Exception exception, String fileName) {
		AnnotationLog log = AnnotationLog.find(fileName);
        if (null == log) {
        	log = new AnnotationLog();
        	log.setFileName(fileName);
        }
        log.addline(Feedback.println(Feedback.WEB, "[ERROR] " + exception.getMessage()));
        log.save();
	}
	
	public static AnnotationLog convertFromSolr(SolrDocument doc) {
		AnnotationLog annotation_log = new AnnotationLog();
		if(doc.getFieldValue("file_name") != null){
			annotation_log.setFileName(doc.getFieldValue("file_name").toString());
		}
		if(doc.getFieldValue("log") != null){
			annotation_log.setLog(doc.getFieldValue("log").toString());
		}

		return annotation_log;
	}
	
	public static AnnotationLog find(String file_name) {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.ANNOTATION_LOG).build();
		SolrQuery query = new SolrQuery();
		query.set("q", "file_name:\"" + file_name + "\"");
		query.set("rows", "10000000");
		
		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			Iterator<SolrDocument> i = results.iterator();
			if (i.hasNext()) {
				AnnotationLog log = convertFromSolr(i.next());
				return log;
			}
		} catch (Exception e) {
			System.out.println("[ERROR] AnnotationLog.find(String) - Exception message: " + e.getMessage());
		}
	
		return null;
	}
	
	public static int delete(String file_name) {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.ANNOTATION_LOG).build();
		try {	
			UpdateResponse response = solr.deleteByQuery("file_name:\"" + file_name + "\"");
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] AnnotationLog.delete(String) - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] AnnotationLog.delete(String) - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] AnnotationLog.delete(String) - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
}

