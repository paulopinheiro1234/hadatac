package org.hadatac.entity.pojo;

import org.hadatac.data.loader.util.Sparql;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class Deployment {

	private String uri;
	private String localName;
	private String ccsvUri;
	private DateTime startedAt;
	private DateTime endedAt;
	
	public Instrument instrument;
	public Platform platform;
	
	public Deployment() {
		startedAt = null;
		endedAt = null;
		instrument = null;
		platform = null;
	}
	
	public String getStartedAt() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(startedAt);
	}

	public void setStartedAt(String startedAt) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	public void setStartedAtXsd(String startedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	public String getEndedAt() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(endedAt);
	}

	public void setEndedAt(String endedAt) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	public void setEndedAtXsd(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.startedAt = formatter.parseDateTime(endedAt);
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public String getCcsvUri() {
		return ccsvUri;
	}

	public void setCcsvUri(String ccsvUri) {
		this.ccsvUri = ccsvUri;
	}
	
	public static Deployment findFromDataCollection(HADataC hadatac) {
		String queryString = Sparql.prefix
				+ "SELECT ?startedAt ?endedAt ?detector ?instrument ?platform WHERE {\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> a vstoi:Deployment .\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> prov:startedAtTime ?startedAt .\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> hasneto:hasDetector ?detector .\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> hasneto:hasInstrument ?instrument .\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> vstoi:hasPlatform ?platform .\n"
				+ "  OPTIONAL { <" + hadatac.dataCollection.getDeploymentUri() + "> prov:endedAtTime ?endedAt . }\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() == 1) {
			QuerySolution soln = resultsrw.next();
			Deployment deployment = new Deployment();
			Resource resource = ResourceFactory.createResource(hadatac.dataCollection.getDeploymentUri());
			deployment.setLocalName(resource.getLocalName());
			deployment.setUri(hadatac.dataCollection.getDeploymentUri());
			deployment.setStartedAtXsd(soln.getLiteral("startedAt").getString());
			if (soln.getLiteral("endedAt") != null) { deployment.setEndedAtXsd(soln.getLiteral("endedAt").getString()); }
			hadatac.deployment = deployment;
			deployment.platform = Platform.find(hadatac);
			deployment.instrument = Instrument.find(hadatac);
			
			return deployment;
		}
		
		return null;
	}
	
	public static Deployment findFromPreamble(HADataC hadatac) {
		String queryString = Sparql.prefix
				+ "SELECT ?startedAt ?endedAt ?detector ?instrument ?platform WHERE {\n"
				+ "  <" + hadatac.getDeploymentUri() + "> a vstoi:Deployment .\n"
				+ "  <" + hadatac.getDeploymentUri() + "> prov:startedAtTime ?startedAt .\n"
				+ "  <" + hadatac.getDeploymentUri() + "> hasneto:hasDetector ?detector .\n"
				+ "  <" + hadatac.getDeploymentUri() + "> hasneto:hasInstrument ?instrument .\n"
				+ "  <" + hadatac.getDeploymentUri() + "> vstoi:hasPlatform ?platform .\n"
				+ "  OPTIONAL { <" + hadatac.getDeploymentUri() + "> prov:endedAtTime ?endedAt . }\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() == 1) {
			QuerySolution soln = resultsrw.next();
			Deployment deployment = new Deployment();
			deployment.setLocalName(hadatac.deployment.getLocalName());
			deployment.setUri(hadatac.getDeploymentUri());
			deployment.setStartedAtXsd(soln.getLiteral("startedAt").getString());
			if (soln.getLiteral("endedAt") != null) { deployment.setEndedAtXsd(soln.getLiteral("endedAt").getString()); }
			deployment.platform = Platform.find(hadatac);
			deployment.instrument = Instrument.find(hadatac);
			
			return deployment;
		}
		
		return null;
	}

	public static Deployment find(Model model, DataCollection dataCollection) {
		String queryString = Sparql.prefix
				+ "SELECT ?dp WHERE {\n"
				+ "  ?dp hasneto:hasDataCollection <" + dataCollection.getCcsvUri() + "> .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() == 1) {
			QuerySolution soln = resultsrw.next();
			Deployment deployment = new Deployment();
			deployment.setLocalName(soln.getResource("dp").getLocalName());
			deployment.setCcsvUri(soln.getResource("dp").getURI());
			return deployment;
		}
		
		return null;
	}
}
