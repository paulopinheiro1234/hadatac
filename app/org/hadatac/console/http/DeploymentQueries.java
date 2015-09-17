package org.hadatac.console.http;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import play.Play;

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
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?uri ?platform ?instrument ?detector ?date WHERE { " + 
                    "   <" + uri + "> a vstoi:Deployment . " + 
                    "   <" + uri + "> vstoi:hasPlatform ?platformuri .  " +
                    "   ?platformuri rdfs:label ?platform . " +
                    "   <" + uri + "> hasneto:hasInstrument ?instrumenturi .  " + 
                    "   ?instrumenturi rdfs:label ?instrument . " +
                    "   <" + uri + "> hasneto:hasDetector ?detectoruri .  " + 
                    "   ?detectoruri rdfs:label ?detector . " +
                    "   <" + uri + "> prov:startedAtTime ?date .  " + 
                    "}";
                break;
            case DEPLOYMENT_CHARACTERISTICS_BY_URI : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?deturi ?detModel ?sp ?ec ?ecName WHERE { " + 
                    "   <" + uri + "> a vstoi:Deployment . " + 
                    "   <" + uri + "> hasneto:hasDetector ?deturi .  " +
                    "   ?deturi a ?detModel . " +
                    "   ?sp vstoi:perspectiveOf ?detModel . " +
                    "   ?sp hasneto:hasPerspectiveCharacteristic ?ec ." +
                    "   ?ec rdfs:label ?ecName .  " + 
                    "}";
                break;
            case DEPLOYMENT_CHARACTERISTICS : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?deploy ?deturi ?detModel WHERE { " + 
                    "   ?deploy a vstoi:Deployment . " + 
                    "   ?deploy hasneto:hasDetector ?deturi .  " +
                    "   ?deturi a ?detModel . " +
                    //"   OPTIONAL { ?sp vstoi:perspectiveOf ?detModel } " +
                    //"   OPTIONAL { ?sp vstoi:hasPerspectiveCharacteristic ?ec } " +
                    //"   OPTIONAL { ?ec rdfs:label ?ecName }  " + 
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
                    "   ?dep hasneto:hasInstrument ?instrumenturi .  " + 
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
                    "   ?dep hasneto:hasInstrument ?instrumenturi .  " + 
                    "   ?instrumenturi rdfs:label ?instrument . " +
                    "   ?dep prov:startedAtTime ?startdatetime .  " + 
                    "   FILTER EXIST { ?dep prov:startedAtTime ?enddatetime . } " + 
                    "} " +
                    "ORDER BY DESC(?datetime) ";
                break;
            default :
            	q = "";
            	System.out.println("WARNING: no query for tab " + concept);
        }// /switch
        return q;
    } // /querySelector


    public static String exec(String concept, String uri) {
	    String collection = Play.application().configuration().getString("hadatac.solr.triplestore") + "/store/sparql";
        StringBuffer sparql_query = new StringBuffer();
        sparql_query.append(collection);
        sparql_query.append("?q=");

        String q = querySelector(concept, uri);
        System.out.println("Query: [" + q + "]");
        try {
            sparql_query.append(URLEncoder.encode(q, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }            

        CloseableHttpClient httpClient = HttpClients.createDefault();
        Scanner in = null;
        try {
        	HttpClient client = new DefaultHttpClient();
        	HttpGet request = new HttpGet(sparql_query.toString().replace(" ", "%20"));
        	request.setHeader("Accept", "application/sparql-results+json");
        	HttpResponse response;
            StringWriter writer = new StringWriter();			
            try {
				response = client.execute(request);

                try {
		    		IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
		    	} catch (Exception e) {
			    	// TODO Auto-generated catch block
			    	e.printStackTrace();
			    } 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
            return writer.toString();
        } finally {
        }
    } // /executeQuery()
}
