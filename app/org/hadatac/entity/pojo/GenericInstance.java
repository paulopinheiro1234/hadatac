package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.FirstLabel;
import org.hadatac.utils.NameSpaces;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.URIUtils;

public class GenericInstance extends HADatAcThing implements Comparable<GenericInstance> {

    public GenericInstance(String uri,
            String typeUri,
            String label,
            String comment) {
        this.uri = uri;
        this.typeUri = typeUri;
        this.label = label;
        this.comment = comment;
    }

    public GenericInstance() {
        this.uri = "";
        this.typeUri = "";
        this.label = "";
        this.comment = "";
    }

    public String getTypeLabel() {
    	PlatformType pltType = PlatformType.find(getTypeUri());
    	if (pltType == null || pltType.getLabel() == null) {
    		return "";
    	}
    	return pltType.getLabel();
    }

    public static GenericInstance find(String uri) {
        GenericInstance instance = null;
        Model model;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
        model = qexec.execDescribe();

        StmtIterator stmtIterator = model.listStatements();
        if (!stmtIterator.hasNext()) {
        	return instance;
        }
        
        instance = new GenericInstance();
        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                instance.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                instance.setTypeUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                instance.setComment(object.asLiteral().getString());
            }
        }

        instance.setUri(uri);

        return instance;
    }

    public static int getNumberGenericInstances(String requiredClass) {
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += " select (count(?uri) as ?tot) where { " + 
                " ?model rdfs:subClassOf* " + requiredClass + " . " + 
                " ?uri a ?model ." + 
                "}";

        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                return Integer.parseInt(soln.getLiteral("tot").getString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String jsonInstanceStatisticsByType(String requiredClass) {
    	String result = "[['Model', 'Quantity']";
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += " select ?modelName (count(?uri) as ?tot) where { " + 
                " ?model rdfs:subClassOf* " + requiredClass + " . " + 
                " ?model rdfs:label ?modelName . " +
                " ?uri a ?model ." + 
                " } " +
                " GROUP BY ?modelName ";
        System.out.println(query);
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            int i = 0;
            String n = "";
            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                i = Integer.parseInt(soln.getLiteral("tot").getString());
                n = soln.getLiteral("modelName").getString();
                if (n != null && !n.isEmpty()) {
                	result = result + ", ['" + n + "'," + i + "]";
                }
            }	
        } catch (Exception e) {
            e.printStackTrace();
        }
        result = result + "]";
        return result;
    }

    public static List<GenericInstance> findGenericWithPages(String requiredClass, int pageSize, int offset) {
        List<GenericInstance> instances = new ArrayList<GenericInstance>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
        		"SELECT ?uri WHERE { " + 
                " ?model rdfs:subClassOf* " + requiredClass + " . " + 
                " ?uri a ?model . } " + 
                " LIMIT " + pageSize + 
                " OFFSET " + offset;

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) {
                GenericInstance instance = GenericInstance.find(soln.getResource("uri").getURI());
                instances.add(instance);
            }
        }
        return instances;
    }

    @Override
    public int compareTo(GenericInstance another) {
        return this.getLabel().compareTo(another.getLabel());
    }

    @Override
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }

}
