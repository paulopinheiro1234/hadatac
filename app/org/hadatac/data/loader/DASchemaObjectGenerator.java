package org.hadatac.data.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.DASVirtualObject;

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


public class DASchemaObjectGenerator extends BasicGenerator {
	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	Map<String, String> codeMap;

	// the DASOGenerator object for each study will have java objects of all the templates, too
	List<DASVirtualObject> templateList = new ArrayList<DASVirtualObject>();
	List<String> timeList = new ArrayList<String>();

	public DASchemaObjectGenerator(File file, String SDDName, Map<String, String> codeMap) {
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
                    //System.out.println("[DASOGenerator] adding to timeList: " + current.get("Time"));
                }
            }

            dict.close();
			br.close();
		} catch (Exception e) {
            System.out.println("[DASObjectGenerator] Error opening SDD file");
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
	}

	private String getLabel(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Label"));
	}

	private String getAttribute(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("AttributeType"));
	}

	private String getUnit(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Unit"));
	}

	private String getTime(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("Time"));
	}

	private String getEntity(CSVRecord rec) {
		String entity = getValueByColumnName(rec, mapCol.get("Entity"));
		if (entity.length() == 0) {
			return null;
		} else {
			if (codeMap.containsKey(entity)) {
				System.out.println("[DASOGenerator] code matched: " + entity); 
				return codeMap.get(entity);
			} else {
				return entity;
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
				items.add(kbPrefix + "DASO-" + SDDName + "-" + item.replace("_","-").replace("??", ""));
			}
			return String.join(" & ", items);
		}
	}

	private String getWasDerivedFrom(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("WasDerivedFrom"));
	}

	private String getWasGeneratedBy(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("WasGeneratedBy"));
	}
   
	public String getSDDName(){
		return this.SDDName;
	}

	public List<DASVirtualObject> getTemplateList(){
		return this.templateList;
	}
    
	private Boolean checkVirtual(CSVRecord rec) {
		if (getLabel(rec).contains("??")){
			return true;
		} else {
			return false;
		}
	}

	private String getPosition(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("HasPosition"));
	}

	@Override
	public List< Map<String, Object> > createRows() throws Exception {
		rows.clear();
		int row_number = 0;
		for (CSVRecord record : records) {
			if (getEntity(record)  == null || getEntity(record).equals("")  || timeList.contains(getLabel(record))){
                //System.out.println("[DASOGenerator] getEntity(record) = " + getEntity(record) + ", so skipping....");
				continue;
			} else {
                //System.out.println("[DASOGenerator] creating a row....");
				rows.add(createRow(record, ++row_number));
			}
		}
        System.out.println("[DASOGenerator] Added " + row_number + " rows!");
		return rows;
	}

	//Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition   
	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DASO-" + SDDName + "-" + getLabel(rec).trim().replace(" ","").replace("_","-").replace("??", ""));
		row.put("a", "hasco:DASchemaObject");
		row.put("rdfs:label", getLabel(rec).trim().replace(" ","").replace("_","-").replace("??", ""));
		row.put("rdfs:comment", getLabel(rec).trim().replace(" ","").replace("_","-").replace("??", ""));
		row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
		row.put("hasco:hasEntity", getEntity(rec));
		row.put("hasco:hasRole", getRole(rec));
		row.put("sio:inRelationTo", getInRelationTo(rec));
		if (getInRelationTo(rec).length() > 0) {
			if (getRelation(rec).length() > 0) {
				row.put("sio:Relation", getRelation(rec));
			} else {
				row.put("sio:Relation", "sio:inRelationTo");
			}
		}
		row.put("hasco:hasUnit", getUnit(rec));
		row.put("hasco:isVirtual", checkVirtual(rec).toString());
		row.put("hasco:hasPosition", getPosition(rec));
		row.put("hasco:isPIConfirmed", "false");

		// Also generate a DASVirtualObject for each virtual column
		if(checkVirtual(rec)) {
			row.put("dcterms:alternativeName", getLabel(rec).trim().replace(" ",""));
			//System.out.println("[DASOGen] getTime = " + getTime(rec));
			row.put("sio:existsAt", getTime(rec));
			DASVirtualObject toAdd = new DASVirtualObject(getLabel(rec).trim().replace(" ",""), row);
			templateList.add(toAdd);
			//System.out.println("[DASOGenerator] created template: \n" + toAdd);
		}

		return row;
	}
}
