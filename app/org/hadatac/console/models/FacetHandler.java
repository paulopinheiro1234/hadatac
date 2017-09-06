package org.hadatac.console.models;

import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FacetHandler {

	public Map<String, Object> facetsAll;
	
	public final String ENTITY_CHARACTERISTIC_FACET = "facetsEC";
	public final String STUDY_FACET = "facetsS";
	public final String UNIT_FACET = "facetsU";
	public final String TIME_FACET = "facetsT";
	public final String PLATFORM_INSTRUMENT_FACET = "facetsPI";

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

	private void loadOneFacet(List<Pair> l, String facet) {
		if (facet == null || facet.equals("") || facet.equals("{}")) {
			return;
		}
		String field = "";
		String value = "";
		if (facet.indexOf('"') >= 0) {
			facet = facet.substring(facet.indexOf('"') + 1);
			if (facet.indexOf('"') >= 0) {
				field = facet.substring(0,facet.indexOf('"'));
				if (facet.indexOf('"') >= 0) {
					facet = facet.substring(facet.indexOf('"') + 1);				   
					if (facet.indexOf('"') >= 0) {
						facet = facet.substring(facet.indexOf('"') + 1);
						if (facet.indexOf('"') >= 0) {
							value = facet.substring(0,facet.indexOf('"'));
						}
					}
				}
			}
		}
		if (!field.equals("") && !value.equals("")) { 
			Pair obj = new Pair(field, value);
			l.add(obj);
		}
	}

	private void loadList(List<Pair> l, String str) {
		if (str == null || str.equals("")) {
			return;
		}
		//System.out.println(">> loadList = <" + str + ">");
		if (str.indexOf(',') == -1) {
			loadOneFacet(l,str);}
		else {
			StringTokenizer st = new StringTokenizer(str,",");
			while (st.hasMoreTokens()) {
				loadOneFacet(l, st.nextToken());
			}	
		}    
		return;
	}

	public void loadFacets(String str) {
		if (str == null || str.equals("")) {
			return;
		}		    
		// EC list
		str = str.substring(str.indexOf('['));
		String ECList = str.substring(1,str.indexOf(']'));
		if (ECList != null && !ECList.equals("") && !ECList.equals("{}")) {
			loadList(getFacetByName(ENTITY_CHARACTERISTIC_FACET), ECList);
		}
		str = str.substring(str.indexOf(']'));		    
		// S list
		str = str.substring(str.indexOf('['));
		String SList = str.substring(1,str.indexOf(']'));		    
		if (SList != null && !SList.equals("") && !SList.equals("{}")) {
			loadList(getFacetByName(STUDY_FACET), SList);
		}
		str = str.substring(str.indexOf(']'));		    
		// U list
		str = str.substring(str.indexOf('['));
		String UList = str.substring(1,str.indexOf(']'));		    
		if (UList != null && !UList.equals("") && !UList.equals("{}")) {
			loadList(getFacetByName(UNIT_FACET), UList);
		}
		str = str.substring(str.indexOf(']'));		    
		// T list
		str = str.substring(str.indexOf('['));
		String TList = str.substring(1,str.indexOf(']'));		    
		if (TList != null && !TList.equals("") && !TList.equals("{}")) {
			loadList(getFacetByName(TIME_FACET), TList);
		}
		str = str.substring(str.indexOf(']'));		    
		// PI list
		str = str.substring(str.indexOf('['));
		String PIList = str.substring(1,str.indexOf(']'));		    
		if (PIList != null && !PIList.equals("") && !PIList.equals("{}")) {
			loadList(getFacetByName(PLATFORM_INSTRUMENT_FACET), PIList);
		}
		str = str.substring(str.indexOf(']'));		    
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
			//System.out.println("inside pivot: " + temp.getField());
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
		Iterator<Map.Entry<String, Object>> i = facetsAll.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>)i.next();
			List<Pair> tmpFacets = (List<Pair>)entry.getValue();
			//System.out.println("List's name: " + entry.getKey() + " size:" + tmpFacets.size());
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
