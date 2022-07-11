package org.hadatac.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

public class ApiUtil {
    public static ObjectNode createResponse(Object response, boolean ok) {
        ObjectNode result = null;
        try {
            result = Json.newObject();
            result.put("isSuccessful", ok);
            if (response instanceof String) {
                result.put("body", (String) response);
            } else {
                result.set("body", (JsonNode) response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
