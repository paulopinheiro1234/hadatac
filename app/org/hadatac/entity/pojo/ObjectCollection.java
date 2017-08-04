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
    private String hasScopeUri = "";    
    private String spaceScopeUri = "";
    private String timeScopeUri = "";
    private List<String> objectUris = null;

    public ObjectCollection() {
	this.uri = "";
	this.type = "";
	this.label = "";
	this.comment = "";
	this.studyUri = "";
	this.hasScopeUri = "";
	this.spaceScopeUri = "";
	this.timeScopeUri = "";
	this.objectUris = new ArrayList<String>();
    }
    
    public ObjectCollection(String uri,
			    String type,
			    String label,
			    String comment,
			    String studyUri,
			    String hasScopeUri,
			    String spaceScopeUri,
			    String timeScopeUri) {
	this.setUri(uri);
	this.setType(type);
	this.setLabel(label);
	this.setComment(comment);
	this.setStudyUri(studyUri);
	this.setHasScopeUri(hasScopeUri);
	this.setSpaceScopeUri(spaceScopeUri);
	this.setTimeScopeUri(timeScopeUri);
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

    public String getHasScopeUri() {
	return hasScopeUri;
    }

    public ObjectCollection getHasScope() {
	if (hasScopeUri == null || hasScopeUri.equals("")) {
	    return null;
	}
	return ObjectCollection.find(hasScopeUri);
    }

    public void setHasScopeUri(String hasScopeUri) {
	this.hasScopeUri = hasScopeUri;
    }

    public String getSpaceScopeUri() {
	return spaceScopeUri;
    }

    public ObjectCollection getSpaceScope() {
	if (spaceScopeUri == null || spaceScopeUri.equals("")) {
	    return null;
	}
	return ObjectCollection.find(spaceScopeUri);
    }

    public void setSpaceScopeUri(String spaceScopeUri) {
	this.spaceScopeUri = spaceScopeUri;
    }

    public String getTimeScopeUri() {
	return timeScopeUri;
    }

    public ObjectCollection getTimeScope() {
	if (timeScopeUri == null || timeScopeUri.equals("")) {
	    return null;
	}
	return ObjectCollection.find(timeScopeUri);
    }

    public void setTimeScopeUri(String timeScopeUri) {
	this.timeScopeUri = timeScopeUri;
    }

    public static ObjectCollection find(String oc_uri) {
	oc_uri = URLDecoder.decode(oc_uri);
	ObjectCollection oc = null;
	System.out.println("Looking for object collection with URI " + oc_uri);
	if (oc_uri.startsWith("http")) {
	    oc_uri = "<" + oc_uri + ">";
	}
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT  ?ocType ?comment ?studyUri ?hasScopeUri ?spaceScopeUri ?timeScopeUri WHERE { " + 
	    "    " + oc_uri + " a ?ocType . " + 
	    "    " + oc_uri + " hasco:isMemberOf ?studyUri .  " + 
	    "    OPTIONAL { " + oc_uri + " rdfs:comment ?comment } . " + 
	    "    OPTIONAL { " + oc_uri + " hasco:hasScope ?hasScopeUri } . " + 
	    "    OPTIONAL { " + oc_uri + " hasco:hasSpaceScope ?spaceScopeUri } . " + 
	    "    OPTIONAL { " + oc_uri + " hasco:hasTimeScope ?timeScopeUri } . " + 
	    "}";
	System.out.println("Search In ObjectCollection: [" + queryString + "]");
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] ObjectCollection. Could not find OC with URI: " + oc_uri);
	    return oc;
	}
	
	String typeStr = "";
	String labelStr = "";
	String studyUriStr = "";
	String commentStr = "";
	String hasScopeUriStr = "";
	String spaceScopeUriStr = "";
	String timeScopeUriStr = "";
	
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
		
		try {
		    if (soln.getResource("hasScopeUri") != null && soln.getResource("hasScopeUri").getURI() != null) {
			hasScopeUriStr = soln.getResource("hasScopeUri").getURI();
		    }
		} catch (Exception e1) {
		    hasScopeUriStr = "";
		}
		
		try {
		    if (soln.getResource("spaceScopeUri") != null && soln.getResource("spaceScopeUri").getURI() != null) {
			spaceScopeUriStr = soln.getResource("spaceScopeUri").getURI();
		    }
		} catch (Exception e1) {
		    spaceScopeUriStr = "";
		}
		
		try {
		    if (soln.getResource("timeScopeUri") != null && soln.getResource("timeScopeUri").getURI() != null) {
			timeScopeUriStr = soln.getResource("timeScopeUri").getURI();
		    }
		} catch (Exception e1) {
		    timeScopeUriStr = "";
		}
		
		oc = new ObjectCollection(oc_uri,
					  typeStr,
					  labelStr,
					  commentStr,
					  studyUriStr,
					  hasScopeUriStr,
					  spaceScopeUriStr,
					  timeScopeUriStr);
	    }
	}

	
	// retrieve URIs of objects that are member of the collection
	String queryMemberStr = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT  ?uriMember WHERE { " + 
	    "    ?uriMember hasco:isMemberOf " + oc_uri + " .  " + 
	    "}";

	System.out.println("Second query: " + queryMemberStr);
	Query queryMember = QueryFactory.create(queryMemberStr);
	QueryExecution qexecMember = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), queryMember);
	ResultSet resultsMember = qexecMember.execSelect();
	ResultSetRewindable resultsrwMember = ResultSetFactory.copyResults(resultsMember);
	qexecMember.close();
	
	if (resultsrwMember.hasNext()) {
	
	    String uriMemberStr = "";
	    
	    while (resultsrwMember.hasNext()) {
		QuerySolution soln = resultsrwMember.next();
		if (soln != null) {
		    try {
			if (soln.getResource("uriMember") != null && soln.getResource("uriMember").getURI() != null) {
			    uriMemberStr = soln.getResource("uriMember").getURI();
			    oc.getObjectUris().add(uriMemberStr);
			}
		    } catch (Exception e1) {
			uriMemberStr = "";
		    }
		    
		}
	    }
	}
	return oc;
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
	    "   ?uri hasco:isMemberOf  <" + study.getUri() + "> . " +
	    " } ";
	System.out.println("Query for findByStudy : " + queryString);
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

    private void saveObjectUris(String oc_uri) {
	if (objectUris == null || objectUris.size() == 0) {
	    return;
	}

	String insert = "";
	
	insert += NameSpaces.getInstance().printSparqlNameSpaceList();
    	insert += INSERT_LINE1;
	for (String uri : objectUris) {
	    if (uri != null && !uri.equals("")) {
		if (uri.startsWith("http")) {
		    insert += "  <" + uri + "> hasco:isMemberOf  " + oc_uri + " . ";
		} else {
		    insert += "  " + uri + " hasco:isMemberOf  " + oc_uri + " . ";
		}
	    }
	}
    	insert += LINE_LAST;
	System.out.println(insert);
    	UpdateRequest request = UpdateFactory.create(insert);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				      request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
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
	    insert += oc_uri + " hasco:isMemberOf  <" + this.getStudyUri() + "> . ";
	} else {
	    insert += oc_uri + " hasco:isMemberOf  " + this.getStudyUri() + " . ";
	}
	if (this.getComment() != null && !this.getComment().equals("")) {
	    insert += oc_uri + " rdfs:comment  \"" + this.getComment() + "\" . ";
	}
	if (this.getHasScopeUri() != null && !this.getHasScopeUri().equals("")) {
	    if (this.getHasScopeUri().startsWith("http")) {
		insert += oc_uri + " hasco:hasScope  <" + this.getHasScopeUri() + "> . ";
	    } else {
		insert += oc_uri + " hasco:hasScope  " + this.getHasScopeUri() + " . ";
	    }
	}
	if (this.getSpaceScopeUri() != null && !this.getSpaceScopeUri().equals("")) {
	    if (this.getSpaceScopeUri().startsWith("http")) {
		insert += oc_uri + " hasco:hasSpaceScope  <" + this.getSpaceScopeUri() + "> . ";
	    } else {
		insert += oc_uri + " hasco:hasSpaceScope  " + this.getSpaceScopeUri() + " . ";
	    }
	}
	if (this.getTimeScopeUri() != null && !this.getTimeScopeUri().equals("")) {
	    if (this.getTimeScopeUri().startsWith("http")) {
		insert += oc_uri + " hasco:hasTimeScope  <" + this.getTimeScopeUri() + "> . ";
	    } else {
		insert += oc_uri + " hasco:hasTimeScope  " + this.getTimeScopeUri() + " . ";
	    }
	}
    	insert += LINE_LAST;
	System.out.println(insert);
    	UpdateRequest request = UpdateFactory.create(insert);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				      request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
	saveObjectUris(oc_uri);
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
    	row.put("hasco:isMemberOf", getStudyUri());
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
