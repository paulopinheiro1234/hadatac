package org.hadatac.data.loader;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.annotator.AutoAnnotator;

import java.lang.String;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class DASchemaObjectGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	String startTime = "";
	String SDDName = "";
	String study_id = "";
	
	public DASchemaObjectGenerator(File file) {
		super(file);
		this.SDDName = file.getName();
		this.study_id = AutoAnnotator.study_id;
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
//        mapCol.put("??mother", "chear-kb:ObjectTypeMother");
//        mapCol.put("??child", "chear-kb:ObjectTypeChild");
//        mapCol.put("??birth", "chear-kb:ObjectTypeBirth");
//        mapCol.put("??household", "chear-kb:ObjectTypeHousehold");
//        mapCol.put("??headhousehold", "chear-kb:ObjectTypeHeadHousehold");
//        mapCol.put("??father", "chear-kb:ObjectTypeFather");
	}
    
    private String getLabel(CSVRecord rec) {
    	return rec.get(mapCol.get("Label"));
    }
    
    private String getAttribute(CSVRecord rec) {
    	return rec.get(mapCol.get("AttributeType"));
    }
    
    private String getUnit(CSVRecord rec) {
    	if (rec.get(mapCol.get("Unit")) != null) {
    		return rec.get(mapCol.get("Unit"));
    	} else {
    		return "";
    	}
    }
    
    private String getTime(CSVRecord rec) {
    	return rec.get(mapCol.get("Time"));
    }
    
    private String getEntity(CSVRecord rec) {
    	return rec.get(mapCol.get("Entity"));
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
    			answer += kbPrefix + "DASO-" + i.replace("_","-").replace("??", "") + "-" + study_id + " & ";
    		}
    		return answer.substring(0, answer.length() - 3);
//    		return kbPrefix + "DASO-" + items.get(0).replace("_","-").replace("??", "") + "-" + study_id;
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
    		if (getEntity(record)  == null || getEntity(record).equals("")){
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
    	row.put("hasURI", kbPrefix + "DASO-" + getLabel(rec).replace("_","-").replace("??", "") + "-" + study_id);
    	row.put("a", "hasco:DASchemaObject");
    	row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName.replace(".csv", ""));
    	row.put("hasco:hasEntity", getEntity(rec));
    	row.put("hasco:hasRole", getRole(rec));
    	row.put("sio:inRelationTo", getInRelationTo(rec));
    	row.put("sio:Relation", getRelation(rec));
    	row.put("hasco:isVirtual", checkVirtual(rec).toString());
    	row.put("hasco:isPIConfirmed", "false");
    	
    	return row;
    }
}