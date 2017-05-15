package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.metadata.loader.ValueCellProcessing;

public class DataAcquisitionGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	
	public DataAcquisitionGenerator(File file) {
		super(file);
	}
	
	@Override
	void initMapping() {
		mapCol.clear();
        mapCol.put("DataAcquisitionName", "DA Name");
        mapCol.put("Comment", "Comment");
        mapCol.put("Method", "Method");
        mapCol.put("Study", "Study");
        mapCol.put("DataDictionaryName", "Data Dictionary Name");
        mapCol.put("Epi/Lab", "Epi/Lab");
	}
	
    private String getDataAcquisitionName(CSVRecord rec) {
    	return rec.get(mapCol.get("DataAcquisitionName"));
    }
    
    private String getComment(CSVRecord rec) {
    	return rec.get(mapCol.get("Comment"));
    }
    
    private String getMethod(CSVRecord rec) {
    	return rec.get(mapCol.get("Method"));
    }
    
    private String getStudy(CSVRecord rec) {
    	return rec.get(mapCol.get("Study"));
    }
    
    private String getDataDictionaryName(CSVRecord rec) {
    	return rec.get(mapCol.get("DataDictionaryName"));
    }
    
    private String getEpiLab(CSVRecord rec) {
    	return rec.get(mapCol.get("Epi/Lab"));
    }
    
    @Override
    Map<String, Object> createRow(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", kbPrefix + "DA-" + getDataAcquisitionName(rec));
    	row.put("a", "hasco:DataAcquisition");
    	row.put("rdfs:label", getDataAcquisitionName(rec));
    	row.put("rdfs:comment", getComment(rec));
    	row.put("hasneto:hasDeployment", kbPrefix + "DPL-" + getDataAcquisitionName(rec));
    	row.put("hasco:hasMethod", "hasco:" + getMethod(rec));
    	row.put("hasco:isDataAcquisitionOf", kbPrefix + "STD-Pilot-" + getStudy(rec));
    	row.put("hasco:hasSchema", kbPrefix + "DAS-" + getDataDictionaryName(rec));
    	
    	createDataAcquisition(row);
    	
    	return row;
    }
    
    void createDataAcquisition(Map<String, Object> row) {
    	DataAcquisition dataAcquisition = new DataAcquisition();
    	dataAcquisition.setUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasURI")));
    	dataAcquisition.setLabel(ValueCellProcessing.replacePrefixEx((String)row.get("rdfs:label")));
    	dataAcquisition.setComment(ValueCellProcessing.replacePrefixEx((String)row.get("rdfs:comment")));
    	dataAcquisition.setDeploymentUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasneto:hasDeployment")));
    	dataAcquisition.setMethodUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasco:hasMethod")));
    	dataAcquisition.setStudyUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasco:isDataAcquisitionOf")));
    	dataAcquisition.setSchemaUri(ValueCellProcessing.replacePrefixEx((String)row.get("hasco:hasSchema")));
    	dataAcquisition.save();
    }
}