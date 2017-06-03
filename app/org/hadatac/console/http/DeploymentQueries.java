package org.hadatac.console.http;

import java.io.ByteArrayOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class DeploymentQueries {

    public static final String DEPLOYMENT_BY_URI                 = "DeploymentByURI";
    public static final String DEPLOYMENT_CHARACTERISTICS        = "DeploymentCharacteristics";
    public static final String DEPLOYMENT_CHARACTERISTICS_BY_URI = "DeploymentCharacteristicsByURI";
    public static final String DETECTOR_SENSING_PERSPECTIVE      = "DetectorSensingPerspective";
    public static final String ACTIVE_DEPLOYMENTS                = "ActiveDeployments";
    public static final String CLOSED_DEPLOYMENTS                = "ClosedDeployments";
    
    public static String querySelector(String concept, String uri){
        // default query?
        String q = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        switch (concept){
            case DEPLOYMENT_BY_URI :
                if (uri.startsWith("http")) {
		   uri = "<" + uri + ">";
		}
                q = NameSpaces.getInstance().printSparqlNameSpaceList() + 
		    //"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    //"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    //"PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	    //    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	    //    "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?uri ?platform ?hasFirstCoordinate ?hasSecondCoordinate ?instrument ?detector ?date WHERE { " + 
                    "   " + uri + " a vstoi:Deployment . " + 
                    "   " + uri + " vstoi:hasPlatform ?platformuri .  " +
                    "   ?platformuri rdfs:label ?platform . " +
                    "   OPTIONAL { ?platformuri hasco:hasFirstCoordinate ?hasFirstCoordinate . } " +
                    "   OPTIONAL { ?platformuri hasco:hasSecondCoordinate ?hasSecondCoordinate . } " +
                    "   " + uri + " hasco:hasInstrument ?instrumenturi .  " + 
                    "   ?instrumenturi rdfs:label ?instrument . " +
                    "   OPTIONAL { " + uri + " hasco:hasDetector ?detectoruri . } " + 
                    "   OPTIONAL { ?detectoruri rdfs:label ?detector . } " +
                    "   " + uri + " prov:startedAtTime ?date .  " + 
                    "}";
	        //System.out.println("DEPLOYMENT_BY_URI query: " + q);
                break;
            case DEPLOYMENT_CHARACTERISTICS_BY_URI : 
                if (uri.startsWith("http")) {
		   uri = "<" + uri + ">";
		}
                q = NameSpaces.getInstance().printSparqlNameSpaceList() + 
		    //"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    //"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    //"PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	    //    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	    //    "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?deturi ?detModel ?insturi ?instModel ?sp ?ent ?char ?charName WHERE { { " + 
                    "   " + uri + " a vstoi:Deployment . " + 
                    "   " + uri + " hasco:hasDetector ?deturi .  " +
                    "   ?deturi a ?detModel . " +
                    "   ?sp vstoi:perspectiveOf ?detModel . " +
                    "   ?sp hasco:hasPerspectiveEntity ?ent ." +
                    "   ?sp hasco:hasPerspectiveCharacteristic ?char . " +
                    "   ?char rdfs:label ?charName .  " + 
                    "} " + 
                    "UNION { " + 
                    "   " + uri + " a vstoi:Deployment . " + 
                    "   " + uri + " hasco:hasInstrument ?insturi .  " +
                    "   ?insturi a ?instModel . " +
                    "   ?sp vstoi:perspectiveOf ?instModel . " +
                    "   ?sp hasco:hasPerspectiveEntity ?ent ." +
                    "   ?sp hasco:hasPerspectiveCharacteristic ?char . " +
                    "   ?char rdfs:label ?charName .  " + 
                    "} }";
                break;
            case DEPLOYMENT_CHARACTERISTICS : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?deploy ?deturi ?detModel WHERE { " + 
                    "   ?deploy a vstoi:Deployment . " + 
                    "   ?deploy hasco:hasDetector ?deturi .  " +
                    "   ?deturi a ?detModel . " +
                    "}";
                break;
            case DETECTOR_SENSING_PERSPECTIVE : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                        "PREFIX foaf:<http://xmlns.com/foaf/0.1/>" + 
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" +
                        "SELECT ?det ?model ?sp ?ec ?ecName WHERE { " + 
                        "    ?model rdfs:subClassOf* vstoi:Detector . " +
                        "    ?det a ?model . " +
                    	"    OPTIONAL { ?model rdfs:label ?modelName }  " + 
                        "    OPTIONAL { ?sp vstoi:perspectiveOf ?model } " +
                        "    OPTIONAL { ?sp vstoi:hasPerspectiveCharacteristic ?ec } " +
                        "    OPTIONAL { ?ec rdfs:label ?ecName }  " + 
                    	"}";
                break;
            case ACTIVE_DEPLOYMENTS : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?platform ?instrument ?datetime WHERE { " + 
                    "   ?dep a vstoi:Deployment . " + 
                    "   ?dep vstoi:hasPlatform ?platformuri .  " +
                    "   ?platformuri rdfs:label ?platform . " +
                    "   ?dep hasco:hasInstrument ?instrumenturi .  " + 
                    "   ?instrumenturi rdfs:label ?instrument . " +
                    "   ?dep prov:startedAtTime ?datetime .  " + 
                    "   FILTER NOT EXIST { ?dep prov:startedAtTime ?enddatetime . } " + 
                    "} " + 
                    "ORDER BY DESC(?datetime) ";
                break;
            case CLOSED_DEPLOYMENTS : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
            	    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
            	    "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?platform ?instrument ?startdatetime ?enddatetime WHERE { " + 
                    "   ?dep a vstoi:Deployment . " + 
                    "   ?dep vstoi:hasPlatform ?platformuri .  " +
                    "   ?platformuri rdfs:label ?platform . " +
                    "   ?dep hasco:hasInstrument ?instrumenturi .  " + 
                    "   ?instrumenturi rdfs:label ?instrument . " +
                    "   ?dep prov:startedAtTime ?startdatetime .  " + 
                    "   FILTER EXIST { ?dep prov:startedAtTime ?enddatetime . } " + 
                    "} " +
                    "ORDER BY DESC(?datetime) ";
                break;
            default :
            	q = "";
            	System.out.println("WARNING: no query for tab " + concept);
        }
        
        return q;
    }

    public static String exec(String concept, String uri) {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try {
    		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    					querySelector(concept, uri);
    		Query query = QueryFactory.create(queryString);
    			
    		QueryExecution qexec = QueryExecutionFactory.sparqlService(
    				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    		ResultSet results = qexec.execSelect();
    		
    		ResultSetFormatter.outputAsJSON(outputStream, results);
    		qexec.close();
    		
    		return outputStream.toString("UTF-8");
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	return "";
    }
}
