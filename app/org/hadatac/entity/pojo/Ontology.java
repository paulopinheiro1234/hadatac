package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class Ontology extends HADatAcThing {

    static String className = "owl:Ontology";

    private String version = "";
    
    public Ontology() {
        setUri("");
        setLabel("");
        setComment("");
    }

    public Ontology(String uri) {
        setUri(uri);
        setLabel("");
        setComment("");
    }

    public String getVersion() {
    	return version;
    }
    
    public void setVersion(String version) {
    	this.version = version;
    }
    
    public static Ontology find(String uri) {
        Ontology ontology = null;
        Model model;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
        model = qexec.execDescribe();

        ontology = new Ontology();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                ontology.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                ontology.setTypeUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                ontology.setComment(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2002/07/owl#versionIRI")) {
                ontology.setVersion(object.asResource().getURI());
            } 
        }

        ontology.setUri(uri);

        return ontology;
    }

    public static String getVersionFromAbbreviation(String abbr) {
    	NameSpaces nss = NameSpaces.getInstance();
    	String uri = nss.getNameByAbbreviation(abbr).replace("#", "");    	
    	System.out.println("OntVersion: " + uri);
    	if (uri == null || uri.isEmpty()) {
    		return "";
    	}
    	Ontology ont = Ontology.find(uri);
    	if (ont == null || ont.getVersion() == null) {
        	System.out.println("OntVersion ont: is null");
    		return "";
    	}
    	System.out.println("OntVersion is [" + ont.getVersion() + "]");
    	return ont.getVersion();
    }
    
    public static List<Ontology> find() {
        List<Ontology> ontologies = new ArrayList<Ontology>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?ont rdfs:subClassOf* owl:Ontology . " + 
                " ?uri a ?ont ." + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Ontology ontology = find(soln.getResource("uri").getURI());
            ontologies.add(ontology);
        }			

        return ontologies;
    }
    
    @Override
	public boolean saveToSolr() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int deleteFromSolr() {
		// TODO Auto-generated method stub
		return 0;
	}

}
