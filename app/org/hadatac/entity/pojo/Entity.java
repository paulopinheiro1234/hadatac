package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.data.model.SPARQLUtilsFacetSearch;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;


public class Entity extends HADatAcClass implements Comparable<Entity> {

    static String className = "sio:SIO_000776";

    public List<Characteristic> characteristics;

    public Entity() {
        super(className);
        characteristics = new ArrayList<Characteristic>();
    }

    public static List<Entity> find() {
        List<Entity> entities = new ArrayList<Entity>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf* sio:SIO_000776 . " +
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

    public static Entity facetSearchFind(String uri) {

        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtilsFacetSearch.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        Entity entity = new Entity();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();
            RDFNode object = statement.getObject();
            if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("rdfs:label"))) {
                String label = object.asLiteral().getString();

                // prefer longer one
                if (label.length() > entity.getLabel().length()) {
                    entity.setLabel(label);
                }
            } else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("rdfs:subClassOf"))) {
                entity.setSuperUri(object.asResource().getURI());
            }
        }

        entity.setUri(uri);
        entity.setLocalName(uri.substring(uri.indexOf('#') + 1));

        return entity;
    }

    public static Entity find(String uri) {
        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        Entity entity = new Entity();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();
            RDFNode object = statement.getObject();
            if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("rdfs:label"))) {
                String label = object.asLiteral().getString();

                // prefer longer one
                if (label.length() > entity.getLabel().length()) {
                    entity.setLabel(label);
                }
            } else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("rdfs:subClassOf"))) {
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

