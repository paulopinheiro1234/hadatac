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
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import com.google.common.collect.Iterables;

public class SampleGenerator {
	final String kbPrefix = "chear-kb:";
	private String dataAcquisition = "";
	private Iterable<CSVRecord> records = null;
	private CSVRecord rec = null;
	private int counter = 1; //starting index number
	private List< Map<String, Object> > rows = new ArrayList<Map<String, Object>>();
	private HashMap<String, String> mapCol = new HashMap<String, String>();
	
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
        mapCol.put("sampleID", "specimen_id");
        mapCol.put("sampleSuffix", "suffix");
        mapCol.put("subjectID", "patient_id");
        mapCol.put("pilotNum", "project_id");
        mapCol.put("sampleType", "sample_type");
		mapCol.put("samplingMethod", "sample_collection_method");
		mapCol.put("samplingVol", "sample_quantity");
		mapCol.put("samplingVolUnit", "sample_quantity_uom");
		mapCol.put("storageTemp", "sample_storage_temp");
		mapCol.put("FTcount", "sample_freeze_thaw_cycles");
	}
	
	private int getSampleCount(String pilotNum){
		int count = 0;
		String sampleCountQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT (count(DISTINCT ?sampleURI) as ?sampleCount) WHERE { "
				+ " ?sampleURI hasco:isObjectOf ?SC . "
				+ " ?SC hasco:isSampleCollectionOf chear-kb:STD-Pilot-" + pilotNum + " . "
				+ "}";
		QueryExecution qexecSample = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), sampleCountQuery);
		ResultSet sampleResults = qexecSample.execSelect();
		ResultSetRewindable resultsrwSample = ResultSetFactory.copyResults(sampleResults);
		qexecSample.close();
		if (resultsrwSample.hasNext()) {
			QuerySolution soln = resultsrwSample.next();
			Literal countLiteral = (Literal) soln.get("sampleCount");
			if(countLiteral != null){ 
				count += countLiteral.getInt();
			}
		}
		
		return count;
	}
	
	private String getUri() {
		return kbPrefix + "SPL-" + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("pilotNum")))) 
			+ "-Pilot-" + rec.get(mapCol.get("pilotNum")); //  + "-" + getSampleSuffix()
	}
	
	private String getType() {
		if(!rec.get(mapCol.get("sampleType")).equalsIgnoreCase("NULL")){
			return rec.get(mapCol.get("sampleType"));
		} else {
			return "sio:Sample";
		}
	}
	
	private String getLabel() {
		return "SID " + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("pilotNum")))) + " - Pilot " 
			+ rec.get(mapCol.get("pilotNum")) + " " + getSampleSuffix();
	}
	
    private String getOriginalID() {
    	if(!rec.get(mapCol.get("sampleID")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("sampleID"));
    	} else {
    		return "";
    	}
    }
    
    private String getSubjectUri() {
    	if (rec.get(mapCol.get("subjectID")).equalsIgnoreCase("NULL")) {
    		return "";
    	}
    	
    	String subject = "";
		String subjectQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT ?subjectURI WHERE { "
				+ " ?subjectURI hasco:originalID \"" + rec.get(mapCol.get("subjectID")) + "\" . }";
		
		QueryExecution qexecSubject = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectQuery);
		ResultSet subjectResults = qexecSubject.execSelect();
		ResultSetRewindable resultsrwSubject = ResultSetFactory.copyResults(subjectResults);
		qexecSubject.close();
		if (resultsrwSubject.hasNext()) {
			QuerySolution soln = resultsrwSubject.next();
			subject = soln.get("subjectURI").toString();
		}
    	
    	return subject;
    }
    
    private String getDataAcquisition() {
    	return dataAcquisition;
    }
    
    private String getComment() {
    	return "Sample " + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("pilotNum")))) 
    		+ " for Pilot " + rec.get(mapCol.get("pilotNum")) + " " + getSampleSuffix();
    }
    
    private String getSamplingMethod() {
    	if(!rec.get(mapCol.get("samplingMethod")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("samplingMethod"));
    	} else {
    		return "";
    	}
    }
    
    private String getSamplingVolume() {
    	if(!rec.get(mapCol.get("samplingVol")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("samplingVol"));
    	} else {
    		return "";
    	}
    }
    
    private String getSamplingVolumeUnit() {
    	if(!rec.get(mapCol.get("samplingVolUnit")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("samplingVolUnit"));
    	} else {
    		return "obo:UO_0000095"; // default volume unit
    	}
    }
    
    private String getStorageTemperature() {
    	if(!rec.get(mapCol.get("storageTemp")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("storageTemp"));
    	} else {
    		return "";
    	}
    }
    
    private String getStorageTemperatureUnit() {
    	// defaulting to Celsius since SID file does not contain temp unit
    	return "obo:UO_0000027";
    }
    
    private String getNumFreezeThaw() {
    	if(!rec.get(mapCol.get("FTcount")).equalsIgnoreCase("NULL")){
    		return rec.get("FTcount");
    	} else {
    		return "";
    	}
    }
    
    private String getSampleSuffix() {
    	if(!rec.get(mapCol.get("sampleSuffix")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("sampleSuffix"));
    	} else {
    		return "";
    	}
    }
    
    private String getStudyUri() {
    	return kbPrefix + "STD-Pilot-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCollectionUri() {
    	return kbPrefix + "SC-Pilot-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCollectionLabel() {
    	return "Sample Collection of Pilot Study " + rec.get(mapCol.get("pilotNum"));
    }
    
    public Map<String, Object> createRow() {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri());
    	row.put("a", getType());
    	row.put("rdfs:label", getLabel());
    	row.put("hasco:originalID", getOriginalID());
    	row.put("hasco:isSampleOf", getSubjectUri());
    	row.put("hasco:isObjectOf", getCollectionUri());
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
    
    public Map<String, Object> createCollectionRow() {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getCollectionUri());
    	row.put("a", "hasco:SampleCollection");
    	row.put("rdfs:label", getCollectionLabel());
    	row.put("hasco:hasSize", Integer.toString(Iterables.size(records)+1));
    	row.put("hasco:isSampleCollectionOf", getStudyUri());
    	counter++;
    	
    	return row;
    }
    
    public List< Map<String, Object> > createCollectionRows() {
    	for (CSVRecord record : records) {
    		rec = record;
    		rows.add(createCollectionRow());
    	}

    	return rows;
    }
    
    public String toString() {
    	if (rows.isEmpty()) {
    		return "";
    	}
    	
    	List<String> colNames = new ArrayList<String>(rows.get(0).keySet());
		String result = String.join(",", colNames);
    	for (Map<String, Object> row : rows) {
    		List<String> values = new ArrayList<String>();
    		for (String colName : colNames) {
    			if (row.containsKey(colName)) {
    				values.add((String)row.get(colName));
    			}
    			else {
    				values.add("");
    			}
    		}
    		result += "\n";
    		result += String.join(",", values);
    	}
    	
    	return result;
    }
}
