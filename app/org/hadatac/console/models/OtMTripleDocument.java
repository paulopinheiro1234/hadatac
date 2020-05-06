package org.hadatac.console.models;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Random;
import com.fasterxml.jackson.databind.JsonNode;

public class OtMTripleDocument{
	public TreeMap<String,ArrayList<String>> items = new TreeMap<String,ArrayList<String>>();
	//public String resultType;
	private ArrayList<String> vars;
	private int numVars;
	
	public OtMTripleDocument() {
	    System.out.println("created new OtMTripleDocument");
	}
	
    public OtMTripleDocument(JsonNode node, ArrayList<String> vars) {
        //this.resultType = resultType;
        this.vars = vars;
        this.numVars = vars.size();
        String variable;
        for( int i = 0; i < this.numVars; i++){
            variable = "";
            try {
                if (node.get(vars.get(i)).hasNonNull("value")) {
                    variable = node.get(vars.get(i)).get("value").asText();
                    if(variable.contains("#")) {
                        if(!variable.contains("URI"))
                            variable = prettyFromURI(variable);
                    }
                    if(isDouble(variable)){
                        variable = toNum(Double.parseDouble(variable));
                    }
                }
                //System.out.println("read: " + vars.get(i) + " = " + variable);
            } catch (NullPointerException e){
                if(vars.get(i).equals("sn")) {
                    variable = generateID();
                    //System.out.println(resultType + " is missing a serial number! " + variable + " generated as placeholder");
                }
                else {
                    variable = "";
                    //System.out.println("Error getting " + vars.get(i) + " from results " + resultType);
                }
            }// /catch
            addItem(vars.get(i), variable);
        }// /for
    }// constructor
    
    public void addDoc(JsonNode node){
        String variable;
        for( int i = 0; i < this.numVars; i++){
            variable = "";
            try {
                if(node.get(vars.get(i)).hasNonNull("value")){
                    variable = node.get(vars.get(i)).get("value").asText();
                    if(variable.contains("#"))
                        if(!variable.contains("URI"))
                            variable = prettyFromURI(variable);
                    if(isDouble(variable)){
                        variable = toNum(Double.parseDouble(variable));
                    }
                }
                //System.out.println("read: " + vars.get(i) + " = " + variable);
            } catch (NullPointerException e){
                if(vars.get(i).equals("sn")) {
                    variable = generateID();
                    //System.out.println(resultType + " is missing a serial number! " + variable + " generated as placeholder");
                }
                else {
                    variable = "";
                    //System.out.println("Error getting " + vars.get(i) + " from results " + resultType);
                }
            }// /catch
            addItem(vars.get(i), variable);
        }// /for
    }// /addDoc()
    
    // Adds a new key-value pair
    // If the key already exists, use the below method
    private void addItem(String key, String value){
        if(this.items.containsKey(key))
            addValue(key, value);
        ArrayList<String> vals = new ArrayList<String>();
        vals.add(value);
        items.put(key,vals);
    }
    
    // Adds a value to an existing key
    private void addValue(String key, String value){
        this.items.get(key).add(value);
    }
	
	// The Scala table generators use this method to access the triple's fields
	// This method is distinct from TripleDocument's implementation in that
	//   this returns an ARRAY of Strings (due to one-to-many mapping), rather
	//   than a single String. This is true even if the ArrayList contains only
	//   one String.
	public ArrayList<String> get(String key){
	    return this.items.get(key);
	}
	
	public boolean has(String key){
	    if (!this.vars.contains(key))
	        return false;
	    else if (this.items.get(key).isEmpty())
	        return false;
	    else return true;
	}
	
	private static String prettyFromURI (String origURI) {
		if (!origURI.contains("#"))
			return origURI;
		String pretty = origURI.substring(origURI.indexOf('#') + 1);
		String prettyFinal = "" + pretty.charAt(0);
		for (int pos = 1; pos < pretty.length(); pos++) {
			if (Character.isLowerCase(pretty.charAt(pos - 1)) && Character.isUpperCase(pretty.charAt(pos))) {
				prettyFinal = prettyFinal + " " + pretty.charAt(pos);
			} else {
				prettyFinal = prettyFinal + pretty.charAt(pos);
			}
		}
		return prettyFinal;
	}
	
    private static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
	
    private static String toNum(double d) {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
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