package org.hadatac.entity.pojo;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.metadata.loader.URIUtils;

public class SDD {
	private Map<String, String> mapCatalog = new HashMap<String, String>();
	private Map<String, String> codeMappings = new HashMap<String, String>();
	private Map<String, String> mapAttrObj = new HashMap<String, String>();
	private Map<String, Map<String, String>> codebook = new HashMap<String, Map<String, String>>();
	private Map<String, List<String>> timelineMap = new HashMap<String, List<String>>();
	private RecordFile file = null;
	
	public SDD(RecordFile file) {
		this.file = file;
		readCatalog(file);
	}
	
	public String getName() {
		return (file.getFile().getName().split("\\.")[0]).replace("_", "-").replace("SDD-", "");
	}
	
	public Map<String, String> getCatalog() {
		return mapCatalog;
	}
	
	public Map<String, String> getCodeMapping() {
		return codeMappings;
	}
	
	public Map<String, String> getMapAttrObj() {
		return mapAttrObj;
	}
	
	public Map<String, Map<String, String>> getCodebook() {
		return codebook;
	}
	
	public Map<String, List<String>> getTimeLineMap() {
		return timelineMap;
	}
	
	private void readCatalog(RecordFile file) {
	    if (!file.isValid()) {
            return;
        }
	    
	    for (Record record : file.getRecords()) {
	        mapCatalog.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
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
		
	public void readDataDictionary(RecordFile file) {
		if (!file.isValid()) {
			return;
		}
		
		for (Record record : file.getRecords()) {
			mapAttrObj.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(2));
		}
		System.out.println("mapAttrObj: " + mapAttrObj);
	}
	
	public void readCodeMapping(RecordFile file) {
		if (!file.isValid()) {
			return;
		}
		
		for (Record record : file.getRecords()) {
			codeMappings.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
		}
	}
	
	public void readCodebook(RecordFile file) {
		if (!file.isValid()) {
			return;
		}
		
		for (Record record : file.getRecords()) {
			if (!record.getValueByColumnIndex(0).isEmpty()) {
				String colName = record.getValueByColumnIndex(0);
				Map<String, String> mapCodeClass = null;
				if (!codebook.containsKey(colName)) {
					mapCodeClass = new HashMap<String, String>();
					codebook.put(colName, mapCodeClass);
				} else {
					mapCodeClass = codebook.get(colName);
				}
				String classUri = "";
				if (!record.getValueByColumnIndex(3).isEmpty()) {
					// Class column
					classUri = URIUtils.replacePrefixEx(record.getValueByColumnIndex(3));
				} 
//					else {
//						// Resource column
//						classUri = URIUtils.replacePrefixEx(record.get(4));
//					}
				mapCodeClass.put(record.getValueByColumnIndex(1), classUri);
			}
		}
	}
	
	public void readtimelineFile(RecordFile file) {
		if (!file.isValid()) {
			return;
		}
		
		for (Record record : file.getRecords()) {
			if (!record.getValueByColumnIndex(0).isEmpty()) {
				String colName = record.getValueByColumnIndex(0);
				List<String> tmpList = new ArrayList<String>();
				if (!record.getValueByColumnIndex(1).isEmpty()) {
					tmpList.add(record.getValueByColumnIndex(1));
				} else {
					tmpList.add("null");
				}
				if (!record.getValueByColumnIndex(2).isEmpty()) {
					tmpList.add(record.getValueByColumnIndex(2));
				} else {
					tmpList.add("null");
				}
				if (!record.getValueByColumnIndex(5).isEmpty()) {
					tmpList.add(record.getValueByColumnIndex(5));
				} else {
					tmpList.add("null");
				}
				System.out.println(tmpList.get(0) + " " + tmpList.get(1) + " " + tmpList.get(2));
				timelineMap.put(colName, tmpList);
			}
		}
	}
}
