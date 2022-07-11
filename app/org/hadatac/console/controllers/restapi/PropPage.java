package org.hadatac.console.controllers.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.Pivot;
import org.hadatac.entity.pojo.*;
import org.hadatac.utils.ApiUtil;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.*;

public class PropPage extends Controller {

    private final static int MAX_PAGE_SIZE = 80;
    private final static String STUDY_OBJECT_URI = "http://hadatac.org/ont/hasco/StudyObject";
    private final static String SOC_URI = "http://hadatac.org/ont/hasco/ObjectCollection";

    public Result getPage(String classUri, String grouperUri, int offset, int pageSize) {
        switch (classUri) {
            case STUDY_OBJECT_URI:
                return getObjectsInSOC(grouperUri, offset, pageSize);
            case SOC_URI:
                return getSOCsInStudy(grouperUri, offset, pageSize);
        }
        return notFound(ApiUtil.createResponse("/api/page doss not recognize class uri " + classUri + ".", false));
    }

    private Result getObjectsInSOC(String socUri, int offset, int pageSize) {
        if(pageSize < 1) {
            pageSize = MAX_PAGE_SIZE;
            System.out.println("[RestAPI] getObjectsInCollection : Yikes! Resetting that page size for you!");
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectCollection soc = ObjectCollection.find(socUri);
        if(soc == null){
            return notFound(ApiUtil.createResponse("SOC with uri " + socUri + " not found", false));
        }
        int totalResultSize = soc.getCollectionSize();
        if(totalResultSize < 1){
            return notFound(ApiUtil.createResponse("SOC with uri " + socUri + " not found", false));
        }
        List<StudyObject> results = StudyObject.findByCollectionWithPages(soc, pageSize, offset);
        if(results == null){
            return notFound(ApiUtil.createResponse("SOC with URI " + socUri + "not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getSOCsInStudy(String studyUri, int offset, int pageSize) {
        if(pageSize < 1) {
            pageSize = MAX_PAGE_SIZE;
            System.out.println("[RestAPI] getSOCsInStudy : Yikes! Resetting that page size for you!");
        }
        ObjectMapper mapper = new ObjectMapper();
        Study study = Study.find(studyUri);
        if (study == null){
            return notFound(ApiUtil.createResponse("Study with uri " + studyUri + " not found", false));
        }
        //int totalResultSize = study.getNumberOfSOCs();
        //if(totalResultSize < 1){
        //    return notFound(ApiUtil.createResponse("Study with uri " + studyUri + " not found", false));
        //}
        //List<ObjectCollection> results = ObjectCollection.findByStudyUri(study, pageSize, offset);
        List<ObjectCollection> results = ObjectCollection.findByStudyUri(studyUri);
        if (results == null) {
            return notFound(ApiUtil.createResponse("SOCs from study with URI " + studyUri + "not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    public Result getPageSize(String classUri, String grouperUri) {
        switch (classUri) {
            case STUDY_OBJECT_URI:
                return getObjectsInSOCSize(grouperUri);
            case SOC_URI:
                return getSOCsInStudySize(grouperUri);
        }
        return notFound(ApiUtil.createResponse("/api/page doss not recognize class uri " + classUri + ".", false));
    }

    private Result getObjectsInSOCSize(String socUri) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectCollection soc = ObjectCollection.find(socUri);
        if(soc == null){
            return notFound(ApiUtil.createResponse("SOC with uri " + socUri + " not found", false));
        }
        int totalResultSize = soc.getCollectionSize();
        if(totalResultSize < 1){
            return notFound(ApiUtil.createResponse("SOC with uri " + socUri + " not found", false));
        }
        int results = StudyObject.getNumberStudyObjectsByCollection(socUri);
        if(results <= 0){
            return notFound(ApiUtil.createResponse("SOC with URI " + socUri + "not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getSOCsInStudySize(String studyUri) {
        ObjectMapper mapper = new ObjectMapper();
        Study study = Study.find(studyUri);
        if (study == null){
            return notFound(ApiUtil.createResponse("Study with uri " + studyUri + " not found", false));
        }
        int results = ObjectCollection.getNumberCollectionsByStudy(studyUri);
        if (results <= 0) {
            return notFound(ApiUtil.createResponse("SOCs from study with URI " + studyUri + "not found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

}
