package models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BundledResults{
	public TreeMap<String,TripleDocument> sparqlResults;
    public String treeResults;
    public String json;
    
    private ArrayList<String> vars = new ArrayList<String>();
    private int numVars;
    private TreeNode newTree;
    
	public BundledResults() {}

	public BundledResults(String json_result, boolean usingURIs){
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
		
		// build TreeQueryResults:
	    this.newTree = null;
		if(vars.contains("modelName") && vars.contains("superModelName")){
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
		}// /TREE QUERY RESULTS CONSTRUCTION
		
        // NOW BUILD THE SPARQLQUERYRESULTS:
        this.sparqlResults = new TreeMap<String,TripleDocument>();
        while (parseResults.hasNext()){
            try {
                JsonNode doc = parseResults.next();
                TripleDocument triple = new TripleDocument(doc, vars);
                //System.out.println(triple);
                // One of the fields in the TripleDocument should function as a primary key for rendering purposes
                if (doc.has("sn")) { this.sparqlResults.put(triple.get("sn"),triple); }
                else {
                    if (doc.has("modelName")) {
                        //System.out.println(triple.get("modelName") + " : " + triple);
                        this.sparqlResults.put(triple.get("modelName"),triple);
                    }
                    else this.sparqlResults.put(triple.generateID(), triple);
                }
			//System.out.println(the_docs.size());
            } catch (Exception e){
			    e.printStackTrace();
		    }
		}// /try/catch
	}// /constructor

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