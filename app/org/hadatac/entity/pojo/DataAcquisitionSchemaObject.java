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

public class DataAcquisitionSchemaObject {

        private String uri;
    	private String entity;
        private String role;
    	private String inRelationTo;
    	private String relation;
    	
        public DataAcquisitionSchemaObject(String uri, String entity, String role, String inRelationTo, String relation) {
	    this.uri = uri;
	    this.entity = entity;
	    this.role = role;
	    this.inRelationTo = inRelationTo;
	    this.relation = relation;
	}
    	
        public String getUri() {
	    return uri;
	}

    	public String getEntity() {
	    return entity;
	}

    	public String getRole() {
	    return role;
	}

    	public String getInRelationTo() {
	    return inRelationTo;
	}

    	public String getRelation() {
	    return relation;
	}

        public static List<DataAcquisitionSchemaObject> findBySchema (String schemaUri) {
	    //System.out.println("Looking for data acuisition schema objectss for " + schemaUri);
	     List<DataAcquisitionSchemaObject> objects = new ArrayList<DataAcquisitionSchemaObject>();
    	     String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    			"SELECT ?uri ?hasEntity ?hasRole ?inRelationTo ?relation WHERE { " + 
    			"   ?uri a hasco:DASchemaObject . " + 
    			"   ?uri hasco:partOfSchema " + schemaUri + " .  " + 
    			"   ?uri hasco:hasEntity ?hasEntity  ." + 
    			"   OPTIONAL { ?uri hasco:hasRole ?hasRole } .  " + 
    			"   OPTIONAL { ?uri sio:inRelationTo ?inRelationTo } ." + 
    			"   OPTIONAL { ?uri sio:relation ?relation } ." + 
    			"}";
    	     Query query = QueryFactory.create(queryString);
		
    	     QueryExecution qexec = QueryExecutionFactory.sparqlService(
		    Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	     ResultSet results = qexec.execSelect();
	     ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	     qexec.close();

	     if (!resultsrw.hasNext()) {
		   System.out.println("[WARNING] DataAcquisitionSchemaObject. Could not find objects for schema: " + schemaUri);
		   return objects;
	     }
		
	     while (resultsrw.hasNext()) {
		  QuerySolution soln = resultsrw.next();
		  String hasRoleStr = "";
		  String inRelationToStr = "";
		  String relationStr = "";
		  try {
			if (soln != null && soln.getResource("uri") != null && soln.getResource("uri").getURI() != null &&
			    soln.getResource("hasEntity") != null && soln.getResource("hasEntity").getURI() != null) {

			    try {
				if (soln.getResource("hasRole") != null && soln.getResource("hasRole").getURI() != null) {
				    hasRoleStr = soln.getResource("hasRole").getURI();
				} 
			    } catch (Exception e1) {
				hasRoleStr = "";
			    }
			    				    
			    try {
				if (soln.getResource("inRelationTo") != null && soln.getResource("inRelationTo").getURI() != null) {
				    inRelationToStr = soln.getResource("inRelationTo").getURI();
				}
			    } catch (Exception e1) {
				inRelationToStr = "";
			    }
			    				    
			    try {
				if (soln.getResource("relation") != null && soln.getResource("relation").getURI() != null) {
				    relationStr = "";
				}
			    } catch (Exception e1) {
				relationStr = "";
			    }
			    				    

			    System.out.println("daso role in dataacquisitionschema: " + hasRoleStr);
			    DataAcquisitionSchemaObject obj = new DataAcquisitionSchemaObject(
					soln.getResource("uri").getURI(),
					soln.getResource("hasEntity").getURI(),
					hasRoleStr,
					inRelationToStr,
					relationStr);
			    objects.add(obj);
			}
		  }  catch (Exception e) {
			System.out.println("[ERROR] DataAcquisitionSchemaObject. uri: e.Message: " + e.getMessage());
                  }

	     }
	     return objects;
	}
    
}
