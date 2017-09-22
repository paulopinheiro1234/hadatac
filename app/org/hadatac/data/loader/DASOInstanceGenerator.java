package org.hadatac.data.loader;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.annotator.AutoAnnotator;
import org.hadatac.entity.pojo.DASOInstance;
import org.hadatac.data.loader.DASVirtualObject;
import play.Play;
import java.lang.String;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.csv.CSVRecord;


public class DASOInstanceGenerator{
	private ArrayList<DASVirtualObject> templateList;

	final HashMap<String,String> codeMap = AutoAnnotator.codeMappings;
	final HashMap<String, Map<String,String>> codebook = AutoAnnotator.codebook;

	public DASOInstanceGenerator(String studyId, ArrayList<DASVirtualObject> objs){
		for(DASVirtualObject temp : objs){
			this.templateList.add(DASVirtualObject.resetStudyId(temp,studyId));
		}
	}

	// TODO: finish this
	/*private void resolveVirtualEntities(String rowValue) {
		for (Map.Entry<String, String> entry : objRelations.entrySet()) {
			if(entry.getValue().contains("DASO")){
				// Check to see if there's a codebook entry
				if(codebook.containsKey(entry.getValue())){
					System.out.println("[DASVirtualObject]: resolving " + entry.getValue());
					HashMap<String,String> currentCodeColumn = (HashMap)codebook.get(entry.getValue());
				} else {
					// If not, fetch the appropriate row entity's URI
					System.out.println("[DASVirtualObject]: " + entry.getValue() + " not found in codebook");
					// blorp
				}
			}
		}
	}// resolveVirtualEntities()
	*/


	// for each
	/*  Study ID: default-study
			templateURI: hbgd-kb:DASO-subj_cat_infosheet-summaryClass
			rdfs:subClassOf hbgd-kb:DASO-subj_cat_infosheet-id-key
			rdfs:label summaryClass
			rdfs:type owl:Class
	*/

	// private String studyId;
	// private String templateUri;
	// private Map<String,String> objRelations;
	public HashMap<String,DASOInstance> generateRowInstances(CSVRecord rec){
		HashMap<String,DASOInstance> instances = new HashMap<String,DASOInstance>();
		String tempLabel = "";
		String tempType = "";
		String tempKey = "";
		HashMap<String,String> tempRelations = new HashMap<String,String>();
		for(DASVirtualObject current : templateList){
			for(Map.Entry<String, String> entry : current.getObjRelations().entrySet()){
				// resolve from the CSV:
				if(entry.getKey().equals("rdfs:label")){
					tempLabel = entry.getValue(); 
				}	else if (entry.getKey().equals("rdfs:type")){
					tempType = entry.getValue();
				}	else if(entry.getKey().equals("sio:identifier")){
					tempKey = entry.getValue();
				} else {
					tempRelations.put(entry.getKey(), entry.getValue());
				}
				if(tempKey.equals("") || tempKey == null) tempKey = tempLabel;
			}// /iterate over relations
			if((tempLabel!=null || !tempLabel.equals("")) && (tempType!=null || !tempType.equals(""))){
				DASOInstance tempDASOI = new DASOInstance(current.getStudyId(), tempKey, tempLabel, tempType, tempRelations);
				instances.put(current.getTemplateUri(), tempDASOI);
			} else {
				System.out.println("[DASOInstanceGenerator] WARN: row instance missing uri or type info!");
			}
		}// /iterate over templates
		return instances;
	}// /generateRowInstances
	
// public DASOInstance(String studyId, String rowKey, String label, String type, HashMap<String,String> relations)

}// /class
