package org.hadatac.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hadatac.console.models.Pivot;
import org.hadatac.entity.pojo.Measurement;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AcquisitionQueryResult {
	private long nDocSize = 0;
	public List<Measurement> documents;
	public Map<String, Map<String, Long>> field_facets;
	public Map<String, Map<String, Map<String, String>>> date_facets;
	public Map<String, List<Pivot>> pivot_facets;
	public Map<String, Pivot> extra_facets;
	
	public long getDocumentSize(){
		return this.nDocSize;
	}
	public void setDocumentSize(long nDocSize){
		this.nDocSize = nDocSize;
	}
	
        public List<Measurement> getDocuments() {
	        return documents;
        }

	public AcquisitionQueryResult() {
		documents = new ArrayList<Measurement>();
		field_facets = new HashMap<String, Map<String, Long>>();
		date_facets = new TreeMap<String, Map<String, Map<String, String>>>();
		pivot_facets = new HashMap<String, List<Pivot>>();
		extra_facets = new HashMap<String, Pivot>();
	}
	
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			System.out.println("[ERROR] AcquisitionQueryResult.toJSON() - Exception message: " + e.getMessage());
		} 
		return "";
	}
}
