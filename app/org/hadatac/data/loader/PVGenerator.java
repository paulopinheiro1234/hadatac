package org.hadatac.data.loader;

import java.io.File;
import org.hadatac.utils.ConfigProp;

import java.lang.String;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class PVGenerator extends BasicGenerator {
	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDFileName = "";
	Map<String, String> codeMap;
	Map<String, Map<String, String>> pvMap = new HashMap<String, Map<String, String>>();
	String study_id = "";
	Map<String, String> mapAttrObj;

	public PVGenerator(File file, String SDDFileName, String study_id, 
			Map<String, String> mapAttrObj) {
		super(file);
		this.SDDFileName = SDDFileName;
		this.study_id = study_id;
		this.mapAttrObj = mapAttrObj;
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
			String colNameInSDD = getLabel(rec).replace(" ", "");
			if (mapAttrObj.containsKey(colNameInSDD) && mapAttrObj.get(colNameInSDD).length() > 0) {
				return kbPrefix + "DASA-" + SDDFileName.replace("SDD-", "").replace(".csv", "") + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", "");
			} else {
				return kbPrefix + "DASO-" + SDDFileName.replace("SDD-", "").replace(".csv", "") + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", "");
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
					+ ("-" + SDDFileName.replace("SDD-", "").replace(".csv", "") + "-" + getCode(rec)).replaceAll("--", "-")).replace(" ",""));
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
