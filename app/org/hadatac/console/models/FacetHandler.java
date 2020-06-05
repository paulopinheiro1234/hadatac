package org.hadatac.console.models;

import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class FacetHandler {

    public Map<String, Facet> facetCatalog = new HashMap<String, Facet>();

    public static final String ENTITY_CHARACTERISTIC_FACET = "facetsEC";
    public static final String ENTITY_CHARACTERISTIC_FACET2 = "facetsEC2";
    public static final String STUDY_FACET = "facetsS";
    public static final String OBJECT_COLLECTION_FACET = "facetsOC";
    public static final String UNIT_FACET = "facetsU";
    public static final String TIME_FACET = "facetsT";
    public static final String PLATFORM_INSTRUMENT_FACET = "facetsPI";

    public FacetHandler() {
        facetCatalog.put(ENTITY_CHARACTERISTIC_FACET, new Facet());
        facetCatalog.put(ENTITY_CHARACTERISTIC_FACET2, new Facet());
        facetCatalog.put(STUDY_FACET, new Facet());
        facetCatalog.put(OBJECT_COLLECTION_FACET, new Facet());
        facetCatalog.put(UNIT_FACET, new Facet());
        facetCatalog.put(TIME_FACET, new Facet());
        facetCatalog.put(PLATFORM_INSTRUMENT_FACET, new Facet());
    }

    public Facet getFacetByName(String facetName) {
        if (facetCatalog.containsKey(facetName)) {
            return facetCatalog.get(facetName);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getValuesByFacetField(String facetName, String fieldName) {
        List<String> results = new ArrayList<String>();
        if (facetCatalog.containsKey(facetName)) {
            if (((Map<String, List<String>>)facetCatalog.get(facetName)).containsKey(fieldName)) {
                for (String value : ((Map<String, List<String>>)facetCatalog.get(facetName)).get(fieldName)) {
                    if (!results.contains(value)) {
                        results.add(value);
                    }
                }
            }
        }

        return results;
    }

    public String values(String facetName) {
        List<String> results = facetCatalog.get(facetName).values();
        return (new Gson()).toJson(results);
    }

    /**
     * To serialize this object into a JSON string
     * @return String A JSON string
     */
    public String toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String output =  mapper.writeValueAsString(this);
            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * To construct the facet catalog given a JSON string from
     * the front end
     * @param str A JSON string
     * @return Nothing
     */
    public void loadFacetsFromString(String str) {
        if (str == null || str.equals("")) {
            // Default facets
            str = "{\"facetsEC\":[],\"facetsEC2\":[],\"facetsS\":[],\"facetsOC\":[],\"facetsU\":[],\"facetsT\":[],\"facetsPI\":[]}";
        }

        try {
            JSONObject obj = (JSONObject)(new JSONParser().parse(str));
            Facet facet = Facet.loadFacet(obj.get(ENTITY_CHARACTERISTIC_FACET), ENTITY_CHARACTERISTIC_FACET);
            facetCatalog.put(ENTITY_CHARACTERISTIC_FACET, facet);

            facet = Facet.loadFacet(obj.get(ENTITY_CHARACTERISTIC_FACET2), ENTITY_CHARACTERISTIC_FACET2);
            facetCatalog.put(ENTITY_CHARACTERISTIC_FACET2, facet);

            facet = Facet.loadFacet(obj.get(STUDY_FACET), STUDY_FACET);
            facetCatalog.put(STUDY_FACET, facet);

            facet = Facet.loadFacet(obj.get(OBJECT_COLLECTION_FACET), OBJECT_COLLECTION_FACET);
            facetCatalog.put(OBJECT_COLLECTION_FACET, facet);

            facet = Facet.loadFacet(obj.get(UNIT_FACET), UNIT_FACET);
            facetCatalog.put(UNIT_FACET, facet);

            facet = Facet.loadFacet(obj.get(TIME_FACET), TIME_FACET);
            facetCatalog.put(TIME_FACET, facet);

            facet = Facet.loadFacet(obj.get(PLATFORM_INSTRUMENT_FACET), PLATFORM_INSTRUMENT_FACET);
            facetCatalog.put(PLATFORM_INSTRUMENT_FACET, facet);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return;
    }

    /**
     * To transform the constraints hold by a given facet object
     * into an executable Solr query for getting its corresponding
     * data point statistics.
     * @param facet The facet to be transformed
     * @return String An executable Solr query
     */
    public String getTempSolrQuery(Facet facet) {
        //System.out.println("getTempSolrQuery() facet.getFacetName(): " + facet.getFacetName());

        List<String> facetQueries = new ArrayList<String>();
        Iterator<Map.Entry<String, Facet>> iter = facetCatalog.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Facet> entry = (Map.Entry<String, Facet>)iter.next();
            if (!entry.getKey().equals(facet.getFacetName())) {
                facetQueries.add(entry.getValue().toSolrQuery());
            }
        }

        facetQueries.add(facet.toSolrQuery());

        String query = "";
        query = String.join(" AND ", facetQueries.stream()
                .filter(s -> !s.equals(""))
                .collect(Collectors.toList()));

        if (query.isEmpty()) {
            query = "*:*";
        } else {
            query = "(" + query + ")"; 
        }

        return query;
    }

    /**
     * To transform the constraints specified by user selections
     * for all facets into an executable Solr query for getting
     * data point statistics.
     * @return String Executable Solr queries
     */
    public String toSolrQuery() {
        List<String> facetQueries = new ArrayList<String>();
        Iterator<Map.Entry<String, Facet>> iter = facetCatalog.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Facet> entry = (Map.Entry<String, Facet>)iter.next();
            facetQueries.add(entry.getValue().toSolrQuery());
        }

        String query = "";
        query = String.join(" AND ", facetQueries.stream()
                .filter(s -> !s.equals(""))
                .collect(Collectors.toList()));

        if (query.isEmpty()) {
            query = "*:*";
        } else {
            query = "(" + query + ")"; 
        }

        return query;
    }
}
