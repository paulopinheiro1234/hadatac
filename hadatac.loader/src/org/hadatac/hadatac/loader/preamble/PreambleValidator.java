package org.hadatac.hadatac.loader.preamble;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.hadatac.loader.util.FileFactory;

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

public class PreambleValidator {
	/*
	private static String getPreamble(File ccsvFile) throws IOException {
		BufferedReader br;
		String line;
		StringBuilder preamble = new StringBuilder();
		boolean inPreamble = false;
		
		fileFactory.openFile("ccsv", "r");
		
		br = fileFactory.getReader("ccsv");
		
		while ((line = br.readLine()) != null) {
			if (line.contains("== END-PREAMBLE ==")) {
				inPreamble = false;
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
	
	private static void validate(FileFactory files) throws IOException {
		ArrayList<Resource> characteristics = new ArrayList<Resource>();
		Resource deployment = null;
		Resource edc = null;
		ResultSet results;
		ResultSetRewindable resultsrw;
		String queryString;
		Model model = ModelFactory.createDefaultModel();
		Model modelMt;
		Query query;
		SolrClient solrClient = null;
		SolrQuery solrQuery = null;
		QueryResponse queryResponse = null;
		String url;
		String prefix =		"PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vstoi#> "
						+ 	"PREFIX oboe: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#> "
						+ 	"PREFIX ccsv: <http://jefferson.tw.rpi.edu/ontology/ccsv#> "
						+ 	"PREFIX hasneto: <http://jefferson.tw.rpi.edu/ontology/hasneto#> ";
		
		String preamble = getPreamble(ccsvFile);
		model.read(new ByteArrayInputStream(preamble.getBytes()), null, "TTL");
		
		// -- START verify if model is successfully loaded
		if (model.isEmpty()) {
			System.out.println("[ERROR] Preamble not a well-formed Turtle.");
			return;
		} else {
			System.out.println("[OK] Preamble a well-formed Turtle.");
		}
		// -- END verify if model is successfully loaded
		
		// -- START has exactly one dataset
		queryString = prefix
					+ "SELECT ?ds WHERE {"
					+ "  ?ds a vstoi:Dataset ."
					+ "}";
		query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			results = qexec.execSelect();
			resultsrw = ResultSetFactory.copyResults(results);
		} catch (Exception e) {
			System.out.println("[ERROR] Error on querying for dataset - Exception message: " + e.getMessage());
			return;
		}
		
		if (resultsrw.size() == 1) { // success
			System.out.println("[OK] Preamble contains exactly one dataset.");
		} else { // fail
			System.out.println("[ERROR] Preamble contains " + resultsrw.size() + " datasets. Must contain exactly one.");
			return;
		}
		// -- END has exactly one dataset
		
		// -- START must have at least one measurementType
		queryString = prefix
					+ "SELECT ?mt WHERE {"
					+ "  ?ds a vstoi:Dataset ."
					+ "  ?ds hasneto:hasMeasurementType ?mt ."
					+ "}";
		query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			results = qexec.execSelect();
			resultsrw = ResultSetFactory.copyResults(results);
		} catch (Exception e) {
			System.out.println("[ERROR] Error on querying for measurements - Exception message: " + e.getMessage());
		}
		
		if (resultsrw.size() == 0) { // fail
			System.out.println("[ERROR] Dataset does not contain any measurements.");
			return;
		} else { // success
			System.out.println("[OK] Dataset contains " + resultsrw.size()  + " measurement(s). Checking for descriptions of each...");
		}
		// -- END must have at least one measurementType 
		
		// -- START must have every measurement type described
		resultsrw.reset();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			queryString = "DESCRIBE <" + soln.getResource("mt").getURI() + ">";
			query = QueryFactory.create(queryString);
			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
				modelMt = qexec.execDescribe();
			} catch (Exception e) {
				System.out.println("[ERROR] Error on describing measurement <" + soln.getResource("mt").getLocalName() + "> - Exception message: " + e.getMessage());
				return;
			}
			
			if (modelMt.size() == 0) { // fail
				System.out.println("[ERROR] Measurement <" + soln.getResource("mt").getLocalName()  + "> referenced but not defined.");
				return;
			} else { // success
				queryString = prefix
						+ "SELECT ?mt WHERE { "
						+ "  ?mt a oboe:Measurement . "
						+ "}";
				query = QueryFactory.create(queryString);
				try (QueryExecution qexec = QueryExecutionFactory.create(query, modelMt)) {
					ResultSet results2 = qexec.execSelect();
					ResultSetRewindable resultsrw2 = ResultSetFactory.copyResults(results2);
					
					if (resultsrw2.size() == 0) { // fail
						System.out.println("[ERROR] Measurement <" + soln.getResource("mt").getLocalName()  + "> referenced and defined, but not as a oboe:Measurement.");
						return;
					} else { // success
						System.out.println("[OK] Measurement <" + soln.getResource("mt").getLocalName()  + "> referenced and defined as a oboe:Measurement.");
					}
				} catch (Exception e) { 
					System.out.println("[ERROR] Error on querying measurement <" + soln.getResource("mt").getLocalName() + "> - Exception message: " + e.getMessage());
					return;
				}
				
				// -- START every measurement must have exactly one characteristic
				queryString = prefix
						+ "SELECT ?char WHERE { "
						+ "  ?mt a oboe:Measurement . "
						+ "  ?mt oboe:ofCharacteristic ?char . "
						+ "}";
				query = QueryFactory.create(queryString);
				try (QueryExecution qexec = QueryExecutionFactory.create(query, modelMt)) {
					ResultSet results2 = qexec.execSelect();
					ResultSetRewindable resultsrw2 = ResultSetFactory.copyResults(results2);
					
					if (resultsrw2.size() != 1) { // fail
						System.out.println("[ERROR] Measurement <" + soln.getResource("mt").getLocalName()  + "> has " + resultsrw2.size() + " characteristic(s), but it should have exactly one.");
						return;
					} else { // success
						characteristics.add(resultsrw2.next().getResource("char"));
						System.out.println("[OK] Measurement <" + soln.getResource("mt").getLocalName()  + "> has exactly one characteristic.");
					}
				} catch (Exception e) {
					System.out.println("[ERROR] Error on querying measurement <" + soln.getResource("mt").getLocalName() + "> for its characteristic - Exception message: " + e.getMessage());
					return;
				}
				// -- END every measurement must have exactly one characteristic
			}
		}
		// -- END must have every measurement type described
		
		// -- START must have exactly one Empirical Data Collection (EDC)
		queryString = prefix
				+ "SELECT ?edc WHERE {"
				+ "  ?edc a hasneto:EDC ."
				+ "}";
		query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			results = qexec.execSelect();
			resultsrw = ResultSetFactory.copyResults(results);
		} catch (Exception e) {
			System.out.println("[ERROR] Error on querying for Empirical Data Collections - Exception message: " + e.getMessage());
		}
		if (resultsrw.size() != 1) { // fail
			System.out.println("[ERROR] Preamble contains " + resultsrw.size() + " Empirical Data Collection(s), but it should contain exactly one.");
			return;
		} else { // success
			edc = resultsrw.next().getResource("edc").asResource();
			System.out.println("[OK] Preamble contains exactly one Empirical Data Collection.");
		}
		// -- END must have exactly one Empirical Data Collection (EDC)
		
		// -- START must have exactly one knowledge base source
		queryString = prefix
				+ "SELECT ?kb ?url WHERE {"
				+ "  ?kb a ccsv:KnowledgeBase ."
				+ "  ?kb ccsv:hasConnectionURL ?url ."
				+ "}";
		query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			results = qexec.execSelect();
			resultsrw = ResultSetFactory.copyResults(results);
			if (resultsrw.size() != 1) { // fail
				System.out.println("[ERROR] Preamble contains " + resultsrw.size() + " knowledge base(s), but it should contain exactly one.");
				return;
			} else { // success
				url = resultsrw.next().getLiteral("url").asLiteral().getString();
				System.out.println("[OK] Preamble contains exactly one knowledge base. URL: " + url);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] Error on querying for knowledge base source - Exception message: " + e.getMessage());
			return;
		}
		// -- END must have exactly one knowledge base source
		
		// -- START query knowledge base for each characteristic
		Iterator<Resource> i = characteristics.iterator();
		while (i.hasNext()) {
			Resource characteristic = i.next();
			try (QueryExecution qexec = QueryExecutionFactory.sparqlService(url, prefix + " SELECT ?ent WHERE { ?ent oboe:hasCharacteristic <" + characteristic.getURI() + "> }")) {
				results = qexec.execSelect();
				resultsrw = ResultSetFactory.copyResults(results);
				//ResultSetFormatter.outputAsCSV(results);
				if (resultsrw.size() == 1) {
					System.out.println("[OK] Characteristic " + characteristic.getLocalName() + " is associated to the entity " + resultsrw.next().get("ent").asResource().getLocalName() + " in the knowledge base.");
				} else {
					System.out.println("[ERROR] Characteristic " + characteristic.getLocalName() + " does not have an unique associated entity in the knowledge base.");
				}
			} catch (Exception e) {
				System.out.println("[ERROR] Error on connection to the knowledge base looking for characteristics - Exception message: " + e.getMessage());
			}
		}
		// -- END query knowledge base for each characteristic
		
		// -- START check EDC status
		solrClient = new HttpSolrClient("https://jeffersonsecure.tw.rpi.edu/solr/sdc");
		solrQuery = new SolrQuery();
		solrQuery.add("q", "uri:" + edc.getURI());
		try {
			queryResponse = solrClient.query(solrQuery);
			solrClient.close();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Error on connection to the scientific data collection on SOLR - Exception message: " + e.getMessage());
			solrClient.close();
			return;
		}
		SolrDocumentList list = queryResponse.getResults();
		if (list.isEmpty()) { // EDC is new, so the preamble must contain deployment information
		}
		// -- END check EDC status
		
		// -- START check preamble for Deployment info
		queryString = prefix
				+ "SELECT ?dp WHERE {"
				+ "  ?dp a vstoi:Deployment ."
				+ "}";
		query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			results = qexec.execSelect();
			resultsrw = ResultSetFactory.copyResults(results);
		} catch (Exception e) {
			System.out.println("[ERROR] Error on querying preamble for Deployment - Exception message: " + e.getMessage());
		}
		if (resultsrw.size() == 1) { // success
			deployment = resultsrw.next().get("dp").asResource();
			System.out.println("[OK] Preamble contains deployment info <" + deployment.getLocalName() + ">. Checking knowledge base for it...");
		} else if (resultsrw.size() == 0) {
			System.out.println("[OK] Preamble does not contain any deployment information. Checking knowledge base for it...");
		} else {
			System.out.println("[ERROR] Preamble contains " + resultsrw.size() + " deployments, but an EDC can only refer to a single deployment.");
			return;
		}
		// -- END check preamble for Deployment info
		
		// -- START check knowledge base for Deployment info
		if (deployment != null) {
			try (QueryExecution qexec = QueryExecutionFactory.sparqlService(url, prefix + " SELECT ?type WHERE { <" + deployment.getURI() + "> a ?type . }")) {
				results = qexec.execSelect();
				resultsrw = ResultSetFactory.copyResults(results);
				//ResultSetFormatter.outputAsCSV(results);
				if (resultsrw.size() == 1) {
					System.out.println("[ERROR] Deployment " + deployment.getLocalName() + " is defined both in the preamble and knowledge base.");
					return;
				} else {
					System.out.println("[OK] Deployment <" + deployment.getLocalName() + "> is a new deployment defined only in the preamble.");
				}
			} catch (Exception e) {
				System.out.println("[ERROR] Error on connection to the knowledge base looking for deployments - Exception message: " + e.getMessage());
			}
		} else {
			
		}
		// -- END check knowledge base for Deployment info
	}
	*/
}
