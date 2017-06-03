package org.hadatac.console.http;
//This Java Class was Dynamically Generated
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory
;import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
public class GetSparqlQueryDynamic {
    public String collection;
    
    public GetSparqlQueryDynamic () {} 

    public GetSparqlQueryDynamic (SparqlQuery query) {
    	this(Collections.METADATA_SPARQL, query);
    }
    
    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query) {
        this.collection = Collections.getCollectionsName(collectionSource);
        System.out.println("Collection: " + collection);
    }

    public GetSparqlQueryDynamic (SparqlQuery query, String tabName) {
    	this(Collections.METADATA_SPARQL, query, tabName);
    }

    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query, String tabName) {
    	this.collection = Collections.getCollectionsName(collectionSource);
        System.out.println("Collection: " + collection);
    }
    
    public String querySelectorGeneric(String tabName){
    	String indicator = DynamicFunctions.replaceTabNameWithConcept(tabName);
    	System.out.println("In Query Selector: " + indicator + "\n");
    	String q = DynamicFunctions.getPrefixes() + 
    			"SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                "WHERE { " + 
                "  ?id rdfs:subClassOf* " + indicator + ". " + 
                "  ?id rdfs:subClassOf ?superId . " + 
                "  ?id rdfs:label ?label ." + 
                "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                "  OPTIONAL {?id skos:definition ?def} . " + 
                "  OPTIONAL {?id hasco:hasUnit ?unit} . " + 
                "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                "} ";
    	return q;
    }

    public String executeQuery(String tab) throws IllegalStateException, IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + querySelectorGeneric(tab);
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(collection, query);
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