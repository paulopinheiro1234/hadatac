package org.hadatac.entity.pojo;

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

public class Subject {
	public String uri = "";
	public String type = "";
	public String label = "";
	public String ofStudy = "";
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getOfStudy() {
		return ofStudy;
	}
	public void setOfStudy(String ofStudy) {
		this.ofStudy = ofStudy;
	}
	
	public static Subject find(String study_uri, String subject_id) {
		Subject subject = new Subject();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?uri WHERE {\n"
				+ "  ?uri hasco:originalID " + subject_id + " .\n"
				+ "  ?uri hasco:isSubjectOf ?cohort .\n"
				+ "  ?cohort hasco:isCohortOf " + study_uri + " .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		System.out.println(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			subject.setUri(soln.getResource("uri").getURI());
		}
		
		return subject;
	}	
}


