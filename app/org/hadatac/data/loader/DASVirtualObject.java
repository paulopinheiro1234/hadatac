package org.hadatac.data.loader;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.annotator.AutoAnnotator;
import play.Play;
import java.lang.String;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVRecord;

public class DASVirtualObject {
	final String kbPrefix = Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
	private Map<String,Object> origRow;

	private String studyId;
	private String templateUri;
	private Map<String,String> objRelations;

	final HashMap<String,String> codeMap = AutoAnnotator.codeMappings;
    
	// takes the row created in DASchemaObjectGenerator
	// iff that row is virtual
	public DASVirtualObject(String study_id, Map<String,Object> dasoRow) {
		this.studyId = study_id;
		this.origRow = dasoRow;
		this.objRelations = new HashMap<String,String>();

		if(dasoRow.get("hasURI") == null || dasoRow.get("hasURI").equals("")){
			//handle an error
		} else {
			this.templateUri = dasoRow.get("hasURI").toString();
		}
		if(dasoRow.get("rdfs:label") == null || dasoRow.get("rdfs:label").equals("")){
			//handle an error
		} else {
			this.objRelations.put("rdfs:label", dasoRow.get("rdfs:label").toString());
		}
		if(dasoRow.get("hasco:hasEntity") == null || dasoRow.get("hasco:hasEntity").equals("")){
			//handle an error
		} else {
			this.objRelations.put("rdfs:type", dasoRow.get("hasco:hasEntity").toString());
		}
		if(dasoRow.get("hasco:hasRole") == null || dasoRow.get("hasco:hasRole").equals("")){
			//handle an error
		} else {
			this.objRelations.put("hasco:hasRole", dasoRow.get("hasco:hasRole").toString());
		}
		if(dasoRow.get("sio:inRelationTo") == null || dasoRow.get("sio:inRelationTo").equals("")){
			//handle an error
		} else {
			if(dasoRow.get("sio:Relation") == null || dasoRow.get("sio:Relation").equals("")){
				this.objRelations.put("sio:hasAttribute", dasoRow.get("sio:inRelationTo").toString());
			} else {
				this.objRelations.put(dasoRow.get("sio:Relation").toString(), dasoRow.get("sio:inRelationTo").toString());
			}
		}
	}// DASOVirtualObject()

	private String getEntity(CSVRecord rec) {
		if ((rec.get(objRelations.get("Entity"))) == null || (rec.get(objRelations.get("Entity"))).equals("")) {
			return null;
		} else {
			if (codeMap.containsKey(rec.get(objRelations.get("Entity")))) {
				System.out.println("[DASVirtualObject] code matched: " + rec.get(objRelations.get("Entity"))); 
				return codeMap.get(rec.get(objRelations.get("Entity")));
			} else {
				return rec.get(objRelations.get("Entity"));
			}
		}
	}// getEntity()

	public String toString(){
		String result = "";
		result += "Study ID: " + studyId + "\n";
		result += "templateURI: " + templateUri + "\n";
		for (Map.Entry<String, String> entry : objRelations.entrySet()) {
			result += entry.getKey() + "/" + entry.getValue() + "\n";
		}	
		return result;
	}// /toString()

	//public DASOInstance generateInstance(CSVRecord rec){
	//	
	//}// /generateInstance()








}// /class
