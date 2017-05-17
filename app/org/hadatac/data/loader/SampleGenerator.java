package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import com.google.common.collect.Iterables;

public class SampleGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	private String dataAcquisition = "";
	private int counter = 1; //starting index number
	
	public SampleGenerator(File file) {
		super(file);
	}
	
	@Override
	void initMapping() {
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

	@Override
	Map<String, Object> createRow(CSVRecord rec, int rownumber) {
		Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri(rec));
    	row.put("a", getType(rec));
    	row.put("rdfs:label", getLabel(rec));
    	row.put("hasco:originalID", getOriginalID(rec));
    	row.put("hasco:isSampleOf", getSubjectUri(rec));
    	row.put("hasco:isObjectOf", getCollectionUri(rec));
    	row.put("rdfs:comment", getComment(rec));
    	row.put("hasco:hasSamplingMethod", getSamplingMethod(rec));
    	row.put("hasco:hasSamplingVolume", getSamplingVolume(rec));
    	row.put("hasco:hasSamplingVolumeUnit", getSamplingVolumeUnit(rec));
    	row.put("hasco:hasStorageTemperature", getStorageTemperature(rec));
    	row.put("hasco:hasStorageTemperatureUnit", getStorageTemperatureUnit());
    	row.put("hasco:hasNumFreezeThaw", getNumFreezeThaw(rec));
    	counter++;
    	
    	return row;
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
	
	private String getUri(CSVRecord rec) {
		return kbPrefix + "SPL-" + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("pilotNum")))) 
			+ "-Pilot-" + rec.get(mapCol.get("pilotNum")); //  + "-" + getSampleSuffix()
	}
	
	private String getType(CSVRecord rec) {
		if(!rec.get(mapCol.get("sampleType")).equalsIgnoreCase("NULL")){
			return rec.get(mapCol.get("sampleType"));
		} else {
			return "sio:Sample";
		}
	}
	
	private String getLabel(CSVRecord rec) {
		return "SID " + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("pilotNum")))) + " - Pilot " 
			+ rec.get(mapCol.get("pilotNum")) + " " + getSampleSuffix(rec);
	}
	
    private String getOriginalID(CSVRecord rec) {
    	if(!rec.get(mapCol.get("sampleID")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("sampleID"));
    	} else {
    		return "";
    	}
    }
    
    private String getSubjectUri(CSVRecord rec) {
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
    
    private String getComment(CSVRecord rec) {
    	return "Sample " + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("pilotNum")))) 
    		+ " for Pilot " + rec.get(mapCol.get("pilotNum")) + " " + getSampleSuffix(rec);
    }
    
    private String getSamplingMethod(CSVRecord rec) {
    	if(!rec.get(mapCol.get("samplingMethod")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("samplingMethod"));
    	} else {
    		return "";
    	}
    }
    
    private String getSamplingVolume(CSVRecord rec) {
    	if(!rec.get(mapCol.get("samplingVol")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("samplingVol"));
    	} else {
    		return "";
    	}
    }
    
    private String getSamplingVolumeUnit(CSVRecord rec) {
    	if(!rec.get(mapCol.get("samplingVolUnit")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("samplingVolUnit"));
    	} else {
    		return "obo:UO_0000095"; // default volume unit
    	}
    }
    
    private String getStorageTemperature(CSVRecord rec) {
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
    
    private String getNumFreezeThaw(CSVRecord rec) {
    	if(!rec.get(mapCol.get("FTcount")).equalsIgnoreCase("NULL")){
    		return rec.get("FTcount");
    	} else {
    		return "";
    	}
    }
    
    private String getSampleSuffix(CSVRecord rec) {
    	if(!rec.get(mapCol.get("sampleSuffix")).equalsIgnoreCase("NULL")){
    		return rec.get(mapCol.get("sampleSuffix"));
    	} else {
    		return "";
    	}
    }
    
    private String getStudyUri(CSVRecord rec) {
    	return kbPrefix + "STD-Pilot-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCollectionUri(CSVRecord rec) {
    	return kbPrefix + "SC-Pilot-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCollectionLabel(CSVRecord rec) {
    	return "Sample Collection of Pilot Study " + rec.get(mapCol.get("pilotNum"));
    }
    
    public Map<String, Object> createCollectionRow(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getCollectionUri(rec));
    	row.put("a", "hasco:SampleCollection");
    	row.put("rdfs:label", getCollectionLabel(rec));
    	row.put("hasco:hasSize", Integer.toString(Iterables.size(records)+1));
    	row.put("hasco:isSampleCollectionOf", getStudyUri(rec));
    	counter++;
    	
    	return row;
    }
    
    public List< Map<String, Object> > createCollectionRows() {
    	rows.clear();
    	for (CSVRecord record : records) {
    		rows.add(createCollectionRow(record));
    	}
    	return rows;
    }
}
