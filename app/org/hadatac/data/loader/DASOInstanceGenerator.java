package org.hadatac.data.loader;

import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.utils.ConfigProp;

import java.lang.String;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;


public class DASOInstanceGenerator extends BaseGenerator {

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

    public DASOInstanceGenerator(RecordFile file, String studyUri, String oasUri, DataAcquisitionSchema das, String fileName) {
        super(file);
        
        this.studyUri = studyUri;
        this.oasUri = oasUri;
        this.das = das;
        socPaths.clear();
        socLabels.clear();

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

        if (fileName == null || fileName.equals("")) {
            System.out.println("DASOInstanceGenerator: NO RECORD FILE PROVIDED");
            return;
        } 
        this.fileName = fileName;

        if (mainLabel.equals("")) {
            AnnotationLog.println("DASOInstanceGenerator: NO IDENTIFIER", fileName);
            return;
        } else {
            AnnotationLog.println("DASOInstanceGenerator: Study URI: " + studyUri, fileName);
            AnnotationLog.println("DASOInstanceGenerator: Label of main DASO: " + mainLabel, fileName);
        }

        retrieveAvailableSOCs();
        identifyMainDASO();
        identifyGroundingPathForMainSOC();
        identifyTargetDasoURIs();
        identitySOCsForDASOs();
        retrieveAdditionalSOCs();
        printRequiredSOCs();
        computePathsForTargetSOCs();
        computeLabelsForTargetSOCs();
    }
    
    private void retrieveAvailableSOCs() {
        /* 
         *  (1/9) INITIALLY AVAILABLE SOCs
         */

        AnnotationLog.println("DASOInstanceGenerator: (1/9) ======== INITIALLY AVAILABLE SOCs ========", fileName);
        socsList = ObjectCollection.findByStudyUri(studyUri);
        if (socsList == null) {
            AnnotationLog.println("DASOInstanceGenerator: no SOC is available", fileName);
            socsList = new ArrayList<ObjectCollection>();  
        } else {
            for (ObjectCollection soc : socsList) {
                AnnotationLog.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Reference : " + soc.getSOCReference(), fileName);
            }
        }
    }
    
    private void identifyMainDASO() {
        /* 
         *  (2/9) IDENTIFY MAIN DASO and DASOS REQUIRED FROM DASAs. THESE DASOS ARE LISTED IN STEP (4)
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
                    AnnotationLog.println("DASOInstanceGenerator: [ERROR] FAILED TO LOAD MAIN DASO", fileName);
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
            AnnotationLog.println("DASOInstanceGenerator: [ERROR] FAILED TO LOAD MAIN SOC", fileName);
            return;
        }
        
        mainSocUri = mainSoc.getUri();
        AnnotationLog.println("DASOInstanceGenerator: (2/9) ============= MAIN DASO ================", fileName);
        AnnotationLog.println("DASOInstanceGenerator: Main DASO: " + mainDasoUri, fileName);
        AnnotationLog.println("DASOInstanceGenerator: Main SOC: " + mainSocUri, fileName);
    }
    
    private void identifyGroundingPathForMainSOC() {
        /* 
         *  (3/8) IDENTIFY GROUNDING PATH FOR MAIN SOC
         */

        AnnotationLog.println("DASOInstanceGenerator: (3/9) =========== GROUNDING PATH FOR  MAIN SOC ============", fileName);
        if (mainSoc.getHasScopeUri() == null) {
            AnnotationLog.println("DASOInstanceGenerator: Main SOC is already grounded. No grouding path required", fileName);
            groundingSoc = mainSoc;
        } else {
            AnnotationLog.println("DASOInstanceGenerator: Main SOC is not grounded. Computing grouding path", fileName);
            ObjectCollection currentSoc = mainSoc;
            while (currentSoc.getHasScopeUri() != null && !currentSoc.getHasScopeUri().equals("") && !containsUri(currentSoc.getHasScopeUri(), groundingPath)) {

                ObjectCollection nextSoc = ObjectCollection.find(currentSoc.getHasScopeUri());
                if (nextSoc == null) {
                    AnnotationLog.println("DASOInstanceGenerator: [ERROR] Could not find SOC with following URI : " + currentSoc.getHasScopeUri(), fileName);
                    return;
                } else {
                    if (!containsUri(nextSoc.getUri(), groundingPath)) {
                        groundingPath.add(nextSoc);
                    }
                    currentSoc = nextSoc;
                }
            }
            for (ObjectCollection soc : groundingPath) {
                AnnotationLog.println("DASOInstanceGenerator: SOC in grouding path: " + soc.getUri(), fileName);
            }
        }
    }
    
    private void identifyTargetDasoURIs() {
        /* 
         *  (4/9) IDENTIFY URIs of TARGET DASOs
         */

        AnnotationLog.println("DASOInstanceGenerator: (4/9) =============== TRAVERSE DASOS ================", fileName);

        for (Map.Entry<String, DataAcquisitionSchemaObject> entry : dasos.entrySet()) {
            DataAcquisitionSchemaObject daso = entry.getValue();
            processTargetDaso(daso);
        }
    }
    
    private void identitySOCsForDASOs() {
        /* 
         *  (5/9) IDENTIFY SOCs ASSOCIATED WITH IDENTIFIED DASOs
         */
        AnnotationLog.println("DASOInstanceGenerator: (5/9) ===== IDENTIFY SOCs ASSOCIATED WITH IDENTIFIED DASOs ======", fileName);

        requiredSocs.clear();
        for (Map.Entry<String, DataAcquisitionSchemaObject> entry : dasos.entrySet()) {
            DataAcquisitionSchemaObject daso = entry.getValue();
            if (!findCreateAssociatedSOC(daso)) {
                AnnotationLog.println("DASOInstanceGenerator: [WARNING] Cannot create SOC for the following daso: " + daso.getUri(), fileName);
            }
        }
    }
    
    private void retrieveAdditionalSOCs() {
        /* 
         *  (6/9) RETRIEVING ADDITIONAL SOCs required for traversing existing SOCs
         */

        AnnotationLog.println("DASOInstanceGenerator: (6/9) ======== RETRIEVING ADDITINAL  SOCs ========", fileName);
        for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
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
                    AnnotationLog.println("DASOInstanceGenerator: [ERROR] Could not find SOC with following URI : " + currentSoc.getHasScopeUri(), fileName);
                    return;
                } else {
                    if (!requiredSocs.containsKey(nextSoc.getUri())) {
                        requiredSocs.put(nextSoc.getUri(), nextSoc);
                        AnnotationLog.println("DASOInstanceGenerator: Loading SOC: " + nextSoc.getUri() + " to required SOCs", fileName);
                    }
                    currentSoc = nextSoc;
                }
            }
        }
    }
    
    private void printRequiredSOCs() {
        /* 
         *  (7/9) LIST OF REQUIRED SOCs
         */
        AnnotationLog.println("DASOInstanceGenerator: (7/9) ======== REQUIRED SOCs ========", fileName);
        for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
            ObjectCollection soc = entry.getValue();
            AnnotationLog.println("DASOInstanceGenerator: SOC: " + soc.getUri() + "   Reference : " + soc.getSOCReference() + 
                    "    with hasScope: " + soc.getHasScopeUri(), fileName);
        }
    }
    
    private void computePathsForTargetSOCs() {
        /* 
         *  (8/9) COMPUTE PATH for each TARGET SOC
         */

        AnnotationLog.println("DASOInstanceGenerator: (8/9) ======== BUILD SOC PATHS ========", fileName);
        for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
            String key = entry.getKey();
            ObjectCollection soc = entry.getValue();
            List<ObjectCollection> socs = new ArrayList<ObjectCollection>();
            AnnotationLog.println("DASOInstanceGenerator: START: " + soc.getUri(), fileName);
            AnnotationLog.println("DASOInstanceGenerator: PATH ---->> ", fileName);
            if (soc.getHasScope() == null) {
                AnnotationLog.println("DASOInstanceGenerator:       " + soc.getUri(), fileName);
                socs.add(soc);
                socPaths.put(key,socs);
            } else {
                String toUri = soc.getHasScope().getUri();
                ObjectCollection nextTarget = soc;
                AnnotationLog.println("DASOInstanceGenerator:       " + nextTarget.getUri(), fileName);
                socs.add(nextTarget);
                while (!nextTarget.getUri().equals(mainSocUri)) {
                    String nextTargetUri = nextTarget.getHasScopeUri();
                    nextTarget =  requiredSocs.get(nextTargetUri);
                    if (nextTarget == null) {
                        AnnotationLog.println("DASOInstanceGenerator: [ERROR] Could not complete path for " + toUri, fileName);
                        return;
                    }
                    AnnotationLog.println("DASOInstanceGenerator:       " + nextTarget.getUri(), fileName);
                    socs.add(nextTarget);
                }
                socPaths.put(key,socs);
            } 
        }
    }
    
    private void computeLabelsForTargetSOCs() {
        /* 
         *  (9/9) COMPUTE LABEL for each TARGET SOC
         */

        AnnotationLog.println("DASOInstanceGenerator: (9/9) ======== COMPUTE SOC LABELS ========", fileName);
        for (Map.Entry<String, ObjectCollection> entry : requiredSocs.entrySet()) {
            ObjectCollection soc = entry.getValue();
            String fullLabel = "";
            boolean process = true;

            AnnotationLog.println("DASOInstanceGenerator: START: " + soc.getUri(), fileName);
            String label = soc.getGroundingLabel();
            if (label == null) {
                label = "";
            }
            if (soc.getHasScope() == null || !label.equals("")) {
                fullLabel = label;
                AnnotationLog.println("DASOInstanceGenerator: Computed label [" + fullLabel + "]", fileName);
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
                        AnnotationLog.println("DASOInstanceGenerator: [ERROR] Could not complete path for " + toUri, fileName);
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
                AnnotationLog.println("DASOInstanceGenerator: Computed label [" + fullLabel + "]", fileName);       
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
        AnnotationLog.println("DASOInstanceGenerator: DASO: " + daso.getUri() + "   From : " + daso.getLabel() + "  To: " + toUri, fileName);

        //  LOAD each TARGET DASO into DASOs, if TARGET DASO is not loaded yet
        if (toUri != null && !toUri.equals("") && !dasos.containsKey(toUri)) {
            AnnotationLog.println("DASOInstanceGenerator: Loading " + toUri, fileName);
            DataAcquisitionSchemaObject newDaso = DataAcquisitionSchemaObject.find(toUri);
            if (newDaso == null) {
                AnnotationLog.println("DASOInstanceGenerator: [ERROR] Could not find DASO with following URI : " + toUri, fileName);
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
            //AnnotationLog.println("socFromTargetDaso : " + targetObj.getLabel() + "    soc's getSOCReference " + soc.getSOCReference(), fileName); 
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
                    AnnotationLog.println("DASOInstanceGenerator: Reference: " + daso.getLabel() + "  Associated SOC : " + associatedSOC + "    with hasScope: " + associatedSOC.getHasScopeUri(), fileName);
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
                    AnnotationLog.println("DASOInstanceGenerator:       [WARNING] SOC association ignored for " + daso.getUri(), fileName);
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
            newSoc.setNamedGraph(oasUri);
            newSoc.saveToTripleStore();
            addObject(newSoc);
            
            if (!requiredSocs.containsKey(newSoc.getUri())) {
                requiredSocs.put(newSoc.getUri(), newSoc);
                socsList.add(newSoc);
            }
            AnnotationLog.println("DASOInstanceGenerator: Reference: " + daso.getLabel() + "   Created SOC : " + newSOCUri + "    with hasScope: " + scopeUri, fileName);
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

    public Map<String, Map<String, String>> generateRowInstances(String id) {
        /* Returns : First String : DASO's Label
         *           Object URI   : The actual URI of the object that was retrieved/created for the identifier in CSV Record
         */

        if (id == null || id.equals("")) {
            System.out.println("DASOInstanceGenerator: no identifier provided. See if your SDD contains an identifier," + 
                    " and if the corresponding label in ths file is a valid identifier.");
            return null;
        }
        //System.out.println("DASOInstanceGenerator: generate row instances for : " + id);

        Map<String, Map<String,String>> objMapList = new HashMap<String, Map<String,String>>();

        /*
         *   TRAVERSE list of objects for current record
         */

        for (Map.Entry<String, List<ObjectCollection>> entry : socPaths.entrySet()) {
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
                    nextObjUri = createStudyObject(nextSoc, currentObjUri);
                }

                if (nextObjUri == null || nextObjUri.equals("")) {
                    //System.out.println("DASOInstanceGenerator:          [ERROR] Path generation stopped. Error ocurred retrieving/creating objects in path. See log above.");
                    currentSoc = nextSoc;
                    currentObjUri = nextObjUri;
                    break;
                }

                //System.out.println("DASOInstanceGenerator:          Scope Obj URI=[" + currentObjUri + "]  SOC=[" + nextSoc.getUri() + 
                //		   "]  =>  Obj Uri=[" + nextObjUri + "]");

                currentSoc = nextSoc;
                currentObjUri = nextObjUri;
            }

            StudyObject obj = StudyObject.find(currentObjUri);
            if (obj != null) { 
                List<String> objTimes = StudyObject.retrieveTimeScopeUris(currentObjUri);
                Map<String,String> referenceEntry = new HashMap<String,String>();
                referenceEntry.put(StudyObject.STUDY_OBJECT_URI, currentObjUri);
                referenceEntry.put(StudyObject.STUDY_OBJECT_TYPE, obj.getTypeUri());
                referenceEntry.put(StudyObject.SOC_TYPE, currentSoc.getTypeUri());
                referenceEntry.put(StudyObject.SOC_LABEL, socLabels.get(currentSoc.getSOCReference()));
                referenceEntry.put(StudyObject.OBJECT_SCOPE_URI, id);
                referenceEntry.put(StudyObject.OBJECT_ORIGINAL_ID, obj.getOriginalId());
                if (objTimes != null && objTimes.size() > 0) {
                    referenceEntry.put(StudyObject.OBJECT_TIME, objTimes.get(0));
                }

                objMapList.put(currentSoc.getSOCReference(), referenceEntry);
            }

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

    private String createStudyObject(ObjectCollection nextSoc, String currentObjUri) {
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
        newObj.setNamedGraph(oasUri);
        newObj.saveToTripleStore();
        addObject(newObj);
   
        //System.out.println("DASOInstanceGenerator:          Created Obj with URI=[" + newUri + "]   Type=[" + newTypeUri + "]");
        
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

    /* **************************************************************************************
     *                                                                                      *
     *  RETRIEVE URI, ORIGINAL ID,  AND TYPE OF GROUNDING OBJECT FROM CURRENT OBJECT URI    *
     *                                                                                      *
     ****************************************************************************************/

    public Map<String, String> retrieveGroundObject(String id){
        /* Returns : First String : DASO's Label
         *           Object URI   : The actual URI of the object that was retrieved/created for the identifier in CSV Record
         */

        if (id == null || id.equals("")) {
            System.out.println("DASOInstanceGenerator: no identifier provided. See if your SDD contains an identifier," + 
                    " and if the corresponding label in ths file is a valid identifier.");
            return null;
        }
        //System.out.println("DASOInstanceGenerator: retrieve ground object for : " + id);
        //System.out.println("DASOInstanceGenerator: groundingPath : " + groundingPath);

        ObjectCollection currentSoc = mainSoc;
        StudyObject obj = null;
        Map<String,String> groundObj = new HashMap<String,String>();

        //System.out.println("DASOInstanceGenerator:     PATH >>> ");

        // Lookup first study object
        String currentObjUri = StudyObject.findUriBySocAndOriginalId(currentSoc.getUri(), id); 

        if (groundingPath == null || groundingPath.size() <= 0) {
            obj = StudyObject.find(currentObjUri);
            if (obj != null) {
                groundObj.put(StudyObject.STUDY_OBJECT_URI, obj.getUri());
                groundObj.put(StudyObject.STUDY_OBJECT_TYPE, obj.getTypeUri());
                groundObj.put(StudyObject.SUBJECT_ID, obj.getOriginalId());
                return groundObj;
            } 
            return null;
        }

        //System.out.println("DASOInstanceGenerator:          Obj Original ID=[" + id + "]   SOC=[" + currentSoc.getUri() + "] =>  Obj URI=[" + currentObjUri + "]");

        for (ObjectCollection nextSoc : groundingPath) {
            //System.out.println("            " + nextSoc.getUri() + "  ");
            String nextObjUri = StudyObject.findUriBySocAndObjectScopeUri(currentSoc.getUri(), currentObjUri); 
            if (nextObjUri == null || nextObjUri.equals("")) {
                //System.out.println("DASOInstanceGenerator:          [ERROR] Path generation stopped. Error ocurred retrieving/creating objects in path. See log above.");
                currentSoc = nextSoc;
                currentObjUri = nextObjUri;
                break;
            }

            //System.out.println("DASOInstanceGenerator:          Scope Obj URI=[" + currentObjUri + "]  SOC=[" + nextSoc.getUri() + 
            //		   "]  =>  Obj Uri=[" + nextObjUri + "]");

            currentSoc = nextSoc;
            currentObjUri = nextObjUri;
        }

        obj = StudyObject.find(currentObjUri);
        groundObj.put(StudyObject.STUDY_OBJECT_URI, obj.getUri());
        groundObj.put(StudyObject.STUDY_OBJECT_TYPE, obj.getTypeUri());
        groundObj.put(StudyObject.SUBJECT_ID, obj.getOriginalId());
        return groundObj;

    }// /retrieveGroundObject
    
}// /class
