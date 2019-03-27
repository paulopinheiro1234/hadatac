package org.hadatac.console.controllers.annotator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.hadatac.utils.Feedback;


public class AnnotationLogger {
	@Field("file_name")
	private String fileName = "";
	@Field("log_str")
	private String log = "";
	
	private AnnotationLogger() {}
	
	private AnnotationLogger(String fileName) {
		this.fileName = fileName;
	}
	
	public static Map<String, AnnotationLogger> loggers = new HashMap<String, AnnotationLogger>();
	
	public static AnnotationLogger getLogger(String name) {
	    if (loggers.containsKey(name)) {
	        return loggers.get(name);
	    } else {
	        if (loggers.size() == 50) {
	            loggers.clear();
	        }
	        AnnotationLogger logger = create(name);
	        if (!logger.getLog().isEmpty()) {
	            loggers.put(name, logger);
	        }
	        return logger;
	    }
    }

	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String file_name) {		
		if (!this.fileName.equals(file_name)) {
		    AnnotationLogger curlogger = AnnotationLogger.find(file_name);
		    if (null != curlogger) {
		        setLog(curlogger.getLog());
		    }
		}
		
		this.fileName = file_name;
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
	
	public void addline(String newLine) {
		this.log += (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + " " + newLine;
		save();
	}
	
	public int save() {
	    assert(!fileName.isEmpty());
	    
	    fileName = fileName.replace("/", "[SLASH]");
	    
		try {
			SolrClient client = new HttpSolrClient.Builder(
					CollectionUtil.getCollectionPath(CollectionUtil.Collection.ANNOTATION_LOG)).build();
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] AnnotationLogger.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public int delete() {
	    return AnnotationLogger.delete(getFileName());
    }
	
	public void printExceptionById(String id) {
	    printException(ErrorDictionary.getDetailById(id));
    }
	
	public void printExceptionByIdWithArgs(String id, Object... args) {
        printException(String.format(ErrorDictionary.getDetailById(id), args));
    }
	
	public void printException(Exception exception) {
	    addline(Feedback.println(Feedback.WEB, "[ERROR] " + exception.getMessage()));
    }
    
    public void printException(String message) {
        addline(Feedback.println(Feedback.WEB, "[ERROR] " + message));
    }
    
    public void printWarningById(String id) {
        printWarning(ErrorDictionary.getDetailById(id));
    }
    
    public void printWarningByIdWithArgs(String id, Object... args) {
        printWarning(String.format(ErrorDictionary.getDetailById(id), args));
    }
    
    public void printWarning(String message) {
        addline(Feedback.println(Feedback.WEB, "[WARNING] " + message));
    }
    
    public void println(String message) {
        addline(Feedback.println(Feedback.WEB, "[LOG] " + message));
    }
	
	public static AnnotationLogger convertFromSolr(SolrDocument doc) {
	    AnnotationLogger annotation_log = new AnnotationLogger();
		if (doc.getFieldValue("file_name") != null) {
			annotation_log.setFileName(doc.getFieldValue("file_name").toString().replace("[SLASH]", "/"));
		}
		if (doc.getFieldValue("log_str") != null) {
			annotation_log.setLog(doc.getFieldValue("log_str").toString());
		}

		return annotation_log;
	}
	
	public static AnnotationLogger find(String fileName) {
	    fileName = fileName.replace("/", "[SLASH]");
	    
		SolrClient solr = new HttpSolrClient.Builder(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.ANNOTATION_LOG)).build();
		SolrQuery query = new SolrQuery();
		query.set("q", "file_name:\"" + fileName + "\"");
		query.set("rows", "10000000");
		
		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			Iterator<SolrDocument> i = results.iterator();
			if (i.hasNext()) {
			    AnnotationLogger log = convertFromSolr(i.next());
				return log;
			}
		} catch (Exception e) {
			System.out.println("[ERROR] AnnotationLog.find(String) - Exception message: " + e.getMessage());
		}
	
		return null;
	}
	
	private static AnnotationLogger create(String fileName) {
	    AnnotationLogger log = AnnotationLogger.find(fileName);
		if (null == log) {
			log = new AnnotationLogger();
	    	log.setFileName(fileName);
		}
		
    	return log;
	}
	
	public static int delete(String fileName) {
	    fileName = fileName.replace("/", "[SLASH]");
	    
		SolrClient solr = new HttpSolrClient.Builder(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.ANNOTATION_LOG)).build();
		try {	
			UpdateResponse response = solr.deleteByQuery("file_name:\"" + fileName + "\"");
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] AnnotationLogger.delete(String) - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] AnnotationLogger.delete(String) - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] AnnotationLogger.delete(String) - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
}

