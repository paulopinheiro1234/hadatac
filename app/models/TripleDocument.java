package models;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Random;
import com.fasterxml.jackson.databind.JsonNode;

public class TripleDocument{
	public TreeMap<String,String> items = new TreeMap<String,String>();
	public String resultType;
	private int numVars;
	
	public TripleDocument() {}
	
	public TripleDocument(JsonNode node, ArrayList<String> vars, String resultType) {
	    this.resultType = resultType;
	    this.numVars = vars.size();
	    String variable;
	    for( int i = 0; i < this.numVars; i++){
	        variable = "";
	        try {
	            if(node.get(vars.get(i)).hasNonNull("value"))
	                variable = node.get(vars.get(i)).get("value").asText();
	                //System.out.println("read: " + vars.get(i) + " = " + variable);
	        } catch (NullPointerException e){
	            if(vars.get(i).equals("sn")) {
	                variable = generateID();
	                System.out.println(resultType + " is missing a serial number! " + variable + " generated as placeholder");
	            }
	            else {
	                variable = "[NULL]";
	                //System.out.println("Error getting " + vars.get(i) + " from results " + resultType);
	            }
	        }// /catch
	        this.items.put(vars.get(i), variable);
	    }// /for
	}// constructor
	
	// The Scala table generators use this method to access the triple's fields
	public String get(String key){
	    return this.items.get(key);
	}
	
	public boolean has(String key){
	    boolean b = false;
	    if(!(this.items.get(key).equals("[NULL]"))){
	        b = true;
	    }
	    return b;
	}

    // The accordion menus for results are composed of two div elements
    //   that must have corresponding names that are unique for each entry.
    // We use serial numbers where applicable, since these are guaranteed
    //   to be unique, but for queries where the results don't have serial numbers,
    //   (eg, Entities) we'll just generate a random 5-digit number instead.
    public String generateID(){
        Random rand = new Random();
        int randomNum = rand.nextInt((99999 - 10000) + 1) + 10000;
        return String.valueOf(randomNum);
    }
}