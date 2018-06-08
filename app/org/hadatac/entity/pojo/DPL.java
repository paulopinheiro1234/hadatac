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

public class DPL {
	private Map<String, String> mapCatalog = new HashMap<String, String>();
	private RecordFile dplfile = null;
	
	public DPL(RecordFile file) {
		this.dplfile = file;
		readCatalog(file);
	}
	
	/*
	public String getName() {
	    String sddName = mapCatalog.get("Study_ID");
	    System.out.println("sddName: " + sddName);
	    if (sddName == null) {
	        return "";
	    }
	    return sddName;
	}
	*/
	
	public String getNameFromFileName() {
	    return (dplfile.getFile().getName().split("\\.")[0]).replace("_", "-").replace("DPL-", "");
    }
	
	public String getFileName() {
	    return dplfile.getFile().getName();
    }
	
	public Map<String, String> getCatalog() {
		return mapCatalog;
	}
	
	private void readCatalog(RecordFile file) {
	    if (!file.isValid()) {
            return;
        }
	    
	    for (Record record : file.getRecords()) {
	        mapCatalog.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
	    }
	}

	public boolean readSheet(RecordFile file) {

		// TODO: Add code to read arbitrary sheet
		return true;

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
	
	public boolean checkCellUriRegistered(String str) {
        String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList();
//        System.out.println(prefixString);
        if (str.contains(":")){
        	String[] split = str.split(":");
        	String prefixname = split[0];
    		if (!prefixString.contains(prefixname)){
    			return false;
    		}
    		return true;
        } else {
        	return true;
        }
	}
	
	public boolean checkCellUriResolvable(String str) {

        if (str.contains(":")){
        	if (URIUtils.isValidURI(str)){
	        	try {
	        		URIUtils.convertToWholeURI(str);
	        	} catch (Exception e) {
					return false;
				}
        	} else {
        		return false;
        	}
        }
        return true;
	}
	
}
