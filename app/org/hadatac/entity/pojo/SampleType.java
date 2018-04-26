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
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

public class SampleType extends HADatAcClass implements Comparable<SampleType> {

	static String className = "sio:Sample";

	public SampleType () {
		super(className);
	}

	public static List<SampleType> find() {
		List<SampleType> studyTypes = new ArrayList<SampleType>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				" ?uri rdfs:subClassOf* " + className + " . " + 
				"} ";

		//System.out.println("Query: " + queryString);
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			SampleType studyType = find(soln.getResource("uri").getURI());
			studyTypes.add(studyType);
		}			

		java.util.Collections.sort((List<SampleType>) studyTypes);
		return studyTypes;

	}

	public static Map<String,String> getMap() {
		List<SampleType> list = find();
		Map<String,String> map = new HashMap<String,String>();
		for (SampleType typ: list) 
			map.put(typ.getUri(),typ.getLabel());
		return map;
	}

	public static SampleType find(String uri) {
		SampleType studyType = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				ConfigFactory.load().getString("hadatac.solr.triplestore") 
				+ CollectionUtil.METADATA_SPARQL, query);
		model = qexec.execDescribe();

		studyType = new SampleType();
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

		return studyType;
	}

	@Override
	public int compareTo(SampleType another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getLocalName().compareTo(another.getLocalName());
	}
}
