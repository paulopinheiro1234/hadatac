package org.hadatac.console.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
//import javax.swing.tree.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OtMSparqlQueryResultsOriginal{

    public TreeMap<String,OtMTripleDocument> sparqlResults;
    public String treeResults;
    public String json;
    
    private ArrayList<String> vars = new ArrayList<String>();
    private int numVars;
    private TreeNode newTree;
    
	public OtMSparqlQueryResultsOriginal() {}

	public OtMSparqlQueryResultsOriginal(String json_result, boolean usingURIs){
        this.json = json_result;
        //System.out.println(this.json);
        // create an ObjectMapper instance.
        ObjectMapper mapper = new ObjectMapper();
        // use the ObjectMapper to read the json string and create a tree
        JsonNode node = null;
		try {
			node = mapper.readTree(json);
		} catch (IOException e) {
			e.printStackTrace();
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
		}// /try/catch
		
		Iterator<JsonNode> parseResults = bindings.iterator();
		numVars = vars.size();
		//System.out.println("Number of Vars: " + numVars + "\n");
		//System.out.println("Vars: " + vars + "\n");
		
		// build TreeQueryResults:
        if(vars.contains("id") && vars.contains("superId"))
            buildTreeQueryResults(bindings, usingURIs);
		else this.treeResults = "";
        // NOW BUILD THE SPARQLQUERYRESULTS:
        this.sparqlResults = new TreeMap<String,OtMTripleDocument>();
        while (parseResults.hasNext()){
            try {
                JsonNode doc = parseResults.next();
                OtMTripleDocument triple;
                OtMTripleDocument active = getTriple(doc.get("id").get("value").asText());
                if (active == null){
                    triple = new OtMTripleDocument(doc, vars);
                }
                else {
                    triple = active;
                    triple.addDoc(doc);
                }
                //System.out.println(triple);
                // The ID field in OtMTriple should be an ArrayList with exactly one thing in it
                if (doc.has("id"))
                    this.sparqlResults.put(triple.get("id").get(0),triple);
                else
                    this.sparqlResults.put(triple.generateID(), triple);
            } catch (Exception e){
			    e.printStackTrace();
		    }
		}// /while
	}// /constructor
	
	// This is the same as SparqlQueryResults regardless of whether the
	//    properties are one-to-one or one-to-many
	private void buildTreeQueryResults(JsonNode bindings, boolean usingURIs){
        this.newTree = null;
        Iterator<JsonNode> elements = bindings.elements();
        String modelN = null;
        String superN = null;
        while (elements.hasNext()){
            modelN = "";
		    superN = "";
            JsonNode binding = elements.next();
            System.out.println("Binding: " + binding + "\n");
            JsonNode modelNameNode = binding.findPath("id");
            System.out.println("modelNameNode: " + modelNameNode + "\n");
            if (modelNameNode != null && modelNameNode.get("value") != null) {
                modelN = modelNameNode.get("value").asText();
            }
		    JsonNode superNameNode = binding.findPath("superId");
		    System.out.println("superNameNode: " + superNameNode + "\n");
            if (superNameNode != null && superNameNode.get("value") != null) {
                superN = superNameNode.get("value").asText();
                System.out.println("superN: " + superN + "\n");
            }
            if (usingURIs && ! modelN.equals("")) {
                modelN = prettyFromURI(modelN);
                System.out.println("modelN: " + modelN + "\n");
            }
            if (usingURIs && ! superN.equals("")) {
                superN = prettyFromURI(superN);
                System.out.println("usingURIs superN: " + superN + "\n");
            }
            if (superN.equals("")) {
                newTree = new TreeNode(modelN);
                System.out.println("Super Node is blank: " + newTree.toJson(0) + "\n");
            } else {
                TreeNode parent;
                if (newTree == null) {
                    parent = null;
                    System.out.println("Parent Null New Tree");
                } else {
                    parent = newTree.hasValue(superN);
                }
                if (parent == null) {
                    if (newTree == null) {
                        newTree = new TreeNode(superN);
                        newTree.addChild(modelN);
                        System.out.println("New Tree in Null Parent: " + newTree.toJson(0) + "\n");
                    } else {
                        System.out.println("Parent <" + superN + "> not found");
                        TreeNode newNode = new TreeNode(superN);
                        //newNode.addChild(modelN);
                        System.out.println("New Tree Name: " + newTree.getName() + "\n");
                        System.out.println("New Node Name: " + newNode.getName() + "\n");
                        if (newTree.getName() == modelN) {
                        	//newNode.
                        }
                        
                    }
                } else {
                    TreeNode copyNode = newTree.hasValue(modelN);
                    if (copyNode == null) {
                        parent.addChild(modelN);
                    } else {
                    	parent.addChild(copyNode);
                    }
                    System.out.println("copy node: " + copyNode.toJson(0) + "\n");
                }
            }
        System.out.println("model Name = <"+ modelN + " , " + superN + ">"); 
        System.out.println("New Tree in Loop: " + newTree.toJson(0) + "\n");
        }// /while
        if (newTree == null) 
            this.treeResults = "";
        else
            this.treeResults = newTree.toJson(0);
        System.out.println("New Tree : " + newTree.toJson(0) + "\n");
        System.out.println("Tree Results: " + this.treeResults);
	}// /buildTreeQueryResults
	
	public OtMTripleDocument getTriple (String key){
	    OtMTripleDocument item = this.sparqlResults.get(key);
	    return item;
	}
	
	/*public ArrayList<OtMTripleDocument> getMatching (String prop, String value){
        ArrayList<TripleDocument> results = new ArrayList<TripleDocument>();
        TripleDocument doc;
        for (Map.Entry<String, OtMTripleDocument> entry : this.sparqlResults.entrySet()) {
            doc = entry.getValue();
            if(doc.get(prop).equals(value)) {
                results.add(doc);
            }
        }
        return results;
	}*/

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
