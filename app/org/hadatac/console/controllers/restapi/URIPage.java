package org.hadatac.console.controllers.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hadatac.entity.pojo.*;
import org.hadatac.utils.ApiUtil;
import org.hadatac.utils.HASCO;
import play.mvc.Controller;
import play.mvc.Result;

public class URIPage extends Controller {

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
            //System.out.println("inside getUri(): URI [" + uri + "]");

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

    }

    private Result processResult(Object result, String uri) {
        ObjectMapper mapper = new ObjectMapper();
        //System.out.println("[RestAPI] processing object: " + uri);
        JsonNode jsonObject = null;
        try {
            ObjectNode obj = mapper.convertValue(result, ObjectNode.class);
            jsonObject = mapper.convertValue(obj, JsonNode.class);
            //System.out.println(prettyPrintJsonString(jsonObject));
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

}
