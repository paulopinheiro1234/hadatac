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
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.URIUtils;

public class ObjectCollectionType extends HADatAcClass implements Comparable<ObjectCollectionType> {

	static String className = "hasco:ObjectCollection";
	String studyObjectTypeUri = "";
	String acronym = "";
	String labelFragment = "";

	public ObjectCollectionType () {
		super(className);
		studyObjectTypeUri = "";
		acronym = "";
		labelFragment = "";
	}

	public String getStudyObjectTypeUri() {
		return studyObjectTypeUri;
	}

	public StudyObjectType getStudyObjectType() {
		if (studyObjectTypeUri == null || studyObjectTypeUri.equals("")) {
			return null;
		}
		return StudyObjectType.find(studyObjectTypeUri);
	}

	public void setStudyObjectTypeUri(String studyObjectTypeUri) {
		this.studyObjectTypeUri = studyObjectTypeUri;
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public String getLabelFragment() {
		return labelFragment;
	}

	public void setLabelFragment(String  labelFragment) {
		this.labelFragment = labelFragment;
	}

	public static List<ObjectCollectionType> find() {
		List<ObjectCollectionType> objectCollectionTypes = new ArrayList<ObjectCollectionType>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				"    ?uri rdfs:subClassOf* " + className + " . " + 
				"} ";

		// System.out.println("Query: " + queryString);
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			ObjectCollectionType objectCollectionType = find(soln.getResource("uri").getURI());
			objectCollectionTypes.add(objectCollectionType);
		}			

		java.util.Collections.sort((List<ObjectCollectionType>) objectCollectionTypes);
		return objectCollectionTypes;

	}

	public static Map<String,String> getMap() {
		List<ObjectCollectionType> list = find();
		Map<String,String> map = new HashMap<String,String>();
		for (ObjectCollectionType typ: list) 
			map.put(typ.getUri(),typ.getLabel());
		return map;
	}

	public static ObjectCollectionType find(String uri) {
		ObjectCollectionType objectCollectionType = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
		model = qexec.execDescribe();

		objectCollectionType = new ObjectCollectionType();
		StmtIterator stmtIterator = model.listStatements();

		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				objectCollectionType.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				objectCollectionType.setSuperUri(object.asResource().getURI());
			} else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("hasco:hasStudyObjectType"))) {
				objectCollectionType.setStudyObjectTypeUri(object.asResource().getURI());
			} else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("hasco:hasAcronym"))) {
				objectCollectionType.setAcronym(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("hasco:hasStudyObjectType"))) {
				objectCollectionType.setLabelFragment(object.asLiteral().getString());
			}
		}

		objectCollectionType.setUri(uri);
		objectCollectionType.setLocalName(uri.substring(uri.indexOf('#') + 1));

		return objectCollectionType;
	}

	@Override
	public int compareTo(ObjectCollectionType another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getLocalName().compareTo(another.getLocalName());
	}

}
