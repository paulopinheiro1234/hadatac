package org.hadatac.data.loader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import org.apache.commons.io.FileUtils;

public class DASchemaGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	String startTime = "";
	String SDDName = "";
	
	public DASchemaGenerator(File file) {
		super(file);
		this.SDDName = file.getName();
                System.out.println("Inside DASchemaGenerator constructor()");
                System.out.println("Inside DASchemaGenerator file inside constructor: " + SDDName);
	}
	
	@Override
	void initMapping() {
            int pos = fileName.indexOf("PS") + 2;
	    System.out.println("Inside initMapping: " + fileName.substring(pos, pos + 1));
		mapCol.clear();
                mapCol.put("Study", fileName.substring(pos, pos + 1));
	}
	
    private String getLabel() {
    	return "Schema for Study " + mapCol.get("Study") + " EPI Data Acquisitions";
    }
    
    private String getComment() {
    	return "";
    }
    
    @Override
    Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
        System.out.println("Inside DASchemaGenerator.createRow()");
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", kbPrefix + "DAS-" + SDDName);
    	row.put("a", "hasco:DASchema");
    	row.put("rdfs:label", getLabel());
    	row.put("rdfs:comment", getComment());
    	row.put("hasco:isSchemaOf", kbPrefix + "STD-" + mapCol.get("Study"));
    	
    	return row;
    }
}