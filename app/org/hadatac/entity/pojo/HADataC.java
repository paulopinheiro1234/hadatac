package org.hadatac.entity.pojo;

import org.hadatac.data.loader.util.Sparql;

import play.Play;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;

public class HADataC {
	
	private String uri;
	private String localName;
	private String host;
	
	public Dataset dataset;
	public DataCollection dataCollection;
	public Deployment deployment;
	
	public HADataC () {
		dataset = null;
		dataCollection = null;
		deployment = null;
	}
	
	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public static HADataC find() {
		HADataC hadatac = new HADataC();
		hadatac.setHost("http://localhost");
		hadatac.setLocalName("kb");
		return hadatac;
	}
	
	public static HADataC find(Model model) {
		String queryString = Sparql.prefix
				+ "SELECT ?kb ?host WHERE {\n"
				+ "  ?kb a hadatac:KnowledgeBase .\n"
				+ "  ?kb hadatac:hasHost ?host .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() == 1) {
			QuerySolution soln = resultsrw.next();
			HADataC hadatac = new HADataC();
			hadatac.setLocalName(soln.getResource("kb").getLocalName());
			hadatac.setHost(soln.getLiteral("host").getString());
			return hadatac;
		}
		
		return null;
	}
	
	public String getUri() {
		return host + "/hadatac";
	}
	
	public String getDatasetKbUri() {
		//return host + "/hadatac/" + deployment.getLocalName() + "/" + dataCollection.getLocalName() + "/" + dataset.getLocalName();
		return host + "/hadatac/dataset/" + dataset.getLocalName();
	}
	
	public String getDataCollectionKbUri() {
		//return host + "/hadatac/" + deployment.getLocalName() + "/" + dataCollection.getLocalName();
		return host + "/hadatac/datacollection/" + dataCollection.getLocalName();
	}
	
	public String getDeploymentUri() {
		//return host + "/hadatac/" + deployment.getLocalName();
		return host + "/hadatac/deployment/" + deployment.getLocalName();
	}
	
	public String getMeasurementUri() {
		//return host + "/hadatac/" + deployment.getLocalName();
		return host + "/hadatac/measurement/";
	}
	
	/*
	public String getMeasurementURI(String uri) {
		return host + "/hadatac/" + deployment.getLocalName() + "/" + dataCollection.getLocalName() + "/" + dataset.getLocalName() + "/" + measurements.get(uri).getLocalName();
	}
	
	public String getDatasetKbURI() {
		return host + "/hadatac/" + deployment.getLocalName() + "/" + dataCollection.getLocalName() + "/" + dataset.getLocalName();
	}
	
	public String getDatasetPreambleURI() {
		return dataset.getURI();
	}
	
	public String getDataCollectionKbURI() {
		return hostname + "/hadatac/" + deployment.getLocalName() + "/" + dataCollection.getLocalName();
	}
	
	public String getDataCollectionPreambleURI() {
		return dataCollection.getURI();
	}
	
	public String getDeploymentKbURI() {
		return hostname + "/hadatac/" + deployment.getLocalName();
	}
	
	public String getDeploymentPreambleURI() {
		return deployment.getURI();
	}
	*/
	
	public String getStaticMetadataSparqlURL() {
		return host + ":7574/solr/store/sparql";
	}
	
	public String getDynamicMetadataURL() {
		return host + ":8983/solr/sdc";
	}
	
	public String getDynamicMetadataSelectURL() {
		return host + ":8983/solr/sdc/select";
	}
	
	public String getDynamicMetadataUpdateURL() {
		return host + ":8983/solr/sdc/update";
	}
	
	public String getMeasurementUpdateURL() {
		return host + ":8983/solr/measurement/update";
	}
	
	public String getMeasurementURL() {
		return host + ":8983/solr/measurement";
	}
}