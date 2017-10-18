package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

public class Unit extends HADatAcClass implements Comparable<Unit> {

	static String className = "uo:0000000";

	public Unit() {
		super(className);
	}

	public static List<Unit> find() {
		List<Unit> units = new ArrayList<Unit>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				" ?uri rdfs:subClassOf* sio:Quantity . " + 
				"} ";

		//System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Unit unit = find(soln.getResource("uri").getURI());
			units.add(unit);
			break;
		}			

		java.util.Collections.sort((List<Unit>) units);
		return units;

	}

	public static Map<String,String> getMap() {
		List<Unit> list = find();
		Map<String,String> map = new HashMap<String,String>();
		for (Unit ent: list) 
			map.put(ent.getUri(),ent.getLabel());
		return map;
	}

	public static Unit find(String uri) {
		Unit unit = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Play.application().configuration().getString("hadatac.solr.triplestore") 
				+ Collections.METADATA_SPARQL, query);
		model = qexec.execDescribe();

		unit = new Unit();
		StmtIterator stmtIterator = model.listStatements();

		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				unit.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				unit.setSuperUri(object.asResource().getURI());
			}
		}

		unit.setUri(uri);
		unit.setLocalName(uri.substring(uri.indexOf('#') + 1));

		//System.out.println(uri + " " + unit.getLocalName() + " " + unit.getSuperUri());

		return unit;
	}

	@Override
	public int compareTo(Unit another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getLocalName().compareTo(another.getLocalName());
	}

	/*public static String getHierarchyJson() {
	String collection = "";
	String q = 
	    "SELECT ?id ?superId ?label ?comment WHERE { " + 
	    "   ?id rdfs:subClassOf* uo:0000000 . " + 
	    "   ?id rdfs:subClassOf ?superId .  " + 
	    "   OPTIONAL { ?id rdfs:label ?label . } " + 
	    "   OPTIONAL { ?id rdfs:comment ?comment . } " +
	    "}";
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try {
	    String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + q;
	    Query query = QueryFactory.create(queryString);
	    QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	    ResultSet results = qexec.execSelect();
	    ResultSetFormatter.outputAsJSON(outputStream, results);
	    qexec.close();

	    return outputStream.toString("UTF-8");
    	} catch (Exception e) {
	    e.printStackTrace();
	}
    	return "";
	} */

}
