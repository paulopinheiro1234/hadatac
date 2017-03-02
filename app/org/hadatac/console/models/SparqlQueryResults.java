package org.hadatac.console.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SparqlQueryResults{
    public TreeMap<String,TripleDocument> sparqlResults;
    public String treeResults;
    public String json;
    
    private ArrayList<String> vars = new ArrayList<String>();
    private TreeNode newTree;
    
	public SparqlQueryResults() {}

	public SparqlQueryResults(String json_result, boolean usingURIs){
        this.json = json_result;
        // create an ObjectMapper instance.
        ObjectMapper mapper = new ObjectMapper();
        // use the ObjectMapper to read the json string and create a tree
        JsonNode node = null;
		try {
			node = mapper.readTree(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(null == node){
			this.treeResults = "";
			return;
		}
		JsonNode header = node.get("head");
		header = header.get("vars");
		JsonNode bindings = node.get("results");
		bindings = bindings.get("bindings");
		
		// parse the head, and record the bindings
		Iterator<JsonNode> parseHead = header.iterator();
		String var = "";
		try{
		    while(parseHead.hasNext()){
		        var = parseHead.next().asText();
		        vars.add(var);
		    }
		} catch (Exception e){
			e.printStackTrace();
		}
		
		Iterator<JsonNode> parseResults = bindings.iterator();
		
		// build TreeQueryResults:
        if(vars.contains("modelName") && vars.contains("superModelName"))
            buildTreeQueryResults(bindings, usingURIs);
		else this.treeResults = "";    
		
        // NOW BUILD THE SPARQLQUERYRESULTS:
        this.sparqlResults = new TreeMap<String,TripleDocument>();
        while (parseResults.hasNext()){
            try {
                JsonNode doc = parseResults.next();
                TripleDocument triple = new TripleDocument(doc, vars);
                //System.out.println(triple);
                // One of the fields in the TripleDocument should function as a primary key for rendering purposes
                if (triple.has("sn")) { 
                    this.sparqlResults.put(triple.get("sn"),triple);
                    //System.out.println("Adding to results [sn]: " + triple.get("sn"));
                }
                else if (triple.has("modelName")) {
                    this.sparqlResults.put(triple.get("modelName"),triple);
                    //System.out.println("Adding to results [modelName]: " + triple.get("modelName"));
                }
                else if (triple.has("sp")) {
                    this.sparqlResults.put(triple.get("sp"),triple);
                    //System.out.println("Adding to results [sp]: " + triple.get("sp"));
                }
                else this.sparqlResults.put(triple.generateID(), triple);
            } catch (Exception e){
			    e.printStackTrace();
		    }
		}
	}
	
	private void buildTreeQueryResults(JsonNode bindings, boolean usingURIs){
        this.newTree = null;
        Iterator<JsonNode> elements = bindings.elements();
        String modelN = null;
        String superN = null;
        while (elements.hasNext()){
            modelN = "";
		    superN = "";
            JsonNode binding = elements.next();
            JsonNode modelNameNode = binding.findPath("modelName");
            if (modelNameNode != null && modelNameNode.get("value") != null) {
                modelN = modelNameNode.get("value").asText();
            }
		    JsonNode superNameNode = binding.findPath("superModelName");
            if (superNameNode != null && superNameNode.get("value") != null) {
                superN = superNameNode.get("value").asText();
            }
            if (usingURIs && ! modelN.equals("")) {
                modelN = prettyFromURI(modelN);
            }
            if (usingURIs && ! superN.equals("")) {
                superN = prettyFromURI(superN);
            }
            if (superN.equals("")) {
                newTree = new TreeNode(modelN);
            } else {
                TreeNode parent;
                if (newTree == null) {
                    parent = null;
                } else {
                    parent = newTree.hasValue(superN);
                }
                if (parent == null) {
                    if (newTree == null) {
                        newTree = new TreeNode(superN);
                        newTree.addChild(modelN);
                    } else {
                        //System.out.println("Parent <" + superN + "> not found");
                    }
                } else {
                    TreeNode copyNode = newTree.hasValue(modelN);
                    if (copyNode == null) {
                        parent.addChild(modelN);
                    }
                }
            }
        //System.out.println("model Name = <"+ modelN + " , " + superN + ">"); 
        }// /while
        if (newTree == null) 
            this.treeResults = "";
        else
            this.treeResults = newTree.toJson(0);
	}
	
	public TripleDocument getTriple (String key){
	    TripleDocument item = this.sparqlResults.get(key);
	    return item;
	}
	
	public ArrayList<TripleDocument> getMatching (String prop, String value){
        ArrayList<TripleDocument> results = new ArrayList<TripleDocument>();
        TripleDocument doc;
        for (Map.Entry<String, TripleDocument> entry : this.sparqlResults.entrySet()) {
            doc = entry.getValue();
            if(doc.get(prop).equals(value)) {
                results.add(doc);
            }
        }
        return results;
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
}
