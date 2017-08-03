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

public class Indicator  implements Comparable<Indicator> {
	private String uri;
	private String label;
	private String comment;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
		
	public static List<Indicator> find() {
		List<Indicator> indicators = new ArrayList<Indicator>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            " ?uri rdfs:subClassOf hasco:Indicator . " + 
			"} ";
		
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Indicator indicator = find(soln.getResource("uri").getURI());
			indicators.add(indicator);
		}			

		java.util.Collections.sort((List<Indicator>) indicators);
		return indicators;		
	}
		
	public static Indicator find(String uri) {
		Indicator indicator = null;
		Model model;
		Statement statement;
		RDFNode object;
		
		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Play.application().configuration().getString("hadatac.solr.triplestore") 
				+ Collections.METADATA_SPARQL, query);
		model = qexec.execDescribe();
		
		indicator = new Indicator();
		StmtIterator stmtIterator = model.listStatements();
		
		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				indicator.setLabel(object.asLiteral().getString());
			}
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
				indicator.setComment(object.asLiteral().getString());
			}
		}
		
		indicator.setUri(uri);
		
		return indicator;
	}

	public static List<Indicator> findRecursive() {
		List<Indicator> indicators = new ArrayList<Indicator>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            " ?uri rdfs:subClassOf hasco:Indicator+ . " + 
			"} ";
		
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Indicator indicator = find(soln.getResource("uri").getURI());
			indicators.add(indicator);
		}			

		java.util.Collections.sort((List<Indicator>) indicators);
		return indicators;		
	}
	
    public static List<Indicator> findStudyIndicators() {
		List<Indicator> indicators = new ArrayList<Indicator>();
		String query = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT DISTINCT ?indicator ?indicatorLabel ?indicatorComment WHERE { "
				+ " ?subTypeUri rdfs:subClassOf* hasco:Study . "
				+ " ?studyUri a ?subTypeUri . "
				+ " ?dataAcq hasco:isDataAcquisitionOf ?studyUri ."
				+ " ?dataAcq hasco:hasSchema ?schemaUri ."
				+ " ?schemaAttribute hasco:partOfSchema ?schemaUri . "
				+ " ?schemaAttribute hasco:hasAttribute ?attribute . "
				+ " {  { ?indicator rdfs:subClassOf hasco:StudyIndicator } UNION { ?indicator rdfs:subClassOf hasco:SampleIndicator } } . "
				+ " ?indicator rdfs:label ?indicatorLabel . " 
				+ " OPTIONAL { ?indicator rdfs:comment ?indicatorComment } . "
				+ " ?attribute rdfs:subClassOf+ ?indicator . " 
				+ " ?attribute rdfs:label ?attributeLabel . "
				+ " }";
		
		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		Indicator indicator = null;
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			indicator = new Indicator();
			indicator.setUri(soln.getResource("indicator").getURI());
			indicator.setLabel(soln.get("indicatorLabel").toString());
			if(soln.contains("indicatorComment")){
				indicator.setComment(soln.get("indicatorComment").toString());
			}
			indicators.add(indicator);
		}
		java.util.Collections.sort(indicators);
		return indicators; 
    }

	@Override
    public int compareTo(Indicator another) {
        return this.getLabel().compareTo(another.getLabel());
    }
	
}
