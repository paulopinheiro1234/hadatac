package org.hadatac.data.loader;

import org.hadatac.entity.pojo.DataFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import java.lang.String;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PVGenerator extends BaseGenerator {
	
	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	Map<String, String> codeMap;
	Map<String, Map<String, String>> pvMap = new HashMap<String, Map<String, String>>();
	Map<String, String> mapAttrObj;
	Map<String, String> codeMappings;

	public PVGenerator(DataFile dataFile, String SDDName,  
			Map<String, String> mapAttrObj, Map<String, String> codeMappings) {
		super(dataFile);
		this.SDDName = SDDName;
		this.mapAttrObj = mapAttrObj;
		this.codeMappings = codeMappings;
	}
	
	//Column	Code	Label	Class	Resource
	@Override
	public void initMapping() {
		mapCol.clear();
		mapCol.put("Label", "Column");
		mapCol.put("Code", "Code");
		mapCol.put("CodeLabel", "Label");
		mapCol.put("Class", "Class");
		mapCol.put("Resource", "Resource");
	}

	private String getLabel(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Label"));
	}

	private String getCode(Record rec) {
		String ss = Normalizer.normalize(rec.getValueByColumnName(mapCol.get("Code")), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").trim();
		int iend = ss.indexOf(".");
		if (iend != -1){
			ss = ss.substring(0 , iend);
		}
		return ss.trim();
	}

	private String getCodeLabel(Record rec) {
		return rec.getValueByColumnName(mapCol.get("CodeLabel"));
	}

	private String getClass(Record rec) {
		String cls = rec.getValueByColumnName(mapCol.get("Class"));
		if (cls.length() > 0) {
			if (URIUtils.isValidURI(cls)) {
				return cls;
			}
		} else {
			if (codeMappings.containsKey(getCode(rec))) {
				return codeMappings.get(getCode(rec));
			}
		}

		return "";
	}

	private String getResource(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Resource"));
	}

	private Boolean checkVirtual(Record rec) {
		if (getLabel(rec).contains("??")){
			return true;
		} else {
			return false;
		}
	}

	private String getPVvalue(Record rec) {
		if ((getLabel(rec)).length() > 0) {
			String colNameInSDD = getLabel(rec).replace(" ", "");
			if (mapAttrObj.containsKey(colNameInSDD) && mapAttrObj.get(colNameInSDD).length() > 0) {
				return kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", "");
			} else {
				return kbPrefix + "DASO-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", "");
			}
		} else {
			return "";
		}
	}
	
	public List<String> createUris() throws Exception {
		int rowNumber = 0;
		List<String> result = new ArrayList<String>();
		for (Record record : records) {
			result.add((kbPrefix + "PV-" + getLabel(record).replace("_","-").replace("??", "") + ("-" + SDDName + "-" + getCode(record)).replaceAll("--", "-")).replace(" ","") + "-" + rowNumber);
			++rowNumber;
		}
		return result;
	}

	@Override
	public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {	
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", (kbPrefix + "PV-" + getLabel(rec).replaceAll("[^a-zA-Z0-9:-]", "-") + ("-" + SDDName + "-" + getCode(rec)).replaceAll("--", "-")).replace(" ","").replaceAll("[^A-Za-z0-9:-]", "") + "-" + rowNumber);
		row.put("a", "hasco:PossibleValue");
		row.put("hasco:hasCode", getCode(rec));
		row.put("hasco:hasCodeLabel", getCodeLabel(rec));
		row.put("hasco:hasClass", getClass(rec));
		row.put("hasco:isPossibleValueOf", getPVvalue(rec));
		
		return row;
	}

	@Override
	public String getTableName() {
		return "PossibleValue";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "Error in PVGenerator: " + e.getMessage();
	}
}
