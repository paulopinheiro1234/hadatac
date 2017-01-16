package org.hadatac.console.models;

import org.apache.jena.base.Sys;
import org.hadatac.console.http.GetSparqlQuery;
import org.hadatac.utils.Collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ForceFieldQuery {
	String collectionSource = Collections.METADATA_SPARQL;
	List<AgentNode> agents = new ArrayList<AgentNode>();
	
	public ForceFieldQuery() {
		this(Collections.METADATA_SPARQL);
	}

	public ForceFieldQuery(String source) {
		collectionSource = source;
		System.out.println("Collection source in use: " + collectionSource);
		agents.add(new AgentNode("prov:Agent", "Public", AgentNode.AGENT, "", ""));
		addGroupNodes();
		addPersonNodes();
	}
	
	private JsonNode readTreeNodes(String tabQuery){
		SparqlQuery query = new SparqlQuery();
	    GetSparqlQuery query_submit = new GetSparqlQuery(collectionSource, query);
		System.out.println("Requested: <" + tabQuery + ">");
		String query_json = null;
	    try {
			query_json = query_submit.executeQuery(tabQuery);
			System.out.println("Here is the result: <" + query_json + ">");
		} catch (IllegalStateException e1) {
	        e1.printStackTrace();
		}
	
	    ObjectMapper mapper = new ObjectMapper();
	    // use the ObjectMapper to read the json string and create a tree
	    JsonNode rootNode = null;
		try {
			rootNode = mapper.readTree(query_json);
			System.out.println("Here is the size of results: <" + rootNode.size() + ">");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return rootNode;
	}
	
	private String getNodeValue(JsonNode node, String defaultValue) {
	    if (node != null && node.get("value") != null) {
	    	return node.get("value").asText();
	    } 
	    else {
	    	return defaultValue;
	    }
	}
	
	private void addGroupNodes(){
		JsonNode rootNode = readTreeNodes("GroupsH");
		JsonNode bindingsNode = rootNode.findPath("bindings");
		System.out.println("Here is the size of bindings: <" + bindingsNode.size() + ">");
		
		Iterator<JsonNode> elements = bindingsNode.elements();
		while (elements.hasNext()){			
		    JsonNode binding = elements.next();
		    String uri = getNodeValue(binding.findPath("agent"), "");
		    String name = getNodeValue(binding.findPath("name"), uri);
		    String email = getNodeValue(binding.findPath("email"), "");
		    String memberOf = getNodeValue(binding.findPath("group"), "");
		    agents.add(new AgentNode(uri, name, AgentNode.GROUP, email, memberOf));
		}
	}
	
	private void addPersonNodes(){
		JsonNode rootNode = readTreeNodes("PeopleH");
		JsonNode bindingsNode = rootNode.findPath("bindings");
		System.out.println("Here is the size of bindings: <" + bindingsNode.size() + ">");
		System.out.println(bindingsNode.toString());
		
		Iterator<JsonNode> elements = bindingsNode.elements();
		while (elements.hasNext()){
		    JsonNode binding = elements.next();
		    String uri = getNodeValue(binding.findPath("agent"), "");
			String name = getNodeValue(binding.findPath("name"), uri);
			String email = getNodeValue(binding.findPath("email"), "");
			String memberOf = getNodeValue(binding.findPath("group"), "");
		    agents.add(new AgentNode(uri, name, AgentNode.PERSON, email, memberOf));
		}
	}
	
	private int findAgentIndex(String uri) {
		Iterator<AgentNode> ag = agents.iterator();
		if (uri.equals("Public")){
			return 0;
		}
		while (ag.hasNext()) {
			AgentNode tmpAgent = ag.next();
			if (tmpAgent.getURI().equals(uri)){
				return agents.indexOf(tmpAgent);
			}
		}
		return -1;
	}
	
	private String toJson() {
		JSONObject tree = new JSONObject();
		
		JSONArray nodes = new JSONArray();
		Iterator<AgentNode> ag = agents.iterator();
		while (ag.hasNext()) {
			AgentNode tmpAgent = ag.next();
			JSONObject agent = new JSONObject();
			System.out.println(tmpAgent.getName());
			agent.put("name", tmpAgent.getName());
			if (tmpAgent.getEmail() != null && !tmpAgent.getEmail().equals("")) {
				agent.put("email", tmpAgent.getEmail());
			}
			agent.put("group", tmpAgent.getType() + 1);
			nodes.add(agent);
		}
		tree.put("nodes", nodes);
		
		JSONArray links = new JSONArray();
		ag = agents.iterator();
		while (ag.hasNext()) {
			AgentNode tmpAgent = ag.next();
			JSONObject edge = new JSONObject();
			System.out.println(tmpAgent.getName() + "=====");
			System.out.println(tmpAgent.getMemberOf() + "!!!!!");
			if (!tmpAgent.getMemberOf().equals("")) {
				int ind = findAgentIndex(tmpAgent.getMemberOf());
				if (ind == -1) {
					System.out.println("Invalid memberOf info for " + tmpAgent.getURI() + " under " + tmpAgent.getMemberOf());
				}
				else {
					edge.put("source", agents.indexOf(tmpAgent));
					edge.put("target", ind);
					edge.put("value", 4);
					links.add(edge);
					
				}
			}
		}
		tree.put("links", links);
		System.out.println(tree.toJSONString());
		
		return tree.toJSONString();
	}
	
    public String getQueryResult() {
    	if (agents.size() == 0){
    		return "";
    	}
    	else{
    		return toJson();
    	}
    } 
}
