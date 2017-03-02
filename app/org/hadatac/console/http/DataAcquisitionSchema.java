package org.hadatac.console.http;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class DataAcquisitionSchema {

    public static final String ATTRIBUTE_BY_SCHEMA_URI = "AttributeBySchemaURI";
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

    public List<SchemaAttribute> getAttributes() {
    	return attributes;
    }
    
    public void setAttributes(List<SchemaAttribute> attributes) {
		this.attributes = attributes;
	}
    
    public static String querySelector(String concept, String uri){
        // default query?
        String q = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        switch (concept){
            case ATTRIBUTE_BY_SCHEMA_URI : 
            	q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + 
            		"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            		"PREFIX prov: <http://www.w3.org/ns/prov#>  " +
            		"PREFIX vstoi: <http://hadatac.org/ont/vstoi#> " +
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#> " +
        			"PREFIX hasco: <http://hadatac.org/ont/hasco/> " +
        			"SELECT ?uri ?hasPosition ?hasEntity ?hasAttribute ?hasUnit ?hasSource ?isPIConfirmed WHERE { " + 
        			"   ?uri a hasneto:DASchemaAttribute . " + 
        			"   ?uri hasneto:partOfSchema " + "<" + uri + "> .  " + 
        			"   ?uri hasco:hasPosition ?hasPosition .  " + 
        			"   OPTIONAL { ?uri hasneto:hasEntity ?hasEntity } ." + 
        			"   OPTIONAL { ?uri hasneto:hasAttribute ?hasAttribute } ." + 
        			"   OPTIONAL { ?uri hasneto:hasUnit ?hasUnit } ." + 
        			"   OPTIONAL { ?uri hasco:hasSource ?hasSource } ." + 
        			"   OPTIONAL { ?uri hasco:isPIConfirmed ?isPIConfirmed } ." + 
        			"}";
                break;
            default :
            	q = "";
            	System.out.println("WARNING: no query for tab " + concept);
        }
        return q;
    }

    public static String exec(String concept, String uri) {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try {
    		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    					querySelector(concept, uri);
    		Query query = QueryFactory.create(queryString);
    			
    		QueryExecution qexec = QueryExecutionFactory.sparqlService(
    				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    		ResultSet results = qexec.execSelect();
    		
    		ResultSetFormatter.outputAsJSON(outputStream, results);
    		qexec.close();
    		
    		return outputStream.toString("UTF-8");
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return "";
    }
    
    public static DataAcquisitionSchema find(String schemaUri) {
    	DataAcquisitionSchema schema = null;
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				querySelector(ATTRIBUTE_BY_SCHEMA_URI, schemaUri);
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
			SchemaAttribute sa = schema.new SchemaAttribute(
					soln.getLiteral("hasPosition").getString(),
					soln.getResource("hasEntity").getURI(),
					soln.getResource("hasAttribute").getURI(),
					soln.getResource("hasUnit").getURI());
			attributes.add(sa);
		}
		
		schema.setAttributes(attributes);
		
		return schema;
    }
}
