package org.hadatac.console.controllers.restapi;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.entity.pojo.*;
import org.hadatac.entity.pojo.STR;
import org.hadatac.utils.ApiUtil;
import org.hadatac.utils.HASCO;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.console.models.Pivot;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SolrUtils;

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

    private final static int OBJ = 0;
    private final static int DPL = 1;

    //****************
// UTILITY METHODS
//****************
    // Utility methods for formatting the "values ?class { <stuff> }" part of the blazegraph queries
    private String formatQueryValues(List<String> uris){
        String classes = "";
        for (String s : uris) {
            classes += "<" + s + "> ";
        }
        return classes;
    }
    private String formatQueryValues(String uri){
        if(uri == null) return null;
        else return "<" + uri + "> ";
    }

    // Utilty method for parsing blazegraph query results
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


    //*******************
// SOLR QUERY METHODS
//*******************
    // query solr to see for what variables we have measurements
    // and return the URI's for those variables
    private List<String> getUsedVarsInStudy(String studyUri){
        SolrQuery solrQuery = new SolrQuery();
        // restrict to the study provided by parameter
        System.out.println("[RespApi.getUsedVarsInStudy] Study URI: " + studyUri);
        solrQuery.setQuery("study_uri_str:\"" + studyUri + "\"");
        solrQuery.setRows(0);
        solrQuery.setFacet(true);
        solrQuery.setFacetLimit(-1);
        solrQuery.setParam("json.facet", "{ "
                + "vars:{ "
                + "type: terms, "
                + "field: characteristic_uri_str, "
                // limit is important - the default is only 10 facets (variables) if this is left out
                + "limit: 1000}}");
        // parse results
        List<String> uris = new ArrayList<String>();
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
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
        solrQuery.setQuery("*:*");
        solrQuery.setRows(0);
        solrQuery.setFacet(true);
        solrQuery.setFacetLimit(-1);
        solrQuery.setParam("json.facet", "{ "
                + "vars:{ "
                + "type: terms, "
                + "field: characteristic_uri_str, "
                // limit is important - the default is only 10 facets (variables) if this is left out
                + "limit: 5000}}");
        //System.out.println("[getAllUsedVars] solrQuery = " + solrQuery);
        // parse results
        List<String> uris = new ArrayList<String>();
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(solrQuery, SolrRequest.METHOD.POST);
            //System.out.println("[getAllUsedVars] res: " + queryResponse);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            uris = parsePivotForVars(pivot);
        } catch (Exception e) {
            System.out.println("[ERROR] RestApi.getAllUsedVars() - Exception message: " + e.getMessage());
            return null;
        }
        System.out.println("[getAllUsedVars] Found " + uris.size() + " variables with measurements");
        return uris;
    }// getAllUsedVars

    //"study_uri_str":"http://hadatac.org/kb/hbgd#STD-CPP"
//"dasa_uri_str":"http://hadatac.org/kb/hbgd#DASA-HBGD-SUBJ-APGAR1"
// need to get the actual variable URI....
    // getting measurements from solr!
    private SolrDocumentList getSolrMeasurements(String studyUri, String variableUri){
        //List<Measurement> listMeasurement = new ArrayList<Measurement>();
        SolrDocumentList results = null;
        // build Solr query!
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("study_uri_str:\"" + studyUri + "\"" +
                "AND characteristic_uri_str:\"" + variableUri + "\"");
        solrQuery.setRows(10000000);
        // make Solr query!
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(solrQuery, SolrRequest.METHOD.POST);
            //System.out.println("[getSolrMeasurements] res: " + queryResponse);
            solr.close();

            results = queryResponse.getResults();
        } catch (Exception e) {
            System.out.println("[RestAPI.getMeasurements] ERROR: " + e.getMessage());
        }
        return results;
    }// /getSolrMeasurements()

    // getting measurements from solr for a particular study object
    private SolrDocumentList getSolrMeasurements(int obj_dpl, String studyUri, String variableUri, String objUri){
        SolrDocumentList results = null;
        // build Solr query!
        SolrQuery solrQuery = new SolrQuery();
        if (obj_dpl == OBJ) {
            solrQuery.setQuery("study_uri_str:\"" + studyUri + "\"" +
                    "AND study_object_uri_str:\"" + objUri + "\"" +
                    "AND characteristic_uri_str:\"" + variableUri + "\"");
        } else {
            SolrDocumentList strResult = getSolrSTRs(objUri);
            if (strResult == null) {
                return null;
            }
            String strStr = parseSTRs(strResult);
            if (strStr == null || strStr.equals("")) {
                return null;
            }
            solrQuery.setQuery("study_uri_str:\"" + studyUri + "\"" +
                    "AND " +  strStr + " " +
                    "AND characteristic_uri_str:\"" + variableUri + "\"");
        }
        solrQuery.setRows(10000);
        System.out.println("[RestAPI] solr query: " + solrQuery);
        // make Solr query!
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(solrQuery, SolrRequest.METHOD.POST);
            //System.out.println("[getSolrMeasurements] res: " + queryResponse);
            solr.close();

            results = queryResponse.getResults();
        } catch (Exception e) {
            System.out.println("[RestAPI.getMeasurements] ERROR: " + e.getMessage());
        }
        return results;
    }// /getSolrMeasurements()

    // getting measurements from solr for a particular study object
    private SolrDocumentList getSolrMeasurements(int obj_dpl, String studyUri, String variableUri, String objUri, String fromdatetime, String todatetime){
        //List<Measurement> listMeasurement = new ArrayList<Measurement>();
        SolrDocumentList results = null;
        // build Solr query!
        SolrQuery solrQuery = new SolrQuery();
        if (obj_dpl == OBJ) {
            solrQuery.setQuery("study_uri_str:\"" + studyUri + "\"" +
                    "AND study_object_uri_str:\"" + objUri + "\"" +
                    "AND characteristic_uri_str:\"" + variableUri + "\"" +
                    "AND timestamp_date:[" + fromdatetime + " TO " + todatetime + "]");
        } else {
            SolrDocumentList strResult = getSolrSTRs(objUri);
            if (strResult == null) {
                return null;
            }
            String strStr = parseSTRs(strResult);
            if (strStr == null || strStr.equals("")) {
                return null;
            }
            solrQuery.setQuery("study_uri_str:\"" + studyUri + "\"" +
                    "AND " + strStr  + " " +
                    "AND characteristic_uri_str:\"" + variableUri + "\"" +
                    "AND timestamp_date:[" + fromdatetime + " TO " + todatetime + "]");
        }
        solrQuery.setRows(10000);
        System.out.println("[RestAPI] solr query: " + solrQuery);
        // make Solr query!
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(solrQuery, SolrRequest.METHOD.POST);
            //System.out.println("[getSolrMeasurements] res: " + queryResponse);
            solr.close();

            results = queryResponse.getResults();
        } catch (Exception e) {
            System.out.println("[RestAPI.getMeasurements] ERROR: " + e.getMessage());
        }
        return results;
    }// /getSolrMeasurements()

    private String getSolrMeasurementsTimeRange(int obj_dpl, String studyUri, String variableUri, String objUri){
        String firstTime = null;
        String lastTime = null;
        SolrDocumentList results = null;
        // build first Solr query!
        SolrQuery solrQuery = new SolrQuery();
        if (obj_dpl == OBJ) {
            solrQuery.setQuery("study_uri_str:\"" + studyUri + "\"" +
                    "AND study_object_uri_str:\"" + objUri + "\"" +
                    "AND characteristic_uri_str:\"" + variableUri + "\"" );
        } else {
            SolrDocumentList strResult = getSolrSTRs(objUri);
            if (strResult == null) {
                return null;
            }
            String strStr = parseSTRs(strResult);
            if (strStr == null || strStr.equals("")) {
                return null;
            }
            solrQuery.setQuery("study_uri_str:\"" + studyUri + "\"" +
                    "AND " +  strStr + " " +
                    "AND characteristic_uri_str:\"" + variableUri + "\"");
        }
        solrQuery.setRows(1);
        solrQuery.setSort("timestamp_date",SolrQuery.ORDER.asc);
        System.out.println("[RestAPI] solr query: " + solrQuery);
        SolrClient solr = null;
        QueryResponse queryResponse = null;
        // make Solr queries!
        try {
            solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            queryResponse = solr.query(solrQuery, SolrRequest.METHOD.POST);
            //System.out.println("[getSolrMeasurements] res: " + queryResponse);
            solr.close();

            results = queryResponse.getResults();
        } catch (Exception e) {
            System.out.println("[RestAPI.getMeasurements] ERROR: " + e.getMessage());
            return "ERROR: no initial time for selection";
        }
        firstTime = parseMeasurementTimeStamp(results);
        solrQuery.setSort("timestamp_date",SolrQuery.ORDER.desc);
        try {
            solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            queryResponse = solr.query(solrQuery, SolrRequest.METHOD.POST);
            //System.out.println("[getSolrMeasurements] res: " + queryResponse);
            solr.close();

            results = queryResponse.getResults();
        } catch (Exception e) {
            System.out.println("[RestAPI.getMeasurements] ERROR: " + e.getMessage());
            return "ERROR: no final time for selection";
        }
        lastTime = parseMeasurementTimeStamp(results);
        if (firstTime != null && !firstTime.equals("") && lastTime != null && !lastTime.equals("")) {
            return "[" + firstTime + ";" + lastTime + "]";
        }
        return "ERROR";
    }// /getSolrMeasurementsTimeRange()

    // getting STRs from solr!
    private SolrDocumentList getSolrSTRs(String dplUri){
        SolrDocumentList results = null;
        // build Solr query!
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("deployment_uri_str:\"" + dplUri + "\"");
        solrQuery.setRows(10000);
        // make Solr query!
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_COLLECTION)).build();
            QueryResponse queryResponse = solr.query(solrQuery, SolrRequest.METHOD.POST);
            solr.close();

            results = queryResponse.getResults();
        } catch (Exception e) {
            System.out.println("[RestAPI.getSolrSTRs] ERROR: " + e.getMessage());
        }
        return results;
    }// /getSolrSTRs()

    //*************************
// BLAZEGRAPH QUERY METHODS
//*************************
    // given one or more variable attribute URI's, query blazegraph for
    // the details on those variables and construct JSON array from results
    private ArrayNode variableQuery(String classes){
        if(classes == null){
            return null;
        }
        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "select distinct ?class ?label ?dasa ?unit ?shortname ?unitlabel ?itype ?itypelabel where {" +
                "?class rdfs:label ?label ." +
                "?dasa hasco:hasAttribute ?class ." +
                "?dasa hasco:hasUnit ?unit . " +
                "OPTIONAL { ?dasa rdfs:label ?shortname .} " +
                "OPTIONAL { ?unit rdfs:label ?unitlabel . } " +
                "OPTIONAL { ?class rdfs:subClassOf+ ?itype . " +
                "           ?itype rdfs:subClassOf hasco:StudyIndicator . " +
                "           ?itype rdfs:label ?itypelabel . }" +
                "} values ?class { " + classes + "} ";

        System.out.println("[VariableQuery] sparql query\n" + sparqlQueryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), sparqlQueryString);

        if (resultsrw.size() == 0) {
            System.out.println("[VariableQuery] No variables found in blazegraph!");
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();
        while(resultsrw.hasNext()){
            ObjectNode temp = mapper.createObjectNode();
            QuerySolution soln = resultsrw.next();
            if (soln.get("class") != null) {
                temp.put("uri", soln.get("class").toString());
            } else {
                System.out.println("[VariableQuery] ERROR: Result returned without URI? Skipping....");
                continue;
            }
            if (soln.get("label") != null) {
                temp.put("label", soln.get("label").toString());
            }
            if (soln.get("dasa") != null) {
                temp.put("dasa", soln.get("dasa").toString());
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
            if (soln.get("itype") != null) {
                temp.put("indicatortype", soln.get("itype").toString());
            }
            if (soln.get("itypelabel") != null) {
                temp.put("indicatortypelabel", soln.get("itypelabel").toString());
            }
            anode.add(temp);
        }// /parse sparql results
        System.out.println("[VariableQuery] parsed " + anode.size() + " results into array");
        return anode;
    }// /variableQuery()

    // Given one or more variables, gets details on the indicator types for each of those variables
    private ArrayNode indicatorQuery(String classes){
        if(classes == null){
            return null;
        }
        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "select distinct ?indicator ?label where {" +
                "?class rdfs:subClassOf+ ?indicator . " +
                "?indicator rdfs:subClassOf hasco:StudyIndicator ." +
                "?indicator rdfs:label ?label ." +
                "} values ?class { " + classes + "} ";
        System.out.println("[VariableQuery] sparql query\n" + sparqlQueryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), sparqlQueryString);

        if(resultsrw.size() == 0){
            System.out.println("[IndicatorQuery] No indicators found in blazegraph!");
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();
        while(resultsrw.hasNext()){
            ObjectNode temp = mapper.createObjectNode();
            QuerySolution soln = resultsrw.next();
            if (soln.get("indicator") != null) {
                temp.put("uri", soln.get("indicator").toString());
            } else {
                System.out.println("[IndicatorQuery] ERROR: Result returned without URI? Skipping....");
                continue;
            }
            if (soln.get("label") != null) {
                temp.put("label", soln.get("label").toString());
            }
            if (soln.get("class") != null) {
                temp.put("varclass", soln.get("class").toString());
            }
            anode.add(temp);
        }// /parse sparql results
        System.out.println("[IndicatorQuery] parsed " + anode.size() + " results into array");
        return anode;
    }// /indicatorquery

    // handles blazegraph query and result parsing for unit details
    private ArrayNode unitsQuery(String classes){
        if(classes == null){
            return null;
        }
        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "select distinct ?uri ?label ?short where {" +
                "?dasa hasco:hasAttribute ?class ." +
                "?dasa hasco:hasUnit ?uri . " +
                "?uri rdfs:label ?label ." +
                "OPTIONAL { ?uri obo:hasExactSynonym ?short . }" +
                "} values ?class { " + classes + "} ";

        //System.out.println("[unitsQuery] sparql query\n" + sparqlQueryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), sparqlQueryString);

        if (resultsrw.size() == 0) {
            System.out.println("[unitsQuery] No units found in blazegraph!");
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();

        while(resultsrw.hasNext()){
            ObjectNode temp = mapper.createObjectNode();
            QuerySolution soln = resultsrw.next();
            if (soln.get("uri") != null) {
                temp.put("uri", soln.get("uri").toString());
            } else {
                System.out.println("[unitsQuery] ERROR: Result returned without URI? Skipping....");
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
        System.out.println("[unitsQuery] parsed " + anode.size() + " results into array");
        return anode;
    }// /unitsQuery

    // handles blazegraph query and result parsing for deployment details
    private ArrayNode deploymentsQuery(){
        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "select ?dpl ?plt ?ins where { " +
                "?dpl a vstoi:Deployment . " +
                "?dpl vstoi:hasPlatform ?plt . " +
                "?dpl hasco:hasInstrument ?ins . " +
                "}";
        //System.out.println("[deploymentsQuery] sparql query\n" + sparqlQueryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), sparqlQueryString);

        if (resultsrw.size() == 0) {
            System.out.println("[deploymentsQuery] No deployments found in blazegraph!");
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();

        while(resultsrw.hasNext()){
            ObjectNode temp = mapper.createObjectNode();
            QuerySolution soln = resultsrw.next();
            if (soln.get("dpl") != null) {
                temp.put("uri", soln.get("dpl").toString());
            } else {
                System.out.println("[deploymentsQuery] ERROR: Result returned without URI? Skipping....");
                continue;
            }
            if (soln.get("plt") != null) {
                temp.put("platform", soln.get("plt").toString());
            }
            if (soln.get("ins") != null) {
                temp.put("instrument", soln.get("ins").toString());
            }
            anode.add(temp);
        }// /parse sparql results
        System.out.println("[deploymentsQuery] parsed " + anode.size() + " results into array");
        return anode;
    }// /deploymentsQuery

    private ArrayNode platformsQuery(){
        List<Platform> plats = Platform.find();
        if (plats.size() == 0) {
            System.out.println("[platformsQuery] No platforms found in blazegraph!");
            return null;
        }

        List<FieldOfView> fovs = FieldOfView.find();

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();

        for (Platform plat : plats) {
            ObjectNode temp = mapper.createObjectNode();
            if (plat.getUri() != null) {
                temp.put("uri", plat.getUri());
            }
            if (plat.getLabel() != null && !plat.getLabel().isEmpty()) {
                temp.put("label", plat.getLabel());
            }
            if (plat.getFirstCoordinate() != null) {
                temp.put("coord1", plat.getFirstCoordinate().toString());
            }
            if (plat.getFirstCoordinateUnitLabel() != null && !plat.getFirstCoordinateUnitLabel().isEmpty()) {
                temp.put("coord1Unit", plat.getFirstCoordinateUnitLabel());
            }
            if (plat.getFirstCoordinateCharacteristicLabel() != null && !plat.getFirstCoordinateCharacteristicLabel().isEmpty()) {
                temp.put("coord1Char", plat.getFirstCoordinateCharacteristicLabel());
            }
            if (plat.getSecondCoordinate() != null) {
                temp.put("coord2", plat.getSecondCoordinate().toString());
            }
            if (plat.getSecondCoordinateUnitLabel() != null && !plat.getSecondCoordinateUnitLabel().isEmpty()) {
                temp.put("coord2Unit", plat.getSecondCoordinateUnitLabel());
            }
            if (plat.getSecondCoordinateCharacteristicLabel() != null && !plat.getSecondCoordinateCharacteristicLabel().isEmpty()) {
                temp.put("coord2Char", plat.getSecondCoordinateCharacteristicLabel());
            }
            if (plat.getThirdCoordinate() != null) {
                temp.put("coord3", plat.getThirdCoordinate().toString());
            }
            if (plat.getThirdCoordinateUnitLabel() != null && !plat.getThirdCoordinateUnitLabel().isEmpty()) {
                temp.put("coord3Unit", plat.getThirdCoordinateUnitLabel());
            }
            if (plat.getThirdCoordinateCharacteristicLabel() != null && !plat.getThirdCoordinateCharacteristicLabel().isEmpty()) {
                temp.put("coord3Char", plat.getThirdCoordinateCharacteristicLabel());
            }
            if (plat.getPartOf() != null && !plat.getPartOf().isEmpty()) {
                temp.put("partOf", plat.getPartOf());
            }
            if (plat.getReferenceLayout() != null && !plat.getReferenceLayout().isEmpty()) {
                temp.put("refLayout" , plat.getReferenceLayout());
            }
            if (plat.getLayout() != null && !plat.getLayout().isEmpty()) {
                temp.put("layout", plat.getLayout());
            }
            if (plat.getWidth() != null) {
                temp.put("layoutWidth", plat.getWidth());
            }
            if (plat.getWidthUnit() != null && !plat.getWidthUnit().isEmpty()) {
                temp.put("layoutWidthUnit", plat.getWidthUnitLabel());
            }
            if (plat.getDepth() != null) {
                temp.put("layoutDepth" , plat.getDepth());
            }
            if (plat.getDepthUnit() != null && !plat.getDepthUnit().isEmpty()) {
                temp.put("layoutDepthUnit", plat.getDepthUnitLabel());
            }
            if (plat.getHeight() != null) {
                temp.put("layoutHeight", plat.getHeight());
            }
            if (plat.getHeightUnit() != null && !plat.getHeightUnit().isEmpty()) {
                temp.put("layoutHeightUnit", plat.getHeightUnitLabel());
            }
            boolean cont = true;
            Iterator<FieldOfView> fovIterator = fovs.iterator();
            while (fovIterator.hasNext() && cont) {
                FieldOfView fov = fovIterator.next();
                if (fov.getIsFOVOf() != null && fov.getIsFOVOf().equals(plat.getUri())) {
                    if (fov.getGeometry() != null && !fov.getGeometry().isEmpty()) {
                        temp.put("fovgeometry" , fov.getGeometry());
                    }
                    if (fov.getFirstParameter() != null) {
                        temp.put("fovparam1", fov.getFirstParameter().toString());
                    }
                    if (fov.getFirstParameterUnitLabel() != null && !fov.getFirstParameterUnitLabel().isEmpty()) {
                        temp.put("fovparam1Unit", fov.getFirstParameterUnitLabel());
                    }
                    if (fov.getFirstParameterCharacteristicLabel() != null && !fov.getFirstParameterCharacteristicLabel().isEmpty()) {
                        temp.put("fovparam1Char", fov.getFirstParameterCharacteristicLabel());
                    }
                    if (fov.getSecondParameter() != null) {
                        temp.put("fovparam2", fov.getSecondParameter().toString());
                    }
                    if (fov.getSecondParameterUnitLabel() != null && !fov.getSecondParameterUnitLabel().isEmpty()) {
                        temp.put("fovparam2Unit", fov.getSecondParameterUnitLabel());
                    }
                    if (fov.getSecondParameterCharacteristicLabel() != null && !fov.getSecondParameterCharacteristicLabel().isEmpty()) {
                        temp.put("fovparam2Char", fov.getSecondParameterCharacteristicLabel());
                    }
                    if (fov.getThirdParameter() != null) {
                        temp.put("fovparam3", fov.getThirdParameter().toString());
                    }
                    if (fov.getThirdParameterUnitLabel() != null && !fov.getThirdParameterUnitLabel().isEmpty()) {
                        temp.put("fovparam3Unit", fov.getThirdParameterUnitLabel());
                    }
                    if (fov.getThirdParameterCharacteristicLabel() != null && !fov.getThirdParameterCharacteristicLabel().isEmpty()) {
                        temp.put("fovparam3Char", fov.getThirdParameterCharacteristicLabel());
                    }
                    cont = false;
                }
            }
            anode.add(temp);
        }
        System.out.println("[platformsQuery] parsed " + anode.size() + " results into array");
        return anode;
    }// /platformsQuery

    private ArrayNode fieldsOfViewQuery(){
        List<FieldOfView> fovs = FieldOfView.find();
        if (fovs.size() == 0) {
            System.out.println("[fieldsofviewQuery] No fields of view found in blazegraph!");
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();

        for (FieldOfView fov : fovs) {
            ObjectNode temp = mapper.createObjectNode();
            if (fov.getUri() != null) {
                temp.put("uri", fov.getUri());
            }
            if (fov.getLabel() != null && !fov.getLabel().isEmpty()) {
                temp.put("label", fov.getLabel());
            }
            if (fov.getIsFOVOf() != null && !fov.getIsFOVOf().isEmpty()) {
                temp.put("platform_uri", fov.getIsFOVOf());
            }
            if (fov.getGeometry() != null && !fov.getGeometry().isEmpty()) {
                temp.put("geometry" , fov.getGeometry());
            }
            if (fov.getFirstParameter() != null) {
                temp.put("param1", fov.getFirstParameter().toString());
            }
            if (fov.getFirstParameterUnitLabel() != null && !fov.getFirstParameterUnitLabel().isEmpty()) {
                temp.put("param1Unit", fov.getFirstParameterUnitLabel());
            }
            if (fov.getFirstParameterCharacteristicLabel() != null && !fov.getFirstParameterCharacteristicLabel().isEmpty()) {
                temp.put("param1Char", fov.getFirstParameterCharacteristicLabel());
            }
            if (fov.getSecondParameter() != null) {
                temp.put("param2", fov.getSecondParameter().toString());
            }
            if (fov.getSecondParameterUnitLabel() != null && !fov.getSecondParameterUnitLabel().isEmpty()) {
                temp.put("param2Unit", fov.getSecondParameterUnitLabel());
            }
            if (fov.getSecondParameterCharacteristicLabel() != null && !fov.getSecondParameterCharacteristicLabel().isEmpty()) {
                temp.put("param2Char", fov.getSecondParameterCharacteristicLabel());
            }
            if (fov.getThirdParameter() != null) {
                temp.put("param3", fov.getThirdParameter().toString());
            }
            if (fov.getThirdParameterUnitLabel() != null && !fov.getThirdParameterUnitLabel().isEmpty()) {
                temp.put("param3Unit", fov.getThirdParameterUnitLabel());
            }
            if (fov.getThirdParameterCharacteristicLabel() != null && !fov.getThirdParameterCharacteristicLabel().isEmpty()) {
                temp.put("param3Char", fov.getThirdParameterCharacteristicLabel());
            }
            anode.add(temp);
        }
        System.out.println("[platformsQuery] parsed " + anode.size() + " results into array");
        return anode;
    }// /platformsQuery

    private ArrayNode ocQuery(String ocUri){
        if(ocUri == null){
            return null;
        }
        String sparqlQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "SELECT ?objUri ?typeUri ?studyUri WHERE { " +
                "   ?objUri hasco:isMemberOf  <" + ocUri + "> . " +
                "   ?objUri a ?typeUri . " +
                "   <" + ocUri + "> hasco:isMemberOf  ?studyUri . " +
                " } ";

        System.out.println("[ocQuery] sparql query\n" + sparqlQueryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), sparqlQueryString);

        if (resultsrw.size() == 0) {
            System.out.println("[ocQuery] No objects found in collection " + ocUri);
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = mapper.createArrayNode();

        while(resultsrw.hasNext()){
            ObjectNode temp = mapper.createObjectNode();
            QuerySolution soln = resultsrw.next();
            if (soln.get("objUri") != null) {
                temp.put("objectUri", soln.get("objUri").toString());
            } else {
                System.out.println("[ocQuery] ERROR: Result returned without URI? Skipping....");
                continue;
            }
            if (soln.get("typeUri") != null) {
                temp.put("typeUri", soln.get("typeUri").toString());
            }
            if (soln.get("studyUri") != null) {
                temp.put("studyUri", soln.get("studyUri").toString());
            }
            anode.add(temp);
        }// /parse sparql results
        System.out.println("[ocUri] parsed " + anode.size() + " objects into array");
        return anode;
    }// /getAllOCs

    private STR getSTR(String strUri) {
        if (strUri == null || strUri.equals("")) {
            return null;
        }
        STR str = STR.findByUri(strUri);
        return str;
    }// /getSTR

// ********
// GET ALL:
// ********

    // ********
    // Studies!
    // ********
    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result getAllStudies(){
        ObjectMapper mapper = new ObjectMapper();
        List<Study> theStudies = Study.find();
        System.out.println("[getAllStudies] found " + theStudies.size() + " things");
        if(theStudies.size() == 0){
            return notFound(ApiUtil.createResponse("No studies found", false));
        }
        ArrayNode theResults = mapper.createArrayNode();
        for (Study st : theStudies){
            ObjectNode temp = mapper.createObjectNode();
            List<String> vars = getUsedVarsInStudy(st.getUri());
            System.out.println("[getAllStudies] back from getUsedVarsInStudy");
            ArrayNode anode = null;
            if (vars.size() > 0) {
                anode = mapper.convertValue(vars, ArrayNode.class);
            }
            //ArrayNode anode = variableQuery(formatQueryValues(uris));
            //temp.set("study_info", mapper.convertValue(theStudies, JsonNode.class));
            temp.set("study_info", mapper.convertValue(st, JsonNode.class));
            if (anode == null) {
                System.out.println("[RestApi] WARN: no variables found for study " + st.getUri());
            } else {
                temp.set("variable_uris", anode);
            }
            theResults.add(temp);
        }
        JsonNode jsonObject = mapper.convertValue(theResults, JsonNode.class);
        return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getAllStudies()

    // **********
    // Variables!
    // **********
    // This is the same as the study-specific method,
    // but gets all of the variables for which we have
    // measurements in hadatac
    public Result getAllVariables(){
        ObjectMapper mapper = new ObjectMapper();
        // 1. Query Solr for all variables for which we have measurements
        List<String> uris = getAllUsedVars();
        if(uris.size() == 0) return notFound(ApiUtil.createResponse("Encountered SOLR error!", false));
        // 2. Query Blazegraph for those details
        ArrayNode anode = variableQuery(formatQueryValues(uris));
        // 3. Construct response
        if (anode == null){
            return notFound(ApiUtil.createResponse("Encountered Blazegraph error!", false));
        } else{
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            System.out.println("[getAllVars] done");
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getAllVariables

    // ******
    // Units!
    // ******
    // Mirrors getAllVariables, but returns Unit details instead of Variable details
    public Result getAllUnits(){
        ObjectMapper mapper = new ObjectMapper();
        // 1. Query Solr for all variables for which we have measurements
        List<String> uris = getAllUsedVars();
        if(uris.size() == 0) return notFound(ApiUtil.createResponse("Encountered SOLR error!", false));
        // 2. Query Blazegraph for details on those units
        ArrayNode anode = unitsQuery(formatQueryValues(uris));
        // 3. Construct response
        if (anode == null){
            return notFound(ApiUtil.createResponse("Encountered Blazegraph error!", false));
        } else{
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            System.out.println("[getAllUnits] Done");
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getAllUnits

    // ************
    // Deployments!
    // ************
    public Result getAllDeployments(){
        ObjectMapper mapper = new ObjectMapper();
        // 1. Query Blazegraph for deployments
        ArrayNode anode = deploymentsQuery();
        // 2. Construct response
        if (anode == null){
            return notFound(ApiUtil.createResponse("Encountered Blazegraph error!", false));
        } else{
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            System.out.println("[getAllDeployments] Done");
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getAllDeployments

    // ************
    // Platforms
    // ************
    public Result getAllPlatforms(){
        ObjectMapper mapper = new ObjectMapper();
        // 1. Query Blazegraph for platforms
        ArrayNode anode = platformsQuery();
        // 2. Construct response
        if (anode == null){
            return notFound(ApiUtil.createResponse("Encountered Blazegraph error!", false));
        } else{
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            System.out.println("[getAllPlatforms] Done");
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getAllPlatforms

    // **************
    // Fields of View
    // **************
    public Result getAllFieldsOfView(){
        ObjectMapper mapper = new ObjectMapper();
        // 1. Query Blazegraph for fields of view
        ArrayNode anode = fieldsOfViewQuery();
        // 2. Construct response
        if (anode == null){
            return notFound(ApiUtil.createResponse("Encountered Blazegraph error!", false));
        } else{
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            System.out.println("[getAllFieldsOfView] Done");
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getAllFieldsOfView

    // ***********
    // Indicators!
    // ***********
    // Mirrors getAllVariables, but returns Indicator details instead of Variable details
    public Result getAllIndicators(){
        ObjectMapper mapper = new ObjectMapper();
        // 1. Query Solr for all variables for which we have measurements
        List<String> uris = getAllUsedVars();
        if(uris.size() == 0) return notFound(ApiUtil.createResponse("Encountered SOLR error!", false));
        // 2. Query Blazegraph for details on those units
        ArrayNode anode = indicatorQuery(formatQueryValues(uris));
        // 3. Construct response
        if (anode == null){
            return notFound(ApiUtil.createResponse("Encountered Blazegraph error!", false));
        } else{
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            System.out.println("[getAllIndicators] Done");
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getAllUnits



// ********
// GET FOR GIVEN STUDY:
// ********

    // ******
    // Study!
    // ******
    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    // test with http%3A%2F%2Fhadatac.org%2Fkb%2Fhbgd%23STD-CPP4
    public Result getStudy(String studyUri){
        ObjectMapper mapper = new ObjectMapper();
        Study result = Study.find(studyUri);
        System.out.println("[RestAPI] type: " + result.getTypeUri());
        if(result == null || result.getTypeUri() == null || result.getTypeUri() == ""){
            return notFound(ApiUtil.createResponse("Study with name/ID " + studyUri + " not found", false));
        }
        // get the list of variables in that study
        // serialize the Study object first as ObjectNode
        //   as JsonNode is immutable and meant to be read-only
        ObjectNode obj = mapper.convertValue(result, ObjectNode.class);
        List<String> vars = getUsedVarsInStudy(studyUri);
        ArrayNode anode = mapper.convertValue(vars, ArrayNode.class);
        //ArrayNode anode = variableQuery(formatQueryValues(uris));
        if(anode == null){
            System.out.println("[RestApi] WARN: no variables found for study " + studyUri);
        }
        obj.set("variable_uris", anode);
        JsonNode jsonObject = mapper.convertValue(obj, JsonNode.class);
        return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getStudy()

    // **********
    // Variables!
    // **********
    // test with http%3A%2F%2Fhadatac.org%2Fkb%2Fhbgd%23STD-CPP4
    // The DASchema will return ALL variables in the schema for the given study
    // We need to hit solr to make sure we only get variables for which there exist measurements
    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result getVariablesInStudy(String studyUri){
        ObjectMapper mapper = new ObjectMapper();
        // 1. Query solr for all variables for which we have measurements in the given study
        List<String> uris = getUsedVarsInStudy(studyUri);
        if(uris.size() == 0) return notFound(ApiUtil.createResponse("Encountered SOLR error!", false));
        // 2. Query blazegraph for details on those variables
        ArrayNode anode = variableQuery(formatQueryValues(uris));
        // 3. Construct response
        if (anode == null){
            return notFound(ApiUtil.createResponse("Encountered Blazegraph error!", false));
        } else{
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            System.out.println("[GetVarsInStudy] done");
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getVariablesInStudy

    // ******
    // Units!
    // ******
    // This method takes the same solr query as above,
    // but instead queries the triple store for units instead of attributes
    public Result getUnitsInStudy(String studyUri){
        ObjectMapper mapper = new ObjectMapper();
        // 1. Query Solr for variables for which we have measurements for the given study
        List<String> uris = getUsedVarsInStudy(studyUri);
        if(uris == null){
            return notFound(ApiUtil.createResponse("Encountered SOLR error!", false));
        }
        // Step 2: query blazegraph for details
        ArrayNode anode = unitsQuery(formatQueryValues(uris));
        // 3. Construct response
        if (anode == null){
            return notFound(ApiUtil.createResponse("Encountered Blazegraph error!", false));
        } else{
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            System.out.println("[getUnitsInStudy] done");
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getUnitsInStudy


    // ******************
    // ObjectCollections!
    // ******************
    // Given the study URI, return the URI's and types of all object collections
    // (not the full arrays of the collections themselves)
    // Returning the ObjectCollection pojo includes an array of the included objects
    //    themselves, so to avoid overload for the extremely large OC's, we parse the
    //    query results into a smaller JSON object with URI and TYPE URI.
    public Result getOCListInStudy(String studyUri){
        ObjectMapper mapper = new ObjectMapper();
        if (studyUri == null) {
            return badRequest(ApiUtil.createResponse("No study specified", false));
        }
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "SELECT ?uri ?ocType WHERE { \n" +
                "   ?ocType rdfs:subClassOf+ hasco:ObjectCollection . \n" +
                "   ?uri a ?ocType . \n" +
                "   ?uri hasco:isMemberOf <" + studyUri + "> . \n" +
                " } ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

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

//*************
//GET SPECIFIC:
//*************

    public Result getUri(String uri){
        if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
            return badRequest(ApiUtil.createResponse("[" + uri + "] is an invalid URI", false));
        }

        try {

            /*
             *   Process URI against SOLR entities first
             */

            Measurement measurementResult = Measurement.find(uri);
            if (measurementResult != null && measurementResult.getTypeUri() != null && measurementResult.getTypeUri().equals(HASCO.VALUE)) {
                return processResult(measurementResult, measurementResult.getUri());
            }

            DataFile dataFileResult = DataFile.findByUri(uri);
            if (dataFileResult != null && dataFileResult.getTypeUri() != null && dataFileResult.getTypeUri().equals(HASCO.DATA_FILE)) {
                return processResult(dataFileResult, dataFileResult.getUri());
            }

            /*
             *  Now uses GenericInstance to process URI against TripleStore content
             */

            Object finalResult = null;
            String typeUri = null;
            GenericInstance result = GenericInstance.find(uri);
            System.out.println("inside getUri(): URI [" + uri + "]");

            if (result == null) {
                    return notFound(ApiUtil.createResponse("No instance found for uri [" + uri + "]", false));
            }

            if (result.getHascoTypeUri() == null || result.getHascoTypeUri().isEmpty()) {
                return notFound(ApiUtil.createResponse("No valid HASCO type found for uri [" + uri + "]", false));
            }

            if (result.getHascoTypeUri().equals(HASCO.STUDY)) {
                finalResult = Study.find(uri);
                if (finalResult != null) {
                    typeUri = ((Study) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.OBJECT_COLLECTION)) {
                finalResult = ObjectCollection.findForBrowser(uri);
                if (finalResult != null) {
                    typeUri = ((ObjectCollection) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.VIRTUAL_COLUMN)) {
                finalResult = VirtualColumn.find(uri);
                if (finalResult != null) {
                    typeUri = ((VirtualColumn) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.DATA_ACQUISITION)) {
                finalResult = STR.findByUri(uri);
                if (finalResult != null) {
                    typeUri = ((STR) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.STUDY_OBJECT)) {
                finalResult = StudyObject.find(uri);
                if (finalResult != null) {
                    typeUri = ((StudyObject) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.DA_SCHEMA)) {
                finalResult = DataAcquisitionSchema.find(uri);
                if (finalResult != null) {
                    typeUri = ((DataAcquisitionSchema) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.DA_SCHEMA_ATTRIBUTE)) {
                finalResult = DataAcquisitionSchemaAttribute.find(uri);
                if (finalResult != null) {
                    typeUri = ((DataAcquisitionSchemaAttribute) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.DA_SCHEMA_OBJECT)) {
                finalResult = DataAcquisitionSchemaObject.find(uri);
                if (finalResult != null) {
                    typeUri = ((DataAcquisitionSchemaObject) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.DEPLOYMENT)) {
                finalResult = Deployment.find(uri);
                if (finalResult != null) {
                    typeUri = ((Deployment) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.PLATFORM)) {
                finalResult = Platform.find(uri);
                if (finalResult != null) {
                    typeUri = ((Platform) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.INSTRUMENT)) {
                finalResult = Instrument.find(uri);
                if (finalResult != null) {
                    typeUri = ((Instrument) finalResult).getHascoTypeUri();
                }
            } else if (result.getHascoTypeUri().equals(HASCO.DETECTOR)) {
                finalResult = Detector.find(uri);
                if (finalResult != null) {
                    typeUri = ((Detector) finalResult).getHascoTypeUri();
                }
            } else {
                finalResult = result;
                if (finalResult != null) {
                    typeUri = ((GenericInstance) finalResult).getHascoTypeUri();
                }
            }
            if (finalResult == null || typeUri == null || typeUri.equals("")){
                return notFound(ApiUtil.createResponse("No instance found for uri [" + uri + "]", false));
            }

            // list object properties and associated classes

            return processResult(finalResult, uri);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(ApiUtil.createResponse("Error processing URI [" + uri + "]", false));
        }

        //System.out.println("[RestAPI] type inside getUri(uri): " + typeUri);
        // get the list of variables in that study
        // serialize the Study object first as ObjectNode
        //   as JsonNode is immutable and meant to be read-only
        //JsonNode jsonObject = null;
        //try {
        //    ObjectNode obj = mapper.convertValue(finalResult, ObjectNode.class);
        //    jsonObject = mapper.convertValue(obj, JsonNode.class);
            //System.out.println(prettyPrintJsonString(jsonObject));
        //} catch (Exception e) {
        //    return badRequest(ApiUtil.createResponse("Error processing the json object for URI [" + uri + "]", false));
        //}
        //return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getUri()

    private Result processResult(Object result, String uri) {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("[RestAPI] processing object: " + uri);
        JsonNode jsonObject = null;
        try {
            ObjectNode obj = mapper.convertValue(result, ObjectNode.class);
            jsonObject = mapper.convertValue(obj, JsonNode.class);
            System.out.println(prettyPrintJsonString(jsonObject));
        } catch (Exception e) {
            return badRequest(ApiUtil.createResponse("Error processing the json object for URI [" + uri + "]", false));
        }
        return ok(ApiUtil.createResponse(jsonObject, true));
    }

    public String prettyPrintJsonString(JsonNode jsonNode) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(jsonNode.toString(), Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
    public Result getHADatAcClass(String classUri){
        ObjectMapper mapper = new ObjectMapper();
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + classUri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        StmtIterator stmtIterator = model.listStatements();

        // returns null if not statement is found
        if (!stmtIterator.hasNext()) {
            return notFound(ApiUtil.createResponse("No class found for uri [" + classUri + "]", false));
        }

        Object typeClass = new Object();

        System.out.println("Inside getHADatAcClass");

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            System.out.println("predicate: " + statement.getPredicate().getURI());
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
                object.asResource().getURI());
            }
        }

        String typeUri = null;

        System.out.println("[RestAPI] class: " + classUri);
        try {
            // get the list of variables in that study
            // serialize the Study object first as ObjectNode
            //   as JsonNode is immutable and meant to be read-only
            ObjectNode obj = mapper.convertValue(model, ObjectNode.class);
            JsonNode jsonObject = mapper.convertValue(obj, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(ApiUtil.createResponse("Error parsing class", false));
        }
    }// /getHADatAcClass()
    */

    // ******
    // Study!
    // ******
    // see: getStudy above

    // *********
    // Variable!
    // *********
    // test with http%3A%2F%2Fpurl.org%2Fhbgd%2Fkg%2Fns%2FSUBJID
    // or http%3A%2F%2Fpurl.org%2Fhbgd%2Fkg%2Fns%2FHAZ
    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result getVariable(String attributeUri){
        ObjectMapper mapper = new ObjectMapper();
        // 2. Query blazegraph for details on the given variable
        ArrayNode anode = variableQuery(formatQueryValues(attributeUri));
        // 3. Construct response
        if (anode == null){
            return notFound(ApiUtil.createResponse("Encountered Blazegraph error!", false));
        } else{
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
            //System.out.println("[getVariable] done");
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getVariable()

    // *****************
    // ObjectCollection!
    // *****************
    // test with http%3A%2F%2Fhadatac.org%2Fkb%2Fhbgd%23CH-CPP4
    // Given the study URI, return the URI's of all StudyObjects that are subjects in that study
    //   - each subject / object should include a study_uri, variable_type_uris
    public Result getObjectCollection(String ocUri){
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode anode = ocQuery(ocUri);
        if(anode == null){
            return notFound(ApiUtil.createResponse("ObjectCollection with uri " + ocUri + " not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(anode, JsonNode.class);
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
        return getObjectsInCollection(ocUri, offset, 250);
    }// /getObjectsInCollection

    //total_count, page, size
    public Result getObjectsInCollection(String ocUri, int offset, int pageSize){
        if(pageSize < 1) {
            pageSize = 250;
            //System.out.println("[RestAPI] getObjectsInCollection : Yikes! Resetting that page size for you!");
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectCollection objs = ObjectCollection.find(ocUri);
        if(objs == null){
            return notFound(ApiUtil.createResponse("ObjectCollection with uri " + ocUri + " not found", false));
        }
        int totalResultSize = objs.getCollectionSize();
        if(totalResultSize < 1){
            return notFound(ApiUtil.createResponse("ObjectCollection with uri " + ocUri + " not found", false));
        }
        List<StudyObject> results = StudyObject.findByCollectionWithPages(objs, pageSize, offset);
        if(results == null){
            return notFound(ApiUtil.createResponse("ObjectCollection with URI " + ocUri + "not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getObjectsInCollection

    // *************
    // Measurements!
    // *************

    private JsonNode parseMeasurements(SolrDocumentList solrResults) {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, STR> strMap = new HashMap<String, STR>();

        // parse results
        ArrayNode anode = mapper.createArrayNode();
        Iterator<SolrDocument> i = solrResults.iterator();
        while (i.hasNext()) {

            SolrDocument doc = i.next();
            STR str = null;
            String strUri = SolrUtils.getFieldValue(doc, "acquisition_uri_str");
            if (!strMap.containsKey(strUri)) {
                str = getSTR(strUri);
                if (str != null) {
                    strMap.put(strUri, str);
                }
            } else {
                str = strMap.get(strUri);
            }
            ObjectNode temp = mapper.createObjectNode();
            temp.put("measurementuri", SolrUtils.getFieldValue(doc, "uri"));
            temp.put("studyuri", SolrUtils.getFieldValue(doc, "study_uri_str"));
            temp.put("studyobjecturi", SolrUtils.getFieldValue(doc, "study_object_uri_str"));
            temp.put("variableuri", SolrUtils.getFieldValue(doc, "characteristic_uri_str"));
            temp.put("value", SolrUtils.getFieldValue(doc, "value_str"));
            temp.put("unituri", SolrUtils.getFieldValue(doc, "unit_uri_str"));
            temp.put("timeValue", SolrUtils.getFieldValue(doc, "time_value_double"));
            temp.put("timeUnit", SolrUtils.getFieldValue(doc, "time_value_unit_uri_str"));
            temp.put("timestamp", SolrUtils.getFieldValue(doc, "timestamp_date"));
            if (str == null || str.getDeployment() == null) {
                temp.put("platform", "");
                temp.put("instrument", "");
            } else {
                if (str.getDeployment() != null && str.getDeployment().getPlatform() != null) {
                    if (str.getDeployment().getPlatform().getLabel() != null) {
                        temp.put("platform", str.getDeployment().getPlatform().getLabel());
                    } else {
                        temp.put("platform", str.getDeployment().getPlatform().getUri());
                    }
                } else {
                    temp.put("platform", "");
                }
                if (str.getDeployment() != null && str.getDeployment().getInstrument() != null) {
                    if (str.getDeployment().getInstrument().getLabel() != null) {
                        temp.put("instrument", str.getDeployment().getInstrument().getLabel());
                    } else {
                        temp.put("instrument", str.getDeployment().getInstrument().getUri());
                    }
                } else {
                    temp.put("instrument", "");
                }
            }

            anode.add(temp);
        }// /parse solr results

        if(anode == null){
            return null;
        } else {
            return mapper.convertValue(anode, JsonNode.class);
        }
    }

    private String parseMeasurementTimeStamp(SolrDocumentList solrResults) {
        Iterator<SolrDocument> i = solrResults.iterator();
        if (i.hasNext()) {
            SolrDocument doc = i.next();
            return SolrUtils.getFieldValue(doc, "timestamp_date");
        }
        return null;
    }

    private String parseSTRs(SolrDocumentList solrResults) {
        Iterator<SolrDocument> i = solrResults.iterator();
        List<String> uris = new ArrayList<String>();
        while (i.hasNext()) {
            SolrDocument doc = i.next();
            uris.add(SolrUtils.getFieldValue(doc, "uri"));
        }
        String str = "";
        if (uris.size() == 1) {
            str = "acquisition_uri_str:\"" + uris.get(0) + "\"";
            return str;
        }

        if (uris.size() > 0) {
            str = "( ";
            for (int uri = 0; uri < uris.size(); uri++) {
                str += "acquisition_uri_str:\"" + uris.get(uri) + "\"";
                if (uri < (uris.size() - 1)) {
                    str += " OR ";
                }
            }
            str += ")";
            return str;
        }

        return null;
    }

    // :study_uri/:variable_uri/
    public Result getMeasurements(String studyUri, String variableUri){
        if(variableUri == null){
            return badRequest(ApiUtil.createResponse("No variable specified", false));
        }
        if(studyUri == null){
            return badRequest(ApiUtil.createResponse("No study specified", false));
        }
        // Solr query
        SolrDocumentList solrResults = getSolrMeasurements(studyUri, variableUri);
        if(solrResults.size() < 1){
            return notFound(ApiUtil.createResponse("Solr Message: no measurements found", false));
        }

        JsonNode jsonObject = parseMeasurements(solrResults);

        if(jsonObject == null){
            return internalServerError(ApiUtil.createResponse("Error parsing measurments", false));
        } else {
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getMeasurements()

    // :study_uri/:variable_uri/:object_uri
    public Result getMeasurementsForObj(String studyUri, String variableUri, String objUri){
        if(variableUri == null){
            return badRequest(ApiUtil.createResponse("No variable specified", false));
        }
        if(studyUri == null){
            return badRequest(ApiUtil.createResponse("No study specified", false));
        }
        // Solr query
        //System.out.println("[RestAPI] Getting measurements for " + objUri);
        SolrDocumentList solrResults = getSolrMeasurements(OBJ, studyUri, variableUri, objUri);
        if(solrResults.size() < 1){
            return notFound(ApiUtil.createResponse("Solr Message: no measurements found", false));
        }

        JsonNode jsonObject = parseMeasurements(solrResults);

        if(jsonObject == null){
            return internalServerError(ApiUtil.createResponse("Error parsing measurments", false));
        } else {
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getMeasurementsForObj()

    // :study_uri/:variable_uri/:object_uri/:fromdatetime/:todatetime
    public Result getMeasurementsForObjInPeriod(String studyUri, String variableUri, String objUri, String fromdatetime, String todatetime){
        if(variableUri == null){
            return badRequest(ApiUtil.createResponse("No variable specified", false));
        }
        if(studyUri == null){
            return badRequest(ApiUtil.createResponse("No study specified", false));
        }
        if(fromdatetime == null){
            return badRequest(ApiUtil.createResponse("No fromdatetime specified", false));
        }
        if(todatetime == null){
            return badRequest(ApiUtil.createResponse("No todatetime specified", false));
        }
        // Solr query
        //.out.println("[RestAPI] Getting measurements for " + objUri);
        SolrDocumentList solrResults = getSolrMeasurements(OBJ, studyUri, variableUri, objUri, fromdatetime, todatetime);
        if(solrResults.size() < 1){
            return notFound(ApiUtil.createResponse("Solr Message: no measurements found", false));
        }

        JsonNode jsonObject = parseMeasurements(solrResults);

        if(jsonObject == null){
            return internalServerError(ApiUtil.createResponse("Error parsing measurments", false));
        } else {
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getMeasurementsForObjInPeriod()

    // :study_uri/:variable_uri/:object_uri/timerange
    public Result getMeasurementsForObjTimeRange(String studyUri, String variableUri, String objUri){
        if(variableUri == null){
            return badRequest(ApiUtil.createResponse("No variable specified", false));
        }
        if(studyUri == null){
            return badRequest(ApiUtil.createResponse("No study specified", false));
        }
        // Solr query
        System.out.println("[RestAPI] Getting time range for measurements for " + objUri);
        String timerange = getSolrMeasurementsTimeRange(OBJ, studyUri, variableUri, objUri);
        if(timerange == null){
            return internalServerError(ApiUtil.createResponse("Error retrieving time range for  measurements", false));
        } else {
            return ok(timerange);
        }
    }// /getMeasurementsForObjTimeRange()

    // :study_uri/:variable_uri/:deployment_uri
    public Result getMeasurementsForDpl(String studyUri, String variableUri, String dplUri){
        if(variableUri == null){
            return badRequest(ApiUtil.createResponse("No variable specified", false));
        }
        if(studyUri == null){
            return badRequest(ApiUtil.createResponse("No study specified", false));
        }
        // Solr query
        System.out.println("[RestAPI] Getting measurements for " + dplUri);
        SolrDocumentList solrResults = getSolrMeasurements(DPL, studyUri, variableUri, dplUri);
        if(solrResults == null || solrResults.size() < 1){
            return notFound(ApiUtil.createResponse("Solr Message: no measurements found", false));
        }

        JsonNode jsonObject = parseMeasurements(solrResults);

        if(jsonObject == null){
            return internalServerError(ApiUtil.createResponse("Error parsing measurments", false));
        } else {
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getMeasurementsForDlp()

    // :study_uri/:variable_uri/:deployment_uri/:fromdatetime/:todatetime
    public Result getMeasurementsForDplInPeriod(String studyUri, String variableUri, String dplUri, String fromdatetime, String todatetime){
        if(variableUri == null){
            return badRequest(ApiUtil.createResponse("No variable specified", false));
        }
        if(studyUri == null){
            return badRequest(ApiUtil.createResponse("No study specified", false));
        }
        if(fromdatetime == null){
            return badRequest(ApiUtil.createResponse("No fromdatetime specified", false));
        }
        if(todatetime == null){
            return badRequest(ApiUtil.createResponse("No todatetime specified", false));
        }
        // Solr query
        System.out.println("[RestAPI] Getting measurements for " + dplUri);
        SolrDocumentList solrResults = getSolrMeasurements(DPL, studyUri, variableUri, dplUri, fromdatetime, todatetime);
        if(solrResults == null || solrResults.size() < 1){
            return notFound(ApiUtil.createResponse("Solr Message: no measurements found", false));
        }

        JsonNode jsonObject = parseMeasurements(solrResults);

        if(jsonObject == null){
            return internalServerError(ApiUtil.createResponse("Error parsing measurments", false));
        } else {
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }// /getMeasurementsForDplInPeriod()

    // :study_uri/:variable_uri/:deployment_uri/timerange
    public Result getMeasurementsForDplTimeRange(String studyUri, String variableUri, String dplUri){
        if(variableUri == null){
            return badRequest(ApiUtil.createResponse("No variable specified", false));
        }
        if(studyUri == null){
            return badRequest(ApiUtil.createResponse("No study specified", false));
        }
        // Solr query
        System.out.println("[RestAPI] Getting time range for measurements for deployment " + dplUri);
        String timerange = getSolrMeasurementsTimeRange(DPL, studyUri, variableUri, dplUri);
        if(timerange == null){
            return internalServerError(ApiUtil.createResponse("Error retrieving time range for  measurements", false));
        } else {
            return ok(timerange);
        }
    }// /getMeasurementsForDplTimeRange()

}// /RestApi
