package org.hadatac.data.loader.ccsv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.hadatac.data.loader.util.FileFactory;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.entity.pojo.Dataset;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.HADataC;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.Subject;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.controllers.fileviewer.CSVPreview;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;

import play.Play;

public class Parser {

	private FileFactory files;

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
			e.printStackTrace();
			message += "[ERROR] Fail to open the csv file\n";
			return new ParsingResult(1, message);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.DEFAULT.withHeader().parse(files.getReader("csv"));
		} catch (IOException e) {
			e.printStackTrace();
			message += "[ERROR] Fail to parse header of the csv file\n";
			return new ParsingResult(1, message);
		}
		int total_count = 0;
		int batch_size = 10000;

		boolean isSubjectPlatform = Subject.isPlatform(da.getDeployment().getPlatform().getUri());
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		boolean isSample;
		boolean isSubject;
		String matrix = "";
		String analyte = "";
		String unitOverride = "";
		String unitLabelOverride = "";

		ObjectCollection oc = null;
		if (da.getGlobalScopeUri() != null && !da.getGlobalScopeUri().equals("")) {
			oc = ObjectCollection.find(da.getGlobalScopeUri());
			if (oc != null) {
				for (StudyObject obj : oc.getObjects()) {
					System.out.println("Object " + obj.getLabel() + "  ID: "  + obj.getUri() + "   orig.: " + obj.getOriginalId());
				}
			}
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
			System.out.println("[DEBUG] schema.getEntityLabel() = " + schema.getEntityLabel()); // returns "??summaryClass"
			posEntity = tempPositionOfLabel(schema.getEntityLabel()); //tempPositionOfLabel returns -1 right now - this is bad.
		}
		if (!schema.getUnitLabel().equals("")) {
			posUnit = tempPositionOfLabel(schema.getUnitLabel()); 
		}
		if (!schema.getInRelationToLabel().equals("")) {
			posInRelation = tempPositionOfLabel(schema.getInRelationToLabel()); 
		}

		for (CSVRecord record : records) {

			// HACK FOR JUNE20
			isSample = false;
			unitOverride = "";
			unitLabelOverride = "";
			if (record.get(0).toLowerCase().equals("sample") ) {
				isSample = true;
				if (record.get(5) != null && !record.get(5).equals("")) {
					matrix = record.get(5);
					matrix = matrix.substring(0,1).toUpperCase() + matrix.substring(1).toLowerCase();
				} else {
					matrix = "";
				}
				if (record.get(6) != null && !record.get(6).equals("")) {
					analyte = "concentration of " + record.get(6);
				} else {
					analyte = "";
				}
				if (record.get(8) != null && !record.get(8).equals("")) {
					switch (record.get(8).trim()) {
					case "microg/L":  
						unitOverride = "http://geneontology.org/GO.format.obo-1_2.shtml#UO_0000301";
						unitLabelOverride = "microgram per liter";
						break;
					case "ppb" :  
						unitOverride = "http://geneontology.org/GO.format.obo-1_2.shtml#UO_0000170";
						unitLabelOverride = "parts per billion";
						break;
					}
				}
				//System.out.println("CSV Record: matrix " + matrix + " Analyte: " + analyte);
			}

			Iterator<DataAcquisitionSchemaAttribute> iter = schema.getAttributes().iterator();
			//System.out.println("pos. of id column: " + schema.getIdLabel());
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

				if (dasa.getTempPositionInt() <= -1 || dasa.getTempPositionInt() >= record.size()) {
					continue;
				} else if (record.get(dasa.getTempPositionInt()).isEmpty()) { 
					continue;
				} else {
					String originalValue = record.get(dasa.getTempPositionInt());
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

				measurement.setTimestamp(new Date(Long.MAX_VALUE).toInstant().toString());
				measurement.setAbstractTime("");

				// full-row regular (Epoch) timemestamp
				if(dasa.getLabel() == schema.getTimestampLabel()) {
					String sTime = record.get(posTimestamp);
					int timeStamp = new BigDecimal(sTime).intValue();
					measurement.setTimestamp(Instant.ofEpochSecond(timeStamp).toString());

					// full-row regular (XSD) time interval
				} else if (!schema.getTimeInstantLabel().equals("")) {
					String timeValue = record.get(posTimeInstant);
					//System.out.println("Time Instant value: " + timeValue);
					if (timeValue != null) {
						//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy HH:mm");
						//LocalDateTime datetime = LocalDateTime.parse(timeValue, formatter);
						//measurement.setTimestamp(datetime.toInstant(ZoneOffset.UTC).toString());
						try {
							measurement.setTimestamp(timeValue);
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

					// row-specific, SSD-named time 
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

				/*		
		// contrete time(stamps)
		if(dasa.getTempPositionInt() == schema.getTimestampColumn()) {
		    String sTime = record.get(schema.getTimestampColumn());
		    int timeStamp = new BigDecimal(sTime).intValue();
		    Date time = new Date((long)timeStamp * 1000);
		    measurement.setTimestamp(time.toString());
		} else if (schema.getTimeInstantColumn() != -1) {
		    String timeValue = record.get(schema.getTimeInstantColumn());
		    //System.out.println("Time Instant value: " + timeValue);
		    if (timeValue != null) {
			try {
			    measurement.setTimestamp(timeValue);
			} catch (Exception e) {
			    //measurement.setTimestamp("");
			}
		    }
		}    

		// abstract times 
		else if (dasa.getEventUri() != null && !dasa.getEventUri().equals("")) {
		    String daseUri = dasa.getEventUri();
		    DataAcquisitionSchemaEvent dase = schema.getEvent(daseUri); 
		    if (dase != null) {
			if (dase.getLabel() != null && !dase.getLabel().equals("")) {
			    measurement.setTimestamp("At " + dase.getLabel());
			} else if (dase.getEntity() != null && !dase.getEntity().equals("")) {
			    measurement.setTimestamp("At " + dase.getEntity().substring(dase.getEntity().indexOf("#") + 1));
			} else {
			    measurement.setTimestamp("At " + daseUri);
			}
		    } 
		}

		// no time information
		else {
		measurement.setTimestamp("");
		}
				 */

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

				if (!schema.getOriginalIdLabel().equals("")){

					//System.out.println("Recording....: " + record.get(posOriginalId));
					//String auxUri = oc.getUriFromOriginalId(record.get(tempPositionOfLabel(schema.getOriginalIdLabel())));
					String auxUri = record.get(posOriginalId);
					measurement.setObjectUri(auxUri);
					measurement.setPID(auxUri);
					measurement.setSID("");

				}  else if (!schema.getIdLabel().equals("")){

					//String auxUri = oc.getUriFromOriginalId(record.get(tempPositionOfLabel(schema.getIdLabel())));
					String auxUri = record.get(posId);
					measurement.setObjectUri(auxUri);
					measurement.setPID(auxUri);
					measurement.setSID("");

				} else {

					measurement.setObjectUri("");
					measurement.setPID("");
					measurement.setSID("");

				}

				/*
		  if (dasa.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Human"))) {
		  //System.out.println("Matching reference subject: " + record.get(schema.getIdColumn()));
		  Subject subject = Subject.findSubject(measurement.getStudyUri(), record.get(schema.getIdColumn()));
		  if (subject != null) {
		  String subjectUri = subject.getUri();
		  measurement.setObjectUri(subjectUri);
		  measurement.setPID(subjectUri);
		  measurement.setSID(subjectUri);
		  } else {
		  measurement.setObjectUri("");
		  }
		  } else if (dasa.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Sample"))) {
		  //System.out.println("Matching reference sample: " + record.get(schema.getIdColumn()));
		  String sampleUri = Subject.findSampleUri(measurement.getStudyUri(), record.get(schema.getIdColumn()));
		  if (sampleUri != null) {
		  measurement.setObjectUri(sampleUri);
		  measurement.setPID(sampleUri);
		  measurement.setSID(sampleUri);
		  } else {
		  measurement.setObjectUri("");
		  }
		  }
		  } else {
		  if(isSubjectPlatform) {
		  measurement.setObjectUri(da.getDeployment().getPlatform().getUri());
		  } else {
		  measurement.setObjectUri("");
		  }
		  } 
				 */

				/*=============================*
				 *                             *
				 *   SET URI, OWNER AND DA D   *
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

				// HACK FOR JUNE 20
				if (isSample && !unitOverride.equals("") && !unitLabelOverride.equals("")) {
					measurement.setUnit(uppercaseFirstLetter(unitLabelOverride));
					measurement.setUnitUri(unitOverride);
				} else if (!schema.getUnitLabel().equals("")) {
					String unitValue = record.get(posUnit);
					if (unitValue != null) {
						measurement.setUnit(uppercaseFirstLetter(unitValue));
						measurement.setUnitUri(dasa.getUnit());
					} else {
						measurement.setUnit("");
						measurement.setUnitUri(dasa.getUnit());
					}
				} else {
					measurement.setUnit(uppercaseFirstLetter(dasa.getUnitLabel()));
					measurement.setUnitUri(dasa.getUnit());
				}

				/*=================================*
				 *                                 *
				 *   SET INSTRUMENT AND PLATFORM   *
				 *                                 *
				 *=================================*/

				measurement.setInstrumentModel(uppercaseFirstLetter(da.getDeployment().getInstrument().getLabel()));
				measurement.setInstrumentUri(uppercaseFirstLetter(da.getDeployment().getInstrument().getUri()));
				measurement.setPlatformName(uppercaseFirstLetter(da.getDeployment().getPlatform().getLabel()));
				measurement.setPlatformUri(uppercaseFirstLetter(da.getDeployment().getPlatform().getUri()));

				/*=================================*
				 *                                 *
				 *   SET ENTITY AND ATTRIB         *
				 *                                 *
				 *=================================*/

				// HACK FOR JUNE 20
				//System.out.println("dasa.getEntity : <" + dasa.getEntity() + ">");

				/*if (isSample && !matrix.equals("") && !analyte.equals("")) {
		    measurement.setEntity(uppercaseFirstLetter(matrix));
		    measurement.setCharacteristic(uppercaseFirstLetter(analyte));
		} else if (dasa.getEntity().equals("http://semanticscience.org/resource/Human")) {
		    String dasoUri = dasa.getObjectUri();
		    DataAcquisitionSchemaObject daso = schema.getObject(dasoUri); 
		    if (daso != null) {
			measurement.setEntity(uppercaseFirstLetter(daso.getRole().substring(daso.getRole().indexOf("#") + 1)));
		    } else {
			measurement.setEntity(uppercaseFirstLetter(dasa.getEntityLabel()));
		    }
		    measurement.setCharacteristic(uppercaseFirstLetter(dasa.getAttributeLabel()));
		} else {
		    measurement.setEntity(uppercaseFirstLetter(dasa.getEntityLabel()));
		    measurement.setCharacteristic(uppercaseFirstLetter(dasa.getAttributeLabel()));
		    } */

				// TODO: un-hack this
				// HACK FOR AUGUST 18
				measurement.setEntity("Subject"); // should not be hard-coded
				measurement.setEntityUri(dasa.getEntity());
				System.out.println("[debug]: getEntity " + dasa.getEntity());
				measurement.setCharacteristic(uppercaseFirstLetter(dasa.getAttributeLabel()));
				measurement.setCharacteristicUri(dasa.getAttribute());
		
				System.out.println("schema.getEntityLabel() = " + schema.getEntityLabel());
				System.out.println("record.get(posEntity) = " + record.get(posEntity)); // array index out of bounds exception :c
				if (!schema.getEntityLabel().equals("") && !record.get(posEntity).equals("")) {
					measurement.setEntity(record.get(posEntity));
				}

				/*=================================*
				 *                                 *
				 *   SET DATASET                   *
				 *                                 *
				 *=================================*/

				//measurement.setDatasetUri(hadatacCcsv.getDatasetKbUri());
				measurement.setDatasetUri(dataFile.getDatasetUri());

				try {
					solr.addBean(measurement);
					//System.out.println("indexMeasurements() ADDING VALUES");
				} catch (IOException | SolrServerException e) {
					System.out.println("[ERROR] SolrClient.addBean - e.Message: " + e.getMessage());
				}


				// INTERMEDIARY COMMIT

				//System.out.println(total_count);

				if((++total_count) % batch_size == 0){
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

		//System.out.println("indexMeasurements() HERE 7");
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
			e.printStackTrace();
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

		// print final mapping
		System.out.println("[Ok] Mapping of attributes and labels");
		//for (DataAcquisitionSchemaAttribute dasa : dasas) {
		//    System.out.println("Label: " + dasa.getLabel() + "    Position: [" + dasa.getTempPositionInt() + "]");
		//}	
		for (DataAcquisitionSchemaAttribute dasa : dasas) {
			if (dasa.getTempPositionInt() > -1) {
				System.out.println("Label: " + dasa.getLabel() + "    Position: [" + dasa.getTempPositionInt() + "]");
			}
			/*else{
				System.out.println("[ERROR] Label: " + dasa.getLabel() + "    Position: [" + dasa.getTempPositionInt() + "]");
			}*/
		}	

	}

	private int tempPositionOfLabel(String label) {
		//System.out.println("inside tempPositionOfLabel: " + label);
		if (label == null || label.equals("")) {
			return -1;
		}
		for (DataAcquisitionSchemaAttribute dasa : schema.getAttributes()) {
			//System.out.println(dasa.getLabel());
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
