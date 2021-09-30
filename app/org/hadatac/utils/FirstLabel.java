package org.hadatac.utils;

import com.typesafe.config.ConfigFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.entity.pojo.SPARQLUtilsFacetSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstLabel {

    private static final Logger log = LoggerFactory.getLogger(FirstLabel.class);

    public static String getLabel(String... s)  {

        String uri = s.length > 0 ? s[0] : null;
        String namedGraph = s.length > 1 ? s[1] : null;
        return retrieveLabel(uri, namedGraph, false);

    }

    public static String getLabelFacetSearch(String... s) {
        String uri = s.length > 0 ? s[0] : null;
        String namedGraph = s.length > 1 ? s[1] : null;
        return retrieveLabel(uri, namedGraph, true);
    }

    private static String retrieveLabel(String uri, String namedGraph, boolean useInMemoryModel) {

        if ((uri == null) || (uri.equals(""))) {
            log.warn("an empty URI is given for retrieving it label.");
            return "";
        }

        if (uri.startsWith("http")) uri = "<" + uri.trim() + ">";
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "SELECT ?graph ?label WHERE { \n" +
                "GRAPH ?graph { \n" +
                "  " + uri + " rdfs:label ?label . } \n" +
                "}";
        ResultSetRewindable resultsrw = null;
        if ( !useInMemoryModel ) {
            resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
        } else {
            resultsrw = SPARQLUtilsFacetSearch.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
        }

        if ( resultsrw == null || resultsrw.size() == 0) {
            log.warn("URI " + uri + " does not have any label in the graph.");
            return "";
        }

        String labelStr = "";

        // if there is only one label returned, use it anyway regardless of the preferred namedGraph
        if ( resultsrw.size() == 1 ) {
            QuerySolution soln = resultsrw.next();
            if (soln.get("label") != null) {
                labelStr = soln.get("label").toString();
            }
            else if(labelStr.isEmpty()) {
                log.warn("URI " + uri + " does not have any label in the graph.");
                return "";
            }
        }

        // if more than label is returned, use the one returned by the preferred namedGraph

        if ( namedGraph == null || namedGraph.isEmpty() ) {
            namedGraph = ConfigFactory.load().getString("hadatac.graph.preferred");
            if (namedGraph == null || namedGraph.isEmpty()) {
                log.warn("multiple labels encounterred with URI " + uri + ", but no preferred graph is specified");
            }
        }

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if ( soln.get("graph") == null ) continue;
            if ( soln.get("graph").toString().toLowerCase().contains(namedGraph.toLowerCase()) == false && resultsrw.hasNext() ) continue;
            if (soln.get("label") != null) {
                labelStr = soln.get("label").toString();
            }
            if (!labelStr.isEmpty()) break;
            else {
                log.warn("URI " + uri + " does not have a label in the preferred graph " + namedGraph);
                break;
            }
        }
        return labelStr;
    }

    public static String getPrettyLabel(String uri) {

        String prettyLabel = null;

        // the passed parameter is indeed a URI
        if ( uri.startsWith("http") || uri.startsWith("<http") ) {
            prettyLabel = getLabel(uri).replace("@en", "");
        }
        // the passed parameter is a label already
        else {
            prettyLabel = uri.replace("@en", "");
        }

        if (!prettyLabel.equals("")) {
            String c0 = prettyLabel.substring(0,1).toUpperCase();
            if (prettyLabel.length() == 1) {
                prettyLabel = c0;
            } else {
                prettyLabel = c0 + prettyLabel.substring(1);
            }
        }
        return prettyLabel;
    }

    public static String getLabelDescription(String uri) {
        //uri=" http://purl.obolibrary.org/obo/CMO_0000012";
        if ((uri == null) || (uri.equals(""))) {
            return "";
        }

        //System.out.println("[FirstLabel] getLabel() request:[" + uri + "]");

        if (uri.startsWith("http")) {
            uri = "<" + uri.trim() + ">";
        }

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "PREFIX obo-term: <http://purl.obolibrary.org/obo/> \n"+
                "SELECT ?id ?definition WHERE { \n" +
                "  " + uri + " obo-term:IAO_0000115 ?definition . \n" +
                "}";

        //System.out.println("[FirstLabel] getLabel() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        String labelStr = "";
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln.get("definition") != null) {
                labelStr = soln.get("definition").toString();

            }


            if (!labelStr.isEmpty()) {

                break;
            }
            else if(labelStr.isEmpty()){
                System.out.println("RETURNED EMPTY");
            }
        }
        if(labelStr==""){
            labelStr=differentQuery(uri);
        }
        // System.out.println(labelStr);
        return labelStr;
    }

    public static String differentQuery(String uri) {
        System.out.println(uri);
        if ((uri == null) || (uri.equals(""))) {
            return "";
        }

        //System.out.println("[FirstLabel] getLabel() request:[" + uri + "]");

        if (uri.startsWith("http")) {
            uri = "<" + uri.trim() + ">";
        }
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "PREFIX obo: <http://purl.obolibrary.org/obo/> \n"+
                "SELECT ?id ?definition WHERE { \n" +
                "  " + uri + " obo:def ?definition . \n" +
                "}";
        //System.out.println("[FirstLabel] getLabel() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        String labelStr = "";
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();

            if (soln.get("definition") != null) {
                labelStr = soln.get("definition").toString();

            }


            if (!labelStr.isEmpty()) {

                break;
            }
            else if(labelStr.isEmpty()){
                System.out.println("RETURNED EMPTY");
            }
        }
        if(labelStr==""){
            labelStr=differentQuery2(uri);
        }

        return labelStr;
    }

    public static String differentQuery2(String uri) {
        //uri=" http://purl.obolibrary.org/obo/CMO_0000012";
        if ((uri == null) || (uri.equals(""))) {
            return "";
        }

        //System.out.println("[FirstLabel] getLabel() request:[" + uri + "]");

        if (uri.startsWith("http")) {
            uri = "<" + uri.trim() + ">";
        }

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "PREFIX obo: <http://purl.obolibrary.org/obo/> \n"+
                "SELECT ?id ?definition WHERE { \n" +
                "  " + uri + " obo:IAO_0000115 ?definition . \n" +
                "}";

        System.out.println(queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        String labelStr = "";
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln.get("definition") != null) {
                labelStr = soln.get("definition").toString();

            }


            if (!labelStr.isEmpty()) {

                break;
            }
            else if(labelStr.isEmpty()){
                System.out.println("RETURNED EMPTY");
            }
        }

        System.out.println(labelStr);
        return labelStr;
    }


    public static String getSioLabelDescription(String uri) {
        System.out.println(uri);
        if ((uri == null) || (uri.equals(""))) {
            return "";
        }

        //System.out.println("[FirstLabel] getLabel() request:[" + uri + "]");

        if (uri.startsWith("http")) {
            uri = "<" + uri.trim() + ">";
        }
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "PREFIX dct: <http://purl.org/dc/terms/> \n"+
                "SELECT ?id ?description WHERE { \n" +
                "  " + uri + " dct:description ?description . \n" +
                "}";
        //System.out.println("[FirstLabel] getLabel() queryString: \n" + queryString);
        System.out.println(queryString);
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        String labelStr = "";
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();

            if (soln.get("description") != null) {
                labelStr = soln.get("description").toString();

            }


            if (!labelStr.isEmpty()) {

                break;
            }
            else if(labelStr.isEmpty()){
                System.out.println("RETURNED EMPTY");
            }
        }

        return labelStr;
    }

}
