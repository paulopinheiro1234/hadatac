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
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

public class Aggregate extends HADatAcClass implements Comparable<Aggregate> {

	static String className = "hadatac-aggregate:aggregate";
	
	public Aggregate() {
		super(className);
	}
	
	public static List<Aggregate> find() {
		List<Aggregate> aggregates = new ArrayList<Aggregate>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				" ?uri rdfs:subClassOf hadatac-aggregate:aggregate . " + 
				"} ";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Aggregate aggregate = find(soln.getResource("uri").getURI());
			aggregates.add(aggregate);
		}			

		java.util.Collections.sort((List<Aggregate>) aggregates);
		return aggregates;
	}
	
	public static Aggregate find(String uri) {
		Aggregate aggregate = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
		model = qexec.execDescribe();

		aggregate = new Aggregate();
		StmtIterator stmtIterator = model.listStatements();

		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				aggregate.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				aggregate.setSuperUri(object.asResource().getURI());
			}
		}

		aggregate.setUri(uri);
		aggregate.setLocalName(uri.substring(uri.indexOf('#') + 1));
		if (aggregate.getLabel() == null || aggregate.getLabel().equals("")) {
			aggregate.setLabel(aggregate.getLocalName());
		}

		return aggregate;
	}

	@Override
	public int compareTo(Aggregate o) {
		return 0;
	}

}
