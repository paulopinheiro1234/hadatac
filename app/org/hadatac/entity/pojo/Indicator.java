package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.WordUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
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
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

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

    private static List<Indicator> findImmediateSubclasses(String indicatorUri, boolean justSub) {
        List<Indicator> indicators = new ArrayList<Indicator>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf " + indicatorUri + " . " + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Indicator indicator = find(soln.getResource("uri").getURI());
            indicator.setSuperUri(indicatorUri);
            if (!indicatorUri.equals(className) || (indicatorUri.equals(className) && !justSub)) {
            	indicators.add(indicator);
            } 
            if (indicatorUri.equals(className)) {
            	indicators.addAll(Indicator.findImmediateSubclasses(URIUtils.replaceNameSpace(indicator.getUri()), justSub));
            }
        }			

        java.util.Collections.sort((List<Indicator>) indicators);
        return indicators;		
    }

    public static List<Indicator> find() {
    	return Indicator.findImmediateSubclasses(className, false);
    }

    public static List<Indicator> findSubClasses() {
    	return Indicator.findImmediateSubclasses(className, true);
    }

    public static Indicator find(String uri) {
        Indicator indicator = null;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

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

    /* 
     * Argument can be proper attribute or inRelationTo-Attribute
     */    
    public static List<Indicator> findForAttribute(String attributeUri) {
        List<Indicator> indicators = new ArrayList<Indicator>();
        if (attributeUri == null || attributeUri.isEmpty()) {
        	return indicators;
        }
        if (attributeUri.startsWith("http")) {
        	attributeUri = "<" + attributeUri + ">";
        }
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?super1 WHERE { " +
                "     " + attributeUri + " rdfs:subClassOf* ?super . " +
                "     { ?super1 rdfs:subClassOf <http://hadatac.org/ont/hasco/StudyIndicator> } UNION { ?super1 rdfs:subClassOf <http://hadatac.org/ont/hasco/SampleIndicator>} . " +
                "     ?super rdfs:subClassOf ?super1 . " +
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("super1") != null) {
            	Indicator indicator = find(soln.getResource("super1").getURI());
            	indicators.add(indicator);
            } else {
            	System.out.println("[WARNING] Retrieved null result from SOLR");
            }
        }			

        java.util.Collections.sort((List<Indicator>) indicators);
        return indicators;		
    }

    public static Map<String, Map<String,String>> getValuesAndLabels(Map<String, String> indicatorMap) {
        Map<String, Map<String,String>> indicatorValueMap = new HashMap<String, Map<String,String>>();
        Map<String,String> values = new HashMap<String, String>();
        String indicatorValue = "";
        String indicatorValueLabel = "";
        for (Map.Entry<String, String> entry : indicatorMap.entrySet()) {
            values = new HashMap<String, String>();
            String indicatorType = entry.getKey().toString();
            String indvIndicatorQuery = 
            		NameSpaces.getInstance().printSparqlNameSpaceList() +
            		" SELECT DISTINCT ?indicator " +
                    " (MIN(?label_) AS ?label)" +
                    " WHERE { ?indicator rdfs:subClassOf " + indicatorType + " . " +
                    "   ?indicator rdfs:label ?label_ . " + 
                    " } GROUP BY ?indicator ?label_"; 
            try {				
                ResultSetRewindable resultsrwIndvInd = SPARQLUtils.select(
                        CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), indvIndicatorQuery);

                while (resultsrwIndvInd.hasNext()) {
                    QuerySolution soln = resultsrwIndvInd.next();
                    indicatorValueLabel = "";
                    if (soln.contains("label")){
                        indicatorValueLabel = soln.get("label").toString();
                    }
                    else {
                        //System.out.println("getIndicatorValues() No Label: " + soln.toString() + "\n");
                    }
                    if (soln.contains("indicator")){
                        indicatorValue = URIUtils.replaceNameSpaceEx(soln.get("indicator").toString());
                        values.put(indicatorValue,indicatorValueLabel);
                    }
                }
                indicatorValueMap.put(indicatorType,values);
            } catch (QueryExceptionHTTP e) {
                e.printStackTrace();
            }
        }
        return indicatorValueMap;
    }

    public static Map<String, List<String>> getValuesJustLabels(Map<String, String> indicatorMap){

        Map<String, List<String>> indicatorValueMap = new HashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        String indicatorValueLabel = "";
        for(Map.Entry<String, String> entry : indicatorMap.entrySet()){
            values = new ArrayList<String>();
            String indicatorType = entry.getKey().toString();
            String indvIndicatorQuery = 
            		NameSpaces.getInstance().printSparqlNameSpaceList() 
                    + " SELECT DISTINCT ?indicator (MIN(?label_) AS ?label) WHERE { "
                    + " ?indicator rdfs:subClassOf " + indicatorType + " . "
                    + " ?indicator rdfs:label ?label_ . "
                    + " } "
                    + " GROUP BY ?indicator ?label_";
            try {				
                ResultSetRewindable resultsrwIndvInd = SPARQLUtils.select(
                        CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), indvIndicatorQuery);

                while (resultsrwIndvInd.hasNext()) {
                    QuerySolution soln = resultsrwIndvInd.next();
                    indicatorValueLabel = "";
                    if (soln.contains("label")){
                        indicatorValueLabel = soln.get("label").toString();
                    }
                    else {
                        //System.out.println("getIndicatorValues() No Label: " + soln.toString() + "\n");
                    }
                    if (soln.contains("indicator")){
                        values.add(indicatorValueLabel);
                    }
                }
                String indicatorTypeLabel = entry.getValue().toString();
                indicatorValueMap.put(indicatorTypeLabel,values);
            } catch (QueryExceptionHTTP e) {
                e.printStackTrace();
            }
        }
        return indicatorValueMap;
    }

    public static Map<String, List<String>> getValues(Map<String, String> indicatorMap) {
        Map<String, List<String>> indicatorValueMap = new HashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        String indicatorValueLabel = "";
        for(Map.Entry<String, String> entry : indicatorMap.entrySet()){
            values = new ArrayList<String>();
            String indicatorType = entry.getKey().toString();
            //System.out.println("Indicator (inside getValues()): indicatorType is [" + indicatorType + "]");
            String indvIndicatorQuery = NameSpaces.getInstance().printSparqlNameSpaceList() + 
            		" SELECT DISTINCT ?indicator " +
                    "(MIN(?label_) AS ?label)" +
                    "WHERE { ?indicator rdfs:subClassOf " + indicatorType + " . " +
                    "?indicator rdfs:label ?label_ . " + 
                    "} GROUP BY ?indicator ?label";
            try {				
                ResultSetRewindable resultsrwIndvInd = SPARQLUtils.select(
                        CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), indvIndicatorQuery);

                while (resultsrwIndvInd.hasNext()) {
                    QuerySolution soln = resultsrwIndvInd.next();
                    if (soln.contains("label")){
                        indicatorValueLabel = URIUtils.replaceNameSpaceEx(soln.get("indicator").toString());
                        values.add(indicatorValueLabel);
                        //System.out.println("Indicator (inside getValues()): indicator value label is [" + indicatorValueLabel + "]");
                    }
                    else {
                        //System.out.println("getIndicatorValues() No Label: " + soln.toString() + "\n");
                    }
                }
                indicatorValueMap.put(indicatorType,values);
            } catch (QueryExceptionHTTP e) {
                e.printStackTrace();
            }
        }
        return indicatorValueMap;
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
                + " ?schemaAttribute ?x ?attribute . "
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
    
    public Map<Facetable, List<Facetable>> getTargetFacetsForInRelationTo(Facet facet, FacetHandler facetHandler) {
        Map<Facetable, List<Facetable>> irtList = new HashMap<Facetable, List<Facetable>>();
        Map<Facetable, List<Facetable>> mapIndicatorToIrtList = new HashMap<Facetable, List<Facetable>>();
        
        InRelationToInstance irt = new InRelationToInstance();
    	String listIrt = "";
        irtList = irt.getTargetFacetsFromSolr(facet, facetHandler);
        for (Facetable obj : irtList.keySet()) {
        	//System.out.println("New Indicator's TARGET FACETS: " + obj.getField() + "  [" + obj.getUri() + "]  Count: [" + obj.getCount() + "]");
        	listIrt = listIrt + "<" + obj.getUri() + "> ";
        }
        
        String query = NameSpaces.getInstance().printSparqlNameSpaceList() +
            	" SELECT DISTINCT ?indicator ?indicatorLabel ?inRelationToUri ?inRelationToLabel WHERE { " +
            	"   VALUES ?inRelationToUri { " + listIrt + " } " +
            	"   ?inRelationToUri rdfs:subClassOf* ?indicator . " + 
            	"   { ?indicator rdfs:subClassOf hasco:SampleIndicator } UNION { ?indicator rdfs:subClassOf hasco:StudyIndicator } . " + 
            	"   ?indicator rdfs:label ?indicatorLabel . " +
            	"   ?inRelationToUri rdfs:label ?inRelationToLabel . " +
            	" }";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            //System.out.println("Indicator.getValuesForInRelationTo --   Uri: " + soln.getResource("indicator").getURI() + 
            //                   "  label: " + soln.get("indicatorLabel").toString() +             
            //                   "  inRelationToUri: " + soln.getResource("inRelationToUri").toString());
            String indUri = soln.getResource("indicator").getURI();
            String irtUri = soln.getResource("inRelationToUri").toString();

            Indicator indicator = new Indicator();
            indicator.setUri(indUri);
            //System.out.println("Indicator.getTargetFacets(): identified indicator [" + indicator.getUri() + "]  label: [" + indicator.getLabel() + "]");
            indicator.setLabel(soln.get("indicatorLabel").toString());
            indicator.setField("indicator_uri_str");
            indicator.setQuery(query);

            //System.out.println("Indicator.getTargetFacets(): identified attribute/inrelationto [" + soln.get("attributeUri").toString() + "]  label: [" + WordUtils.capitalize(soln.get("attributeLabel").toString()) + "]");
            InRelationToInstance irtInst = new InRelationToInstance();
            irtInst.setUri(irtUri);
            irtInst.setLabel(soln.get("inRelationToLabel").toString());
            irtInst.setField("in_relation_to_uri_str");

            for (Facetable obj : irtList.keySet()) {
            	if (irtUri.equals(obj.getUri())) {
            		irtInst.setCount(obj.getCount());
            		int updatedCount = indicator.getCount() + obj.getCount();
            		indicator.setCount(updatedCount);
            	}
            }

            if (!mapIndicatorToIrtList.containsKey(indicator)) {
                List<Facetable> irts = new ArrayList<Facetable>();
                mapIndicatorToIrtList.put(indicator, irts);
            }
            if (!mapIndicatorToIrtList.get(indicator).contains(irtInst)) {
                mapIndicatorToIrtList.get(indicator).add(irtInst);
            } 

            Facet subFacet = facet.getChildById(indicator.getUri());
            subFacet.putFacet("indicator_uri_str", indicator.getUri());
            subFacet.putFacet("in_relation_to_uri_str", irtInst.getUri());
        }
        return mapIndicatorToIrtList;
	}

    /*
    public static Pivot getValuesForInRelationTo(Pivot pivot) {

    	Pivot newPivot = new Pivot();
        newPivot.setField(pivot.getField());
        newPivot.setValue(pivot.getValue());
        newPivot.setTooltip(pivot.getTooltip());
        newPivot.setQuery(pivot.getQuery());
        newPivot.setCount(pivot.getCount());

    	String listIrt = "";
    	//System.out.println("Indicator.getValuesForInRelationTo - pivotEC2_0: field: " + pivot.getField() + "  value: " + pivot.getValue() + "   count: " + pivot.getCount());
        for (Pivot pivot1 : pivot.children) {
        	//System.out.println("Indicator.getValuesForInRelationTo - pivotEC2_1: field: " + pivot1.getField() + "  value: " + pivot1.getValue() + "   count: " + pivot1.getCount());
        	listIrt = listIrt + "<" + pivot1.getValue() + "> ";
        }
    	
        String query = NameSpaces.getInstance().printSparqlNameSpaceList() +
        	" SELECT DISTINCT ?indicator ?indicatorLabel ?inRelationToUri WHERE { " +
        	"   VALUES ?inRelationToUri { " + listIrt + " } " +
        	"   ?inRelationToUri rdfs:subClassOf* ?indicator . " + 
        	"   { ?indicator rdfs:subClassOf hasco:SampleIndicator } UNION { ?indicator rdfs:subClassOf hasco:StudyIndicator } . " + 
        	"   ?indicator rdfs:label ?indicatorLabel . " +
        	" }";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            //System.out.println("Indicator.getValuesForInRelationTo --   Uri: " + soln.getResource("indicator").getURI() + 
            //                   "  label: " + soln.get("indicatorLabel").toString() +             
            //                   "  inRelationToUri: " + soln.getResource("inRelationToUri").toString());
            String indUri = soln.getResource("indicator").getURI();
            String irtUri = soln.getResource("inRelationToUri").toString();
            if (irtUri != null && !irtUri.isEmpty()) {
	            Pivot indicatorPivot = null;
	            for (Pivot auxPivot : newPivot.children) {
	            	if (auxPivot.getTooltip().equals(indUri)) {
	            		indicatorPivot = auxPivot;
	            		break;
	            	}
	            }
	            if (indicatorPivot == null) {
	            	indicatorPivot = new Pivot();
	            	indicatorPivot.setField("indicator_uri_str");
	            	//indicatorPivot.setValue(soln.getResource("indicator").getURI());
	            	indicatorPivot.setValue(soln.get("indicatorLabel").toString());
	            	indicatorPivot.setTooltip(soln.getResource("indicator").getURI());
	            	indicatorPivot.setQuery("");
	            	indicatorPivot.setCount(0);
	            	newPivot.addChild(indicatorPivot);
                    //System.out.println("Indicator.getValuesForInRelationTo --  created indicator pivot for  " + indicatorPivot.getTooltip()); 
	            }
	            for (Pivot irtPivot : pivot.children) {
	            	if (irtPivot != null && irtPivot.getValue() != null) {
		            	if (irtPivot.getValue().equals(irtUri)) {
		            		indicatorPivot.addChild(irtPivot);
		                    //System.out.println("Indicator.getValuesForInRelationTo --  added " + irtPivot.getValue() + " to " + indicatorPivot.getTooltip()); 
		            		break;
		            	}
	            	}
	            }
            }
        }

    	return newPivot;
    }
    */
    
    @Override
    public Map<Facetable, List<Facetable>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromTripleStore(facet, facetHandler);
    }

    @Override
    public Map<Facetable, List<Facetable>> getTargetFacetsFromTripleStore(
            Facet facet, FacetHandler facetHandler) {

    	if (facet.getFacetName().equals("facetsEC2")) {
    		return this.getTargetFacetsForInRelationTo(facet, facetHandler);
    	}
    	
        String valueConstraint = "";
        if (!facet.getFacetValuesByField("indicator_uri_str").isEmpty()) {
            valueConstraint += " VALUES ?indicator { " + stringify(
                    facet.getFacetValuesByField("indicator_uri_str")) + " } \n ";
        }

        if (!facet.getFacetValuesByField("characteristic_uri_str_multi").isEmpty()) {
            valueConstraint += " VALUES ?attributeUri { " + stringify(
                    facet.getFacetValuesByField("characteristic_uri_str_multi")) + " } \n ";
        }

        if (!facet.getFacetValuesByField("dasa_uri_str").isEmpty()) {
            valueConstraint += " VALUES ?schemaAttribute { " + stringify(
                    facet.getFacetValuesByField("dasa_uri_str")) + " } \n ";
        }

        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?indicator ?dataAcq ?schemaAttribute ?attributeUri ?attributeLabel WHERE { \n"
                + valueConstraint + " \n"
                + "?subTypeUri rdfs:subClassOf* hasco:Study . \n"
                + "?studyUri a ?subTypeUri . \n"
                + "?dataAcq hasco:isDataAcquisitionOf ?studyUri . \n"
                + "?dataAcq hasco:hasSchema ?schemaUri . \n"
                + "?schemaAttribute hasco:partOfSchema ?schemaUri . \n"
                + "?schemaAttribute ?x ?attributeUri . \n" 
                + "?attributeUri rdfs:subClassOf* ?indicator . \n"
                + "?attributeUri rdfs:label ?attributeLabel . \n"
                + " { ?indicator rdfs:subClassOf hasco:SampleIndicator } UNION { ?indicator rdfs:subClassOf hasco:StudyIndicator } . \n"
                + "}";

        // System.out.println("Indicator query: \n" + query);
        
        Map<Facetable, List<Facetable>> mapIndicatorToCharList = new HashMap<Facetable, List<Facetable>>();
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                Indicator indicator = new Indicator();
                indicator.setUri(soln.get("indicator").toString());
                //System.out.println("Indicator.getTargetFacets(): identified indicator [" + indicator.getUri() + "]  label: [" + indicator.getLabel() + "]");
                indicator.setLabel(WordUtils.capitalize(HADatAcThing.getShortestLabel(soln.get("indicator").toString())));
                indicator.setField("indicator_uri_str");
                indicator.setQuery(query);

                //System.out.println("Indicator.getTargetFacets(): identified attribute/inrelationto [" + soln.get("attributeUri").toString() + "]  label: [" + WordUtils.capitalize(soln.get("attributeLabel").toString()) + "]");
                AttributeInstance attrib = new AttributeInstance();
                attrib.setUri(soln.get("attributeUri").toString());
                attrib.setLabel(WordUtils.capitalize(soln.get("attributeLabel").toString()));
                attrib.setField("characteristic_uri_str");

                if (!mapIndicatorToCharList.containsKey(indicator)) {
                    List<Facetable> attributes = new ArrayList<Facetable>();
                    mapIndicatorToCharList.put(indicator, attributes);
                }
                if (!mapIndicatorToCharList.get(indicator).contains(attrib)) {
                    mapIndicatorToCharList.get(indicator).add(attrib);
                    //System.out.println("ADDED ATTRIBUTE");
                } 
                /*
                else {
                    InRelationToInstance irt = new InRelationToInstance();
                    irt.setUri(soln.get("attributeUri").toString());
                    irt.setLabel(WordUtils.capitalize(soln.get("attributeLabel").toString()));
                    irt.setField("in_relation_to_uri_str");
                    if (!mapIndicatorToCharList.get(indicator).contains(irt)) {
                        mapIndicatorToCharList.get(indicator).add(irt);
                        System.out.println("ADDED INRELATIONTO");
                    }
                } */

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
    public int compareTo(Indicator another) {
    	String from = this.getSuperUri() + this.getLabel();
    	String to = another.getSuperUri() + another.getLabel();
        return from.compareTo(to);
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

        //System.out.println("Indicator.save(): Checking URI");
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
            System.out.println("[ERROR] QueryParseException due to update query: " + insert);
            throw e;
        }

        return true;
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
