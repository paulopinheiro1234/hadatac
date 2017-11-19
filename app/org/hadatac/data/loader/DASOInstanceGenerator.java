package org.hadatac.data.loader;

import org.hadatac.entity.pojo.DASOInstance;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DASVirtualObject;
import org.hadatac.metadata.loader.ValueCellProcessing;
import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVRecord;


public class DASOInstanceGenerator{
	private String studyId;
	private List<DASVirtualObject> templateList;

	public Map<String,String> codeMappings = null;
	public Map<String, Map<String,String>> codebook = null;

	public DASOInstanceGenerator(String studyId, 
			List<DASVirtualObject> objs,
			Map<String, String> codeMappings, 
			Map<String, Map<String,String>> codebook) {
		this.studyId = studyId;
		this.templateList = objs;
		this.codeMappings = codeMappings;
		this.codebook = codebook;
	}

	//
	private String resolveVirtualEntity(DASVirtualObject workingObj, String workingField, CSVRecord rec) {
		//System.out.println("[DASOInstanceGen]: resolving " + workingField  + " for " + workingObj.getTemplateUri());
		if(workingField.contains("DASO") || workingField.startsWith("??")){
			if(codebook.containsKey(workingField)){
				// Check to see if there's a codebook entry
				Map<String,String> cbForCol = codebook.get(workingField);
				try{
					DataAcquisitionSchemaObject toResolve = DataAcquisitionSchemaObject.find(ValueCellProcessing.convertToWholeURI(workingField));
					//System.out.println("[DASOInstanceGen] expanded " + toResolve.getLabel());
					String colName = toResolve.getLabel();
					if(rec.isMapped(colName)) {
						String item = rec.get(colName);
						//System.out.println("[DASOInstanceGen] colName: " + colName + " item: " + item);
						if (cbForCol.containsKey(item))
							return cbForCol.get(item);
						else return item;
					}	else if(rec.isMapped(colName.replace("-","_"))) {
						String item = rec.get(colName.replace("-","_"));
						//System.out.println("[DASOInstanceGen] colName: " + colName + " item: " + item);
						if (cbForCol.containsKey(item))
							return cbForCol.get(item);
						else return item;
					}
				} catch (Exception e){
					System.out.println("[DASOInstanceGen] ERROR resolving entity: ");
					e.printStackTrace(System.out);
				}
			} else {
				// If not, fetch the appropriate row entity's URI
				System.out.println("[DASOInstanceGen]: " + workingObj.getTemplateUri() + " not found in codebook");
				// find the right template
				// get that uri
			}
		}
		return "";
	}// resolveVirtualEntities()


	// for each
	/*  Study ID: default-study
			templateURI: hbgd-kb:DASO-subj_cat_infosheet-summaryClass
			rdfs:subClassOf hbgd-kb:DASO-subj_cat_infosheet-id-key
			rdfs:label summaryClass
			rdfs:type owl:Class
	*/

	/*@Overwrite
	public List<Map<String,Object>> createRows(){
	}// /createRows

	@Override
	public Map<String,Object> createRow(CSVRecord rec, int row_number) throws Exception {
		
	}// /createRow
	*/

	// private String studyId;
	// private String templateUri;
	// private Map<String,String> objRelations;
	public HashMap<String,DASOInstance> generateRowInstances(CSVRecord rec){
		//System.out.println("[DASOInstanceGenerator] Inside generateRowInstances!");
		HashMap<String, DASOInstance> instances = new HashMap<String,DASOInstance>();
		String tempLabel = "";
		String tempType = "";
		String tempKey = "";
		HashMap<String,String> tempRelations = new HashMap<String,String>();
		for(DASVirtualObject current : templateList){ // for all templates
			for(Map.Entry<String, String> entry : current.getObjRelations().entrySet()){ // for all relations in each template
				// resolve from the CSV:
				if(entry.getKey().equals("rdfs:label")){
					tempLabel = entry.getValue(); 
					//System.out.println("[DASOInstanceGenerator] tempLabel set to " + tempLabel);
				}	else if (entry.getKey().equals("rdfs:type")){
					tempType = entry.getValue();
					//System.out.println("[DASOInstanceGenerator] tempType set to " + tempType);
				}	else if(entry.getKey().equals("sio:identifier")){
					tempKey = entry.getValue();
					//System.out.println("[DASOInstanceGenerator] tempKey set to " + tempKey);
				} else {
					if(rec.isMapped(current.getOriginalLabel())){
						//System.out.println("[DASOInstanceGenerator] added relation from CSV file " + entry.getKey() + " " + rec.get(current.getOriginalLabel()));
						tempRelations.put(entry.getKey(), rec.get(current.getOriginalLabel()));
					} else {
						String resolved = resolveVirtualEntity(current, entry.getValue(), rec);
						tempRelations.put(entry.getKey(),resolved);
					}
				}
				if(tempKey.equals("") || tempKey == null) tempKey = tempLabel;
			}// /iterate over relations
			if((tempLabel!=null || !tempLabel.equals("")) && (tempType!=null || !tempType.equals(""))){
				DASOInstance tempDASOI = new DASOInstance(this.studyId, tempKey, tempLabel, tempType, tempRelations);
				// current.getTemplateUri() *should* match what is in DASchemaAttribute table's "attributeOf" field!
				instances.put(ValueCellProcessing.convertToWholeURI(current.getTemplateUri()), tempDASOI);
				//System.out.println("[DASOInstanceGenerator] Made an instance: " + tempDASOI);
			} else {
				System.out.println("[DASOInstanceGen] WARN: row instance missing uri or type info!");
			}
		}// /iterate over templates
		return instances;
	}// /generateRowInstances
	


}// /class
