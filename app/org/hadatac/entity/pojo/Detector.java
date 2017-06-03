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

public class Detector implements Comparable<Detector>  {
	private String uri;
	private String localName;
	private String label;
	private String serialNumber;
	private String isInstrumentAttachment;
	
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
	
	public String getIsInstrumentAttachment() {
		return isInstrumentAttachment;
	}
	
	public void setIsInstrumentAttachment(String isInstrumentAttachment) {
		this.isInstrumentAttachment = isInstrumentAttachment;
	}
	
	public static List<Detector> find() {
		//System.out.println("Inside Lits<Detector>");
		List<Detector> detectors = new ArrayList<Detector>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            " ?detModel rdfs:subClassOf+ vstoi:Detector . " + 
		    " ?uri a ?detModel ." + 
			"} ";
			
		//System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Detector detector = find(soln.getResource("uri").getURI());
			detectors.add(detector);
		}			

		java.util.Collections.sort((List<Detector>) detectors);
		return detectors;
		
	}
	
	public static Detector find(String uri) {
		Detector detector = null;
		Model model;
		Statement statement;
		RDFNode object;
		
		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Play.application().configuration().getString("hadatac.solr.triplestore") 
				+ Collections.METADATA_SPARQL, query);
		model = qexec.execDescribe();
		
		detector = new Detector();
		StmtIterator stmtIterator = model.listStatements();
		
		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				detector.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#hasSerialNumber")) {
				detector.setSerialNumber(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#isInstrumentAttachment")) {
				detector.setIsInstrumentAttachment(object.asResource().getURI());
			}
		}

		detector.setUri(uri);
		
		return detector;
	}

	@Override
    public int compareTo(Detector another) {
        return this.getLabel().compareTo(another.getLabel());
    }
	
	public static List<Detector> findAvailable() {
		List<Detector> detectors = new ArrayList<Detector>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            "   { ?detModel rdfs:subClassOf+ vstoi:Detector . " + 
		    "     ?uri a ?detModel ." + 
			"   } MINUS { " + 
			"     ?dep_uri a vstoi:Deployment . " + 
		    "     ?dep_uri hasco:hasDetector ?uri .  " +
			"     FILTER NOT EXISTS { ?dep_uri prov:endedAtTime ?enddatetime . } " + 
			"    } " + 
			"} " + 
			"ORDER BY DESC(?datetime) ";
			
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Detector detector = find(soln.getResource("uri").getURI());
			detectors.add(detector);
		}			

		java.util.Collections.sort((List<Detector>) detectors);
		return detectors;
		
	}
	
	public static List<Detector> findDeployed() {
		List<Detector> detectors = new ArrayList<Detector>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            "   ?detModel rdfs:subClassOf+ vstoi:Detector . " + 
		    "   ?uri a ?detModel ." + 
			"   ?dep_uri a vstoi:Deployment . " + 
		    "   ?dep_uri hasco:hasDetector ?uri .  " +
			"   FILTER NOT EXISTS { ?dep_uri prov:endedAtTime ?enddatetime . } " + 
			"} " + 
			"ORDER BY DESC(?datetime) ";
			
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Detector detector = find(soln.getResource("uri").getURI());
			detectors.add(detector);
		}			

		java.util.Collections.sort((List<Detector>) detectors);
		return detectors;
		
	}
	
}
