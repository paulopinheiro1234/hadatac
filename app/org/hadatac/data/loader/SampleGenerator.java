package org.hadatac.data.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class SampleGenerator {
	final String kbPrefix = "chear-kb:";
	String dataAcquisition = "";
	Iterable<CSVRecord> records = null;
	CSVRecord rec = null;
	int counter = 0;
	List< Map<String, Object> > rows = new ArrayList<Map<String,Object>>();
	HashMap<String, Integer> mapCol = new HashMap<String, Integer>();
	
	public SampleGenerator(File file) {
		try {
			records = CSVFormat.DEFAULT.withHeader().parse(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initMapping();
	}
	
	private void initMapping() {
		mapCol.clear();
        mapCol.put("sampleID", 0);
        mapCol.put("sampleSuffix", 3);
        mapCol.put("subjectID", 7);
        mapCol.put("pilotNum", 8);
        mapCol.put("sampleType", 11);
		mapCol.put("samplingMethod", 12);
		mapCol.put("samplingVol", 15);
		mapCol.put("samplingVolUnit", 16);
		mapCol.put("storageTemp", 18);
		mapCol.put("FTcount", 19);
	}
	
	private String getUri() { 
		return kbPrefix + "SPL-" + String.format("%04d", counter) 
			+ "-Pilot-" + rec.get(mapCol.get("pilotNum") + "-" + rec.get(mapCol.get("sampleSuffix"))); 
	}
	private String getType() {
		return rec.get(mapCol.get("sampleType"));
	}
	private String getLabel() {
		return "SID " + String.format("%04d", counter) + " - Pilot " 
			+ rec.get(mapCol.get("pilotNum")) + " " + rec.get(mapCol.get("sampleSuffix"));
	}
    private String getOriginalID() {
    	return rec.get(mapCol.get("sampleID"));
    }
    private String getSubjectUri() {
    	return kbPrefix + "SBJ-" + String.format("%04d", counter) 
    		+ "-" + rec.get(mapCol.get("subjectID")) + "-Pilot-" + rec.get(mapCol.get("pilotNum"));
    }
    private String getDataAcquisition() {
    	return dataAcquisition;
    }
    private String getComment() {
    	return "Sample " + String.format("%04d", counter) 
    		+ " for Pilot " + rec.get(mapCol.get("pilotNum")) + " " + rec.get(mapCol.get("sampleSuffix"));
    }
    private String getSamplingMethod() {
    	return rec.get(mapCol.get("samplingMethod"));
    }
    private String getSamplingVolume() {
    	return rec.get(mapCol.get("samplingVol"));
    }
    private String getSamplingVolumeUnit() {
    	return rec.get(mapCol.get("samplingVolUnit"));
    }
    private String getStorageTemperature() {
    	return rec.get(mapCol.get("storageTemp"));
    }
    private String getStorageTemperatureUnit() {
    	return rec.get(mapCol.get("storageTempUnit"));
    }
    private String getNumFreezeThaw() {
    	return rec.get(mapCol.get("FTcount"));
    }
    
    public Map<String, Object> createRow() {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri());
    	row.put("a", getType());
    	row.put("rdfs:label", getLabel());
    	row.put("hasco:originalID", getOriginalID());
    	row.put("hasco:isSampleOf", getSubjectUri());
    	row.put("hasco:isMeasuredObjectOf", getDataAcquisition());
    	row.put("rdfs:comment", getComment());
    	row.put("hasco:hasSamplingMethod", getSamplingMethod());
    	row.put("hasco:hasSamplingVolume", getSamplingVolume());
    	row.put("hasco:hasSamplingVolumeUnit", getSamplingVolumeUnit());
    	row.put("hasco:hasStorageTemperature", getStorageTemperature());
    	row.put("hasco:hasStorageTemperatureUnit", getStorageTemperatureUnit());
    	row.put("hasco:hasNumFreezeThaw", getNumFreezeThaw());
    	counter++;
    	
    	return row;
    }
    
    public List< Map<String, Object> > createRows() {
    	rows.clear();
    	for (CSVRecord record : records) {
    		rec = record;
    		rows.add(createRow());
    	}
    	
    	return rows;
    }
    
    public String toString() {
    	String result = String.join("\t", rows.get(0).keySet());
    	for (Map<String, Object> row : rows) {
    		List<String> values = new ArrayList<String>();
    		for (String colName : rows.get(0).keySet()) {
    			if (row.containsKey(colName)) {
    				values.add((String)row.get(colName));
    			}
    			else {
    				values.add("");
    			}
    		}
    		result += "\n";
    		result += String.join("\t", values);
    	}
    	
    	return result;
    }
}