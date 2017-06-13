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
import org.hadatac.entity.pojo.Dataset;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.HADataC;
import org.hadatac.entity.pojo.Measurement;
//import org.hadatac.entity.pojo.MeasurementType;
import org.hadatac.entity.pojo.Subject;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Feedback;

import play.Play;

public class Parser {
	
	private FileFactory files;
	
	private HADataC hadatacCcsv;
        private HADataC hadatacKb;
        private DataAcquisitionSchema schema;
	
	public Parser() {
		hadatacCcsv = null;
		hadatacKb = null;
		schema = null;
	}
	
	public ParsingResult validate(int mode, FileFactory files) throws IOException {
		ParsingResult result = null;
		String message = "";
		String preamble;
		
		this.files = files;
		files.openFile("ccsv", "r");
		
		preamble = getPreamble();
		Model model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(preamble.getBytes()), null, "TTL");
		
		// Verify if model is successfully loaded
		if (model.isEmpty()) {
			message += Feedback.println(mode, "[ERROR] Preamble not a well-formed Turtle.");
			System.out.println("[ERROR] Preamble not a well-formed Turtle.");
		} 
		else {
			message += Feedback.println(mode, "[OK] Preamble a well-formed Turtle.");
			System.out.println("[OK] Preamble a well-formed Turtle.");
		}
		
		result = loadFromPreamble(mode, model);
		
		if (result.getStatus() == 0) {
			message += result.getMessage();
			result = loadFromKb(mode);
			message += result.getMessage();
		}
		
		files.closeFile("ccsv", "r");
		
		return new ParsingResult(result.getStatus(), message);
	}
	
	public ParsingResult index(int mode) {
		System.out.println("indexing...");
		
		DataAcquisition dataAcquisition = DataAcquisition.create(hadatacCcsv, hadatacKb);
		if (hadatacCcsv.getDataAcquisition().getStatus() > 0) {
			hadatacKb.getDataAcquisition().merge(dataAcquisition);
		} 
		else {
			hadatacKb.setDataAcquisition(dataAcquisition);
		}
		hadatacKb.getDataAcquisition().save();
		ParsingResult result = indexMeasurements();

		return new ParsingResult(result.getStatus(), result.getMessage());
	}
	
	private ParsingResult indexMeasurements(){
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
		//for(MeasurementType mt : hadatacKb.getDataset().getMeasurementTypes()){
		//	if(mt.getTimestampColumn() > -1){
		//		nTimeStampCol = mt.getTimestampColumn();
		//	}
		//	if(mt.getTimeInstantColumn() > -1){
		//		nTimeInstantCol = mt.getTimeInstantColumn();
		//	}
		//	if(mt.getIdColumn() > -1){
		//		nIdCol = mt.getIdColumn();
		//	}
		//}
		
		boolean isSubjectPlatform = Subject.isPlatform(hadatacKb.getDeployment().getPlatform().getUri());
		SolrClient solr = new HttpSolrClient(Play.application().configuration().
				getString("hadatac.solr.data") + Collections.DATA_ACQUISITION);
                boolean isSample;
                boolean isSubject;
		String matrix = "";
		String analyte = "";
		for (CSVRecord record : records) {
		        // HACK FOR JUNE20
		        isSample = false;
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
			    //System.out.println("CSV Record: matrix " + matrix + " Analyte: " + analyte);
			}
			Iterator<DataAcquisitionSchemaAttribute> iter = schema.getAttributes().iterator();
			//System.out.println("pos. of id column: " + schema.getIdColumn());
			//Iterator<MeasurementType> iter = hadatacKb.getDataset().getMeasurementTypes().iterator();
			while (iter.hasNext()) {
			        DataAcquisitionSchemaAttribute dasa = iter.next();
			        //MeasurementType measurementType = iter.next();
				if (dasa.getPositionInt() == schema.getTimestampColumn()) {
					continue;
				}
				if (dasa.getPositionInt() == schema.getTimeInstantColumn()) {
					continue;
				}
				if (dasa.getPositionInt() == schema.getIdColumn()) {
					continue;
				}
				
				Measurement measurement = new Measurement();
				if (dasa.getPositionInt() > -1 && record.get(dasa.getPositionInt() - 1).isEmpty()){
					continue;
				} else {
					String originalValue = record.get(dasa.getPositionInt() - 1);
					String codeValue = Subject.findCodeValue(
							dasa.getAttribute(), originalValue);
					if (codeValue == null) {
					     measurement.setValue(originalValue);
					} else {
					     measurement.setValue(codeValue);
					}
				}
				
				if(dasa.getPositionInt() == schema.getTimestampColumn()) {
				        String sTime = record.get(schema.getTimestampColumn() - 1);
					int timeStamp = new BigDecimal(sTime).intValue();
					Date time = new Date((long)timeStamp * 1000);
					measurement.setTimestamp(time.toString());
				} else if(dasa.getPositionInt() == schema.getTimeInstantColumn()) {
				    measurement.setTimestamp(record.get(schema.getTimeInstantColumn() - 1));
				} else {
				    measurement.setTimestamp("");
				}
				
				measurement.setStudyUri(ValueCellProcessing.replaceNameSpaceEx(hadatacKb.getDataAcquisition().getStudyUri()));
				if (schema.getIdColumn() > -1){
					if (dasa.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Human"))) {
					        //System.out.println("Matching reference subject: " + record.get(schema.getIdColumn() - 1));
       						Subject subject = Subject.findSubject(measurement.getStudyUri(), record.get(schema.getIdColumn() - 1));
						if (subject != null) {
							String subjectUri = subject.getUri();
							measurement.setObjectUri(subjectUri);
						} else {
							measurement.setObjectUri("");
						}
					} else if (dasa.getEntity().equals(ValueCellProcessing.replacePrefixEx("sio:Sample"))) {
					        //System.out.println("Matching reference sample: " + record.get(schema.getIdColumn() - 1));
						String sampleUri = Subject.findSampleUri(measurement.getStudyUri(), record.get(schema.getIdColumn() - 1));
						if (sampleUri != null) {
							measurement.setObjectUri(sampleUri);
						} else {
							measurement.setObjectUri("");
						}
					}
				} else {
					if(isSubjectPlatform) {
						measurement.setObjectUri(hadatacKb.getDeployment().getPlatform().getUri());
					} else {
						measurement.setObjectUri("");
					}
				}
				
				measurement.setUri(ValueCellProcessing.replacePrefixEx(measurement.getStudyUri()) + "/" 
						+ ValueCellProcessing.replaceNameSpaceEx(hadatacKb.getDataAcquisition().getUri()).split(":")[1] + "/"
						+ hadatacCcsv.getDataset().getLocalName() + "/" 
						+ dasa.getLocalName() + "-" + total_count);
				measurement.setOwnerUri(hadatacKb.getDataAcquisition().getOwnerUri());
				measurement.setAcquisitionUri(hadatacKb.getDataAcquisition().getUri());
				measurement.setUnit(dasa.getUnitLabel());
				measurement.setUnitUri(dasa.getUnit());
				measurement.setCharacteristicUri(dasa.getAttribute());
				measurement.setInstrumentModel(hadatacKb.getDeployment().getInstrument().getLabel());
				measurement.setInstrumentUri(hadatacKb.getDeployment().getInstrument().getUri());
				measurement.setPlatformName(hadatacKb.getDeployment().getPlatform().getLabel());
				measurement.setPlatformUri(hadatacKb.getDeployment().getPlatform().getUri());
				// HACK FOR JUNE 20
				//System.out.println("dasa.getEntity : <" + dasa.getEntity() + ">");
				if (isSample && !matrix.equals("") && !analyte.equals("")) {
				    measurement.setEntity(matrix);
				    measurement.setCharacteristic(analyte);
				} else if (dasa.getEntity().equals("http://semanticscience.org/resource/Human")) {
				    String dasoUri = dasa.getObjectUri();
				    DataAcquisitionSchemaObject daso = schema.getObject(dasoUri); 
				    if (daso != null) {
					measurement.setEntity(daso.getRole().substring(daso.getRole().indexOf("#") + 1));
				    } else {
					measurement.setEntity(dasa.getEntityLabel());
				    }
				    measurement.setCharacteristic(dasa.getAttributeLabel());
				} else {
				    measurement.setEntity(dasa.getEntityLabel());
				    measurement.setCharacteristic(dasa.getAttributeLabel());
				}
				measurement.setEntityUri(dasa.getEntity());
				measurement.setDatasetUri(hadatacCcsv.getDatasetKbUri());
				try {
					solr.addBean(measurement);
				} catch (IOException | SolrServerException e) {
					System.out.println("[ERROR] SolrClient.addBean - e.Message: " + e.getMessage());
				}
				if((++total_count) % batch_size == 0){
					try {
						System.out.println("solr.commit()...");
						solr.commit();
						System.out.println(String.format("Committed %s measurements!", batch_size));
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
				System.out.println(String.format("Committed %s measurements!", total_count % batch_size));
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
		
		hadatacKb.getDataAcquisition().addNumberDataPoints(total_count);
		hadatacKb.getDataAcquisition().save();
		
		System.out.println("Finished indexMeasurements()");
		try {
			solr.close();
		} catch (IOException e) {
			System.out.println("[ERROR] SolrClient.close - e.Message: " + e.getMessage());
			message += "[ERROR] Fail to close solr\n";
		}
		return new ParsingResult(0, message);
	}
	
	private ParsingResult loadFromKb(int mode) {
		System.out.println("loadFromKb is called!");
		
		String message = "";
		hadatacKb = HADataC.find();
		hadatacKb.setDataAcquisition(DataAcquisition.find(hadatacCcsv));
		if (hadatacCcsv.getDataAcquisition().getStatus() > 0) {
			if (hadatacKb.getDataAcquisition() == null) {
				message += Feedback.println(mode, "[ERROR] Data Acquisition not found in the knowledge base.");
				return new ParsingResult(1, message);
			} 
			else {
				message += Feedback.println(mode, "[OK] Data Acquisition found on the knowledge base.");
			}
		} 
		else {
			if (hadatacKb.getDataAcquisition() != null) {
				message += Feedback.println(mode, "[ERROR] Data Acquisition already exists in the knowledge base.");
				return new ParsingResult(1, message);
			} 
			else {
				message += Feedback.println(mode, "[OK] Data Acquisition does not exist in the knowledge base.");
			}
		}
		
		// data acquisition schema
		String schemaUri = hadatacKb.getDataAcquisition().getSchemaUri();
		if (schemaUri == null || schemaUri.equals("")) {
		    message += Feedback.println(mode, "[ERROR] Data Acquisition Schema is not specified in the Data Acquisition.");
		} else {
		    schema = DataAcquisitionSchema.find(schemaUri);
		    if  (schema != null) {
			message += Feedback.println(mode, "[OK] Data Acquisition Schema " + schemaUri + " found in the knowledge base.");
		    } else {
			message += Feedback.println(mode, "[ERROR] Data Acquisition Schema " + schemaUri + " does not exist in the knowledge base.");
		    }
		}

		// dataset
		if (hadatacCcsv.getDataAcquisition().getStatus() > 0) {
			if (hadatacKb.getDataAcquisition().containsDataset(hadatacCcsv.getDatasetKbUri())) {
				message += Feedback.println(mode, "[ERROR] Dataset was already processed.");
			} 
			else {
				message += Feedback.println(mode, "[OK] Dataset is not already processed.");
			}
		}
		else {
			message += Feedback.println(mode, "[OK] Dataset is not already processed. This is a new Data Acquisition.");
		}
		Dataset dataset = new Dataset();
		dataset.setUri(hadatacCcsv.getDatasetKbUri());
		hadatacKb.setDataset(dataset);
		
		// deployment
		if (hadatacCcsv.getDataAcquisition().getStatus() > 0) {
			hadatacKb.setDeployment(Deployment.findFromDataAcquisition(hadatacKb));
		} else {
			hadatacKb.setDeployment(Deployment.findFromPreamble(hadatacCcsv));
		}
		if (hadatacKb.getDeployment() == null) {
			message += Feedback.println(mode, "[ERROR] Deployment is not defined in the knowledge base.");
		} else {
			message += Feedback.println(mode, "[OK] Deployment is defined in the knowledge base: <" + 
					hadatacKb.getDeployment().getLocalName() + ">");
			if (hadatacKb.getDeployment().getEndedAt() == null) {
				message += Feedback.println(mode, "[ERROR] Deployment is already finished at: " + 
					hadatacKb.getDeployment().getEndedAt() + "");
			} else {
				message += Feedback.println(mode, "[OK] Deployment is still open.");
			}
		}
		
		// measurement types
		//hadatacKb.getDataset().setMeasurementTypes(MeasurementType.find(hadatacCcsv));
		
		return new ParsingResult(0, message);
	}
	
	private ParsingResult loadFromPreamble(int mode, Model model) {
		String message = "";
		
		// load hadatac
		hadatacCcsv = HADataC.find(model);
		if (hadatacCcsv == null) {
			System.out.println("hadatacCcsv == null");
			message += Feedback.println(mode, "[ERROR] Preamble does not contain a single hadatac:KnowledgeBase.");
			return new ParsingResult(1, message);
		} else {
			System.out.println("[OK] Preamble contains a single hadatac:KnowledgeBase: <" + hadatacCcsv.getLocalName() + ">");
			message += Feedback.println(mode, "[OK] Preamble contains a single hadatac:KnowledgeBase: <" + hadatacCcsv.getLocalName() + ">");
		}
		
		// load dataset
		hadatacCcsv.setDataset(Dataset.find(model));
		if (hadatacCcsv.getDataset() == null) {
			message += Feedback.println(mode, "[ERROR] Preamble does not contain a single vstoi:Dataset.");
			return new ParsingResult(1, message);
		} else {
			System.out.println("[OK] Preamble contains a single vstoi:Dataset: <" + hadatacCcsv.getDataset().getLocalName() + ">");
			message += Feedback.println(mode, "[OK] Preamble contains a single vstoi:Dataset: <" + hadatacCcsv.getDataset().getLocalName() + ">");
		}
		
		// load datacollection
		hadatacCcsv.setDataAcquisition(DataAcquisition.find(model, hadatacCcsv.getDataset()));;
		if (hadatacCcsv.getDataAcquisition() == null) {
			message += Feedback.println(mode, "[ERROR] Preamble does not contain a single hasco:DataAcquisition.");
			return new ParsingResult(1, message);
		} 
		else {
			System.out.println("[OK] Preamble contains a single hasco:DataAcquisition: <" + 
								hadatacCcsv.getDataAcquisition().getLocalName() + ">");
			message += Feedback.println(mode, "[OK] Preamble contains a single hasco:DataAcquisition: <" + 
								hadatacCcsv.getDataAcquisition().getLocalName() + ">");
		}
		
		// deployment
		if (hadatacCcsv.getDataAcquisition().getStatus() == 0) {
			System.out.println("Deployment find");
			hadatacCcsv.setDeployment(Deployment.find(model, hadatacCcsv.getDataAcquisition()));
			if (hadatacCcsv.getDeployment() == null) {
				message += Feedback.println(mode, "[ERROR] This hasco:DataAcquisition requires a vstoi:Deployment that is not specified.");
				return new ParsingResult(1, message);
			} else {
				message += Feedback.println(mode, "[OK] This hasco:DataAcquisition requires a vstoi:Deployment that is specified: <" + 
						hadatacCcsv.getDeployment().getLocalName() + ">");
			}
		} else {
			message += Feedback.println(mode, "[OK] This hasco:DataAcquisition does not require a vstoi:Deployment in the preamble.");
		}
		
		// load measurement types
		//hadatacCcsv.getDataset().setMeasurementTypes(MeasurementType.find(model, hadatacCcsv.getDataset()));
		//if (hadatacCcsv.getDataset().getMeasurementTypes().isEmpty()) {
		//	System.out.println("Measurement is empty");
		//	message += Feedback.println(mode, "[ERROR] Preamble does not contain any well described measurement types.");
		//	return new ParsingResult(1, message);
		//} else {
		//	message += Feedback.print(mode, "[OK] Preamble contains the following well described measurement types: ");
		//	Iterator<MeasurementType> i = hadatacCcsv.getDataset().getMeasurementTypes().iterator();
		//	while (i.hasNext()) {
		//		message += Feedback.print(mode, "<" + i.next().getLocalName() + "> ");
		//	}
		//	message += Feedback.println(mode, "");
		//}
		
		return new ParsingResult(0, message);
	}
	
	private String getPreamble() throws IOException {
		BufferedReader br;
		String line;
		StringBuilder preamble = new StringBuilder();
		
		boolean inPreamble = false;
		boolean inCsv = false;
		
		files.openFile("csv", "w");
		br = files.getReader("ccsv");
		
		while ((line = br.readLine()) != null) {
			if (inCsv) {
				files.writeln("csv", line);
			}
			if (line.contains("== END-PREAMBLE ==")) {
				inPreamble = false;
				inCsv = true;
			}
			if (inPreamble) {
				preamble.append(line + "\n");
			}
			if (line.contains("== START-PREAMBLE ==")) {
				inPreamble = true;
			}
		}
		files.closeFile("csv", "w");
		
		return preamble.toString();
	}
}
