package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
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

import play.Play;

public class DeploymentGenerator extends BasicGenerator {
	final String kbPrefix = Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
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
        mapCol.put("DataAcquisitionName", "name");
        mapCol.put("Method", "method");
        mapCol.put("Study", "study");
        mapCol.put("Epi/Lab", "epi/lab");
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
    	return kbPrefix + "STD-" + rec.get(mapCol.get("Study"));
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
    Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", kbPrefix + "DPL-" + getDataAcquisitionName(rec));
    	row.put("a", "vstoi:Deployment");
    	row.put("vstoi:hasPlatform", getPlatform(rec));
    	row.put("hasco:hasInstrument", getInstrument(rec));
    	if (startTime.isEmpty()) {
        	row.put("prov:startedAtTime", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")).format(new Date()));
    	}
    	else {
    		row.put("prov:startedAtTime", startTime);
    	}
    	
    	//row.put("hasco:hasDetector", "");
    	//row.put("prov:endedAtTime", "");
    	//row.put("vstoi:subDeploymentOf", "");
    	
    	return row;
    }
}