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
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.utils.Collections;

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
	
	private int getSampleCount(String pilotNum){
		int count=0;
		String sampleCountQuery = DynamicFunctions.getPrefixes() + "SELECT (count(DISTINCT ?sampleURI) as ?sampleCount) WHERE {?sampleURI hasco:isMeasuredObjectOf ?DA . ?DA hasco:isDataAcquisitionOf chear-kb:STD-Pilot-" + pilotNum + " . }";
		QueryExecution qexecSample = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), sampleCountQuery);
		ResultSet sampleResults = qexecSample.execSelect();
		ResultSetRewindable resultsrwSample = ResultSetFactory.copyResults(sampleResults);
		QuerySolution soln = resultsrwSample.next();
		int value = Integer.parseInt(soln.get("sampleCount").toString());
		qexecSample.close();
		count += value;
		return count;
	}
	
	private String getUri() {
		return kbPrefix + "SPL-" + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("pilotNum")))) 
			+ "-Pilot-" + rec.get(mapCol.get("pilotNum") + "-" + rec.get(mapCol.get("sampleSuffix"))); 
	}
	private String getType() {
		return rec.get(mapCol.get("sampleType"));
	}
	private String getLabel() {
		return "SID " + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("pilotNum")))) + " - Pilot " 
			+ rec.get(mapCol.get("pilotNum")) + " " + rec.get(mapCol.get("sampleSuffix"));
	}
    private String getOriginalID() {
    	return rec.get(mapCol.get("sampleID"));
    }
    private String getSubjectUri() {
    	if(mapCol.get("subjectID")!=null){
    		//mapping of subject uri to given original id
    	}
    	return kbPrefix + "SBJ-" + String.format("%04d", counter) 
    		+ "-" + rec.get(mapCol.get("subjectID")) + "-Pilot-" + rec.get(mapCol.get("pilotNum"));
    }
    private String getDataAcquisition() {
    	return dataAcquisition;
    }
    private String getComment() {
    	return "Sample " + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("pilotNum")))) 
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