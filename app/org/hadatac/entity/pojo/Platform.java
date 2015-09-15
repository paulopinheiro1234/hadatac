package org.hadatac.entity.pojo;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.data.loader.util.Sparql;

public class Platform {
	private String uri;
	private String localName;
	private String label;
	private String location;
	private String elevation;
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getElevation() {
		return elevation;
	}
	public void setElevation(String elevation) {
		this.elevation = elevation;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public static Platform find(HADataC hadatac) {
		Platform platform = null;
		
		String queryString = Sparql.prefix
				+ "SELECT ?platform ?label ?lat ?lon ?ele WHERE {\n"
				+ "  <" + hadatac.getDeploymentUri() + "> vstoi:hasPlatform ?platform .\n"
				+ "  OPTIONAL { ?platform rdfs:label ?label . }\n"
				+ "  OPTIONAL { ?platform <http://hadatac.org/ont/hasneto#hasFirstCoordinate> ?lat . }\n"
				+ "  OPTIONAL { ?platform <http://hadatac.org/ont/hasneto#hasSecondCoordinate> ?lon . }\n"
				+ "  OPTIONAL { ?platform <http://hadatac.org/ont/hasneto#hasThirdCoordinate> ?ele . }\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		System.out.println(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() == 1) {
			QuerySolution soln = resultsrw.next();
			platform = new Platform();
			platform.setLocalName(soln.getResource("platform").getLocalName());
			platform.setUri(soln.getResource("platform").getURI());
			if (soln.getLiteral("label") != null) { platform.setLabel(soln.getLiteral("label").getString()); }
			else { platform.setLabel(soln.getResource("platform").getLocalName()); }
			if (soln.getLiteral("lat") != null && soln.getLiteral("lon") != null) {
				platform.setLocation(soln.getLiteral("lat").getString() + ";" + soln.getLiteral("lon").getString());
			}
			if (soln.getLiteral("ele") != null) { platform.setLabel(soln.getLiteral("ele").getString()); }
		}
		
		return platform;
	}
}
