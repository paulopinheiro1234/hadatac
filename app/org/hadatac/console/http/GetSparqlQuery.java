package org.hadatac.console.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.TreeMap;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.query.ResultSetFormatter;

import play.Play;

public class GetSparqlQuery {
    public String collection;
    
    public GetSparqlQuery () {} 

    public GetSparqlQuery (SparqlQuery query) {
    	this(Collections.METADATA_SPARQL, query);
    }
    
    public GetSparqlQuery (String collectionSource, SparqlQuery query) {
        this.collection = Collections.getCollectionsName(collectionSource);
        System.out.println("Collection: " + collection);
    }

    public GetSparqlQuery (SparqlQuery query, String tabName) {
    	this(Collections.METADATA_SPARQL, query, tabName);
    }

    public GetSparqlQuery (String collectionSource, SparqlQuery query, String tabName) {
    	this.collection = Collections.getCollectionsName(collectionSource);
        System.out.println("Collection: " + collection);
    }
    
    public String querySelector(String tabName){
        String q = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        switch (tabName){
            case "Platforms" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#> " + 
                    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#> " + 
                    "PREFIX hasneto: <http://hadatac.org/ont/hasneto#> " + 
                    "SELECT ?platURI ?name ?modelName ?sn ?lat ?lng WHERE {" +
                    "    ?platModel rdfs:subClassOf+" + 
                    "    vstoi:Platform  ." + 
                    "    ?platURI a ?platModel ." +
                    "    ?platModel rdfs:label ?modelName ." +
                    "    ?platURI rdfs:label ?name ." + 
                    "    OPTIONAL { ?platURI vstoi:hasSerialNumber ?sn } ." + 
                    "    OPTIONAL { ?platURI hasco:hasFirstCoordinate ?lat ." +
                    "               ?platURI hasco:hasSecondCoordinate ?lng } ." +
                    "}";
                break;
            case "Instruments" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" +
                    "SELECT ?instURI ?name ?modelName ?sn WHERE {" +
                    " ?instModel rdfs:subClassOf+" +
                    " vstoi:Instrument ." +
                    " ?instURI a ?instModel ." +
                    " ?instURI rdfs:label ?name ." +
                    " OPTIONAL { ?instURI vstoi:hasSerialNumber ?sn } ." +
                    " ?instModel rdfs:label ?modelName ." +
                    "}";
                break;
            case "Detectors" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" +
                    "SELECT ?detURI ?name ?modelName ?instName ?sn ?instSN WHERE {" +
                    " ?model rdfs:subClassOf+" +
                    " vstoi:Detector ." +
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
                    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" +
                    "SELECT ?model ?modelName ?superModelName ?maker ?desc ?page ?minTemp ?maxTemp ?tempUnit ?docLink ?numAtt ?numDet ?maxLog WHERE {" +
                    "   ?model rdfs:subClassOf* vstoi:Instrument . " + 
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
                    "PREFIX sio: <http://semanticscience.org/resource/>" +
                    "SELECT ?id ?superId ?label ?chara WHERE { " + 
                    "   ?id rdfs:subClassOf* sio:Object . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label . } " + 
                    "   OPTIONAL { ?id rdfs:comment ?chara . } " +
                    "}";
                break;
            case "OrganizationsH" : 
            	q = "PREFIX prov: <http://www.w3.org/ns/prov#> " + 
            		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            		"SELECT * WHERE { " +
            		"  ?agent a foaf:Organization . " + 
            		"  OPTIONAL { ?agent foaf:name ?name . } " + 
            		"  OPTIONAL { ?agent foaf:mbox ?email . } " + 
            		"}";
                break;
            case "GroupsH" : 
            	q = "PREFIX prov: <http://www.w3.org/ns/prov#> " + 
            		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
            		"PREFIX hadatac: <http://hadatac.org/ont/hadatac#> " + 
            		"SELECT * WHERE { " +
            		"  ?agent a foaf:Group . " + 
            		"  OPTIONAL { ?agent foaf:name ?name . } " + 
            		"  OPTIONAL { ?agent foaf:homepage ?page . } " + 
            		"  OPTIONAL { ?agent hadatac:isMemberOfGroup ?group . } " + 
            		"}";
                break;
            case "PeopleH" : 
            	q = "PREFIX prov: <http://www.w3.org/ns/prov#> " + 
            		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " + 
            		"PREFIX hadatac: <http://hadatac.org/ont/hadatac#> " + 
            		"SELECT * WHERE { " +
            		"  ?agent a foaf:Person . " + 
            		"  OPTIONAL { ?agent foaf:name ?name . } " + 
            		"  OPTIONAL { ?agent foaf:mbox ?email . } " + 
            		"  OPTIONAL { ?agent hadatac:isMemberOfGroup ?group . } " + 
            		"}";
                break;
            case "DetectorModels" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX foaf:<http://xmlns.com/foaf/0.1/>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" +
                    "SELECT ?model ?modelName ?superModelName ?maker ?desc ?page WHERE { " + 
                    "    ?model rdfs:subClassOf* vstoi:Detector . " + 
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
                    "PREFIX hasneto: <http://hadatac.org/ont/hasneto#> " +
                    "PREFIX chear: <http://hadatac.org/ont/chear#>" + 
                    "PREFIX sio: <http://semanticscience.org/resource/>" +
                	"SELECT DISTINCT ?modelName ?superModelName ?label ?comment WHERE { " + 
                    "   ?modelName rdfs:subClassOf* sio:Attribute . " +
                    "   ?modelName rdfs:subClassOf ?superModelName .  " + 
                    "   OPTIONAL { ?modelName rdfs:label ?label } . " + 
                    " 	OPTIONAL { ?modelName rdfs:comment ?comment } . " +
                	"}";
                break;
            case "PlatformModels" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX foaf:<http://xmlns.com/foaf/0.1/>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" +
                	"SELECT ?model ?modelName ?superModelName ?maker ?desc ?page WHERE { " + 
                    "   ?model rdfs:subClassOf* vstoi:Platform . " + 
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
                    "PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>" +
                    "SELECT ?id ?superModelName ?comment ?label WHERE { " + 
                    "   ?id rdfs:subClassOf* obo:UO_0000000 . " + 
                    "   ?id rdfs:subClassOf ?superModelName .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } ." +
                    "   OPTIONAL { ?id rdfs:comment ?comment } . " +
                    "}";
                break;
            case "SensingPerspectives" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" +
                    "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?sp ?ofModelName ?chara ?accpercent ?accrtwo ?outputres ?maxresponse ?timeunit ?low ?high WHERE {" +
                    " ?sp a vstoi:SensingPerspective . " +
                    " ?sp vstoi:perspectiveOf ?ofModel . " +
                    " ?ofModel rdfs:label ?ofModelName . " +
                    " ?sp hasco:hasPerspectiveCharacteristic ?chara ." +
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
                    "PREFIX hasneto: <http://hadatac.org/ont/hasneto#> " + 
                    "SELECT ?ecName ?entity ?chara WHERE { " + 
                    "   ?ec a hasneto:EntityCharacteristic . " + 
                    "   ?ec rdfs:label ?ecName .  " + 
                    "   ?ec hasneto:ofEntity ?entity .  " + 
                    "   ?ec hasneto:ofCharacteristic ?chara .  " + 
                    "}";
                break;
            case "Deployments" : 
                q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        	        "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
        	        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
                    "SELECT ?uri ?platform ?platformName ?instrument ?instrumentName ?date WHERE { " + 
                    "   ?uri a vstoi:Deployment . " + 
                    "   ?uri vstoi:hasPlatform ?platform .  " + 
                    "   ?uri hasco:hasInstrument ?instrument .  " + 
                    "   ?uri prov:startedAtTime ?date .  " + 
                    "   OPTIONAL { ?platform rdfs:label ?platformName } ." + 
                    "   OPTIONAL { ?instrument rdfs:label ?instrumentName } ." + 
                    "}";
                break;
            case "Demographics" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:Demographic . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
//                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
					" 	OPTIONAL { ?id rdfs:comment ?comment } . " +
                    "}";
                break;
            case "BirthOutcomes" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:BirthOutcome . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
//                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    " 	OPTIONAL { ?id rdfs:comment ?comment } . " +
                    "}";
                break;
            case "HousingCharacteristic" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:HousingCharacteristic . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
//                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    " 	OPTIONAL { ?id rdfs:comment ?comment } . " +
                    "}";
                break;
            case "ATIDU" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:ATIDU . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
//                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    " 	OPTIONAL { ?id rdfs:comment ?comment } . " +
                    "}";
                break;
            case "Anthropometry" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:Anthropometry . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
//                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    " 	OPTIONAL { ?id rdfs:comment ?comment } . " +
                    "}";
                break;
            case "PregnancyCharacteristic" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:PregnancyCharacteristic . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
//                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    " 	OPTIONAL { ?id rdfs:comment ?comment } . " +
                    "}";
                break;
            case "Analytes" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:Analyte . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    "}";
                break;
            case "Alkaloids" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:AlkylPhosphatePesticideMetabolite . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    "}";
                break;
            case "Arsenic" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:ArsenicSpecies . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    "}";
                break;
            case "Elements" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:Element . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    "}";
                break;
            case "OrganicAromatic" :
            	q = "PREFIX sio: <http://semanticscience.org/resource/>" + 
            		"PREFIX chear: <http://hadatac.org/ont/chear#>" + 
            		"PREFIX prov: <http://www.w3.org/ns/prov#>" + 
            		"PREFIX hasco: <http://hadatac.org/ont/hasco/>" + 
            		"PREFIX hasneto: <http://hadatac.org/ont/hasneto#>" + 
            		"PREFIX dcterms: <http://purl.org/dc/terms/>" + 
            		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
            		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
            		"SELECT ?id ?superId ?label ?comment WHERE { " + 
                    "   ?id rdfs:subClassOf* chear:OrganicAromaticCompound . " + 
                    "   ?id rdfs:subClassOf ?superId .  " + 
                    "   OPTIONAL { ?id rdfs:label ?label } . " + 
                    " 	OPTIONAL { ?id skos:definition ?comment } . " +
                    "}";
                break;
            case "Indicators" :
            	q = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" + 
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + 
                        "PREFIX hasneto: <http://hadatac.org/ont/hasneto#> " +
                        "PREFIX chear: <http://hadatac.org/ont/chear#>" + 
                        "PREFIX sio: <http://semanticscience.org/resource/>" +
                    	"SELECT DISTINCT ?modelName ?superModelName ?label ?comment WHERE { " + 
                        "   ?modelName rdfs:subClassOf* hasco:Indicator . " +
                        "   ?modelName rdfs:subClassOf ?superModelName .  " + 
                        "   OPTIONAL { ?modelName rdfs:label ?label } . " + 
                        " 	OPTIONAL { ?modelName rdfs:comment ?comment } . " +
                    	"}";
                break;
            default :
            	q = "";
            	System.out.println("WARNING: no query for tab " + tabName);
        }
        return q;
    }
    
    public String executeQuery(String tab) {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try {
    		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    				querySelector(tab);
    		Query query = QueryFactory.create(queryString);
    			
    		QueryExecution qexec = QueryExecutionFactory.sparqlService(collection, query);
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

