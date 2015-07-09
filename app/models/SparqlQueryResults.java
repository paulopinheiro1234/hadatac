package models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SparqlQueryResults extends QueryResults {

    public String json;
    public ArrayList<TripleDocument> triples_list = new ArrayList<TripleDocument>();
    
    public ArrayList<String> vars = new ArrayList<String>();
    private int numVars;
    
    public SparqlQueryResults () {} 

    // This constructor assumes that json is a well-formed JSON string
    //  which also conforms to the SPARQL 1.1 Query Results JSON format:
    //  http://www.w3.org/TR/sparql11-results-json/ 
    public SparqlQueryResults (String json, String tabName) {
        this.json = json;
        //if(tabName.equals("InstrumentModels")) System.out.println(json);
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
		
		try {
		    while (parseResults.hasNext()){
				JsonNode doc = parseResults.next();
				TripleDocument triple = new TripleDocument(doc, vars, tabName);
				//System.out.println(triple);
				triples_list.add(triple);
			}
			//System.out.println(the_docs.size());
		} catch (Exception e){
			e.printStackTrace();
		}// /try/catch
    }// /SparqlQueryResults()
}

