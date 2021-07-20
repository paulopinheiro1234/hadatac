package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.annotations.PropertyField;
import org.hadatac.annotations.ReversedPropertyField;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;


public class VirtualColumn extends HADatAcClass implements Comparable<VirtualColumn> {

    static String className = "hasco:VirtualColumn";

    public List<VirtualColumn> virtualColumns;

    public static String INDENT1 = "   ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
    public static String LINE_LAST = "}  ";

    @ReversedPropertyField(uri="hasco:hasVirtualColumn")
    private String studyUri = "";
    
    @PropertyField(uri="hasco:hasGroundingLabel")
    private String hasGroundingLabel = "";
    
    @PropertyField(uri="hasco:hasSOCReference")
    private String hasSOCReference = "";

    public VirtualColumn(
            String studyUri,
            String hasGroundingLabel,
            String hasSOCReference) {
        super(className);
        String vcUri="";
        if(studyUri.contains("SSD")){
            vcUri = studyUri.replace("SSD", "VC") + "-" + hasSOCReference.replace("??", "");
        }
        if (studyUri.contains("STD")){
            vcUri = studyUri.replace("STD", "VC") + "-" + hasSOCReference.replace("??", "");
        }
        this.setUri(vcUri);
        this.setStudyUri(studyUri);
        this.setGroundingLabel(hasGroundingLabel);
        this.setSOCReference(hasSOCReference);
        virtualColumns = new ArrayList<VirtualColumn>();
    }

    private VirtualColumn() {
        super(className);
        virtualColumns = new ArrayList<VirtualColumn>();
    }

    @Override
    public String getUri() {
        return uri;
    }
    
    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getStudyUri() {
        return studyUri;
    }
    
    public void setStudyUri(String studyUri) {
        this.studyUri = studyUri;
    }
    
    public String getGroundingLabel() {
        return hasGroundingLabel;
    }
    
    public void setGroundingLabel(String hasGroundingLabel) {
        this.hasGroundingLabel = hasGroundingLabel;
    }
    
    public String getSOCReference() {
        return hasSOCReference;
    }
    
    public void setSOCReference(String hasSOCReference) {
        this.hasSOCReference = hasSOCReference;
    }
    
    public static List<VirtualColumn> find() {
        List<VirtualColumn> vcs = new ArrayList<VirtualColumn>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf* hasco:VisrtualColumn . " + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            VirtualColumn vc = find(soln.getResource("uri").getURI());
            vcs.add(vc);
            break;
        }			

        java.util.Collections.sort((List<VirtualColumn>) vcs);
        
        return vcs;
    }

    public static List<VirtualColumn> findByStudyUri(String studyUri) {
        if (studyUri == null) {
            return null;
        }
        List<VirtualColumn> vcList = new ArrayList<VirtualColumn>();

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri WHERE { \n" + 
                "   <" + studyUri + "> hasco:hasVirtualColumn ?uri . \n" +
                " } ";
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) { 
                //System.out.println("VirtualColumn: findByStudyUri() : " + soln.getResource("uri").getURI());
                VirtualColumn vc = VirtualColumn.find(soln.getResource("uri").getURI());
                vcList.add(vc);
            }
        }
        return vcList;
    }

    public static Map<String,String> getMap() {
        List<VirtualColumn> list = find();
        Map<String,String> map = new HashMap<String,String>();
        for (VirtualColumn vc: list) 
            map.put(vc.getUri(),vc.getLabel());
        return map;
    }
    
    public static List<String> getSubclasses(String uri) {
        List<String> subclasses = new ArrayList<String>();
        
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
                + " SELECT ?uri WHERE { \n"
                + " ?uri rdfs:subClassOf* <" + uri + "> . \n"
                + " } \n";

        //System.out.println("queryString: " + queryString);
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            subclasses.add(soln.get("uri").toString());
        }
        
        return subclasses;
    }

    public static VirtualColumn find(String studyUri, String SOCReference) {
        String vcUri="";
        if(studyUri.contains("SSD")){
            vcUri = studyUri.replace("SSD", "VC") + "-" + SOCReference.replace("??", "");
        }
        if (studyUri.contains("STD")){
            vcUri = studyUri.replace("STD", "VC") + "-" + SOCReference.replace("??", "");
        }
        return VirtualColumn.find(vcUri);
    }
    
    public static VirtualColumn find(String uri) {
        if ("".equals(uri.trim())) {
            return null;
        }
        
        //System.out.println("VirtualColumn find is called for uri " + uri);
        
        String queryString = "DESCRIBE <" + uri + ">";
        
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        StmtIterator stmtIterator = model.listStatements();

        if (!stmtIterator.hasNext()) {
            return null;
        }
        
        VirtualColumn vc = new VirtualColumn();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();
            RDFNode object = statement.getObject();
            RDFNode subject = statement.getSubject();
            if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("rdfs:label"))) {
                String label = object.asLiteral().getString();
                
                // prefer longer one
                if (label.length() > vc.getLabel().length()) {
                    vc.setLabel(label);
                }
            } else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("rdfs:subClassOf"))) {
                vc.setSuperUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("hasco:hasGroundingLabel"))) {
                vc.setGroundingLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("hasco:hasSOCReference"))) {
                vc.setSOCReference(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("hasco:hasVirtualColumn"))) {
                vc.setStudyUri(subject.asResource().getURI());
            }

        }
        
        vc.setUri(uri);
        vc.setLocalName(uri.substring(uri.indexOf('#') + 1));

        return vc;
    }

    @Override
    public int compareTo(VirtualColumn another) {
        if (this.getLabel() != null && another.getLabel() != null) {
            return this.getLabel().compareTo(another.getLabel());
        }
        return this.getLocalName().compareTo(another.getLocalName());
    }

    @Override
    public boolean saveToTripleStore() {
        String insert = "";

        String vc_uri = URIUtils.replacePrefix(this.getUri());
        if (!vc_uri.startsWith("<")) {
            vc_uri = "<" + vc_uri + ">";
        }

        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;

        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }
        if (className != null && !className.equals("")) {
            insert += vc_uri + " a <" + URIUtils.replacePrefixEx(className) + "> . ";
        }
        if (this.getLabel() != null && !this.getLabel().equals("")) {
            insert += vc_uri + " rdfs:label  \"" + this.getLabel() + "\" . ";
        }
        if (this.getStudyUri().startsWith("http")) {
            insert += " <" + this.getStudyUri() + "> hasco:hasVirtualColumn  " + vc_uri + " . ";
        } else {
            insert += " " + this.getStudyUri() + " hasco:hasVirtualColumn  " + vc_uri + " . ";
        }
        if (this.getComment() != null && !this.getComment().equals("")) {
            insert += vc_uri + " rdfs:comment  \"" + this.getComment() + "\" . ";
        }
        if (this.getGroundingLabel() != null && !this.getGroundingLabel().equals("")) {
            insert += vc_uri + " hasco:hasGroundingLabel  \"" + this.getGroundingLabel() + "\" . ";
        }
        if (this.getSOCReference() != null && !this.getSOCReference().equals("")) {
            insert += vc_uri + " hasco:hasSOCReference  \"" + this.getSOCReference() + "\" . ";
        }

        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }

        insert += LINE_LAST;

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
            processor.execute();
        } catch (QueryParseException e) {
            System.out.println("QueryParseException due to update query: " + insert);
            throw e;
        }

        return true;
    }
    
    @Override
    public void deleteFromTripleStore() {
        super.deleteFromTripleStore();
    }

    @Override
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }

}

