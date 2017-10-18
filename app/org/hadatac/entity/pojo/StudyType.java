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

public class StudyType extends HADatAcClass implements Comparable<StudyType> {

	static String className = "hasco:Study";

	public StudyType () {
		super(className);
	}

	public static List<StudyType> find() {
		List<StudyType> studyTypes = new ArrayList<StudyType>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				" ?uri rdfs:subClassOf* " + className + " . " + 
				"} ";

		//System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			StudyType studyType = find(soln.getResource("uri").getURI());
			studyTypes.add(studyType);
		}			

		java.util.Collections.sort((List<StudyType>) studyTypes);
		return studyTypes;

	}

	public static Map<String,String> getMap() {
		List<StudyType> list = find();
		Map<String,String> map = new HashMap<String,String>();
		for (StudyType typ: list) 
			map.put(typ.getUri(),typ.getLabel());
		return map;
	}

	public static StudyType find(String uri) {
		StudyType studyType = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Play.application().configuration().getString("hadatac.solr.triplestore") 
				+ Collections.METADATA_SPARQL, query);
		model = qexec.execDescribe();

		studyType = new StudyType();
		StmtIterator stmtIterator = model.listStatements();

		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				studyType.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				studyType.setSuperUri(object.asResource().getURI());
			}
		}

		studyType.setUri(uri);
		studyType.setLocalName(uri.substring(uri.indexOf('#') + 1));

		//System.out.println(uri + " " + entity.getLocalName() + " " + entity.getSuperUri());

		return studyType;
	}

	@Override
	public int compareTo(StudyType another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getLocalName().compareTo(another.getLocalName());
	}

}
