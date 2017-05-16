package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.RDFNode;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class DeploymentGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	String startTime = "";
	
	public DeploymentGenerator(File file) {
		super(file);
	}
	
	public DeploymentGenerator(File file, String startTime) {
		super(file);
		this.startTime = startTime;
	}
	
	@Override
	void initMapping() {
		mapCol.clear();
        mapCol.put("DataAcquisitionName", "DA Name");
        mapCol.put("Comment", "Comment");
        mapCol.put("Method", "Method");
        mapCol.put("Study", "Study");
        mapCol.put("Epi/Lab", "Epi/Lab");
	}
	
	private String getCohortAsPlatform(CSVRecord rec) {
		String cohort = "";
		String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT ?cohort WHERE { "
				+ " ?cohort hasco:isCohortOf " + getStudy(rec) + " . "
				+ " }";
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), strQuery);
		ResultSet resultSet = qe.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(resultSet);
		qe.close();
		if (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			RDFNode node = soln.get("cohort");
			if (null != node) {
				cohort = ValueCellProcessing.replaceNameSpaceEx(node.toString());
			}
		}
    	
    	return cohort;
	}
	
	private String getSampleCollectionAsPlatform(CSVRecord rec) {
		String sampleCollection = "";
		String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT ?sampleCollection WHERE { "
				+ " ?sampleCollection hasco:isSampleCollectionOf " + getStudy(rec) + " . "
				+ " }";
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), strQuery);
		ResultSet resultSet = qe.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(resultSet);
		qe.close();
		if (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			RDFNode node = soln.get("sampleCollection");
			if (null != node) {
				sampleCollection = ValueCellProcessing.replaceNameSpaceEx(node.toString());
			}
		}
    	
    	return sampleCollection;
	}
	
	private String getPlatform(CSVRecord rec) {
		if (isEpiData(rec)) {
			return getCohortAsPlatform(rec);
		}
		else if (isLabData(rec)) {
			return getSampleCollectionAsPlatform(rec);
		}
		
		return "";
    }
	
	private String getInstrument(CSVRecord rec) {
		if (isEpiData(rec)) {
			// “generic questionnaire” is the instrument
			return kbPrefix + "INS-GENERIC-QUESTIONNAIRE";
		}
		else if (isLabData(rec)) {
			return kbPrefix + "INS-GENERIC-PHYSICAL-INSTRUMENT";
		}
    	return "";
    }
	
    private String getDataAcquisitionName(CSVRecord rec) {
    	return rec.get(mapCol.get("DataAcquisitionName"));
    }
    
    private String getMethod(CSVRecord rec) {
    	return rec.get(mapCol.get("Method"));
    }
    
    private String getStudy(CSVRecord rec) {
    	return kbPrefix + "STD-Pilot-" + rec.get(mapCol.get("Study"));
    }
    
    private String getDataDictionaryName(CSVRecord rec) {
    	return isEpiData(rec)? rec.get(mapCol.get("DataDictionaryName")) : "";
    }
    
    private Boolean isEpiData(CSVRecord rec) {
    	return rec.get(mapCol.get("Epi/Lab")).equalsIgnoreCase("EPI");
    }
    
    private Boolean isLabData(CSVRecord rec) {
    	return rec.get(mapCol.get("Epi/Lab")).equalsIgnoreCase("LAB");
    }
    
    @Override
    Map<String, Object> createRow(CSVRecord rec, int rownumber) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", kbPrefix + "DPL-" + getDataAcquisitionName(rec));
    	row.put("a", "vstoi:Deployment");
    	row.put("vstoi:hasPlatform", getPlatform(rec));
    	row.put("hasneto:hasInstrument", getInstrument(rec));
    	if (startTime.isEmpty()) {
    		DateFormat isoFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        	row.put("prov:startedAtTime", isoFormat.format(new Date()));
    	}
    	else {
    		row.put("prov:startedAtTime", startTime);
    	}
    	
    	//row.put("hasneto:hasDetector", "");
    	//row.put("prov:endedAtTime", "");
    	//row.put("vstoi:subDeploymentOf", "");
    	
    	return row;
    }
}