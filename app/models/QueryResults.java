package models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QueryResults {

    public String json;
    public ArrayList<Document> the_docs = new ArrayList<Document>();

    public QueryResults () {} 

    public QueryResults (String json) {
        this.json = json;
        //System.out.println(json);
        // create an ObjectMapper instance.
        ObjectMapper mapper = new ObjectMapper();
        // use the ObjectMapper to read the json string and create a tree
        JsonNode node = null;
		try {
			node = mapper.readTree(json);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//JsonNode documents = node.get("response").get("docs");
		
		JsonNode documents;
		
		//Surrounded with try catch to avoid null result set crashing the app
		try {
			documents = node.get("response").get("docs");
			Iterator<JsonNode> doc_iterator = documents.iterator();
			while (doc_iterator.hasNext()){
				JsonNode doc = doc_iterator.next();
				TreeMap<String, String> fields = new TreeMap<String, String>();
				
				Iterator<String> docFields = doc.fieldNames();
				while (docFields.hasNext()){
					String docField = docFields.next();
					if (docField.equals("characteristic")){
						continue;
					}
					fields.put(docField, doc.get(docField).asText());
					//System.out.println(docField);
				}
				
				List<JsonNode> characteristic = doc.findValues("characteristic");
				ArrayList<String> characteristics = new ArrayList<String>();
				for (JsonNode c : characteristic){
					Iterator<String> chars = c.fieldNames();
					while (chars.hasNext()){
						//TODO Rename this
						//TODO Figure out why I did this. Can't remember why it's here
						String the_thing = chars.next();
						//characteristics.add(the_thing);
						//System.out.println(the_thing);
					}
					System.out.println(c.toString());
					characteristics.add(c.toString());
				}
				the_docs.add(new Document(fields, characteristics));
			}
			//System.out.println(the_docs.size());
		} catch (Exception e){
			e.printStackTrace();
		}
    }
}
