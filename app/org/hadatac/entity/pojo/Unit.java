package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;


public class Unit extends HADatAcClass implements Comparable<Unit> {

	static String className = "uo:0000000";

	public Unit() {
		super(className);
	}

	public static List<Unit> find() {
		List<Unit> units = new ArrayList<Unit>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				" ?uri rdfs:subClassOf* sio:SIO_000052 . " + 
				"} ";

		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

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
		try {
		    model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
		            CollectionUtil.Collection.METADATA_SPARQL), queryString);
		} catch (Exception e) {
		    System.out.println("[ERROR] Unit.find(uri) failed to execute descrive query");
		    return null;
		}
		StmtIterator stmtIterator = model.listStatements();
		if (model.size() > 0) {
			unit = new Unit();
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
			if (unit.getLabel() == null || unit.getLabel().equals("")) {
				unit.setLabel(unit.getLocalName());
			}
		}

		return unit;
	}

	public static Unit facetSearchFind(String uri) {
		Unit unit = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		try {
			model = SPARQLUtilsFacetSearch.describe(CollectionUtil.getCollectionPath(
					CollectionUtil.Collection.METADATA_SPARQL), queryString);
		} catch (Exception e) {
			System.out.println("[ERROR] Unit.find(uri) failed to execute descrive query");
			return null;
		}
		StmtIterator stmtIterator = model.listStatements();
		if (model.size() > 0) {
			unit = new Unit();
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
			if (unit.getLabel() == null || unit.getLabel().equals("")) {
				unit.setLabel(unit.getLocalName());
			}
		}

		return unit;
	}

	@Override
	public int compareTo(Unit another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getLocalName().compareTo(another.getLocalName());
	}
}
