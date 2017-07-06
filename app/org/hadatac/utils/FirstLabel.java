package org.hadatac.utils;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.utils.Collections;

public class FirstLabel {

    private String uri = "";
    private String label = "";

    public static String getLabel (String uri) {
	if (uri.startsWith("http")) {
	    uri = "<" + uri + ">";
	}
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?label WHERE { " + 
	    "  " + uri + " rdfs:label ?label ." + 
	    "}";
	Query query = QueryFactory.create(queryString);
	QueryExecution qexec = QueryExecutionFactory.sparqlService(
				    Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	String labelStr = "";
	QuerySolution soln;
	while (resultsrw.hasNext()) {
	    soln = resultsrw.next();
	    try {
		if (soln.getLiteral("label") != null && soln.getLiteral("label").getString() != null) {
		    labelStr = soln.getLiteral("label").getString();
		}
	    } catch (Exception e1) {
		labelStr = "";
	    }
	    if (!labelStr.equals("")) {
		break;
	    }
	}
	return labelStr;
	
    }
    
}
