package org.hadatac.data.loader;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.DASVirtualObject;
import org.hadatac.entity.pojo.DataFile;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DASchemaObjectGenerator extends BaseGenerator {
	
	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	Map<String, String> codeMap;

	// the DASOGenerator object for each study will have java objects of all the templates, too
	List<DASVirtualObject> templateList = new ArrayList<DASVirtualObject>();
        //List<String> timeList = new ArrayList<String>();

	public DASchemaObjectGenerator(DataFile dataFile, String SDDName, Map<String, String> codeMap) {
		super(dataFile);
		this.codeMap = codeMap;
		this.SDDName = SDDName;
		
		//for (Record rec : file.getRecords()) {
		//    if (rec.getValueByColumnName("Time") != null && !rec.getValueByColumnName("Time").isEmpty()) {
		//	timeList.add(rec.getValueByColumnName("Time"));
		//    }
		//}
	}
    
	//Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition	
	@Override
	public void initMapping() {
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
		return rec.getValueByColumnName(mapCol.get("Unit"));
	}

	private String getTime(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Time"));
	}
	
	private String getEntity(Record rec) {
		String entity = rec.getValueByColumnName(mapCol.get("Entity"));
		if (entity != null && entity.length() == 0) {
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

	private String getRole(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Role"));
	}

	public String getRelation(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Relation"));
	}

	public String getInRelationTo(Record rec) {
		String inRelationTo = rec.getValueByColumnName(mapCol.get("InRelationTo"));
		if (inRelationTo.length() == 0) {
			return "";
		} else {
			List<String> items = new ArrayList<String>();
			for (String item : Arrays.asList(inRelationTo.split("\\s*,\\s*"))) {
				items.add(kbPrefix + "DASO-" + SDDName + "-" + item.replace("_","-").replace("??", ""));
			}
			
			return items.get(0);
		}
	}
	
	public String getInRelationToString(Record rec) {
		String inRelationTo = rec.getValueByColumnName(mapCol.get("InRelationTo"));
		if (inRelationTo.length() == 0) {
			return "";
		} else {
			return inRelationTo;
		}
	}

	private String getWasDerivedFrom(Record rec) {
		return rec.getValueByColumnName(mapCol.get("WasDerivedFrom"));
	}

	private String getWasGeneratedBy(Record rec) {
		return rec.getValueByColumnName(mapCol.get("WasGeneratedBy"));
	}
   
	public String getSDDName(){
		return this.SDDName;
	}

	public List<DASVirtualObject> getTemplateList(){
		return this.templateList;
	}
    
	private Boolean checkVirtual(Record rec) {
		if (getLabel(rec).contains("??")){
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void createRows() throws Exception {
	    rows.clear();
	    int rowNumber = 0;
	    List<String> column_name = new ArrayList<String>();
	    for (Record record : records) {
		System.out.println("[DASOGenerator] Processing [" + getLabel(record) + "]");
		//if (getEntity(record)  == null || getEntity(record).equals("")  || timeList.contains(getLabel(record))){
		if (getEntity(record)  == null || getEntity(record).equals("")) {
		    System.out.println("[DASOGenerator]     getEntity(record) = [" + getEntity(record) + "]");
		    if (column_name.contains(getLabel(record))){
			rows.add(createRelationRow(record, ++rowNumber));
		    }
		} else {
		    System.out.println("[DASOGenerator]     creating a row....");
		    rows.add(createRow(record, ++rowNumber));
		    column_name.add(getLabel(record));
		}
	    }
	    
	    System.out.println("[DASOGenerator] Added " + rowNumber + " rows!");
	}
    
        public List<String> createUris() throws Exception {
	    List<String> result = new ArrayList<String>();
	    for (Record record : records) {
		//if (getEntity(record)  == null || getEntity(record).equals("")  || timeList.contains(getLabel(record))){
		if (getEntity(record)  == null || getEntity(record).equals("")) {
		    continue;
		} else {
		    result.add(kbPrefix + "DASO-" + SDDName + "-" + getLabel(record).trim().replace(" ","").replace("_","-").replace("??", ""));
		}
	    }
	    return result;
	}
    
	//Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition   
	@Override
	public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DASO-" + SDDName + "-" + getLabel(rec).trim().replace(" ","").replace("_","-").replace("??", ""));
		row.put("a", "hasco:DASchemaObject");
		row.put("rdfs:label", getLabel(rec));
		row.put("rdfs:comment", getLabel(rec).trim().replace(" ","").replace("_","-").replace("??", ""));
		row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
		row.put("hasco:hasEntity", getEntity(rec));
		row.put("hasco:hasRole", getRole(rec));
		System.out.println("[DASOGenerator] [createRow] getRelation: [" + getRelation(rec) + "]    inRelationTo: [" + getInRelationTo(rec) + "]    inRelationToString: [" + getInRelationToString(rec) + "]");
		if (getRelation(rec).length() > 0) {
			if (getInRelationTo(rec).length() > 0) {
				row.put(getRelation(rec), getInRelationTo(rec));
				row.put("hasco:inRelationToLabel", getInRelationToString(rec));
				row.put("sio:Relation", getRelation(rec));
			}
		} else {
			if (getInRelationTo(rec).length() > 0) {
				row.put("sio:inRelationTo", getInRelationTo(rec));
				row.put("hasco:inRelationToLabel", getInRelationToString(rec));
				row.put("sio:Relation", "sio:inRelationTo");
			}
		}
		row.put("hasco:hasUnit", getUnit(rec));
		row.put("hasco:isVirtual", checkVirtual(rec).toString());
		row.put("hasco:isPIConfirmed", "false");
		row.put("hasco:wasDerivedFrom", getWasDerivedFrom(rec));

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
	
	Map<String, Object> createRelationRow(Record rec, int rowNumber) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DASO-" + SDDName + "-" + getLabel(rec).trim().replace(" ","").replace("_","-").replace("??", ""));
		if (getRelation(rec).length() > 0) {
			if (getInRelationTo(rec).length() > 0) {
				row.put(getRelation(rec), getInRelationTo(rec));
				row.put("hasco:inRelationToLabel", getInRelationToString(rec));
				row.put("sio:Relation", getRelation(rec));
			}
		} else {
			if (getInRelationTo(rec).length() > 0) {
				row.put("sio:inRelationTo", getInRelationTo(rec));
				row.put("hasco:inRelationToLabel", getInRelationToString(rec));
				row.put("sio:Relation", "sio:inRelationTo");
			}
		}
		return row;
	}

	@Override
	public String getTableName() {
		return "DASchemaObject";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "Error in DASchemaObjectGenerator: " + e.getMessage();
	}
}
