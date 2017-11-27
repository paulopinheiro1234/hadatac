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
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.Templates;

public class DeploymentGenerator extends BasicGenerator {
	final String kbPrefix = ConfigProp.getKbPrefix();
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
	    mapCol.put("DataAcquisitionName", Templates.DATAACQUISITIONNAME);
	    mapCol.put("Method", Templates.METHOD);
	    mapCol.put("Study", Templates.DASTUDYID);
	    mapCol.put("Epi/Lab", Templates.EPILAB);
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
	String tempStr = "";
	if (isEpiData(rec)) {
	    tempStr = getCohortAsPlatform(rec);
	    if (tempStr == null || tempStr.equals("")) {
		tempStr = getSampleCollectionAsPlatform(rec);
	    } 
	    return tempStr;
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

	String tempDA = getDataAcquisitionName(rec);
	if (tempDA == null || tempDA.equals("")) {
	    System.out.println("[ERROR] Deployment not created because of empty DA info."); 
	    return null;
	}

	String tempDeployment = getPlatform(rec);
	if (tempDeployment == null || tempDeployment.equals("")) {
	    System.out.println("[ERROR] Platform for " + tempDA + " is empty. Deployment was not created."); 
	    return null;
	}

	String tempInstrument = getInstrument(rec);
	if (tempInstrument == null || tempInstrument.equals("")) {
	    System.out.println("[ERROR] Instrument for " + tempDA + " is empty. Deployment was not created."); 
	    return null;
	}

    	row.put("hasURI", kbPrefix + "DPL-" + tempDA);
    	row.put("a", "vstoi:Deployment");
    	row.put("vstoi:hasPlatform", tempDeployment);
    	row.put("hasco:hasInstrument", tempInstrument);
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