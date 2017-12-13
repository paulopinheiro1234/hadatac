package org.hadatac.console.controllers.restapi;

import java.util.List;

import org.hadatac.utils.ApiUtil;
import org.hadatac.entity.pojo.Study;
import org.hadatac.utils.State;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import org.hadatac.console.controllers.AuthApplication;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import play.mvc.Result;
import play.mvc.Controller;
import play.libs.Json;

public class RestApi extends Controller {

    //@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result getStudies(){
        ObjectMapper mapper = new ObjectMapper();
        List<Study> theStudies = Study.find();
        System.out.println("[RestApi] found " + theStudies.size() + " things");
        //for (Study std : theStudies){
        //    System.out.println("[RestApi] retreived URI " + std.getUri());
        //}
        JsonNode jsonObject = mapper.convertValue(theStudies, JsonNode.class);

        return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getStudies()

    public static Result getStudy(String studyUri){
        ObjectMapper mapper = new ObjectMapper();
        Study result = Study.find(studyUri);
        JsonNode jsonObject = mapper.convertValue(result, JsonNode.class);
        
        return ok(ApiUtil.createResponse(jsonObject, true));
    }// /getStudy()

}// /RestApi
