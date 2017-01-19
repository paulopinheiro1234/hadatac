package org.hadatac.console.http;
import java.io.ByteArrayOutputStream;
//This Java Class was Dynamically Generated
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class GetSparqlQueryDynamic {
    public String collection;

    public GetSparqlQueryDynamic () {} 

    public GetSparqlQueryDynamic (SparqlQuery query) {
        this(Collections.METADATA_SPARQL, query);
    }

    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query) {
        this.collection = Collections.getCollectionsName(collectionSource);
        System.out.println("Collection: " + collection);
    }

    public GetSparqlQueryDynamic (SparqlQuery query, String tabName) {
        this(Collections.METADATA_SPARQL, query, tabName);
    }

    public GetSparqlQueryDynamic (String collectionSource, SparqlQuery query, String tabName) {
        this.collection = Collections.getCollectionsName(collectionSource);
        System.out.println("Collection: " + collection);
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
