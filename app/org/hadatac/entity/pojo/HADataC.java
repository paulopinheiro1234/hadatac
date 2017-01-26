package org.hadatac.entity.pojo;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class HADataC {
	
	private String localName;
	private String host;
	
	private Dataset dataset;
	private DataAcquisition dataAcquisition;
	private Deployment deployment;
	
	public HADataC () {
		dataset = null;
		dataAcquisition = null;
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
	
	public Dataset getDataset() {
		return dataset;
	}
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
	public DataAcquisition getDataAcquisition() {
		return dataAcquisition;
	}
	public void setDataAcquisition(DataAcquisition dataAcquisition) {
		this.dataAcquisition = dataAcquisition;
	}

	public Deployment getDeployment() {
		return deployment;
	}
	public void setDeployment(Deployment deployment) {
		this.deployment = deployment;
	}
	
	public static HADataC find() {
		HADataC hadatac = new HADataC();
		hadatac.setHost("http://localhost");
		hadatac.setLocalName("kb");
		return hadatac;
	}
	
	public static HADataC find(Model model) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT ?kb ?host WHERE {\n"
				+ "  ?kb a hadatac:KnowledgeBase .\n"
				+ "  ?kb hadatac:hasHost ?host .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() >= 1) {
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
		return dataset.getCcsvUri();
	}
	
	public String getDataAcquisitionKbUri() {
		return dataAcquisition.getCcsvUri();
	}
	
	public String getDeploymentUri() {
		return deployment.getUri();
	}
	
	public String getMeasurementUri() {
		return host + "/hadatac/measurement/";
	}
	
	public String getStaticMetadataSparqlURL() {
		return Collections.getCollectionsName(Collections.METADATA_SPARQL);
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