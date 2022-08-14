package org.hadatac.console.controllers.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.hadatac.entity.pojo.*;
import org.hadatac.utils.ApiUtil;
import org.hadatac.vocabularies.HASCO;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class ListPage extends Controller {

    private final static int MAX_PAGE_SIZE = 80;

    public Result getPage(String classUri, int offset, int pageSize) {
        System.out.println("[RestAPI] inside ListPage. ClassUri is [" + classUri + "]");
        switch (classUri) {
            case HASCO.STUDY:
                return getStudies(offset, pageSize);
            case HASCO.DA_SCHEMA_ATTRIBUTE:
                return getVariables(offset, pageSize);
            case HASCO.DA_SCHEMA_OBJECT:
                return getEntities(offset, pageSize);
            case HASCO.VARIABLE_SPEC:
                return getVariableSpecs(offset, pageSize);
            case HASCO.ONTOLOGY:
                return getOntologies(offset, pageSize);
        }
        return notFound(ApiUtil.createResponse("/api/page doss not recognize class uri " + classUri + ".", false));
    }

    private Result getStudies(int offset, int pageSize) {
        if(pageSize < 1) {
            pageSize = MAX_PAGE_SIZE;
            System.out.println("[RestAPI] getObjectsInCollection : Yikes! Resetting that page size for you!");
        }
        ObjectMapper mapper = new ObjectMapper();
        List<Study> results = Study.findWithPages(pageSize, offset);
        if (results == null) {
            return notFound(ApiUtil.createResponse("No study has been found", false));
        } else {
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter("studyFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
            mapper.setFilterProvider(filterProvider);
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getVariables(int offset, int pageSize) {
        if(pageSize < 1) {
            pageSize = MAX_PAGE_SIZE;
            System.out.println("[RestAPI] getVariables : Yikes! Resetting that page size for you!");
        }
        ObjectMapper mapper = new ObjectMapper();
        List<DataAcquisitionSchemaAttribute> results = DataAcquisitionSchemaAttribute.findWithPages(pageSize, offset);
        if (results == null) {
            return notFound(ApiUtil.createResponse("No variable has been found", false));
        } else {
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter("variableFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
            mapper.setFilterProvider(filterProvider);
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getEntities(int offset, int pageSize) {
        if(pageSize < 1) {
            pageSize = MAX_PAGE_SIZE;
            System.out.println("[RestAPI] getEntities : Yikes! Resetting that page size for you!");
        }
        ObjectMapper mapper = new ObjectMapper();
        List<DataAcquisitionSchemaObject> results = DataAcquisitionSchemaObject.findWithPages(pageSize, offset);
        if (results == null) {
            return notFound(ApiUtil.createResponse("No entity has been found", false));
        } else {
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter("sddObjectFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
            mapper.setFilterProvider(filterProvider);
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getVariableSpecs(int offset, int pageSize) {
        if (pageSize < 1) {
            pageSize = MAX_PAGE_SIZE;
            System.out.println("[RestAPI] getVariables : Yikes! Resetting that page size for you!");
        }
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("[RestAPI] getVariableSpecs");
        List<VariableSpec> results = VariableSpec.findWithPages(pageSize, offset);
        if (results == null) {
            return notFound(ApiUtil.createResponse("No variable spec has been found", false));
        } else {
            System.out.println("[RestAPI] getVariableSpecs. Size of results [" + results.size() + "]");
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter("variableSpecFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
            mapper.setFilterProvider(filterProvider);
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getOntologies(int offset, int pageSize) {
        if(pageSize < 1) {
            pageSize = MAX_PAGE_SIZE;
            System.out.println("[RestAPI] getOntologies : Yikes! Resetting that page size for you!");
        }
        ObjectMapper mapper = new ObjectMapper();
        List<NameSpace> results = NameSpace.findWithPages(pageSize, offset);
        if (results == null) {
            return notFound(ApiUtil.createResponse("No ontology has been found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    public Result getPageSize(String classUri) {
        switch (classUri) {
            case HASCO.STUDY:
                return getStudiesSize();
            case HASCO.DA_SCHEMA_ATTRIBUTE:
                return getVariablesSize();
            case HASCO.DA_SCHEMA_OBJECT:
                return getEntitiesSize();
            case HASCO.VARIABLE_SPEC:
                return getVariableSpecsSize();
            case HASCO.ONTOLOGY:
                return getOntologiesSize();
        }
        return notFound(ApiUtil.createResponse("/api/page doss not recognize class uri " + classUri + ".", false));
    }

    private Result getStudiesSize() {
        ObjectMapper mapper = new ObjectMapper();
        int results = Study.getNumberStudies();
        if(results <= 0){
            return notFound(ApiUtil.createResponse("No study has been found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getVariablesSize() {
        ObjectMapper mapper = new ObjectMapper();
        int results = DataAcquisitionSchemaAttribute.getNumberDASAs();
        if(results <= 0){
            return notFound(ApiUtil.createResponse("No variable has been found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getEntitiesSize() {
        ObjectMapper mapper = new ObjectMapper();
        int results = DataAcquisitionSchemaObject.getNumberDASOs();
        if(results <= 0){
            return notFound(ApiUtil.createResponse("No entity has been found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getVariableSpecsSize() {
        ObjectMapper mapper = new ObjectMapper();
        int results = VariableSpec.getNumberVariableSpecs();
        if(results <= 0){
            return notFound(ApiUtil.createResponse("No variable spec has been found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

    private Result getOntologiesSize() {
        ObjectMapper mapper = new ObjectMapper();
        int results = NameSpace.getNumberOntologies();
        if(results <= 0){
            return notFound(ApiUtil.createResponse("No ontology has been found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

}
