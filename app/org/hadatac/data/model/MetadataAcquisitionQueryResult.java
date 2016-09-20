package org.hadatac.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.console.models.Pivot;
import org.hadatac.entity.pojo.Study;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MetadataAcquisitionQueryResult {
	public List<Study> documents;
	public Map<String, Map<String, Long>> field_facets;
	public Map<String, List<Pivot>> pivot_facets;
	
	public MetadataAcquisitionQueryResult() {
		documents = new ArrayList<Study>();
		field_facets = new HashMap<String, Map<String, Long>>();
		pivot_facets = new HashMap<String, List<Pivot>>();
	}
	
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			System.out.println("[ERROR] MetadataAcquisitionQueryResult.toJSON() - Exception message: " + e.getMessage());
		} 
		return "";
	}
}
