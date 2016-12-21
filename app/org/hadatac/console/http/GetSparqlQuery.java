package org.hadatac.console.http;
//This Java Class was Dynamically Generated
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.TreeMap;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.utils.Collections;
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
    private int numThings = 51;
    public String[] thingTypes = new String[numThings];

    public GetSparqlQuery () {} 

    public GetSparqlQuery (SparqlQuery query) {
        this(Collections.METADATA_SPARQL, query);
    }

    public GetSparqlQuery (String collectionSource, SparqlQuery query) {
        addThingTypes();
        this.collection = Collections.getCollectionsName(collectionSource);
        System.out.println("Collection: " + collection);

        for (String tabName : thingTypes ){
            this.sparql_query = new StringBuffer();
            this.sparql_query.append(collection);
            this.sparql_query.append("?q=");
            String q = querySelector(tabName);

            @SuppressWarnings("unused")
            String quote = new String();
            try {
                this.sparql_query.append(URLEncoder.encode(q, "UTF-8"));
                quote = URLEncoder.encode("\"", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            this.list_of_queries.put(tabName, this.sparql_query);
        }
    }

    public GetSparqlQuery (SparqlQuery query, String tabName) {
        this(Collections.METADATA_SPARQL, query, tabName);
    }

    public GetSparqlQuery (String collectionSource, SparqlQuery query, String tabName) {
        this.collection = Collections.getCollectionsName(collectionSource);
        System.out.println("Collection: " + collection);
        this.sparql_query = new StringBuffer();
        this.sparql_query.append(collection);
        this.sparql_query.append("?q=");
        String q = querySelector(tabName);

        @SuppressWarnings("unused")
        String quote = new String();
        try {
            this.sparql_query.append(URLEncoder.encode(q, "UTF-8"));
            quote = URLEncoder.encode("\"", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        this.list_of_queries.put(tabName, this.sparql_query);
    }

    public void addThingTypes(){
        thingTypes[0]  = "AlcoholTobaccoandIllicitDrugUse";
        thingTypes[1]  = "Acculturation";
        thingTypes[2]  = "AlkylPhosphatePesticideMetabolite";
        thingTypes[3]  = "Anthropometry";
        thingTypes[4]  = "ArsenicSpecies";
        thingTypes[5]  = "PhysicalandMentalAssessment";
        thingTypes[6]  = "BirthOutcome";
        thingTypes[7]  = "DeliveryCharacteristics";
        thingTypes[8]  = "Demographic";
        thingTypes[9]  = "DietandNutrition";
        thingTypes[10]  = "Element";
        thingTypes[11]  = "EnvironmentalExposure";
        thingTypes[12]  = "HospitalUtilizationandAccesstoCare";
        thingTypes[13]  = "HousingCharacteristic";
        thingTypes[14]  = "MedicalHistory";
        thingTypes[15]  = "MentalHealth";
        thingTypes[16]  = "MercurySpecies";
        thingTypes[17]  = "NeighborhoodCharacteristic";
        thingTypes[18]  = "OralHealth";
        thingTypes[19]  = "VolatileOrganicCompound";
        thingTypes[20]  = "Paraben";
        thingTypes[21]  = "ParentalHealthandFamilyHistory";
        thingTypes[22]  = "PFC";
        thingTypes[23]  = "PersonalProductUse";
        thingTypes[24]  = "EnvironmentalPhenol";
        thingTypes[25]  = "Phthalate";
        thingTypes[26]  = "PhysicalActivityandFitness";
        thingTypes[27]  = "PolybrominatedDiphenylEther";
        thingTypes[28]  = "PregnancyCharacteristic";
        thingTypes[29]  = "PregnancySample";
        thingTypes[30]  = "PrescriptionMedicationandDietarySupplements";
        thingTypes[31]  = "ReproductiveHealth";
        thingTypes[32]  = "SleepCharacterisitic";
        thingTypes[33]  = "SocioeconomicClassStatus";
        thingTypes[34]  = "TargetedAnalyte";
        thingTypes[35]  = "TobaccoMetabolite";
        thingTypes[36]  = "Creatinine";
        thingTypes[37]  = "DataAcquisitionSchemaAttribute";
        thingTypes[38]  = "Organization";
        thingTypes[39]  = "Person";
        thingTypes[40]  = "Attribute";
        thingTypes[41]  = "Cell";
        thingTypes[42]  = "Entity";
        thingTypes[43]  = "Object";
        thingTypes[44]  = "Process";
        thingTypes[45]  = "Tissue";
        thingTypes[46]  = "Deployments";
        thingTypes[47]  = "Detector";
        thingTypes[48]  = "Instrument";
        thingTypes[49]  = "Platform";
        thingTypes[50]  = "SensingPerspective";
    }

    public String querySelector(String tabName){
        String q = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        switch (tabName){
            case "Organization":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* http://www.w3.org/ns/prov#Organization . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Person":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* http://www.w3.org/ns/prov#Person . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Detector":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* vstoi:Detector . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "SensingPerspective":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* vstoi:SensingPerspective . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Platform":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* vstoi:Platform . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Deployments":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* vstoi:Deployment . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Instrument":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* vstoi:Instrument . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "PregnancySample":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:PregnancySample . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Cell":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* sio:Cell . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Tissue":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* sio:Tissue . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Process":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* sio:Process . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Entity":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* sio:Entity . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Object":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* sio:Object . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Attribute":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* sio:Attribute . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "EnvironmentalExposure":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:EnvironmentalExposure . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "BirthOutcome":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:BirthOutcome . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "OralHealth":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:OralHealth . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "ParentalHealthandFamilyHistory":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:ParentalHealthAndFamilyHistory . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "TargetedAnalyte":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:TargetedAnalyte . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "ReproductiveHealth":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:ReproductiveHealth . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "PrescriptionMedicationandDietarySupplements":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:PrescriptionMedication . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "NeighborhoodCharacteristic":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:NeighborhoodCharacteristic . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "PregnancyCharacteristic":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:PregnancyCharacteristic . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "AlcoholTobaccoandIllicitDrugUse":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:ATIDU . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "DeliveryCharacteristics":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:DeliveryCharacteristic . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "DietandNutrition":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:DietAndNutrition . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "SocioeconomicClassStatus":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:SocioeconomicStatus . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "HousingCharacteristic":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:HousingCharacteristic . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "PersonalProductUse":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:PersonalProductUse . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "MedicalHistory":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:MedicalHistory . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Anthropometry":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:Anthropometry . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "SleepCharacterisitic":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:SleepCharacterisitic . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Acculturation":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:Acculturation . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "MentalHealth":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:MentalHealth . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "PhysicalandMentalAssessment":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:Assessment . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Demographic":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:Demographic . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "HospitalUtilizationandAccesstoCare":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:HospitalUtilization . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "PhysicalActivityandFitness":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:PhysicalActivityAndFitness . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Element":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:Element . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "PolybrominatedDiphenylEther":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:PolybrominatedDiphenylEther . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "ArsenicSpecies":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:ArsenicSpecies . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Paraben":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:Paraben . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "TobaccoMetabolite":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:TobaccoMetabolite . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Phthalate":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:Phthalate . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "MercurySpecies":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:MercurySpecies . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "EnvironmentalPhenol":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:Phenol . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "VolatileOrganicCompound":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:OrganicAromaticCompound . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "Creatinine":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chebi:16737 . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "PFC":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:Perfluorocarbon . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "AlkylPhosphatePesticideMetabolite":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* chear:AlkylPhosphatePesticideMetabolite . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            case "DataAcquisitionSchemaAttribute":
               q= "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#> PREFIX sio: <http://semanticscience.org/resource/> PREFIX chear: <http://hadatac.org/ont/chear#> PREFIX prov: <http://www.w3.org/ns/prov#> PREFIX hasco: <http://hadatac.org/ont/hasco/> PREFIX hasneto: <http://hadatac.org/ont/hasneto#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX foaf:<http://xmlns.com/foaf/0.1/> PREFIX vstoi: <http://hadatac.org/ont/vstoi#>" + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* hasneto:DASchemaAttribute . " + 
                   "  ?id rdfs:subClassOf ?superId . " + 
                   "  ?id rdfs:label ?label ." + 
                   "  OPTIONAL {?id dcterms:identifier ?iden} . " + 
                   "  OPTIONAL {?id rdfs:comment ?comment} . " + 
                   "  OPTIONAL {?id skos:definition ?def} . " + 
                   "  OPTIONAL {?id hasneto:hasUnit ?unit} . " + 
                   "  OPTIONAL {?id skos:editorialNote ?note} . " + 
                   "  OPTIONAL {?id prov:wasAttributedTo ?attrTo} . " + 
                   "  OPTIONAL {?id prov:wasAssociatedWith ?assocWith} . " + 
                   "} ";
                break;
            default :
                q = "";
                System.out.println("WARNING: no query for tab " + tabName);
        }
    return q; 
    }

    public String executeQuery(String tab) throws IllegalStateException, IOException{
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(list_of_queries.get(tab).toString().replace(" ", "%20"));
            System.out.println(tab + " : " + list_of_queries.get(tab));
            request.setHeader("Accept", "application/sparql-results+json");
            HttpResponse response = client.execute(request);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
            System.out.println("Response: " + response);
            return writer.toString();
        } finally {}
    }
}