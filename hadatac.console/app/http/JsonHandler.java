package http;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHandler {
    
    //private String field_count_url = "http://localhost:8983/solr/datasets/browse?q=*%3A*&wt=json&facet=true&rows=0";
	private String field_count_url = "http://jeffersontest.tw.rpi.edu/solr/datasets/browse?q=*%3A*&wt=json&facet=true&rows=0";
    public Map<String, HashMap<String, String>> categories_facets_and_counts = new HashMap<String, HashMap<String, String>>();
    public Map<String, ArrayList<String>> categories_and_facets = new HashMap<String, ArrayList<String>>();

    public JsonHandler() {}

    public Boolean getFieldCountJson() throws MalformedURLException, IOException{
    //public String getFieldCountJson() throws MalformedURLException, IOException{
        InputStream in = new URL( field_count_url ).openStream();
        String response = new String();
        try {
            response = IOUtils.toString( in );
        } 
        finally {
            IOUtils.closeQuietly(in);
        }
        // create an ObjectMapper instance.
        ObjectMapper mapper = new ObjectMapper();
        // use the ObjectMapper to read the json string and create a tree
        JsonNode node = mapper.readTree(response);
        JsonNode facet_fields = node.get("facet_counts").get("facet_fields");
        Iterator<JsonNode> field_count = facet_fields.iterator();
        Iterator<String> cat_name_it = facet_fields.fieldNames();

        String field_category = new String();    
        String facet;
        String count;

        while (field_count.hasNext()){
            ArrayList<String> the_list = new ArrayList<String>();
            JsonNode category = field_count.next();
            field_category = cat_name_it.next();
            
            Iterator<JsonNode> cat_it = category.iterator();

            //System.out.printf("Category: %s\n", field_category);
            
            while (cat_it.hasNext()){
                facet = cat_it.next().asText();
                count = cat_it.next().asText();
                //System.out.printf("   Facet %s - Count %s\n", facet, count);
                //HashMap <String, String> temp_map = new HashMap<String, String>();
                //temp_map.put(facet, count);
                //categories_facets_and_counts.put(field_category, temp_map);
                the_list.add(facet);
            }
            categories_and_facets.put(field_category, the_list);
        }
        //return categories_facets_and_counts;
        return true;
        //return field_category;
    }// getFieldCount()

}
