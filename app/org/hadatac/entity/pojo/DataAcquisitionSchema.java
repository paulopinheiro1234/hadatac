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

public class DataAcquisitionSchema {

	private String uri = "";
    private List<SchemaAttribute> attributes = null;
    
    public class SchemaAttribute {
    	private String position;
    	private String entity;
    	private String attribute;
    	private String unit;
    	
    	public SchemaAttribute(String position, String entity, String attribute, String unit) {
    		this.position = position;
    		this.entity = entity;
    		this.attribute = attribute;
    		this.unit = unit;
		}
    	
    	public String getPosition() {
    		return position;
		}
    	public String getEntity() {
    		return entity;
		}
    	public String getAttribute() {
    		return attribute;
		}
    	public String getUnit() {
    		return unit;
		}
    }
    
    public String getUri() {
    	return uri;
	}
    public void setUri(String uri) {
    	this.uri = uri;
	}

    public List<SchemaAttribute> getAttributes() {
    	return attributes;
    }
    
    public void setAttributes(List<SchemaAttribute> attributes) {
		this.attributes = attributes;
	}
    
    public static DataAcquisitionSchema find(String schemaUri) {
    	System.out.println("Looking for schema " + schemaUri);
    	DataAcquisitionSchema schema = null;
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    			"SELECT ?uri ?hasPosition ?hasEntity ?hasAttribute ?hasUnit ?hasSource ?isPIConfirmed WHERE { " + 
    			"   ?uri a hasneto:DASchemaAttribute . " + 
    			"   ?uri hasneto:partOfSchema " + "<" + schemaUri + "> .  " + 
    			"   ?uri hasco:hasPosition ?hasPosition .  " + 
    			"   OPTIONAL { ?uri hasneto:hasEntity ?hasEntity } ." + 
    			"   OPTIONAL { ?uri hasneto:hasAttribute ?hasAttribute } ." + 
    			"   OPTIONAL { ?uri hasneto:hasUnit ?hasUnit } ." + 
    			"   OPTIONAL { ?uri hasco:hasSource ?hasSource } ." + 
    			"   OPTIONAL { ?uri hasco:isPIConfirmed ?isPIConfirmed } ." + 
    			"}";
    	Query query = QueryFactory.create(queryString);
		
    	QueryExecution qexec = QueryExecutionFactory.sparqlService(
			Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (!resultsrw.hasNext()) {
			return schema;
		}
		
		schema = new DataAcquisitionSchema();
		List<SchemaAttribute> attributes = new ArrayList<SchemaAttribute>();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln.getLiteral("hasPosition") != null && soln.getLiteral("hasPosition").getString() != null &&
			    soln.getResource("hasEntity") != null && soln.getResource("hasEntity").getURI() != null &&
			    soln.getResource("hasAttribute") != null && soln.getResource("hasAttribute").getURI() != null &&
			    soln.getResource("hasUnit") != null && soln.getResource("hasUnit").getURI() != null) {
			    SchemaAttribute sa = schema.new SchemaAttribute(
					soln.getLiteral("hasPosition").getString(),
					soln.getResource("hasEntity").getURI(),
					soln.getResource("hasAttribute").getURI(),
					soln.getResource("hasUnit").getURI());
			    attributes.add(sa);
			}
		}
		schema.setAttributes(attributes);
		
		return schema;
    }
    
    public static List<DataAcquisitionSchema> findAll() {
    	List<DataAcquisitionSchema> schemas = new ArrayList<DataAcquisitionSchema>();
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    			"SELECT ?uri WHERE { " + 
    			"   ?uri a hasco:DASchema . } ";
    	System.out.println(NameSpaces.getInstance().printSparqlNameSpaceList());
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
}
