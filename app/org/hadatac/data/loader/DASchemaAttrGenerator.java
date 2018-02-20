package org.hadatac.data.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

public class DASchemaAttrGenerator extends BasicGenerator {

	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	Map<String, String> codeMap;
	Map<String, List<String>> hasEntityMap = new HashMap<String, List<String>>();
	Map<String, String> currentHasEntity = new HashMap<String, String>();

	public DASchemaAttrGenerator(File file, String SDDName, Map<String, String> codeMap) {
		super(file);
		this.codeMap = codeMap;
		this.SDDName = SDDName;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line =  "";
			while((line = br.readLine()) != null){
				String str[] = line.split(",", -1);
				List<String> tmp = new ArrayList<String>();
				tmp.add(str[2]);
				tmp.add(str[5]);
				hasEntityMap.put(str[0], tmp);
				System.out.println(str[0] + " *** " + tmp);
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
	}

	private String getLabel(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Label"));
	}

	private String getAttribute(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("AttributeType"));
	}

	private String getAttributeOf(CSVRecord rec) {
		if (getValueByColumnName(rec, mapCol.get("AttributeOf").trim()).equals("")) {
			return "";
		}
		//System.out.println("DASchemaAttrGenerator: getAttributeOf() = " + SDDName + "-" + rec.get(mapCol.get("AttributeOf")).replace("??", ""));
		return kbPrefix + "DASO-" + SDDName + "-" + getValueByColumnName(rec, mapCol.get("AttributeOf")).replace(" ", "").replace("_","-").replace("??", "");
	}

	private String getUnit(CSVRecord rec) {
		String original = getValueByColumnName(rec, mapCol.get("Unit"));
		if (URIUtils.isValidURI(original)) {
			return original;
		} else if (codeMap.containsKey(original)) {
			return codeMap.get(original);
		}
		
		return "";
	}

	private String getTime(CSVRecord rec) {
		if (getValueByColumnName(rec, mapCol.get("Time").trim()).equals("")) {
			return "";
		}
		//System.out.println("DASchemaAttrGenerator: getTime() = " + SDDName + "-" + rec.get(mapCol.get("Time")).trim().replace(" ","").replace("_","-").replace("??", ""));
		return kbPrefix + "DASE-" + SDDName + "-" + getValueByColumnName(rec, mapCol.get("Time")).trim().replace(" ","").replace("_","-").replace("??", "");
	}

	private String getEntity(CSVRecord rec) {
		if (getValueByColumnName(rec, mapCol.get("AttributeOf")).equals("")) {
			currentHasEntity.put(getLabel(rec), "chear:unknownEntity");
			return "chear:unknownEntity";
		} else {
			if (codeMap.containsKey(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))))) {
				System.out.println("codeMap: " + codeMap.get(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf")))));
				currentHasEntity.put(getLabel(rec), codeMap.get(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf")))));
				return codeMap.get(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))));
			} else {
//				System.out.println(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))).get(1));
				if (hasEntityMap.containsKey(getValueByColumnName(rec, mapCol.get("AttributeOf")))){
					if(codeMap.containsKey(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))).get(1))){
						currentHasEntity.put(getLabel(rec), codeMap.get(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))).get(1)));
						return codeMap.get(hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))).get(1));
					}
					currentHasEntity.put(getLabel(rec), hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))).get(1));
					return hasEntityMap.get(getValueByColumnName(rec, mapCol.get("AttributeOf"))).get(1);
				}
				currentHasEntity.put(getLabel(rec), "chear:unknownEntity");
				return "chear:unknownEntity";
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
		String inRelationTo = getValueByColumnName(rec, mapCol.get("InRelationTo"));
		if (inRelationTo.length() == 0) {
			return "";
		} else {
			List<String> items = new ArrayList<String>();
			for (String item : Arrays.asList(inRelationTo.split("\\s*,\\s*"))) {
				items.add(kbPrefix + "DASO-" + SDDName + "-" + item.replace(" ", "").replace("_","-").replace("??", ""));
			}
			return String.join(" & ", items);
		}
	}

	private String getWasDerivedFrom(CSVRecord rec) {
		//replace & with , for excel approach
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
		List<String> column_name = new ArrayList<String>();
		int row_number = 0;
		for (CSVRecord record : records) {
			if (getAttribute(record)  == null || getAttribute(record).equals("")){
				if (column_name.contains(getLabel(record))){
					rows.add(createRelationRow(record, ++row_number));
				}
				continue;
			} else {
				rows.add(createRow(record, ++row_number));
				column_name.add(getLabel(record));
			}
		}
		System.out.println("rows 1 " + rows);
		return rows;
	}
	
	public List<String> createUris() throws Exception {
		List<String> result = new ArrayList<String>();
		for (CSVRecord record : records) {
			if (getAttribute(record)  == null || getAttribute(record).equals("")){
				continue;
			} else {
				result.add(kbPrefix + "DASA-" + SDDName + "-" + getLabel(record).trim().replace(" ", "").replace("_","-").replace("??", ""));
			}
		}
		return result;
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
		if (!currentHasEntity.containsKey(getLabel(rec))){
			row.put("hasco:hasEntity", getEntity(rec));
		}
		if (getRelation(rec).length() > 0) {
			row.put(getRelation(rec), getInRelationTo(rec));
		} else {
			row.put("sio:inRelationTo", getInRelationTo(rec));
		}
		if (getInRelationTo(rec).length() > 0) {
			if (getRelation(rec).length() > 0) {
				row.put("sio:Relation", getRelation(rec));
			} else {
				row.put("sio:Relation", "sio:inRelationTo");
			}
		}
		row.put("hasco:hasAttribute", getAttribute(rec));
		row.put("hasco:hasUnit", getUnit(rec));
		row.put("hasco:hasEvent", getTime(rec));
		row.put("hasco:hasSource", "");
		row.put("hasco:isAttributeOf", getAttributeOf(rec));
		row.put("hasco:isVirtual", checkVirtual(rec).toString());
		row.put("hasco:isPIConfirmed", "false");

		return row;
	}
	
	Map<String, Object> createRelationRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", ""));
		if (getRelation(rec).length() > 0) {
			row.put(getRelation(rec), getInRelationTo(rec));
		} else {
			row.put("sio:inRelationTo", getInRelationTo(rec));
		}
		if (getInRelationTo(rec).length() > 0) {
			if (getRelation(rec).length() > 0) {
				row.put("sio:Relation", getRelation(rec));
			} else {
				row.put("sio:Relation", "sio:inRelationTo");
			}
		}

		return row;
	}
}
