package org.hadatac.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import play.Play;
import play.libs.Json;


public class ApiUtil {
    public static ObjectNode createResponse(Object response, boolean ok) {
        ObjectNode result = Json.newObject();
        result.put("isSuccessful", ok);
        if (response instanceof String) {
            result.put("body", (String) response);
        } else {
            result.put("body", (JsonNode) response);
        }
        return result;
    }// /createResponse

}// 
