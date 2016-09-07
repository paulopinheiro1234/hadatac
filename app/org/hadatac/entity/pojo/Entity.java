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

public class Entity implements HADatAcClass, Comparable<Entity> {
	private String uri;
	private String superUri;
	private String localName;
	private String label;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getSuperUri() {
		return superUri;
	}
	public void setSuperUri(String superUri) {
		this.superUri = superUri;
	}
	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public static List<Entity> find() {
		List<Entity> entities = new ArrayList<Entity>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			" SELECT ?uri WHERE { " +
            " ?uri rdfs:subClassOf* sio:Object . " + 
			"} ";
			
		//System.out.println("Query: " + queryString);
		Query query = QueryFactory.create(queryString);
			
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
			
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Entity entity = find(soln.getResource("uri").getURI());
			entities.add(entity);
		}			

		java.util.Collections.sort((List<Entity>) entities);
		return entities;
		
	}
	
	public static Entity find(String uri) {
		Entity entity = null;
		Model model;
		Statement statement;
		RDFNode object;
		
		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Play.application().configuration().getString("hadatac.solr.triplestore") + "/store/sparql", query);
		model = qexec.execDescribe();
		
		entity = new Entity();
		StmtIterator stmtIterator = model.listStatements();
		
		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				entity.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				entity.setSuperUri(object.asResource().getURI());
			}
		}
		
		entity.setUri(uri);
		entity.setLocalName(uri.substring(uri.indexOf('#') + 1));
		
		//System.out.println(uri + " " + entity.getLocalName() + " " + entity.getSuperUri());
		
		return entity;
	}
	
	@Override
    public int compareTo(Entity another) {
        if (this.getLabel() != null && another.getLabel() != null) {
		   return this.getLabel().compareTo(another.getLabel());
        }
        return this.getLocalName().compareTo(another.getLocalName());
    }
	
}
