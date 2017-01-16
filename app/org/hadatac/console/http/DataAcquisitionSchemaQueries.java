package org.hadatac.console.http;

import java.io.ByteArrayOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import play.Play;

public class DataAcquisitionSchemaQueries {

    public static final String ATTRIBUTE_BY_SCHEMA_URI                 = "AttributeBySchemaURI";
    
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
}
