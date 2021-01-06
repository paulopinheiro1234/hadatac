package org.hadatac.utils;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.entity.pojo.SPARQLUtilsFacetSearch;

public class FirstLabel {

    public static String getLabel(String uri) {
        
        if ((uri == null) || (uri.equals(""))) {
            return "";
        } 

        //System.out.println("[FirstLabel] getLabel() request:[" + uri + "]");

        if (uri.startsWith("http")) {
            uri = "<" + uri.trim() + ">";
        }
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?label WHERE { \n" + 
                "  " + uri + " rdfs:label ?label . \n" + 
                "}";

        //System.out.println("[FirstLabel] getLabel() queryString: \n" + queryString);
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        String labelStr = "";
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln.get("label") != null) {
                labelStr = soln.get("label").toString();
                
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

    public static String getLabelFacetSearch(String uri) {

        if ((uri == null) || (uri.equals(""))) {
            return "";
        }

        //System.out.println("[FirstLabel] getLabel() request:[" + uri + "]");

        if (uri.startsWith("http")) {
            uri = "<" + uri.trim() + ">";
        }
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "SELECT ?label WHERE { \n" +
                "  " + uri + " rdfs:label ?label . \n" +
                "}";

        //System.out.println("[FirstLabel] getLabel() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtilsFacetSearch.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        String labelStr = "";
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln.get("label") != null) {
                labelStr = soln.get("label").toString();

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
