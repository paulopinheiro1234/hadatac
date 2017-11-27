package org.hadatac.data.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;

public class DASchemaAttrGenerator extends BasicGenerator {

	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	Map<String, String> codeMap;
	Map<String, String> hasEntityMap = new HashMap<String, String>();

	public DASchemaAttrGenerator(File file, String SDDName, Map<String, String> codeMap) {
		super(file);
		this.codeMap = codeMap;
		this.SDDName = SDDName;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line =  null;

			while((line = br.readLine()) != null){
				String str[] = line.split(",");
				if (str[5].length() > 0){
					hasEntityMap.put(str[0], str[5]);
				}
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Error Reading File");			
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
	}

	private String getLabel(CSVRecord rec) {
		return rec.get(mapCol.get("Label"));
	}

	private String getAttribute(CSVRecord rec) {
		return rec.get(mapCol.get("AttributeType"));
	}

	private String getAttributeOf(CSVRecord rec) {
		if (rec.get(mapCol.get("AttributeOf")) == null || rec.get(mapCol.get("AttributeOf").trim()).equals("")) {
			return "";
		}
		//System.out.println("DASchemaAttrGenerator: getAttributeOf() = " + SDDName + "-" + rec.get(mapCol.get("AttributeOf")).replace("??", ""));
		return kbPrefix + "DASO-" + SDDName + "-" + rec.get(mapCol.get("AttributeOf")).replace("??", "");
	}

	private String getUnit(CSVRecord rec) {
		String original = rec.get(mapCol.get("Unit"));
		String expansion = ValueCellProcessing.replacePrefixEx(rec.get(mapCol.get("Unit")));
		if (original.length() != expansion.length()) {
			return expansion;
		} else if (codeMap.containsKey(original)) {
			return codeMap.get(original);
		}
			return "obo:UO_0000186";
	}

	private String getTime(CSVRecord rec) {
		if (rec.get(mapCol.get("Time")) == null || rec.get(mapCol.get("Time").trim()).equals("")) {
			return "";
		}
		//System.out.println("DASchemaAttrGenerator: getTime() = " + SDDName + "-" + rec.get(mapCol.get("Time")).trim().replace(" ","").replace("_","-").replace("??", ""));
		return kbPrefix + "DASE-" + SDDName + "-" + rec.get(mapCol.get("Time")).trim().replace(" ","").replace("_","-").replace("??", "");
	}

	private String getEntity(CSVRecord rec) {
		if ((rec.get(mapCol.get("AttributeOf"))) == null || (rec.get(mapCol.get("AttributeOf"))).equals("")) {
			return "chear:unknownEntity";
		} else {
			if (codeMap.containsKey(hasEntityMap.get(rec.get(mapCol.get("AttributeOf"))))) {
				return codeMap.get(hasEntityMap.get(rec.get(mapCol.get("AttributeOf"))));
			} else {
				if (hasEntityMap.containsKey(rec.get(mapCol.get("AttributeOf")))){
					return hasEntityMap.get(rec.get(mapCol.get("AttributeOf")));
				} else {
					return rec.get(mapCol.get("AttributeOf"));
				}

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
		return rec.get(mapCol.get("InRelationTo"));
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
			if (getAttribute(record)  == null || getAttribute(record).equals("")){
				continue;
			} else {
				rows.add(createRow(record, ++row_number));
			}
		}

		return rows;
	}

	//Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition   
	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", ""));
		row.put("a", "hasco:DASchemaAttribute");
		row.put("rdfs:label", getLabel(rec));
		row.put("rdfs:comment", getLabel(rec));
		row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
		row.put("hasco:hasPosition", getPosition(rec));
		row.put("hasco:hasEntity", getEntity(rec));
		row.put("hasco:hasAttribute", getAttribute(rec));
		row.put("hasco:hasUnit", getUnit(rec));
		row.put("hasco:hasEvent", getTime(rec));
		row.put("hasco:hasSource", "");
		row.put("hasco:isAttributeOf", getAttributeOf(rec));
		row.put("hasco:isVirtual", checkVirtual(rec).toString());
		row.put("hasco:isPIConfirmed", "false");

		return row;
	}
}
