package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.labkey.remoteapi.CommandException;

import com.typesafe.config.ConfigFactory;

public class Indicator extends HADatAcThing implements Comparable<Indicator> {

    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";

    private String uri;
    private String label;
    private String comment;
    private String superUri;

    static String className = "hasco:Indicator";

    public Indicator() {
        setUri("");
        setSuperUri("hasco:Indicator");
        setLabel("");
        setComment("");
    }

    public Indicator(String uri) {
        setUri(uri);
        setSuperUri("hasco:Indicator");
        setLabel("");
        setComment("");
    }

    public Indicator(String uri, String label, String comment) {
        setUri(uri);
        setSuperUri("hasco:Indicator");
        setLabel(label);
        setComment(comment);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSuperUri() {
        return superUri;
    }
    public void setSuperUri(String superUri) {
        this.superUri = superUri;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if((o instanceof Indicator) && (((Indicator)o).getUri().equals(this.getUri()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }

    public static List<Indicator> find() {
        List<Indicator> indicators = new ArrayList<Indicator>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf hasco:Indicator . " + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Indicator indicator = find(soln.getResource("uri").getURI());
            indicators.add(indicator);
        }			

        java.util.Collections.sort((List<Indicator>) indicators);
        return indicators;		
    }

    public static Indicator find(String uri) {
        Indicator indicator = null;
        Model model;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
        model = qexec.execDescribe();

        indicator = new Indicator();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                indicator.setLabel(object.asLiteral().getString());
            }
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                indicator.setComment(object.asLiteral().getString());
            }
        }

        indicator.setUri(uri);

        return indicator;
    }

    public static List<Indicator> findRecursive() {
        List<Indicator> indicators = new ArrayList<Indicator>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf hasco:Indicator+ . " + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Indicator indicator = find(soln.getResource("uri").getURI());
            indicators.add(indicator);
        }			

        java.util.Collections.sort((List<Indicator>) indicators);
        return indicators;		
    }

    public static List<Indicator> findStudyIndicators() {
        List<Indicator> indicators = new ArrayList<Indicator>();
        String query = NameSpaces.getInstance().printSparqlNameSpaceList() 
                + " SELECT DISTINCT ?indicator ?indicatorLabel ?indicatorComment WHERE { "
                + " ?subTypeUri rdfs:subClassOf* hasco:Study . "
                + " ?studyUri a ?subTypeUri . "
                + " ?dataAcq hasco:isDataAcquisitionOf ?studyUri ."
                + " ?dataAcq hasco:hasSchema ?schemaUri ."
                + " ?schemaAttribute hasco:partOfSchema ?schemaUri . "
                + " ?schemaAttribute hasco:hasAttribute ?attribute . "
                + " {  { ?indicator rdfs:subClassOf hasco:StudyIndicator } UNION { ?indicator rdfs:subClassOf hasco:SampleIndicator } } . "
                + " ?indicator rdfs:label ?indicatorLabel . " 
                + " OPTIONAL { ?indicator rdfs:comment ?indicatorComment } . "
                + " ?attribute rdfs:subClassOf+ ?indicator . " 
                + " ?attribute rdfs:label ?attributeLabel . "
                + " }";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
        
        Indicator indicator = null;
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            indicator = new Indicator();
            indicator.setUri(soln.getResource("indicator").getURI());
            indicator.setLabel(soln.get("indicatorLabel").toString());
            if(soln.contains("indicatorComment")){
                indicator.setComment(soln.get("indicatorComment").toString());
            }
            indicators.add(indicator);
        }
        
        java.util.Collections.sort(indicators);
        return indicators; 
    }

    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {

        String valueConstraint = "";
        if (!facet.getFacetValuesByField("indicator_uri_str").isEmpty()) {
            valueConstraint += " VALUES ?studyIndicator { " + stringify(
                    facet.getFacetValuesByField("indicator_uri_str"), true) + " } \n ";
        }

        if (!facet.getFacetValuesByField("characteristic_uri_str_multi").isEmpty()) {
            valueConstraint += " VALUES ?attributeUri { " + stringify(
                    facet.getFacetValuesByField("characteristic_uri_str_multi"), true) + " } \n ";
        }

        if (!facet.getFacetValuesByField("dasa_uri_str").isEmpty()) {
            valueConstraint += " VALUES ?schemaAttribute { " + stringify(
                    facet.getFacetValuesByField("dasa_uri_str"), true) + " } \n ";
        }

        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?studyIndicator ?indicatorLabel ?dataAcq ?schemaAttribute ?attributeUri ?attributeLabel WHERE { \n"
                + valueConstraint + " \n"
                + "?subTypeUri rdfs:subClassOf* hasco:Study . \n"
                + "?studyUri a ?subTypeUri . \n"
                + "?dataAcq hasco:isDataAcquisitionOf ?studyUri . \n"
                + "?dataAcq hasco:hasSchema ?schemaUri . \n"
                + "?schemaAttribute hasco:partOfSchema ?schemaUri . \n"
                + "?schemaAttribute hasco:hasAttribute ?attributeUri . \n" 
                + "?attributeUri rdfs:subClassOf* ?studyIndicator . \n"
                + "?attributeUri rdfs:label ?attributeLabel . \n"
                + "?studyIndicator rdfs:subClassOf hasco:StudyIndicator . \n"
                //+ " { { ?studyIndicator rdfs:subClassOf hasco:StudyIndicator } UNION { ?studyIndicator rdfs:subClassOf hasco:ScienceIndicator } } . \n"
                + "?studyIndicator rdfs:label ?indicatorLabel . \n"
                + "}";

        System.out.println("Indicator query: " + query);

        Map<HADatAcThing, List<HADatAcThing>> mapIndicatorToCharList = new HashMap<HADatAcThing, List<HADatAcThing>>();
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
            
            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                Indicator indicator = new Indicator();
                indicator.setUri(soln.get("studyIndicator").toString());
                indicator.setLabel(WordUtils.capitalize(soln.get("indicatorLabel").toString()));
                indicator.setField("indicator_uri_str");

                AttributeInstance attrib = new AttributeInstance();
                attrib.setUri(soln.get("attributeUri").toString());
                attrib.setLabel(WordUtils.capitalize(soln.get("attributeLabel").toString()));
                attrib.setField("characteristic_uri_str");

                if (!mapIndicatorToCharList.containsKey(indicator)) {
                    List<HADatAcThing> attributes = new ArrayList<HADatAcThing>();
                    mapIndicatorToCharList.put(indicator, attributes);
                }
                if (!mapIndicatorToCharList.get(indicator).contains(attrib)) {
                    mapIndicatorToCharList.get(indicator).add(attrib);
                }

                Facet subFacet = facet.getChildById(indicator.getUri());
                subFacet.putFacet("indicator_uri_str", indicator.getUri());
                subFacet.putFacet("dasa_uri_str", soln.get("schemaAttribute").toString());
            }
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
        }

        return mapIndicatorToCharList;
    }

    @Override
    public int saveToLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        row.put("rdfs:subClassOf", "hasco:Indicator");
        row.put("rdfs:label", getLabel());
        row.put("rdfs:comment", getComment());
        rows.add(row);

        int totalChanged = 0;
        try {
            totalChanged = loader.insertRows("IndicatorType", rows);
        } catch (CommandException e) {
            try {
                totalChanged = loader.updateRows("IndicatorType", rows);
            } catch (CommandException e2) {
                System.out.println("[ERROR] Could not insert or update Indicator(s)");
            }
        }
        return totalChanged;
    }

    @Override
    public int deleteFromLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri().replace("<","").replace(">","")));
        rows.add(row);
        for (Map<String,Object> r : rows) {
            System.out.println("deleting Indicator " + r.get("hasURI"));
        }

        try {
            return loader.deleteRows("IndicatorType", rows);
        } catch (CommandException e) {
            System.out.println("[ERROR] Could not delete Indicator(s)");
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int compareTo(Indicator another) {
        return this.getLabel().compareTo(another.getLabel());
    }

    @Override
    public boolean saveToTripleStore() {
        if (uri == null || uri.equals("")) {
            System.out.println("[ERROR] Trying to save Indicator without assigning a URI");
            return false;
        }

        deleteFromTripleStore();

        String insert = "";
        String ind_uri = "";

        System.out.println("Indicator.save(): Checking URI");
        if (this.getUri().startsWith("<")) {
            ind_uri = this.getUri();
        } else {
            ind_uri = "<" + this.getUri() + ">";
        }
        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;
        
        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }
        
        if (label != null && !label.equals("")) {
            insert += ind_uri + " rdfs:label \"" + label + "\" .  ";
        }
        if (comment != null && !comment.equals("")) {
            insert += ind_uri + " rdfs:comment \"" + comment + "\" .  ";
        }
        if (superUri != null && !superUri.equals("")) {
            insert += ind_uri + " rdfs:subClassOf <" + DynamicFunctions.replacePrefixWithURL(superUri) + "> .  ";
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
        String query = "";
        if (this.getUri() == null || this.getUri().equals("")) {
            return;
        }
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += DELETE_LINE1;
        if (this.getUri().startsWith("http")) {
            query += "<" + this.getUri() + ">";
        } else {
            query += this.getUri();
        }
        query += DELETE_LINE3;
        query += LINE_LAST;
        UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
        processor.execute();
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
