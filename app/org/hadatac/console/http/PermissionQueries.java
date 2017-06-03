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

public class PermissionQueries {

    public static final String PERMISSION_BY_EMAIL       = "PermissionByEmail";
    public static final String PERMISSIONS_BY_URI        = "PermissionsByURI";
    
    public static String querySelector(String concept, String uri){
        String q = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        switch (concept){
            case PERMISSION_BY_EMAIL : 
                q = "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
            		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                    "SELECT * WHERE { " + 
                    "   ?uri a foaf:Person . " + 
                    "   ?uri foaf:mbox \"" + uri + "\" . " +
                    "}";
                break;
            case PERMISSIONS_BY_URI : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?deturi ?detModel ?sp ?ec ?ecName WHERE { " + 
                    "   <" + uri + "> a vstoi:Deployment . " + 
                    "   <" + uri + "> hasco:hasDetector ?deturi .  " +
                    "   ?deturi a ?detModel . " +
                    "   ?sp vstoi:perspectiveOf ?detModel . " +
                    "   ?sp hasco:hasPerspectiveCharacteristic ?ec ." +
                    "   ?ec rdfs:label ?ecName .  " + 
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
    				Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL), query);
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
