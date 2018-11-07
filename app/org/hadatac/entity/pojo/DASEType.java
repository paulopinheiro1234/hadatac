package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.text.WordUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;


public class DASEType extends HADatAcThing implements Comparable<DASEType> {

    public DASEType () {}

    @Override
    public boolean equals(Object o) {
        if((o instanceof DASEType) && (((DASEType)o).getLabel().equals(this.getLabel()))) {
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
    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromTripleStore(facet, facetHandler);
    }

    @Override
    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacetsFromTripleStore(
            Facet facet, FacetHandler facetHandler) {
        
        String valueConstraint = "";
        if (!facet.getFacetValuesByField("dase_type_uri_str").isEmpty()) {
            valueConstraint += " VALUES ?daseTypeUri { " + stringify(
                    facet.getFacetValuesByField("dase_type_uri_str"), true) + " } \n ";
        }
        
        facet.clearFieldValues("dase_type_uri_str");
        
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?daseUri ?daseTypeUri ?daseLabel ?daseTypeLabel WHERE { \n"
                + valueConstraint + " \n"
                + " ?daseUri a hasco:DASchemaEvent . \n"
                + " { \n"
                + " ?daseUri a ?daseTypeUri . \n"
                + " } UNION { \n"
                + " ?daseUri hasco:hasEntity ?daseTypeUri . \n"
                + " } . \n"
                + " ?daseUri rdfs:label ?daseLabel . \n"
                + " ?daseTypeUri rdfs:label ?daseTypeLabel . \n"
                + "}";
        
        System.out.println("DASE query: " + query);
        
        Map<HADatAcThing, List<HADatAcThing>> mapTypeToInstanceList = new HashMap<HADatAcThing, List<HADatAcThing>>();
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                DASEType daseType = new DASEType();
                daseType.setUri(soln.get("daseTypeUri").toString());
                daseType.setLabel(WordUtils.capitalize(soln.get("daseTypeLabel").toString()));
                daseType.setField("dase_type_uri_str");
                
                TimeInstance timeInstance = new TimeInstance();
                timeInstance.setUri(soln.get("daseUri").toString());
                timeInstance.setLabel(WordUtils.capitalize(soln.get("daseLabel").toString()));
                timeInstance.setField("named_time_str");
                
                if (!mapTypeToInstanceList.containsKey(daseType)) {
                    List<HADatAcThing> timeInstances = new ArrayList<HADatAcThing>();
                    mapTypeToInstanceList.put(daseType, timeInstances);
                }
                if (!mapTypeToInstanceList.get(daseType).contains(timeInstance)) {
                    mapTypeToInstanceList.get(daseType).add(timeInstance);
                }
                
                Facet subFacet = facet.getChildById(daseType.getUri());
                DataAcquisitionSchemaEvent event = DataAcquisitionSchemaEvent.find(soln.get("daseUri").toString());
                if (event != null && event.getEntity().equals(soln.get("daseTypeUri").toString())) {
                    subFacet.putFacet("named_time_str", soln.get("daseTypeUri").toString());
                } else {
                    subFacet.putFacet("named_time_str", soln.get("daseUri").toString());
                }
            }
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
        }

        return mapTypeToInstanceList;
    }

    @Override
    public int compareTo(DASEType another) {
        if (getLabel() != null && another.getLabel() != null) {
            return getLabel().compareTo(another.getLabel());
        }
        return getUri().compareTo(another.getUri());
    }

    @Override
    public boolean saveToTripleStore() {
        return false;
    }

    @Override
    public void deleteFromTripleStore() {
    }

    @Override
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }

    @Override
    public int saveToLabKey(String userName, String password) {
        return 0;
    }

    @Override
    public int deleteFromLabKey(String userName, String password) {
        return 0;
    }
}
