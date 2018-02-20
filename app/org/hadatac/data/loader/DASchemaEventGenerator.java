package org.hadatac.data.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.hadatac.utils.ConfigProp;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class DASchemaEventGenerator extends BasicGenerator {

	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	List<String> timeList = new ArrayList<String>();
	Map<String, String> codeMap;
	Map<String, List<String>> tlm;

	public DASchemaEventGenerator(File dd, Map<String, List<String>> tlm, String SDDName, Map<String, String> codeMap) {
		super(dd);
		this.codeMap = codeMap;
		this.SDDName = SDDName;
		this.tlm = tlm;
		System.out.println("tlm key size : " + tlm.keySet().size());

        CSVRecord current = null;

		try {
			BufferedReader br = new BufferedReader(new FileReader(dd));
            CSVParser dict = CSVFormat.DEFAULT.withHeader().parse(br);
            Iterator<CSVRecord> dictIter = dict.iterator();

			while(dictIter.hasNext()) {
                current = dictIter.next();
                if(current.get("Time") != null && current.get("Time") != ""){
                    timeList.add(current.get("Time"));
                    //System.out.println("[DASEGenerator] adding to timeList: " + current.get("Time"));
                }
            }

            dict.close();
			br.close();
		} catch (Exception e) {
            System.out.println("[DASEventGenerator] Error opening SDD file");
			e.printStackTrace();
		}
	}
	
	//Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition	
	@Override
	void initMapping() {
		mapCol.clear();
		mapCol.put("Label", "Column");
		mapCol.put("AttributeType", "Attribute");
		mapCol.put("AttributeOf", "attributeOf");
		mapCol.put("Unit", "Unit");
		mapCol.put("Time", "Time");
		mapCol.put("Entity", "Entity");
		mapCol.put("Role", "Role");
		mapCol.put("Relation", "Relation");
		mapCol.put("InRelationTo", "inRelationTo");
		mapCol.put("WasDerivedFrom", "wasDerivedFrom");       
		mapCol.put("WasGeneratedBy", "wasGeneratedBy");
	}

	private String getLabel(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Label"));
	}

	private String getAttribute(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("AttributeType"));
	}

	private String getUnit(CSVRecord rec) {
		if (codeMap.containsKey(getValueByColumnName(rec, mapCol.get("Unit")))) {
			return codeMap.get(getValueByColumnName(rec, mapCol.get("Unit")));
		} else if (getValueByColumnName(rec, mapCol.get("Unit")) != null){
			return getValueByColumnName(rec, mapCol.get("Unit"));
		}
		
		return "";
	}

	private String getTime(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Time"));
	}

	private String getEntity(CSVRecord rec) {
		if (getValueByColumnName(rec, mapCol.get("Entity")).equals("")) {
			return null;
		} else {
			if (codeMap.containsKey(getValueByColumnName(rec, mapCol.get("Entity")))) {
				return codeMap.get(getValueByColumnName(rec, mapCol.get("Entity")));
			} else {
				return getValueByColumnName(rec, mapCol.get("Entity"));
			}
		}
	}

	private String getRole(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Role"));
	}

	private String getRelation(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Relation"));
	}

	private String getInRelationTo(CSVRecord rec) {
		if (getValueByColumnName(rec, mapCol.get("InRelationTo")).equals("")){
			return "";
		} else {
			List<String> items = Arrays.asList(getValueByColumnName(rec, mapCol.get("InRelationTo")).split("\\s*,\\s*"));
			String answer = "";
			for (String i : items){
				answer += kbPrefix + "DASO-" + i.replace("_","-").replace("??", "") +  " & ";
			}
			return answer.substring(0, answer.length() - 3);
			//    		return kbPrefix + "DASO-" + items.get(0).replace("_","-").replace("??", "");
		}
	}

	private String getWasDerivedFrom(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("WasDerivedFrom"));
	}

	private String getWasGeneratedBy(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("WasGeneratedBy"));
	}

	private Boolean checkVirtual(CSVRecord rec) {
		if (getLabel(rec).contains("??")){
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List< Map<String, Object> > createRows() throws Exception {
		rows.clear();
		int row_number = 0;
		for (CSVRecord record : records) {
			if (timeList.contains(getLabel(record)) && getLabel(record).length()>0){
				rows.add(createRow(record, ++row_number));
			}
		}
		
		for (Entry<String, List<String>> entry : tlm.entrySet()) {
			if (entry.getKey().startsWith("??")){
				rows.add(createTimeLineRow(entry, ++row_number));				
			}
		}
	    
		Iterator<Map<String, Object>> iterrrr = rows.iterator();
		while(iterrrr.hasNext()){
			System.out.println(iterrrr.next().entrySet().toString());
		}

		return rows;
	}
	
	public List<String> createUris() throws Exception {
		List<String> result = new ArrayList<String>();
		for (CSVRecord record : records) {
			if (timeList.contains(getLabel(record)) && getLabel(record).length()>0){
				result.add(kbPrefix + "DASE-" + SDDName + "-" + getLabel(record).trim().replace(" ","").replace("_","-").replace("??", ""));
			}
		}
		
		for (Entry<String, List<String>> entry : tlm.entrySet()) {
			if (entry.getKey().startsWith("??")){
				result.add(kbPrefix + "DASE-" + SDDName + "-" + entry.getKey().trim().replace(" ","").replace("_","-").replace("??", ""));				
			}
		}
		
		return result;
	}

	//Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition   
	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DASE-" + SDDName + "-" + getLabel(rec).trim().replace(" ","").replace("_","-").replace("??", ""));
		row.put("a", "hasco:DASchemaEvent");
		row.put("rdfs:label", getLabel(rec).trim().replace(" ","").replace("_","-").replace("??", "")); 
		row.put("rdfs:comment", getLabel(rec).trim().replace(" ","").replace("_","-").replace("??", "")); 
		row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
		row.put("hasco:hasEntity", getEntity(rec));
		row.put("hasco:hasUnit", getUnit(rec));
		row.put("sio:inRelationTo", getInRelationTo(rec));
		row.put("sio:Relation", getRelation(rec));
		row.put("hasco:isVirtual", checkVirtual(rec).toString());
		row.put("hasco:isPIConfirmed", "false");
		return row;
	}
	
	Map<String, Object> createTimeLineRow(Entry<String, List<String>> entry, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DASE-" + SDDName + "-" + entry.getKey().trim().replace(" ","").replace("_","-").replace("??", ""));
		row.put("a", "hasco:DASchemaEvent");
		row.put("rdfs:label", entry.getValue().get(0));
		row.put("rdfs:comment", entry.getValue().get(0)); 
		row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
		row.put("hasco:hasEntity", entry.getValue().get(1).trim().replace(" ",""));
		System.out.println("till now all good..");
		row.put("hasco:hasUnit", entry.getValue().get(2).trim().replace(" ",""));
		System.out.println("till now all good2..");
		row.put("sio:inRelationTo", "");
		row.put("sio:Relation", "");
		row.put("hasco:isVirtual", "true");
		row.put("hasco:isPIConfirmed", "false");
		System.out.println("till now all good3..");
		return row;
	}
}
