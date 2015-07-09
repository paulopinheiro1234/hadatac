package models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TreeQueryResults extends QueryResults{
    public String json;
    public TreeNode newTree;
    
    public TreeQueryResults () {} 
    
    public TreeQueryResults (String query_json, boolean usingURIs) { 
        this.json = json;
        this.newTree = null;
        
        ObjectMapper mapper = new ObjectMapper();
        // use the ObjectMapper to read the json string and create a tree
        JsonNode rootNode = null;
    	try {
    		rootNode = mapper.readTree(query_json);
    		//System.out.println("AQUI ESTA O TAMANHO DO RESULTADO 2: <" + rootNode.size() + ">");
    	} catch (IOException e) {
    		e.printStackTrace();
    	}  
	
    	//JsonNode phoneNosNode = rootNode.path("phoneNumbers");
    	JsonNode bindingsNode = rootNode.findPath("bindings");
    	//System.out.println("AQUI ESTA O TAMANHO DO BINDINGS: <" + bindingsNode.size() + ">");
    	Iterator<JsonNode> elements = bindingsNode.elements();
	
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
		    			System.out.println("Parent <" + superN + "> not found");
		    		}
		    	} else {
		    		TreeNode copyNode = newTree.hasValue(modelN);
		    		if (copyNode == null) {
     		    		parent.addChild(modelN);
		    		}
		    	}
		    }
            //System.out.println("model Name = <"+ modelN + " , " + superN + ">"); 
		}
		//System.out.println("JSON RESULTANTE: <" + newTree.toJson(0) + ">");
    }// /public TreeQueryResults

    public String getQueryResult() {
        if (newTree == null) 
            return "";
        else
            return newTree.toJson(0);
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
