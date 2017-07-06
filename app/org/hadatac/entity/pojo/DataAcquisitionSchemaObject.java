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

public class DataAcquisitionSchemaObject {

    private String uri;
    private String label;
    private String entity;
    private String entityLabel;
    private String role;
    private String inRelationTo;
    private String inRelationToLabel;
    private String relation;
    private String relationLabel;
    
    public DataAcquisitionSchemaObject(String uri, 
				       String label, 
				       String entity, 
				       String entityLabel, 
				       String role, 
				       String inRelationTo, 
				       String inRelationToLabel, 
				       String relation, 
				       String relationLabel) {
	this.uri = uri;
	this.label = label;
	this.entity = entity;
	this.entityLabel = entityLabel;
	this.role = role;
	this.inRelationTo = inRelationTo;
	this.inRelationToLabel = inRelationToLabel;
	this.relation = relation;
	this.relationLabel = relationLabel;
    }
    
    public String getUri() {
	return uri;
    }
    
    public String getUriNamespace() {
	return ValueCellProcessing.replaceNameSpaceEx(uri);
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
    
    public String getRole() {
	return role;
    }
    
    public String getInRelationTo() {
	return inRelationTo;
    }
    
    public String getInRelationToLabel() {
	if (inRelationToLabel.equals("")) {
	    return ValueCellProcessing.replaceNameSpaceEx(inRelationTo);
	}
	return inRelationToLabel;
    }
    
    public String getRelation() {
	return relation;
    }
    
    public String getRelationLabel() {
	if (relationLabel.equals("")) {
	    return ValueCellProcessing.replaceNameSpaceEx(relation);
	}
	return relationLabel;
    }
    
    public static DataAcquisitionSchemaObject find (String uri) {
	//System.out.println("Looking for data acquisition schema objects with uri: " + uri);
	DataAcquisitionSchemaObject object = null;
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?entity  ?role ?inRelationTo ?relation WHERE { " + 
	    "   <" + uri + "> a hasco:DASchemaObject . " + 
	    "   OPTIONAL { <" + uri + "> hasco:hasEntity ?entity } . " + 
	    "   OPTIONAL { <" + uri + "> hasco:hasRole ?role } .  " + 
	    "   OPTIONAL { <" + uri + "> sio:inRelationTo ?inRelationTo } . " + 
	    "   OPTIONAL { <" + uri + "> sio:relation ?relation } . " + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(
				    Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] DataAcquisitionSchemaObject. Could not find object with uri: " + uri);
	    return null;
	}
	
	QuerySolution soln = resultsrw.next();
	String labelStr = "";
	String entityStr = "";
	String entityLabelStr = "";
	String hasRoleStr = "";
	String inRelationToStr = "";
	String inRelationToLabelStr = "";
	String relationStr = "";
	String relationLabelStr = "";

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
		    if (soln.getLiteral("hasRole") != null && soln.getLiteral("hasRole").getString() != null) {
			hasRoleStr = soln.getLiteral("hasRole").getString();
		    } 
		} catch (Exception e1) {
		    hasRoleStr = "";
		}
		
		try {
		    if (soln.getResource("inRelationTo") != null && soln.getResource("inRelationTo").getURI() != null) {
			inRelationToStr = soln.getResource("inRelationTo").getURI();
			inRelationToLabelStr = FirstLabel.getLabel(inRelationToStr);
		    }
		} catch (Exception e1) {
		    inRelationToStr = "";
		    inRelationToLabelStr = "";
		}
		
		try {
		    if (soln.getResource("relation") != null && soln.getResource("relation").getURI() != null) {
			relationStr = soln.getResource("relation").getURI();
			relationLabelStr = FirstLabel.getLabel(relationStr);
		    }
		} catch (Exception e1) {
		    relationStr = "";
		    relationLabelStr = "";
		}
		
		object = new DataAcquisitionSchemaObject(uri,
							 labelStr,
							 entityStr,
							 entityLabelStr,
							 hasRoleStr,
							 inRelationToStr,
							 inRelationToLabelStr,
							 relationStr,
							 relationLabelStr);
	    }
	}  catch (Exception e) {
	    System.out.println("[ERROR] DataAcquisitionSchemaObject. uri: e.Message: " + e.getMessage());
	}
	return object;
    }
    
    public static List<DataAcquisitionSchemaObject> findBySchema (String schemaUri) {
	//System.out.println("Looking for data acquisition schema objectss for " + schemaUri);
	List<DataAcquisitionSchemaObject> objects = new ArrayList<DataAcquisitionSchemaObject>();
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?uri ?label ?hasEntity ?hasRole ?inRelationTo ?relation WHERE { " + 
	    "   ?uri a hasco:DASchemaObject . " + 
	    "   ?uri hasco:partOfSchema " + schemaUri + " .  " + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(
				    Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] DataAcquisitionSchemaObject. Could not find objects for schema: " + schemaUri);
	    return objects;
	}
	
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    try {
		if (soln != null && soln.getResource("uri") != null && soln.getResource("uri").getURI() != null) {
		    
		    DataAcquisitionSchemaObject obj = DataAcquisitionSchemaObject.find(soln.getResource("uri").getURI());
		    if (obj != null) {
			objects.add(obj);
		    }
		}
	    }  catch (Exception e) {
		System.out.println("[ERROR] DataAcquisitionSchemaObject. uri: e.Message: " + e.getMessage());
	    }
	    
	}
	return objects;
    }
    
}
