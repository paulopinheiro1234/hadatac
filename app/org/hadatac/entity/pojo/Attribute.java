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
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import play.Play;

public class Attribute extends HADatAcClass implements Comparable<Attribute> {

	static String className = "sio:Attribute";

	public Attribute () {
		super(className);
	}

	public static List<Attribute> find() {
		List<Attribute> attributes = new ArrayList<Attribute>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				" ?uri rdfs:subClassOf* sio:Attribute . " + 
				"} ";

		//System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Attribute attribute = find(soln.getResource("uri").getURI());
			attributes.add(attribute);
		}			

		java.util.Collections.sort((List<Attribute>) attributes);
		return attributes;
	}

	public static String findCodeValue(String dasa_uri, String code) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ " SELECT ?codeClass ?codeResource WHERE {"
				+ " ?possibleValue a hasco:PossibleValue . "
				+ " ?possibleValue hasco:isPossibleValueOf <" + dasa_uri + "> . "
				+ " ?possibleValue hasco:hasCode ?code . "
				+ " ?possibleValue hasco:hasClass ?codeClass . "
				+ " FILTER (lcase(str(?code)) = \"" + code.toLowerCase() + "\") "
				+ " }";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		if (resultsrw.size() > 0) {
			QuerySolution soln = resultsrw.next();
			try {
				if (null != soln.getResource("codeClass")) {
					String classUri = soln.getResource("codeClass").toString();
					if (classUri.length() != 0) {
						return ValueCellProcessing.replacePrefixEx(classUri);
					}
				}
			} catch (Exception e1) {
				return null;
			}
		}

		return null;
	}

	public static Map<String,String> getMap() {
		List<Attribute> list = find();
		Map<String,String> map = new HashMap<String,String>();
		for (Attribute att : list) 
			map.put(att.getUri(),att.getLabel());
		return map;
	}

	public static Attribute find(String uri) {
		Attribute attribute = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Play.application().configuration().getString("hadatac.solr.triplestore") 
				+ Collections.METADATA_SPARQL, query);
		model = qexec.execDescribe();

		attribute = new Attribute();
		StmtIterator stmtIterator = model.listStatements();

		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				attribute.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				attribute.setSuperUri(object.asResource().getURI());
			}
		}

		attribute.setUri(uri);
		attribute.setLocalName(uri.substring(uri.indexOf('#') + 1));

		//System.out.println(uri + " " + entity.getLocalName() + " " + entity.getSuperUri());

		return attribute;
	}

	@Override
	public int compareTo(Attribute another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getLocalName().compareTo(another.getLocalName());
	}
}
