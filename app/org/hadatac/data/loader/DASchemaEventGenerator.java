package org.hadatac.data.loader;

import org.hadatac.utils.ConfigProp;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

public class DASchemaEventGenerator extends BasicGenerator {

	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	List<String> timeList = new ArrayList<String>();
	Map<String, String> codeMap;
	Map<String, List<String>> tlm;

	public DASchemaEventGenerator(
			RecordFile dd, 
			Map<String, List<String>> tlm, 
			String SDDName, 
			Map<String, String> codeMap) {
		super(dd);
		this.codeMap = codeMap;
		this.SDDName = SDDName;
		this.tlm = tlm;
		System.out.println("tlm key size : " + tlm.keySet().size());
		
		for (Record rec : dd.getRecords()) {
			if (!rec.getValueByColumnName("Time").isEmpty()) {
                timeList.add(rec.getValueByColumnName("Time"));
            }
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

	private String getLabel(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Label"));
	}

	private String getAttribute(Record rec) {
		return rec.getValueByColumnName(mapCol.get("AttributeType"));
	}

	private String getUnit(Record rec) {
		if (codeMap.containsKey(rec.getValueByColumnName(mapCol.get("Unit")))) {
			return codeMap.get(rec.getValueByColumnName(mapCol.get("Unit")));
		} else if (rec.getValueByColumnName(mapCol.get("Unit")) != null){
			return rec.getValueByColumnName(mapCol.get("Unit"));
		}
		
		return "";
	}

	private String getTime(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Time"));
	}

	private String getEntity(Record rec) {
		if (rec.getValueByColumnName(mapCol.get("Entity")).equals("")) {
			return null;
		} else {
			if (codeMap.containsKey(rec.getValueByColumnName(mapCol.get("Entity")))) {
				return codeMap.get(rec.getValueByColumnName(mapCol.get("Entity")));
			} else {
				return rec.getValueByColumnName(mapCol.get("Entity"));
			}
		}
	}

	private String getRole(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Role"));
	}

	private String getRelation(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Relation"));
	}

	private String getInRelationTo(Record rec) {
		if (rec.getValueByColumnName(mapCol.get("InRelationTo")).equals("")){
			return "";
		} else {
			List<String> items = Arrays.asList(rec.getValueByColumnName(mapCol.get("InRelationTo")).split("\\s*,\\s*"));
			String answer = "";
			for (String i : items){
				answer += kbPrefix + "DASO-" + i.replace("_","-").replace("??", "") +  " & ";
			}
			return answer.substring(0, answer.length() - 3);
		}
	}

	private String getWasDerivedFrom(Record rec) {
		return rec.getValueByColumnName(mapCol.get("WasDerivedFrom"));
	}

	private String getWasGeneratedBy(Record rec) {
		return rec.getValueByColumnName(mapCol.get("WasGeneratedBy"));
	}

	private Boolean checkVirtual(Record rec) {
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
		for (Record record : records) {
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
		for (Record record : records) {
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
	Map<String, Object> createRow(Record rec, int row_number) throws Exception {
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

	@Override
	public String getTableName() {
		return "DASchemaEvent";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "Error in DASchemaEventGenerator: " + e.getMessage();
	}
}
