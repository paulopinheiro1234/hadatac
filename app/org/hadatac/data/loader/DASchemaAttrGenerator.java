package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class DASchemaAttrGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	String startTime = "";
	String SDDName = "";
	
	public DASchemaAttrGenerator(File file) {
		super(file);
		this.SDDName = file.getName();
	}
	
	@Override
	void initMapping() {
		mapCol.clear();
        mapCol.put("DASchemaAttribute", "Column");
        mapCol.put("Label", "Label");
        mapCol.put("Attribute", "Attribute");
        mapCol.put("AttributeOf", "attributeOf");
        mapCol.put("Unit", "Unit");
        mapCol.put("Time", "Time");
        mapCol.put("Entity", "Entity");
        mapCol.put("Role", "Role");
        mapCol.put("Relation", "Relation");
        mapCol.put("InRelationTo", "inRelationTo");
        mapCol.put("WasDerivedFrom", "wasDerivedFrom");       
        mapCol.put("WasGeneratedBy", "wasGeneratedBy");
        mapCol.put("??mother", "chear-kb:ObjectTypeMother");
        mapCol.put("??child", "chear-kb:ObjectTypeChild");
        mapCol.put("??birth", "chear-kb:ObjectTypeBirth");
        mapCol.put("??household", "chear-kb:ObjectTypeHousehold");
        mapCol.put("??headhousehold", "chear-kb:ObjectTypeHeadHousehold");
        mapCol.put("??father", "chear-kb:ObjectTypeFather");
	}
	
    private String getDASchemaName(CSVRecord rec) {
    	return rec.get(mapCol.get("DASchemaAttribute"));
    }
    
    private String getLabel(CSVRecord rec) {
    	return rec.get(mapCol.get("Label"));
    }
    
    private String getAttribute(CSVRecord rec) {
    	return rec.get(mapCol.get("Attribute"));
    }
    
    private String getAttributeOf(CSVRecord rec) {
    	if (mapCol.containsKey(rec.get(mapCol.get("AttributeOf")))){
    		return mapCol.get(rec.get(mapCol.get("AttributeOf")));
    	} else {
    		return "";
    	}
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
    	return rec.get(mapCol.get("InRelationTo"));
    }
    
    private String getWasDerivedFrom(CSVRecord rec) {
    	return rec.get(mapCol.get("WasDerivedFrom"));
    }
    
    private String getWasGeneratedBy(CSVRecord rec) {
    	return rec.get(mapCol.get("WasGeneratedBy"));
    }
    
    private String getAssociatedObject(CSVRecord rec) {
    	return getAttributeOf(rec);
    }
    
    @Override
    Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", kbPrefix + "DASA-" + getDASchemaName(rec));
    	row.put("a", "hasneto:DASchemaAttribute");
    	row.put("rdfs:label", getDASchemaName(rec));
    	row.put("rdfs:comment", getLabel(rec));
    	row.put("hasneto:partOfSchema", kbPrefix + "DAS-" + SDDName);
    	row.put("hasco:hasPosition", String.valueOf(row_number));
    	row.put("hasneto:hasEntity", getEntity(rec));
    	row.put("hasneto:hasAttribute", getAttribute(rec));
    	row.put("hasneto:hasUnit", getUnit(rec));
    	row.put("hasco:hasSource", "");
    	row.put("hasco:hasAssociatedObject", getAssociatedObject(rec));
    	row.put("hasco:isPIConfirmed", "false");
    	
    	return row;
    }
}