package org.hadatac.console.http;

import com.typesafe.config.ConfigFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SPARQLUtilsFacetSearch {

    private static final Logger log = LoggerFactory.getLogger(SPARQLUtilsFacetSearch.class);
    private static Set<String> visited = new HashSet<>();

    private static ConcurrentHashMap<String, ResultSetRewindable> selectCache = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Model> describeCache = new ConcurrentHashMap<>();

    public static void clearCache() {
        selectCache.clear();
        describeCache.clear();
        visited.clear();
    }

    public static ResultSetRewindable select(String sparqlService, String queryString) {
        // System.out.println("\nqueryString: " + queryString + "\n");

        /*log.info("in SPARQLUtilsFacetSearch.select");
        if ( visited.contains(queryString) ) {
            log.info("encountered!!!!!!");
        } else {
            visited.add(queryString);
        }*/

        if ( "ON".equalsIgnoreCase(ConfigFactory.load().getString("hadatac.facet_search.readOnlyMode")) ) {
            if (selectCache.containsKey(queryString)) {
                return selectCache.get(queryString);
            }
        }

        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlService, query);
            ResultSet results = qexec.execSelect();
            ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
            qexec.close();
            if ( "ON".equalsIgnoreCase(ConfigFactory.load().getString("hadatac.facet_search.readOnlyMode")) ) {
                selectCache.put(queryString,resultsrw);
            }
            return resultsrw;
        } catch (QueryParseException e) {
            System.out.println("[ERROR] queryString: " + queryString);
            throw e;
        }
    }

    public static Model describe(String sparqlService, String queryString) {
        // System.out.println("\nqueryString: " + queryString + "\n");
        /*log.info("in SPARQLUtilsFacetSearch.describe");
        if ( visited.contains(queryString) ) {
            log.info("describe encountered!!!!!!");
        } else {
            visited.add(queryString);
        }*/

        if ( "ON".equalsIgnoreCase(ConfigFactory.load().getString("hadatac.facet_search.readOnlyMode")) ) {
            if (describeCache.containsKey(queryString)) {
                return describeCache.get(queryString);
            }
        }

        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlService, query);
            Model model = qexec.execDescribe();
            qexec.close();
            if ( "ON".equalsIgnoreCase(ConfigFactory.load().getString("hadatac.facet_search.readOnlyMode")) ) {
                describeCache.put(queryString, model);
            }
            return model;
        } catch (QueryParseException e) {
            System.out.println("[ERROR] queryString: " + queryString);
            throw e;
        }
    }
}
