package org.hadatac.console.models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectDetails {
	
	private Map<String, String> details;

	public ObjectDetails() {
	       
	       details = new HashMap<String, String>();

	}
	
	public void putObject(String uri, String html) {
	    details.put(uri, html);
	    return;
	}
	
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String output =  mapper.writeValueAsString(details);
			return output;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return "";
	}
}
