package org.hadatac.entity.pojo;

import java.io.File;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class SSD {
	private Map<String, String> mapCatalog = new HashMap<String, String>();
	private Map<String, List<String>> mapContent = new HashMap<String, List<String>>();
	private RecordFile ssdfile = null;
	
	public SSD(RecordFile file) {
		this.ssdfile = file;
		readCatalog(file);
		readContent(file);
	}
	
	public String getNameFromFileName() {
	    return (ssdfile.getFile().getName().split("\\.")[0]).replace("_", "-").replace("SSD-", "");
    }
	
	public String getFileName() {
	    return ssdfile.getFile().getName();
    }
	
	public Map<String, String> getCatalog() {
		return mapCatalog;
	}
	
	public Map<String, List<String>> getMapContent() {
		return mapContent;
	}
	
	private void readCatalog(RecordFile file) {
	    if (!file.isValid()) {
            return;
        }
	    
	    for (Record record : file.getRecords()) {
	        mapCatalog.put(record.getValueByColumnIndex(1), record.getValueByColumnIndex(0));
	    }
	}
	
	private void readContent(RecordFile file) {
	    if (!file.isValid()) {
            return;
        }
	    
	    for (Record record : file.getRecords()) {
	    	List<String> tmp = new ArrayList<String>();
	    	tmp.add(record.getValueByColumnName("hasURI"));
	    	tmp.add(record.getValueByColumnName("type"));
	    	tmp.add(record.getValueByColumnName("hasScope"));
	    	tmp.add(record.getValueByColumnName("hasTimeScope"));
	    	tmp.add(record.getValueByColumnName("role"));
	        mapContent.put(record.getValueByColumnName("hasURI"), tmp);
	    }
	}
	
	public File downloadFile(String fileURL, String fileName) {
		if(fileURL == null || fileURL.length() == 0){
			return null;
		} else {
			try {
				URL url = new URL(fileURL);
				File file = new File(fileName);
				FileUtils.copyURLToFile(url, file);
				return file;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public boolean checkCellValue(String str) {
		if(str.contains(",")){
			return false;
		}
		if(str.contains(" ")){
			return false;
		}
		return true;
	}
}
	