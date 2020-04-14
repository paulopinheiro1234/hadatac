package org.hadatac.utils;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;

public class FirstLabel {

    public static String getLabel(String uri) {
        System.out.println("HEY!:"+uri);
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
        System.out.println(labelStr);
        return labelStr;
    }

    public static String getPrettyLabel(String uri) {
        String prettyLabel = getLabel(uri).replace("@en","");
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
        // System.out.println(labelStr);
        return labelStr;
    }

}
