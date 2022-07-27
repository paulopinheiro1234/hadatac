package org.hadatac.console.controllers.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.NameSpace;
import org.hadatac.entity.pojo.Study;
import org.hadatac.utils.ApiUtil;
import org.hadatac.vocabularies.HASCO;
import play.mvc.Controller;
import play.mvc.Result;

public class ListConcept extends Controller {

    public Result getList(String classUri) {
        switch (classUri) {
            case HASCO.ONTOLOGY:
                return getOntologies();
        }
        return notFound(ApiUtil.createResponse("/api/list doss not recognize class uri " + classUri + ".", false));
    }

    private Result getOntologies() {
        ObjectMapper mapper = new ObjectMapper();
        java.util.List<NameSpace> results = NameSpace.findAll();
        if (results == null) {
            return notFound(ApiUtil.createResponse("No ontology has been found", false));
        } else {
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

}
