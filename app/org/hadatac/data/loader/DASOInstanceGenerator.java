package org.hadatac.data.loader;

import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.VirtualColumn;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.StudyObjectMatching;
import org.hadatac.entity.pojo.Study;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.data.loader.Cache;

import java.lang.String;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;


public class DASOInstanceGenerator extends BaseGenerator {

	private final boolean DEBUG_MODE = false;
	private final int ID_LENGTH = 5;
	
	private final String SIO_OBJECT = "sio:Object";
    private final String SIO_SAMPLE = "sio:Sample";;
    private final String kbPrefix = ConfigProp.getKbPrefix();
    private String studyUri;
    private String oasUri;
    private DataAcquisitionSchema das;
    private String mainLabel;
    private DataAcquisitionSchemaObject mainDaso;
    private String mainDasoUri;
    private ObjectCollection mainSoc;
    private ObjectCollection groundingSoc;
    private String mainSocUri;
    private String fileName; 
    private Map<String, DataAcquisitionSchemaObject> dasos = new ConcurrentHashMap<String, DataAcquisitionSchemaObject>();  
    private Map<String, ObjectCollection> requiredSocs = new ConcurrentHashMap<String, ObjectCollection>();  
    private List<ObjectCollection> socsList = null;  
    private List<ObjectCollection> groundingPath = new ArrayList<ObjectCollection>();  
    private Map<String, List<ObjectCollection>> socPaths = new HashMap<String, List<ObjectCollection>>(); 
    private Map<String, String> socLabels = new ConcurrentHashMap<String, String>();
    private Map<String, ObjectCollection> socMatchingSOCs = new ConcurrentHashMap<String, ObjectCollection>();
    
    public DASOInstanceGenerator(DataFile dataFile, String studyUri, String oasUri, DataAcquisitionSchema das, String fileName) {
        super(dataFile);

        this.studyUri = studyUri;
        this.oasUri = oasUri;
        this.das = das;
        socPaths.clear();
        socLabels.clear();

        /* ***************************************************************************************
         *                                                                                       *
         *                MAPPING OF IDENTIFIER AND ASSOCIATED OBJECTS                           *
         *                                                                                       *
         ****************************************************************************************/

        mainLabel = "";
        String origId = das.getOriginalIdLabel(); 
        String id = das.getIdLabel(); 
        if (origId != null && !origId.equals("")) {
            mainLabel = origId;
        } else if (id != null && !id.equals("")) {
            mainLabel = id;
        }

        if (fileName == null || fileName.equals("")) {
            logger.printException("DASOInstanceGenerator: [ERROR] NO RECORD FILE PROVIDED");
            return;
        } 
        this.fileName = fileName;

        if (mainLabel.equals("")) {
            logger.printException("DASOInstanceGenerator: NO IDENTIFIER");
            return;
        } else {
            logger.println("DASOInstanceGenerator: Study URI: " + studyUri);
            logger.println("DASOInstanceGenerator: Label of main DASO: " + mainLabel);
        }

        ////////////////////////////////////////////
        // Strictly follow the given order of steps
        if (!retrieveAvailableSOCs()) {
            return;
        }
        if (!identifyMainDASO()) {
            return;
        }
        if (!identifyGroundingPathForMainSOC()) {
            return;
        }
        if (!identifyTargetDasoURIs()) {
            return;
        }
        if (!identitySOCsForDASOs()) {
            return;
        }
        if (!retrieveAdditionalSOCs()) {
            return;
        }
        if (!printRequiredSOCs()) {
            return;
        }
        if (!computePathsForTargetSOCs()) {
            return;
        }
        if (!computeLabelsForTargetSOCs()) {
            return;
        }
        if (!mapSOCsAndMatchings()) {
            return;
        }
        ////////////////////////////////////////////
    }

    private boolean retrieveAvailableSOCs() {
        /* 
         *  (1/10) INITIALLY AVAILABLE SOCs
         */

        logger.println("DASOInstanceGenerator: (1/10) ======== INITIALLY AVAILABLE SOCs ========");
        socsList = ObjectCollection.findByStudyUri(studyUri);
        if (socsList == null) {
            logger.println("DASOInstanceGenerator: no SOC is available");
            socsList = new ArrayList<ObjectCollection>();  
        } else {
            for (ObjectCollection soc : socsList) {
                logger.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Reference : " + soc.getSOCReference());
            }
        }
        
        return true;
    }

    private boolean identifyMainDASO() {
        /* 
         *  (2/10) IDENTIFY MAIN DASO and DASOS REQUIRED FROM DASAs. THESE DASOS ARE LISTED IN STEP (4)
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
                    logger.printException("DASOInstanceGenerator: [ERROR] FAILED TO LOAD MAIN DASO");
                    return false;
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
            logger.printException("DASOInstanceGenerator: FAILED TO LOAD MAIN SOC. The virtual column for the file identifier (the row with attribute hasco:originalID) is not one of the firtual columns in the SSD for this study.");
            return false;
        }
        mainSocUri = mainSoc.getUri();
        logger.println("DASOInstanceGenerator: (2/10) ============= MAIN DASO ================");
        logger.println("DASOInstanceGenerator: Main DASO: " + mainDasoUri);
        logger.println("DASOInstanceGenerator: Main SOC: " + mainSocUri);
        
        return true;
    }

    private boolean identifyGroundingPathForMainSOC() {
        /* 
         *  (3/10) IDENTIFY GROUNDING PATH FOR MAIN SOC
         */

        logger.println("DASOInstanceGenerator: (3/10) =========== GROUNDING PATH FOR  MAIN SOC ============");
        if (mainSoc.getHasScopeUri() == null || mainSoc.getHasScopeUri().equals("")) {
            logger.println("DASOInstanceGenerator: Main SOC is already grounded. No grouding path required");
            groundingSoc = mainSoc;
        } else {
            logger.println("DASOInstanceGenerator: Main SOC is not grounded. Computing grouding path");
            ObjectCollection currentSoc = mainSoc;
            while (currentSoc.getHasScopeUri() != null && !currentSoc.getHasScopeUri().equals("") && !containsUri(currentSoc.getHasScopeUri(), groundingPath)) {

                ObjectCollection nextSoc = ObjectCollection.find(currentSoc.getHasScopeUri());
                if (nextSoc == null) {
                    logger.printException("DASOInstanceGenerator: Could not find SOC with following URI : " + currentSoc.getHasScopeUri());
                    return false;
                } else {
                    if (!containsUri(nextSoc.getUri(), groundingPath)) {
                        groundingPath.add(nextSoc);
                    }
                    currentSoc = nextSoc;
                }
            }
            for (ObjectCollection soc : groundingPath) {
                logger.println("DASOInstanceGenerator: SOC in grouding path: " + soc.getUri());
            }
        }
        
        return true;
    }

    private boolean identifyTargetDasoURIs() {
        /* 
         *  (4/10) IDENTIFY URIs of TARGET DASOs
         */

        logger.println("DASOInstanceGenerator: (4/10) =============== TRAVERSE DASOS ================");

        for (Map.Entry<String, DataAcquisitionSchemaObject> entry : dasos.entrySet()) {
            String key = entry.getKey();
            DataAcquisitionSchemaObject daso = entry.getValue();
            processTargetDaso(daso);
        }
        
        return true;
    }

    private boolean identitySOCsForDASOs() {
        /* 
         *  (5/10) IDENTIFY SOCs ASSOCIATED WITH IDENTIFIED DASOs
         */

        logger.println("DASOInstanceGenerator: (5/10) ===== IDENTIFY SOCs ASSOCIATED WITH IDENTIFIED DASOs ======");

        this.requiredSocs.clear();
        for (Map.Entry<String, DataAcquisitionSchemaObject> entry : dasos.entrySet()) {
            String key = entry.getKey();
            DataAcquisitionSchemaObject daso = entry.getValue();
            if (!findCreateAssociatedSOC(daso)) {
                logger.printWarning("DASOInstanceGenerator: Cannot create SOC for the following daso: " + daso.getUri());
            }
        }
        
        return true;
    }

    private boolean retrieveAdditionalSOCs() {
        /* 
         *  (6/10) RETRIEVING ADDITIONAL SOCs required for traversing existing SOCs
         */

        logger.println("DASOInstanceGenerator: (6/10) ======== RETRIEVING ADDITINAL  SOCs ========");
        for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
            String key = entry.getKey();
            ObjectCollection soc = entry.getValue();
            ObjectCollection currentSoc = soc;
            while (currentSoc.getHasScopeUri() != null && !currentSoc.getHasScopeUri().equals("") && 
                    !requiredSocs.containsKey(currentSoc.getHasScopeUri()) && !containsUri(currentSoc.getHasScopeUri(),groundingPath)) {

                // lookup in socsList for next SOC, i.e., currentSoc.getHasScopeUri()
                ObjectCollection nextSoc = null;
                for (ObjectCollection tmpSoc : socsList) {
                    if (tmpSoc.getUri().equals(currentSoc.getHasScopeUri())) {
                        nextSoc = tmpSoc;
                        break;
                    }
                }
                if (nextSoc == null) {
                    logger.printException("DASOInstanceGenerator: Could not find SOC with following URI : " + currentSoc.getHasScopeUri());
                    return false;
                } else {
                    if (!requiredSocs.containsKey(nextSoc.getUri())) {
                        requiredSocs.put(nextSoc.getUri(), nextSoc);
                        logger.println("DASOInstanceGenerator: Loading SOC: " + nextSoc.getUri() + " to required SOCs");
                    }
                    currentSoc = nextSoc;
                }
            }   
        }
        
        return true;
    }

    private boolean printRequiredSOCs() {
        /* 
         *  (7/10) LIST OF REQUIRED SOCs
         */

        logger.println("DASOInstanceGenerator: (7/10) ======== REQUIRED SOCs ========");
        for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
            String key = entry.getKey();
            ObjectCollection soc = entry.getValue();
            logger.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Reference : " + soc.getSOCReference() + 
                    "    with hasScope: " + soc.getHasScopeUri());
        }
        
        return true;
    }

    private boolean computePathsForTargetSOCs() {
        /* 
         *  (8/10) COMPUTE PATH for each TARGET SOC
         */

        logger.println("DASOInstanceGenerator: (8/10) ======== BUILD SOC PATHS ========");
        for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
            String key = entry.getKey();
            ObjectCollection soc = entry.getValue();
            List<ObjectCollection> socs = new ArrayList<ObjectCollection>();
            logger.println("DASOInstanceGenerator: START: " + soc.getUri());
            logger.println("DASOInstanceGenerator: PATH ---->> ");
            if (soc.getHasScope() == null) {
                logger.println("DASOInstanceGenerator:       " + soc.getUri());
                socs.add(soc);
                socPaths.put(key,socs);
            } else {
                String toUri = soc.getHasScope().getUri();
                ObjectCollection nextTarget = soc;
                logger.println("DASOInstanceGenerator:       " + nextTarget.getUri());
                socs.add(nextTarget);
                while (!nextTarget.getUri().equals(mainSocUri)) {
                    String nextTargetUri = nextTarget.getHasScopeUri();
                    nextTarget =  requiredSocs.get(nextTargetUri);
                    if (nextTarget == null) {
                        logger.printException("DASOInstanceGenerator: Could not complete path for " + toUri);
                        return false;
                    }
                    logger.println("DASOInstanceGenerator:       " + nextTarget.getUri());
                    socs.add(nextTarget);
                }
                socPaths.put(key,socs);
            } 
        }
        
        return true;
    }

    private boolean computeLabelsForTargetSOCs() {
        /* 
         *  (9/10) COMPUTE LABEL for each TARGET SOC
         */

        logger.println("DASOInstanceGenerator: (9/10) ======== COMPUTE SOC LABELS ========");
        for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
            String key = entry.getKey();
            ObjectCollection soc = entry.getValue();
            String fullLabel = "";
            boolean process = true;

            logger.println("DASOInstanceGenerator: START: " + soc.getUri());
            String label = soc.getGroundingLabel();
            if (label == null) {
                label = "";
            }
            if (soc.getHasScope() == null || !label.equals("")) {
                fullLabel = label;
                logger.println("DASOInstanceGenerator: Computed label [" + fullLabel + "]");
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
                        logger.printException("DASOInstanceGenerator: Could not complete path for " + toUri);
                        return false;
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
                logger.println("DASOInstanceGenerator: Computed label [" + fullLabel + "]");       
                if (soc.getRoleLabel() == null || soc.getRoleLabel().equals("")) {
                    soc.saveRoleLabel(fullLabel);
                }
                socLabels.put(soc.getSOCReference(), fullLabel);
            }
        }
        
        return true;
    }

    private boolean mapSOCsAndMatchings() {
        /* 
         *  (10/10) Map SOCs and Matchings 
         */

        logger.println("DASOInstanceGenerator: (10/10) ======== MAP SOCs and MATCHINGS ========");
        for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
            String key = entry.getKey();
            ObjectCollection soc = entry.getValue();
            if (!socMatchingSOCs.containsKey(soc.getUri())) {
            	List<ObjectCollection> matchingSOCs = ObjectCollection.findMatchingScopeCollections(soc.getUri());
            	if (matchingSOCs.size() > 1) {
            		logger.printWarning("DASOInstanceGenerator: SOC: " + soc.getUri() + "   has more than one matching SOC");
            	}
            	if (matchingSOCs.size() >- 0) {
            		socMatchingSOCs.put(soc.getUri(), matchingSOCs.get(0));
            		logger.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Has matching SOC: " + matchingSOCs.get(0).getUri());
            	}
            }
        }
        
        return true;
    }

    /* **************************************************************************************
     *                                                                                      *
     *                            SUPPORTING METHODS                                        *
     *                                                                                      *
     ****************************************************************************************/

    private boolean containsUri(String uri, List<ObjectCollection> list) {
        if (uri == null || uri.equals("") || list == null || list.size() == 0) {
            return false;
        }
        for (ObjectCollection soc : list) {
            if (soc.getUri() != null && !soc.getUri().equals("")) {
                if (soc.getUri().equals(uri)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean processTargetDaso(DataAcquisitionSchemaObject daso) { 
        String toUri = targetUri(daso);
        logger.println("DASOInstanceGenerator: DASO: " + daso.getUri() + "   From : " + daso.getLabel() + "  To: " + toUri);

        //  LOAD each TARGET DASO into DASOs, if TARGET DASO is not loaded yet
        if (toUri != null && !toUri.equals("") && !dasos.containsKey(toUri)) {
            logger.println("DASOInstanceGenerator: Loading " + toUri);
            DataAcquisitionSchemaObject newDaso = DataAcquisitionSchemaObject.find(toUri);
            if (newDaso == null) {
                logger.println("DASOInstanceGenerator: [ERROR] Could not find DASO with following URI : " + toUri);
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
        	//logger.println("socFromTargetDaso : " + targetObj.getLabel() + "    soc's getSOCReference " + soc.getSOCReference()); 
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
                    logger.println("DASOInstanceGenerator: Reference: " + daso.getLabel() + "  Associated SOC : " + associatedSOC + "    with hasScope: " + associatedSOC.getHasScopeUri());
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
                	String tmpUri = targetUri(daso);
                	if (tmpUri == null || tmpUri.isEmpty()) {
                		logger.println("DASOInstanceGenerator:       [WARNING] SOC association ignored for " + daso.getUri());
                		return false;
                	} 
                	scopeUri = tmpUri.replace("DASO", "SOC");
                }
            }
            String newLabel = daso.getLabel().replace("??","");
            String collectionType = null;
            if (daso.getEntity().equals(URIUtils.replacePrefixEx("hasco:StudyObjectMatching"))) {
            	collectionType = ObjectCollection.MATCHING_COLLECTION;
            } else if (isSample(daso)) {
                collectionType = ObjectCollection.SAMPLE_COLLECTION;
            } else {
                collectionType = ObjectCollection.SUBJECT_COLLECTION;
            }

            VirtualColumn newVc = VirtualColumn.find(studyUri, daso.getLabel());
            if (newVc == null) {
                newVc = new VirtualColumn(studyUri, "", daso.getLabel());
                newVc.setNamedGraph(oasUri);
                newVc.saveToTripleStore();
                // addObject(newVc);
            }
            ObjectCollection newSoc = new ObjectCollection(newSOCUri, collectionType, newLabel, newLabel, studyUri, 
            		newVc.getUri(), "", scopeUri, null, null, null, "0");
            newSoc.setNamedGraph(oasUri);
            newSoc.saveToTripleStore();
            // addObject(newSoc);

            if (!requiredSocs.containsKey(newSoc.getUri())) {
                requiredSocs.put(newSoc.getUri(), newSoc);
                socsList.add(newSoc);
            }
            logger.println("DASOInstanceGenerator: Reference: " + daso.getLabel() + "   Created SOC : " + newSOCUri + "    with hasScope: " + scopeUri);
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
     *                GENERATE INSTANCES FOR A GIVEN ROW's IDENTIFIER                       *
     *                                                                                      *
     ****************************************************************************************/

    public Map<String, Map<String, String>> generateRowInstances(String id) {
        /* Returns : First String : DASO's Label
         *           Object URI   : The actual URI of the object that was retrieved/created for the identifier in CSV Record
         */

        if (id == null || id.equals("")) {
            System.out.println("DASOInstanceGenerator: [ERROR] no identifier provided. See if your SDD contains an identifier," + 
                    " and if the corresponding label in ths file is a valid identifier.");
            return null;
        }
        if (DEBUG_MODE) { 
        	System.out.println("DASOInstanceGenerator: generate row instances for : " + id);
        }

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

            if (DEBUG_MODE) { 
            	System.out.println("DASOInstanceGenerator:     PATH >>> ");
            }

            // Lookup first study object
            ObjectCollection currentSoc = iter.previous();
            String currentObjUri = getCachedObjectBySocAndOriginalId(currentSoc.getUri(), id); 
            if (DEBUG_MODE) { 
            	System.out.println("DASOInstanceGenerator:          Obj Original ID=[" + id + "]   SOC=[" + currentSoc.getUri() + "] =>  Obj URI=[" + currentObjUri + "]");
            }

            ObjectCollection previousSoc = null;
            String previousObjUri = null;
            while (currentObjUri != null && !currentObjUri.equals("") && iter.hasPrevious()) {
                ObjectCollection nextSoc = iter.previous();
                if (DEBUG_MODE) { 
                	System.out.println("            " + nextSoc.getUri() + "  ");
                }

                /*
                 *   RETRIEVE/CREATE next object in the path
                 */

                String nextObjUri = getCachedSocAndScopeUri(nextSoc.getUri(), currentObjUri); 
                if (nextObjUri == null || nextObjUri.equals("")) {
                    nextObjUri = createStudyObject(nextSoc, currentObjUri);
                }

                if (nextObjUri == null || nextObjUri.equals("")) {
                    if (DEBUG_MODE) { 
                    	System.out.println("DASOInstanceGenerator:          [ERROR] Path generation stopped. Error ocurred retrieving/creating objects in path. See log above.");
                    }
                    currentSoc = nextSoc;
                    currentObjUri = nextObjUri;
                    break;
                }

                if (DEBUG_MODE) { 
                	System.out.println("DASOInstanceGenerator:          Scope Obj URI=[" + currentObjUri + "]  SOC=[" + nextSoc.getUri() + 
                			"]  =>  Obj Uri=[" + nextObjUri + "]");
                }

                previousSoc = currentSoc;
                previousObjUri = currentObjUri;
                currentSoc = nextSoc;
                currentObjUri = nextObjUri;
            }

            if (currentObjUri == null || currentObjUri.equals("")) {
            	System.out.println("DASOInstanceGenerator:     Response >>> failed to load object");
            } else {
            	StudyObject obj = getCachedObject(currentObjUri);
            	if (obj != null) { 
            		// TO-DO we will need to have a mechanism to decide whether to use instances or classes to represent abstract time
            		List<String> objTypeTimes = StudyObject.retrieveTimeScopeTypeUris(currentObjUri);
            		Map<String,String> referenceEntry = new HashMap<String,String>();
            		referenceEntry.put(StudyObject.STUDY_OBJECT_URI, currentObjUri);
            		referenceEntry.put(StudyObject.STUDY_OBJECT_TYPE, obj.getTypeUri());
            		referenceEntry.put(StudyObject.SOC_TYPE, currentSoc.getTypeUri());
            		referenceEntry.put(StudyObject.SOC_LABEL, socLabels.get(currentSoc.getSOCReference()));
            		referenceEntry.put(StudyObject.SUBJECT_ID, id);
            		if (previousObjUri != null && !previousObjUri.isEmpty()) {
            			referenceEntry.put(StudyObject.SCOPE_OBJECT_URI, previousObjUri);
            		}
            		if (previousSoc != null && previousSoc.getUri() != null && !previousSoc.getUri().isEmpty()) {
            			referenceEntry.put(StudyObject.SCOPE_OBJECT_SOC_URI, previousSoc.getUri());
            		}
            		referenceEntry.put(StudyObject.OBJECT_ORIGINAL_ID, obj.getOriginalId());
            		referenceEntry.put(StudyObject.SOC_URI, currentSoc.getUri());
            		if (objTypeTimes != null && objTypeTimes.size() > 0) {
            			referenceEntry.put(StudyObject.OBJECT_TIME, objTypeTimes.get(0));
            		}

            		objMapList.put(currentSoc.getSOCReference(),referenceEntry);

            	}	
            }

        }

        if (DEBUG_MODE) { 
        	System.out.println("DASOInstanceGenerator:     Response >>> ");
        	for (Map.Entry<String, Map<String,String>> entry : objMapList.entrySet()) {
        		String label = entry.getKey();
        		Map<String,String> objMapEntry = entry.getValue();
        		System.out.println("DASOInstanceGenerator:          Label=[" + label + "]    Obj Uri=[" + objMapEntry.get(StudyObject.STUDY_OBJECT_URI) + "]");
        	}
        }

        return objMapList;
    }// /generateRowInstances

    /*
     *   CREATE next object in the path if it does not exist
     */

    private String createStudyObject(ObjectCollection nextSoc, String currentObjUri) {
        String newOriginalId = String.valueOf(nextSoc.getNextCounter());
        newOriginalId = addLeftZeros(newOriginalId);
        String newUri = createObjectUri(newOriginalId, nextSoc.getUri(), nextSoc.getTypeUri());
        String newLabel = createObjectLabel(newOriginalId, nextSoc);
        String newTypeUri = "";
        DataAcquisitionSchemaObject daso = dasoFromSoc(nextSoc, dasos);
        if (daso == null || daso.getEntity() == null || daso.getEntity().equals("")) {
            if (nextSoc.getTypeUri().equals(ObjectCollection.MATCHING_COLLECTION)) {
                newTypeUri = URIUtils.replacePrefixEx(StudyObjectMatching.className);
            } else if (nextSoc.getTypeUri().equals(ObjectCollection.SUBJECT_COLLECTION)) {
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
        newObj.setNamedGraph(oasUri);
        newObj.setDeletable(false);
        addObjectToCache(newObj, currentObjUri);

        if (DEBUG_MODE) { 
        	System.out.println("DASOInstanceGenerator:          Created Obj with URI=[" + newUri + "]   Type=[" + newTypeUri + "]");
        }

        return newObj.getUri();
    }

    private String addLeftZeros(String str) {
    	str = str.trim();
    	String zeros = "";
    	if (str.length() < ID_LENGTH) {
    		while (zeros.length() <= ID_LENGTH - str.length()) {
    			zeros = "0" + zeros;
    		}
    	}
    	return zeros + str;
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

    private String createObjectLabel(String originalID, ObjectCollection soc) {
        if (soc.getRoleLabel() != null && !soc.getRoleLabel().equals("")) {
        	return soc.getRoleLabel() + " " + originalID;
        } 
        String labelPrefix = "";
        if (soc.getTypeUri().equals(ObjectCollection.SUBJECT_COLLECTION)) {
            labelPrefix = "SBJ ";
        } else {
            labelPrefix = "SPL ";
        }
        return labelPrefix + originalID + " - " + socIdFromUri(soc.getUri());

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
    

    /*
     *   METHODS RELATED TO INTERNAL CACHE
     */

    public boolean initiateCache(String study_uri) {
    	if (study_uri == null || study_uri.equals("")) {
    		return false;
    	}
    	Study study = Study.find(study_uri);
    	if (mainSoc != null) {
    		System.out.println("INITIATE CACHE BEING CALLED!");
    		addCache(new Cache<String, StudyObject>("cacheObject", true, study.getObjectsMapInBatch()));
    		addCache(new Cache<String, String>("cacheObjectBySocAndScopeUri", false, StudyObject.buildCachedObjectBySocAndScopeUri()));
    		addCache(new Cache<String, String>("cacheObjectBySocAndOriginalId", false, StudyObject.buildCachedObjectBySocAndOriginalId()));
    		addCache(new Cache<String, String>("cacheScopeBySocAndObjectUri", false, StudyObject.buildCachedScopeBySocAndObjectUri()));
    	    
    		return true;
    	}
    	return false;
    }
    
    @SuppressWarnings("unchecked")
    private void addObjectToCache(StudyObject newObj, String scopeObjUri) {
    	if (newObj == null || caches.get("cacheObject").containsKey(newObj.getUri())) {
    		return;
    	}
    	
    	if (!caches.get("cacheObject").containsKey(newObj.getUri())) {
    	    caches.get("cacheObject").put(newObj.getUri(), newObj);
    	}
    	
    	String keySocAndOriginalId = newObj.getIsMemberOf() + ":" + newObj.getOriginalId();
    	if (!caches.get("cacheObjectBySocAndOriginalId").containsKey(keySocAndOriginalId)) {
    	    caches.get("cacheObjectBySocAndOriginalId").put(keySocAndOriginalId, newObj.getUri());
    	}
    	
    	String keySocAndScopeUri =  newObj.getIsMemberOf() + ":" + scopeObjUri;
    	if (!caches.get("cacheObjectBySocAndScopeUri").containsKey(keySocAndScopeUri)) {
    		caches.get("cacheObjectBySocAndScopeUri").put(keySocAndScopeUri, newObj.getUri());
    	}
    }

    @SuppressWarnings("unchecked")
    private StudyObject getCachedObject(String key) {
    	if (caches.get("cacheObject").containsKey(key)) {
    		return (StudyObject)caches.get("cacheObject").get(key); 
    	} else {
    		return null;
    	}
    }
    
    @SuppressWarnings("unchecked")
    private String getCachedObjectBySocAndOriginalId(String soc_uri, String id) {
    	String key = soc_uri + ":" + id;
    	if (caches.get("cacheObjectBySocAndOriginalId").containsKey(key)) {
    		return (String)caches.get("cacheObjectBySocAndOriginalId").get(key); 
    	} else {
    		return null;
    	}
    }
    
    @SuppressWarnings("unchecked")
    private String getCachedSocAndScopeUri(String soc_uri, String scope_uri) {
    	String key = soc_uri + ":" + scope_uri;
    	if (caches.get("cacheObjectBySocAndScopeUri").containsKey(key)) {
    		return (String)caches.get("cacheObjectBySocAndScopeUri").get(key); 
    	} else {
    		return null;
    	}
    }
    
    @SuppressWarnings("unchecked")
    private String getCachedScopeBySocAndObjectUri(String soc_uri, String obj_uri) {
    	String key = soc_uri + ":" + obj_uri;
    	if (caches.get("cacheScopeBySocAndObjectUri").containsKey(key)) {
    		return (String)caches.get("cacheScopeBySocAndObjectUri").get(key); 
    	} else {
    		return null;
    	}
    }
 
    public Map<String,ObjectCollection> getMatchingSOCs() {
    	return socMatchingSOCs;
    }
    
    /* **************************************************************************************
     *                                                                                      *
     *  RETRIEVE URI, ORIGINAL ID,  AND TYPE OF GROUNDING OBJECT FROM CURRENT OBJECT URI    *
     *                                                                                      *
     ****************************************************************************************/

    public Map<String, String> retrieveGroundObject(String id) {
        /* Returns : First String : DASO's Label
         *           Object URI   : The actual URI of the object that was retrieved/created for the identifier in CSV Record
         */

        if (id == null || id.equals("")) {
            System.out.println("DASOInstanceGenerator: [ERROR] no identifier provided. See if your SDD contains an identifier," + 
                    " and if the corresponding label in ths file is a valid identifier.");
            return null;
        }
        if (DEBUG_MODE) { 
        	System.out.println("DASOInstanceGenerator: retrieve ground object for : " + id);
        	System.out.println("DASOInstanceGenerator: groundingPath : " + groundingPath);
        }

        ObjectCollection currentSoc = mainSoc;
        StudyObject obj = null;
        Map<String,String> groundObj = new HashMap<String,String>();

        if (DEBUG_MODE) { 
        	System.out.println("DASOInstanceGenerator:     PATH >>> ");
        }

        // Lookup first study object
        if (DEBUG_MODE) { 
        	System.out.println("DASOInstanceGenerator: CachedObjectBySocAndOriginalId: soc: [" +currentSoc.getUri() + "]   Id: [" + id + "]");
        }
        String currentObjUri = getCachedObjectBySocAndOriginalId(currentSoc.getUri(), id); 
        if (DEBUG_MODE) { 
        	System.out.println("DASOInstanceGenerator: currentObjUri: [" +currentObjUri + "]");
        }

        if (groundingPath == null || groundingPath.size() <= 0) {
        	obj = getCachedObject(currentObjUri);
        	if (obj == null || obj.getUri() == null || obj.getUri().equals("")) {
                System.out.println("DASOInstanceGenerator: [ERROR] Could not retrieve first Study Object for URI=[" + currentObjUri + "]");
                return null;
            }
            groundObj.put(StudyObject.STUDY_OBJECT_URI, obj.getUri());
            groundObj.put(StudyObject.STUDY_OBJECT_TYPE, obj.getTypeUri());
            groundObj.put(StudyObject.SUBJECT_ID, obj.getOriginalId());
            return groundObj;
        } 

        if (DEBUG_MODE) { 
        	System.out.println("DASOInstanceGenerator:          Obj Original ID=[" + id + "]   SOC=[" + currentSoc.getUri() + "] =>  Obj URI=[" + currentObjUri + "]");
        }

        for (ObjectCollection nextSoc : groundingPath) {
            if (DEBUG_MODE) { 
            	System.out.println("DASOInstanceGenerator:      nextSOC=[" + nextSoc.getUri() + "] Obj URI=[" + currentObjUri + "]");
            }
            String nextObjUri = getCachedScopeBySocAndObjectUri(nextSoc.getUri(), currentObjUri); 
            if (nextObjUri == null || nextObjUri.equals("")) {
                //System.out.println("DASOInstanceGenerator:          [ERROR] Path generation stopped. Error ocurred retrieving/creating objects in path. See log above.");
                currentSoc = nextSoc;
                currentObjUri = nextObjUri;
                break;
            }

            if (DEBUG_MODE) { 
            	System.out.println("DASOInstanceGenerator:          Scope Obj URI=[" + currentObjUri + "]  nextSOC=[" + nextSoc.getUri() + 
            			"]  =>  Obj Uri=[" + nextObjUri + "]");
            }

            currentSoc = nextSoc;
            currentObjUri = nextObjUri;
        }

    	obj = getCachedObject(currentObjUri);
        if (obj == null) {
            System.out.println("DASOInstanceGenerator: [ERROR] Could not retrieve Study Object for URI=[" + currentObjUri + "]");
            return null;
        }
        groundObj.put(StudyObject.STUDY_OBJECT_URI, obj.getUri());
        groundObj.put(StudyObject.STUDY_OBJECT_TYPE, obj.getTypeUri());
        groundObj.put(StudyObject.SUBJECT_ID, obj.getOriginalId());
        return groundObj;

    }// /retrieveGroundObject

}// /class
