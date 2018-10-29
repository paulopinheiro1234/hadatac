package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Facet {
	private String id;
	private String facetName;
	private Map<String, List<String>> mapFieldValues = new HashMap<String, List<String>>();
	private Map<String, Facet> children = new HashMap<String, Facet>();
    
	public Facet() {}
	
	public Facet(String id) {
		this.id = id;
	}
	
	public String getFacetName() {
		return facetName;
	}
	
	public void setFacetName(String facetName) {
		this.facetName = facetName;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Map<String, List<String>> getFieldValues() {
		return mapFieldValues;
	}
	
	public List<Facet> getChildrenAsList() {
		return children.values().stream()
				.collect(Collectors.toList());
	}
	
	public Map<String, Facet> getChildren() {
		return children;
	}
	
	public Facet getChildById(String id) {
		Facet facet = null;
		if (children.containsKey(id)) {
			facet = children.get(id);
			facet.addFieldValues(getFieldValues());
		} else {
			facet = new Facet(id);
			facet.setFacetName(getFacetName());
			facet.addFieldValues(getFieldValues());
			addChild(facet);
		}
		
		return facet;
	}
	
	public void addChild(Facet facet) {
		if (children.containsKey(facet.getId())) {
			children.get(facet.getId()).merge(facet);
		} else {
			children.put(facet.getId(), facet);
		}
	}
	
	public static Facet loadFacet(Object data, String facetName) {
		Facet facet = new Facet();
		facet.setFacetName(facetName);
		
		JSONArray arrFacets = (JSONArray)data;
		if (null != arrFacets) {
			for (int i = 0; i < arrFacets.size(); i++) {
				JSONObject subfacet = (JSONObject)arrFacets.get(i);
				for (Object field : subfacet.keySet()) {
					if (!((String)field).equals("children") && !((String)field).equals("id")) {
						facet.putFacet((String)field, (String)(subfacet.get((String)field)));
					}
				}
				
				if (subfacet.containsKey("children")) {
					Facet childFacet = loadFacet(subfacet.get("children"), facetName);
					if (subfacet.containsKey("id")) {
						childFacet.setId((String)(subfacet.get("id")));
						childFacet.setFacetName(facetName);
					}
					facet.addChild(childFacet);
				}
			}
		}
		
		return facet;
	}
	
	public void merge(Facet facet) {
		if (facet == null) {
			return;
		}
		
		// Merge current level
		addFieldValues(facet.getFieldValues());
		
		// Merge children's levels
		for (String id : facet.getChildren().keySet()) {
			if (getChildren().containsKey(id)) {
				getChildren().get(id).merge(facet.getChildren().get(id));
			} else {
				getChildren().put(id, facet.getChildren().get(id));
			}
		}
	}
	
	public void tailor(Facet facet) {
		if (facet == null) {
			return;
		}
		
		for (String field : getFieldValues().keySet()) {
			if (facet.getFieldValues().containsKey(field)) {
				Iterator<String> iter = getFieldValues().get(field).iterator();
				while (iter.hasNext()) {
					String value = iter.next();
				    if (!facet.getFieldValues().get(field).contains(value)) {
				    	iter.remove();
				    }
				}
			}
		}
	}
	
	public List<String> getFacetValuesByField(String field) {
		if (mapFieldValues.containsKey(field)) {
			return mapFieldValues.get(field);
		} else {
			return new ArrayList<String>();
		}
	}
	
	public void addFieldValues(Map<String, List<String>> mapFieldValues) {
		for (Map.Entry<String, List<String>> entry : mapFieldValues.entrySet()) {
			for (String val : entry.getValue()) {
				putFacet(entry.getKey(), val);
			}
		}
	}

	public boolean putFacet(String field, String value) {
		Map<String, List<String>> mapFieldValues = getFieldValues();
		if (!mapFieldValues.containsKey(field)) {
			mapFieldValues.put(field, new ArrayList<String>());
		}
		if (!mapFieldValues.get(field).contains(value)) {
			mapFieldValues.get(field).add(value);
			return true;	// Not existed before
		}
		
		return false;	// Already existed
	}

	public void removeFacet(String field, String value) {
		Map<String, List<String>> mapFieldValues = getFieldValues();
		if (!mapFieldValues.containsKey(field)) {
			return;
		}
		mapFieldValues.get(field).remove(value);
	}
	
	public void clearFieldValues(String field) {
		Map<String, List<String>> mapFieldValues = getFieldValues();
		if (!mapFieldValues.containsKey(field)) {
			return;
		}
		mapFieldValues.remove(field);
	}
	
	public List<String> values() {
		List<String> values = new ArrayList<String>();
		Map<String, List<String>> mapFieldValues = getFieldValues();
		for (String field : mapFieldValues.keySet()) {
			values.addAll(mapFieldValues.get(field));
		}
		
		for (Facet facet : getChildrenAsList()) {
			values.addAll(facet.values());
		}
		
		return values;
	}
	
	public String toSolrQuery() {
		List<String> fieldQueries = new ArrayList<>();
		for (String field : getFieldValues().keySet()) {
			if (!getIgnoredFields().contains(field) && !field.isEmpty()) {
				fieldQueries.add(field + ":(" + String.join(", ", getFieldValues().get(field).stream().map(
                        p -> "\"" + p + "\"").collect(Collectors.toList())) + ")");
			}
		}
		
		String fieldQuery = String.join(" AND ", fieldQueries.stream()
				.filter(s -> !s.equals(""))
				.map(s -> wrapWithParentheses(s))
				.collect(Collectors.toList()));
		
		List<String> childrenQueries = new ArrayList<>();
		for (Facet f : getChildrenAsList()) {
			childrenQueries.add(f.toSolrQuery());
		}
		
		String childrenQuery = String.join(" OR ", childrenQueries.stream()
				.filter(s -> !s.equals(""))
				.map(s -> wrapWithParentheses(s))
				.collect(Collectors.toList()));
		
		String query = String.join(" AND ", Arrays.asList(fieldQuery, childrenQuery).stream()
				.filter(s -> !s.equals(""))
				.map(s -> wrapWithParentheses(s))
				.collect(Collectors.toList()));
		
		return wrapWithParentheses(query);
	}
	
	public String currentLevelToSolrQuery() {
		List<String> fieldQueries = new ArrayList<>();
		for (String field : getFieldValues().keySet()) {
			if (!getIgnoredFields().contains(field)) {
			    fieldQueries.add(field + ":(" + String.join(", ", getFieldValues().get(field).stream().map(
                        p -> "\"" + p + "\"").collect(Collectors.toList())) + ")");
			}
		}
		
		String facetsQuery = String.join(" AND ", fieldQueries.stream()
				.filter(s -> !s.equals(""))
				.map(s -> wrapWithParentheses(s))
				.collect(Collectors.toList()));
		
		return wrapWithParentheses(facetsQuery);
	}
	
	private String fieldValuesToSolrQuery(Map<String, List<String>> fieldValues) {
		List<String> fieldQueries = new ArrayList<>();
		
		for (String field : getFieldValues().keySet()) {
			if (!getIgnoredFields().contains(field)) {
			    fieldQueries.add(field + ":(" + String.join(", ", getFieldValues().get(field).stream().map(
                        p -> "\"" + p + "\"").collect(Collectors.toList())) + ")");
			}
		}
		
		String facetsQuery = String.join(" AND ", fieldQueries.stream()
				.filter(s -> !s.equals(""))
				.map(s -> wrapWithParentheses(s))
				.collect(Collectors.toList()));
		
		return wrapWithParentheses(facetsQuery);
	}
	
	public String bottommostFacetsToSolrQuery() {
		List<String> fieldQueries = new ArrayList<>();
		
		if (getChildrenAsList().isEmpty()) {
			fieldQueries.add(fieldValuesToSolrQuery(getFieldValues()));
		} else {
			for (Facet f : getChildrenAsList()) {
				fieldQueries.add(f.bottommostFacetsToSolrQuery());
			}
		}
		
		String facetsQuery = String.join(" OR ", fieldQueries.stream()
				.filter(s -> !s.equals(""))
				.map(s -> wrapWithParentheses(s))
				.collect(Collectors.toList()));
		
		return wrapWithParentheses(facetsQuery);
	}
	
	private String wrapWithParentheses(String str) {
		if (!str.trim().equals("")) {
			str = "(" + str + ")";
		}
		
		return str;
	}
	
	private List<String> getIgnoredFields() {
		return Arrays.asList("indicator_uri_str", "entity_role_uri_str", 
				"platform_uri_str", "instrument_uri_str", "dase_type_uri_str");
	}
}
