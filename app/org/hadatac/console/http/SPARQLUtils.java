package org.hadatac.console.http;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;


public class SPARQLUtils {
    
    public static ResultSetRewindable select(
            String sparqlService, 
            String queryString) {
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlService, query);
            ResultSet results = qexec.execSelect();
            ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
            qexec.close();
            
            return resultsrw;
        } catch (QueryParseException e) {
            System.out.println("[ERROR] queryString: " + queryString);
            throw e;
        }
    }
}
