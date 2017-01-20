package org.hadatac.metadata.loader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class CSVExporterContext{

	String username = null;
	String password = null;
	String kbURL = null;
	boolean verbose = false;

	String processMessage = "";
	String loadFileMessage = "";
	
	Set<String> subclasses = new HashSet<String>();
	Set<String> properties = new HashSet<String>();

	public CSVExporterContext(String un, String pwd, String kb, boolean ver) {
		username = un;
		password = pwd;
		kbURL = kb;
		verbose = ver;
	}
	
	public String getLang(String contentType) {
		if (contentType.contains("turtle")) {
			return "TTL";
		} else if (contentType.contains("rdf+xml")) {
			return "RDF/XML";
		} else {
			return "";
		}
	}
	
	public void getTriples(OutputStream outStream) throws IllegalStateException, IOException{
		ResultSetRewindable results = getSelectQueryResult(queryAllTriples());
		ResultSetFormatter.outputAsCSV(outStream, results);
	}
	
	public void getInstrumentModelTriples(OutputStream outStream) throws IllegalStateException, IOException{
		subclasses.clear();
		properties.clear();
		getAllSubClasses("http://hadatac.org/ont/vstoi#Instrument", subclasses);
		for(String subclass : subclasses){
			getDescribeQueryResult(queryDescription(subclass), properties);
		}
		ResultSetRewindable final_results = getSelectQueryResult(
				queryEntityOnProperty(subclasses, properties, "InstrumentModel"));
		ResultSetFormatter.outputAsCSV(outStream, final_results);
	}
	
	public void getInstrumentTriples(OutputStream outStream) throws IllegalStateException, IOException{
		subclasses.clear();
		properties.clear();
		getAllSubClasses("http://hadatac.org/ont/vstoi#Instrument", subclasses);	
		for(String subclass : subclasses){
			Set<String> new_instances = getAllInstances(subclass);
			for(String instance : new_instances){
				getDescribeQueryResult(queryDescription(instance), properties);
			}
		}
		ResultSetRewindable final_results = getSelectQueryResult(
				queryInstanceOnProperty(subclasses, properties, "Instrument"));
		ResultSetFormatter.outputAsCSV(outStream, final_results);
	}
	
	public void getDetectorModelTriples(OutputStream outStream) throws IllegalStateException, IOException{
		subclasses.clear();
		properties.clear();
		getAllSubClasses("http://hadatac.org/ont/vstoi#Detector", subclasses);
		for(String subclass : subclasses){
			getDescribeQueryResult(queryDescription(subclass), properties);
		}
		ResultSetRewindable final_results = getSelectQueryResult(
				queryEntityOnProperty(subclasses, properties, "DetectorModel"));
		ResultSetFormatter.outputAsCSV(outStream, final_results);
	}
	
	public void getDetectorTriples(OutputStream outStream) throws IllegalStateException, IOException{
		subclasses.clear();
		properties.clear();
		getAllSubClasses("http://hadatac.org/ont/vstoi#Detector", subclasses);
		for(String subclass : subclasses){
			Set<String> new_instances = getAllInstances(subclass);
			for(String instance : new_instances){
				getDescribeQueryResult(queryDescription(instance), properties);
			}
		}
		ResultSetRewindable final_results = getSelectQueryResult(
				queryInstanceOnProperty(subclasses, properties, "Detector"));
		ResultSetFormatter.outputAsCSV(outStream, final_results);
	}
	
	public ResultSetRewindable getSelectQueryResult(String query){
		String service = kbURL + Collections.METADATA_SPARQL;
		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		ResultSet results = qe.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qe.close();
		return resultsrw;
	}
	
	public void getDescribeQueryResult(String query, Set<String> properties){
		String service = kbURL + Collections.METADATA_SPARQL;
		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		
		Model model = qe.execDescribe();
		StmtIterator stmtIterator = model.listStatements();
		Statement statement;
		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			properties.add(statement.getPredicate().getURI());
		}
	}
	
	public String queryEntityOnProperty(Set<String> parent_classes, Set<String> properties, String entity){
		String select = String.format(" SELECT ?%s ", entity);
		String which_triples = "";
		Iterator<String> iter = parent_classes.iterator();
		while(iter.hasNext()){
			String parent_class = iter.next();
			if(iter.hasNext()){
				which_triples += String.format("{ ?%s rdfs:subClassOf <%s> } UNION \n", entity, parent_class);
			}
			else{
				which_triples += String.format("{ ?%s rdfs:subClassOf <%s> } . \n", entity, parent_class);
			}
		}
		iter = properties.iterator();
		while(iter.hasNext()){
			String property = iter.next();
			String[] name = property.split("#", 2);
			select += String.format("?%s ", name[1]);
			which_triples += String.format("OPTIONAL { ?%s <%s> ?%s } . \n", entity, property, name[1]);
		}
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
    			+ select
    			+ " WHERE { "
    			+ which_triples
    			+ " } ";
		System.out.println(queryString);
		return queryString;
	}
	
	public String queryInstanceOnProperty(Set<String> classes, Set<String> properties, String entity){
		String select = String.format(" SELECT ?%s ", entity);
		String which_triples = "";
		Iterator<String> iter = classes.iterator();
		while(iter.hasNext()){
			String cls = iter.next();
			if(iter.hasNext()){
				which_triples += String.format("{ ?%s rdf:type <%s> } UNION \n", entity, cls);
			}
			else{
				which_triples += String.format("{ ?%s rdf:type <%s> } . \n", entity, cls);
			}
		}
		iter = properties.iterator();
		while(iter.hasNext()){
			String property = iter.next();
			String[] name = property.split("#", 2);
			select += String.format("?%s ", name[1]);
			which_triples += String.format("OPTIONAL { ?%s <%s> ?%s } . \n", entity, property, name[1]);
		}
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
    			+ select
    			+ " WHERE { "
    			+ which_triples
    			+ " } ";
		System.out.println(queryString);
		return queryString;
	}
	
	public String queryAllTriples(){
		String queryString = "SELECT ?s ?p ?o WHERE { ?s ?p ?o . }";
		return queryString;
	}
    
    public String queryTest(){
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
    			+ " SELECT ?instrument "
    			+ " WHERE { "
    			+ " ?instrument <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://hadatac.org/ont/hadatac-sn#H3123> ."
    			+ " } ";
		return queryString;
    }
    
    public void getAllSubClasses(String parent_class, Set<String> subclasses){
    	subclasses.add(parent_class);
    	ResultSetRewindable results = getSelectQueryResult(querySubClass(parent_class));
		while(results.hasNext()){
			QuerySolution soln = results.next();
			String uri = soln.getResource("subclasses").getURI();
			getAllSubClasses(uri, subclasses);
		}
    }
    
    public Set<String> getAllInstances(String class_uri){
    	Set<String> instances = new HashSet<String>();
    	ResultSetRewindable results = getSelectQueryResult(queryInstance(class_uri));
		while(results.hasNext()){
			QuerySolution soln = results.next();
			String uri = soln.getResource("instances").getURI();
			instances.add(uri);
		}
		return instances;
    }
		
    public String querySubClass(String parent_class){
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
    			+ String.format(" SELECT ?subclasses "
    			+ " WHERE { "
    			+ " ?subclasses rdfs:subClassOf <%s> ."
    			+ " } ", parent_class);
		return queryString;
    }
    
    public String queryInstance(String class_uri){
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
    			+ String.format(" SELECT ?instances "
    			+ " WHERE { "
    			+ " ?instances rdf:type <%s> ."
    			+ " } ", class_uri);
		return queryString;
    }
    
    public String queryDescription(String uri){
    	String describe = String.format("DESCRIBE <%s>", uri);
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + describe;
		return queryString;
    }
}