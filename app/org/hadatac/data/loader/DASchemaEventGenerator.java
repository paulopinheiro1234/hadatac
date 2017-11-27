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

	public DASchemaEventGenerator(File file, String SDDName, Map<String, String> codeMap) {
		super(file);
		this.codeMap = codeMap;
		this.SDDName = SDDName;

        CSVRecord current = null;

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
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
		mapCol.put("HasPosition", "hasPosition");
		//  mapCol.put("??mother", "chear-kb:ObjectTypeMother");
		//  mapCol.put("??child", "chear-kb:ObjectTypeChild");
		//  mapCol.put("??birth", "chear-kb:ObjectTypeBirth");
		//  mapCol.put("??household", "chear-kb:ObjectTypeHousehold");
		//  mapCol.put("??headhousehold", "chear-kb:ObjectTypeHeadHousehold");
		//  mapCol.put("??father", "chear-kb:ObjectTypeFather");
	}

	private String getLabel(CSVRecord rec) {
		return rec.get(mapCol.get("Label"));
	}

	private String getAttribute(CSVRecord rec) {
		return rec.get(mapCol.get("AttributeType"));
	}

	private String getUnit(CSVRecord rec) {
		if (codeMap.containsKey(rec.get(mapCol.get("Unit")))) {
			return codeMap.get(rec.get(mapCol.get("Unit")));
		} else if (rec.get(mapCol.get("Unit")) != null){
			return rec.get(mapCol.get("Unit"));
		}
		return "obo:UO_0000186";
	}

	private String getTime(CSVRecord rec) {
		return rec.get(mapCol.get("Time"));
	}

	private String getEntity(CSVRecord rec) {
		if ((rec.get(mapCol.get("Entity"))) == null || (rec.get(mapCol.get("Entity"))).equals("")) {
			return null;
		} else {
			if (codeMap.containsKey(rec.get(mapCol.get("Entity")))) {
				return codeMap.get(rec.get(mapCol.get("Entity")));
			} else {
				return rec.get(mapCol.get("Entity"));
			}
		}
	}

	private String getRole(CSVRecord rec) {
		return rec.get(mapCol.get("Role"));
	}

	private String getRelation(CSVRecord rec) {
		return rec.get(mapCol.get("Relation"));
	}

	private String getInRelationTo(CSVRecord rec) {
		if (rec.get(mapCol.get("InRelationTo")) == null || rec.get(mapCol.get("InRelationTo")).equals("")){
			return "";
		} else {
			List<String> items = Arrays.asList(rec.get(mapCol.get("InRelationTo")).split("\\s*,\\s*"));
			String answer = "";
			for (String i : items){
				answer += kbPrefix + "DASO-" + i.replace("_","-").replace("??", "") +  " & ";
			}
			return answer.substring(0, answer.length() - 3);
			//    		return kbPrefix + "DASO-" + items.get(0).replace("_","-").replace("??", "");
		}
	}

	private String getWasDerivedFrom(CSVRecord rec) {
		return rec.get(mapCol.get("WasDerivedFrom"));
	}

	private String getWasGeneratedBy(CSVRecord rec) {
		return rec.get(mapCol.get("WasGeneratedBy"));
	}

	private Boolean checkVirtual(CSVRecord rec) {
		if (getLabel(rec).contains("??")){
			return true;
		} else {
			return false;
		}
	}

	private String getPosition(CSVRecord rec) {
		return rec.get(mapCol.get("HasPosition"));
	}

	@Override
	public List< Map<String, Object> > createRows() throws Exception {
		rows.clear();
		int row_number = 0;
		for (CSVRecord record : records) {
			if (timeList.contains(getLabel(record))){
				rows.add(createRow(record, ++row_number));
			}
		}
		return rows;
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
}
