package org.hadatac.data.loader;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.annotator.AutoAnnotator;
import play.Play;
import java.lang.String;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.csv.CSVRecord;
import org.hadatac.entity.pojo.DASVirtualObject;

public class DASchemaObjectGenerator extends BasicGenerator {

	final String kbPrefix = Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
	String startTime = "";
	String SDDName = "";
	String studyId = "";
	HashMap<String, String> codeMap;
	// the DASOGenerator object for each study will have java objects of all the templates, too
	List<DASVirtualObject> templateList;
    
	public DASchemaObjectGenerator(File file, String study_id) {
		super(file);
		this.studyId = study_id;
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
		// mapCol.put("??mother", "chear-kb:ObjectTypeMother");
		// mapCol.put("??child", "chear-kb:ObjectTypeChild");
		// mapCol.put("??birth", "chear-kb:ObjectTypeBirth");
		// mapCol.put("??household", "chear-kb:ObjectTypeHousehold");
		// mapCol.put("??headhousehold", "chear-kb:ObjectTypeHeadHousehold");
		// mapCol.put("??father", "chear-kb:ObjectTypeFather");
		templateList = new ArrayList<DASVirtualObject>();
	}
   
	public String getSDDName(){
		return this.SDDName;
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

	public List<DASVirtualObject> getTemplateList(){
		return this.templateList;
	}
    
	private String getEntity(CSVRecord rec) {
		if ((rec.get(mapCol.get("Entity"))) == null || (rec.get(mapCol.get("Entity"))).equals("")) {
			return null;
		} else {
			if (codeMap.containsKey(rec.get(mapCol.get("Entity")))) {
				System.out.println("[DASO] code matched: " + rec.get(mapCol.get("Entity"))); 
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
				answer += kbPrefix + "DASO-" + SDDName + "-" + i.replace("_","-").replace("??", "") + " & ";
			}
			return answer.substring(0, answer.length() - 3);
			// return kbPrefix + "DASO-" + items.get(0).replace("_","-").replace("??", "");
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
		SDDName = fileName.replace("SDD-","").replace(".csv","");
		codeMap = AutoAnnotator.codeMappings;
		rows.clear();
		int row_number = 0;
		for (CSVRecord record : records) {
			if (getEntity(record)  == null || getEntity(record).equals("")){
				continue;
			} else {
				rows.add(createRow(record, ++row_number));
			}
		}
		//for(int i = 0; i < templateList.size(); i++){
		//	System.out.println("[DAShemaObjectGenerator] " + templateList.get(i));
		//}
		return rows;
	}// /createRows()
    
    //Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition   
		// CSV record objects here are rows from the SDD file.
    @Override
    Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", kbPrefix + "DASO-" + SDDName + "-" + getLabel(rec).trim().replaceAll("[ ,.]","").replace("_","-").replace("??", ""));
    	row.put("a", "hasco:DASchemaObject");
    	row.put("rdfs:label", getLabel(rec).trim().replace("[ ,.]","").replace("_","-").replace("??", ""));
    	row.put("rdfs:comment", getLabel(rec).trim().replace("[ ,.]","").replace("_","-").replace("??", ""));
    	row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
    	row.put("hasco:hasEntity", getEntity(rec));
    	row.put("hasco:hasRole", getRole(rec));
    	if (getRelation(rec) != null || !getRelation(rec).equals("")){
    		row.put("sio:Relation", getRelation(rec));
    	}
    	if (getInRelationTo(rec) != null || !getInRelationTo(rec).equals("")){
    		row.put("sio:inRelationTo", getInRelationTo(rec));
    	}
//    	row.put("sio:inRelationTo", getInRelationTo(rec));
    	row.put("hasco:isVirtual", checkVirtual(rec).toString());
    	row.put("hasco:isPIConfirmed", "false");

			// Also generate a DASVirtualObject for each virtual column
			if(checkVirtual(rec)){
				row.put("dcterms:alternativeName", getLabel(rec).trim().replace(" ",""));
				DASVirtualObject toAdd = new DASVirtualObject(getLabel(rec).trim().replace(" ",""), row);
				templateList.add(toAdd);
				//System.out.println("[DASOGenerator] created template: \n" + toAdd);
			}
    	
    	return row;
    }
}
