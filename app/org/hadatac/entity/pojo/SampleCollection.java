package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.entity.pojo.SampleCollection;
import org.labkey.remoteapi.CommandException;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.routes;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;

public class SampleCollection extends HADatAcThing {

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:SampleCollection;  ";
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    private String studyUri = "";
    private List<Sample> samples = null;

    public SampleCollection() {
	this.uri = "";
	this.type = "";
	this.label = "";
	this.comment = "";
	this.studyUri = "";
	this.samples = new ArrayList<Sample>();
    }
    
    public SampleCollection(String uri,
			    String type,
			    String label,
			    String comment,
			    String studyUri) {
	this.setUri(uri);
	this.setType(type);
	this.setLabel(label);
	this.setComment(comment);
	this.setStudyUri(studyUri);
	this.samples = new ArrayList<Sample>();
    }

    public SampleCollectionType getSampleCollectionType() {
	if (type == null || type.equals("")) {
	    return null;
	}
	SampleCollectionType scType = SampleCollectionType.find(type);
	return scType;    
    }

    public String getStudyUri() {
	return studyUri;
    }

    public Study getStudy() {
	if (studyUri == null || studyUri.equals("")) {
	    return null;
	}
	return Study.find(studyUri);
    }

    public void setStudyUri(String studyUri) {
	this.studyUri = studyUri;
    }

    public List<Sample> getSamples() {
	return samples;
    }

    public void setSamples(List<Sample> samples) {
	this.samples = samples;
    }

    public static SampleCollection find(String sc_uri) {
	sc_uri = URLDecoder.decode(sc_uri);
	SampleCollection sc = null;
	//System.out.println("Looking for sample collection with URI " + sc_uri);
	if (sc_uri.startsWith("http")) {
	    sc_uri = "<" + sc_uri + ">";
	}
	//System.out.println("In SampleCollection: [" + sc_uri + "]");
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT  ?scType ?label ?comment ?studyUri WHERE { " + 
	    "    " + sc_uri + " a ?scType . " + 
	    "    " + sc_uri + " hasco:isSampleCollectionOf ?studyUri .  " + 
	    "    OPTIONAL { " + sc_uri + " rdfs:comment ?comment } . " + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] SampleCollection. Could not find SC with URI: " + sc_uri);
	    return sc;
	}
	
	String typeStr = "";
	String labelStr = "";
	String studyUriStr = "";
	String commentStr = "";
	
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null) {
		
		try {
		    if (soln.getResource("scType") != null && soln.getResource("scType").getURI() != null) {
			typeStr = soln.getResource("scType").getURI();
		    }
		} catch (Exception e1) {
		    typeStr = "";
		}
		
		labelStr = FirstLabel.getLabel(sc_uri);
		
		try {
		    if (soln.getResource("studyUri") != null && soln.getResource("studyUri").getURI() != null) {
			studyUriStr = soln.getResource("studyUri").getURI();
		    }
		} catch (Exception e1) {
		    studyUriStr = "";
		}
		
		try {
		    if (soln.getLiteral("comment") != null && soln.getLiteral("comment").getString() != null) {
			commentStr = soln.getLiteral("comment").getString();
		    }
		} catch (Exception e1) {
		    commentStr = "";
		}
		
		sc = new SampleCollection(sc_uri,
					  typeStr,
					  labelStr,
					  commentStr,
					  studyUriStr);
	    }
	}
	return sc;
    }
    	
    public static List<SampleCollection> findAll() {
    	List<SampleCollection> sc_list = new ArrayList<SampleCollection>();
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?uri WHERE { " + 
	    "   ?scType rdfs:subClassOf+ hasco:SampleCollection . " +
	    "   ?uri a ?scType . } ";
    	Query query = QueryFactory.create(queryString);
    	QueryExecution qexec = QueryExecutionFactory.sparqlService(
								   Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null && soln.getResource("uri").getURI() != null) { 
		SampleCollection sc = SampleCollection.find(soln.getResource("uri").getURI());
		sc_list.add(sc);
	    }
	}
	return sc_list;
    }

    public static List<SampleCollection> findByStudy(Study study) {
	if (study == null) {
	    return null;
	}
    	List<SampleCollection> schemas = new ArrayList<SampleCollection>();
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?uri WHERE { " + 
	    "   ?scType rdfs:subClassOf+ hasco:SampleCollection . " +
	    "   ?uri a ?scType .  " +
	    "   ?uri hasco:isSampleCollectionOf  <" + study.getUri() + "> . " +
	    " } ";
    	Query query = QueryFactory.create(queryString);
    	QueryExecution qexec = QueryExecutionFactory.sparqlService(
			Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null && soln.getResource("uri").getURI() != null) { 
		SampleCollection schema = SampleCollection.find(soln.getResource("uri").getURI());
		schemas.add(schema);
	    }
	}
	return schemas;
    }

    public void save() {
	String insert = "";

	String sc_uri = "";
	if (this.getUri().startsWith("<")) {
	    sc_uri = this.getUri();
	} else {
	    sc_uri = "<" + this.getUri() + ">";
	}

	insert += NameSpaces.getInstance().printSparqlNameSpaceList();
    	insert += INSERT_LINE1;
    	insert += sc_uri + " a <" + type + "> . ";
    	insert += sc_uri + " rdfs:label  \"" + this.getLabel() + "\" . ";
	if (this.getStudyUri().startsWith("http")) {
	    insert += sc_uri + " hasco:isSampleCollectionOf  <" + this.getStudyUri() + "> . ";
	} else {
	    insert += sc_uri + " hasco:isSampleCollectionOf  " + this.getStudyUri() + " . ";
	}
	if (this.getComment() != null && !this.getComment().equals("")) {
	    insert += sc_uri + " rdfs:comment  \"" + this.getComment() + "\" . ";
	}
    	insert += LINE_LAST;
	//System.out.println(insert);
    	UpdateRequest request = UpdateFactory.create(insert);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				      request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public int saveToLabKey(String user_name, String password) {
	String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
    	LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
    	List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
    	row.put("a", ValueCellProcessing.replaceNameSpaceEx(getType()));
    	row.put("rdfs:label", getLabel());
    	row.put("hasco:isSampleCollectionOf", getStudyUri());
    	rows.add(row);

	int totalChanged = 0;
    	try {
	    totalChanged = loader.insertRows("SampleCollection", rows);
	} catch (CommandException e) {
	    try {
		totalChanged = loader.updateRows("SampleCollection", rows);
	    } catch (CommandException e2) {
		System.out.println("[ERROR] Could not insert or update SampleCollection(s)");
	    }
	}
	return totalChanged;
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public int deleteFromLabKey(String user_name, String password) throws CommandException {
	String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
    	LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
    	List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri().replace("<","").replace(">","")));
    	rows.add(row);
	for (Map<String,Object> str : rows) {
	    System.out.println("deleting Sample Collection " + row.get("hasURI"));
	}
    	return loader.deleteRows("SampleCollection", rows);
    }
    
    public void delete() {
	String query = "";

	String sc_uri = "";
	if (this.getUri().startsWith("<")) {
	    sc_uri = this.getUri();
	} else {
	    sc_uri = "<" + this.getUri() + ">";
	}

	query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += DELETE_LINE1;
    	query += " " + sc_uri + "  ";
        query += DELETE_LINE3;
    	query += LINE_LAST;

    	UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
    }
	
}
