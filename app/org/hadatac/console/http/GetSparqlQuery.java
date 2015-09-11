package org.hadatac.console.http;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.TreeMap;

import org.hadatac.console.models.SparqlQuery;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import play.Play;

public class GetSparqlQuery {

    public StringBuffer sparql_query = new StringBuffer();
    public TreeMap<String, StringBuffer> list_of_queries = new TreeMap<String, StringBuffer>();
    public String collection;
    private int numThings = 14;
    public String[] thingTypes = new String[numThings];
    
    public GetSparqlQuery () {} 

    //list_of_queries contains all the queries to execute
    //this.sparql_query will be a query to return all documents in the last collection of
    //collection_urls.
    //this.sparql_query should NOT BE USED OUTSIDE OF THIS CLASS UNLESS YOU KNOW WHAT YOU'RE DOING
    //I'm mostly talking to myself here.

    // for SPARQL queries!
    public GetSparqlQuery (SparqlQuery query) {
        //addSparqlUrls();
        addThingTypes();
        this.collection = Play.application().configuration().getString("hadatac.solr.triplestore") + "/store/sparql";
        
        for (String tabName : thingTypes ){
            this.sparql_query = new StringBuffer();
            this.sparql_query.append(collection);
            this.sparql_query.append("?q=");
            String q = querySelector(tabName);

            String quote = new String();
            try {
                this.sparql_query.append(URLEncoder.encode(q, "UTF-8"));
                quote = URLEncoder.encode("\"", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            
            //System.out.println(tabName + " : " + this.sparql_query);
            this.list_of_queries.put(tabName, this.sparql_query);
        }
    }// /getSolrQuery for SPARQL

    // For SPARQL queries that only make one query (instead of for all tabs)
    // Ideally, the above method should be depreciated in favor of this one, as we move
    //    all thingType queries to their own separate pages.
    public GetSparqlQuery (SparqlQuery query, String tabName) {
        //this.collection = "http://jeffersontest.tw.rpi.edu/solr4/store/sparql";
	this.collection = Play.application().configuration().getString("hadatac.solr.triplestore") + "/store/sparql";
        this.sparql_query = new StringBuffer();
        this.sparql_query.append(collection);
        this.sparql_query.append("?q=");
        String q = querySelector(tabName);
            
        String quote = new String();
        try {
            this.sparql_query.append(URLEncoder.encode(q, "UTF-8"));
            quote = URLEncoder.encode("\"", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }            
        //System.out.println(tabName + " : " + this.sparql_query);
        this.list_of_queries.put(tabName, this.sparql_query);
    }// /getSolrQuery for SPARQL

    // TYPES of THINGS in the metadata. These should be high-level concepts.
    // If this list is updated, make sure each new thingtype has a corresponding
    //  query in the method below, and that numThings is updated accordingly.
    // (Until I make a more dynamic implementation for this....)
    // IDEA: a config file that we can parse into Thing + query, with methods to check it dynamically?
    public void addThingTypes(){
        thingTypes[0]  = "Platforms";
        thingTypes[1]  = "PlatformModels";
        thingTypes[2]  = "Instruments";
        thingTypes[3]  = "InstrumentModels";
        thingTypes[4]  = "Detectors";
        thingTypes[5]  = "DetectorModels";
        thingTypes[6]  = "Entities";
        thingTypes[7]  = "OrganizationsH";
        thingTypes[8]  = "PeopleH";
        thingTypes[9]  = "Characteristics";
        thingTypes[10] = "Units";
	    thingTypes[11] = "SensingPerspectives";
	    thingTypes[12] = "EntityCharacteristics";
	    thingTypes[13] = "Deployments";
    }
    
    public String querySelector(String tabName){
        // default query?
        String q = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        switch (tabName){
            case "Platforms" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#> " + 
                    "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vstoi#> " + 
                    "PREFIX hasneto: <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#> " + 
                    "SELECT ?platURI ?name ?modelName ?sn ?lat ?lng WHERE {" +
                    "    ?platModel rdfs:subClassOf+" + 
                    "    <http://jefferson.tw.rpi.edu/ontology/vstoi#Platform>  ." + 
                    "    ?platURI a ?platModel ." +
                    "    ?platModel rdfs:label ?modelName ." +
                    "    ?platURI rdfs:label ?name ." + 
                    "    OPTIONAL { ?platURI vstoi:hasSerialNumber ?sn } ." + 
                    "    OPTIONAL { ?platModel hasneto:hasFirstCoordinate ?lat ." +
                    "               ?platModel hasneto:hasSecondCoordinate ?lng } ." +
                    "}";
                break;
            case "Instruments" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vstoi#>" +
                    "SELECT ?instURI ?name ?modelName ?sn WHERE {" +
                    " ?instModel rdfs:subClassOf+" +
                    " <http://jefferson.tw.rpi.edu/ontology/vstoi#Instrument> ." +
                    " ?instURI a ?instModel ." +
                    " ?instURI rdfs:label ?name ." +
                    " OPTIONAL { ?instURI vstoi:hasSerialNumber ?sn } ." +
                    " ?instModel rdfs:label ?modelName ." +
                    "}";
                break;
            case "Detectors" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vstoi#>" +
                    "SELECT ?detURI ?name ?modelName ?instName ?sn ?instSN WHERE {" +
                    " ?model rdfs:subClassOf+" +
                    " <http://jefferson.tw.rpi.edu/ontology/vstoi#Detector> ." +
                    " ?model rdfs:label ?modelName ." + 
                    " ?detURI a ?model ." +
                    " ?detURI rdfs:label ?name ." +
                    " OPTIONAL { ?detURI vstoi:hasSerialNumber ?sn } ." +
                    " OPTIONAL { ?detURI vstoi:isInstrumentAttachment ?inst ." +
                    "            ?inst rdfs:label ?instName  ." +                    
                    "            ?inst vstoi:hasSerialNumber ?instSN } ." +
                    "}";
                break;
            case "InstrumentModels" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX foaf:<http://xmlns.com/foaf/0.1/>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                    "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vstoi#>" +
                    "SELECT ?model ?modelName ?superModelName ?maker ?desc ?page ?minTemp ?maxTemp ?tempUnit ?docLink ?numAtt ?numDet ?maxLog WHERE {" +
                    "   ?model rdfs:subClassOf* <http://jefferson.tw.rpi.edu/ontology/vstoi#Instrument> . " + 
                    "   ?model rdfs:label ?modelName ." + 
                    "   OPTIONAL { ?model rdfs:subClassOf ?superModel .  " + 
                    "              ?superModel rdfs:label ?superModelName } ." +
                    "   OPTIONAL { ?model vstoi:hasMaker ?m ." +
                    "              ?m foaf:homepage ?page ." +
                    "              ?m foaf:name ?maker } ." +
                    "   OPTIONAL { ?model vstoi:minOperatingTemperature ?minTemp ." +
                    "              ?model vstoi:maxOperatingTemperature ?maxTemp ." +
                    "              ?model vstoi:hasOperatingTemperatureUnit ?tempUnit } ." +
                    "   OPTIONAL { ?model rdfs:comment ?desc } ." + 
                    "   OPTIONAL { ?model vstoi:numAttachedDetectors ?numAtt } ." +
                    "   OPTIONAL { ?model vstoi:maxDetachableDetectors ?numDet } ." +
                    "   OPTIONAL { ?model vstoi:maxLoggedMeasurements ?maxLog } ." +
                    "   OPTIONAL { ?model vstoi:hasWebDocumentation ?docLink } ." + 
                    "}";
                break;
            case "Entities" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                    "PREFIX oboe: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>" + 
                    "SELECT ?id ?superId ?chara WHERE { " + 
                    "   ?id rdfs:subClassOf* oboe:Entity . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    //"   OPTIONAL { ?ent rdfs:label ?id . } " + 
                    "   OPTIONAL { ?id oboe:hasCharacteristic ?chara . } " +
                    "}";
                break;
            case "OrganizationsH" : 
            	q = "PREFIX prov: <http://www.w3.org/ns/prov#> " + 
            		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            		"SELECT * WHERE { " +
            		"  ?agent a foaf:Group . " + 
            		"  ?agent foaf:name ?name . " + 
            		"  OPTIONAL { ?agent foaf:mbox ?email . } " + 
            		"  OPTIONAL { ?agent foaf:member ?member . } " +
            		"}";
                break;
            case "PeopleH" : 
            	q = "PREFIX prov: <http://www.w3.org/ns/prov#> " + 
            		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            		"SELECT * WHERE { " +
            		"  ?agent a foaf:Person . " + 
            		"  ?agent foaf:name ?name . " + 
            		"  OPTIONAL { ?agent foaf:mbox ?email . } " + 
            		"  OPTIONAL { ?agent foaf:member ?member . } " +
            		"}";
                break;
            case "DetectorModels" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX foaf:<http://xmlns.com/foaf/0.1/>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                    "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vstoi#>" +
                    "SELECT ?modelName ?superModelName ?maker ?desc ?page WHERE { " + 
                    "    ?model rdfs:subClassOf* <http://jefferson.tw.rpi.edu/ontology/vstoi#Detector> . " + 
                	"    ?model rdfs:subClassOf ?superModel .  " + 
                	"    OPTIONAL { ?model rdfs:label ?modelName }  " + 
                	"    OPTIONAL { ?superModel rdfs:label ?superModelName }  " +
                    "    OPTIONAL { ?model vstoi:hasMaker ?m ." +
                    "               ?m foaf:name ?maker ." + 
                    "               ?m foaf:homepage ?page } ." + 
                    "    OPTIONAL { ?model rdfs:comment ?desc } ." + 
                    "    OPTIONAL { ?model vstoi:hasWebDocumentation ?docLink } ." + 
                	"}";
                break;
            case "Characteristics" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                	"SELECT ?modelName ?superModelName WHERE { " + 
                    "   ?modelName rdfs:subClassOf* <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#Characteristic> . " + 
                	"   ?modelName rdfs:subClassOf ?superModelName .  " + 
                	//"   OPTIONAL { ?model rdfs:label ?modelName }  " + 
                	//"   OPTIONAL { ?superModel rdfs:label ?superModelName }  " +
                	"}";
                break;
            case "PlatformModels" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX foaf:<http://xmlns.com/foaf/0.1/>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                    "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vstoi#>" +
                	"SELECT ?modelName ?superModelName ?maker ?desc ?page WHERE { " + 
                    "   ?model rdfs:subClassOf* <http://jefferson.tw.rpi.edu/ontology/vstoi#Platform> . " + 
                	"   ?model rdfs:subClassOf ?superModel .  " + 
                	"   OPTIONAL { ?model rdfs:label ?modelName }  " + 
                	"   OPTIONAL { ?superModel rdfs:label ?superModelName }  " +
                	"   OPTIONAL { ?modelName vstoi:hasMaker ?m ." +
                    "               ?m foaf:name ?maker ." + 
                    "               ?m foaf:homepage ?page } ." + 
                    "    OPTIONAL { ?model rdfs:comment ?desc } ." + 
                	"}";
                break;
            case "Units" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                    "PREFIX oboe: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>" + 
                    "SELECT ?modelName ?superModelName ?chara WHERE { " + 
                    "   ?modelName rdfs:subClassOf* oboe:Standard . " + 
                    "   ?modelName rdfs:subClassOf ?superModelName .  " + 
                    "   OPTIONAL { ?modelName oboe:standardFor ?m .  " + 
                    "              ?m oboe:ofCharacteristic ?chara } . " +
                    "}";
                break;
            case "SensingPerspectives" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vstoi#>" +
                    //"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "PREFIX hasneto: <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#>  " +
                    "SELECT ?sp ?ofModelName ?chara ?accpercent ?accrtwo ?outputres ?maxresponse ?timeunit ?low ?high WHERE {" +
                    " ?sp a vstoi:SensingPerspective . " +
                    " ?sp vstoi:perspectiveOf ?ofModel . " +
                    " ?ofModel rdfs:label ?ofModelName . " +
                    " ?sp hasneto:hasPerspectiveCharacteristic ?chara ." +
                    " OPTIONAL { ?sp vstoi:hasAccuracyPercentage ?accpercent } ." +
                    " OPTIONAL { ?sp vstoi:hasAccuracyR2 ?accrtwo } ." +
                    " OPTIONAL { ?sp vstoi:hasOutputResolution ?outputres } ." +
                    " OPTIONAL { ?sp vstoi:hasMaxResponseTime ?maxresponse ." +
                    "            ?sp vstoi:hasResponseTimeUnit ?timeunit } ." +
                    " OPTIONAL { ?sp vstoi:hasLowRangeValue ?low ." +
                    "            ?sp vstoi:hasHighRangeValue ?high } ." +
                    "}";
                break;
            case "EntityCharacteristics" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                    "SELECT ?ecName ?entity ?chara WHERE { " + 
                    "   ?ec a <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#EntityCharacteristic> . " + 
                    "   ?ec rdfs:label ?ecName .  " + 
                    "   ?ec <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#ofEntity> ?entity .  " + 
                    "   ?ec <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#ofCharacteristic> ?chara .  " + 
                    "}";
                break;
            case "Deployments" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?platform ?instrument ?date WHERE { " + 
                    "   ?dep a vstoi:Deployment . " + 
                    "   ?dep vstoi:hasPlatform ?platform .  " + 
                    "   ?dep hasneto:hasInstrument ?instrument .  " + 
                    "   ?dep prov:startedAtTime ?date .  " + 
                    "}";
                break;
            default :
            	q = "";
            	System.out.println("WARNING: no query for tab " + tabName);
        }// /switch
        return q;
    } // /querySelector


    //Preconditions: The GetSparqlQuery object has been initialized with a Query object
    //Inputs: None. Executes query based on the member string sparql_query.
    //Output: Returns JSON in the form of a string. Currently does not handle http errors
    //		  very gracefully. Need to change this.
    //Postconditions: None
    public String executeQuery(String tab) throws IllegalStateException, IOException{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //HttpGet get = new HttpGet(this.collection_urls.get(collection));
        
        Scanner in = null;
        try {
        	HttpClient client = new DefaultHttpClient();
        	HttpGet request = new HttpGet(list_of_queries.get(tab).toString().replace(" ", "%20"));
        	//System.out.println(tab + " : " + list_of_queries.get(tab));
        	request.setHeader("Accept", "application/sparql-results+json");
        	HttpResponse response = client.execute(request);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
            //System.out.println("response: " + response);    
            return writer.toString();
        } finally
        {
            //in.close();
            //request.close();
        }
    } // /executeQuery()
}
