package org.hadatac.console.controllers.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hadatac.entity.pojo.*;
import org.hadatac.utils.ApiUtil;
import org.hadatac.utils.HASCO;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class ListPage extends Controller {

    private final static int MAX_PAGE_SIZE = 80;
    private final static String DATAFILE_URI = "http://hadatac.org/ont/hasco/DataFile";

    public Result getPage(String classUri, int offset, int pageSize) {
        switch (classUri) {
            case HASCO.STUDY:
                return getStudies(offset, pageSize);
            case HASCO.DA_SCHEMA_ATTRIBUTE:
                return getVariables(offset, pageSize);
            case HASCO.DA_SCHEMA_OBJECT:
                return getEntities(offset, pageSize);
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
        System.out.println("Inside API/ListPage calling getVariables()");
        List<DataAcquisitionSchemaAttribute> results = DataAcquisitionSchemaAttribute.findWithPages(pageSize, offset);
        System.out.println("Result of getVariables(): " + results);
        if (results == null) {
            return notFound(ApiUtil.createResponse("No variable has been found", false));
        } else {
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
        System.out.println("Inside API/ListPage calling getEntities()");
        List<DataAcquisitionSchemaObject> results = DataAcquisitionSchemaObject.findWithPages(pageSize, offset);
        System.out.println("Result of getEntities(): " + results);
        if (results == null) {
            return notFound(ApiUtil.createResponse("No entity has been found", false));
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

}
