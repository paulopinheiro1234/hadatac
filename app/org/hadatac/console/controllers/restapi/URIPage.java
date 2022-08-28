package org.hadatac.console.controllers.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.hadatac.entity.pojo.*;
import org.hadatac.utils.ApiUtil;
import org.hadatac.vocabularies.HASCO;
import org.hadatac.vocabularies.SIO;
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
                return processResult(measurementResult, HASCO.VALUE, measurementResult.getUri());
            }

            DataFile dataFileResult = DataFile.findByUri(uri);
            if (dataFileResult != null && dataFileResult.getTypeUri() != null && dataFileResult.getTypeUri().equals(HASCO.DATA_FILE)) {
                return processResult(dataFileResult, HASCO.DATA_FILE, dataFileResult.getUri());
            }

            NameSpace nameSpaceResult = NameSpace.find(uri);
            if (nameSpaceResult != null && nameSpaceResult.getTypeUri() != null && nameSpaceResult.getTypeUri().equals(HASCO.ONTOLOGY)) {
                return processResult(nameSpaceResult, HASCO.ONTOLOGY, nameSpaceResult.getUri());
            }

            /*
             *  Now uses GenericInstance to process URI against TripleStore content
             */

            Object finalResult = null;
            String typeUri = null;
            GenericInstance result = GenericInstance.find(uri);
            System.out.println("inside getUri(): URI [" + uri + "]");

            if (result == null) {
                return notFound(ApiUtil.createResponse("No generic instance found for uri [" + uri + "]", false));
            }

            /*
            if (result.getHascoTypeUri() == null || result.getHascoTypeUri().isEmpty()) {
                System.out.println("inside getUri(): typeUri [" + result.getTypeUri() + "]");
                if (!result.getTypeUri().equals("http://www.w3.org/2002/07/owl#Class")) {
                    return notFound(ApiUtil.createResponse("No valid HASCO type found for uri [" + uri + "]", false));
                }
            }
             */

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
            } else if (result.getHascoTypeUri().equals(HASCO.VARIABLE_SPEC)) {
                finalResult = VariableSpec.find(uri);
                if (finalResult != null) {
                    typeUri = ((VariableSpec) finalResult).getHascoTypeUri();
                    System.out.println("typeUri for Variable Spec is [" + typeUri + "]");
                } else {
                    System.out.println("FinalResult is null for Variable Spec.");
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
                /*
            } else if (result.getTypeUri().equals("http://www.w3.org/2002/07/owl#Class")) {
                finalResult = HADatAcClass.find(uri);

                if (finalResult != null) {
                    typeUri = ((HADatAcClass) finalResult).getHascoTypeUri();
                }
                 */
            } else {
                finalResult = result;
                if (finalResult != null) {
                    typeUri = ((GenericInstance) finalResult).getHascoTypeUri();
                }
            }
            if (finalResult == null || typeUri == null || typeUri.equals("")){
                return notFound(ApiUtil.createResponse("No type-specific instance found for uri [" + uri + "]", false));
            }

            // list object properties and associated classes

            return processResult(finalResult, result.getHascoTypeUri(), uri);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(ApiUtil.createResponse("Error processing URI [" + uri + "]", false));
        }

    }

    private Result processResult(Object result, String typeResult, String uri) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();

        // STUDY OBJECT
        if (typeResult.equals(HASCO.STUDY)) {
            filterProvider.addFilter("studyFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("studyFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // STUDY OBJECT
        if (typeResult.equals(HASCO.OBJECT_COLLECTION)) {
            filterProvider.addFilter("studyObjectFilter", SimpleBeanPropertyFilter.serializeAllExcept("measurements"));
        } else if (typeResult.equals(HASCO.STUDY_OBJECT)) {
            filterProvider.addFilter("studyObjectFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("studyObjectFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // VALUE
        if (typeResult.equals(HASCO.VALUE)) {
            filterProvider.addFilter("valueFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("valueFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment", "studyObjectUri", "variable"));
        }

        // DEPLOYMENT
        if (typeResult.equals(HASCO.DEPLOYMENT)) {
            filterProvider.addFilter("deploymentFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("deploymentFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // STR
        if (typeResult.equals(HASCO.DATA_ACQUISITION)) {
            filterProvider.addFilter("strFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("strFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // DATA FILE
        if (typeResult.equals(HASCO.DATA_FILE)) {
            filterProvider.addFilter("dataFileFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("dataFileFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // DA_SCHEMA
        if (typeResult.equals(HASCO.DA_SCHEMA)) {
            filterProvider.addFilter("sddFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("sddFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // DA_SCHEMA_ATTRIBUTE
        if (typeResult.equals(HASCO.DA_SCHEMA_ATTRIBUTE)) {
            filterProvider.addFilter("variableFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("variableFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment", "variableSpec"));
        }

        // VARIABLE_SPEC
        if (typeResult.equals(HASCO.VARIABLE_SPEC)) {
            filterProvider.addFilter("variableSpecFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("variableSpecFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // DA_SCHEMA_OBJECT
        if (typeResult.equals(HASCO.DA_SCHEMA_OBJECT)) {
            filterProvider.addFilter("sddObjectFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("sddObjectFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // ENTITY
        if (typeResult.equals(SIO.ENTITY)) {
            filterProvider.addFilter("entityFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("entityFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        mapper.setFilterProvider(filterProvider);

        System.out.println("[RestAPI] generating JSON for following object: " + uri);
        JsonNode jsonObject = null;
        try {
            ObjectNode obj = mapper.convertValue(result, ObjectNode.class);
            jsonObject = mapper.convertValue(obj, JsonNode.class);
            System.out.println(prettyPrintJsonString(jsonObject));
        } catch (Exception e) {
            e.printStackTrace();
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
