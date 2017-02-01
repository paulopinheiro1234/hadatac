package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import play.Play;

public class Platform implements Comparable<Platform> {
	private String uri;
	private String localName;
	private String label;
	private String location;
	private String firstCoordinate;
	private String secondCoordinate;
	private String thirdCoordinate;
	private String elevation;
	private String serialNumber;
	
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
	public String getFirstCoordinate() {
		return firstCoordinate;
	}
	public void setFirstCoordinate(String firstCoordinate) {
		this.firstCoordinate = firstCoordinate;
	}

	public String getSecondCoordinate() {
		return secondCoordinate;
	}

	public void setSecondCoordinate(String secondCoordinate) {
		this.secondCoordinate = secondCoordinate;
	}
	
	public String getThirdCoordinate() {
		return thirdCoordinate;
	}

	public void setThirdCoordinate(String thirdCoordinate) {
		this.thirdCoordinate = thirdCoordinate;
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	public static Platform find(String uri) {
		Platform platform = null;
		Model model;
		Statement statement;
		RDFNode object;
		
		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Play.application().configuration().getString("hadatac.solr.triplestore") 
				+ Collections.METADATA_SPARQL, query);
		model = qexec.execDescribe();
		
		platform = new Platform();
		StmtIterator stmtIterator = model.listStatements();
		
		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				platform.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#hasSerialNumber")) {
				platform.setSerialNumber(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasneto#hasFirstCoordinate")) {
				platform.setFirstCoordinate(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasneto#hasSecondCoordinate")) {
				platform.setSecondCoordinate(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasneto#hasThirdCoordinate")) {
				platform.setThirdCoordinate(object.asLiteral().getString());
			}
		}
		
		platform.setUri(uri);
		
		return platform;
	}
	
	public static List<Platform> find() {
		System.out.println("Inside Lits<Pltaform>");
		List<Platform> platforms = new ArrayList<Platform>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            " ?platModel rdfs:subClassOf+ vstoi:Platform . " + 
		    " ?uri a ?platModel ." + 
			"} ";
			
		System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("URI from main query: " + soln.getResource("uri").getURI());
			Platform platform = find(soln.getResource("uri").getURI());
			platforms.add(platform);
		}			

		java.util.Collections.sort((List<Platform>) platforms);
		return platforms;
		
	}
	
	public static Platform find(HADataC hadatac) {
		Platform platform = null;
		
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
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
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			platform = new Platform();
			platform.setLocalName(soln.getResource("platform").getLocalName());
			platform.setUri(soln.getResource("platform").getURI());
			if (soln.getLiteral("label") != null) {
				platform.setLabel(soln.getLiteral("label").getString());
			}
			else {
				platform.setLabel(soln.getResource("platform").getLocalName());
			}
			if(soln.getLiteral("lat") != null) {
				platform.setFirstCoordinate(soln.getLiteral("lat").getString());
			}
			if(soln.getLiteral("lon") != null) {
				platform.setSecondCoordinate(soln.getLiteral("long").getString());
			}
			if(soln.getLiteral("ele") != null) {
				platform.setThirdCoordinate(soln.getLiteral("ele").getString());
				platform.setLocation("(" + platform.getFirstCoordinate() + ", " 
						 				 + platform.getSecondCoordinate() + ", "
						 				 + platform.getThirdCoordinate() + ")");
			}
			if (soln.getLiteral("ele") != null) {
				platform.setElevation(soln.getLiteral("ele").getString());
			}
		}
		
		return platform;
	}

	@Override
    public int compareTo(Platform another) {
        return this.getLabel().compareTo(another.getLabel());
    }
	
}
