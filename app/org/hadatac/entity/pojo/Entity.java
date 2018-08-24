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

import com.avaje.ebeaninternal.server.lib.util.Str;
import com.typesafe.config.ConfigFactory;

public class Entity extends HADatAcClass implements Comparable<Entity> {

    static String className = "sio:Object";

    public List<Characteristic> characteristics;

    public Entity() {
        super(className);
        characteristics = new ArrayList<Characteristic>();
    }

    public static List<Entity> find() {
        List<Entity> entities = new ArrayList<Entity>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf* sio:Object . " + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Entity entity = find(soln.getResource("uri").getURI());
            entities.add(entity);
            break;
        }			

        java.util.Collections.sort((List<Entity>) entities);
        
        return entities;
    }

    public static Map<String,String> getMap() {
        List<Entity> list = find();
        Map<String,String> map = new HashMap<String,String>();
        for (Entity ent: list) 
            map.put(ent.getUri(),ent.getLabel());
        return map;
    }
    
    public static List<String> getSubclasses(String uri) {
        List<String> subclasses = new ArrayList<String>();
        
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
                + " SELECT ?uri WHERE { \n"
                + " ?uri rdfs:subClassOf* <" + uri + "> . \n"
                + " } \n";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            subclasses.add(soln.get("uri").toString());
        }
        
        return subclasses;
    }

    public static Entity find(String uri) {
        Entity entity = null;
        Model model;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
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

