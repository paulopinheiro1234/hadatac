package org.hadatac.data.loader;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.annotator.AutoAnnotator;
import org.hadatac.entity.pojo.DASOInstance;
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
	final HashMap<String, List<String>> codebook = AutoAnnotator.codebook;
    
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
	
	public void resetStudyID(String study_id){
		//if(this.studyId.equals("default-study") || this.studyId.equals("")){
			this.studyId = study_id;
			System.out.println("[DASVirtualObject] studyId RESET to " + studyId);
		//} else {
		//	System.out.println("[DASVirtualObject] studyId already set to " + studyId);
		//}
	}


	/*private boolean resolveVirtualEntities(CSVRecord rec) {
		for (Map.Entry<String, String> entry : objRelations.entrySet()) {
			if(entry.getValue().contains("DASO")){  
				// Check to see if there's a code mapping entry
				if(codeMap.containsKey(entry.getValue())){
					System.out.println("[DASVirtualObject]: entry.getValue() = " + entry.getValue());
				}
				// If not, get the relevant URI of the other entity
				//else {
				//}
			}
		}
	}// resolveVirtualEntities()
	*/

	public String toString(){
		String result = "";
		result += "Study ID: " + studyId + "\n";
		result += "templateURI: " + templateUri + "\n";
		for (Map.Entry<String, String> entry : objRelations.entrySet()) {
			result += entry.getKey() + " " + entry.getValue() + "\n";
		}	
		return result;
	}// /toString()

	/*
	// DASOInstance(String label, String type, HashMap<String,String> relations, HashMap<String,String> templateVals)
	public DASOInstance generateInstance(CSVRecord rec){
			
	}// /generateInstance()
	*/







}// /class
