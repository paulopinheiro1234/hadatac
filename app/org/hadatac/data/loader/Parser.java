package org.hadatac.data.loader;

import java.io.IOException;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DASVirtualObject;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.Feedback;

import com.typesafe.config.ConfigFactory;

public class Parser {

    private DataAcquisitionSchema schema = null;
    private List<DASVirtualObject> templateList = null;

    public Parser() {
        templateList = new ArrayList<DASVirtualObject>();
    }

    public ParsingResult indexMeasurements(RecordFile file, DataAcquisition da, DataFile dataFile) {
        System.out.println("[Parser] indexMeasurements()...");

        AnnotationLog log = AnnotationLog.create(dataFile.getFileName());

        schema = DataAcquisitionSchema.find(da.getSchemaUri());
        if (schema == null) {
            log.addline(Feedback.println(Feedback.WEB, "[ERROR] " + da.getSchemaUri() + " cannot be found!"));
            return new ParsingResult(1, log.getLog());
        }

        Map<String, DataAcquisitionSchemaObject> mapSchemaObjects = new HashMap<String, DataAcquisitionSchemaObject>();
        Map<String, DataAcquisitionSchemaEvent> mapSchemaEvents = new HashMap<String, DataAcquisitionSchemaEvent>();
        
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

        int total_count = 0;
        int batch_size = 10000;

        SolrClient solr = new HttpSolrClient.Builder(
                ConfigFactory.load().getString("hadatac.solr.data") 
                + CollectionUtil.DATA_ACQUISITION).build();

        // ASSIGN values for tempPositionInt
        List<String> unknownHeaders = schema.defineTemporaryPositions(file.getHeaders());
        if (!unknownHeaders.isEmpty()) {
            log.addline(Feedback.println(Feedback.WEB, 
                    "[WARNING] Failed to match the following " + unknownHeaders.size() + " headers: " + unknownHeaders));
        }

        // ASSIGN positions for MetaDASAs
        int posTimestamp = -1;
        int posTimeInstant = -1;
        int posNamedTime = -1;
        int posId = -1;
        int posOriginalId = -1;
        int posEntity = -1;
        int posUnit = -1;
        int posInRelation = -1;
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

        // Store possible values before hand to avoid frequent SPARQL queries
        Map<String, Map<String, String>> possibleValues = DataAcquisitionSchema.findPossibleValues(da.getSchemaUri());
        Map<String, List<String>> mapIDStudyObjects = DataAcquisitionSchema.findIdUriMappings(da.getStudyUri());
        String dasoUnitUri = DataAcquisitionSchema.findByLabel(da.getSchemaUri(), schema.getUnitLabel());

        //System.out.println("possibleValues: " + possibleValues);

        // Comment out row instance generation
        /*
		// Need to be fixed here by getting codeMap and codebook from sparql query
		DASOInstanceGenerator dasoiGen = new DASOInstanceGenerator(da.getStudy().getUri(), 
				templateList, AnnotationWorker.codeMappings, AnnotationWorker.codebook_K);
		Map<String, DASOInstance> rowInstances = new HashMap<String,DASOInstance>();
         */

        for (Record record : file.getRecords()) {
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
                    int timeStamp = new BigDecimal(sTime).intValue();
                    measurement.setTimestamp(Instant.ofEpochSecond(timeStamp).toString());
                } else if (!schema.getTimeInstantLabel().equals("")) {
                    // full-row regular (XSD) time interval
                    String timeValue = record.getValueByColumnIndex(posTimeInstant);
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
                    DataAcquisitionSchemaEvent dase = null;
                    String daseUri = dasa.getEventUri();
                    if (mapSchemaEvents.containsKey(daseUri)) {
                        dase = mapSchemaEvents.get(daseUri);
                    } else {
                        System.out.println("daseUri: " + daseUri);
                        dase = schema.getEvent(daseUri);
                        if (dase != null) {
                            mapSchemaEvents.put(daseUri, dase);
                        }
                    }
                    if (dase != null) {
                        measurement.setAbstractTime(dase.getUri());
                    }
                }

                /*============================*
                 *                            *
                 *   SET STUDY                *
                 *                            *
                 *============================*/
                measurement.setStudyUri(da.getStudyUri());

                /*=============================*
                 *                             *
                 *   SET OBJECT ID, PID, SID   *
                 *                             *
                 *=============================*/
                measurement.setObjectCollectionType("");
                measurement.setStudyObjectUri("");
                measurement.setObjectUri("");
                measurement.setPID("");
                measurement.setSID("");

                String id = "";
                if (!schema.getOriginalIdLabel().equals("")) {
                    id = record.getValueByColumnIndex(posOriginalId);
                } else if (!schema.getIdLabel().equals("")) {
                    id = record.getValueByColumnIndex(posId);
                }

                if (!id.equals("")) {
                    if (dasa.getEntity().equals(URIUtils.replacePrefixEx("sio:Sample"))) {
                        if (mapIDStudyObjects.containsKey(id)) {
                            measurement.setStudyObjectUri(mapIDStudyObjects.get(id).get(0));
                            measurement.setObjectUri(mapIDStudyObjects.get(id).get(2));
                            measurement.setPID(mapIDStudyObjects.get(id).get(1));
                        }
                        measurement.setObjectCollectionType(URIUtils.replacePrefixEx("hasco:SampleCollection"));
                        measurement.setSID(id);
                    } else {
                        if (mapIDStudyObjects.containsKey(id)) {
                            measurement.setStudyObjectUri(mapIDStudyObjects.get(id).get(0));
                            measurement.setObjectUri(mapIDStudyObjects.get(id).get(0));
                        }
                        measurement.setObjectCollectionType(URIUtils.replacePrefixEx("hasco:SubjectGroup"));
                        measurement.setPID(id);
                    }
                }

                /*=============================*
                 *                             *
                 *   SET URI, OWNER AND DA URI *
                 *                             *
                 *=============================*/

                measurement.setUri(URIUtils.replacePrefixEx(measurement.getStudyUri()) + "/" + 
                        URIUtils.replaceNameSpaceEx(da.getUri()).split(":")[1] + "/" +
                        dasa.getLocalName() + "-" + total_count);
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
                        if (!schema.getOriginalIdLabel().equals("")) {
                            String originalId = record.getValueByColumnIndex(posOriginalId);
                            // values of daso might exist in the triple store
                            if (daso.getEntity().equals(URIUtils.replacePrefixEx("sio:Human"))) {
                                if (mapIDStudyObjects.containsKey(originalId)) {
                                    measurement.setObjectUri(mapIDStudyObjects.get(originalId).get(0));
                                }
                                measurement.setPID(originalId);
                            } else if (daso.getEntity().equals(URIUtils.replacePrefixEx("sio:Sample"))) {
                                if (mapIDStudyObjects.containsKey(originalId)) {
                                    measurement.setObjectUri(mapIDStudyObjects.get(originalId).get(2));
                                    measurement.setPID(mapIDStudyObjects.get(originalId).get(1));
                                }
                                measurement.setSID(originalId);
                            }
                        }
                        measurement.setEntityUri(daso.getEntity());
                    }
                } else {
                    measurement.setEntityUri(dasa.getObjectUri());
                }
                measurement.setCharacteristicUri(dasa.getAttribute());

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

                try {
                    solr.addBean(measurement);
                } catch (IOException | SolrServerException e) {
                    System.out.println("[ERROR] SolrClient.addBean - e.Message: " + e.getMessage());
                }

                // INTERMEDIARY COMMIT
                if((++total_count) % batch_size == 0) {
                    try {
                        System.out.println("solr.commit()...");
                        solr.commit();
                        System.out.println(String.format("[OK] Committed %s measurements!", batch_size));
                        log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Committed %s measurements!", batch_size)));
                    } catch (IOException | SolrServerException e) {
                        System.out.println("[ERROR] SolrClient.commit - e.Message: " + e.getMessage());
                        log.addline(Feedback.println(Feedback.WEB, "[ERROR] Fail to commit to solr"));
                        try {
                            solr.close();
                        } catch (IOException e1) {
                            System.out.println("[ERROR] SolrClient.close - e.Message: " + e1.getMessage());
                            log.addline(Feedback.println(Feedback.WEB, "[ERROR] Fail to close solr"));
                        }
                        return new ParsingResult(1, log.getLog());
                    }
                }
            }
        }

        // FINAL COMMIT
        try {
            System.out.println("solr.commit()...");
            solr.commit();
            System.out.println(String.format("[OK] Committed %s measurements!", total_count % batch_size));
            log.addline(Feedback.println(Feedback.WEB, 
                    String.format("[OK] Committed %s measurements!", total_count % batch_size)));
        } catch (IOException | SolrServerException e) {
            System.out.println("[ERROR] SolrClient.commit - e.Message: " + e.getMessage());
            log.addline(Feedback.println(Feedback.WEB, "[ERROR] Fail to commit to solr"));
            try {
                solr.close();
            } catch (IOException e1) {
                System.out.println("[ERROR] SolrClient.close - e.Message: " + e1.getMessage());
                log.addline(Feedback.println(Feedback.WEB, "[ERROR] Fail to close solr"));
            }
            return new ParsingResult(1, log.getLog());
        }

        da.addNumberDataPoints(total_count);
        da.saveToSolr();

        System.out.println("Finished indexMeasurements()");
        try {
            solr.close();
        } catch (IOException e) {
            System.out.println("[ERROR] SolrClient.close - e.Message: " + e.getMessage());
            log.addline(Feedback.println(Feedback.WEB, "[ERROR] Fail to close solr"));
        }

        return new ParsingResult(0, log.getLog());
    }
}
