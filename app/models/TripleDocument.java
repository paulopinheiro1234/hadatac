package models;

import java.util.TreeMap;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;

public class TripleDocument{
	
	public TreeMap<String,String> items = new TreeMap<String,String>();
	private int numVars;
	
	public TripleDocument() {}
	
	public TripleDocument(JsonNode node, ArrayList<String> vars) {
	    this.numVars = vars.size();
	    String variable;
	    for( int i = 0; i < this.numVars; i++){
	        variable = node.get(vars.get(i)).get("value").asText();
	        this.items.put(vars.get(i), variable);
	    }
	    
	}
}