package org.hadatac.data.loader.ccsv;

import java.io.IOException;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.data.loader.util.FileFactory;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DASVirtualObject;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.controllers.fileviewer.CSVPreview;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;

import play.Play;

public class Parser {

	private DataAcquisitionSchema schema;
	private List<DASVirtualObject> templateList;

	private static String path_unproc = ConfigProp.getPathUnproc();

	public Parser() {
		schema = null;
		templateList = new ArrayList<DASVirtualObject>();
	}

	public ParsingResult indexMeasurements(FileFactory files, DataAcquisition da, DataFile dataFile) {
		System.out.println("[Parser] indexMeasurements()...");

		Map<String, DataAcquisitionSchemaObject> mapSchemaObjects = new HashMap<String, DataAcquisitionSchemaObject>();
		schema = DataAcquisitionSchema.find(da.getSchemaUri());

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

		String message = "";

		try {
			files.openFile("csv", "r");
		} catch (IOException e) {
			System.out.println("[ERROR] Fail to open the csv file\n");
			message += "[ERROR] Fail to open the csv file\n";
			return new ParsingResult(1, message);
		}

		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.DEFAULT.withHeader().parse(files.getReader("csv"));
		} catch (IOException e) {
			System.out.println("[ERROR] Fail to parse header of the csv file\n");
			message += "[ERROR] Fail to parse header of the csv file\n";
			return new ParsingResult(1, message);
		}

		int total_count = 0;
		int batch_size = 10000;

		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		boolean isSample;
		String matrix = "";
		String analyte = "";
		ObjectCollection oc = null;
		if (da.getGlobalScopeUri() != null && !da.getGlobalScopeUri().equals("")) {
			oc = ObjectCollection.find(da.getGlobalScopeUri());
		}

		// ASSIGN values for tempPositionInt
		defineTemporaryPositions(files.getFileName());

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
			posTimestamp = tempPositionOfLabel(schema.getTimestampLabel());
			System.out.println("Finished tempPositionOfLabel!!!");
		}
		if (!schema.getTimeInstantLabel().equals("")) {
			posTimeInstant = tempPositionOfLabel(schema.getTimeInstantLabel());
			System.out.println("Finished tempPositionOfLabel!!!");
		}
		if (!schema.getNamedTimeLabel().equals("")) {
			posNamedTime = tempPositionOfLabel(schema.getNamedTimeLabel());
			System.out.println("Finished tempPositionOfLabel!!!");
		}
		if (!schema.getIdLabel().equals("")) {
			posId = tempPositionOfLabel(schema.getIdLabel());
			System.out.println("Finished tempPositionOfLabel!!!");
		}
		if (!schema.getOriginalIdLabel().equals("")) {
			posOriginalId = tempPositionOfLabel(schema.getOriginalIdLabel());
			System.out.println("Finished tempPositionOfLabel!!!");
		}
		if (!schema.getEntityLabel().equals("")) {
			posEntity = tempPositionOfLabel(schema.getEntityLabel());
			System.out.println("Finished tempPositionOfLabel!!!");
		}
		if (!schema.getUnitLabel().equals("")) {
			posUnit = tempPositionOfLabel(schema.getUnitLabel());
			System.out.println("Finished tempPositionOfLabel!!!");
		}
		if (!schema.getInRelationToLabel().equals("")) {
			posInRelation = tempPositionOfLabel(schema.getInRelationToLabel());
			System.out.println("Finished tempPositionOfLabel getInRelationToLabel !!!");
		}
		
		// Store possible values before hand to avoid frequent SPARQL queries
		Map<String, Map<String, String>> possibleValues = DataAcquisitionSchema.findPossibleValues(da.getSchemaUri());
		Map<String, List<String>> mapIDStudyObjects = DataAcquisitionSchema.findIdUriMappings(da.getStudyUri());
		String dasoUnitUri = DataAcquisitionSchema.findByPosIndex(da.getSchemaUri(), Integer.toString(posUnit + 1));
		
		// Comment out row instance generation
		/*
		// Need to be fixed here by getting codeMap and codebook from sparql query
		DASOInstanceGenerator dasoiGen = new DASOInstanceGenerator(da.getStudy().getUri(), 
				templateList, AnnotationWorker.codeMappings, AnnotationWorker.codebook_K);
		Map<String, DASOInstance> rowInstances = new HashMap<String,DASOInstance>();
		*/

		for (CSVRecord record : records) {
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
				//System.out.println("[Parser] read a DASA " + dasa.getUri() + " with label " + dasa.getLabel());
				// why is schema.getAttributes() returning DASAs that don't belong to the schema?
				if (!dasa.getPartOfSchema().equals(schema.getUri())){
					//System.out.println("[Parser] .... Skipping attribute " + dasa.getPartOfSchema() + " != " + schema.getUri());
					continue;
				}
				if (!record.isMapped(dasa.getLabel())) {
					//System.out.println("[Parser] .... Skipping attribute " + dasa.getLabel() + " : not in the DA file");
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

				if (dasa.getTempPositionInt() <= -1 || dasa.getTempPositionInt() > record.size()) {
					continue;
				} else if (record.get(dasa.getTempPositionInt() - 1).isEmpty()) { 
					continue;
				} else {
					String originalValue = record.get(dasa.getTempPositionInt() - 1);
					if (possibleValues.containsKey(dasa.getAttribute())) {
						if (possibleValues.get(dasa.getAttribute()).containsKey(originalValue.toLowerCase())) {
							measurement.setValue(possibleValues.get(dasa.getAttribute()).get(originalValue.toLowerCase()));
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
				measurement.setTimestamp(new Date(Long.MAX_VALUE));
				measurement.setAbstractTime("");

				// full-row regular (Epoch) timemestamp
				if(dasa.getLabel() == schema.getTimestampLabel()) {
					String sTime = record.get(posTimestamp);
					int timeStamp = new BigDecimal(sTime).intValue();
					measurement.setTimestamp(Instant.ofEpochSecond(timeStamp).toString());
				// full-row regular (XSD) time interval
				} else if (!schema.getTimeInstantLabel().equals("")) {
					String timeValue = record.get(posTimeInstant);
					if (timeValue != null) {
						try {
							measurement.setTimestamp(timeValue);
							System.out.println("timeValue: " + timeValue);
						} catch (Exception e) {
							measurement.setTimestamp(new Date(Long.MAX_VALUE).toInstant().toString());
						}
					}

					// full-row named time
				} else if (!schema.getNamedTimeLabel().equals("")) {
					String timeValue = record.get(posNamedTime);
					if (timeValue != null) {
						//System.out.println("[Parser] timeValue = " + timeValue);
						measurement.setAbstractTime(timeValue);
					} else {
						measurement.setAbstractTime("");
					}
				} else if (dasa.getEventUri() != null && !dasa.getEventUri().equals("")) {
					String daseUri = dasa.getEventUri();
					DataAcquisitionSchemaEvent dase = schema.getEvent(daseUri); 
					if (dase != null) {
						if (dase.getLabel() != null && !dase.getLabel().equals("")) {
							measurement.setAbstractTime("At " + dase.getLabel());
						} else if (dase.getEntity() != null && !dase.getEntity().equals("")) {
							measurement.setAbstractTime("At " + dase.getEntity().substring(dase.getEntity().indexOf("#") + 1));
						} else {
							measurement.setAbstractTime("At " + daseUri);
						}
					} 
				}

				/*============================*
				 *                            *
				 *   SET STUDY                *
				 *                            *
				 *============================*/
				measurement.setStudyUri(ValueCellProcessing.replaceNameSpaceEx(da.getStudyUri()));

				/*=============================*
				 *                             *
				 *   SET OBJECT ID, PID, SID   *
				 *                             *
				 *=============================*/

				String id = "";
				if (!schema.getOriginalIdLabel().equals("")) {
					id = record.get(posOriginalId);
				} else if (!schema.getIdLabel().equals("")) {
					id = record.get(posId);
				}
				
				if (!id.equals("")) {
					if (dasa.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Human"))) {
						if (mapIDStudyObjects.containsKey(id)) {
							measurement.setObjectUri(mapIDStudyObjects.get(id).get(0));
						} else {
							measurement.setObjectUri("");
						}
						measurement.setPID(id);
						measurement.setSID("");
					} else if (dasa.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Sample"))) {
						if (mapIDStudyObjects.containsKey(id)) {
							measurement.setObjectUri(mapIDStudyObjects.get(id).get(2));
							measurement.setPID(mapIDStudyObjects.get(id).get(1));
						} else {
							measurement.setObjectUri("");
							measurement.setPID("");
						}
						measurement.setSID(id);
					}
				} else {
					measurement.setObjectUri("");
					measurement.setPID("");
					measurement.setSID("");
				}

				//String auxUri = rowInstances.get(dasa.getObjectUri()).getUri();
				
				/*=============================*
				 *                             *
				 *   SET URI, OWNER AND DA URI *
				 *                             *
				 *=============================*/

				measurement.setUri(ValueCellProcessing.replacePrefixEx(measurement.getStudyUri()) + "/" + 
						ValueCellProcessing.replaceNameSpaceEx(da.getUri()).split(":")[1] + "/" +
						dasa.getLocalName() + "-" + total_count);
				measurement.setOwnerUri(da.getOwnerUri());
				measurement.setAcquisitionUri(da.getUri());
				
				/*======================================*
				 *                                      *
				 *   SET ENTITY AND CHARACTERISTIC URI  *              *
				 *                                      *
				 *======================================*/
				measurement.setSchemaAttributeUri(dasa.getUri().replace("<", "").replace(">", ""));
				DataAcquisitionSchemaObject daso = null;
				String dasoUri = dasa.getObjectUri();
				if (mapSchemaObjects.containsKey(dasoUri)) {
					daso = mapSchemaObjects.get(dasoUri);
				} else {
					daso = DataAcquisitionSchemaObject.find(dasoUri);
					mapSchemaObjects.put(dasoUri, daso);
				}
				
				if (null != daso) {
					if (daso.getPositionInt() > 0) {
						// values of daso exist in the columns
						String dasoValue = record.get(daso.getPositionInt() - 1);
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
							String originalId = record.get(posOriginalId);
							// values of daso might exist in the triple store
							if (daso.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Human"))) {
								System.out.println("sio:Human========================");
								System.out.println("schema.getOriginalIdLabel(): " + schema.getOriginalIdLabel());
								if (mapIDStudyObjects.containsKey(originalId)) {
									measurement.setObjectUri(mapIDStudyObjects.get(originalId).get(0));
								} else {
									measurement.setObjectUri("");
								}
								measurement.setPID(originalId);
								measurement.setSID("");
							} else if (daso.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Sample"))) {
								System.out.println("sio:Sample========================");
								System.out.println("schema.getOriginalIdLabel(): " + schema.getOriginalIdLabel());
								if (mapIDStudyObjects.containsKey(originalId)) {
									measurement.setObjectUri(mapIDStudyObjects.get(originalId).get(2));
									measurement.setPID(mapIDStudyObjects.get(originalId).get(1));
								} else {
									measurement.setObjectUri("");
									measurement.setPID("");
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
				
				/*=============================*
				 *                             *
				 *   SET UNIT                  *
				 *                             *
				 *=============================*/
				if (!dasa.getUnit().equals("")) {
					// Assign units from the Unit column of SDD
					measurement.setUnitUri(dasa.getUnit());
				} else {
					if (!schema.getUnitLabel().equals("")) {
						// unit exists in the columns
						String unitValue = record.get(posUnit);
						if (unitValue != null) {
							if (possibleValues.containsKey(dasoUnitUri)) {
								if (possibleValues.get(dasoUnitUri).containsKey(unitValue.toLowerCase())) {
									measurement.setUnitUri(possibleValues.get(dasoUnitUri).get(unitValue.toLowerCase()));
								} else {
									measurement.setUnitUri("");
								}
							} else {
								measurement.setUnitUri("");
							}
						}
					} else {
						measurement.setUnitUri("");
					}
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
						message += String.format("[OK] Committed %s measurements!\n", batch_size);
					} catch (IOException | SolrServerException e) {
						System.out.println("[ERROR] SolrClient.commit - e.Message: " + e.getMessage());
						message += "[ERROR] Fail to commit to solr\n";
						try {
							solr.close();
						} catch (IOException e1) {
							System.out.println("[ERROR] SolrClient.close - e.Message: " + e1.getMessage());
							message += "[ERROR] Fail to close solr\n";
						}
						return new ParsingResult(1, message);
					}
				}
			}
		}

		// FINAL COMMIT
		try {
			try {
				System.out.println("solr.commit()...");
				solr.commit();
				System.out.println(String.format("[OK] Committed %s measurements!", total_count % batch_size));
				message += String.format("[OK] Committed %s measurements!\n", total_count % batch_size);
			} catch (IOException | SolrServerException e) {
				solr.close();
				System.out.println("[ERROR] SolrClient.commit - e.Message: " + e.getMessage());
				message += "[ERROR] Fail to commit to solr\n";
				return new ParsingResult(1, message);
			}
			files.closeFile("csv", "r");
		} catch (IOException e) {
			message += "[ERROR] Fail to close the csv file\n";
			return new ParsingResult(1, message);
		}

		da.addNumberDataPoints(total_count);
		da.save();

		System.out.println("Finished indexMeasurements()");
		try {
			solr.close();
		} catch (IOException e) {
			System.out.println("[ERROR] SolrClient.close - e.Message: " + e.getMessage());
			message += "[ERROR] Fail to close solr\n";
		}
		return new ParsingResult(0, message);
	}


	private void defineTemporaryPositions(String filename) {
		if (schema == null || schema.getAttributes() == null || schema.getAttributes().size() == 0) {
			return;
		}
		List<DataAcquisitionSchemaAttribute> dasas = schema.getAttributes();
		List<String> headers = CSVPreview.getCSVHeaders(path_unproc, filename);

		// reset temporary positions
		for (DataAcquisitionSchemaAttribute dasa : dasas) {
			dasa.setTempPositionInt(-1);
		}

		// match dasas and labels, assigning temporary positions
		for (int h = 0; h < headers.size(); h++) {
			for (int d = 0; d < dasas.size(); d++) {
				if (headers.get(h).equals(dasas.get(d).getLabel())) {
					dasas.get(d).setTempPositionInt(h);
				}
			} 
		}

		// override temporary positions with permanent positions
		for (int i = 0; i < dasas.size(); i++) {
			if (dasas.get(i).getPositionInt() >= 0) {
				dasas.get(i).setTempPositionInt(dasas.get(i).getPositionInt());
			} 
		}
	}

	private int tempPositionOfLabel(String label) {
		if (label == null || label.equals("")) {
			return -1;
		}
		for (DataAcquisitionSchemaAttribute dasa : schema.getAttributes()) {
			if (dasa.getLabel().equals(label)) {
				return dasa.getTempPositionInt() - 1;
			}
		}

		return -1;
	}


	private String uppercaseFirstLetter(String str) {
		if (str == null || str.equals("")) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

}
