package org.hadatac.console.http;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.CollectionUtil.Collection;
import org.hadatac.utils.NameSpaces;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;


public class GetSparqlQueryDynamic {
    public String collection;

    public GetSparqlQueryDynamic () {}

    public GetSparqlQueryDynamic (SparqlQuery query) {
        this(CollectionUtil.Collection.METADATA_SPARQL, query);
    }

    public GetSparqlQueryDynamic (Collection collectionSource, SparqlQuery query) {
        collection = CollectionUtil.getCollectionPath(collectionSource);
        System.out.println("Collection: " + collection);
    }

    public GetSparqlQueryDynamic (SparqlQuery query, String tabName) {
        this(CollectionUtil.Collection.METADATA_SPARQL, query, tabName);
    }

    public GetSparqlQueryDynamic (Collection collectionSource, SparqlQuery query, String tabName) {
        this.collection = CollectionUtil.getCollectionPath(collectionSource);
        System.out.println("Collection: " + collection);
    }

    public String querySelectorGeneric(String tabName) {
        String indicator = DynamicFunctions.getConceptUriByTabName(tabName);

        System.out.println("In Query Selector: " + indicator);
        String q = DynamicFunctions.getPrefixes() +
                "SELECT DISTINCT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " +
                "WHERE { " +
                "  ?id rdfs:subClassOf* " + indicator + ". " +
                "  ?id rdfs:subClassOf ?superId . " +
                "  ?id rdfs:label ?label ." +
                "  OPTIONAL {?id dcterms:identifier ?iden} . " +
                "  OPTIONAL {?id rdfs:comment ?comment} . " +
                "  OPTIONAL {?id skos:definition ?def} . " +
                "  OPTIONAL {?id hasco:hasUnit ?unit} . " +
                "  OPTIONAL {?id skos:editorialNote ?note} . " +
                "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " +
                "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " +
                "} ";
        return q;
    }

    public String executeQuery(String tab) throws IllegalStateException, IOException {
        String indicatorUri = DynamicFunctions.getConceptUriByTabName(tab);
        if (!URIUtils.isValidURI(indicatorUri)) {
            return "";
        }

        try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                    + "SELECT DISTINCT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith WHERE { \n"
                    + "  ?id rdfs:subClassOf* " + indicatorUri + " . \n"
                    + "  ?id rdfs:subClassOf ?superId . \n"
                    + "  ?id rdfs:label ?label . \n"
                    + "  OPTIONAL { ?id dcterms:identifier ?iden } . \n"
                    + "  OPTIONAL { ?id rdfs:comment ?comment } . \n"
                    + "  OPTIONAL { ?id skos:definition ?def } . \n"
                    + "  OPTIONAL { ?id hasco:hasUnit ?unit } . \n"
                    + "  OPTIONAL { ?id skos:editorialNote ?note } . \n"
                    + "  OPTIONAL { ?id prov:wasAttributedTo ?attrTo } . \n"
                    + "  OPTIONAL { ?id prov:wasAssociatedWith ?assocWith } . \n"
                    + "} \n";

            ResultSetRewindable resultsrw = SPARQLUtils.select(collection, queryString);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, resultsrw);

            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
