package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.labkey.remoteapi.CommandException;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.routes;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;

public class DataAcquisitionSchema {

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:DASchema;  ";
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "DAS-";
    public static List<String> METADASA = Arrays.asList("sio:TimeStamp", 
							"sio:TimeInstant", 
							"hasco:originalID", 
							"hasco:uriID", 
							"hasco:hasMetaEntity", 
							"hasco:hasMetaEntityURI", 
							"hasco:hasMetaAttribute", 
							"hasco:hasMetaAttributeURI", 
							"hasco:hasMetaUnit", 
							"hasco:hasMetaUnitURI", 
							"sio:InRelationTo",
							"hasco:hasLOD",
							"hasco:hasCalibration",
							"hasco:hasElevation",
							"hasco:hasLocation");
    private String uri = "";
    private String label = "";
    private List<DataAcquisitionSchemaAttribute> attributes = null;
    private List<DataAcquisitionSchemaObject> objects = null;
    private List<DataAcquisitionSchemaEvent> events = null;
    private int timestampColumn;
    private int timeInstantColumn;
    private int idColumn;
    private int elevationColumn;
    private int entityColumn;
    private int unitColumn;
    private int inRelationToColumn;
    
    public DataAcquisitionSchema() {
	this.timestampColumn = -1;
	this.timeInstantColumn = -1;
	this.elevationColumn = -1;
	this.idColumn = -1;
	this.entityColumn = -1;
	this.unitColumn = -1;
	this.inRelationToColumn = -1;
	this.attributes = new ArrayList<DataAcquisitionSchemaAttribute>();
	this.objects = new ArrayList<DataAcquisitionSchemaObject>();
	this.events = new ArrayList<DataAcquisitionSchemaEvent>();
    }
    
    public DataAcquisitionSchema(String uri, String label) {
	this();
	this.uri = uri;
	this.label = label;
    }
    
    public String getUri() {
    	return uri.replace("<","").replace(">","");
    }

    public String getUriNamespace() {
	return ValueCellProcessing.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
    }

    public void setUri(String uri) {
    	this.uri = uri;
    }

    public String getLabel() {
    	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public int getTimestampColumn() {
	return timestampColumn;
    }

    public void setTimestampColumn(int timestampColumn) {
	this.timestampColumn = timestampColumn;
    }

    public int getTimeInstantColumn() {
	return timeInstantColumn;
    }

    public void setTimeInstantColumn(int timeInstantColumn) {
	this.timeInstantColumn = timeInstantColumn;
    }

    public int getIdColumn() {
	return idColumn;
    }

    public void setIdColumn(int idColumn) {
	this.idColumn = idColumn;
    }

    public int getElevationColumn() {
	return elevationColumn;
    }

    public void setElevationColumn(int elevationColumn) {
	this.elevationColumn = elevationColumn;
    }

    public int getEntityColumn() {
	return entityColumn;
    }

    public void setEntityColumn(int entityColumn) {
	this.entityColumn = entityColumn;
    }

    public int getUnitColumn() {
	return unitColumn;
    }

    public void setUnitColumn(int unitColumn) {
	this.unitColumn = unitColumn;
    }

    public int getInRelationToColumn() {
	return inRelationToColumn;
    }

    public void setInRelationToColumn(int inRelationToColumn) {
	this.inRelationToColumn = inRelationToColumn;
    }

    public int getTotalDASA() {
	if (attributes == null) {
	    return -1;
	}
    	return attributes.size();
    }

    public int getTotalDASE() {
	if (events == null) {
	    return -1;
	}
    	return events.size();
    }

    public int getTotalDASO() {
	if (objects == null) {
	    return -1;
	}
    	return objects.size();
    }

    public List<DataAcquisitionSchemaAttribute> getAttributes() {
    	return attributes;
    }
    
    public void setAttributes(List<DataAcquisitionSchemaAttribute> attributes) {
	if (attributes == null) {
	   System.out.println("[ERROR] No DataAcquisitionSchemaAttribute for " + uri + " is defined in the knowledge base. ");
	} else {
	    this.attributes = attributes;
	    for (DataAcquisitionSchemaAttribute dasa : attributes) {
		dasa.setDataAcquisitionSchema(this);
		if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("sio:TimeStamp"))) {
		    setTimestampColumn(dasa.getPositionInt());
		    System.out.println("[OK] DataAcquisitionSchema TimeStampColumn: " + dasa.getPositionInt());
		}
		if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("sio:TimeInstant"))) {
		    setTimeInstantColumn(dasa.getPositionInt());
		    System.out.println("[OK] DataAcquisitionSchema TimeInstantColumn: " + dasa.getPositionInt());
		}
		if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:originalID")) || 
		    dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:uriID")) ) {
		    setIdColumn(dasa.getPositionInt());
		    System.out.println("[OK] DataAcquisitionSchema IdColumn: " + dasa.getPositionInt());
		}
		if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:hasEntity"))) {
		    setEntityColumn(dasa.getPositionInt());
		    System.out.println("[OK] DataAcquisitionSchema EntityColumn: " + dasa.getPositionInt());
		}
		if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:hasUnit"))) {
		    setUnitColumn(dasa.getPositionInt());
		    System.out.println("[OK] DataAcquisitionSchema UnitColumn: " + dasa.getPositionInt());
		}
		if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("sio:InRelationTo"))) {
		    setInRelationToColumn(dasa.getPositionInt());
		    System.out.println("[OK] DataAcquisitionSchema InRelationToColumn: " + dasa.getPositionInt());
		}
		System.out.println("[OK] DataAcquisitionSchemaAttribute <" + dasa.getUri() + "> is defined in the knowledge base. " + 
				   "Entity: \""    + dasa.getEntityLabel()     + "\"; " + 
				   "Attribute: \"" + dasa.getAttributeLabel() + "\"; " + 
				   "Unit: \""      + dasa.getUnitLabel()       + "\""); 
		//System.out.println("     DataAcquisitionSchemaAttribute DASO URI: \"" + dasa.getObjectUri() + "\"");
		//System.out.println("     DataAcquisitionSchemaAttribute DASE URI: \"" + dasa.getEventUri() + "\"");
	    }
	}

    }
    
    public List<DataAcquisitionSchemaObject> getObjects() {
    	return objects;
    }
    
    public void setObjects(List<DataAcquisitionSchemaObject> objects) {
	if (objects == null) {
	   System.out.println("[WARNING] No DataAcquisitionSchemaObject for " + uri + " is defined in the knowledge base. ");
	} else {
	    this.objects = objects;
	    for (DataAcquisitionSchemaObject daso : objects) {
		System.out.println("[OK] DataAcquisitionSchemaObject <" + daso.getUri() + "> is defined in the knowledge base. " + 
				   "Role: \""  + daso.getRole() + "\"");
	    }
	}
    }

    public DataAcquisitionSchemaObject getObject(String dasoUri) {
	for (DataAcquisitionSchemaObject daso : objects) {
	    if (daso.getUri().equals(dasoUri)) {
		return daso;
	    }
	}
	return null;
    }
    
    public List<DataAcquisitionSchemaEvent> getEvents() {
    	return events;
    }
    
    public void setEvents(List<DataAcquisitionSchemaEvent> events) {
	if (events == null) {
	   System.out.println("[WARNING] No DataAcquisitionSchemaEvent for " + uri + " is defined in the knowledge base. ");
	} else {
	    this.events = events;
	    for (DataAcquisitionSchemaEvent dase : events) {
		System.out.println("[OK] DataAcquisitionSchemaEvent <" + dase.getUri() + "> is defined in the knowledge base. " + 
				   "Label: \""  + dase.getLabel() + "\"");
	    }
	}
    }

    public DataAcquisitionSchemaEvent getEvent(String daseUri) {
	for (DataAcquisitionSchemaEvent dase : events) {
	    if (dase.getUri().equals(daseUri)) {
		return dase;
	    }
	}
	return null;
    }
    
    public static DataAcquisitionSchema find(String schemaUri) {
	System.out.println("Looking for data acquisition schema " + schemaUri);
	DataAcquisitionSchema schema = new DataAcquisitionSchema();
	if (schemaUri == null || schemaUri.equals("")) {
	    System.out.println("[ERROR] DataAcquisitionSchema URI blank or null.");
	    return schema;
	}
	if (schemaUri.startsWith("http")) {
	    schemaUri = "<" + schemaUri + ">";
	}
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    " ASK { " + schemaUri + " a hasco:DASchema . } ";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	boolean uriExist = qexec.execAsk();
        qexec.close();
	
	if (!uriExist) {
	    System.out.println("[WARNING] DataAcquisitionSchema. Could not find schema for uri: " + schemaUri);
	    return schema;
	}
	
	schema.setUri(schemaUri);
	schema.setLabel(FirstLabel.getLabel(schemaUri));
	schema.setAttributes(DataAcquisitionSchemaAttribute.findBySchema(schemaUri));
	schema.setObjects(DataAcquisitionSchemaObject.findBySchema(schemaUri));
	schema.setEvents(DataAcquisitionSchemaEvent.findBySchema(schemaUri));
	System.out.println("[OK] DataAcquisitionSchema " + schemaUri + " exists. " + 
			   "It has " + schema.getAttributes().size() + " attributes, " + 
			   schema.getObjects().size() + " objects, and " + 
			   schema.getEvents().size() + " events.");
	return schema;
    }
    	
    public static List<DataAcquisitionSchema> findAll() {
    	List<DataAcquisitionSchema> schemas = new ArrayList<DataAcquisitionSchema>();
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    			"SELECT ?uri WHERE { " + 
    			"   ?uri a hasco:DASchema . } ";
    	Query query = QueryFactory.create(queryString);
    	QueryExecution qexec = QueryExecutionFactory.sparqlService(
			Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null && soln.getResource("uri").getURI() != null) { 
		DataAcquisitionSchema schema = DataAcquisitionSchema.find(soln.getResource("uri").getURI());
		schemas.add(schema);
	    }
	}
	return schemas;
    }

    public static DataAcquisitionSchema create(String uri) {
	DataAcquisitionSchema das = new DataAcquisitionSchema();
	das.setUri(uri);
	return das;
    }
    
    public void save() {
	// SAVING DAS's DASAs
	for (DataAcquisitionSchemaAttribute dasa : attributes) {
	    dasa.save();
	}

	// SAVING DAS ITSELF
	String insert = "";
	insert += NameSpaces.getInstance().printSparqlNameSpaceList();
    	insert += INSERT_LINE1;
    	insert += this.getUri() + " a hasco:DASchema . ";
    	insert += this.getUri() + " rdfs:label  \"" + this.getLabel() + "\" . ";
    	insert += LINE_LAST;
	//System.out.println(insert);
    	UpdateRequest request = UpdateFactory.create(insert);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				      request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public int saveToLabKey(String user_name, String password) throws CommandException {
	// SAVING DAS's DASAs
	for (DataAcquisitionSchemaAttribute dasa : attributes) {
	    //System.out.println("Saving DASA " + dasa.getUri() + " into LabKey");
	    dasa.saveToLabKey(user_name, password);
	}

	// SAVING DAS ITSELF
	String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
    	LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
    	List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
    	row.put("a", "hasco:DataAcquisitionSchema");
    	row.put("rdfs:label", getLabel());
    	rows.add(row);
    	return loader.insertRows("DASchema", rows);
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public int deleteFromLabKey(String user_name, String password) throws CommandException {
	// DELETING DAS's DASAs
	for (DataAcquisitionSchemaAttribute dasa : attributes) {
	    //System.out.println("Deleting DASA " + dasa.getUri() + " from LabKey");
	    dasa.deleteFromLabKey(user_name, password);
	}

	// DELETING DAS ITSELF
	String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
    	LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
    	List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
    	rows.add(row);
    	return loader.deleteRows("DASchema", rows);
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
	
}
