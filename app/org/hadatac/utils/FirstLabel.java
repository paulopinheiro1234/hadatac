package org.hadatac.utils;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;

public class FirstLabel {
	
	public static String getLabel(String uri) {
		if (uri.startsWith("http")) {
			uri = "<" + uri + ">";
		}
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT ?label WHERE { \n" + 
				"  " + uri + " rdfs:label ?label . \n" + 
				"}";
		
		// System.out.println("getLabel() queryString: \n" + queryString);
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
		        CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

		String labelStr = "";
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln.get("label") != null) {
				labelStr = soln.get("label").toString();
			}
			
			if (!labelStr.isEmpty()) {
				break;
			}
		}
		
		return labelStr;
	}
}
