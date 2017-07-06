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
import org.hadatac.utils.FirstLabel;
import org.hadatac.metadata.loader.ValueCellProcessing;

public class DataAcquisitionSchemaEvent {

    private String uri;
    private String label;
    private String entity;
    private String entityLabel;
    private String unit;
    private String unitLabel;
    
    public DataAcquisitionSchemaEvent(String uri, String label, String entity, String entityLabel, String unit, String unitLabel) {
	this.uri = uri;
	this.label = label;
	this.entity = entity;
	this.entityLabel = entityLabel;
	this.unit = unit;
	this.unitLabel = unitLabel;
    }
    
    public String getUri() {
	return uri;
    }
    
    public String getLabel() {
	return label;
    }
    
    public String getEntity() {
	return entity;
    }
    
    public String getEntityLabel() {
	if (entityLabel.equals("")) {
	    return ValueCellProcessing.replaceNameSpaceEx(entity);
	}
	return entityLabel;
    }
    
    public String getUnit() {
	return unit;
    }
    
    public String getUnitLabel() {
	if (unitLabel.equals("")) {
	    return ValueCellProcessing.replaceNameSpaceEx(unit);
	}
	return unitLabel;
    }
    
    public static DataAcquisitionSchemaEvent find(String uri) {
	DataAcquisitionSchemaEvent event = null;
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?entity ?unit WHERE { " + 
	    "   <" + uri + "> a hasco:DASchemaEvent . " + 
	    "   OPTIONAL { <" + uri + ">  hasco:hasEntity ?entity } ." + 
	    "   OPTIONAL { <" + uri + "> hasco:hasUnit ?unit } ." + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] DataAcquisitionSchemaEvent. Could not find event for uri: " + uri);
	    return event;
	}
	
	QuerySolution soln = resultsrw.next();
	String labelStr = "";
	String entityStr = "";
	String entityLabelStr = "";
	String unitStr = "";
	String unitLabelStr = "";
	try {
	    if (soln != null) {

		labelStr = FirstLabel.getLabel(uri);
		
		try {
		    if (soln.getResource("entity") != null && soln.getResource("entity").getURI() != null) {
			entityStr = soln.getResource("entity").getURI();
			entityLabelStr = FirstLabel.getLabel(entityStr);
		    }
		} catch (Exception e1) {
		    entityStr = "";
		    entityLabelStr = "";
		}
		
		try {
		    if (soln.getResource("unit") != null && soln.getResource("unit").getURI() != null) {
			unitStr = soln.getResource("unit").getURI();
			unitLabelStr = FirstLabel.getLabel(unitStr);
		    }
		} catch (Exception e1) {
		    unitStr = "";
		    unitLabelStr = "";
		}
		
		event = new DataAcquisitionSchemaEvent(uri,
						       labelStr,
						       entityStr,
						       entityLabelStr,
						       unitStr,
						       unitLabelStr);
	    }
	}  catch (Exception e) {
	    System.out.println("[ERROR] DataAcquisitionSchemaEvent. uri: e.Message: " + e.getMessage());
	}
	
	return event;
    }
    
    public static List<DataAcquisitionSchemaEvent> findBySchema (String schemaUri) {
	//System.out.println("Looking for data acuisition schema events for " + schemaUri);
	List<DataAcquisitionSchemaEvent> objects = new ArrayList<DataAcquisitionSchemaEvent>();
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?uri WHERE { " + 
	    "   ?uri a hasco:DASchemaEvent . " + 
	    "   ?uri hasco:partOfSchema " + schemaUri + " .  " + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] DataAcquisitionSchemaEvent. Could not find events for schema: " + schemaUri);
	    return objects;
	}
	
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    try {
		if (soln != null && soln.getResource("uri") != null && soln.getResource("uri").getURI() != null) {
		    
		    DataAcquisitionSchemaEvent obj = DataAcquisitionSchemaEvent.find(soln.getResource("uri").getURI());
		    if (obj != null) {
			objects.add(obj);
		    }
		}
	    }  catch (Exception e) {
		System.out.println("[ERROR] DataAcquisitionSchemaEvent. uri: e.Message: " + e.getMessage());
	    }
	    
	}
	return objects;
    }
    
}
