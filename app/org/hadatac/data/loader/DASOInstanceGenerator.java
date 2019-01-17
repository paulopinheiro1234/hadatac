package org.hadatac.data.loader;

import org.hadatac.entity.pojo.DASOInstance;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.DASVirtualObject;
import org.hadatac.metadata.loader.URIUtils;
import java.lang.String;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import org.apache.commons.csv.CSVRecord;

public class DASOInstanceGenerator{

        private DataAcquisitionSchema das;
        private List<StudyObject> objList;
        private String mainLabel;
        private String mainUri;
        private DataAcquisitionSchemaObject mainDaso;
        private Map<String, DataAcquisitionSchemaObject> dasos = new ConcurrentHashMap<String, DataAcquisitionSchemaObject>();  
        private Map<String, ObjectCollection> socs = new ConcurrentHashMap<String, ObjectCollection>();  
        private List<ObjectCollection> socsList = new ArrayList<ObjectCollection>();  
        private Map<String, List<String>> socPaths = new HashMap<String, List<String>>(); 

        //private String studyId;
        //public Map<String,String> codeMappings = null;
        //public Map<String, Map<String,String>> codebook = null;

	/*public DASOInstanceGenerator(String studyId, 
			List<DASVirtualObject> objs,
			Map<String, String> codeMappings, 
			Map<String, Map<String,String>> codebook) {
		this.studyId = studyId;
		this.templateList = objs;
		this.codeMappings = codeMappings;
		this.codebook = codebook;
	}*/

    public DASOInstanceGenerator(String studyUri, DataAcquisitionSchema das) {
	    this.das = das;
	    this.socPaths.clear();

	    /* 
	     *  IDENTIFY MAIN DASO FROM DASAs (the DASO that has an identifier)
             */

	    mainLabel = "";
	    String origId = das.getOriginalIdLabel(); 
	    String id = das.getIdLabel(); 
	    if (origId != null && !origId.equals("")) {
		mainLabel = origId;
	    } else if (id != null && !id.equals("")) {
		mainLabel = id;
	    }
	    
	    if (mainLabel.equals("")) {
		System.out.println("DASOInstanceGenerator: NO IDENTIFIER");
		return;
	    } else {
		System.out.println("DASOInstanceGenerator: Label of main DASO: " + mainLabel);
	    }
		
	    /* 
	     *  IDENTIFY URI of MAIN DASO and SUPORTING DADOS FROM DASAs
             */

	    mainUri = "";
	    Iterator<DataAcquisitionSchemaAttribute> iterAttributes = das.getAttributes().iterator();
	    while (iterAttributes.hasNext()) {
		DataAcquisitionSchemaAttribute dasa = iterAttributes.next();
		String dasoUri = dasa.getObjectUri(); 
		DataAcquisitionSchemaObject tmpDaso = DataAcquisitionSchemaObject.find(dasoUri);
		if (dasa.getLabel().equals(mainLabel)) {
		    mainUri = dasoUri;
		    mainDaso = tmpDaso;
		    if (mainDaso == null) {
			System.out.println("DASOInstanceGenerator: FAILED TO LOAD MAIN DASO");
			return;
		    }
		} 
		if (dasoUri != null && !dasoUri.equals("") && !dasos.containsKey(dasoUri)) {
		    if (tmpDaso != null) {
			dasos.put(dasoUri, tmpDaso);
		    }
		}
		
	    }

	    /* 
	     *  AVAILABLE SOCs
             */

	    System.out.println("DASOInstanceGenerator: ======== AVAILABLE SOCs ========");
	    socsList = ObjectCollection.findByStudyUri(studyUri);
	    for (ObjectCollection soc : socsList) {
		System.out.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Reference : " + soc.getSOCReference());
	    }
	    
	    /* 
	     *  IDENTIFY URIs of TARGET DASOs
             */

	    System.out.println("DASOInstanceGenerator: Main DASO: " + mainUri);
	    System.out.println("DASOInstanceGenerator: ======== DASO LIST ========");
	    for (Map.Entry<String, DataAcquisitionSchemaObject> entry : dasos.entrySet()) {
		String key = entry.getKey();
		DataAcquisitionSchemaObject daso = entry.getValue();
		String toUri = targetUri(daso);
		System.out.println("DASOInstanceGenerator: DASO: " + daso.getUri() + "   From : " + daso.getLabel() + "  To: " + toUri);
		
		/*
                 *  LOAD each TARGET DASO into DASOs, if TARGET DASO is not loaded yet
                 */
		
		while (!dasos.containsKey(toUri)) {
		    System.out.println("DASOInstanceGenerator: Loading " + toUri);
		    DataAcquisitionSchemaObject newDaso = DataAcquisitionSchemaObject.find(toUri);
		    if (newDaso == null) {
			System.out.println("DASOInstanceGenerator: [ERROR] Could not find DASO with following URI : " + toUri);
			break;
		    }
		    dasos.put(toUri, newDaso);
		    toUri = targetUri(newDaso);

		    /*
		     *  Associate a SOC to each DASO (DASO's labels are SOC's hasSOCReference
		     */
		    
		    System.out.println("DASOInstanceGenerator: DASO: " + newDaso.getUri() + "   From : " + newDaso.getLabel() + "  To: " + toUri);
		    
		}

	    }
	    
	    /* 
	     *  COMPUTE PATH for each TARGET DASO
             */

	    System.out.println("DASOInstanceGenerator: ======== BUILD DASO PATHS ========");
	    for (Map.Entry<String, DataAcquisitionSchemaObject> entry : dasos.entrySet()) {
		String key = entry.getKey();
		DataAcquisitionSchemaObject daso = entry.getValue();
		String toUri = targetUri(daso);
		System.out.println("DASOInstanceGenerator: START: " + daso.getUri());
		System.out.print("DASOInstanceGenerator: PATH: ");
		
		DataAcquisitionSchemaObject nextTarget = daso;
		List<String> socs = new ArrayList<String>();
		System.out.print(nextTarget.getUri() + "  ");
		socs.add(nextTarget.getUri());
		while (!nextTarget.getUri().equals(mainUri)) {
		    String nextTargetUri = targetUri(nextTarget);
		    nextTarget = dasos.get(nextTargetUri);
		    if (nextTarget == null) {
			System.out.println("DASOInstanceGenerator: [ERROR] Could not complete path for " + toUri);
			break;
		    }
		    System.out.print(nextTarget.getUri() + "  ");
		    socs.add(nextTarget.getUri());
		}
		System.out.println();
		socPaths.put(daso.getUri(),socs);

	    }
	}
    
        private String targetUri(DataAcquisitionSchemaObject daso) {
	   if (!daso.getWasDerivedFrom().equals("")) {
	       String toLabel = daso.getWasDerivedFrom();
	       DataAcquisitionSchemaObject tmpDaso = DataAcquisitionSchemaObject.findByLabelInSchema(das.getUri(), toLabel);
	       if (tmpDaso == null) {
		   return "";
	       } else {
		   
		   return tmpDaso.getUri();
	       }
	   } else if (!daso.getInRelationTo().equals("")) {
	       return daso.getInRelationTo();
	   }
	   return "";
	}

        public Map<String,String> generateRowInstances(String id){
            /* Returns : First String : DASO's Label
             *           Object URI   : The actual URI of the object that was retrieved/created for the identifier in CSV Record
             */

	    System.out.println("DASOInstanceGenerator: generate row instances for : " + id);

	    Map<String,String> objList = new HashMap<String,String>();

	    /*
	     *   RETRIEVE MAIN OBJECT
	     */

	    /*
	     *   TRAVERSE list of objects for current record
	     */

	    for (Map.Entry<String, List<String>> entry : socPaths.entrySet()) {
		String key = entry.getKey();
		List<String> path = entry.getValue();

		/*
		 *   TRAVERSE SOC's PATH
		 */

		for (String objUri : path) {
		    
		    /*
		     *   RETRIEVE OR CREATE next object in the path
		     */

		}
	    
	    }

	    return objList;

	}// /generateRowInstances


        /*
        //
      	private String resolveVirtualEntity(DASVirtualObject workingObj, String workingField, CSVRecord rec) {
		//System.out.println("[DASOInstanceGen]: resolving " + workingField  + " for " + workingObj.getTemplateUri());
		if(workingField.contains("DASO") || workingField.startsWith("??")){
			if(codebook.containsKey(workingField)){
				// Check to see if there's a codebook entry
				Map<String,String> cbForCol = codebook.get(workingField);
				try{
					DataAcquisitionSchemaObject toResolve = DataAcquisitionSchemaObject.find(URIUtils.convertToWholeURI(workingField));
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
        */


	// for each
	/*  Study ID: default-study
			templateURI: hbgd-kb:DASO-subj_cat_infosheet-summaryClass
			rdfs:subClassOf hbgd-kb:DASO-subj_cat_infosheet-id-key
			rdfs:label summaryClass
			rdfs:type owl:Class
	*/

	/*@Overwrite
	public void createRows(){
	}// /createRows

	@Override
	public Map<String,Object> createRow(CSVRecord rec, int rowNumber) throws Exception {
		
	}// /createRow
	*/

	// private String studyId;
	// private String templateUri;
	// private Map<String,String> objRelations;

	        //System.out.println("[DASOInstanceGenerator] Inside generateRowInstances!");
		//HashMap<String, DASOInstance> instances = new HashMap<String,DASOInstance>();

		/*
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
				instances.put(URIUtils.convertToWholeURI(current.getTemplateUri()), tempDASOI);
				//System.out.println("[DASOInstanceGenerator] Made an instance: " + tempDASOI);
			} else {
				System.out.println("[DASOInstanceGen] WARN: row instance missing uri or type info!");
			}
		}// /iterate over templates
                */
	    
}// /class
