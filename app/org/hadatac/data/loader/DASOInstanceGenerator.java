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
    private Map<String, String> socLabels = new ConcurrentHashMap<String, String>();  

    public DASOInstanceGenerator(String studyUri, DataAcquisitionSchema das) {
	this.studyUri = studyUri;
	this.das = das;
	this.socPaths.clear();
	this.socLabels.clear();
	
	/* **************************************************************************************
	 *                                                                                      *
	 *                MAPPING OF IDNTIFIER AND ASSOCIATED OBJECTS                           *
	 *                                                                                      *
	 ****************************************************************************************/
	
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
	 *  (1/8) INITIALLY AVAILABLE SOCs
	 */
	
	System.out.println("DASOInstanceGenerator: (1/8) ======== INITIALLY AVAILABLE SOCs ========");
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
	 *  (2/8) IDENTIFY MAIN DASO and DASOS REQUIRED FROM DASAs. THESE DASOS ARE LISTED IN STEP (3/7)
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
	System.out.println("DASOInstanceGenerator: (2/8) ========----- MAIN DASO ================");
	System.out.println("DASOInstanceGenerator: Main DASO: " + mainDasoUri);
	System.out.println("DASOInstanceGenerator: Main SOC: " + mainSocUri);
	
	/* 
	 *  (3/8) IDENTIFY URIs of TARGET DASOs
	 */
	
	System.out.println("DASOInstanceGenerator: (3/8) =============== TRAVERSE DASOS ================");
	
	for (Map.Entry<String, DataAcquisitionSchemaObject> entry : dasos.entrySet()) {
	    String key = entry.getKey();
	    DataAcquisitionSchemaObject daso = entry.getValue();
	    processTargetDaso(daso);
	}
	
	/* 
	 *  (4/8) IDENTIFY SOCs ASSOCIATED WITH IDENTIFIED DASOs
	 */
	
	System.out.println("DASOInstanceGenerator: (4/8) ===== IDENTIFY SOCs ASSOCIATED WITH IDENTIFIED DASOs ======");
	
	this.requiredSocs.clear();
	for (Map.Entry<String, DataAcquisitionSchemaObject> entry : dasos.entrySet()) {
	    String key = entry.getKey();
	    DataAcquisitionSchemaObject daso = entry.getValue();
	    if (!findCreateAssociatedSOC(daso)) {
		System.out.println("DASOInstanceGenerator: [ERROR] Cannot create SOC for the following daso: " + daso.getUri());
		return;
	    }
	}  
	
	/* 
	 *  (5/8) RETRIEVING ADDITIONAL SOCs required for traversing existing SOCs
	 */
	
	System.out.println("DASOInstanceGenerator: (5/8) ======== RETRIEVING ADDITINAL  SOCs ========");
	for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
	    String key = entry.getKey();
	    ObjectCollection soc = entry.getValue();
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
	 *  (6/8) LIST OF REQUIRED SOCs
	 */
	
	System.out.println("DASOInstanceGenerator: (6/8) ======== REQUIRED SOCs ========");
	for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
	    String key = entry.getKey();
	    ObjectCollection soc = entry.getValue();
	    System.out.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Reference : " + soc.getSOCReference() + 
			       "    with hasScope: " + soc.getHasScopeUri());
	}
	
	/* 
	 *  (7/8) COMPUTE PATH for each TARGET SOC
	 */

	System.out.println("DASOInstanceGenerator: (7/8) ======== BUILD SOC PATHS ========");
	for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
	    String key = entry.getKey();
	    ObjectCollection soc = entry.getValue();
	    List<ObjectCollection> socs = new ArrayList<ObjectCollection>();
	    System.out.println("DASOInstanceGenerator: START: " + soc.getUri());
	    System.out.print("DASOInstanceGenerator: PATH: ");
	    if (soc.getHasScope() == null) {
		System.out.println(soc.getUri());
		socs.add(soc);
		socPaths.put(key,socs);
		break;
	    }
	    String toUri = soc.getHasScope().getUri();
	    
	    ObjectCollection nextTarget = soc;
	    System.out.print(nextTarget.getUri() + "  ");
	    socs.add(nextTarget);
	    while (!nextTarget.getUri().equals(mainSocUri)) {
		String nextTargetUri = nextTarget.getHasScopeUri();
		nextTarget =  requiredSocs.get(nextTargetUri);
		if (nextTarget == null) {
		    System.out.println("DASOInstanceGenerator: [ERROR] Could not complete path for " + toUri);
		    return;
		}
		System.out.print(nextTarget.getUri() + "  ");
		socs.add(nextTarget);
	    }
	    System.out.println();
	    socPaths.put(key,socs);
	    
	}

	/* 
	 *  (8/8) COMPUTE LABEL for each TARGET SOC
	 */
	
	System.out.println("DASOInstanceGenerator: (8/8) ======== COMPUTE SOC LABELS ========");
	for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
	    String key = entry.getKey();
	    ObjectCollection soc = entry.getValue();
	    String fullLabel = "";
	    boolean process = true;

	    System.out.println("DASOInstanceGenerator: START: " + soc.getUri());
	    String label = soc.getGroundingLabel();
	    if (label == null) {
		label = "";
	    }
	    if (soc.getHasScope() == null || !label.equals("")) {
		fullLabel = label;
		System.out.println("DASOInstanceGenerator: Computed label [" + fullLabel + "]");
		socLabels.put(soc.getSOCReference(), fullLabel);
		if (soc.getRoleLabel() == null || soc.getRoleLabel().equals("")) {
		    soc.saveRoleLabel(fullLabel);
		}
		process = false;
	    } else {
		fullLabel = getPrettyLabel(soc.getLabel());
	    }

	    if (process) {
		String toUri = soc.getHasScope().getUri();
	    
		ObjectCollection nextTarget = soc;
		label = nextTarget.getGroundingLabel();
		if (label == null) {
		    label = "";
		}
		while (!nextTarget.getUri().equals(mainSocUri) && label.equals("")) {
		    String nextTargetUri = nextTarget.getHasScopeUri();
		    nextTarget =  requiredSocs.get(nextTargetUri);
		    if (nextTarget == null) {
			System.out.println("DASOInstanceGenerator: [ERROR] Could not complete path for " + toUri);
			return;
		    }
		    label = nextTarget.getGroundingLabel();
		    if (label == null) {
			label = "";
		    }
		    if (label.equals("")) {
			fullLabel = getPrettyLabel(nextTarget.getLabel()) + " " + fullLabel;
		    } else {
			fullLabel = label + " " + fullLabel;
		    }
		}
		System.out.println("DASOInstanceGenerator: Computed label [" + fullLabel + "]");	    
		if (soc.getRoleLabel() == null || soc.getRoleLabel().equals("")) {
		    soc.saveRoleLabel(fullLabel);
		}
		socLabels.put(soc.getSOCReference(), fullLabel);
	    }
	}
    }
    
    /* **************************************************************************************
     *                                                                                      *
     *                            SUPPORTING METHODS                                        *
     *                                                                                      *
     ****************************************************************************************/
    
    private boolean processTargetDaso(DataAcquisitionSchemaObject daso) { 
	String toUri = targetUri(daso);
	System.out.println("DASOInstanceGenerator: DASO: " + daso.getUri() + "   From : " + daso.getLabel() + "  To: " + toUri);
	
	//  LOAD each TARGET DASO into DASOs, if TARGET DASO is not loaded yet
	if (!dasos.containsKey(toUri)) {
	    System.out.println("DASOInstanceGenerator: Loading " + toUri);
	    DataAcquisitionSchemaObject newDaso = DataAcquisitionSchemaObject.find(toUri);
	    if (newDaso == null) {
		System.out.println("DASOInstanceGenerator: [ERROR] Could not find DASO with following URI : " + toUri);
		return false;
	    }
	    dasos.put(toUri, newDaso);
	    return processTargetDaso(newDaso);
	}
	return true;
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
    
    private DataAcquisitionSchemaObject dasoFromSoc(ObjectCollection soc, Map<String, DataAcquisitionSchemaObject> mapDasos) {
	if (soc == null || soc.getSOCReference() == null || soc.getSOCReference().equals("")) {
	    return null;
	}
	for (Map.Entry<String, DataAcquisitionSchemaObject> entry : mapDasos.entrySet()) {
	    String key = entry.getKey();
	    DataAcquisitionSchemaObject daso = entry.getValue();
	    if (soc.getSOCReference().equals(daso.getLabel())) {
		return daso;
	    }
	}
	return null;
    }
    
    private boolean findCreateAssociatedSOC(DataAcquisitionSchemaObject daso) {
	ObjectCollection associatedSOC = null;
	
	//  Try to find existing SOC
	for (ObjectCollection soc : socsList) {
	    if (soc.getSOCReference().equals(daso.getLabel())) {
		associatedSOC = ObjectCollection.find(soc.getUri());
		if (associatedSOC != null) {
		    System.out.println("DASOInstanceGenerator: Reference: " + daso.getLabel() + "  Associated SOC : " + associatedSOC + "    with hasScope: " + associatedSOC.getHasScopeUri());
		    if (!requiredSocs.containsKey(associatedSOC.getUri())) {
			requiredSocs.put(associatedSOC.getUri(), associatedSOC);
		    }
		    break;
		}
	    }
	}
	
	//  Create a SOC when existing SOCs can be associated
	if (associatedSOC == null) { 
	    String newSOCUri = studyUri.replace("STD","SOC") + "-" + daso.getLabel().replace("??","");
	    String scopeUri = "";
	    if (daso != null) {
		ObjectCollection scopeObj = socFromTargetDaso(daso, socsList);
		if (scopeObj != null && scopeObj.getUri() != null) {
		    scopeUri = scopeObj.getUri();
		} else {
		    System.out.println("DASOInstanceGenerator:       SOC association temporarily suspended.");
		    return false;
		} 
	    }
	    String newLabel = daso.getLabel().replace("??","");
	    String collectionType = null;
	    if (isSample(daso)) {
		collectionType = ObjectCollection.SAMPLE_COLLECTION;
	    } else {
		collectionType = ObjectCollection.SUBJECT_COLLECTION;
	    }
	    ObjectCollection newSoc = new ObjectCollection(newSOCUri, collectionType, newLabel, newLabel, studyUri, scopeUri, "", "", daso.getLabel(), null, null);
	    newSoc.saveToTripleStore();
	    if (!requiredSocs.containsKey(newSoc.getUri())) {
		requiredSocs.put(newSoc.getUri(), newSoc);
		socsList.add(newSoc);
	    }
	    System.out.println("DASOInstanceGenerator: Reference: " + daso.getLabel() + "   Created SOC : " + newSOCUri + "    with hasScope: " + scopeUri);
	}
	
	return true;
    }
    
    private String getPrettyLabel(String label) {
	String prettyLabel = label;
	if (!prettyLabel.equals("")) {
	    String c0 = prettyLabel.substring(0,1).toUpperCase();
	    if (prettyLabel.length() == 1) {
		prettyLabel = c0;
	    } else {
		prettyLabel = c0 + prettyLabel.substring(1);
	    }
	}
	return prettyLabel;
    }

    /* **************************************************************************************
     *                                                                                      *
     *                GENERATE INSTANCES FOR A GIVE ROW's IDENTIFIER                        *
     *                                                                                      *
     ****************************************************************************************/
    
    public Map<String,Map<String, String>> generateRowInstances(String id){
	/* Returns : First String : DASO's Label
	 *           Object URI   : The actual URI of the object that was retrieved/created for the identifier in CSV Record
	 */
	
	if (id == null || id.equals("")) {
	    System.out.println("DASOInstanceGenerator: no identifier provided. SEe if your SDD contains an identifier," + 
			       " and if the corresponding label in ths file is a valid identifier.");
	    return null;
	}
	//System.out.println("DASOInstanceGenerator: generate row instances for : " + id);
	
	Map<String,Map<String,String>> objMapList = new HashMap<String,Map<String,String>>();
	
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
	    
	    //System.out.println("DASOInstanceGenerator:     PATH >>> ");
	    
	    // Lookup first study object
	    ObjectCollection currentSoc = iter.previous();
	    String currentObjUri = StudyObject.findUriBySocAndOriginalId(currentSoc.getUri(), id); 
	    //System.out.println("DASOInstanceGenerator:          Obj Original ID=[" + id + "]   SOC=[" + currentSoc.getUri() + "] =>  Obj URI=[" + currentObjUri + "]");
	    
	    while (iter.hasPrevious()) {
		ObjectCollection nextSoc = iter.previous();
		//System.out.println("            " + nextSoc.getUri() + "  ");
		
		/*
		 *   RETRIEVE/CREATE next object in the path
		 */
		
		String nextObjUri = StudyObject.findUriBySocAndScopeUri(nextSoc.getUri(), currentObjUri); 
		if (nextObjUri == null || nextObjUri.equals("")) {
		    nextObjUri = createObject(nextSoc, currentObjUri);
		}
		
		if (nextObjUri == null || nextObjUri.equals("")) {
		    System.out.println("DASOInstanceGenerator:          [ERROR] Path generation stopped. Error ocurred retrieving/creating objects in path. See log above.");
		    currentSoc = nextSoc;
		    currentObjUri = nextObjUri;
		    break;
		}
		
		//System.out.println("DASOInstanceGenerator:          Scope Obj URI=[" + currentObjUri + "]  SOC=[" + nextSoc.getUri() + 
		//		   "]  =>  Obj Uri=[" + nextObjUri + "]");
		
		currentSoc = nextSoc;
		currentObjUri = nextObjUri;
	    }
	    
	    Map<String,String> referenceEntry = new HashMap<String,String>();
	    referenceEntry.put(StudyObject.STUDY_OBJECT_URI, currentObjUri);
	    referenceEntry.put(StudyObject.SOC_TYPE, currentSoc.getTypeUri());
	    referenceEntry.put(StudyObject.SOC_LABEL, socLabels.get(currentSoc.getSOCReference()));
	    referenceEntry.put(StudyObject.OBJECT_SCOPE_URI, id);
	    
	    objMapList.put(currentSoc.getSOCReference(),referenceEntry);
	    
	}
	
	//System.out.println("DASOInstanceGenerator:     Response >>> ");
	for (Map.Entry<String, Map<String,String>> entry : objMapList.entrySet()) {
	    String label = entry.getKey();
	    Map<String,String> objMapEntry = entry.getValue();
	    //System.out.println("DASOInstanceGenerator:          Label=[" + label + "]    Obj Uri=[" + objMapEntry.get(StudyObject.STUDY_OBJECT_URI) + "]");
	}
	
	return objMapList;
	
    }// /generateRowInstances
    
    /*
     *   CREATE next object in the path if it does not exist
     */
    
    private String createObject(ObjectCollection nextSoc, String currentObjUri) {
	//public StudyObject(String uri, String typeUri, String originalId, String label, String isMemberOf, String comment,
	//		   List<String> scopeUris, List<String> timeScopeUris, List<String> spaceScopeUris) 
	
	String newOriginalId = String.valueOf(nextSoc.getNextCounter());
	String newUri = createObjectUri(newOriginalId, nextSoc.getUri(), nextSoc.getTypeUri());
	String newLabel = createObjectLabel(newOriginalId, nextSoc.getUri(), nextSoc.getTypeUri());
	String newTypeUri = "";
	DataAcquisitionSchemaObject daso = dasoFromSoc(nextSoc, dasos);
	if (daso == null || daso.getEntity() == null || daso.getEntity().equals("")) {
	    if (nextSoc.getTypeUri().equals(ObjectCollection.SUBJECT_COLLECTION)) {
		newTypeUri = URIUtils.replacePrefixEx(SIO_OBJECT);
	    } else {
		newTypeUri = URIUtils.replacePrefixEx(SIO_SAMPLE);
	    }
	} else {
	    newTypeUri = daso.getEntity();
	}
	List<String> newScopeUris = new ArrayList<String>();
	List<String> newTimeScopeUris = new ArrayList<String>();
	List<String> newSpaceScopeUris = new ArrayList<String>();
	newScopeUris.add(currentObjUri);
	StudyObject newObj = new StudyObject(newUri, newTypeUri, newOriginalId, newLabel, nextSoc.getUri(), "Automatically generated",
					     newScopeUris, newTimeScopeUris, newSpaceScopeUris);
	if (newObj == null) {
	    System.out.println("DASOInstanceGenerator:          [ERROR] Unable to create Obj");
	    return "";
	} else {
	    newObj.saveToTripleStore();
	    System.out.println("DASOInstanceGenerator:          Created Obj with URI=[" + newUri + "]   Type=[" + newTypeUri + "]");
	} 
	return newObj.getUri();
    }
    
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
