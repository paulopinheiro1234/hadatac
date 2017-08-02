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
import org.hadatac.entity.pojo.ObjectCollection;
import org.labkey.remoteapi.CommandException;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.routes;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;

public class ObjectCollection extends HADatAcThing {

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:ObjectCollection;  ";
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    private String studyUri = "";
    private String inScopeOf = "";
    private List<String> objectUris = null;

    public ObjectCollection() {
	this.uri = "";
	this.type = "";
	this.label = "";
	this.comment = "";
	this.studyUri = "";
	this.inScopeOf = "";
	this.objectUris = new ArrayList<String>();
    }
    
    public ObjectCollection(String uri,
			    String type,
			    String label,
			    String comment,
			    String studyUri) {
	this.setUri(uri);
	this.setType(type);
	this.setLabel(label);
	this.setComment(comment);
	this.setStudyUri(studyUri);
	this.objectUris = new ArrayList<String>();
    }

    public ObjectCollectionType getObjectCollectionType() {
	if (type == null || type.equals("")) {
	    return null;
	}
	ObjectCollectionType ocType = ObjectCollectionType.find(type);
	return ocType;    
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

    public List<String> getObjectUris() {
	return objectUris;
    }

    public void setObjectUris(List<String> objectUris) {
	this.objectUris = objectUris;
    }

    public static ObjectCollection find(String oc_uri) {
	oc_uri = URLDecoder.decode(oc_uri);
	ObjectCollection sc = null;
	//System.out.println("Looking for object collection with URI " + oc_uri);
	if (oc_uri.startsWith("http")) {
	    oc_uri = "<" + oc_uri + ">";
	}
	//System.out.println("In ObjectCollection: [" + oc_uri + "]");
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT  ?ocType ?label ?comment ?studyUri WHERE { " + 
	    "    " + oc_uri + " a ?ocType . " + 
	    "    " + oc_uri + " hasco:isObjectCollectionOf ?studyUri .  " + 
	    "    OPTIONAL { " + oc_uri + " rdfs:comment ?comment } . " + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] ObjectCollection. Could not find SC with URI: " + oc_uri);
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
		    if (soln.getResource("ocType") != null && soln.getResource("ocType").getURI() != null) {
			typeStr = soln.getResource("ocType").getURI();
		    }
		} catch (Exception e1) {
		    typeStr = "";
		}
		
		labelStr = FirstLabel.getLabel(oc_uri);
		
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
		
		sc = new ObjectCollection(oc_uri,
					  typeStr,
					  labelStr,
					  commentStr,
					  studyUriStr);
	    }
	}
	return sc;
    }
    	
    public static List<ObjectCollection> findAll() {
    	List<ObjectCollection> oc_list = new ArrayList<ObjectCollection>();
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?uri WHERE { " + 
	    "   ?ocType rdfs:subClassOf+ hasco:ObjectCollection . " +
	    "   ?uri a ?ocType . } ";
    	Query query = QueryFactory.create(queryString);
    	QueryExecution qexec = QueryExecutionFactory.sparqlService(
			       Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null && soln.getResource("uri").getURI() != null) { 
		ObjectCollection sc = ObjectCollection.find(soln.getResource("uri").getURI());
		oc_list.add(sc);
	    }
	}
	return oc_list;
    }

    public static List<ObjectCollection> findByStudy(Study study) {
	if (study == null) {
	    return null;
	}
    	List<ObjectCollection> schemas = new ArrayList<ObjectCollection>();
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?uri WHERE { " + 
	    "   ?ocType rdfs:subClassOf+ hasco:ObjectCollection . " +
	    "   ?uri a ?ocType .  " +
	    "   ?uri hasco:isObjectCollectionOf  <" + study.getUri() + "> . " +
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
		ObjectCollection schema = ObjectCollection.find(soln.getResource("uri").getURI());
		schemas.add(schema);
	    }
	}
	return schemas;
    }

    public void save() {
	String insert = "";

	String oc_uri = "";
	if (this.getUri().startsWith("<")) {
	    oc_uri = this.getUri();
	} else {
	    oc_uri = "<" + this.getUri() + ">";
	}

	insert += NameSpaces.getInstance().printSparqlNameSpaceList();
    	insert += INSERT_LINE1;
    	insert += oc_uri + " a <" + type + "> . ";
    	insert += oc_uri + " rdfs:label  \"" + this.getLabel() + "\" . ";
	if (this.getStudyUri().startsWith("http")) {
	    insert += oc_uri + " hasco:isObjectCollectionOf  <" + this.getStudyUri() + "> . ";
	} else {
	    insert += oc_uri + " hasco:isObjectCollectionOf  " + this.getStudyUri() + " . ";
	}
	if (this.getComment() != null && !this.getComment().equals("")) {
	    insert += oc_uri + " rdfs:comment  \"" + this.getComment() + "\" . ";
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
    	row.put("hasco:isObjectCollectionOf", getStudyUri());
    	rows.add(row);

	int totalChanged = 0;
    	try {
	    totalChanged = loader.insertRows("ObjectCollection", rows);
	} catch (CommandException e) {
	    try {
		totalChanged = loader.updateRows("ObjectCollection", rows);
	    } catch (CommandException e2) {
		System.out.println("[ERROR] Could not insert or update ObjectCollection(s)");
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
	    System.out.println("deleting Object Collection " + row.get("hasURI"));
	}
    	return loader.deleteRows("ObjectCollection", rows);
    }
    
    public void delete() {
	String query = "";

	String oc_uri = "";
	if (this.getUri().startsWith("<")) {
	    oc_uri = this.getUri();
	} else {
	    oc_uri = "<" + this.getUri() + ">";
	}

	query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += DELETE_LINE1;
    	query += " " + oc_uri + "  ";
        query += DELETE_LINE3;
    	query += LINE_LAST;

    	UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
    }
	
}
