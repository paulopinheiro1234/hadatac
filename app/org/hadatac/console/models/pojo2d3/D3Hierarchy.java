package org.hadatac.console.models.pojo2d3;

import java.util.Iterator;

import org.hadatac.console.models.TreeNode;

import com.fasterxml.jackson.databind.JsonNode;

public class D3Hierarchy {

	public static String generate(JsonNode bindings, boolean usingURIs) {
		String json = "";
        TreeNode newTree = null;
        Iterator<JsonNode> elements = bindings.elements();
        String modelN = null;
        String superN = null;
        while (elements.hasNext()){
            modelN = "";
		    superN = "";
            JsonNode binding = elements.next();
            JsonNode modelNameNode = binding.findPath("id");
            if (modelNameNode != null && modelNameNode.get("value") != null) {
                modelN = modelNameNode.get("value").asText();
            }
		    JsonNode superNameNode = binding.findPath("superId");
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
            json = "";
        else
            json = newTree.toJson(0);
        return json;
	}// /buildTreeQueryResults
	
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
