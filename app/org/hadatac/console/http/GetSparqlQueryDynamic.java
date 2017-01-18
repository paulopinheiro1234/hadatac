package org.hadatac.console.http;
//This Java Class was Dynamically Generated
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.TreeMap;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory
;import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import play.Play;
public class GetSparqlQueryDynamic { 
    public StringBuffer sparql_query = new StringBuffer();
    public TreeMap<String, StringBuffer> list_of_queries = new TreeMap<String, StringBuffer>();
    public String collection;
    private int numThings = 15;
    public String[] thingTypes = new String[numThings];

    public GetSparqlQueryDynamic () {} 

    public GetSparqlQueryDynamic (SparqlQuery query) {
        this(Collections.METADATA_SPARQL, query);
    }

    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query) {
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

    public GetSparqlQueryDynamic (SparqlQuery query, String tabName) {
        this(Collections.METADATA_SPARQL, query, tabName);
    }

    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query, String tabName) {
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
        thingTypes[0]  = "BioEnvIndicator";
        thingTypes[1]  = "CognitionIndicator";
        thingTypes[2]  = "DemographicsIndicator";
        thingTypes[3]  = "GenericIndicator";
        thingTypes[4]  = "PhysicalEnvIndicator";
        thingTypes[5]  = "PhysiologyIndicator";
        thingTypes[6]  = "Attribute";
        thingTypes[7]  = "Entity";
        thingTypes[8]  = "Object";
        thingTypes[9]  = "Process";
        thingTypes[10]  = "Deployments";
        thingTypes[11]  = "Detector";
        thingTypes[12]  = "Instrument";
        thingTypes[13]  = "Platform";
        thingTypes[14]  = "SensingPerspective";
    }

    public String querySelector(String tabName){
        String q = "SELECT * WHERE { ?s ?p ?o } LIMIT 10";
        switch (tabName){
            case "Detector":
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
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
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
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
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
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
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
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
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
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
            case "Process":
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
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
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
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
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
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
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
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
            case "GenericIndicator":
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* case:GenericIndicator . " + 
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
            case "BioEnvIndicator":
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* case:BioEnvironmentIndicator . " + 
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
            case "PhysiologyIndicator":
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* case:PhysiologyIndicator . " + 
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
            case "PhysicalEnvIndicator":
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* case:PhysicalEnvironmentIndicator . " + 
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
            case "CognitionIndicator":
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* case:CognitionIndicator . " + 
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
            case "DemographicsIndicator":
               q= "PREFIX chebi: <http://purl.obolibrary.org/obo/CHEBI_>  PREFIX hadatac-sn: <http://hadatac.org/ont/hadatac-sn#>  PREFIX owl: <http://www.w3.org/2002/07/owl#>  PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  PREFIX case-kb: <http://hadatac.org/kb/case#>  PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  PREFIX chear-kb: <http://hadatac.org/kb/chear#>  PREFIX dct: <http://purl.org/dc/terms/>  PREFIX sio: <http://semanticscience.org/resource/>  PREFIX dcterms: <http://purl.org/dc/terms/>  PREFIX uo: <http://purl.obolibrary.org/obo/uo#>  PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  PREFIX prov: <http://www.w3.org/ns/prov#>  PREFIX hadatac: <http://hadatac.org/ont/hadatac#>  PREFIX case: <http://hadatac.org/ont/case#>  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  PREFIX hasco: <http://hadatac.org/ont/hasco/>  PREFIX uberon: <http://purl.obolibrary.org/obo/uberon.owl#>  PREFIX chear: <http://hadatac.org/ont/chear#>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX fma: <http://purl.org/sig/ont/fma/>  PREFIX obo: <http://geneontology.org/GO.format.obo-1_2.shtml#>  PREFIX pubchem: <http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>  " + 
                   "SELECT ?id ?superId ?label ?iden ?comment ?def ?unit ?note ?attrTo ?assocWith " + 
                   "WHERE { " + 
                   "  ?id rdfs:subClassOf* case:DemographicsIndicator . " + 
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + querySelector(tab);
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