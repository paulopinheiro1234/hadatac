package org.hadatac.console.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FacetHandler {
	public Map<String, String> facetsAnd;
	public Map<String, String> facetsOr;
	
	public FacetHandler() {
		facetsAnd = new HashMap<String, String>();
		facetsOr = new HashMap<String, String>();
	}
	
	public String putFacet(String field, String value) {
		return facetsAnd.put(field, value);
	}
	
	public void removeFacet(String field) {
		facetsAnd.remove(field);
	}
	
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return "";
	}
	
	public String toSolrQuery() {
		String query = "";
		Set<String> fields = facetsAnd.keySet();
		Iterator<String> i = fields.iterator();
		while (i.hasNext()) {
			String field = i.next();
			query += field + ":\"" + facetsAnd.get(field) + "\"";
			if (i.hasNext()) {
				query += " AND ";
			}
		}
		if (query.isEmpty()) {
			query = "*:*";
		}
		return query;
	}
}
