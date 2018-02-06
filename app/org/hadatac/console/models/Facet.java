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
	private Map<String, List<String>> mapFieldValues = new HashMap<String, List<String>>();
	private Map<String, Facet> children = new HashMap<String, Facet>();
    
	public Facet() {}
	
	public Facet(String id) {
		this.id = id;
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
		} else {
			facet = new Facet(id);
			facet.addFieldValues(getFieldValues());
			addChild(facet);
		}
		
		return facet;
	}
	
	public void addChild(Facet facet) {
		children.put(facet.getId(), facet);
	}
	
	public static Facet loadFacet(Object data) {
		Facet facet = new Facet();
		
		JSONArray arrFacets = (JSONArray)data;
		if (null != arrFacets) {
			for (int i = 0; i < arrFacets.size(); i++) {
				JSONObject subfacet = (JSONObject)arrFacets.get(i);
				for (Object field : subfacet.keySet()) {
					if (((String)field).equals("children")) {
						facet.addChild(loadFacet(subfacet.get((String)field)));
					} else if (((String)field).equals("id")) {
						facet.setId((String)(subfacet.get((String)field)));
					} else {
						facet.putFacet((String)field, (String)(subfacet.get((String)field)));
					}
				}
			}
		}
		
		return facet;
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
			if (!getIgnoredFields().contains(field)) {
				fieldQueries.add(String.join(" OR ", getFieldValues().get(field).stream().map(
						p -> field + ":\"" + p + "\"").collect(Collectors.toList())));
			}
		}
		
		for (Facet f : getChildrenAsList()) {
			fieldQueries.add(f.toSolrQuery());
		}
		
		String facetsQuery = String.join(" AND ", fieldQueries.stream()
				.filter(s -> !s.equals(""))
				.map(s -> "(" + s + ")")
				.collect(Collectors.toList()));
		
		if (!facetsQuery.equals("")) {
			facetsQuery = "(" + facetsQuery + ")";
		}
		
		return facetsQuery;
	}
	
	private List<String> getIgnoredFields() {
		return Arrays.asList("indicator_uri_str", "entity_role_uri_str");
	}
}
