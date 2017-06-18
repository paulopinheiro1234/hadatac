package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;

public class DataAcquisitionSchema {

    private String uri = "";
    private List<DataAcquisitionSchemaAttribute> attributes = null;
    private List<DataAcquisitionSchemaObject> objects = null;
    private List<DataAcquisitionSchemaEvent> events = null;
    private int valueColumn;
    private int timestampColumn;
    private int timeInstantColumn;
    private int idColumn;
    private int elevationColumn;
    
    public DataAcquisitionSchema() {
       this.timestampColumn = -1;
       this.timeInstantColumn = -1;
       this.elevationColumn = -1;
       this.idColumn = -1;
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

    public String getUri() {
    	return uri;
    }

    public void setUri(String uri) {
    	this.uri = uri;
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
		if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("sio:TimeStamp"))) {
		    setTimestampColumn(dasa.getPositionInt());
		    System.out.println("[OK] DataAcquisitionSchemat TimeStampColumn: " + dasa.getPositionInt());
		}
		if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("sio:TimeInstant"))) {
		    setTimeInstantColumn(dasa.getPositionInt());
		    System.out.println("[OK] DataAcquisitionSchemat TimeInstantColumn: " + dasa.getPositionInt());
		}
		if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:originalID"))) {
		    setIdColumn(dasa.getPositionInt());
		    System.out.println("[OK] DataAcquisitionSchemat IdColumn: " + dasa.getPositionInt());
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
		
    	QueryExecution qexec = QueryExecutionFactory.sparqlService(
			Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	boolean uriExist = qexec.execAsk();
        qexec.close();
		
	if (uriExist) {
	    schema.setUri(schemaUri);
	    schema.setAttributes(DataAcquisitionSchemaAttribute.findBySchema(schemaUri));
	    schema.setObjects(DataAcquisitionSchemaObject.findBySchema(schemaUri));
	    schema.setEvents(DataAcquisitionSchemaEvent.findBySchema(schemaUri));
	    System.out.println("[OK] DataAcquisitionSchema " + schemaUri + " exists. " + 
                               "It has " + schema.getAttributes().size() + " attributes, " + 
                               schema.getObjects().size() + " objects, and " + 
			       schema.getEvents().size() + " events.");
	} else {
	    System.out.println("[ERROR] DataAcquisitionSchema could not be found.");
	}
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
		DataAcquisitionSchema schema = new DataAcquisitionSchema();
		schema.setUri(soln.getResource("uri").getURI());
		schemas.add(schema);
	    }
	}
	return schemas;
    }

    public static String getFirstLabel (String uri) {
	if (uri.startsWith("http")) {
	    uri = "<" + uri + ">";
	}
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?label WHERE { " + 
	    "  " + uri + " rdfs:label ?label ." + 
	    "}";
	Query query = QueryFactory.create(queryString);
	QueryExecution qexec = QueryExecutionFactory.sparqlService(
				    Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	String labelStr = "";
	QuerySolution soln;
	while (resultsrw.hasNext()) {
	    soln = resultsrw.next();
	    try {
		if (soln.getLiteral("label") != null && soln.getLiteral("label").getString() != null) {
		    labelStr = soln.getLiteral("label").getString();
		}
	    } catch (Exception e1) {
		labelStr = "";
	    }
	    if (!labelStr.equals("")) {
		break;
	    }
	}
	return labelStr;
	
    }
    
}
