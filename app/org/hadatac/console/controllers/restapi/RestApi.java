package org.hadatac.console.controllers.restapi;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hadatac.utils.ApiUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.utils.State;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.console.models.Pivot;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import org.hadatac.console.controllers.AuthApplication;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;

import com.typesafe.config.ConfigFactory;
import play.mvc.Result;
import play.mvc.Controller;
import play.libs.Json;

public class RestApi extends Controller {

    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result getStudies(){
        ObjectMapper mapper = new ObjectMapper();
        List<Study> theStudies = Study.find();
        System.out.println("[RestApi] found " + theStudies.size() + " things");
        if(theStudies.size() == 0){
            return notFound(ApiUtil.createResponse("No studies found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(theStudies, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getStudies()

    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    // test with http%3A%2F%2Fhadatac.org%2Fkb%2Fhbgd%23STD-CPP4
    public Result getStudy(String studyUri){
        ObjectMapper mapper = new ObjectMapper();
        Study result = Study.find(studyUri);
        System.out.println("[RestAPI] type: " + result.getType());
        if(result == null || result.getType() == null || result.getType() == ""){
            return notFound(ApiUtil.createResponse("Study with name/ID " + studyUri + " not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(result, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getStudy()

    // test with http%3A%2F%2Fpurl.org%2Fhbgd%2Fkg%2Fns%2FSUBJID
    // or http%3A%2F%2Fpurl.org%2Fhbgd%2Fkg%2Fns%2FHAZ
    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result getVariable(String variableAttribute){
        ObjectMapper mapper = new ObjectMapper();
        List<DataAcquisitionSchemaAttribute> result = DataAcquisitionSchemaAttribute.findByAttribute(variableAttribute);
        //System.out.println("[RestAPI] attribute: " + result.getAttribute());
        if(result.size() == 0){
            return notFound(ApiUtil.createResponse("Variable with name/ID " + variableAttribute + " not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(result, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getVariable()

    // test with http%3A%2F%2Fhadatac.org%2Fkb%2Fhbgd%23STD-CPP4
    // The DASchema will return ALL variables in the schema for the given study
    // We need to hit solr to make sure we only get variables for which there exist measurements
    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result getVariablesInStudy(String studyUri){
        List<String> uris = getUsedVarsInStudy(studyUri);
        if(uris == null){
            return notFound(ApiUtil.createResponse("Encountered SOLR error!", false));
        }

        // Step 2: query blazegraph with those URI's to get details of those vars
        //         and return the results with those details as a RESTful response
        String classes = "";
        for (String s : uris) {
            classes += "<" + s + "> ";
        }

        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "select distinct ?class ?label ?unit ?shortname ?unitlabel where {" + 
                "?class rdfs:label ?label ." +
                "?class dcterms:identifier ?shortname ." + 
                "?dasa hasco:hasAttribute ?class ." + 
                "?dasa hasco:hasUnit ?unit . " + 
                "OPTIONAL { ?unit rdfs:label ?unitlabel . } " +
                "} values ?class { " + classes + "} ";
        //System.out.println("[GetVarsInStudy] sparql query\n" + sparqlQueryString);
		
		Query sparqlQuery = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), sparqlQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

        if(resultsrw.size() == 0){
            return notFound(ApiUtil.createResponse("No variables found in blazegraph for study " + studyUri, false));
        }
        
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();

        while(resultsrw.hasNext()){
            ObjectNode temp = mapper.createObjectNode();
			QuerySolution soln = resultsrw.next();
			if (soln.get("class") != null) {
				temp.put("uri", soln.get("class").toString());
			} else {
                System.out.println("[GetVarsInStudy] ERROR: Result returned without URI? Skipping....");
                continue;
            }
			if (soln.get("label") != null) {
				temp.put("label", soln.get("label").toString());
			}
			if (soln.get("unit") != null) {
				temp.put("unit", soln.get("unit").toString());
			}
			if (soln.get("unitlabel") != null) {
				temp.put("unitlabel", soln.get("unitlabel").toString());
			}
			if (soln.get("shortname") != null) {
				temp.put("shortname", soln.get("shortname").toString());
			}
            anode.add(temp);
        }// /parse sparql results
        System.out.println("[GetVarsInStudy] parsed " + anode.size() + " results into array");

        JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
        System.out.println("[GetVarsInStudy] done");
        return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getVariablesInStudy

    // This method takes the same solr query as above,
    // but instead queries the triple store for units instead of attributes
    public Result getUnitsInStudy(String studyUri){
        List<String> uris = getUsedVarsInStudy(studyUri);
        if(uris == null){
            return notFound(ApiUtil.createResponse("Encountered SOLR error!", false));
        }

        // Step 2: query blazegraph with those URI's to get details of those vars
        //         and return the results with those details as a RESTful response
        String classes = "";
        for (String s : uris) {
            classes += "<" + s + "> ";
        }

        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "select distinct ?uri ?label ?short where {" + 
                "?dasa hasco:hasAttribute ?class ." + 
                "?dasa hasco:hasUnit ?uri . " + 
                "?uri rdfs:label ?label ." +
                "OPTIONAL { ?uri obo:hasExactSynonym ?short . }" +
                "} values ?class { " + classes + "} ";
        //System.out.println("[GetVarsInStudy] sparql query\n" + sparqlQueryString);
		
		Query sparqlQuery = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), sparqlQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

        if(resultsrw.size() == 0){
            return notFound(ApiUtil.createResponse("No units found in blazegraph for study " + studyUri, false));
        }
        
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();

        while(resultsrw.hasNext()){
            ObjectNode temp = mapper.createObjectNode();
			QuerySolution soln = resultsrw.next();
			if (soln.get("uri") != null) {
				temp.put("uri", soln.get("uri").toString());
			} else {
                System.out.println("[GetUnitsInStudy] ERROR: Result returned without URI? Skipping....");
                continue;
            }
			if (soln.get("label") != null) {
				temp.put("label", soln.get("label").toString());
			}
			if (soln.get("short") != null) {
				temp.put("shortlabel", soln.get("short").toString());
			}
            anode.add(temp);
        }// /parse sparql results
        System.out.println("[GetUnitsInStudy] parsed " + anode.size() + " results into array");

        JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
        System.out.println("[GetUnitsInStudy] done");
        return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getUnitsInStudy

    // This is the same as the study-specific method,
    // but gets all of the variables for which we have
    // measurements in hadatac
    public Result getAllVariables(){
        List<String> uris = getAllUsedVars();
        if(uris == null){
            return notFound(ApiUtil.createResponse("Encountered SOLR error!", false));
        }

        // Step 2: query blazegraph with those URI's to get details of those vars
        //         and return the results with those details as a RESTful response
        String classes = "";
        for (String s : uris) {
            classes += "<" + s + "> ";
        }

        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "select distinct ?class ?label ?unit ?shortname ?unitlabel where {" + 
                "?class rdfs:label ?label ." +
                "?class dcterms:identifier ?shortname ." + 
                "?dasa hasco:hasAttribute ?class ." + 
                "?dasa hasco:hasUnit ?unit . " + 
                "OPTIONAL { ?unit rdfs:label ?unitlabel . } " +
                "} values ?class { " + classes + "} ";
        //System.out.println("[GetAllVars] sparql query\n" + sparqlQueryString);
		
		Query sparqlQuery = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), sparqlQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

        if(resultsrw.size() == 0){
            return notFound(ApiUtil.createResponse("No variables found in blazegraph", false));
        }
        
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();

        while(resultsrw.hasNext()){
            ObjectNode temp = mapper.createObjectNode();
			QuerySolution soln = resultsrw.next();
			if (soln.get("class") != null) {
				temp.put("uri", soln.get("class").toString());
			} else {
                System.out.println("[GetAllVars] ERROR: Result returned without URI? Skipping....");
                continue;
            }
			if (soln.get("label") != null) {
				temp.put("label", soln.get("label").toString());
			}
			if (soln.get("unit") != null) {
				temp.put("unit", soln.get("unit").toString());
			}
			if (soln.get("unitlabel") != null) {
				temp.put("unitlabel", soln.get("unitlabel").toString());
			}
			if (soln.get("shortname") != null) {
				temp.put("shortname", soln.get("shortname").toString());
			}
            anode.add(temp);
        }// /parse sparql results
        System.out.println("[GetAllVars] parsed " + anode.size() + " results into array");

        JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
        System.out.println("[GetAllVars] done");
        return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getAllVariables

    // Mirrors the above, but returns Unit details instead of Variable details
    public Result getAllUnits(){
                List<String> uris = getAllUsedVars();
        if(uris == null){
            return notFound(ApiUtil.createResponse("Encountered SOLR error!", false));
        }

        // Step 2: query blazegraph with those URI's to get details of those vars
        //         and return the results with those details as a RESTful response
        String classes = "";
        for (String s : uris) {
            classes += "<" + s + "> ";
        }

        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "select distinct ?uri ?label ?short where {" + 
                "?dasa hasco:hasAttribute ?class ." + 
                "?dasa hasco:hasUnit ?uri . " + 
                "?uri rdfs:label ?label ." +
                "OPTIONAL { ?uri obo:hasExactSynonym ?short . }" +
                "} values ?class { " + classes + "} ";
        //System.out.println("[GetAllUnits] sparql query\n" + sparqlQueryString);
		
		Query sparqlQuery = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), sparqlQuery);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

        if(resultsrw.size() == 0){
            return notFound(ApiUtil.createResponse("No units found in blazegraph", false));
        }
        
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();

        while(resultsrw.hasNext()){
            ObjectNode temp = mapper.createObjectNode();
			QuerySolution soln = resultsrw.next();
			if (soln.get("uri") != null) {
				temp.put("uri", soln.get("uri").toString());
			} else {
                System.out.println("[GetAllUnits] ERROR: Result returned without URI? Skipping....");
                continue;
            }
			if (soln.get("label") != null) {
				temp.put("label", soln.get("label").toString());
			}
			if (soln.get("short") != null) {
				temp.put("shortlabel", soln.get("short").toString());
			}
            anode.add(temp);
        }// /parse sparql results
        System.out.println("[GetAllUnits] parsed " + anode.size() + " results into array");

        JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
        System.out.println("[GetAllUnits] done");
        return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getAllUnits


    // query solr to see for what variables we have measurements
    // and return the URI's for those variables
    private List<String> getUsedVarsInStudy(String studyUri){
        SolrQuery solrQuery = new SolrQuery();
        // restrict to the study provided by parameter
        solrQuery.setQuery("study_uri_str:\"" + studyUri + "\"");
        //System.out.println("[GetVarsInStudy] query = " + query.getQuery());
        solrQuery.setRows(0);
        solrQuery.setFacet(true);
        solrQuery.setFacetLimit(-1);
        solrQuery.setParam("json.facet", "{ "
                + "vars:{ "
                + "type: terms, "
                + "field: characteristic_uri_str, "
            // limit is important - the default if this is left out is only 10 facets
                + "limit: 1000}}");
        List<String> uris = new ArrayList<String>();

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + CollectionUtil.DATA_ACQUISITION).build();
            QueryResponse queryResponse = solr.query(solrQuery, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            uris = parsePivotForVars(pivot);
        } catch (Exception e) {
            System.out.println("[ERROR] RestApi.getUsedVarsInStudy() - Exception message: " + e.getMessage());
            return null;
        }
        System.out.println("[getUsedVarsInStudy] Found " + uris.size() + " variables for this study");
        return uris;
    }// /getUsedVarsInStudy()

    // as above, we're going to restrict to all variables used in measurements
    // Limit here is increased to 5000
    private List<String> getAllUsedVars(){
        SolrQuery solrQuery = new SolrQuery();
        //System.out.println("[GetVarsInStudy] query = " + query.getQuery());
        solrQuery.setRows(0);
        solrQuery.setFacet(true);
        solrQuery.setFacetLimit(-1);
        solrQuery.setParam("json.facet", "{ "
                + "vars:{ "
                + "type: terms, "
                + "field: characteristic_uri_str, "
            // limit is important - the default if this is left out is only 10 facets
                + "limit: 5000}}");
        List<String> uris = new ArrayList<String>();

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + CollectionUtil.DATA_ACQUISITION).build();
            QueryResponse queryResponse = solr.query(solrQuery, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            uris = parsePivotForVars(pivot);
        } catch (Exception e) {
            System.out.println("[ERROR] RestApi.getUsedVarsInStudy() - Exception message: " + e.getMessage());
            return null;
        }
        System.out.println("[getAllUsedVars] Found " + uris.size() + " variables with measurements");
        return uris;
    }// getAllUsedVars

    private List<String> parsePivotForVars (Pivot p) {
        List<String> results = new ArrayList<String>();
        String str = "";
        for (Pivot pivot_ent : p.children) {
            str = pivot_ent.getValue();
            results.add(str);
        }
        //System.out.println("[getVars parsePivotForVars] results: \n" + results);
        return results;
    }// /parsePivotForVars()


    //public Result getIndicatorTypes(){
    //}


    // Given the study URI, return the URI's and types of all object collections
    // (not the full arrays of the collections themselves)
    // Returning the ObjectCollection pojo includes an array of the included objects
    //    themselves, so to avoid overload for the extremely large OC's, we parse the
    //    query results into a smaller JSON object with URI and TYPE URI.
    public Result getOCListInStudy(String studyUri){
        ObjectMapper mapper = new ObjectMapper();
        if (studyUri == null) {
            return notFound(ApiUtil.createResponse("No study specified", false));
        }
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri ?ocType WHERE { \n" + 
                "   ?ocType rdfs:subClassOf+ hasco:ObjectCollection . \n" +
                "   ?uri a ?ocType . \n" +
                "   ?uri hasco:isMemberOf <" + studyUri + "> . \n" +
                " } ";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), query);
        ResultSet results = qexec.execSelect();
        ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
        ArrayNode anode = mapper.createArrayNode();

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            ObjectNode temp = mapper.createObjectNode();
            if (soln.getResource("uri").getURI() != null && soln.getResource("ocType") != null) { 
    			temp.put("uri", soln.get("uri").toString());
    			temp.put("ocType", soln.get("ocType").toString());
    		} else {
                System.out.println("[GetUnitsInStudy] ERROR: Result returned without URI? Skipping....");
                continue;
            }
            anode.add(temp);
        }

        System.out.println("[getOCListInStudy] parsed " + anode.size() + " results into array");
        System.out.println("[getOCListInStudy] done");
        
        if(anode.size() < 1){
            return notFound(ApiUtil.createResponse("No object collections found for study " + studyUri, false));
        } else {
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getOCsInStudy


    // test with http%3A%2F%2Fhadatac.org%2Fkb%2Fhbgd%23CH-CPP4
    // Given the study URI, return the URI's of all StudyObjects that are subjects in that study
    public Result getObjectCollection(String ocUri){
        ObjectMapper mapper = new ObjectMapper();
        ObjectCollection result = ObjectCollection.find(ocUri);
        //System.out.println("[RestAPI] OC type: " + result.getType());
        if(result == null){
            return notFound(ApiUtil.createResponse("ObjectCollection with uri " + ocUri + " not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(result, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getObjectCollection


    public Result getOCSize(String ocUri){
        ObjectMapper mapper = new ObjectMapper();
        ObjectCollection objs = ObjectCollection.find(ocUri);
        if(objs == null){
            return notFound(ApiUtil.createResponse("ObjectCollection with uri " + ocUri + " not found", false));
        }
        int totalResultSize = objs.getCollectionSize();
        ObjectNode res = mapper.createObjectNode();
        res.put("size", totalResultSize);
        return ok(ApiUtil.createResponse(res, true));
    }


    //total_count, page, size
    public Result getObjectsInCollection(String ocUri, int offset){
        ObjectMapper mapper = new ObjectMapper();
        int pageSize = 250;
        ObjectCollection objs = ObjectCollection.find(ocUri);
        if(objs == null){
            return notFound(ApiUtil.createResponse("ObjectCollection with uri " + ocUri + " not found", false));
        }
        int totalResultSize = objs.getCollectionSize();
        if(totalResultSize < 1){
            return notFound(ApiUtil.createResponse("ObjectCollection with uri " + ocUri + " not found", false));
        }
        if(pageSize > 250 || pageSize < 1) {
            pageSize = 250;
            System.out.println("[RestAPI] getObjectsInCollection : Yikes! Resetting that page size for you!");
        }
        List<StudyObject> results = StudyObject.findByCollectionWithPages(objs, pageSize, offset);
        if(results == null){
            return notFound(ApiUtil.createResponse("ObjectCollection with URI " + ocUri + "not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getObjectsInCollection

}// /RestApi
