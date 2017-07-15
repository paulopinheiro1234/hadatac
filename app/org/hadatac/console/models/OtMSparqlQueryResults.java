package org.hadatac.console.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import org.hadatac.console.controllers.metadata.DynamicFunctions;

import java.util.Iterator;
//import javax.swing.tree.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OtMSparqlQueryResults{

    public TreeMap<String,OtMTripleDocument> sparqlResults;
    public Map<String,String> labelMap = new HashMap<String,String>();
    public String treeResults;
    public String json;
    
    private ArrayList<String> vars = new ArrayList<String>();
    private int numVars;
    private TreeNode newTree;
    
    public OtMSparqlQueryResults() {}
    
    public OtMSparqlQueryResults(String json_result){
        
        this.json = json_result;
        ObjectMapper mapper = new ObjectMapper();
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
	numVars = vars.size();

        if(vars.contains("id") && vars.contains("superId"))
            buildTreeQueryResults(bindings);
	else 
	    this.treeResults = "";
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
                if (doc.has("id"))
                    this.sparqlResults.put(triple.get("id").get(0),triple);
                else
                    this.sparqlResults.put(triple.generateID(), triple);
            } catch (Exception e){
		e.printStackTrace();
	    }
	}
    }
    
    public OtMSparqlQueryResults(String json_result, boolean usingURIs){
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
    
    public OtMSparqlQueryResults(String json_result, boolean usingURIs, String tabName){
        this.json = json_result;
        //System.out.println("INPUT JSON:\n" + this.json);
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
            buildTreeQueryResults(bindings, usingURIs, tabName);
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
    
    private void buildTreeQueryResults(JsonNode bindings){
	this.newTree = null;
        Iterator<JsonNode> elements = bindings.elements();
        String modelN = null;
        String superN = null;
        ArrayList<TreeNode> branchCollection = new ArrayList<TreeNode>();
        while (elements.hasNext()){
            modelN = "";
	    superN = "";
            JsonNode binding = elements.next();
            JsonNode modelNameNode = binding.findPath("id");
            if (modelNameNode != null && modelNameNode.get("value") != null) {
                modelN = modelNameNode.get("value").asText();
		if (binding.findPath("label") != null && binding.findPath("label").get("value") != null) {
		    labelMap.put(modelN, binding.findPath("label").get("value").asText());
		}
	    }
	    JsonNode superNameNode = binding.findPath("superId");
            if (superNameNode != null && superNameNode.get("value") != null) {
                superN = superNameNode.get("value").asText();
            }
            //if (!modelN.equals("")) {
            //    modelN = prettyFromURI(modelN);
            //    modelN = DynamicFunctions.replaceURLWithPrefix(modelN);
            //}
            //if (!superN.equals("")) {
            //    superN = prettyFromURI(superN);
            //    superN = DynamicFunctions.replaceURLWithPrefix(superN);
            //}
            TreeNode currentBranch = new TreeNode(superN);
            currentBranch.addChild(modelN);
            branchCollection.add(currentBranch);
	}
        TreeNode resultsTree = new TreeNode("Empty");
        ArrayList<TreeNode> assignedBranches = new ArrayList<TreeNode>();
        int numIterations = 0;
        int maxIterations = 20;
        while (assignedBranches.size()!=branchCollection.size() && numIterations<maxIterations){
	    numIterations++;
	    for (TreeNode tn : branchCollection){
		if (!assignedBranches.contains(tn)){
		    if (resultsTree.getName().equals("Empty")) {
			resultsTree = new TreeNode(tn.getName());
			resultsTree.addChild(tn.getChildren().get(0));
			assignedBranches.add(tn);
		    } else {
			if (resultsTree.hasValue(tn.getName())!=null){
			    TreeNode branchOfInterest = resultsTree.hasValue(tn.getName());
			    branchOfInterest.addChild(tn.getChildren().get(0));
			    assignedBranches.add(tn);
			} else {
			    if (tn.hasValue(resultsTree.getName())!=null) {
				TreeNode newBranch = new TreeNode(tn.getName());
				newBranch.addChild(resultsTree);
				resultsTree = newBranch;
				assignedBranches.add(tn);
			    } 
			}
		    }
		}
	    }
	}
	addLabels(resultsTree,true);
	addLabels(resultsTree,false);
	this.treeResults = resultsTree.toJson(0);
    }
    
    public void addLabels(TreeNode aNode, boolean isReset) {
	String name = aNode.getName();
	if (isReset) {
	    aNode.setFirstVisit(true);
	} else {
	    if (aNode.getFirstVisit()) {
		aNode.setName(finalName(name));
		aNode.setFirstVisit(false);
	    }
	}
	if(aNode.getChildren().size() == 0) {
	    return;
	}
	for(int i = 0 ; i < aNode.getChildren().size() ; i++) {
	    addLabels(aNode.getChildren().get(i),isReset);
	}
    }
    
    private String finalName (String currentName) {
	if (currentName == null || currentName.equals("")) {
	    return "";
	}
	String newName = prettyFromURI(currentName);
	newName = "[" + DynamicFunctions.replaceURLWithPrefix(newName) + "]";
	if (labelMap.get(currentName) != null) {
	    newName = labelMap.get(currentName) + " " + newName;
	    return newName;
	}
	return newName;
    }	

    // This is the same as SparqlQueryResults regardless of whether the
    //    properties are one-to-one or one-to-many
    private void buildTreeQueryResults(JsonNode bindings, boolean usingURIs){
	this.newTree = null;
        Iterator<JsonNode> elements = bindings.elements();
        String modelN = null;
        String superN = null;
        ArrayList<TreeNode> branchCollection = new ArrayList<TreeNode>();
        while (elements.hasNext()){
            modelN = "";
	    superN = "";
            JsonNode binding = elements.next();
            //System.out.println("Binding: " + binding + "\n");
            JsonNode modelNameNode = binding.findPath("id");
            //System.out.println("modelNameNode: " + modelNameNode + "\n");
            if (modelNameNode != null && modelNameNode.get("value") != null) {
                modelN = modelNameNode.get("value").asText();
            }
	    JsonNode superNameNode = binding.findPath("superId");
	    //System.out.println("superNameNode: " + superNameNode + "\n");
            if (superNameNode != null && superNameNode.get("value") != null) {
                superN = superNameNode.get("value").asText();
                //System.out.println("superN: " + superN + "\n");
            }
            if (usingURIs && ! modelN.equals("")) {
                modelN = prettyFromURI(modelN);
                modelN = DynamicFunctions.replaceURLWithPrefix(modelN);
                //System.out.println("modelN: " + modelN + "\n");
            }
            if (usingURIs && ! superN.equals("")) {
                superN = prettyFromURI(superN);
                superN = DynamicFunctions.replaceURLWithPrefix(superN);
                //System.out.println("usingURIs superN: " + superN + "\n");
            }
            //System.out.println("model Name = <"+ modelN + " , " + superN + ">"); 
            TreeNode currentBranch = new TreeNode(superN);
            currentBranch.addChild(modelN);
            branchCollection.add(currentBranch);
        }// /while
        TreeNode resultsTree = new TreeNode("Empty");
        ArrayList<TreeNode> assignedBranches = new ArrayList<TreeNode>();
        int numIterations = 0;
        int maxIterations = 20;
        while (assignedBranches.size()!=branchCollection.size() && numIterations<maxIterations){
	    numIterations++;
	    for (TreeNode tn : branchCollection){
		if (assignedBranches.contains(tn)){
		    //System.out.println("Current Branch Already Assigned");
		} else {
		    //System.out.println("Branch: " + tn.toJson(0) + "\n");
		    if (resultsTree.getName().equals("Empty")) {
			resultsTree = new TreeNode(tn.getName());
			resultsTree.addChild(tn.getChildren().get(0));
			assignedBranches.add(tn);
		    } else {
			//System.out.println("Not Empty!!!\n");
			if (resultsTree.hasValue(tn.getName())!=null){
			    //System.out.println("Current Branch child of Tree");
			    TreeNode branchOfInterest = resultsTree.hasValue(tn.getName());
			    //System.out.println("Before: " + branchOfInterest.toJson(0) + "\n");
			    branchOfInterest.addChild(tn.getChildren().get(0));
			    //System.out.println("After: " + branchOfInterest.toJson(0) + "\n");
			    assignedBranches.add(tn);
			} else {
			    if (tn.hasValue(resultsTree.getName())!=null) {
				//System.out.println("Tree child of Current Branch\n");
				TreeNode newBranch = new TreeNode(tn.getName());
				newBranch.addChild(resultsTree);
				resultsTree = newBranch;
				assignedBranches.add(tn);
			    } else {
				//System.out.println("Else, Else\n");
			    }
			}
		    }
		}
	    }
        }
        //System.out.println("Results Tree: " + resultsTree.toJson(0));
        this.treeResults = resultsTree.toJson(0);
        //System.out.println("Tree Results: " + this.treeResults);
    }// /buildTreeQueryResults
    
    // This is the same as SparqlQueryResults regardless of whether the
    //    properties are one-to-one or one-to-many
    private void buildTreeQueryResults(JsonNode bindings, boolean usingURIs, String tabName){
        this.newTree = null;
        Iterator<JsonNode> elements = bindings.elements();
        String modelN = null;
        String superN = null;
        ArrayList<TreeNode> branchCollection = new ArrayList<TreeNode>();
        TreeNode topNode = null;
        while (elements.hasNext()){
            modelN = "";
	    superN = "";
            JsonNode binding = elements.next();
            //System.out.println("Binding: " + binding + "\n");
            JsonNode modelNameNode = binding.findPath("id");
            //System.out.println("modelNameNode: " + modelNameNode + "\n");
            if (modelNameNode != null && modelNameNode.get("value") != null) {
                modelN = modelNameNode.get("value").asText();
            }
	    JsonNode superNameNode = binding.findPath("superId");
	    //System.out.println("superNameNode: " + superNameNode + "\n");
            if (superNameNode != null && superNameNode.get("value") != null) {
                superN = superNameNode.get("value").asText();
                //System.out.println("superN: " + superN + "\n");
            }
            if (usingURIs && ! modelN.equals("")) {
                modelN = prettyFromURI(modelN);
                modelN = DynamicFunctions.replaceURLWithPrefix(modelN);
                //System.out.println("modelN: " + modelN + "\n");
            }
            if (usingURIs && ! superN.equals("")) {
                superN = prettyFromURI(superN);
                superN = DynamicFunctions.replaceURLWithPrefix(superN);
                //System.out.println("usingURIs superN: " + superN + "\n");
            }
            //System.out.println("model Name = <"+ modelN + " , " + superN + ">");
            TreeNode currentBranch = new TreeNode(superN);
            currentBranch.addChild(modelN);
            if (binding.findPath("label").get("value").toString().replace(" ","").replace(",","").equals("\"" + tabName + "\"")){
            	System.out.println("Found Top Level Branch: " + tabName + "\n");
            	topNode = currentBranch;
            } else {
                branchCollection.add(currentBranch);
            }
        }// /while
        TreeNode resultsTree = new TreeNode("Empty");
        ArrayList<TreeNode> assignedBranches = new ArrayList<TreeNode>();
        int numIterations = 0;
        int maxIterations = 20;
        while (assignedBranches.size()!=branchCollection.size() && numIterations<maxIterations){
	    numIterations++;
	    for (TreeNode tn : branchCollection){
		if (assignedBranches.contains(tn)){
		    //System.out.println("Current Branch Already Assigned");
		} else {
		    //System.out.println("Branch: " + tn.toJson(0) + "\n");
		    if (topNode != null) {
			resultsTree = new TreeNode(topNode.getName());
			resultsTree.addChild(topNode.getChildren().get(0));
		    } else {
			System.out.println("Warning: No node matching tab name found!\n");
		    }
		    if (resultsTree.getName().equals("Empty")) {
			resultsTree = new TreeNode(tn.getName());
			resultsTree.addChild(tn.getChildren().get(0));
			assignedBranches.add(tn);
		    } else {
			//System.out.println("Not Empty!!!\n");
			if (resultsTree.hasValue(tn.getName())!=null){
			    //System.out.println("Current Branch child of Tree");
			    TreeNode branchOfInterest = resultsTree.hasValue(tn.getName());
			    //System.out.println("Before: " + branchOfInterest.toJson(0) + "\n");
			    branchOfInterest.addChild(tn.getChildren().get(0));
			    //System.out.println("After: " + branchOfInterest.toJson(0) + "\n");
			    assignedBranches.add(tn);
			} else {
			    if (tn.hasValue(resultsTree.getName())!=null) {
				//System.out.println("Tree child of Current Branch\n");
				TreeNode newBranch = new TreeNode(tn.getName());
				newBranch.addChild(resultsTree);
				resultsTree = newBranch;
				assignedBranches.add(tn);
			    } else {
				//System.out.println("Else, Else: Current Branch Not Yet Assigned\n");
			    }
			}
		    }
		}
	    }
        }
        //System.out.println("Results Tree: " + resultsTree.toJson(0));
        this.treeResults = resultsTree.toJson(0);
        /*if (newTree == null) 
	  this.treeResults = "";
	  else
	  this.treeResults = newTree.toJson(0);*/
	//        System.out.println("New Tree : " + newTree.toJson(0) + "\n");
        //System.out.println("Tree Results: " + this.treeResults);
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
