package org.hadatac.console.models;

import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FacetHandler {

	public Map<String, Object> facetsAll;
	
	public static final String ENTITY_CHARACTERISTIC_FACET = "facetsEC";
	public static final String STUDY_FACET = "facetsS";
	public static final String UNIT_FACET = "facetsU";
	public static final String TIME_FACET = "facetsT";
	public static final String PLATFORM_INSTRUMENT_FACET = "facetsPI";

	private class Pair {
		String field = "";
		String value = "";

		Pair(String f, String v) {
			field = f;
			value = v;
		}

		String getField() {
			return field;
		}

		String getValue() {
			return value;
		}
	}

	public FacetHandler() { 
		facetsAll = new HashMap<String, Object>();
		facetsAll.put(ENTITY_CHARACTERISTIC_FACET, new ArrayList<Pair>());
		facetsAll.put(STUDY_FACET, new ArrayList<Pair>());
		facetsAll.put(UNIT_FACET, new ArrayList<Pair>());
		facetsAll.put(TIME_FACET, new ArrayList<Pair>());
		facetsAll.put(PLATFORM_INSTRUMENT_FACET, new ArrayList<Pair>());
	}
	
	@SuppressWarnings("unchecked")
	public List<Pair> getFacetByName(String facetName) {
		if (!facetsAll.containsKey(facetName)) {
			List<Pair> facet = new ArrayList<Pair>();
			facetsAll.put(facetName, facet);
			return facet;
		}
		return (List<Pair>)facetsAll.get(facetName);
	}
	
	public String getTempSolrQuery(String facetName, String f, List<String> values) {
		for (String val : values) {
			putFacet(facetName, f, val);
		}
		String query = toSolrQuery();
		for (String val : values) {
			removeFacet(facetName, f, val);
		}
		
		return query;
	}

	public String putFacet(String facetName, String f, String v) {
		Pair obj = new Pair(f, v);
		getFacetByName(facetName).add(obj);
		return obj.getValue();
	}

	public void removeFacet(String facetName, String f, String v) {
		for (Object obj : getFacetByName(facetName)) {
			Pair temp = (Pair)obj;
			if ((temp.getField().equals(f)) && (temp.getValue().equals(v))) {
				getFacetByName(facetName).remove(temp);
				break;
			}
		}
	}

	public List<String> values(String facetName) {
		List<String> list = new ArrayList<String>();
		for (Object obj : getFacetByName(facetName)) {
			Pair pair = (Pair)obj;
			list.add(pair.getValue());
		}
		return  list;
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

	private void loadFacet(String facetName, Object data, String solrFieldName) {
		JSONArray arr = (JSONArray)data;
		if (null != arr) {
			for (int i = 0; i < arr.size(); i++) {
				for (Object field : ((JSONObject)arr.get(i)).keySet()) {
					Pair pair = new Pair(solrFieldName, (String)((JSONObject)arr.get(i)).get((String)field));
					getFacetByName(facetName).add(pair);
				}
			}
		}
	}
	
	public void loadFacets(String str) {
		if (str == null || str.equals("")) {
			return;
		}
		
		try {
			JSONObject obj = (JSONObject)(new JSONParser().parse(str));
			loadFacet(ENTITY_CHARACTERISTIC_FACET, obj.get(ENTITY_CHARACTERISTIC_FACET), "characteristic_uri_str");
			loadFacet(STUDY_FACET, obj.get(STUDY_FACET), "acquisition_uri_str");
			loadFacet(UNIT_FACET, obj.get(UNIT_FACET), "unit_uri_str");
			loadFacet(TIME_FACET, obj.get(TIME_FACET), "timestamp_date");
			loadFacet(PLATFORM_INSTRUMENT_FACET, obj.get(PLATFORM_INSTRUMENT_FACET), "instrument_uri_str");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return;
	}

	private String facetToSolrQuery(List<Pair> facets) {
		String facetsQuery = "";
		if (facets == null) {
			return facetsQuery;
		}
		
		Iterator<Pair> i = facets.iterator();
		while (i.hasNext()) {
			Pair temp = i.next();
			facetsQuery += temp.getField() + ":\"" + temp.getValue() + "\"";
			if (i.hasNext()) {
				facetsQuery += " OR ";
			}
		}
		if (!facetsQuery.equals("")) {
			facetsQuery = "(" + facetsQuery + ")";
		}
		
		return facetsQuery;
	}

	@SuppressWarnings("unchecked")
	public String toSolrQuery() {
		String query = "";
		String query_tmp = "";
		int populatedLists = 0;
		Iterator<Map.Entry<String, Object>> iter = facetsAll.entrySet().iterator();
		while (iter.hasNext()) {			
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>)iter.next();
			List<Pair> tmpFacets = (List<Pair>)entry.getValue();
			query_tmp = facetToSolrQuery(tmpFacets);
			if (!query_tmp.equals("")) {
				if (tmpFacets.size() > 0) {
					populatedLists++;
				}
				if (populatedLists > 1) {
					query += " AND ";
				}
				query += query_tmp;
			}
		}
		if (query.isEmpty()) {
			query = "*:*";
		} else {
			query = "(" + query + ")"; 
		}
		
		return query;
	}
}
