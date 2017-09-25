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
import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.csv.CSVRecord;

public class DASVirtualObject {
	final String kbPrefix = Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
	private Map<String,Object> origRow;

	private String templateUri;
	private String originalLabel;
	private HashMap<String,String> objRelations;

	final HashMap<String,String> codeMap = AutoAnnotator.codeMappings;
	final HashMap<String, Map<String,String>> codebook = AutoAnnotator.codebook;
    
	// takes the row created in DASchemaObjectGenerator
	// iff that row is virtual
	public DASVirtualObject(String ogLabel, Map<String,Object> dasoRow) {
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
				this.objRelations.put("sio:isRelatedTo", dasoRow.get("sio:inRelationTo").toString());
			} else {
				this.objRelations.put(dasoRow.get("sio:Relation").toString(), dasoRow.get("sio:inRelationTo").toString());
			}
		}
		if(dasoRow.get("dcterms:alternativeName") == null || dasoRow.get("dcterms:alternativeName").equals("")){
			//handle an error
		} else {
			this.originalLabel = dasoRow.get("dcterms:alternativeName").toString();
		}

	}// DASOVirtualObject()

	public HashMap<String,String> getObjRelations(){
		return this.objRelations;
	}

	public String getTemplateUri(){
		return this.templateUri;
	}

	public String getOriginalLabel(){
		return this.originalLabel;
	}

	// for each
	/*  Study ID: default-study
			templateURI: hbgd-kb:DASO-subj_cat_infosheet-summaryClass
			rdfs:subClassOf hbgd-kb:DASO-subj_cat_infosheet-id-key
			rdfs:label summaryClass
			rdfs:type owl:Class
	*/

	
	public String toString(){
		String result = "";
		result += "templateURI: " + this.templateUri + "\n";
		result += "column name: " + this.originalLabel + "\n";
		for (Map.Entry<String, String> entry : this.objRelations.entrySet()) {
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
