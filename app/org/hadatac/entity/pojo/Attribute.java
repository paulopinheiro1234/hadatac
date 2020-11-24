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
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.annotations.PropertyField;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SPARQLUtilsFacetSearch;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

public class Attribute extends HADatAcClass implements Comparable<Attribute> {

    static String className = "sio:SIO_000614";

    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = " a    sio:SIO_000614;  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";

    public Attribute () {
        super(className);
    }

    @PropertyField(uri="dcterms:identifier")
    private String hasDCTerms;

    @PropertyField(uri="skos:notation")
    private String hasSkosNotation;

    public String getHasDCTerms() {
    	return hasDCTerms;
    }
    
    public void setHasDCTerms(String hasDCTerms) {
    	this.hasDCTerms = hasDCTerms;
    }    
    
    public String getHasSkosNotation() {
    	return hasSkosNotation;
    }
    
    public void setHasSkosNotation(String hasSkosNotation) {
    	this.hasSkosNotation = hasSkosNotation;
    }
    
    
    public static List<Attribute> find() {
        List<Attribute> attributes = new ArrayList<Attribute>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf* sio:SIO_000614 . " + 
                "} ";

        //System.out.println("Query: " + queryString);
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Attribute attribute = find(soln.getResource("uri").getURI());
            attributes.add(attribute);
        }			

        java.util.Collections.sort((List<Attribute>) attributes);
        return attributes;
    }

    public static List<Attribute> findByStudy(String study_uri) {
        if (study_uri == null || study_uri.equals("")) {
            return null;
        }
        List<Attribute> attributes = new ArrayList<Attribute>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " select distinct ?attUri ?attLabel where { " +
                "    ?daUri hasco:isDataAcquisitionOf <" + study_uri + "> . " +
                "    ?daUri hasco:hasSchema ?sddUsi . " +
                "    ?dasaUri hasco:partOfSchema ?sddUsi . " +
                "    ?dasaUri hasco:hasAttribute ?attUri . " +
                "    ?attUri rdfs:label ?attLabel . " +
                " } ";

        //System.out.println("Query: " + queryString);
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Attribute attribute = find(soln.getResource("attUri").getURI());
            //System.out.println("Retrieved attribute: " + attribute);
            if (attribute != null && !attributes.contains(attribute)) {
                attributes.add(attribute);
            }
        }			

        java.util.Collections.sort((List<Attribute>) attributes);
        return attributes;
    }

    public static String findHarmonizedCode(String dasa_uri) {
        String fullUri = URIUtils.replacePrefix(dasa_uri);
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?code WHERE {"
                + " " + fullUri + " skos:notation ?code . "
                + " }";

        //System.out.println("Attribute: query [" +  queryString + "]");

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        //System.out.println("Attribute: size of answer [" +  resultsrw.size() + "]");

        if (resultsrw.size() > 0) {
            QuerySolution soln = resultsrw.next();
            try {
                if (soln.getLiteral("code") != null) {
                    String answer = soln.getLiteral("code").getString();
                    if (answer.length() != 0) {
                        return answer;
                    }
                }
            } catch (Exception e1) {
                //e1.printStackTrace();
                return null;
            }
        }

        return null;
    }

    public static String findCodeValue(String dasa_uri, String code) {
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?codeClass ?codeResource WHERE {"
                + " ?possibleValue a hasco:PossibleValue . "
                + " ?possibleValue hasco:isPossibleValueOf <" + dasa_uri + "> . "
                + " ?possibleValue hasco:hasCode ?code . "
                + " ?possibleValue hasco:hasClass ?codeClass . "
                + " FILTER (lcase(str(?code)) = \"" + code + "\") "
                + " }";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (resultsrw.size() > 0) {
            QuerySolution soln = resultsrw.next();
            try {
                if (null != soln.getResource("codeClass")) {
                    String classUri = soln.getResource("codeClass").toString();
                    if (classUri.length() != 0) {
                        return URIUtils.replacePrefixEx(classUri);
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

    public static Attribute facetSearchFind(String uri) {

        Attribute attribute = null;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtilsFacetSearch.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        attribute = new Attribute();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                attribute.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
                attribute.setSuperUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://purl.org/dc/terms/identifier")) {
                attribute.setHasDCTerms(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2004/02/skos/core#notation")) {
                attribute.setHasSkosNotation(object.asLiteral().getString());
            }
        }

        attribute.setUri(uri);
        attribute.setLocalName(uri.substring(uri.indexOf('#') + 1));
        if (attribute.getLabel() == null || attribute.getLabel().equals("")) {
            attribute.setLabel(attribute.getLocalName());
        }

        return attribute;
    }

    public static Attribute find(String uri) {
        Attribute attribute = null;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        attribute = new Attribute();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                attribute.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
                attribute.setSuperUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://purl.org/dc/terms/identifier")) {
                attribute.setHasDCTerms(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2004/02/skos/core#notation")) {
                attribute.setHasSkosNotation(object.asLiteral().getString());
            }
        }

        attribute.setUri(uri);
        attribute.setLocalName(uri.substring(uri.indexOf('#') + 1));
        if (attribute.getLabel() == null || attribute.getLabel().equals("")) {
            attribute.setLabel(attribute.getLocalName());
        }

        return attribute;
    }
    
    public boolean deleteAttribute() {
        String query = "";
        String uri = "";
        if (getUri() == null || getUri().equals("")) {
            return false;
        }

        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += " DELETE WHERE { \n";

        if (getUri().startsWith("http")) {
            uri += "<" + this.getUri() + ">";
        } else {
            uri += this.getUri();
        }

        query += uri + " <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o . \n";
        query += uri + " <http://www.w3.org/2000/01/rdf-schema#label> ?o . \n";
        query += uri + " <http://purl.org/dc/terms/identifier> ?o . \n";
        query += uri + " <http://www.w3.org/2004/02/skos/core#notation> ?o . \n";
        query += " } ";

        try {
	        UpdateRequest request = UpdateFactory.create(query);
	        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
	                request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
	        processor.execute();
        } catch (Exception e) {
            System.out.println("[ERROR] Attribute.java: QueryParseException due to update query: " + query);
        	return false;
        }

        //System.out.println("Deleted <" + getUri() + "> from triple store");
    	
        return true;
        
    }
    
    public boolean updateAttribute() {
    	if (!deleteAttribute()) {
    		return false;
    	};
    	return saveAttribute();
    }
    
    public boolean saveAttribute() {
    	String insert = "";
        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;
        
        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }
        
        insert += "<" + this.getUri() + ">  ";
        insert += LINE3;

        if (this.getSuperUri() != null && !this.getSuperUri().isEmpty()) {
        	insert += " <http://www.w3.org/2000/01/rdf-schema#subClassOf> <" + this.getSuperUri() + "> ;   ";
        }
        if (this.getLabel() != null && !this.getLabel().isEmpty()) {
        	insert += " <http://www.w3.org/2000/01/rdf-schema#label> \"" + this.getLabel() + "\" ;   ";
        }
        if (this.getHasDCTerms() != null && !this.getHasDCTerms().isEmpty()) {
        	insert += " <http://purl.org/dc/terms/identifier> \"" + this.getHasDCTerms() + "\" ;   ";
        }
        if (this.getHasSkosNotation() != null && !this.getHasSkosNotation().isEmpty()) {
        	insert += " <http://www.w3.org/2004/02/skos/core#notation> \"" + this.getHasSkosNotation() + "\" ;   ";
        }
        
        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }
        
        insert += LINE_LAST;

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
            processor.execute();
        } catch (Exception e) {
            System.out.println("[ERROR] Attribute.java: QueryParseException due to update query: " + insert);
            return false;
        }

        //System.out.println("Added <" + getUri() + "> from triple store");
    	

        return true;
    	
    }
    
    @Override
    public void save() {
        saveToTripleStore();
    }
    
    @Override
    public int compareTo(Attribute another) {
        if (this.getLabel() != null && another.getLabel() != null) {
            return this.getLabel().compareTo(another.getLabel());
        }
        return this.getLocalName().compareTo(another.getLocalName());
    }
}
