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
	
	public Map<String, Facet> facetsAll;
	
	public static final String ENTITY_CHARACTERISTIC_FACET = "facetsEC";
	public static final String STUDY_FACET = "facetsS";
	public static final String OBJECT_COLLECTION_FACET = "facetsOC";
	public static final String UNIT_FACET = "facetsU";
	public static final String TIME_FACET = "facetsT";
	public static final String PLATFORM_INSTRUMENT_FACET = "facetsPI";
	public static final String SUBJECT_CHARACTERISTIC_FACET = "facetsSC";

	public FacetHandler() { 
		facetsAll = new HashMap<String, Facet>();
		facetsAll.put(ENTITY_CHARACTERISTIC_FACET, new Facet());
		facetsAll.put(STUDY_FACET, new Facet());
		facetsAll.put(OBJECT_COLLECTION_FACET, new Facet());
		facetsAll.put(UNIT_FACET, new Facet());
		facetsAll.put(TIME_FACET, new Facet());
		facetsAll.put(PLATFORM_INSTRUMENT_FACET, new Facet());
	}
	
	public Facet getFacetByName(String facetName) {
		if (facetsAll.containsKey(facetName)) {
			return facetsAll.get(facetName);
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getValuesByFacetField(String facetName, String fieldName) {
		List<String> results = new ArrayList<String>();
		if (facetsAll.containsKey(facetName)) {
			if (((Map<String, List<String>>)facetsAll.get(facetName)).containsKey(fieldName)) {
				for (String value : ((Map<String, List<String>>)facetsAll.get(facetName)).get(fieldName)) {
					if (!results.contains(value)) {
						results.add(value);
					}
				}
			}
		}
		
		return results;
	}
	
	public String values(String facetName) {
		List<String> results = facetsAll.get(facetName).values();
		return (new Gson()).toJson(results);
	}

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
	
	public void loadFacets(String str) {
		if (str == null || str.equals("")) {
			// Default facets
			str = "{\"facetsEC\":[],\"facetsS\":[],\"facetsOC\":[],\"facetsU\":[],\"facetsT\":[],\"facetsPI\":[]}";
		}
				
		try {
			JSONObject obj = (JSONObject)(new JSONParser().parse(str));
			Facet facet = Facet.loadFacet(obj.get(ENTITY_CHARACTERISTIC_FACET), ENTITY_CHARACTERISTIC_FACET);
			facetsAll.put(ENTITY_CHARACTERISTIC_FACET, facet);
			
			facet = Facet.loadFacet(obj.get(STUDY_FACET), STUDY_FACET);
			facetsAll.put(STUDY_FACET, facet);
			
			facet = Facet.loadFacet(obj.get(OBJECT_COLLECTION_FACET), OBJECT_COLLECTION_FACET);
            facetsAll.put(OBJECT_COLLECTION_FACET, facet);
			
			facet = Facet.loadFacet(obj.get(UNIT_FACET), UNIT_FACET);
			facetsAll.put(UNIT_FACET, facet);
			
			facet = Facet.loadFacet(obj.get(TIME_FACET), TIME_FACET);
			facetsAll.put(TIME_FACET, facet);
			
			facet = Facet.loadFacet(obj.get(PLATFORM_INSTRUMENT_FACET), PLATFORM_INSTRUMENT_FACET);
			facetsAll.put(PLATFORM_INSTRUMENT_FACET, facet);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	public Map<String, List<String>> getTempSparqlConstraints(Facet facet) {
        System.out.println("getTempSparqlConstraints() facet.getFacetName(): " + facet.getFacetName());
        
        Map<String, List<String>> constraints = new HashMap<String, List<String>>();
        Iterator<Map.Entry<String, Facet>> iter = facetsAll.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Facet> entry = (Map.Entry<String, Facet>)iter.next();
            if (!entry.getKey().equals(facet.getFacetName())) {
                entry.getValue().toSparqlConstraints(constraints);
            }
        }
        
        facet.toSparqlConstraints(constraints);
        
        return constraints;
    }
	
	public String getTempSolrQuery(Facet facet, String field, List<String> values) {
		for (String val : values) {
			facet.putFacet(field, val);
		}
		String query = toSolrQuery();
		for (String val : values) {
			facet.removeFacet(field, val);
		}
		
		return query;
	}
	
	public String getTempSolrQuery(Facet facet) {
		System.out.println("getTempSolrQuery() facet.getFacetName(): " + facet.getFacetName());
		
		List<String> facetQueries = new ArrayList<String>();
		Iterator<Map.Entry<String, Facet>> iter = facetsAll.entrySet().iterator();
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

	public String toSolrQuery() {
		List<String> facetQueries = new ArrayList<String>();
		Iterator<Map.Entry<String, Facet>> iter = facetsAll.entrySet().iterator();
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
	
	public String bottommostFacetsToSolrQuery() {
		List<String> facetQueries = new ArrayList<String>();
		Iterator<Map.Entry<String, Facet>> iter = facetsAll.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Facet> entry = (Map.Entry<String, Facet>)iter.next();
			facetQueries.add(entry.getValue().bottommostFacetsToSolrQuery());
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
