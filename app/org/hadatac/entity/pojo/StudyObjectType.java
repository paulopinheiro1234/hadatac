package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.text.WordUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;


public class StudyObjectType extends HADatAcClass implements Comparable<StudyObjectType> {

    static String className = "sio:StudyObject";

    public StudyObjectType () {
        super(className);
    }

    public static List<StudyObjectType> find() {
        List<StudyObjectType> objectTypes = new ArrayList<StudyObjectType>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf* " + className + " . " + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            StudyObjectType objectType = find(soln.getResource("uri").getURI());
            objectTypes.add(objectType);
        }			

        java.util.Collections.sort((List<StudyObjectType>) objectTypes);
        
        return objectTypes;
    }

    public static Map<String,String> getMap() {
        List<StudyObjectType> list = find();
        Map<String,String> map = new HashMap<String,String>();
        for (StudyObjectType typ: list) 
            map.put(typ.getUri(),typ.getLabel());
        return map;
    }

    public static StudyObjectType find(String uri) {
        StudyObjectType objectType = null;
        Model model;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
        model = qexec.execDescribe();

        objectType = new StudyObjectType();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                objectType.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
                objectType.setSuperUri(object.asResource().getURI());
            }
        }

        objectType.setUri(uri);
        objectType.setLocalName(uri.substring(uri.indexOf('#') + 1));

        return objectType;
    }
    
    @Override
    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromTripleStore(facet, facetHandler);
    }
    
    @Override
    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacetsFromTripleStore(
            Facet facet, FacetHandler facetHandler) {

        String valueConstraint = "";
        if (!facet.getFacetValuesByField("object_collection_type_str").isEmpty()) {
            valueConstraint += " VALUES ?objectCollectionType { " + stringify(
                    facet.getFacetValuesByField("object_collection_type_str"), true) + " } \n ";
        }
        if (!facet.getFacetValuesByField("study_object_type_uri_str").isEmpty()) {
            valueConstraint += " VALUES ?studyObjType { " + stringify(
                    facet.getFacetValuesByField("study_object_type_uri_str"), true) + " } \n ";
        }

        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?studyObj ?studyObjType ?role ?studyObjTypeLabel WHERE { \n"
                + valueConstraint + " \n"
                + "?studyObj rdf:type ?studyObjType . \n"
                + "?studyObj hasco:isMemberOf ?objectCollection . \n"
                + "?objectCollection rdf:type ?objectCollectionType . \n"
                + "?objectCollection hasco:hasRoleLabel ?role . \n"
                + "?studyObjType rdfs:label ?studyObjTypeLabel . \n"
                + "}";

        System.out.println("StudyObjectType query: \n" + query);

        Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
        try {            
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                StudyObjectType studyObjectType = new StudyObjectType();
                studyObjectType.setUri(soln.get("studyObjType").toString());
                studyObjectType.setLabel(WordUtils.capitalize(soln.get("studyObjTypeLabel").toString()));
                studyObjectType.setField("study_object_type_uri_str");

                StudyObjectRole role = new StudyObjectRole();
                role.setUri(soln.get("role").toString());
                role.setLabel(WordUtils.capitalize(soln.get("role").toString()));
                role.setField("role_str");

                if (!results.containsKey(studyObjectType)) {
                    System.out.println("studyObjectType: " + studyObjectType.getLabel());
                    results.put(studyObjectType, new ArrayList<HADatAcThing>());
                }
                if (!results.get(studyObjectType).contains(role)) {
                    results.get(studyObjectType).add(role);
                }

                Facet subFacet = facet.getChildById(studyObjectType.getUri());
                subFacet.putFacet("study_object_uri_str", soln.get("studyObj").toString());
            }
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
        }

        return results;
    }
    
    @Override
    public boolean equals(Object o) {
        if((o instanceof StudyObjectType) && (((StudyObjectType)o).getUri().equals(this.getUri()))) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return getUri().hashCode();
    }

    @Override
    public int compareTo(StudyObjectType another) {
        if (this.getLabel() != null && another.getLabel() != null) {
            return this.getLabel().compareTo(another.getLabel());
        }
        return this.getLocalName().compareTo(another.getLocalName());
    }
}
