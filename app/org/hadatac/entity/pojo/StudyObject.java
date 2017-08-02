package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.utils.ConfigProp;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.labkey.remoteapi.CommandException;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;

public class StudyObject extends HADatAcThing {

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:StudyObject;  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "OBJ-";

    String originalId;
    String isFrom;
    String isMemberOf;

    public StudyObject() {
	this("","");
    }

    public StudyObject(String uri, String isMemberOf) {
	this.setUri(uri);
	this.setType("");
	this.setOriginalId("");
	this.setLabel("");
	this.setIsMemberOf(isMemberOf);
	this.setComment("");
	this.setIsFrom("");
    }

    public StudyObject(String uri,
		  String type,
		  String originalId,
		  String label,
		  String isMemberOf,
		  String comment,
		  String isFrom) { 
	this.setUri(uri);
        this.setType(type);
	this.setOriginalId(originalId);
	this.setLabel(label);
	this.setIsMemberOf(isMemberOf);
	this.setComment(comment);
	this.setIsFrom(isFrom);
    }
    
    public StudyObjectType getStudyObjectType() {
	if (type == null || type.equals("")) {
	    return null;
	}
	return StudyObjectType.find(type);
    }
    
    public String getOriginalId() {
    	return originalId;
    }

    public void setOriginalId(String originalId) {
	this.originalId = originalId;
    }
    
    public String getIsFrom() {
	return isFrom;
    }
    
    public void setIsFrom(String isFrom) {
	this.isFrom = isFrom;
    }	
    
    public String getIsMemberOf() {
	return isMemberOf;
    }
    
    public void setIsMemberOf(String isMemberOf) {
	this.isMemberOf = isMemberOf;
    }	
    
    public static StudyObject find(String obj_uri) {
	StudyObject obj = null;
	System.out.println("Looking for object with URI " + obj_uri);
	if (obj_uri.startsWith("http")) {
	    obj_uri = "<" + obj_uri + ">";
	}
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT  ?objType ?originalId ?isMemberOf ?hasLabel " + 
	    " ?hasComment WHERE { " + 
	    "    " + obj_uri + " a ?objType . " + 
	    "    " + obj_uri + " hasco:isMemberOf ?isMemberOf .  " + 
	    "    OPTIONAL { " + obj_uri + " hasco:originalID ?originalId } . " + 
	    "    OPTIONAL { " + obj_uri + " rdfs:label ?hasLabel } . " + 
	    "    OPTIONAL { " + obj_uri + " rdfs:comment ?hasComment } . " + 
	    "    OPTIONAL { " + obj_uri + " hasco:isFrom ?isFrom } . " + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] StudyObject. Could not find OBJ with URI: " + obj_uri);
	    return obj;
	}
	
	String typeStr = "";
	String originalIdStr = "";
	String labelStr = "";
	String isMemberOfStr = "";
	String commentStr = "";
	String isFromStr = "";
	
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null) {
		
		try {
		    if (soln.getResource("objType") != null && soln.getResource("objType").getURI() != null) {
			typeStr = soln.getResource("objType").getURI();
		    }
		} catch (Exception e1) {
		    typeStr = "";
		}
		
		try {
		    if (soln.getLiteral("originalId") != null && soln.getLiteral("originalId").getString() != null) {
			originalIdStr = soln.getLiteral("originalId").getString();
		    }
		} catch (Exception e1) {
		    originalIdStr = "";
		}
		
		labelStr = FirstLabel.getLabel(obj_uri);

		try {
		    if (soln.getResource("isMemberOf") != null && soln.getResource("isMemberOf").getURI() != null) {
			isMemberOfStr = soln.getResource("isMemberOf").getURI();
		    }
		} catch (Exception e1) {
		    isMemberOfStr = "";
		}
		
		try {
		    if (soln.getLiteral("hasComment") != null && soln.getLiteral("hasComment").getString() != null) {
			commentStr = soln.getLiteral("hasComment").getString();
		    }
		} catch (Exception e1) {
		    commentStr = "";
		}
		
		try {
		    if (soln.getResource("isFrom") != null && soln.getResource("isFrom").getURI() != null) {
			isFromStr = soln.getResource("isFrom").getURI();
		    }
		} catch (Exception e1) {
		    isFromStr = "";
		}
		
		obj = new StudyObject(obj_uri,
				typeStr,
				originalIdStr,
				labelStr,
				isMemberOfStr,
				commentStr,
				isFromStr);
	    }
	}
	return obj;
    }
    
    public static List<StudyObject> findByCollection(ObjectCollection oc) {
	if (oc == null) {
	    return null;
	}
    	List<StudyObject> objects = new ArrayList<StudyObject>();
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?uri WHERE { " + 
	    "   ?uri hasco:isMemberOf  <" + oc.getUri() + "> . " +
	    " } ";
	System.out.println("StudyObject findByCollection: " + queryString);
    	Query query = QueryFactory.create(queryString);
    	QueryExecution qexec = QueryExecutionFactory.sparqlService(
			Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null && soln.getResource("uri").getURI() != null) { 
		System.out.println("URI: [" + soln.getResource("uri").getURI() + "]");
		StudyObject object = StudyObject.find(soln.getResource("uri").getURI());
		objects.add(object);
	    }
	}
	return objects;
    }

    public void save() {
	delete();  // delete any existing triple for the current OBJ
	System.out.println("Saving <" + uri + ">");
	if (uri == null || uri.equals("")) {
	    System.out.println("[ERROR] Trying to save OBJ without assigning an URI");
	    return;
	}
	if (isMemberOf == null || isMemberOf.equals("")) {
	    System.out.println("[ERROR] Trying to save OBJ without assigning DAS's URI");
	    return;
	}
	String insert = "";

	String obj_uri = "";
	if (this.getUri().startsWith("<")) {
	    obj_uri = this.getUri();
	} else {
	    obj_uri = "<" + this.getUri() + ">";
	}

	    
	insert += NameSpaces.getInstance().printSparqlNameSpaceList();
    	insert += INSERT_LINE1;
	if (type.startsWith("http")) {
	    insert += obj_uri + " a <" + type + "> . ";
	} else {
	    insert += obj_uri + " a " + type + " . ";
	}
	if (!originalId.equals("")) {
	    insert += obj_uri + " hasco:originalId \""  + originalId + "\" .  ";
	}   
	if (!label.equals("")) {
	    insert += obj_uri + " rdfs:label  \"" + label + "\" . ";
	}
	if (!isMemberOf.equals("")) {
	    if (isMemberOf.startsWith("http")) {
		insert += obj_uri + " hasco:isMemberOf <" + isMemberOf + "> .  "; 
	    } else {
		insert += obj_uri + " hasco:isMemberOf " + isMemberOf + " .  "; 
	    } 
	}
	if (!comment.equals("")) {
	    insert += obj_uri + " hasco:hasComment \""  + comment + "\" .  ";
	}   
	if (!isFrom.equals("")) {
	    if (isFrom.startsWith("http")) {
		insert += obj_uri + " hasco:isFrom <" + isFrom + "> .  "; 
	    } else {
		insert += obj_uri + " hasco:isFrom " + isFrom + " .  "; 
	    } 
	}
	//insert += this.getUri() + " hasco:hasSource " + " .  "; 
	//insert += this.getUri() + " hasco:isPIConfirmed " + " .  "; 
    	insert += LINE_LAST;
	System.out.println("OBJ insert query (pojo's save): <" + insert + ">");
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
    	row.put("hasco:originalID", getOriginalId());
    	row.put("rdfs:label", getLabel());
    	row.put("hasco:isMemberOf", ValueCellProcessing.replaceNameSpaceEx(getIsMemberOf()));
    	row.put("rdfs:comment", getComment());
    	row.put("hasco:isFrom", ValueCellProcessing.replaceNameSpaceEx(getIsFrom()));
    	rows.add(row);
	int totalChanged = 0;
    	try {
	    totalChanged = loader.insertRows("StudyObject", rows);
	} catch (CommandException e) {
	    try {
		totalChanged = loader.updateRows("StudyObject", rows);
	    } catch (CommandException e2) {
		System.out.println("[ERROR] Could not insert or update Object(s)");
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
	    System.out.println("deleting object " + row.get("hasURI"));
	}
    	return loader.deleteRows("StudyObject", rows);
    }
    
    public void delete() {
	String query = "";
	if (this.getUri() == null || this.getUri().equals("")) {
	    return;
	}
	query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += DELETE_LINE1;
	if (this.getUri().startsWith("http")) {
	    query += "<" + this.getUri() + ">";
	} else {
	    query += this.getUri();
	}
        query += DELETE_LINE3;
    	query += LINE_LAST;
	//System.out.println("SPARQL query inside obj poho's delete: " + query);
    	UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
    }
    
}
