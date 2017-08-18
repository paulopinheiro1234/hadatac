package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;

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
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.routes;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;

public class Deployment {
	
    public static String INDENT1 = "     ";
    
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    
    public static String LINE3 = INDENT1 + "a         vstoi:Deployment;  ";
    
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";

    //public static String LINE3_LEGACY = INDENT1 + "a         vstoi:LegacyDeployment;  ";
    public static String LINE3_LEGACY = INDENT1 + "a         vstoi:Deployment;  ";
    
    public static String PLATFORM_PREDICATE =     INDENT1 + "vstoi:hasPlatform        ";
    
    public static String INSTRUMENT_PREDICATE =   INDENT1 + "hasco:hasInstrument    ";
    
    public static String DETECTOR_PREDICATE =     INDENT1 + "hasco:hasDetector      ";
        
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
    
    private Instrument instrument;
    private Platform platform;
    private List<Detector> detectors;
    
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
	this.endedAt = DateTime.parse(endedAt);
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
    
    public Instrument getInstrument() {
	return instrument;
    }

    public void setInstrument(Instrument instrument) {
	this.instrument = instrument;
    }
	
    public Platform getPlatform() {
	return platform;
    }

    public void setPlatform(Platform platform) {
	this.platform = platform;
    }
    
    public List<Detector> getDetectors() {
	return detectors;
    }

    public void setDetectors(List<Detector> detectors) {
	this.detectors = detectors;
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
    	UpdateRequest request = UpdateFactory.create(insert);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
									request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
    }
    
    public void saveEndedAtTime() {
	String insert = "";
       	if (this.getEndedAt() != null) {
	    insert += NameSpaces.getInstance().printSparqlNameSpaceList();
    	    insert += INSERT_LINE1;
    	    insert += "<" + this.getUri() + ">  ";
	    insert += END_TIME_PREDICATE + "\"" + this.getEndedAt() + TIME_XMLS + "  ";
    	    insert += LINE_LAST;
    	    UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(request,Collections.getCollectionsName(Collections.METADATA_UPDATE)); 
            processor.execute();
       	}
    }
    
    public void close(String endedAt) {
	setEndedAtXsd(endedAt);
	List<DataAcquisition> list = DataAcquisition.find(this, true);
	if (!list.isEmpty()) {
	    DataAcquisition dc = list.get(0);
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
    	UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
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
    
    public static Deployment findFromDataAcquisition(HADataC hadatac) {
	//System.out.println("Current URI for FIND FROM DATA ACQUISITION: " + hadatac.getDataAcquisition().getDeploymentUri());
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
	    + "SELECT ?startedAt ?endedAt ?detector ?instrument ?platform WHERE {\n"
	    + "  <" + hadatac.getDataAcquisition().getDeploymentUri() + "> a vstoi:Deployment .\n"
	    + "  <" + hadatac.getDataAcquisition().getDeploymentUri() + "> prov:startedAtTime ?startedAt .\n"
	    + "  <" + hadatac.getDataAcquisition().getDeploymentUri() + "> hasco:hasInstrument ?instrument .\n"
	    + "  <" + hadatac.getDataAcquisition().getDeploymentUri() + "> vstoi:hasPlatform ?platform .\n"
	    + "  OPTIONAL { <" + hadatac.getDataAcquisition().getDeploymentUri() + "> hasco:hasDetector ?detector . }\n"
	    + "  OPTIONAL { <" + hadatac.getDataAcquisition().getDeploymentUri() + "> prov:endedAtTime ?endedAt . }\n"
	    + "}";
	
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (resultsrw.size() >= 1) {
	    QuerySolution soln = resultsrw.next();
	    Deployment deployment = new Deployment();
	    Resource resource = ResourceFactory.createResource(hadatac.getDataAcquisition().getDeploymentUri());
	    deployment.setLocalName(resource.getLocalName());
	    deployment.setUri(hadatac.getDataAcquisition().getDeploymentUri());
	    deployment.setStartedAtXsdWithMillis(soln.getLiteral("startedAt").getString());
	    if (soln.getLiteral("endedAt") != null) { 
		deployment.setEndedAtXsd(soln.getLiteral("endedAt").getString());
	    }
	    hadatac.setDeployment(deployment);
	    deployment.platform = Platform.find(hadatac);
	    deployment.instrument = Instrument.find(hadatac);
	    
	    return deployment;
	}
	
	return null;
    }
    
    public static Deployment findFromPreamble(HADataC hadatac) {
	//System.out.println("Current URI for FIND FROM PREAMBLE: " + hadatac.getDataAcquisition().getDeploymentUri());
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
	    + "SELECT ?startedAt ?endedAt ?detector ?instrument ?platform WHERE {\n"
	    + "  <" + hadatac.getDeploymentUri() + "> a vstoi:Deployment .\n"
	    + "  <" + hadatac.getDeploymentUri() + "> prov:startedAtTime ?startedAt .\n"
	    + "  <" + hadatac.getDeploymentUri() + "> hasco:hasDetector ?detector .\n"
	    + "  <" + hadatac.getDeploymentUri() + "> hasco:hasInstrument ?instrument .\n"
	    + "  <" + hadatac.getDeploymentUri() + "> vstoi:hasPlatform ?platform .\n"
	    + "  OPTIONAL { <" + hadatac.getDeploymentUri() + "> prov:endedAtTime ?endedAt . }\n"
	    + "}";
	
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (resultsrw.size() >= 1) {
	    QuerySolution soln = resultsrw.next();
	    Deployment deployment = new Deployment();
	    deployment.setLocalName(hadatac.getDeployment().getLocalName());
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
	//System.out.println("Current URI for FIND DEPLOYMENT: " + deployment_uri);
	Deployment deployment = null;
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList();
	if (deployment_uri.startsWith("http")) {
	    queryString += "DESCRIBE <" + deployment_uri + ">";
	} else {
	    queryString += "DESCRIBE " + deployment_uri;
	}
	//System.out.println("FIND DEPLOYMENT (queryString): " + queryString);
	Query query = QueryFactory.create(queryString);
	QueryExecution qexec = QueryExecutionFactory.sparqlService(
								   Play.application().configuration().getString("hadatac.solr.triplestore") 
								   + Collections.METADATA_SPARQL, query);
	Model model = qexec.execDescribe();
	
	StmtIterator stmtIterator = model.listStatements();
	if (!model.isEmpty()) {
	    deployment = new Deployment();
	    deployment.setUri(deployment_uri);
	}
	
	while (stmtIterator.hasNext()) {
	    Statement statement = stmtIterator.next();
	    RDFNode object = statement.getObject();
	    if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasInstrument")) {
		deployment.instrument = Instrument.find(object.asResource().getURI());
	    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#hasPlatform")) {
		deployment.platform = Platform.find(object.asResource().getURI());
	    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasDetector")) {
		deployment.detectors.add(Detector.find(object.asResource().getURI()));
	    } else if (statement.getPredicate().getURI().equals("http://www.w3.org/ns/prov#startedAtTime")) {
		deployment.setStartedAtXsdWithMillis(object.asLiteral().getString());
	    }
	}
	
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
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(
								   Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	Deployment dep = null;
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null && soln.getResource("uri").getURI()!= null) { 
		dep = Deployment.find(soln.getResource("uri").getURI()); 
	    }
	    deployments.add(dep);
	}
	
	return deployments;
    }
    
    public static Deployment find(Model model, DataAcquisition dataAcquisition) {
	//System.out.println("FIND DEPLOYMENT OF DATA ACQUISITION ");
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
	    + "SELECT ?dp WHERE {\n"
	    + "  ?dp hasco:hasDataAcquisition <" + dataAcquisition.getCcsvUri() + "> .\n"
	    + "}";
	
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.create(query, model);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	
	if (resultsrw.size() >= 1) {
	    QuerySolution soln = resultsrw.next();
	    Deployment deployment = new Deployment();
	    deployment.setLocalName(soln.getResource("dp").getLocalName());
	    deployment.setCcsvUri(soln.getResource("dp").getURI());
	    return deployment;
	}
	
	return null;
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public int saveToLabKey(String user_name, String password) throws CommandException {
    	
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
        
    	LabkeyDataHandler loader = new LabkeyDataHandler(
    			site, user_name, password, path);
    	
    	List<String> detectorURIs = new ArrayList<String>();
    	for (Detector detector : getDetectors()) {
    		detectorURIs.add(ValueCellProcessing.replaceNameSpaceEx(detector.getUri()));
    	}
    	String detectors = String.join(", ", detectorURIs);
    	
    	List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
    	row.put("a", "vstoi:Deployment");
    	row.put("vstoi:hasPlatform", ValueCellProcessing.replaceNameSpaceEx(getPlatform().getUri()));
    	row.put("hasco:hasInstrument", ValueCellProcessing.replaceNameSpaceEx(getInstrument().getUri()));
    	row.put("hasco:hasDetector", detectors);
    	row.put("prov:startedAtTime", getStartedAt());
    	row.put("prov:endedAtTime", getEndedAt());
    	rows.add(row);
    	
    	return loader.insertRows("Deployment", rows);
    }
}
