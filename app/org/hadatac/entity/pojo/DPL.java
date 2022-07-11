package org.hadatac.entity.pojo;

import java.util.HashMap;
import java.util.Map;

import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;


public class DPL {

	private DataFile dplfile = null;
	private Map<String, String> mapCatalog = new HashMap<String, String>();
	
	public DPL(DataFile dataFile) {
		this.dplfile = dataFile;
		readCatalog(dataFile.getRecordFile());
	}
	
	public String getFileName() {
	    return dplfile.getFileName();
    }
	
	public Map<String, String> getCatalog() {
		return mapCatalog;
	}
	
	private void readCatalog(RecordFile file) {
	    if (!file.isValid()) {
            return;
        }
	    
	    for (Record record : file.getRecords()) {
	    	System.out.println("Inside DPL.readCatalog: " + record.getValueByColumnIndex(0) + " " + record.getValueByColumnIndex(1));
	        mapCatalog.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
	    }
	}	
}
