package org.hadatac.data.loader.ccsv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.hadatac.data.loader.util.FileFactory;
import org.hadatac.data.model.DatasetParsingResult;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Dataset;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.HADataC;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.MeasurementType;
import org.hadatac.entity.pojo.Subject;
import org.hadatac.utils.Feedback;

import play.Play;

public class Parser {
	
	private FileFactory files;
	
	private HADataC hadatacCcsv;
	private HADataC hadatacKb;
	
	public Parser() {
		hadatacCcsv = null;
		hadatacKb = null;
	}
	
	public DatasetParsingResult validate(int mode, FileFactory files) throws IOException {
		DatasetParsingResult result;
		String message = "";
		Model model;
		String preamble;
		
		this.files = files;
		files.openFile("ccsv", "r");
		
		preamble = getPreamble();
		model = ModelFactory.createDefaultModel();
		model.read(new ByteArrayInputStream(preamble.getBytes()), null, "TTL");
		
		// -- START verify if model is successfully loaded
		if (model.isEmpty()) {
			message += Feedback.println(mode, "[ERROR] Preamble not a well-formed Turtle.");
			System.out.println("[ERROR] Preamble not a well-formed Turtle.");
		} 
		else {
			message += Feedback.println(mode, "[OK] Preamble a well-formed Turtle.");
			System.out.println("[OK] Preamble a well-formed Turtle.");
		}
		// -- END verify if model is successfully loaded
		
		result = loadFromPreamble(mode, model);
		
		if (result.getStatus() == 0) {
			message += result.getMessage();
			result = loadFromKb(mode);
			System.out.println("loadFromKb(mode): " + result.getStatus());
			System.out.println("loadFromKb(mode) message: " + result.getMessage());
			message += result.getMessage();
		}
		
		files.closeFile("ccsv", "r");
		
		return new DatasetParsingResult(result.getStatus(), message);
	}
	
	public DatasetParsingResult index(int mode) {
		DataAcquisition dataAcquisition = DataAcquisition.create(hadatacCcsv, hadatacKb);
		if (hadatacCcsv.getDataAcquisition().getStatus() > 0) {
			hadatacKb.getDataAcquisition().merge(dataAcquisition);
		} 
		else {
			hadatacKb.setDataAcquisition(dataAcquisition);
		}
		
		System.out.println("hadatacKb.dataCollection.save(solr)...");
		hadatacKb.getDataAcquisition().save();
		DatasetParsingResult result = indexMeasurements();

		return new DatasetParsingResult(result.getStatus(), result.getMessage());
	}
	
	private DatasetParsingResult indexMeasurements(){
		System.out.println("indexMeasurements()...");
		String message = "";
		
		try {
			files.openFile("csv", "r");
		} catch (IOException e) {
			e.printStackTrace();
			message += "[ERROR] Fail to open the csv file\n";
			return new DatasetParsingResult(1, message);
		}
		Iterable<CSVRecord> records = null;
		try {
			records = CSVFormat.DEFAULT.withHeader().parse(files.getReader("csv"));
		} catch (IOException e) {
			e.printStackTrace();
			message += "[ERROR] Fail to parse header of the csv file\n";
			return new DatasetParsingResult(1, message);
		}
		int total_count = 0;
		int batch_size = 10000;
		int nTimeStampCol = 0;
		int nIdCol = 0;
		for(MeasurementType mt : hadatacKb.getDataset().getMeasurementTypes()){
			if(mt.getTimestampColumn() > -1){
				nTimeStampCol = mt.getTimestampColumn();
			}
			if(mt.getIdColumn() > -1){
				nIdCol = mt.getIdColumn();
			}
		}
		SolrClient solr = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/measurement");
		for (CSVRecord record : records) {
			Iterator<MeasurementType> iter = hadatacKb.getDataset().getMeasurementTypes().iterator();
			while (iter.hasNext()) {
				MeasurementType measurementType = iter.next();
				Measurement measurement = new Measurement();
				if(record.get(measurementType.getValueColumn() - 1).isEmpty()){
					continue;
				}
				else{
					measurement.setValue(record.get(measurementType.getValueColumn() - 1));
				}
				if (measurementType.getTimestampColumn() > -1) {
					continue;
				}
				if (measurementType.getIdColumn() > -1) {
					continue;
				}
				measurement.setTimestamp(record.get(nTimeStampCol - 1));
				measurement.setUri(hadatacCcsv.getMeasurementUri() + hadatacCcsv.getDataset().getLocalName() + "/" + measurementType.getLocalName() + "-" + total_count);
				measurement.setOwnerUri(hadatacKb.getDataAcquisition().getOwnerUri());
				measurement.setAcquisitionUri(hadatacKb.getDataAcquisition().getUri());
				measurement.setStudyUri(hadatacKb.getDataAcquisition().getStudyUri());
				measurement.setObjectUri(Subject.find(measurement.getStudyUri(), record.get(nIdCol - 1)).getUri());
				measurement.setUnit(measurementType.getUnitLabel());
				measurement.setUnitUri(measurementType.getUnitUri());
				measurement.setCharacteristic(measurementType.getCharacteristicLabel());
				measurement.setCharacteristicUri(measurementType.getCharacteristicUri());
				measurement.setInstrumentModel(hadatacKb.getDeployment().instrument.getLabel());
				measurement.setInstrumentUri(hadatacKb.getDeployment().instrument.getUri());
				measurement.setPlatformName(hadatacKb.getDeployment().platform.getLabel());
				measurement.setPlatformUri(hadatacKb.getDeployment().platform.getUri());
				measurement.setEntity(measurementType.getEntityLabel());
				measurement.setEntityUri(measurementType.getEntityUri());
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
						return new DatasetParsingResult(1, message);
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
				System.out.println("[ERROR] SolrClient.commit - e.Message: " + e.getMessage());
				message += "[ERROR] Fail to commit to solr\n";
				return new DatasetParsingResult(1, message);
			}
			solr.close();
			files.closeFile("csv", "r");
		} catch (IOException e) {
			e.printStackTrace();
			message += "[ERROR] Fail to close the csv file\n";
			return new DatasetParsingResult(1, message);
		}
		
		hadatacKb.getDataAcquisition().addNumberDataPoints(total_count);
		hadatacKb.getDataAcquisition().save();
		
		System.out.println("Finished indexMeasurements()");
		return new DatasetParsingResult(0, message);
	}
	
	private DatasetParsingResult loadFromKb(int mode) {
		System.out.println("loadFromKb is called!");
		
		String message = "";
		hadatacKb = HADataC.find();
		hadatacKb.setDataAcquisition(DataAcquisition.find(hadatacCcsv));
		if (hadatacCcsv.getDataAcquisition().getStatus() > 0) {
			if (hadatacKb.getDataAcquisition() == null) {
				message += Feedback.println(mode, "[ERROR] Data Acquisition not found in the knowledge base.");
				return new DatasetParsingResult(1, message);
			} 
			else {
				message += Feedback.println(mode, "[OK] Data Acquisition found on the knowledge base.");
			}
		} 
		else {
			if (hadatacKb.getDataAcquisition() != null) {
				message += Feedback.println(mode, "[ERROR] Data Acquisition already exists in the knowledge base.");
				return new DatasetParsingResult(1, message);
			} 
			else {
				message += Feedback.println(mode, "[OK] Data Acquisition does not exist in the knowledge base.");
			}
		}
		
		// dataset
		if (hadatacCcsv.getDataAcquisition().getStatus() > 0) {
			System.out.println(hadatacCcsv.getDatasetKbUri());
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
			System.out.println("!! FIND FROM DC");
			hadatacKb.setDeployment(Deployment.findFromDataAcquisition(hadatacKb));
		} else {
			System.out.println("!! FIND FROM PREAMBLE");
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
		hadatacKb.getDataset().setMeasurementTypes(MeasurementType.find(hadatacCcsv));
		
		return new DatasetParsingResult(0, message);
	}
	
	private DatasetParsingResult loadFromPreamble(int mode, Model model) {
		String message = "";
		
		// load hadatac
		hadatacCcsv = HADataC.find(model);
		if (hadatacCcsv == null) {
			System.out.println("hadatacCcsv == null");
			message += Feedback.println(mode, "[ERROR] Preamble does not contain a single hadatac:KnowledgeBase.");
			return new DatasetParsingResult(1, message);
		} else {
			System.out.println("[OK] Preamble contains a single hadatac:KnowledgeBase: <" + hadatacCcsv.getLocalName() + ">");
			message += Feedback.println(mode, "[OK] Preamble contains a single hadatac:KnowledgeBase: <" + hadatacCcsv.getLocalName() + ">");
		}
		
		// load dataset
		hadatacCcsv.setDataset(Dataset.find(model));
		if (hadatacCcsv.getDataset() == null) {
			message += Feedback.println(mode, "[ERROR] Preamble does not contain a single vstoi:Dataset.");
			return new DatasetParsingResult(1, message);
		} else {
			System.out.println("[OK] Preamble contains a single vstoi:Dataset: <" + hadatacCcsv.getDataset().getLocalName() + ">");
			message += Feedback.println(mode, "[OK] Preamble contains a single vstoi:Dataset: <" + hadatacCcsv.getDataset().getLocalName() + ">");
		}
		
		// load datacollection
		hadatacCcsv.setDataAcquisition(DataAcquisition.find(model, hadatacCcsv.getDataset()));;
		if (hadatacCcsv.getDataAcquisition() == null) {
			message += Feedback.println(mode, "[ERROR] Preamble does not contain a single hasneto:DataAcquisition.");
			return new DatasetParsingResult(1, message);
		} 
		else {
			System.out.println("[OK] Preamble contains a single hasneto:DataAcquisition: <" + 
								hadatacCcsv.getDataAcquisition().getLocalName() + ">");
			message += Feedback.println(mode, "[OK] Preamble contains a single hasneto:DataAcquisition: <" + 
								hadatacCcsv.getDataAcquisition().getLocalName() + ">");
		}
		
		// deployment
		if (hadatacCcsv.getDataAcquisition().getStatus() == 0) {
			System.out.println("Deployment find");
			hadatacCcsv.setDeployment(Deployment.find(model, hadatacCcsv.getDataAcquisition()));
			if (hadatacCcsv.getDeployment() == null) {
				message += Feedback.println(mode, "[ERROR] This hasneto:DataAcquisition requires a vstoi:Deployment that is not specified.");
				return new DatasetParsingResult(1, message);
			} else {
				message += Feedback.println(mode, "[OK] This hasneto:DataAcquisition requires a vstoi:Deployment that is specified: <" + 
						hadatacCcsv.getDeployment().getLocalName() + ">");
			}
		} else {
			message += Feedback.println(mode, "[OK] This hasneto:DataAcquisition does not require a vstoi:Deployment in the preamble.");
		}
		
		// load measurement types
		hadatacCcsv.getDataset().setMeasurementTypes(MeasurementType.find(model, hadatacCcsv.getDataset()));
		if (hadatacCcsv.getDataset().getMeasurementTypes().isEmpty()) {
			System.out.println("Measurement is empty");
			message += Feedback.println(mode, "[ERROR] Preamble does not contain any well described measurement types.");
			return new DatasetParsingResult(1, message);
		} else {
			message += Feedback.print(mode, "[OK] Preamble contains the following well described measurement types: ");
			Iterator<MeasurementType> i = hadatacCcsv.getDataset().getMeasurementTypes().iterator();
			while (i.hasNext()) {
				message += Feedback.print(mode, "<" + i.next().getLocalName() + "> ");
			}
			message += Feedback.println(mode, "");
		}
		
		return new DatasetParsingResult(0, message);
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
