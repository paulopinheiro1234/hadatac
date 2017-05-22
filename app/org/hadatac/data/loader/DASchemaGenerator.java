package org.hadatac.data.loader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class DASchemaGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	String startTime = "";
	String SDDName = "";
	
	public DASchemaGenerator(File file) {
		super(file);
		this.SDDName = file.getName();
	}
	
	@Override
	void initMapping() {
		mapCol.clear();
        mapCol.put("Study", SDDName.substring(SDDName.indexOf("PS") + 1));
	}
	
    private String getLabel() {
    	return "Schema for Pilot Study" + mapCol.get("Study") + "EPI Data Acquisitions";
    }
    
    private String getComment() {
    	return "";
    }
    
    @Override
    Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", kbPrefix + "DAS-" + SDDName);
    	row.put("a", "hasco:DASchema");
    	row.put("rdfs:label", getLabel());
    	row.put("rdfs:comment", getComment());
    	row.put("hasco:isSchemaOf", kbPrefix + "STD-Pilot-" + mapCol.get("Study"));
    	
    	return row;
    }
}