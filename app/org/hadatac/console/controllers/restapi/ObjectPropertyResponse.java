package org.hadatac.console.controllers.restapi;

import org.hadatac.entity.pojo.GenericInstance;

import java.util.List;

public class ObjectPropertyResponse {

    private boolean more_objects;

    private String object_type;

    private List<GenericInstance> response;

    public ObjectPropertyResponse (List<GenericInstance> response, String object_type, boolean more_objects) {
        this.response = response;
        this.object_type = object_type;
        this.more_objects = more_objects;
    }

    public boolean getMoreObjects() {
        return more_objects;
    };

    public String getObjectType() {
        return object_type;
    };

    public  List<GenericInstance> getResponse() {
        return response;
    };

}
