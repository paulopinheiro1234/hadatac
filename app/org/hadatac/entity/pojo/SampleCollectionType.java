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

public class SampleCollectionType extends HADatAcClass implements Comparable<SampleCollectionType> {

        static String className = "hasco:SampleCollection";

	public SampleCollectionType () {
	    super(className);
	}

	public static List<SampleCollectionType> find() {
	    List<SampleCollectionType> sampleCollectionTypes = new ArrayList<SampleCollectionType>();
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
		SampleCollectionType sampleCollectionType = find(soln.getResource("uri").getURI());
		sampleCollectionTypes.add(sampleCollectionType);
		//System.out.println("type: " + soln.getResource("uri").getURI());
	    }			
	    
	    java.util.Collections.sort((List<SampleCollectionType>) sampleCollectionTypes);
	    return sampleCollectionTypes;
	    
	}

	public static Map<String,String> getMap() {
	    List<SampleCollectionType> list = find();
	    Map<String,String> map = new HashMap<String,String>();
	    for (SampleCollectionType typ: list) 
		map.put(typ.getUri(),typ.getLabel());
	    return map;
	}

	public static SampleCollectionType find(String uri) {
	    SampleCollectionType sampleCollectionType = null;
	    Model model;
	    Statement statement;
	    RDFNode object;
	    
	    String queryString = "DESCRIBE <" + uri + ">";
	    Query query = QueryFactory.create(queryString);
	    QueryExecution qexec = QueryExecutionFactory.sparqlService(Play.application().configuration().getString("hadatac.solr.triplestore") 
								       + Collections.METADATA_SPARQL, query);
	    model = qexec.execDescribe();
	    
	    sampleCollectionType = new SampleCollectionType();
	    StmtIterator stmtIterator = model.listStatements();
	    
	    while (stmtIterator.hasNext()) {
		statement = stmtIterator.next();
		object = statement.getObject();
		if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
		    sampleCollectionType.setLabel(object.asLiteral().getString());
		} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
		    sampleCollectionType.setSuperUri(object.asResource().getURI());
		}
	    }
	    
	    sampleCollectionType.setUri(uri);
	    sampleCollectionType.setLocalName(uri.substring(uri.indexOf('#') + 1));
	    
	    //System.out.println(uri + " " + entity.getLocalName() + " " + entity.getSuperUri());
	    
	    return sampleCollectionType;
	}
	
	@Override
	    public int compareTo(SampleCollectionType another) {
	    if (this.getLabel() != null && another.getLabel() != null) {
		   return this.getLabel().compareTo(another.getLabel());
	    }
	    return this.getLocalName().compareTo(another.getLocalName());
	}
	
}
