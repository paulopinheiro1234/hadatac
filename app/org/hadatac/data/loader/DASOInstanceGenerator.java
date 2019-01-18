package org.hadatac.data.loader;

import org.hadatac.entity.pojo.DASOInstance;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.DASVirtualObject;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import java.lang.String;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import org.apache.commons.csv.CSVRecord;

public class DASOInstanceGenerator{

    private final String SIO_OBJECT = "sio:Object";
    private final String SIO_SAMPLE = "sio:Sample";;
    private final String kbPrefix = ConfigProp.getKbPrefix();
    private String studyUri;
    private DataAcquisitionSchema das;
    private List<StudyObject> objList;
    private String mainLabel;
    private DataAcquisitionSchemaObject mainDaso;
    private String mainDasoUri;
    private ObjectCollection mainSoc;
    private String mainSocUri;
    private Map<String, DataAcquisitionSchemaObject> dasos = new ConcurrentHashMap<String, DataAcquisitionSchemaObject>();  
    private Map<String, ObjectCollection> requiredSocs = new ConcurrentHashMap<String, ObjectCollection>();  
    private List<ObjectCollection> socsList = null;  
    private Map<String, List<ObjectCollection>> socPaths = new HashMap<String, List<ObjectCollection>>(); 

    public DASOInstanceGenerator(String studyUri, DataAcquisitionSchema das) {
	    this.studyUri = studyUri;
	    this.das = das;
	    this.socPaths.clear();
	    this.requiredSocs.clear();

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
		System.out.println("DASOInstanceGenerator: Study URI: " + studyUri);
		System.out.println("DASOInstanceGenerator: Label of main DASO: " + mainLabel);
	    }
		
	    /* 
	     *  INITIALLY AVAILABLE SOCs
             */

	    System.out.println("DASOInstanceGenerator: ======== INITIALLY AVAILABLE SOCs ========");
	    socsList = ObjectCollection.findByStudyUri(studyUri);
	    if (socsList == null) {
		System.out.println("DASOInstanceGenerator: no SOC is available");
		socsList = new ArrayList<ObjectCollection>();  
	    } else {
		for (ObjectCollection soc : socsList) {
		    System.out.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Reference : " + soc.getSOCReference());
		}
	    }
	    
	    /* 
	     *  IDENTIFY URI of MAIN DASO and SUPORTING DADOS FROM DASAs
             */

	    mainDasoUri = "";
	    Iterator<DataAcquisitionSchemaAttribute> iterAttributes = das.getAttributes().iterator();
	    while (iterAttributes.hasNext()) {
		DataAcquisitionSchemaAttribute dasa = iterAttributes.next();
		String dasoUri = dasa.getObjectUri(); 
		DataAcquisitionSchemaObject tmpDaso = DataAcquisitionSchemaObject.find(dasoUri);
		if (dasa.getLabel().equals(mainLabel)) {
		    mainDasoUri = dasoUri;
		    mainDaso = tmpDaso;
		    if (mainDaso == null) {
			System.out.println("DASOInstanceGenerator: [ERROR] FAILED TO LOAD MAIN DASO");
			return;
		    }
		} 
		if (dasoUri != null && !dasoUri.equals("") && !dasos.containsKey(dasoUri)) {
		    if (tmpDaso != null) {
			dasos.put(dasoUri, tmpDaso);
		    }
		}
	    }
	    mainSoc = socFromDaso(mainDaso, socsList);
	    if (mainSoc == null) {
		System.out.println("DASOInstanceGenerator: [ERROR] FAILED TO LOAD MAIN SOC");
		return;
	    }
	    mainSocUri = mainSoc.getUri();
	    System.out.println("DASOInstanceGenerator: ======== MAIN DASO and ASSOCIATED SOC ========");
	    System.out.println("DASOInstanceGenerator: Main DASO: " + mainDasoUri);
	    System.out.println("DASOInstanceGenerator: Main SOC: " + mainSocUri);

	    /* 
	     *  IDENTIFY URIs of TARGET DASOs and ASSOCIATED SOCs
             */

	    System.out.println("DASOInstanceGenerator: ======== DASO LIST WITH ASSOCIATED SOCs ========");
	    for (Map.Entry<String, DataAcquisitionSchemaObject> entry : dasos.entrySet()) {
		String key = entry.getKey();
		DataAcquisitionSchemaObject daso = entry.getValue();
		String toUri = targetUri(daso);
		System.out.println("DASOInstanceGenerator: DASO: " + daso.getUri() + "   From : " + daso.getLabel() + "  To: " + toUri);
		associateSOC(daso);

		/*
                 *  LOAD each TARGET DASO into DASOs, if TARGET DASO is not loaded yet
                 */
		
		while (!dasos.containsKey(toUri)) {
		    System.out.println("DASOInstanceGenerator: Loading " + toUri);
		    DataAcquisitionSchemaObject newDaso = DataAcquisitionSchemaObject.find(toUri);
		    if (newDaso == null) {
			System.out.println("DASOInstanceGenerator: [ERROR] Could not find DASO with following URI : " + toUri);
			return;
		    }
		    dasos.put(toUri, newDaso);
		    toUri = targetUri(newDaso);
		    System.out.println("DASOInstanceGenerator: DASO: " + newDaso.getUri() + "   From : " + newDaso.getLabel() + "  To: " + toUri);
		    daso = newDaso;
		    associateSOC(daso);
		}

	    }

	    /* 
	     *  RETRIEVING ADDITIONAL SOCs required for traversing existing SOCs
             */

	    System.out.println("DASOInstanceGenerator: ======== RETRIEVING ADDITINAL  SOCs ========");
	    for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
		String key = entry.getKey();
		ObjectCollection soc = entry.getValue();
		//System.out.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Reference : " + soc.getSOCReference() + 
		//		   "    with hasScope: " + soc.getHasScopeUri());
		ObjectCollection currentSoc = soc;
		while (currentSoc.getHasScopeUri() != null && !currentSoc.getHasScopeUri().equals("") && !requiredSocs.containsKey(currentSoc.getHasScopeUri())) {

		    // lookup in socsList for next SOC, i.e., currentSoc.getHasScopeUri()
		    ObjectCollection nextSoc = null;
		    for (ObjectCollection tmpSoc : socsList) {
			if (tmpSoc.getUri().equals(currentSoc.getHasScopeUri())) {
			    nextSoc = tmpSoc;
			    break;
			}
		    }
		    if (nextSoc == null) {
			System.out.println("DASOInstanceGenerator: [ERROR] Could not find SOC with following URI : " + currentSoc.getHasScopeUri());
			return;
		    } else {
			if (!requiredSocs.containsKey(nextSoc.getUri())) {
			    requiredSocs.put(nextSoc.getUri(), nextSoc);
			    System.out.println("DASOInstanceGenerator: Loading SOC: " + nextSoc.getUri() + " to required SOCs");
			}
			currentSoc = nextSoc;
		    }
		}
		   
	    }
	    
	    /* 
	     *  LIST OF REQUIRED SOCs
             */

	    System.out.println("DASOInstanceGenerator: ======== REQUIRED SOCs ========");
	    for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
		String key = entry.getKey();
		ObjectCollection soc = entry.getValue();
		System.out.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Reference : " + soc.getSOCReference() + 
				   "    with hasScope: " + soc.getHasScopeUri());
	    }
	    
	    /* 
	     *  COMPUTE PATH for each TARGET SOC
             */

	    System.out.println("DASOInstanceGenerator: ======== BUILD SOC PATHS ========");
	    for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
		String key = entry.getKey();
	        ObjectCollection soc = entry.getValue();
		System.out.println("DASOInstanceGenerator: START: " + soc.getUri());
		System.out.print("DASOInstanceGenerator: PATH: ");
		if (soc.getHasScope() == null) {
		    System.out.println();
		    break;
		}
		String toUri = soc.getHasScope().getUri();
		
		ObjectCollection nextTarget = soc;
		List<ObjectCollection> socs = new ArrayList<ObjectCollection>();
		System.out.print(nextTarget.getUri() + "  ");
		socs.add(nextTarget);
		while (!nextTarget.getUri().equals(mainSocUri)) {
		    String nextTargetUri = nextTarget.getHasScopeUri();
		    nextTarget =  requiredSocs.get(nextTargetUri);
		    if (nextTarget == null) {
			System.out.println("DASOInstanceGenerator: [ERROR] Could not complete path for " + toUri);
			break;
		    }
		    System.out.print(nextTarget.getUri() + "  ");
		    socs.add(nextTarget);
		}
		System.out.println();
		socPaths.put(key,socs);

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

        private boolean isSample(DataAcquisitionSchemaObject daso) {
	   if (!daso.getWasDerivedFrom().equals("")) {
	       String toLabel = daso.getWasDerivedFrom();
	       DataAcquisitionSchemaObject tmpDaso = DataAcquisitionSchemaObject.findByLabelInSchema(das.getUri(), toLabel);
	       if (tmpDaso == null) {
		   return false;
	       } else {
		   return true;
	       }
	   }
	   return false;
	}

        private ObjectCollection socFromTargetDaso(DataAcquisitionSchemaObject daso, List<ObjectCollection> list) {
	    String targetObjUri = targetUri(daso);
	    if (targetObjUri.equals("")) {
		return null;
	    }
	    DataAcquisitionSchemaObject targetObj = DataAcquisitionSchemaObject.find(targetObjUri);
	    if (targetObj == null || targetObj.getLabel() == null || targetObj.getLabel().equals("")) {
		return null;
	    }
	    for (ObjectCollection soc : list) {
		//System.out.println("socFromTargetDaso : " + targetObj.getLabel() + "    soc's getSOCReference " + soc.getSOCReference()); 
		if (soc.getSOCReference().equals(targetObj.getLabel())) {
		    return soc;
		}
	    }
	    return null;
	}

        private ObjectCollection socFromDaso(DataAcquisitionSchemaObject daso, List<ObjectCollection> list) {
	    if (daso == null || daso.getLabel() == null || daso.getLabel().equals("")) {
		return null;
	    }
	    for (ObjectCollection soc : list) {
		if (soc.getSOCReference().equals(daso.getLabel())) {
		    return soc;
		}
	    }
	    return null;
	}

        private void associateSOC(DataAcquisitionSchemaObject daso) {
	    ObjectCollection associatedSOC = null;
	    for (ObjectCollection soc : socsList) {
		if (soc.getSOCReference().equals(daso.getLabel())) {
		    associatedSOC = ObjectCollection.find(soc.getUri());
		    if (associatedSOC != null) {
			System.out.println("DASOInstanceGenerator:       associated SOC : " + associatedSOC + "    with hasScope: " + associatedSOC.getHasScopeUri());
			if (!requiredSocs.containsKey(associatedSOC.getUri())) {
			    requiredSocs.put(associatedSOC.getUri(), associatedSOC);
			}
			break;
		    }
		}
	    }
	    
	    /*
	     *  Create an SOC when not existing SOC can be found
	     */
	    
	    if (associatedSOC == null) { 
		String newSOCUri = studyUri.replace("STD","SOC") + "-" + daso.getLabel().replace("??","");
		String scopeUri = "";
		if (daso != null) {
		    ObjectCollection scopeObj = socFromTargetDaso(daso, socsList);
		    if (scopeObj != null && scopeObj.getUri() != null) {
			scopeUri = scopeObj.getUri();
		    } 
		}
		System.out.println("DASOInstanceGenerator:       created SOC : " + newSOCUri + "    with hasScope: " + scopeUri);
		String newLabel = daso.getLabel().replace("??","");
		String collectionType = null;
		if (isSample(daso)) {
		    collectionType = ObjectCollection.SAMPLE_COLLECTION;
		} else {
		    collectionType = ObjectCollection.SUBJECT_COLLECTION;
		}
		ObjectCollection newSoc = new ObjectCollection(newSOCUri, collectionType, newLabel, newLabel, studyUri, scopeUri, newLabel, daso.getLabel(), null, null);
		newSoc.saveToTripleStore();
		if (!requiredSocs.containsKey(newSoc.getUri())) {
		    requiredSocs.put(newSoc.getUri(), newSoc);
		}
		//ObjectCollection(String uri, String typeUri, String label, String comment, String studyUri, String hasScopeUri, 
		//	    	   String hasGroundingLabel, String hasSOCReference, List<String> spaceScopeUris, List<String> timeScopeUris) 
	    }
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

	    for (Map.Entry<String, List<ObjectCollection>> entry : socPaths.entrySet()) {
		String key = entry.getKey();
		List<ObjectCollection> path = entry.getValue();

		/*
		 *   TRAVERSE SOC's PATH
		 */

		ListIterator<ObjectCollection> iter = path.listIterator(path.size());

		System.out.println("DASOInstanceGenerator:     PATH >>> ");

		// Lookup first study object
		ObjectCollection currentSoc = iter.previous();
		String currentObjUri = StudyObject.findUriBySocAndOriginalId(currentSoc.getUri(), id); 
	        System.out.println("DASOInstanceGenerator:          Obj Original ID=[" + id + "]   SOC=[" + currentSoc.getUri() + "] =>  Obj URI=[" + currentObjUri + "]");
	    
		while (iter.hasPrevious()) {
		    ObjectCollection nextSoc = iter.previous();
		    //System.out.println("            " + nextSoc.getUri() + "  ");

		    /*
		     *   RETRIEVE next object in the path
		     */

		    String nextObjUri = StudyObject.findUriBySocAndScopeUri(nextSoc.getUri(), currentObjUri); 
		    System.out.println("DASOInstanceGenerator:          Scope Obj URI=[" + currentObjUri + "]  SOC=[" + nextSoc.getUri() + 
				       "]  =>  Obj Uri=[" + nextObjUri + "]");
		    
		    if (nextObjUri == null || nextObjUri.equals("")) {

		         /*
			  *   CREATE next object in the path if it does not exist
			  */
			
			//public StudyObject(String uri, String typeUri, String originalId, String label, String isMemberOf, String comment,
			//		   List<String> scopeUris, List<String> timeScopeUris, List<String> spaceScopeUris) 

			String newOriginalId = String.valueOf(nextSoc.getNextCounter());
			String newUri = createObjectUri(newOriginalId, nextSoc.getUri(), nextSoc.getTypeUri());
			String newLabel = createObjectLabel(newOriginalId, nextSoc.getUri(), nextSoc.getTypeUri());
		        String newTypeUri = "";
			if (nextSoc.getTypeUri().equals(ObjectCollection.SUBJECT_COLLECTION)) {
			    newTypeUri = URIUtils.replacePrefixEx(SIO_OBJECT);
			} else {
			    newTypeUri = URIUtils.replacePrefixEx(SIO_SAMPLE);
			}
			List<String> newScopeUris = new ArrayList<String>();
			List<String> newTimeScopeUris = new ArrayList<String>();
			List<String> newSpaceScopeUris = new ArrayList<String>();
			newScopeUris.add(currentObjUri);
			System.out.println("DASOInstanceGenerator:          Creating Obj with URI=[" + newUri + "]   Type=[" + newTypeUri + "]"); 
			
			//public StudyObject(newUri, String typeUri, newOriginalId, newLabel, nextSoc.getUri(), "Automatically generated",
			//		   newScopeUris, newTimeScopeUris, newSpaceScopeUris) 

		    }

		    currentSoc = nextSoc;
		    currentObjUri = nextObjUri;

		}

	    }

	    return objList;

	}// /generateRowInstances

        private String createObjectUri(String originalID, String socUri, String socTypeUri) {
	    String labelPrefix = "";
	    if (socTypeUri.equals(ObjectCollection.SUBJECT_COLLECTION)) {
		labelPrefix = "SBJ-";
	    } else {
		labelPrefix = "SPL-";
	    }
	    String uri = kbPrefix + labelPrefix + originalID + "-" + socIdFromUri(socUri);
	    uri = URIUtils.replacePrefixEx(uri);
	    return uri;

	}

        private String createObjectLabel(String originalID, String socUri, String socTypeUri) {
	    String labelPrefix = "";
	    if (socTypeUri.equals(ObjectCollection.SUBJECT_COLLECTION)) {
		labelPrefix = "SBJ ";
	    } else {
		labelPrefix = "SPL ";
	    }
	    return labelPrefix + originalID + " - " + socIdFromUri(socUri);

	}

        private String socIdFromUri(String socUri) {
	    String SOC_PREFIX = "SOC-";
	    if (socUri == null || socUri.equals("")) {
		return "";
	    }
	    int index = socUri.indexOf(SOC_PREFIX) + SOC_PREFIX.length();
	    if (index == -1) {
		return "";
	    }
	    String resp = socUri.substring(index);
	    if (resp == null) {
		return "";
	    }
	    return resp;
	}




}// /class
