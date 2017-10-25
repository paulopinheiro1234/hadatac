package org.hadatac.data.loader;

import java.io.File;

import org.hadatac.console.controllers.annotator.AutoAnnotator;
import org.hadatac.console.http.ConfigUtils;

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
	HashMap<String, String> AttrORobj;

	//chear-kb:PV-childsex-0

	public PVGenerator(File file) {
		super(file);
		this.SDDName = file.getName();
		this.codeMap = AutoAnnotator.codeMappings;
		this.study_id = AutoAnnotator.study_id;
		System.out.println("RIGHT IN PVG: " + study_id);
		this.pvMap = AutoAnnotator.codebook;
		this.AttrORobj = AutoAnnotator.AttrORobj;

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
		return getValueByColumnName(rec, mapCol.get("Label"));
	}

	private String getCode(CSVRecord rec) {
		return Normalizer.normalize(getValueByColumnName(rec, mapCol.get("Code")), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").trim();
	}

	private String getCodeLabel(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("CodeLabel"));
	}

	private String getClass(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Class"));
	}

	private String getResource(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Resource"));
	}

	private Boolean checkVirtual(CSVRecord rec) {
		if (getLabel(rec).contains("??")){
			return true;
		} else {
			return false;
		}
	}
	
	private String getPVvalue(CSVRecord rec) {
		if ((getLabel(rec)).length() > 0) {
			if (AttrORobj.containsKey(getLabel(rec))) {
				return kbPrefix + "DASA-" + study_id + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", "");
			} else {
				return kbPrefix + "DASO-" + study_id + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", "");
			}
		} else {
			return "";
		}
	}

	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		if (getResource(rec) != null && getResource(rec).length() != 0){
			row.put("hasURI", getResource(rec));
		}
		else{
			row.put("hasURI", (kbPrefix + "PV-" + getLabel(rec).replace("_","-").replace("??", "") 
					+ ("-" + study_id.replace("null", "") + "-" + getCode(rec)).replaceAll("--", "-")).replace(" ",""));
		}
		row.put("a", "hasco:PossibleValue");
		row.put("hasco:hasCode", getCode(rec));
		row.put("hasco:hasCodeLabel", getCodeLabel(rec));
		row.put("hasco:hasClass", getClass(rec));
		row.put("hasco:hasResource", getResource(rec));
		row.put("hasco:isPossibleValueOf", getPVvalue(rec));
		return row;
	}
}
