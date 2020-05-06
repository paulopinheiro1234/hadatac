package org.hadatac.console.http;

import java.io.ByteArrayOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.utils.CollectionUtil;
    
public class GenericSparqlQuery {

    //Inputs: None. Executes query based on the member string sparql_query.
    //Output: Returns JSON in the form of a string. Currently does not handle http errors
    //		  very gracefully. Need to change this.
    //Postconditions: None
    public static String execute(String str_query, boolean isUpdate) {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try {    		
    		ResultSetRewindable resultsrw = SPARQLUtils.select(CollectionUtil.getCollectionPath(
    		        CollectionUtil.Collection.METADATA_SPARQL), str_query);
    		
    		ResultSetFormatter.outputAsJSON(outputStream, resultsrw);
    		
    		return outputStream.toString("UTF-8");
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return "";
    }
}
