package org.hadatac.data.loader;

import java.time.Instant;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.apache.commons.text.WordUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;


public class MeasurementGenerator extends BaseGenerator {

    private ObjectAccessSpec da;
    private DataFile dataFile;

    private DataAcquisitionSchema schema = null;
    private Map<String, DataAcquisitionSchemaObject> mapSchemaObjects = new HashMap<String, DataAcquisitionSchemaObject>();
    private Map<String, DataAcquisitionSchemaObject> mapSchemaEvents = new HashMap<String, DataAcquisitionSchemaObject>();

    // ASSIGN positions for MetaDASAs
    private int posTimestamp = -1;
    private int posTimeInstant = -1;
    private int posNamedTime = -1;
    private int posId = -1;
    private int posOriginalId = -1;
    private int posEntity = -1;
    private int posUnit = -1;
    private int posInRelation = -1;
    private int posLOD = -1;

    private int totalCount = 0;

    private Map<String, Map<String, String>> possibleValues = null;
    private Map<String, String> urisByLabels = null;
    //private Map<String, Map<String, String>> mapIDStudyObjects = null;

    private String dasoUnitUri = "";

    //private List<DASVirtualObject> templateList = new ArrayList<DASVirtualObject>();
    private DASOInstanceGenerator dasoiGen = null; 

    public MeasurementGenerator(RecordFile file, ObjectAccessSpec da, 
            DataAcquisitionSchema schema, DataFile dataFile, DASOInstanceGenerator dasoiGen) {
        super(file);
        this.da = da;
        this.schema = schema;
        this.dataFile = dataFile;
        String fileName = dataFile.getFileName();
        this.dasoiGen = dasoiGen;
        if (dasoiGen.initiateCache(da.getStudyUri())) {
        	setStudyUri(da.getStudyUri());
            urisByLabels = DataAcquisitionSchema.findAllUrisByLabel(schema.getUri());
        } else {
            AnnotationLog log = AnnotationLog.create(dataFile.getFileName());
            log.printException("[ERROR] MeasurementGeneration: failed to initialize the data ingestion.", fileName);;
        }
    }

    /*
    private void createVirtualObjectCollections(DataAcquisitionSchema schema) {
        GeneratorChain chain = new GeneratorChain();
        GeneralGenerator generator = new GeneralGenerator(file, "Virtual Object Collections");

        List<String> socRefs = ObjectCollection.findAll().stream()
                .map(x -> x.getSOCReference()).collect(Collectors.toList());
        for (DataAcquisitionSchemaObject daso : schema.getObjects()) {

            // TO DO: for the following if clause, use the correct criteria for 
            // adding new SOC
            if (!socRefs.contains(daso.getAlternativeName())
                    && !daso.getWasDerivedFrom().isEmpty()) {
                Map<String, Object> row = new HashMap<String, Object>();
                row.put("hasURI", ConfigProp.getKbPrefix() + "SOC-" 
                        + URIUtils.getBaseName(getStudyUri()).replace("STD-", "") + "-" 
                        + daso.getAlternativeName().replace("??", "").toUpperCase());
                row.put("hasco:isMemberOf", URIUtils.replaceNameSpaceEx(getStudyUri()));

                String label = WordUtils.capitalize(daso.getAlternativeName().replace("??", ""));
                row.put("hasco:hasGroundingLabel", label);
                row.put("hasco:hasRoleLabel", label);
                row.put("rdfs:comment", label);
                row.put("rdfs:label", label);
                row.put("hasco:hasSOCReference", daso.getAlternativeName());

                // TO DO: add type and hasScope property for SOC

                generator.addRow(row);
            }
        }
        chain.addGenerator(generator);
        chain.generate();
    }*/

    @Override
    public void preprocess() throws Exception {
        System.out.println("[Parser] indexMeasurements()...");

        AnnotationLog log = AnnotationLog.create(dataFile.getFileName());

        // createVirtualObjectCollections(schema);

        /*
        if(!AnnotationWorker.templateLibrary.containsKey(da.getSchemaUri())){
            System.out.println("[Parser] [WARN] no DASVirtualObject templates for this DataAcquisition. Is this correct?");
            System.out.println("[Parser] Could not retrieve template list for " + da.getSchemaUri());
            System.out.println("[Parser] templateLibrary contains keys ");
            for(String k : AnnotationWorker.templateLibrary.keySet()){
                System.out.println("\t" + k);
            }
        } else {
            templateList = AnnotationWorker.templateLibrary.get(da.getSchemaUri());
            System.out.println("[Parser] Found the right template list for " + da.getSchemaUri());
            for(DASVirtualObject item : templateList){
                System.out.println(item);
            }
        }
         */

        // ASSIGN values for tempPositionInt
        List<String> unknownHeaders = schema.defineTemporaryPositions(file.getHeaders());
        if (!unknownHeaders.isEmpty()) {
            log.addline(Feedback.println(Feedback.WEB, 
                    "[WARNING] Failed to match the following " 
                            + unknownHeaders.size() + " headers: " + unknownHeaders));
        }

        if (!schema.getTimestampLabel().equals("")) {
            posTimestamp = schema.tempPositionOfLabel(schema.getTimestampLabel());
            System.out.println("posTimestamp: " + posTimestamp);
        }
        if (!schema.getTimeInstantLabel().equals("")) {
            posTimeInstant = schema.tempPositionOfLabel(schema.getTimeInstantLabel());
            System.out.println("posTimeInstant: " + posTimeInstant);
        }
        if (!schema.getNamedTimeLabel().equals("")) {
            posNamedTime = schema.tempPositionOfLabel(schema.getNamedTimeLabel());
            System.out.println("posNamedTime: " + posNamedTime);
        }
        if (!schema.getIdLabel().equals("")) {
            posId = schema.tempPositionOfLabel(schema.getIdLabel());
            System.out.println("posId: " + posId);
        }
        if (!schema.getOriginalIdLabel().equals("")) {
            posOriginalId = schema.tempPositionOfLabel(schema.getOriginalIdLabel());
            System.out.println("posOriginalId: " + posOriginalId);
        }
        if (!schema.getEntityLabel().equals("")) {
            posEntity = schema.tempPositionOfLabel(schema.getEntityLabel());
            System.out.println("posEntity: " + posEntity);
        }
        if (!schema.getUnitLabel().equals("")) {
            posUnit = schema.tempPositionOfLabel(schema.getUnitLabel());
            System.out.println("posUnit: " + posUnit);
        }
        if (!schema.getInRelationToLabel().equals("")) {
            posInRelation = schema.tempPositionOfLabel(schema.getInRelationToLabel());
            System.out.println("posInRelation: " + posInRelation);
        }
        if (!schema.getLODLabel().equals("")) {
            posLOD = schema.tempPositionOfLabel(schema.getLODLabel());
            System.out.println("posLOD: " + posLOD);
        }

        // Store necessary information before hand to avoid frequent SPARQL queries
        possibleValues = DataAcquisitionSchema.findPossibleValues(da.getSchemaUri());
        urisByLabels = DataAcquisitionSchema.findAllUrisByLabel(da.getSchemaUri());
        //mapIDStudyObjects = StudyObject.findIdUriMappings(da.getStudyUri());
        dasoUnitUri = urisByLabels.get(schema.getUnitLabel());

        //System.out.println("possibleValues: " + possibleValues);
        
        // Comment out row instance generation
        // Map<String, DASOInstance> rowInstances = new HashMap<String, DASOInstance>();
    }

    @Override
    public HADatAcThing createObject(Record record, int rowNumber) throws Exception {
        System.out.println("rowNumber: " + rowNumber);
        
        // Comment out row instance generation
        /*
        try{
            // complete DASOInstances for the row FIRST
            // so we can refer to these URI's when setting the entity and/or object
            rowInstances.clear();
            rowInstances = dasoiGen.generateRowInstances(record);
        } catch(Exception e){
            System.out.println("[Parser] [ERROR]:");
            e.printStackTrace(System.out);
        }
        // rowInstances keys *should* match what is in DASchemaAttribute table's "attributeOf" field!
        for(Map.Entry instance : rowInstances.entrySet()) {
            System.out.println("[Parser] Made an instance for " + instance.getKey() + " :\n\t" + instance.getValue());
        }
         */

        Map<String, Map<String,String>> objList = null;
        Map<String,String> groundObj = null;
        if (da.hasCellScope()) {
            // Objects defined by Cell Scope
            //if (da.getCellScopeName().get(0).equals("*")) {
            //	measurement.setStudyObjectUri(URIUtils.replacePrefixEx(da.getCellScopeUri().get(0).trim()));
            //measurement.setObjectUri(URIUtils.replacePrefixEx(da.getCellScopeUri().get(0).trim()));
            //measurement.setObjectCollectionType(URIUtils.replacePrefixEx("hasco:SampleCollection"));
            //} else {
            // TO DO: implement rest of cell scope
            //}
        } else {
            // Objects defined by Row Scope
            String id = "";
            if (!schema.getOriginalIdLabel().equals("")) {
                id = record.getValueByColumnIndex(posOriginalId);
            } else if (!schema.getIdLabel().equals("")) {
                id = record.getValueByColumnIndex(posId);
            }
            objList = dasoiGen.generateRowInstances(id);
            groundObj = dasoiGen.retrieveGroundObject(id);
        }

        Iterator<DataAcquisitionSchemaAttribute> iterAttributes = schema.getAttributes().iterator();
        while (iterAttributes.hasNext()) {
            DataAcquisitionSchemaAttribute dasa = iterAttributes.next();

            if (!dasa.getPartOfSchema().equals(schema.getUri())){
                continue;
            }
            if (dasa.getLabel().equals(schema.getTimestampLabel())) {
                continue;
            }
            if (dasa.getLabel().equals(schema.getTimeInstantLabel())) {
                continue;
            }
            if (dasa.getLabel().equals(schema.getNamedTimeLabel())) {
                continue;
            }
            if (dasa.getLabel().equals(schema.getIdLabel())) {
                continue;
            }
            if (dasa.getLabel().equals(schema.getOriginalIdLabel())) {
                continue;
            }
            if (dasa.getLabel().equals(schema.getEntityLabel())) {
                continue;
            }
            if (dasa.getLabel().equals(schema.getUnitLabel())) {
                continue;
            }
            if (dasa.getLabel().equals(schema.getInRelationToLabel())) {
                continue;
            }
            if (dasa.getLabel().equals(schema.getLODLabel())) {
                continue;
            }

            Measurement measurement = new Measurement();

            /*===================*
             *                   *
             *   SET VALUE       *
             *                   *
             *===================*/

            if (dasa.getTempPositionInt() < 0 || dasa.getTempPositionInt() >= record.size()) {
                continue;
            } else if (record.getValueByColumnIndex(dasa.getTempPositionInt()).isEmpty()) { 
                continue;
            } else {
                String originalValue = record.getValueByColumnIndex(dasa.getTempPositionInt());
                String dasa_uri_temp = dasa.getUri();
                measurement.setOriginalValue(originalValue);
                if (possibleValues.containsKey(dasa_uri_temp)) {
                    if (possibleValues.get(dasa_uri_temp).containsKey(originalValue.toLowerCase())) {
                        measurement.setValue(possibleValues.get(dasa_uri_temp).get(originalValue.toLowerCase()));
                    } else {
                        measurement.setValue(originalValue);
                    }
                } else {
                    measurement.setValue(originalValue);
                }
            }

            /*========================*
             *                        *
             * SET LEVEL OF DETECTION *
             *                        *
             *========================*/
            measurement.setLevelOfDetection("");
            if (!schema.getLODLabel().equals("") && posLOD >= 0) {
                measurement.setLevelOfDetection(record.getValueByColumnIndex(posLOD));
            }

            /*============================*
             *                            *
             *   SET TIME(STAMP)          *
             *                            *
             *============================*/

            /*
              - TimestampLabel is used for machine generated timestamp
              - TimeInstantLabel is used for timestamps told to system to be timestamp, but that are not further processed
              - Abstract times are encoded as DASA's events, and are supposed to be strings
             */
            measurement.setTimestamp(new Date(0));
            measurement.setAbstractTime("");

            if(dasa.getLabel() == schema.getTimestampLabel()) {
                // full-row regular (Epoch) timemestamp
                String sTime = record.getValueByColumnIndex(posTimestamp);
                //System.out.println("Timestamp received: " + sTime);
                int timeStamp = new BigDecimal(sTime).intValue();
                //System.out.println("Tmestamp recorded: " + Instant.ofEpochSecond(timeStamp).toString());
                measurement.setTimestamp(Instant.ofEpochSecond(timeStamp).toString());
            } else if (!schema.getTimeInstantLabel().equals("")) {
                // full-row regular (XSD) time interval
                String timeValue = record.getValueByColumnIndex(posTimeInstant);
                //System.out.println("Timestamp received: " + timeValue);
                if (timeValue != null) {
                    try {
                        measurement.setTimestamp(timeValue);
                    } catch (Exception e) {
                        System.out.println("Setting current time!");
                        measurement.setTimestamp(new Date(0).toInstant().toString());
                    }
                }
            } else if (!schema.getNamedTimeLabel().equals("")) {
                // full-row named time
                String timeValue = record.getValueByColumnIndex(posNamedTime);
                if (timeValue != null) {
                    measurement.setAbstractTime(timeValue);
                } else {
                    measurement.setAbstractTime("");
                }
            } else if (dasa.getEventUri() != null && !dasa.getEventUri().equals("")) {
                //DataAcquisitionSchemaEvent dase = null;
                DataAcquisitionSchemaObject dase = null;
                String daseUri = dasa.getEventUri();
                if (mapSchemaEvents.containsKey(daseUri)) {
                    dase = mapSchemaEvents.get(daseUri);
                } else {
                    dase = schema.getEvent(daseUri);
                    if (dase != null) {
                        mapSchemaEvents.put(daseUri, dase);
                    }
                }
                if (dase != null) {
                    if (!dase.getEntity().equals("")) {
                        measurement.setAbstractTime(dase.getEntity());
                    } else {
                        measurement.setAbstractTime(dase.getUri());
                    }
                }
            }

            /*===================================*
             *                                   *
             *   SET STUDY                       *
             *                                   *
             *===================================*/
            measurement.setStudyUri(da.getStudyUri());

            /*===================================*
             *                                   *
             *   SET OBJECT ID, PID, SID, ROLE   *
             *                                   *
             *===================================*/
            measurement.setObjectCollectionType("");
            measurement.setStudyObjectUri("");
            measurement.setStudyObjectTypeUri("");
            measurement.setObjectUri("");
            measurement.setPID("");
            measurement.setSID("");
            measurement.setRole("");
            measurement.setEntityUri("");

            if (da.hasCellScope()) {
                System.out.println("da.hasCellScope() ===============");

                // Objects defined by Cell Scope
                if (da.getCellScopeName().get(0).equals("*")) {
                    measurement.setStudyObjectUri(URIUtils.replacePrefixEx(da.getCellScopeUri().get(0).trim()));
                    measurement.setObjectUri(URIUtils.replacePrefixEx(da.getCellScopeUri().get(0).trim()));
                    measurement.setObjectCollectionType(URIUtils.replacePrefixEx("hasco:SampleCollection"));
                    //System.out.println("Measurement: ObjectURI (before replace): <" + da.getCellScopeUri().get(0).trim() + ">");
                    //System.out.println("Measurement: ObjectURI (after replace): <" + URIUtils.replacePrefixEx(da.getCellScopeUri().get(0).trim()) + ">");
                } else {
                    // TO DO: implement rest of cell scope
                }
            } else {
                // Objects defined by Row Scope
                String id = "";
                if (!schema.getOriginalIdLabel().equals("")) {
                    id = record.getValueByColumnIndex(posOriginalId);
                } else if (!schema.getIdLabel().equals("")) {
                    id = record.getValueByColumnIndex(posId);
                }

                if (!"".equals(id)) {
                    String reference = dasa.getObjectViewLabel();
                    if (reference != null && !reference.equals("")) {
                        if (objList.get(reference) == null) {
                            System.out.println("MeasurementGenerator: [ERROR] Processing objList for reference [" + reference + "]");
                        } else {
                            // from object list
                            measurement.setObjectUri(objList.get(reference).get(StudyObject.STUDY_OBJECT_URI));
                            measurement.setObjectCollectionType(objList.get(reference).get(StudyObject.SOC_TYPE));
                            measurement.setRole(objList.get(reference).get(StudyObject.SOC_LABEL));
                            if (objList.get(reference).get(StudyObject.STUDY_OBJECT_TYPE) != null && !objList.get(reference).get(StudyObject.STUDY_OBJECT_TYPE).equals("")) {
                                measurement.setEntityUri(objList.get(reference).get(StudyObject.STUDY_OBJECT_TYPE));
                            }
                            if (objList.get(reference).get(StudyObject.SOC_TYPE).equals(ObjectCollection.SAMPLE_COLLECTION)) {
                                measurement.setSID(objList.get(reference).get(StudyObject.OBJECT_ORIGINAL_ID));
                            }
                            if (objList.get(reference).get(StudyObject.OBJECT_TIME) != null && !objList.get(reference).get(StudyObject.OBJECT_TIME).equals("")) {
                                measurement.setAbstractTime(objList.get(reference).get(StudyObject.OBJECT_TIME));
                            }

                            // from ground object
                            if (groundObj == null || groundObj.get(StudyObject.STUDY_OBJECT_URI) == null || groundObj.get(StudyObject.STUDY_OBJECT_URI).equals("")) {
                                System.out.println("MeasurementGenerator: [ERROR] Could not retrieve Ground Object for reference [" + reference + "]");
                            } else {
                                measurement.setStudyObjectUri(groundObj.get(StudyObject.STUDY_OBJECT_URI));
                                measurement.setStudyObjectTypeUri(groundObj.get(StudyObject.STUDY_OBJECT_TYPE));
                                measurement.setPID(groundObj.get(StudyObject.SUBJECT_ID));
                            }
                        }
                        //System.out.println("[MeasurementGenerator] For Id=[" + id + "] and reference=[" + reference + "] it was assigned Obj URI=[" + measurement.getObjectUri() + "]");
                    } else {
                        System.out.println("MeasurementGenerator: [ERROR]: could not find DASA reference for ID=[" + id + "]");
                    }
                    
                    /*
                    //if (dasa.getEntity().equals(URIUtils.replacePrefixEx("sio:Human"))) {
                        //measurement.setObjectCollectionType(URIUtils.replacePrefixEx("hasco:SubjectGroup"));
                        //if (mapIDStudyObjects.containsKey(id)) {
                            //measurement.setStudyObjectUri(mapIDStudyObjects.get(id).get(StudyObject.STUDY_OBJECT_URI));
                            //measurement.setStudyObjectTypeUri(mapIDStudyObjects.get(id).get(StudyObject.STUDY_OBJECT_TYPE));
                            //measurement.setObjectUri(mapIDStudyObjects.get(id).get(StudyObject.STUDY_OBJECT_URI));
                            //measurement.setObjectCollectionType(mapIDStudyObjects.get(id).get(StudyObject.SOC_TYPE));
                        //}
                        //measurement.setPID(id);
                    //} else {
                        //if (mapIDStudyObjects.containsKey(id)) {
                            // test if object is in the scope of another object
                            //if (!mapIDStudyObjects.get(id).get(StudyObject.OBJECT_SCOPE_URI).isEmpty()) {
                                //measurement.setStudyObjectUri(mapIDStudyObjects.get(id).get(StudyObject.STUDY_OBJECT_URI));
                                //measurement.setStudyObjectTypeUri(mapIDStudyObjects.get(id).get(StudyObject.STUDY_OBJECT_TYPE));
                                //measurement.setObjectUri(mapIDStudyObjects.get(id).get(StudyObject.OBJECT_SCOPE_URI));
                                //measurement.setPID(mapIDStudyObjects.get(id).get(StudyObject.OBJECT_SCOPE_URI));
                                // test to see if object is member of a sample collection
                                //if (URIUtils.replacePrefixEx("hasco:SampleCollection").equals(mapIDStudyObjects.get(id).get(StudyObject.SOC_TYPE))) {
                                //measurement.setSID(mapIDStudyObjects.get(id).get(StudyObject.SUBJECT_ID));
                                //}
                                //measurement.setObjectCollectionType(mapIDStudyObjects.get(id).get(StudyObject.SOC_TYPE));
                                //measurement.setObjectCollectionType(URIUtils.replacePrefixEx("hasco:SampleCollection"));
                            //} 
                            // assumes that the object is a subject if object is not in the scope of another object
                            //else {
                                // Subject
                                //measurement.setStudyObjectUri(mapIDStudyObjects.get(id).get(StudyObject.STUDY_OBJECT_URI));
                                //measurement.setStudyObjectTypeUri(mapIDStudyObjects.get(id).get(StudyObject.STUDY_OBJECT_TYPE));
                                //measurement.setObjectUri(mapIDStudyObjects.get(id).get(StudyObject.STUDY_OBJECT_URI));
                                //measurement.setPID(mapIDStudyObjects.get(id).get(StudyObject.SUBJECT_ID));
                                //measurement.setObjectCollectionType(mapIDStudyObjects.get(id).get(StudyObject.SOC_TYPE));
                                //measurement.setObjectCollectionType(URIUtils.replacePrefixEx("hasco:SubjectGroup"));
                            //}
                        //}
                    //}
                     */

                }
            }

            /*=============================*
             *                             *
             *   SET URI, OWNER AND DA URI *
             *                             *
             *=============================*/

            measurement.setUri(URIUtils.replacePrefixEx(measurement.getStudyUri()) + "/" + 
                    URIUtils.replaceNameSpaceEx(da.getUri()).split(":")[1] + "/" +
                    dasa.getLabel() + "/" + 
                    dataFile.getFileName() + "-" + totalCount++);
            measurement.setOwnerUri(da.getOwnerUri());
            measurement.setAcquisitionUri(da.getUri());

            /*======================================*
             *                                      *
             *   SET ENTITY AND CHARACTERISTIC URI  *              *
             *                                      *
             *======================================*/
            measurement.setDasoUri(dasa.getObjectUri());
            measurement.setDasaUri(dasa.getUri());

            DataAcquisitionSchemaObject daso = null;
            String dasoUri = dasa.getObjectUri();
            if (mapSchemaObjects.containsKey(dasoUri)) {
                daso = mapSchemaObjects.get(dasoUri);
            } else {
                daso = schema.getObject(dasoUri);
                mapSchemaObjects.put(dasoUri, daso);
            }

            if (measurement.getEntityUri().equals("")) {
                if (null != daso) {
                    if (daso.getTempPositionInt() > 0) {
                        // values of daso exist in the columns
                        String dasoValue = record.getValueByColumnIndex(daso.getTempPositionInt());
                        if (possibleValues.containsKey(dasa.getObjectUri())) {
                            if (possibleValues.get(dasa.getObjectUri()).containsKey(dasoValue.toLowerCase())) {
                                measurement.setEntityUri(possibleValues.get(dasa.getObjectUri()).get(dasoValue.toLowerCase()));
                            } else {
                                measurement.setEntityUri(dasoValue);
                            }
                        } else {
                            measurement.setEntityUri(dasoValue);
                        }
                    } else {
                        measurement.setEntityUri(daso.getEntity());
                    }
                } else {
                    measurement.setEntityUri(dasa.getObjectUri());
                }
            }

            measurement.setCharacteristicUris(Arrays.asList(dasa.getReversedAttributeString()));

            /*======================================*
             *                                      *
             *   SET IN RELATION TO URI             *
             *                                      *
             *======================================*/
            measurement.setInRelationToUri("");

            DataAcquisitionSchemaObject inRelationToDaso = null;
            String inRelationToUri = dasa.getInRelationToUri(URIUtils.replacePrefixEx("sio:inRelationTo"));
            if (mapSchemaObjects.containsKey(inRelationToUri)) {
                inRelationToDaso = mapSchemaObjects.get(inRelationToUri);
            } else {
                inRelationToDaso = schema.getObject(inRelationToUri);
                mapSchemaObjects.put(inRelationToUri, inRelationToDaso);
            }

            if (null != inRelationToDaso) {
                if (inRelationToDaso.getTempPositionInt() > 0) {
                    String inRelationToDasoValue = record.getValueByColumnIndex(inRelationToDaso.getTempPositionInt());
                    if (possibleValues.containsKey(inRelationToUri)) {
                        if (possibleValues.get(inRelationToUri).containsKey(inRelationToDasoValue.toLowerCase())) {
                            measurement.setInRelationToUri(possibleValues.get(inRelationToUri).get(inRelationToDasoValue.toLowerCase()));
                        }
                    }
                } else {
                    // Assign the entity of inRelationToDaso to inRelationToUri
                    measurement.setInRelationToUri(inRelationToDaso.getEntity());
                }
            }

            /*=============================*
             *                             *
             *   SET UNIT                  *
             *                             *
             *=============================*/

            if (!schema.getUnitLabel().equals("") && posUnit >= 0) {
                // unit exists in the columns
                String unitValue = record.getValueByColumnIndex(posUnit);
                if (unitValue != null) {
                    if (possibleValues.containsKey(dasoUnitUri)) {
                        if (possibleValues.get(dasoUnitUri).containsKey(unitValue.toLowerCase())) {
                            measurement.setUnitUri(possibleValues.get(dasoUnitUri).get(unitValue.toLowerCase()));
                        } else {
                            measurement.setUnitUri(unitValue);
                        }
                    } else {
                        measurement.setUnitUri(unitValue);
                    }
                }
            } else {
                measurement.setUnitUri("");
            }

            if (measurement.getUnitUri().equals("") && !dasa.getUnit().equals("")) {
                // Assign units from the Unit column of SDD
                measurement.setUnitUri(dasa.getUnit());
            }

            /*=================================*
             *                                 *
             *   SET DATASET                   *
             *                                 *
             *=================================*/
            measurement.setDatasetUri(dataFile.getDatasetUri());

            objects.add(measurement);
        }

        return null;
    }

    @Override
    public boolean commitObjectsToSolr(List<HADatAcThing> objects) throws Exception {
        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();

        int count = 0;
        int batchSize = 10000;

        for (HADatAcThing measurement : objects) {
            try {
                solr.addBean(measurement);
            } catch (IOException | SolrServerException e) {
                System.out.println("[ERROR] SolrClient.addBean - e.Message: " + e.getMessage());
            }

            // INTERMEDIARY COMMIT
            if((++count) % batchSize == 0) {
                commitToSolr(solr, batchSize);
            }
        }

        // FINAL COMMIT
        commitToSolr(solr, count % batchSize);

        da.addNumberDataPoints(totalCount);
        da.saveToSolr();

        AnnotationLog log = AnnotationLog.create(dataFile.getFileName());
        log.addline(Feedback.println(Feedback.WEB, String.format(
                "[OK] %d object(s) have been committed to solr", count)));

        return true;
    }

    private void commitToSolr(SolrClient solr, int batch_size) throws Exception {
        AnnotationLog log = AnnotationLog.create(dataFile.getFileName());

        try {
            System.out.println("solr.commit()...");
            solr.commit();
            System.out.println(String.format("[OK] Committed %s measurements!", batch_size));
            log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Committed %s measurements!", batch_size)));
        } catch (IOException | SolrServerException e) {
            System.out.println("[ERROR] SolrClient.commit - e.Message: " + e.getMessage());
            try {
                solr.close();
            } catch (IOException e1) {
                System.out.println("[ERROR] SolrClient.close - e.Message: " + e1.getMessage());
            }

            throw new Exception("Fail to commit to solr");
        }
    }

    @Override
    public String getTableName() {
        return "";
    }
}
