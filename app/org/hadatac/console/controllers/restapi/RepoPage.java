package org.hadatac.console.controllers.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hadatac.entity.pojo.HADatAcClass;
import org.hadatac.entity.pojo.Repository;
import org.hadatac.utils.ApiUtil;
import play.mvc.Controller;
import play.mvc.Result;

public class RepoPage extends Controller {

    public Result getRepository(){
        System.out.println("Inside ClassPage.getHADatAcClass");

        ObjectMapper mapper = new ObjectMapper();

        Repository repository = new Repository();

        System.out.println("[RestAPI] class: " + Repository.className);

        try {
            // get the list of variables in that study
            // serialize the Study object first as ObjectNode
            //   as JsonNode is immutable and meant to be read-only
            ObjectNode obj = mapper.convertValue(repository, ObjectNode.class);
            JsonNode jsonObject = mapper.convertValue(obj, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(ApiUtil.createResponse("Error parsing class " + Repository.className, false));
        }
    }

}
