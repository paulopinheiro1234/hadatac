package org.hadatac.hadatac.loader.ccsv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.hadatac.hadatac.loader.entity.Characteristic;
import org.hadatac.hadatac.loader.entity.DataCollection;
import org.hadatac.hadatac.loader.entity.Dataset;
import org.hadatac.hadatac.loader.entity.Deployment;
import org.hadatac.hadatac.loader.entity.Entity;
import org.hadatac.hadatac.loader.entity.HADataC;
import org.hadatac.hadatac.loader.entity.Measurement;
import org.hadatac.hadatac.loader.entity.MeasurementType;
import org.hadatac.hadatac.loader.entity.Unit;
import org.hadatac.hadatac.loader.util.FileFactory;
import org.hadatac.hadatac.loader.util.SparqlQuery;

public class CCSVParser {
	
	private FileFactory files;
	
	private String getLabel(String uri) {
		String url = "http://localhost:7574/solr/store/sparql";
		try (QueryExecution qexec = QueryExecutionFactory.sparqlService(url, SparqlQuery.getLabelQuery(uri))) {
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			if (resultsrw.size() > 0) {
				QuerySolution soln = resultsrw.next();
				return soln.getLiteral("label").toString();
			} else {
				return ResourceFactory.createResource(uri).getLocalName();
			}
		}
	}
	
	private Resource getEntity(String uri) {
		String url = "http://localhost:7574/solr/store/sparql";
		try (QueryExecution qexec = QueryExecutionFactory.sparqlService(url, SparqlQuery.getEntityQuery(uri))) {
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			QuerySolution soln = resultsrw.next();
			return soln.getResource("entity");
		}
	}
	
	private String getPreamble() throws IOException {
		BufferedReader br;
		String line;
		StringBuilder preamble = new StringBuilder();
		
		boolean inPreamble = false;
		boolean inCsv = false;
		
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
		
		return preamble.toString();
	}
	
	public int parse(FileFactory files) throws IOException {
		
		Model model = ModelFactory.createDefaultModel();
		Query query;
		
		HADataC hadatac;
		Deployment deployment;
		DataCollection dataCollection;
		Dataset dataset;
		MeasurementType measurement;
		Characteristic characteristic;
		Unit unit;
		Entity entity;
		Measurement m;
		
		String preamble;
		String queryString;
		
		this.files = files;
		
		files.openFile("ccsv", "r");
		files.openFile("csv", "w");
		files.openFile("ttl", "w");
		
		preamble = getPreamble();
		
		model.read(new ByteArrayInputStream(preamble.getBytes()), null, "TTL");
		
		model.write(files.getWriter("ttl"), "TTL");
		
		files.closeFile("ccsv", "r");
		files.closeFile("csv", "w");
		files.closeFile("ttl", "w");
		
		// get HADataC info
		queryString = SparqlQuery.getHADataCQuery();
		query = QueryFactory.create(queryString);
		
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			QuerySolution soln = resultsrw.next();
			hadatac = new HADataC(soln.getResource("kb"));
			hadatac.setHostname(soln.getLiteral("url").getString());
		}
		
		// temporary deployment
		deployment = new Deployment(ResourceFactory.createResource("http://temp.org/kb#tempDeployment"));
		hadatac.setDeployment(deployment);
		
		// get Dataset info
		queryString = SparqlQuery.getDatasetQuery();
		query = QueryFactory.create(queryString);
		
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			QuerySolution soln = resultsrw.next();
			dataset = new Dataset(soln.getResource("ds"));
			hadatac.setDataset(dataset);
		}
		
		// get DataCollection info
		queryString = SparqlQuery.getDataCollectionQuery(hadatac.getDatasetPreambleURI());
		query = QueryFactory.create(queryString);
		
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			QuerySolution soln = resultsrw.next();
			dataCollection = new DataCollection(soln.getResource("dc"));
			hadatac.setDataCollection(dataCollection);
		}
		
		queryString = SparqlQuery.getMeasurementQuery();
		query = QueryFactory.create(queryString);
		
		int count = 0;
		
		SolrClient solrClient = new HttpSolrClient("http://localhost:8983/solr/measurement");
		
		System.out.println("uri,timestamp,value,unit,unit_uri,entity,entity_uri,characteristic,characteristic_uri,location,elevation,dataset_uri");
		
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			
			ResultSet results = qexec.execSelect();
			//ResultSetFormatter.outputAsCSV(results);
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			
			files.openFile("csv", "r");
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(files.getReader("csv"));
				
			for (CSVRecord record : records) {

				resultsrw.reset();
				for (; resultsrw.hasNext() ; ) {
					QuerySolution soln = resultsrw.next();
					
					// resources from preamble
					Resource measurementPreamble = soln.getResource("mt");
					Resource characteristicPreamble = soln.getResource("characteristic");
					Resource unitPreamble = soln.getResource("unit");
					
					//System.out.println("! before hasMeasurement");
					if (hadatac.hasMeasurement(measurementPreamble.getURI())) {
						measurement = hadatac.getMeasurement(measurementPreamble.getURI());
						characteristic = hadatac.getCharacteristic(characteristicPreamble.getURI());
						entity = hadatac.getEntityFromMeasurement(measurementPreamble.getURI());
						unit = hadatac.getUnit(unitPreamble.getURI());
					} else {
						measurement = new MeasurementType(measurementPreamble);
					
						//System.out.println("! before hasCharacteristic");
						if (hadatac.hasCharacteristic(characteristicPreamble.getURI())) {
							characteristic = hadatac.getCharacteristic(characteristicPreamble.getURI());
						} else {
							characteristic = new Characteristic(characteristicPreamble);
							characteristic.setLabel(getLabel(characteristic.getUri()));
						}
						
						//System.out.println("! before hasUnit");
						if (hadatac.hasUnit(unitPreamble.getURI())) {
							unit = hadatac.getUnit(unitPreamble.getURI());
						} else {
							unit = new Unit(unitPreamble);
							unit.setLabel(unit.getUri());
						}
						
						entity = new Entity(getEntity(characteristic.getUri()));
						entity.setLabel(getLabel(entity.getUri()));
						
						//System.out.println("! before addMeasurement");
						measurement.setCharacteristicURI(characteristic.getUri());
						measurement.setUnitURI(unit.getUri());
						measurement.setEntityURI(entity.getUri());
						hadatac.addMeasurement(measurement.getLocalName(), measurement);
					}
					
					m = new Measurement();
					m.setUri(hadatac.getMeasurementURI(measurement.getLocalName()) + "/" + count);
					if (soln.getLiteral("tsColumn") != null) { m.setTimestamp(record.get(soln.getLiteral("tsColumn").getInt())); }
					if (!record.get(soln.getLiteral("valueColumn").getInt()).equals("")) { m.setValue(Float.parseFloat(record.get(soln.getLiteral("valueColumn").getInt()))); }
					m.setUnit(unit.getLabel());
					m.setUnit_uri(unit.getUri());
					m.setEntity(entity.getLabel());
					m.setEntity_uri(entity.getUri());
					m.setCharacteristic(characteristic.getLabel());
					m.setCharacteristic_uri(characteristic.getUri());
					if (soln.getLiteral("ltColumn") != null) { m.setLatlong(record.get(soln.getLiteral("ltColumn").getInt())); }
					if (soln.getLiteral("dpColumn") != null) { m.setElevation(Float.parseFloat(record.get(soln.getLiteral("dpColumn").getInt()))); }
					m.setDataset_uri(soln.getResource("ds").getLocalName());
					
					/*
					System.out.print(hadatac.getMeasurementURI(measurement.getLocalName()) + "/" + count + ","); // uri
					if (soln.getLiteral("tsColumn") != null) { System.out.print(record.get(soln.getLiteral("tsColumn").getInt()) + ","); } else { System.out.print(","); } //timestamp
					System.out.print(record.get(soln.getLiteral("valueColumn").getInt()) + ","); //value
					System.out.print(unit.getLabel() + ","); //unit
					System.out.print(unit.getUri() + ","); //unit_uri
					System.out.print(entity.getLabel() + ","); //entity
					System.out.print(entity.getUri() + ","); //entity_uri
					System.out.print(characteristic.getLabel() + ","); //characteristic
					System.out.print(characteristic.getUri() + ","); //characteristic_uri
					if (soln.getLiteral("ltColumn") != null) { System.out.print("\"" + record.get(soln.getLiteral("ltColumn").getInt()) + "\","); } else { System.out.print(","); } //location
					if (soln.getLiteral("dpColumn") != null) { System.out.print(record.get(soln.getLiteral("dpColumn").getInt()) + ","); } else { System.out.print(","); } // elevation
					System.out.print(soln.getResource("ds").getLocalName()); //dataset_uri
                    System.out.println("");
                    */
                    
                    try {
            			solrClient.addBean(m);
            		} catch (Exception e) {
            			System.out.println("[ERROR] solrClient.addBean - Exception message: " + e.getMessage());
            		}
				}
				count++;
			}
			files.closeFile("csv", "r");
		}
		try {
			solrClient.commit();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] solrClient.commit - Exception message: " + e.getMessage());
		}
		solrClient.close();
		
		return 0;
	}
}