package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class DataAcquisitionSchemaEvent {

        private String uri;
    	private String entity;
        private String label;
    	
    public DataAcquisitionSchemaEvent(String uri, String entity, String label) {
	    this.uri = uri;
	    this.entity = entity;
	    this.label = label;
	}
    	
        public String getUri() {
	    return uri;
	}

    	public String getEntity() {
	    return entity;
	}

        public String getLabel() {
            return label;
        }

        public static List<DataAcquisitionSchemaEvent> findBySchema (String schemaUri) {
	    //System.out.println("Looking for data acuisition schema events for " + schemaUri);
	     List<DataAcquisitionSchemaEvent> objects = new ArrayList<DataAcquisitionSchemaEvent>();
    	     String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    			"SELECT ?uri ?hasEntity ?label WHERE { " + 
    			"   ?uri a hasco:DASchemaEvent . " + 
    			"   ?uri hasco:partOfSchema " + schemaUri + " .  " + 
    			"   ?uri hasco:hasEntity ?hasEntity  ." + 
    			"   OPTIONAL { ?hasEntity  rdfs:label ?label } ." + 
    			"}";
    	     Query query = QueryFactory.create(queryString);
		
    	     QueryExecution qexec = QueryExecutionFactory.sparqlService(
		    Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	     ResultSet results = qexec.execSelect();
	     ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	     qexec.close();

	     if (!resultsrw.hasNext()) {
		   System.out.println("[WARNING] DataAcquisitionSchemaEvent. Could not find events for schema: " + schemaUri);
		   return objects;
	     }
		
	     while (resultsrw.hasNext()) {
		  QuerySolution soln = resultsrw.next();
		  String entityStr = "";
		  String entityLabelStr = "";
		  try {
			if (soln != null && soln.getResource("uri") != null && soln.getResource("uri").getURI() != null &&
			    soln.getResource("hasEntity") != null && soln.getResource("hasEntity").getURI() != null) {
			    
			    entityStr = soln.getResource("hasEntity").getURI();

			    entityLabelStr = DataAcquisitionSchema.getFirstLabel(entityStr); 
			    if (entityLabelStr.equals("")) {
				entityLabelStr = entityStr.substring(entityStr.indexOf("#") + 1);
			    }

			    DataAcquisitionSchemaEvent obj = new DataAcquisitionSchemaEvent(
					soln.getResource("uri").getURI(),
					entityStr,
					entityLabelStr);
			    objects.add(obj);
			}
		  }  catch (Exception e) {
			System.out.println("[ERROR] DataAcquisitionSchemaEvent. uri: e.Message: " + e.getMessage());
                  }

	     }
	     return objects;
	}
    
}
