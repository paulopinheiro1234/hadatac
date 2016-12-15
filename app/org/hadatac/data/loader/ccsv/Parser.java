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
import org.hadatac.utils.Feedback;

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
	
	public int index(int mode) {
		// data collection
		DataAcquisition dataCollection = DataAcquisition.create(hadatacCcsv, hadatacKb);
		if (hadatacCcsv.dataCollection.getStatus() > 0) {
			hadatacKb.dataCollection.merge(dataCollection);
		} else {
			hadatacKb.dataCollection = dataCollection;
		}
		
		SolrClient solr = new HttpSolrClient(hadatacCcsv.getDynamicMetadataURL());
		System.out.println("hadatacKb.dataCollection.save(solr)...");
		hadatacKb.dataCollection.save(solr);
		try {
			solr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			indexMeasurements();
			System.out.println("solr.commit()...");
			solr.commit();
			solr.close();
		} catch (IOException | SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	
	private int indexMeasurements() throws IOException {
		System.out.println("indexMeasurements()...");
		
		files.openFile("csv", "r");
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(files.getReader("csv"));
		int count = 0;
		for (CSVRecord record : records) {
			Iterator<MeasurementType> i = hadatacKb.dataset.measurementTypes.iterator();
			while (i.hasNext()) {
				MeasurementType measurementType = i.next();
				Measurement measurement = new Measurement();
				measurement.setUri(hadatacCcsv.getMeasurementUri() + hadatacCcsv.dataset.getLocalName() + "/" + measurementType.getLocalName() + "-" + count);
				if (measurementType.getTimestampColumn() > -1) {
					System.out.println("measurementType.getTimestampColumn() > -1");
					measurement.setTimestampXsd(record.get(measurementType.getTimestampColumn()));
				}
				measurement.setOwnerUri(hadatacKb.dataCollection.getOwnerUri());
				measurement.setAcquisitionUri(hadatacKb.dataCollection.getUri());
				measurement.setPermissionUri(hadatacKb.dataCollection.getPermissionUri());
				//measurement.setValue(Double.parseDouble(record.get(measurementType.getValueColumn())));
				if(record.get(measurementType.getValueColumn() - 1).isEmpty()){
					//measurement.setValue("NO_VALUE");
					//System.out.println("NO_VALUE");
					continue;
				}
				else{
					measurement.setValue(record.get(measurementType.getValueColumn() - 1));
				}
				measurement.setUnit(measurementType.getUnitLabel());
				measurement.setUnitUri(measurementType.getUnitUri());
				measurement.setCharacteristic(measurementType.getCharacteristicLabel());
				measurement.setCharacteristicUri(measurementType.getCharacteristicUri());
				measurement.setInstrumentModel(hadatacKb.deployment.instrument.getLabel());
				measurement.setInstrumentUri(hadatacKb.deployment.instrument.getUri());
				measurement.setPlatformName(hadatacKb.deployment.platform.getLabel());
				measurement.setPlatformUri(hadatacKb.deployment.platform.getUri());
				measurement.setEntity(measurementType.getEntityLabel());
				measurement.setEntityUri(measurementType.getEntityUri());
				measurement.setDatasetUri(hadatacCcsv.getDatasetKbUri());
				measurement.save();
			}
			count++;
		}
		
		hadatacKb.dataCollection.addNumberDataPoints(count);
		hadatacKb.dataCollection.save();
		
		files.closeFile("csv", "r");
		System.out.println("Finished indexMeasurements()");
		return 0;
	}
	
	private DatasetParsingResult loadFromKb(int mode) {
		System.out.println("loadFromKb is called!");
		
		String message = "";
		hadatacKb = HADataC.find();
		hadatacKb.dataCollection = DataAcquisition.find(hadatacCcsv);
		if (hadatacCcsv.dataCollection.getStatus() > 0) {
			if (hadatacKb.dataCollection == null) {
				message += Feedback.println(mode, "[ERROR] Data Acquisition not found in the knowledge base.");
				return new DatasetParsingResult(1, message);
			} else {
				message += Feedback.println(mode, "[OK] Data Acquisition found on the knowledge base.");
			}
		} else {
			if (hadatacKb.dataCollection != null) {
				message += Feedback.println(mode, "[ERROR] Data Acquisition already exists in the knowledge base.");
				return new DatasetParsingResult(1, message);
			} else {
				message += Feedback.println(mode, "[OK] Data Acquisition does not exist in the knowledge base.");
			}
		}
		
		// dataset
		if (hadatacCcsv.dataCollection.getStatus() > 0) {
			System.out.println(hadatacCcsv.getDatasetKbUri());
			if (hadatacKb.dataCollection.containsDataset(hadatacCcsv.getDatasetKbUri())) {
				message += Feedback.println(mode, "[ERROR] Dataset was already processed.");
				return new DatasetParsingResult(1, message);
			} else {
				message += Feedback.println(mode, "[OK] Dataset is not already processed.");
				hadatacKb.dataset = new Dataset(); 
				hadatacKb.dataset.setUri(hadatacCcsv.getDatasetKbUri());
			}
		} else {
			message += Feedback.println(mode, "[OK] Dataset is not already processed. This is a new Data Acquisition.");
			hadatacKb.dataset = new Dataset(); 
			hadatacKb.dataset.setUri(hadatacCcsv.getDatasetKbUri());
		}
		
		// deployment
		if (hadatacCcsv.dataCollection.getStatus() > 0) {
			System.out.println("!! FIND FROM DC");
			hadatacKb.deployment = Deployment.findFromDataAcquisition(hadatacKb);
		} else {
			System.out.println("!! FIND FROM PREAMBLE");
			hadatacKb.deployment = Deployment.findFromPreamble(hadatacCcsv);
		}
		if (hadatacKb.deployment == null) {
			message += Feedback.println(mode, "[ERROR] Deployment is not defined in the knowledge base.");
		} else {
			message += Feedback.println(mode, "[OK] Deployment is defined in the knowledge base: <" + hadatacKb.deployment.getLocalName() + ">");
			if (hadatacKb.deployment.getEndedAt() == null) {
				message += Feedback.println(mode, "[ERROR] Deployment is already finished at: " + hadatacKb.deployment.getEndedAt() + "");
			} else {
				message += Feedback.println(mode, "[OK] Deployment is still open.");
			}
		}
		
		// measurement types
		hadatacKb.dataset.measurementTypes = MeasurementType.find(hadatacCcsv);
		
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
		hadatacCcsv.dataset = Dataset.find(model);
		if (hadatacCcsv.dataset == null) {
			message += Feedback.println(mode, "[ERROR] Preamble does not contain a single vstoi:Dataset.");
			return new DatasetParsingResult(1, message);
		} else {
			System.out.println("[OK] Preamble contains a single vstoi:Dataset: <" + hadatacCcsv.dataset.getLocalName() + ">");
			message += Feedback.println(mode, "[OK] Preamble contains a single vstoi:Dataset: <" + hadatacCcsv.dataset.getLocalName() + ">");
		}
		
		// load datacollection
		hadatacCcsv.dataCollection = DataAcquisition.find(model, hadatacCcsv.dataset);
		if (hadatacCcsv.dataCollection == null) {
			message += Feedback.println(mode, "[ERROR] Preamble does not contain a single hasneto:DataAcquisition.");
			return new DatasetParsingResult(1, message);
		} else {
			System.out.println("[OK] Preamble contains a single hasneto:DataAcquisition: <" + hadatacCcsv.dataCollection.getLocalName() + ">");
			message += Feedback.println(mode, "[OK] Preamble contains a single hasneto:DataAcquisition: <" + hadatacCcsv.dataCollection.getLocalName() + ">");
		}
		
		// deployment
		if (hadatacCcsv.dataCollection.getStatus() == 0) {
			System.out.println("Deployment find");
			hadatacCcsv.deployment = Deployment.find(model, hadatacCcsv.dataCollection);
			if (hadatacCcsv.deployment == null) {
				message += Feedback.println(mode, "[ERROR] This hasneto:DataAcquisition requires a vstoi:Deployment that is not specified.");
				return new DatasetParsingResult(1, message);
			} else {
				message += Feedback.println(mode, "[OK] This hasneto:DataAcquisition requires a vstoi:Deployment that is specified: <" + hadatacCcsv.deployment.getLocalName() + ">");
			}
		} else {
			message += Feedback.println(mode, "[OK] This hasneto:DataAcquisition does not require a vstoi:Deployment in the preamble.");
		}
		
		// load measurement types
		hadatacCcsv.dataset.measurementTypes = MeasurementType.find(model, hadatacCcsv.dataset);
		if (hadatacCcsv.dataset.measurementTypes.isEmpty()) {
			System.out.println("Measurement is empty");
			message += Feedback.println(mode, "[ERROR] Preamble does not contain any well described measurement types.");
			return new DatasetParsingResult(1, message);
		} else {
			message += Feedback.print(mode, "[OK] Preamble contains the following well described measurement types: ");
			Iterator<MeasurementType> i = hadatacCcsv.dataset.measurementTypes.iterator();
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
