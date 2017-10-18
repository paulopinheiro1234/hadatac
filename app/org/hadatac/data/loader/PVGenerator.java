package org.hadatac.data.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.annotator.AutoAnnotator;
import org.hadatac.console.http.ConfigUtils;

import play.Play;

import java.lang.String;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class PVGenerator extends BasicGenerator {
	final String kbPrefix = ConfigUtils.getKbPrefix();
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
		System.out.println("RIGHT IN PVG: " + study_id);
		this.pvMap = AutoAnnotator.codebook;
		
	}
	//Column	Code	Label	Class	Resource
	@Override
	void initMapping() {
		mapCol.clear();
        mapCol.put("Label", "Column");
        mapCol.put("Code", "Code");
        mapCol.put("CodeLabel", "Label");
        mapCol.put("Class", "Class");
        mapCol.put("Resource", "Resource");
	}
    
    private String getLabel(CSVRecord rec) {
    	return rec.get(mapCol.get("Label"));
    }
    
    private String getCode(CSVRecord rec) {
    	return Normalizer.normalize(rec.get(mapCol.get("Code")), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").trim();
    }
    
    private String getCodeLabel(CSVRecord rec) {
    		return rec.get(mapCol.get("CodeLabel"));
    }
    
    private String getClass(CSVRecord rec) {
    	return rec.get(mapCol.get("Class"));
    }
    
    private String getResource(CSVRecord rec) {
    	return rec.get(mapCol.get("Resource"));
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
	if (getResource(rec) != ""){
		row.put("hasURI", getResource(rec));
	}
	else{
    		row.put("hasURI", kbPrefix + "PV-" + getLabel(rec).replace("_","-").replace("??", "") + ("-" + study_id.replace("null", "") + "-" + getCode(rec)).replaceAll("--", "-"));
	}
    	row.put("a", "hasco:PossibleValue");
    	row.put("hasco:hasCode", getCode(rec));
    	row.put("hasco:hasCodeLabel", getCodeLabel(rec));
    	row.put("hasco:hasClass", getClass(rec));
    	row.put("hasco:hasResource", getResource(rec));
    	row.put("hasco:isPossibleValueOf", kbPrefix + "DASA-" + getLabel(rec).replace("_","-").replace("??", ""));
    	return row;
    }
}
