package org.hadatac.data.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.annotator.AutoAnnotator;

import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class PVGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	String startTime = "";
	String SDDName = "";
	HashMap<String, String> codeMap;
//	HashMap<String, String> LabelMap = new HashMap<String, String>();
	HashMap<String, List<String>> pvMap = new HashMap<String, List<String>>();
	String study_id = "";
	
	//chear-kb:PV-childsex-0
	
	public PVGenerator(File file) {
		super(file);
		this.SDDName = file.getName();
		this.codeMap = AutoAnnotator.codeMappings;
		this.study_id = AutoAnnotator.study_id;
		this.pvMap = AutoAnnotator.codebook;
		
	}
	//Column	Code	Label	Class
	@Override
	void initMapping() {
		mapCol.clear();
        mapCol.put("Label", "Column");
        mapCol.put("Code", "Code");
        mapCol.put("CodeValue", "Label");
        mapCol.put("Class", "Class");
	}
    
    private String getLabel(CSVRecord rec) {
    	return rec.get(mapCol.get("Label"));
    }
    
    private String getCode(CSVRecord rec) {
    	return rec.get(mapCol.get("Code"));
    }
    
    private String getCodeValue(CSVRecord rec) {
    		return rec.get(mapCol.get("CodeValue"));
    }
    
    private String getClass(CSVRecord rec) {
    	return rec.get(mapCol.get("Class"));
    }
    
    private Boolean checkVirtual(CSVRecord rec) {
    	if (getLabel(rec).contains("??")){
    		return true;
    	} else {
    		return false;
    	}
    }
    
    @Override
    Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", kbPrefix + "PV-" + getLabel(rec).replace("_","-").replace("??", "") + ("-" + study_id.replace("null", "") + "-" + getCode(rec)).replaceAll("--", "-"));
    	row.put("a", "hasco:PossibleValue");
    	row.put("hasco:hasCode", getCode(rec));
    	row.put("hasco:hasCodeValue", getCodeValue(rec));
    	row.put("hasco:hasClass", getClass(rec));
    	row.put("hasco:isPossibleValueOf", kbPrefix + "DASA-" + getLabel(rec).replace("_","-").replace("??", "") + "-" + study_id);
    	
    	return row;
    }
}