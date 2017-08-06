package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import play.Play;

public class StudyObjectType extends HADatAcClass implements Comparable<StudyObjectType> {

        static String className = "sio:StudyObject";

	public StudyObjectType () {
	    super(className);
	}

	public static List<StudyObjectType> find() {
	    List<StudyObjectType> objectTypes = new ArrayList<StudyObjectType>();
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
		StudyObjectType objectType = find(soln.getResource("uri").getURI());
		objectTypes.add(objectType);
	    }			
	    
	    java.util.Collections.sort((List<StudyObjectType>) objectTypes);
	    return objectTypes;
	    
	}

	public static Map<String,String> getMap() {
	    List<StudyObjectType> list = find();
	    Map<String,String> map = new HashMap<String,String>();
	    for (StudyObjectType typ: list) 
		map.put(typ.getUri(),typ.getLabel());
	    return map;
	}

	public static StudyObjectType find(String uri) {
	    StudyObjectType objectType = null;
	    Model model;
	    Statement statement;
	    RDFNode object;
	    
	    String queryString = "DESCRIBE <" + uri + ">";
	    Query query = QueryFactory.create(queryString);
	    QueryExecution qexec = QueryExecutionFactory.sparqlService(Play.application().configuration().getString("hadatac.solr.triplestore") 
								       + Collections.METADATA_SPARQL, query);
	    model = qexec.execDescribe();
	    
	    objectType = new StudyObjectType();
	    StmtIterator stmtIterator = model.listStatements();
	    
	    while (stmtIterator.hasNext()) {
		statement = stmtIterator.next();
		object = statement.getObject();
		if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
		    objectType.setLabel(object.asLiteral().getString());
		} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
		    objectType.setSuperUri(object.asResource().getURI());
		}
	    }
	    
	    objectType.setUri(uri);
	    objectType.setLocalName(uri.substring(uri.indexOf('#') + 1));
	    
	    //System.out.println(uri + " " + entity.getLocalName() + " " + entity.getSuperUri());
	    
	    return objectType;
	}
	
	@Override
	    public int compareTo(StudyObjectType another) {
	    if (this.getLabel() != null && another.getLabel() != null) {
		   return this.getLabel().compareTo(another.getLabel());
	    }
	    return this.getLocalName().compareTo(another.getLocalName());
	}
	
}
