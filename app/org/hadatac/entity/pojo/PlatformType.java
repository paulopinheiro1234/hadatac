package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;


public class PlatformType extends HADatAcClass implements Comparable<PlatformType> {

    static String className = "vstoi:Platform";

    public PlatformType () {
        super(className);
    }

    public static List<PlatformType> find() {
        List<PlatformType> platformTypes = new ArrayList<PlatformType>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf* " + className + " . " + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            PlatformType platformType = find(soln.getResource("uri").getURI());
            platformTypes.add(platformType);
        }			

        java.util.Collections.sort((List<PlatformType>) platformTypes);
        return platformTypes;   
    }

    public static PlatformType find(String uri) {
        PlatformType platformType = null;
        
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        platformType = new PlatformType();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                platformType.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
                platformType.setSuperUri(object.asResource().getURI());
            }
        }

        platformType.setUri(uri);
        platformType.setLocalName(uri.substring(uri.indexOf('#') + 1));

        return platformType;
    }

    
    @Override
    public int compareTo(PlatformType another) {
        if (this.getLabel() != null && another.getLabel() != null) {
            return this.getLabel().compareTo(another.getLabel());
        }
        return this.getLocalName().compareTo(another.getLocalName());
    }
}
