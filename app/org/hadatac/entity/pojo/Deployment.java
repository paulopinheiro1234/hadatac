package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.data.loader.util.Sparql;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import play.Play;

public class Deployment {
	
	public static String INDENT1 = "     ";
	
	public static String INSERT_LINE1 = "INSERT DATA {  ";
    
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
    
    public static String LINE3 = INDENT1 + "a         vstoi:Deployment;  ";
    
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";

    public static String LINE3_LEGACY = INDENT1 + "a         vstoi:LegacyDeployment;  ";
    
    public static String PLATFORM_PREDICATE =     INDENT1 + "vstoi:hasPlatform        ";
    
    public static String INSTRUMENT_PREDICATE =   INDENT1 + "hasneto:hasInstrument    ";
    
    public static String DETECTOR_PREDICATE =     INDENT1 + "hasneto:hasDetector      ";
        
    public static String START_TIME_PREDICATE =   INDENT1 + "prov:startedAtTime		  ";
    
    public static String END_TIME_PREDICATE =     INDENT1 + "prov:endedAtTime		  ";
    
    public static String TIME_XMLS =   "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .";
    
    public static String LINE_LAST = "}  ";

	private String uri;
	private String localName;
	private String ccsvUri;
	private DateTime startedAt;
	private DateTime endedAt;
	private boolean legacy;
	
	public Instrument instrument;
	public Platform platform;
	public List<Detector> detectors;
	
	public Deployment() {
		startedAt = null;
		endedAt = null;
		instrument = null;
		platform = null;
		legacy = false;
		detectors = new ArrayList<Detector>();
	}
	
	public boolean isLegacy() {
		return legacy;
	}

	public void setLegacy(boolean legacy) {
		this.legacy = legacy;
	}

	public String getStartedAt() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(startedAt);
	}
	
	public String getStartedAtXsd() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
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
	public void setStartedAtXsdWithMillis(String startedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
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
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	public void setEndedAtXsdWithMillis(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		this.endedAt = formatter.parseDateTime(endedAt);
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
	
	public void save() {
		String insert = "";
		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
    	insert += INSERT_LINE1;
    	insert += "<" + this.getUri() + ">  ";
    	if (this.isLegacy()) {
    		insert += LINE3_LEGACY;
    	} else {
    		insert += LINE3;
    	}
    	insert += PLATFORM_PREDICATE + "<" + this.platform.getUri() + "> ;   ";
    	insert += INSTRUMENT_PREDICATE + "<" + this.instrument.getUri() + "> ;   ";
    	Iterator<Detector> i = this.detectors.iterator();
    	while (i.hasNext()) {
    		insert += DETECTOR_PREDICATE + "<" + i.next().getUri() + "> ;   ";
    	}
       	insert += START_TIME_PREDICATE + "\"" + this.getStartedAt() + TIME_XMLS + "  ";
       	if (this.endedAt != null) {
           	insert += END_TIME_PREDICATE + "\"" + this.getEndedAt() + TIME_XMLS + "  ";
       	}
    	insert += LINE_LAST;
    	System.out.println(insert);
    	UpdateRequest request = UpdateFactory.create(insert);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_SPARQL));
        processor.execute();
        
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(Play.application().configuration().getString("hadatac.solr.triplestore")
        		+ "/store/update?commit=true");
        try {
			httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveEndedAtTime() {
		String insert = "";
       	if (this.getEndedAt() != null) {
		    insert += NameSpaces.getInstance().printSparqlNameSpaceList();
    	    insert += INSERT_LINE1;
    	    insert += "<" + this.getUri() + ">  ";
           	insert += END_TIME_PREDICATE + "\"" + this.getEndedAt() + TIME_XMLS + "  ";
    	    insert += LINE_LAST;
    	    System.out.println(insert);
    	    UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(request,Collections.getCollectionsName(Collections.METADATA_SPARQL)); 
            processor.execute();
        
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(Play.application().configuration().getString("hadatac.solr.triplestore")
             		+ "/store/update?commit=true");
            try {
    			httpclient.execute(httpget);  
	    	} catch (ClientProtocolException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
	    	} catch (IOException e) {
		    	// TODO Auto-generated catch block
		    	e.printStackTrace();
	    	}
       	}
	}
	
	public void close(String endedAt) {
		setEndedAtXsd(endedAt);
		List<DataCollection> list = DataCollection.find(this, true);
		if (!list.isEmpty()) {
			DataCollection dc = list.get(0);
			dc.close(endedAt);
		}
		saveEndedAtTime();
	}
	
	public void delete() {
		String query = "";
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += DELETE_LINE1;
    	query += "<" + this.getUri() + ">  ";
        query += DELETE_LINE3;
    	query += LINE_LAST;
        System.out.println(query);
    	UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_SPARQL));
        processor.execute();
        
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(Play.application().configuration().getString("hadatac.solr.triplestore")
            	+ "/store/update?commit=true");
        try {
    	    httpclient.execute(httpget);  
	    } catch (ClientProtocolException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
	    } catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
	    }
	}
	
	public static Deployment create(String uri) {
		Deployment deployment = new Deployment();
		
		deployment.setUri(uri);
		
		return deployment;
	}
	
	public static Deployment createLegacy(String uri) {
		Deployment deployment = new Deployment();
		
		deployment.setUri(uri);
		deployment.setLegacy(true);
		
		return deployment;
	}
	
	public static Deployment findFromDataCollection(HADataC hadatac) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?startedAt ?endedAt ?detector ?instrument ?platform WHERE {\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> a vstoi:Deployment .\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> prov:startedAtTime ?startedAt .\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> hasneto:hasDetector ?detector .\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> hasneto:hasInstrument ?instrument .\n"
				+ "  <" + hadatac.dataCollection.getDeploymentUri() + "> vstoi:hasPlatform ?platform .\n"
				+ "  OPTIONAL { <" + hadatac.dataCollection.getDeploymentUri() + "> prov:endedAtTime ?endedAt . }\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		System.out.println(queryString);
		
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
			deployment.setStartedAtXsdWithMillis(soln.getLiteral("startedAt").getString());
			if (soln.getLiteral("endedAt") != null) { deployment.setEndedAtXsd(soln.getLiteral("endedAt").getString()); }
			hadatac.deployment = deployment;
			System.out.println("!! DEPLOYMENT.FINDFROMDC " + deployment.getUri());
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
	
	public static Deployment find(String deployment_uri) {
		Deployment deployment = null;
		Model model;
		Statement statement;
		RDFNode object;
		
		String queryString = "DESCRIBE <" + deployment_uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Play.application().configuration().getString("hadatac.solr.triplestore") + "/store/sparql", query);
		model = qexec.execDescribe();
		
		deployment = new Deployment();
		StmtIterator stmtIterator = model.listStatements();
		
		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasneto#hasInstrument")) {
				deployment.instrument = Instrument.find(object.asResource().getURI());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#hasPlatform")) {
				deployment.platform = Platform.find(object.asResource().getURI());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasneto#hasDetector")) {
				deployment.detectors.add(Detector.find(object.asResource().getURI()));
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/ns/prov#startedAtTime")) {
				deployment.setStartedAtXsdWithMillis(object.asLiteral().getString());
			}
		}
		
		deployment.setUri(deployment_uri);
		
		return deployment;
	}

	public static List<Deployment> find(State state) {
		List<Deployment> deployments = new ArrayList<Deployment>();
	    String queryString = "";
        if (state.getCurrent() == State.ACTIVE) { 
    	   queryString = "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
    			   "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
    			   "SELECT ?uri WHERE { " + 
    			   "   ?uri a vstoi:Deployment . " + 
    			   "   FILTER NOT EXISTS { ?uri prov:endedAtTime ?enddatetime . } " + 
    			   "} " + 
    			   "ORDER BY DESC(?datetime) ";
        } else {
    	   if (state.getCurrent() == State.CLOSED) {
    		   queryString = "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
    				   "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
    				   "SELECT ?uri WHERE { " + 
    				   "   ?uri a vstoi:Deployment . " + 
    				   "   ?uri prov:startedAtTime ?startdatetime .  " + 
    				   "   ?uri prov:endedAtTime ?enddatetime .  " + 
    				   "} " +
    				   "ORDER BY DESC(?datetime) ";
    	   } else {
        	   if (state.getCurrent() == State.ALL) {
        		   queryString = "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        				   "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        				   "SELECT ?uri WHERE { " + 
        				   "   ?uri a vstoi:Deployment . " + 
        				   "} " +
        				   "ORDER BY DESC(?datetime) ";
        	   } else {
        		   System.out.println("Deployment.java: no valid state specified.");
        		   return null;
        	   }
    	   }
        }
		Query query = QueryFactory.create(queryString);
		
		System.out.println(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Deployment dep = null;
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null && soln.getResource("uri").getURI()!= null) { 
				//dep = Deployment.find(soln.getLiteral("uri").getString()); 
				dep = Deployment.find(soln.getResource("uri").getURI()); 
			}
			deployments.add(dep);
			
		}
		
		return deployments;
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
