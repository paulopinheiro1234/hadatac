package org.hadatac.console.controllers.restapi;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hadatac.utils.ApiUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.entity.pojo.Study;
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

    // test with https%3A%2F%2Fhbgd.tw.rpi.edu%2Fns%2FSUBJID
    // or https%3A%2F%2Fhbgd.tw.rpi.edu%2Fns%2FHAZ
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

    // TODO: finish this
    // test with http%3A%2F%2Fhadatac.org%2Fkb%2Fhbgd%23STD-CPP4
    // The DASchema will return ALL variables in the schema for the given study
    // We need to hit solr to make sure we only get variables for which there exist measurements
    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result getVariablesInStudy(String studyUri){
        // Step 1: query solr to see for what variables we have measurements
        //         and return the URI's for those variables
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
            System.out.println("[ERROR] RestApi.getVariablesByStudy() - Exception message: " + e.getMessage());
            return internalServerError(ApiUtil.createResponse("Encountered SOLR error!", false));
        }

        if (uris.isEmpty()){
            return notFound(ApiUtil.createResponse("No variables found in solr for study " + studyUri, false));
        }
        System.out.println("[GetVarsInStudy] Found " + uris.size() + " variables for this study");


        // Step 2: query blazegraph with those URI's to get details of those vars
        //         and return the results with those details as a RESTful response
        String classes = "";
        for (String s : uris) {
            classes += "<" + s + "> ";
        }

        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "select ?class ?label ?unit where {" + 
                "?class rdfs:label ?label ." +
                "?dasa hasco:hasAttribute ?class ." + 
                "?dasa hasco:hasUnit ?unit . " + 
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
            anode.add(temp);
        }// /parse sparql results
        System.out.println("[GetVarsInStudy] parsed " + anode.size() + " results into array");

        JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
        System.out.println("[GetVarsInStudy] done");
        return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getVariablesInStudy

    private List<String> parsePivotForVars (Pivot p) {
        List<String> results = new ArrayList<String>();
        String str = "";
        for (Pivot pivot_ent : p.children) {
            str = pivot_ent.value;
            results.add(str);
        }
        //System.out.println("[getVars parsePivotForVars] results: \n" + results);
        return results;
    }// /parsePivotForVars()


    // Given the study URI, return the URI's and types of all object collections
    // (not the full arrays of the collections themselves)
    public Result getOCsInStudy(String studyUri){
        
        return ok();
    }// /getOCsInStudy

}// /RestApi
