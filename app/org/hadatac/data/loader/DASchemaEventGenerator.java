package org.hadatac.data.loader;

import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class DASchemaEventGenerator extends BaseGenerator {

	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	List<String> timeList = new ArrayList<String>();
	Map<String, String> codeMap;
	Map<String, Map<String, String>> mapTimeline;

	public DASchemaEventGenerator(
			RecordFile dd, 
			Map<String, Map<String, String>> mapTimeline, 
			String SDDName, 
			Map<String, String> codeMap) {
		super(dd);
		this.codeMap = codeMap;
		this.SDDName = SDDName;
		this.mapTimeline = mapTimeline;
		System.out.println("mapTimeline key size : " + mapTimeline.keySet().size());
		
		for (Record rec : dd.getRecords()) {
		    if (rec.getValueByColumnName("Time") != null && !rec.getValueByColumnName("Time").isEmpty()) {
			timeList.add(rec.getValueByColumnName("Time"));
		    }
		}
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
			String answer = String.join(" & ", items.stream()
			        .map(i -> kbPrefix + "DASO-" + i.replace("_", "-").replace("??", ""))
			        .collect(Collectors.toList()));
			return answer;
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
	public HADatAcThing createObject(Record rec, int rowNumber) throws Exception {
	    if (timeList.contains(getLabel(rec)) && getLabel(rec).length() > 0){
	        return createDASEObject(rec);
        }
	    
	    return null;
    }

    @Override
    public void postprocess() throws Exception {
        for (Entry<String, Map<String, String>> entry : mapTimeline.entrySet()) {
            if (entry.getKey().startsWith("??")) {
                objects.add(createDASEObject(entry));
            }
        }
    }
    
    HADatAcThing createDASEObject(Record rec) throws Exception {
        DataAcquisitionSchemaEvent dase = new DataAcquisitionSchemaEvent();
        dase.setUri(URIUtils.replacePrefixEx(kbPrefix + "DASE-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_", "-").replace("??", "")));
        dase.addType(URIUtils.replacePrefixEx("hasco:DASchemaEvent"));
        dase.setLabel(getLabel(rec).trim().replace(" ","").replace("_", "-").replace("??", ""));
        dase.setComment(getLabel(rec));
        dase.setPartOfSchema(URIUtils.replacePrefixEx(kbPrefix + "DAS-" + SDDName));
        dase.setEntity(URIUtils.replacePrefixEx(getEntity(rec)));
        dase.setUnit(URIUtils.replacePrefixEx(getUnit(rec)));
        dase.setInRelationToUri(URIUtils.replacePrefixEx(getInRelationTo(rec)));
        dase.setRelationUri(URIUtils.replacePrefixEx(getRelation(rec)));
        dase.setIsVirtual(checkVirtual(rec).toString());
        dase.setIsPIConfirmed("false");
        
        return dase;
    }
    
    HADatAcThing createDASEObject(Entry<String, Map<String, String>> entry) throws Exception {
        DataAcquisitionSchemaEvent dase = new DataAcquisitionSchemaEvent();
        dase.setUri(URIUtils.replacePrefixEx(kbPrefix + "DASE-" + SDDName + "-" + entry.getKey().trim().replace(" ", "").replace("_", "-").replace("??", "").replace(":", "-")));
        dase.addType(URIUtils.replacePrefixEx("hasco:DASchemaEvent"));
        dase.addType(URIUtils.replacePrefixEx(entry.getValue().get("Type").trim().replace(" ", "")));
        dase.setLabel(entry.getValue().get("Label"));
        dase.setComment(entry.getValue().get("Comment"));
        dase.setPartOfSchema(URIUtils.replacePrefixEx(kbPrefix + "DAS-" + SDDName));
        dase.setUnit(URIUtils.replacePrefixEx(entry.getValue().get("Unit").trim().replace(" ", "")));
        dase.setInRelationToUri(URIUtils.replacePrefixEx(kbPrefix + "DASO-" + SDDName + "-" + entry.getValue().get("inRelationTo").trim().replace(" ","").replace("_","-").replace("??", "").replace(":", "-")));
        dase.setRelationUri(URIUtils.replacePrefixEx("sio:inRelationTo"));
        dase.setIsVirtual("true");
        dase.setIsPIConfirmed("false");
        
        return dase;
    }
	
	public List<String> createUris() throws Exception {
		List<String> result = new ArrayList<String>();
		for (Record record : records) {
			if (timeList.contains(getLabel(record)) && getLabel(record).length()>0){
				result.add(kbPrefix + "DASE-" + SDDName + "-" + getLabel(record).trim().replace(" ","").replace("_","-").replace("??", ""));
			}
		}
		
		for (Entry<String, Map<String, String>> entry : mapTimeline.entrySet()) {
			if (entry.getKey().startsWith("??")){
				result.add(kbPrefix + "DASE-" + SDDName + "-" + entry.getKey().trim().replace(" ","").replace("_","-").replace("??", ""));				
			}
		}
		
		return result;
	}
	
	Map<String, Object> createTimeLineRow(Entry<String, Map<String, String>> entry, int rowNumber) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DASE-" + SDDName + "-" + entry.getKey().trim().replace(" ","").replace("_","-").replace("??", "").replace(":", "-"));
		row.put("a", "hasco:DASchemaEvent");
		row.put("rdfs:label", entry.getValue().get("Label"));
		row.put("rdfs:comment", entry.getValue().get("Comment"));
		row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
		row.put("a", entry.getValue().get("Type").trim().replace(" ", ""));
		row.put("hasco:hasUnit", entry.getValue().get("Unit").trim().replace(" ",""));
		row.put("sio:inRelationTo", kbPrefix + "DASO-" + SDDName + "-" + entry.getValue().get("inRelationTo").trim().replace(" ","").replace("_","-").replace("??", "").replace(":", "-"));
		row.put("sio:Relation", "sio:inRelationTo");
		row.put("hasco:isVirtual", "true");
		row.put("hasco:isPIConfirmed", "false");
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
