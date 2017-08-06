package org.hadatac.data.loader.ccsv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

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
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.HADataC;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.Subject;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Feedback;

import play.Play;

public class Parser2 {
	
    private FileFactory files;
    
    private DataAcquisitionSchema schema;
    
    public Parser2() {
	schema = null;
    }
    
    private ParsingResult indexMeasurements(FileFactory files, DataAcquisition da){
	System.out.println("indexMeasurements()...");
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
	    //System.out.println("pos. of id column: " + schema.getIdColumn());
	    while (iter.hasNext()) {
		DataAcquisitionSchemaAttribute dasa = iter.next();
		if (dasa.getPositionInt() == schema.getTimestampColumn()) {
		    continue;
		}
		if (dasa.getPositionInt() == schema.getTimeInstantColumn()) {
		    continue;
		}
		if (dasa.getPositionInt() == schema.getIdColumn()) {
		    continue;
		}
		if (dasa.getPositionInt() == schema.getEntityColumn()) {
		    continue;
		}
		if (dasa.getPositionInt() == schema.getUnitColumn()) {
		    continue;
		}
		if (dasa.getPositionInt() == schema.getInRelationToColumn()) {
		    continue;
		}
		
		Measurement measurement = new Measurement();
		
		/*===================*
		 *                   *
		 *   SET VALUE       *
		 *                   *
		 *===================*/
		
		if (dasa.getPositionInt() > -1 && record.get(dasa.getPositionInt() - 1).isEmpty()){
		    continue;
		} else {
		    String originalValue = record.get(dasa.getPositionInt() - 1);
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
		  - TimestampColumn is used for machine generated timestamp
		  - TimeInstantColumn is used for timestamps told to system to be timestamp, but that are not further processed
		  - Abstract times are encoded as DASA's events, and are supposed to be strings
		*/
		
		// contrete time(stamps)
		if(dasa.getPositionInt() == schema.getTimestampColumn()) {
		    String sTime = record.get(schema.getTimestampColumn() - 1);
		    int timeStamp = new BigDecimal(sTime).intValue();
		    Date time = new Date((long)timeStamp * 1000);
		    measurement.setTimestamp(time.toString());
		} else if (schema.getTimeInstantColumn() != -1) {
		    String timeValue = record.get(schema.getTimeInstantColumn() - 1);
		    //System.out.println("Time Instant value: " + timeValue);
		    if (timeValue != null) {
			measurement.setTimestamp(timeValue);
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
		
		if (schema.getIdColumn() > -1){
		    if (dasa.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Human"))) {
			//System.out.println("Matching reference subject: " + record.get(schema.getIdColumn() - 1));
			Subject subject = Subject.findSubject(measurement.getStudyUri(), record.get(schema.getIdColumn() - 1));
			if (subject != null) {
			    String subjectUri = subject.getUri();
			    measurement.setObjectUri(subjectUri);
			    measurement.setPID(subjectUri);
			    measurement.setSID(subjectUri);
			} else {
			    measurement.setObjectUri("");
			}
		    } else if (dasa.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Sample"))) {
			//System.out.println("Matching reference sample: " + record.get(schema.getIdColumn() - 1));
			String sampleUri = Subject.findSampleUri(measurement.getStudyUri(), record.get(schema.getIdColumn() - 1));
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
		
		/*=============================*
		 *                             *
		 *   SET URI, OWNER AND DA D   *
		 *                             *
		 *=============================*/
		
		measurement.setUri(ValueCellProcessing.replacePrefixEx(measurement.getStudyUri()) + "/" 
				   + ValueCellProcessing.replaceNameSpaceEx(da.getUri()).split(":")[1] + "/"
				   //+ hadatacCcsv.getDataset().getLocalName() + "/" 
				   + dasa.getLocalName() + "-" + total_count);
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
		} else if (schema.getUnitColumn() != -1) {
		    String unitValue = record.get(schema.getUnitColumn() - 1);
		    //System.out.println("Unit value: " + unitValue);
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
		if (isSample && !matrix.equals("") && !analyte.equals("")) {
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
		}
		
		if (schema.getEntityColumn() != -1 && !record.get(schema.getEntityColumn() - 1).equals("")) {
		    measurement.setEntity(record.get(schema.getEntityColumn() - 1));
		}
		
		measurement.setEntityUri(dasa.getEntity());
		measurement.setCharacteristicUri(dasa.getAttribute());
		
		/*=================================*
		 *                                 *
		 *   SET DATASET                   *
		 *                                 *
		 *=================================*/
		
		//measurement.setDatasetUri(hadatacCcsv.getDatasetKbUri());
		try {
		    solr.addBean(measurement);
		} catch (IOException | SolrServerException e) {
		    System.out.println("[ERROR] SolrClient.addBean - e.Message: " + e.getMessage());
		}
		if((++total_count) % batch_size == 0){
		    try {
			System.out.println("solr.commit()...");
			solr.commit();
			System.out.println(String.format("[OK] Committed %s measurements!", batch_size));
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
	try {
	    try {
		System.out.println("solr.commit()...");
		solr.commit();
		System.out.println(String.format("[OK] Committed %s measurements!", total_count % batch_size));
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
    
    private String uppercaseFirstLetter(String str) {
	if (str == null || str.equals("")) {
	    return str;
	}
	return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
}
