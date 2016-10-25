package org.hadatac.console.models;

import org.hadatac.console.http.GetSparqlQuery;
import org.hadatac.utils.Collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ForceFieldQuery {

	List<AgentNode> agents = new ArrayList<AgentNode>();

	public ForceFieldQuery() {
		this(Collections.METADATA_SPARQL);
	}
	
	public ForceFieldQuery(String collectionSource) {
		//Get query using http.GetSparqlQuery
	    SparqlQuery query = new SparqlQuery();
	    GetSparqlQuery query_submit = new GetSparqlQuery(collectionSource, query);

	    System.out.println("Collection source in use: " + collectionSource);
		String tabQuery = "OrganizationsH";
		agents.add(new AgentNode("prov:Agent", "Agent", AgentNode.AGENT, "", ""));
		System.out.println("REQUESTED: <" + tabQuery + ">");
		String query_json = null;
	    try {
			query_json = query_submit.executeQuery(tabQuery);
			System.out.println("Here is the result: <" + query_json + ">");
		} catch (IllegalStateException | IOException e1) {
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
	
		JsonNode bindingsNode = rootNode.findPath("bindings");
		System.out.println("Here is the size of bindings: <" + bindingsNode.size() + ">");
		Iterator<JsonNode> elements = bindingsNode.elements();
		
		String name = null;
		String uri = null;
		String email = null;
		String memberOf = null;
		while (elements.hasNext()){
			name = "";
			uri = "";
			memberOf = "";

			JsonNode binding = elements.next();
		    JsonNode uriNode = binding.findPath("agent");
		    if (uriNode != null && uriNode.get("value") != null) {
		    	uri = uriNode.get("value").asText();
		    }

			JsonNode nameNode = binding.findPath("name");
		    if (nameNode != null && nameNode.get("value") != null) {
		    	name = nameNode.get("value").asText();
		    } else {
		    	name = uri;
		    }

		    JsonNode emailNode = binding.findPath("email");
		    if (emailNode != null && emailNode.get("value") != null) {
		    	email = emailNode.get("value").asText();
		    }

		    JsonNode memberOfNode = binding.findPath("member");
		    if (memberOfNode != null && memberOfNode.get("value") != null) {
		    	memberOf = memberOfNode.get("value").asText();
		    }
		    		    
		    agents.add(new AgentNode(uri, name, AgentNode.ORGANIZATION, email, memberOf));
		    
		    System.out.println("agent = <"+ uri + " , " + name + " , " + memberOf + ">"); 
		}

		tabQuery = "PeopleH";
		System.out.println("Requested: <" + tabQuery + ">");
		query_json = null;
	    try {
			query_json = query_submit.executeQuery(tabQuery);
			System.out.println("Here is the result: <" + query_json + ">");
		} catch (IllegalStateException | IOException e1) {
	        e1.printStackTrace();
		}
	
	    mapper = new ObjectMapper();
	    // use the ObjectMapper to read the json string and create a tree
	    rootNode = null;
		try {
			rootNode = mapper.readTree(query_json);
			System.out.println("Here is the size of results: <" + rootNode.size() + ">");
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		//JsonNode phoneNosNode = rootNode.path("phoneNumbers");
		bindingsNode = rootNode.findPath("bindings");
		System.out.println("Here is the size of bindings: <" + bindingsNode.size() + ">");
		elements = bindingsNode.elements();
		
		name = null;
		uri = null;
		email = null;
		memberOf = null;
		while (elements.hasNext()){
			name = "";
			uri = "";
			memberOf = "";
		    JsonNode binding = elements.next();
		    
			JsonNode uriNode = binding.findPath("agent");
		    if (uriNode != null && uriNode.get("value") != null) {
		    	uri = uriNode.get("value").asText();
		    }

		    JsonNode nameNode = binding.findPath("name");
		    if (nameNode != null && nameNode.get("value") != null) {
		    	name = nameNode.get("value").asText();
		    } else {
		    	name = uri;
		    }

		    JsonNode emailNode = binding.findPath("email");
		    if (emailNode != null && emailNode.get("value") != null) {
		    	email = emailNode.get("value").asText();
		    }

		    JsonNode memberOfNode = binding.findPath("member");
		    if (memberOfNode != null && memberOfNode.get("value") != null) {
		    	memberOf = memberOfNode.get("value").asText();
		    }
		    		    
		    agents.add(new AgentNode(uri, name, AgentNode.PERSON, email, memberOf));
		    
		    System.out.println("agent = <"+ uri + " , " + name + " , " + email + " , " + memberOf + ">"); 
		}
		
		System.out.println("Json result: <" + toJson() + ">");
    }

	private int findAgentIndex(String uri) {
		Iterator<AgentNode> ag = agents.iterator();
		while (ag.hasNext()) {
			AgentNode tmpAgent = ag.next();
			if (tmpAgent.getURI().equals(uri)) 
				return agents.indexOf(tmpAgent);
		}
		return -1;
	}
	
	private String toJson() {
		String json = "{\n" +
	                  "   \"nodes\":[\n";
		AgentNode theAgent = null;
		Iterator<AgentNode> ag = agents.iterator();
		while (ag.hasNext()) {
			AgentNode tmpAgent = ag.next();
			if (tmpAgent.getType() == AgentNode.AGENT) {
				theAgent = tmpAgent;
			}
			json = json + "    {\"name\":\"" + tmpAgent.getName() + "\",";
			if (tmpAgent.getEmail() != null && !tmpAgent.getEmail().equals("")) {
				json = json + "    \"email\":\"" + tmpAgent.getEmail() + "\",";
			}
			json = json + " \"group\": " + (tmpAgent.getType() + 1) + " }";
			if (ag.hasNext()) {
				json = json + ",\n";				
			} else {
				json = json + "\n";
			}
		}
		json = json + 
				"   ],\n" +
                "   \"links\":[\n";				
		ag = agents.iterator();
		while (ag.hasNext()) {
			AgentNode tmpAgent = ag.next();
			if (tmpAgent.getType() != AgentNode.AGENT) {
				if (tmpAgent.getMemberOf().equals("")) {
					json = json + "    {\"source\": " + agents.indexOf(tmpAgent) + " ,";
					json = json + " \"target\": " + agents.indexOf(theAgent) + " ,";
					json = json + " \"value\": 4 }";					
				} else {
					int ind = findAgentIndex(tmpAgent.getMemberOf());
					if (ind == -1) {
						System.out.println("Invalid memberOf info for " + tmpAgent.getURI() + " under " + tmpAgent.getMemberOf());
					} else {
						json = json + "    {\"source\": " + agents.indexOf(tmpAgent) + " ,";
						json = json + " \"target\": " + ind + " ,";
						json = json + " \"value\": 4 }";
					}
				}
				if (ag.hasNext()) {
		    		json = json + ",\n";				
				} else {
		    		json = json + "\n";
				}
			}
		}
		json = json + 
				"   ]\n" +
				"}";
		return json;
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
