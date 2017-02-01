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
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import play.Play;

public class Instrument  implements Comparable<Instrument> {
	private String uri;
	private String localName;
	private String label;
	private String serialNumber;
	
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
	
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	public static List<Instrument> find() {
		//System.out.println("Inside Lits<Instrument>");
		List<Instrument> instruments = new ArrayList<Instrument>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            " ?instModel rdfs:subClassOf+ vstoi:Instrument . " + 
		    " ?uri a ?instModel ." + 
			"} ";
			
		//System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			//System.out.println("URI from main query: " + soln.getResource("uri").getURI());
			Instrument instrument = find(soln.getResource("uri").getURI());
			instruments.add(instrument);
		}			

		java.util.Collections.sort((List<Instrument>) instruments);
		return instruments;
		
	}
	
	public static List<Instrument> findAvailable() {
		//System.out.println("Inside Lits<Instrument> findAvailable()");
		List<Instrument> instruments = new ArrayList<Instrument>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            "   { ?instModel rdfs:subClassOf+ vstoi:Instrument . " + 
		    "     ?uri a ?instModel ." + 
			"   } MINUS { " + 
			"     ?dep_uri a vstoi:Deployment . " + 
		    "     ?dep_uri hasneto:hasInstrument ?uri .  " +
			"     FILTER NOT EXISTS { ?dep_uri prov:endedAtTime ?enddatetime . } " + 
			"    } " + 
			"} " + 
			"ORDER BY DESC(?datetime) ";
			
		System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("URI from main query: " + soln.getResource("uri").getURI());
			Instrument instrument = find(soln.getResource("uri").getURI());
			instruments.add(instrument);
		}			

		java.util.Collections.sort((List<Instrument>) instruments);
		return instruments;
		
	}
	
	public static List<Instrument> findDeployed() {
		//System.out.println("Inside Lits<Instrument> findAvailable()");
		List<Instrument> instruments = new ArrayList<Instrument>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            "   ?instModel rdfs:subClassOf+ vstoi:Instrument . " + 
		    "   ?uri a ?instModel ." + 
			"   ?dep_uri a vstoi:Deployment . " + 
		    "   ?dep_uri hasneto:hasInstrument ?uri .  " +
			"   FILTER NOT EXISTS { ?dep_uri prov:endedAtTime ?enddatetime . } " + 
			"} " + 
			"ORDER BY DESC(?datetime) ";
			
		//System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			//System.out.println("URI from main query: " + soln.getResource("uri").getURI());
			Instrument instrument = find(soln.getResource("uri").getURI());
			instruments.add(instrument);
		}			

		java.util.Collections.sort((List<Instrument>) instruments);
		return instruments;
		
	}
	
	public static Instrument find(String uri) {
		Instrument instrument = null;
		Model model;
		Statement statement;
		RDFNode object;
		
		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Play.application().configuration().getString("hadatac.solr.triplestore") 
				+ Collections.METADATA_SPARQL, query);
		model = qexec.execDescribe();
		
		instrument = new Instrument();
		StmtIterator stmtIterator = model.listStatements();
		
		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				instrument.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#hasSerialNumber")) {
				instrument.setSerialNumber(object.asLiteral().getString());
			}
		}
		
		instrument.setUri(uri);
		
		return instrument;
	}
	
	public static Instrument find(HADataC hadatac) {
		Instrument instrument = null;
		
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT ?instrument ?label WHERE {\n"
				+ "  <" + hadatac.getDeploymentUri() + "> hasneto:hasInstrument ?instrument .\n"
				+ "  OPTIONAL { ?instrument rdfs:label ?label . }\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			instrument = new Instrument();
			instrument.setLocalName(soln.getResource("instrument").getLocalName());
			instrument.setUri(soln.getResource("instrument").getURI());
			if (soln.getLiteral("label") != null) { instrument.setLabel(soln.getLiteral("label").getString()); }
			else { instrument.setLabel(soln.getResource("instrument").getLocalName()); }
		}
		
		return instrument;
	}

	@Override
    public int compareTo(Instrument another) {
        return this.getLabel().compareTo(another.getLabel());
    }
	
}
