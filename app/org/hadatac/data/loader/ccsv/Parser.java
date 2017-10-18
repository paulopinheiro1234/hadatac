package org.hadatac.data.loader.ccsv;

import java.io.IOException;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.hadatac.data.loader.util.FileFactory;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.Subject;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.controllers.fileviewer.CSVPreview;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;

import play.Play;

public class Parser {

	private DataAcquisitionSchema schema;

	private static String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");

	public Parser() {
		schema = null;
	}

	public ParsingResult indexMeasurements(FileFactory files, DataAcquisition da, DataFile dataFile){
		System.out.println("indexMeasurements()...");
		
		schema = DataAcquisitionSchema.find(da.getSchemaUri());
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
		}
		if (!schema.getTimeInstantLabel().equals("")) {
			posTimeInstant = tempPositionOfLabel(schema.getTimeInstantLabel()); 
		}
		if (!schema.getNamedTimeLabel().equals("")) {
			posNamedTime = tempPositionOfLabel(schema.getNamedTimeLabel()); 
		}
		if (!schema.getIdLabel().equals("")) {
			posId = tempPositionOfLabel(schema.getIdLabel()); 
		}
		if (!schema.getOriginalIdLabel().equals("")) {
			posOriginalId = tempPositionOfLabel(schema.getOriginalIdLabel()); 
		}
		if (!schema.getEntityLabel().equals("")) {
			posEntity = tempPositionOfLabel(schema.getEntityLabel()); 
		}
		if (!schema.getUnitLabel().equals("")) {
			posUnit = tempPositionOfLabel(schema.getUnitLabel()); 
		}
		if (!schema.getInRelationToLabel().equals("")) {
			posInRelation = tempPositionOfLabel(schema.getInRelationToLabel()); 
		}

		for (CSVRecord record : records) {
			Iterator<DataAcquisitionSchemaAttribute> iter = schema.getAttributes().iterator();
			while (iter.hasNext()) {
				DataAcquisitionSchemaAttribute dasa = iter.next();
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
					String codeValue = Subject.findCodeValue(dasa.getAttribute(), originalValue);
					if (codeValue == null) {
						measurement.setValue(originalValue);
					} else {
						measurement.setValue(codeValue);
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
						measurement.setAbstractTime(timeValue);
					} else {
						measurement.setAbstractTime("");
					}
				}  else if (dasa.getEventUri() != null && !dasa.getEventUri().equals("")) {
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

				if (!schema.getOriginalIdLabel().equals("")) {
					String auxUri = record.get(posOriginalId);
					measurement.setObjectUri(auxUri);
					measurement.setPID(auxUri);
					measurement.setSID("");
				}  else if (!schema.getIdLabel().equals("")) {
					String auxUri = record.get(posId);
					measurement.setObjectUri(auxUri);
					measurement.setPID(auxUri);
					measurement.setSID("");
				} else {
					measurement.setObjectUri("");
					measurement.setPID("");
					measurement.setSID("");
				}


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

				/*=============================*
				 *                             *
				 *   SET UNIT                  *
				 *                             *
				 *=============================*/
				if (!schema.getUnitLabel().equals("")) {
					String unitValue = record.get(posUnit);
					if (unitValue != null) {
						measurement.setUnitUri(dasa.getUnit());
					} else {
						measurement.setUnitUri(dasa.getUnit());
					}
				} else {
					measurement.setUnitUri(dasa.getUnit());
				}

				measurement.setSchemaAttributeUri(dasa.getUri().replace("<", "").replace(">", ""));
				measurement.setEntityUri(dasa.getEntity());
				measurement.setCharacteristicUri(dasa.getAttribute());

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
				return dasa.getTempPositionInt();
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
