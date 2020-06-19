package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
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
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


public class Deployment extends HADatAcThing {

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         vstoi:Deployment;  ";
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
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
    private static Map<String, Deployment> DPLCache;

    private static Map<String, Deployment> getCache() {
        if (DPLCache == null) {
            DPLCache = new HashMap<String, Deployment>(); 
        }
        return DPLCache;
    }

    public static void resetCache() {
        DPLCache = null;
    }

    public Deployment() {
        startedAt = null;
        endedAt = null;
        instrument = null;
        platform = null;
        legacy = false;
        detectors = new ArrayList<Detector>();
        Deployment.getCache();
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

    public void saveEndedAtTime() {
        String insert = "";
        if (this.getEndedAt() != null) {
            insert += NameSpaces.getInstance().printSparqlNameSpaceList();
            insert += INSERT_LINE1;
            insert += "<" + this.getUri() + ">  ";
            insert += END_TIME_PREDICATE + "\"" + this.getEndedAt() + TIME_XMLS + "  ";
            insert += LINE_LAST;
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(request,CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE)); 
            processor.execute();
        }
    }

    public void close(String endedAt) {
        setEndedAtXsd(endedAt);
        List<STR> list = STR.find(this, true);
        if (!list.isEmpty()) {
            STR dc = list.get(0);
            dc.close(endedAt);
        }
        saveEndedAtTime();
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

    public static Deployment find(String deployment_uri) {
        if (Deployment.getCache().get(deployment_uri) != null) {
            return Deployment.getCache().get(deployment_uri);
        }

        //System.out.println("Current URI for FIND DEPLOYMENT: " + deployment_uri);

        Deployment deployment = null;
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList();
        if (deployment_uri.startsWith("http")) {
            queryString += "DESCRIBE <" + deployment_uri + ">";
        } else {
            queryString += "DESCRIBE " + deployment_uri;
        }
        // System.out.println("FIND DEPLOYMENT (queryString): " + queryString);
        
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

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
        
        Deployment.getCache().put(deployment_uri, deployment);
        return deployment;
    }

    public static List<Deployment> find(State state) {
        List<Deployment> deployments = new ArrayList<Deployment>();
        String queryString = "";
        if (state.getCurrent() == State.ACTIVE) { 
            queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
            		"SELECT ?uri WHERE { " + 
                    "   ?uri a vstoi:Deployment . " + 
                    "   FILTER NOT EXISTS { ?uri prov:endedAtTime ?enddatetime . } " + 
                    "} " + 
                    "ORDER BY DESC(?datetime) ";
        } else {
            if (state.getCurrent() == State.CLOSED) {
                queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                        "SELECT ?uri WHERE { " + 
                        "   ?uri a vstoi:Deployment . " + 
                        "   ?uri prov:startedAtTime ?startdatetime .  " + 
                        "   ?uri prov:endedAtTime ?enddatetime .  " + 
                        "} " +
                        "ORDER BY DESC(?datetime) ";
            } else {
                if (state.getCurrent() == State.ALL) {
                    queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
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
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

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

    public static List<Deployment> findWithPages(State state, int pageSize, int offset) {
        List<Deployment> deployments = new ArrayList<Deployment>();
        String queryString = "";
        if (state.getCurrent() == State.ACTIVE) { 
            queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
            		"SELECT ?uri WHERE { " + 
                    "   ?uri a vstoi:Deployment . " + 
                    "   FILTER NOT EXISTS { ?uri prov:endedAtTime ?enddatetime . } " + 
                    "} " + 
                    " ORDER BY DESC(?datetime) " +
            		" LIMIT " + pageSize + 
            		" OFFSET " + offset;
        } else {
            if (state.getCurrent() == State.CLOSED) {
                queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                        "SELECT ?uri WHERE { " + 
                        "   ?uri a vstoi:Deployment . " + 
                        "   ?uri prov:startedAtTime ?startdatetime .  " + 
                        "   ?uri prov:endedAtTime ?enddatetime .  " + 
                        "} " +
                        " ORDER BY DESC(?datetime) " +
                        " LIMIT " + pageSize + 
                        " OFFSET " + offset;
            } else {
                if (state.getCurrent() == State.ALL) {
                    queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                            "SELECT ?uri WHERE { " + 
                            "   ?uri a vstoi:Deployment . " + 
                            "} " +
                            " ORDER BY DESC(?datetime) " +
                            " LIMIT " + pageSize + 
                            " OFFSET " + offset;
                } else {
                    System.out.println("Deployment.java: no valid state specified.");
                    return null;
                }
            }
        }
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

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

    public static int getNumberDeployments(State state) {
        String query = "";
        if (state.getCurrent() == State.ACTIVE) { 
            query = NameSpaces.getInstance().printSparqlNameSpaceList() +
            		"SELECT (count(?uri) as ?tot) WHERE { " + 
                    "   ?uri a vstoi:Deployment . " + 
                    "   FILTER NOT EXISTS { ?uri prov:endedAtTime ?enddatetime . } " + 
                    "} "; 
        } else {
            if (state.getCurrent() == State.CLOSED) {
                query = NameSpaces.getInstance().printSparqlNameSpaceList() +
                        "SELECT (count(?uri) as ?tot) WHERE { " + 
                        "   ?uri a vstoi:Deployment . " + 
                        "   ?uri prov:startedAtTime ?startdatetime .  " + 
                        "   ?uri prov:endedAtTime ?enddatetime .  " + 
                        "} ";
            } else {
                if (state.getCurrent() == State.ALL) {
                    query = NameSpaces.getInstance().printSparqlNameSpaceList() +
                            "SELECT (count(?uri) as ?tot) WHERE { " + 
                            "   ?uri a vstoi:Deployment . " + 
                            "} ";
                } else {
                    System.out.println("Deployment.java: no valid state specified.");
                    return 0;
                }
            }
        }
                
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                return Integer.parseInt(soln.getLiteral("tot").getString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static List<Deployment> findWithGeoReference() {
        List<Deployment> deployments = new ArrayList<Deployment>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?platModel rdfs:subClassOf* vstoi:Platform . " + 
                " ?plat a ?platModel ." +
                " ?plat hasco:hasFirstCoordinate ?lat . " +
                " ?plat hasco:hasSecondCoordinate ?lon . " +
                " ?plat hasco:hasFirstCoordinateCharacteristic <" + Platform.LAT + "> . " +
                " ?plat hasco:hasSecondCoordinateCharacteristic <" + Platform.LONG + "> . " +
                " ?uri vstoi:hasPlatform ?plat . " +
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Deployment deployment = find(soln.getResource("uri").getURI());
            deployments.add(deployment);
        }			

        return deployments;
    }


    
    public static List<Deployment> findByPlatformAndStatus(String plat_uri, State state) {
    	if (plat_uri == null) {
    		return null;
    	}
        List<Deployment> deployments = new ArrayList<Deployment>();
    	String p_uri = plat_uri;
    	if (plat_uri.startsWith("http")) {
    		p_uri = "<" + plat_uri + ">"; 
    	}
        String queryString = "";
        if (state.getCurrent() == State.ACTIVE) { 
            queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
            		"SELECT ?uri WHERE { " + 
                    "   ?uri a vstoi:Deployment . " + 
                    "   ?uri vstoi:hasPlatform " + p_uri + " . " + 
                    "   FILTER NOT EXISTS { ?uri prov:endedAtTime ?enddatetime . } " + 
                    "} " + 
                    "ORDER BY DESC(?datetime) ";
        } else {
            if (state.getCurrent() == State.CLOSED) {
                queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                        "SELECT ?uri WHERE { " + 
                        "   ?uri a vstoi:Deployment . " + 
                        "   ?uri vstoi:hasPlatform " + p_uri + " . " + 
                        "   ?uri prov:startedAtTime ?startdatetime .  " + 
                        "   ?uri prov:endedAtTime ?enddatetime .  " + 
                        "} " +
                        "ORDER BY DESC(?datetime) ";
            } else {
                if (state.getCurrent() == State.ALL) {
                    queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                            "SELECT ?uri WHERE { " + 
                            "   ?uri a vstoi:Deployment . " + 
                            "   ?uri vstoi:hasPlatform " + p_uri + " . " + 
                            "} " +
                            "ORDER BY DESC(?datetime) ";
                } else {
                    System.out.println("Deployment.java: no valid state specified.");
                    return null;
                }
            }
        }
                
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

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

    public static List<Deployment> findByReferenceLayoutAndStatus(String plat_uri, State state) {
    	if (plat_uri == null) {
    		return null;
    	}
        List<Deployment> deployments = new ArrayList<Deployment>();
    	String p_uri = plat_uri;
    	if (plat_uri.startsWith("http")) {
    		p_uri = "<" + plat_uri + ">"; 
    	}
        String queryString = "";
        if (state.getCurrent() == State.ACTIVE) { 
            queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
            		"SELECT ?uri WHERE { " + 
                    "   ?uri a vstoi:Deployment . " + 
                    "   ?uri vstoi:hasPlatform ?plt . " + 
                    "   ?plt hasco:hasReferenceLayout " + p_uri + "  . " + 
                    "   FILTER NOT EXISTS { ?uri prov:endedAtTime ?enddatetime . } " + 
                    "} " + 
                    "ORDER BY DESC(?datetime) ";
        } else {
            if (state.getCurrent() == State.CLOSED) {
                queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                        "SELECT ?uri WHERE { " + 
                        "   ?uri a vstoi:Deployment . " + 
                        "   ?uri vstoi:hasPlatform ?plt . " + 
                        "   ?plt hasco:hasReferenceLayout " + p_uri + "  . " + 
                        "   ?uri prov:startedAtTime ?startdatetime .  " + 
                        "   ?uri prov:endedAtTime ?enddatetime .  " + 
                        "} " +
                        "ORDER BY DESC(?datetime) ";
            } else {
                if (state.getCurrent() == State.ALL) {
                    queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                            "SELECT ?uri WHERE { " + 
                            "   ?uri a vstoi:Deployment . " + 
                            "   ?uri vstoi:hasPlatform ?plt . " + 
                            "   ?plt hasco:hasReferenceLayout " + p_uri + "  . " + 
                            "} " +
                            "ORDER BY DESC(?datetime) ";
                } else {
                    System.out.println("Deployment.java: no valid state specified.");
                    return null;
                }
            }
        }
                
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

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


    @Override
    public boolean saveToTripleStore() {
        String insert = "";
        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;
        
        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }
        
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
        
        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }
        
        insert += LINE_LAST;

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
            processor.execute();
        } catch (QueryParseException e) {
            System.out.println("QueryParseException due to update query: " + insert);
            throw e;
        }

        return true;
    }

    @Override
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }

}
