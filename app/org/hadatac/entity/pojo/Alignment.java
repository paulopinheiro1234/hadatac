package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.console.models.Pivot;

public class Alignment {
	private Map<String, StudyObject> objects = new HashMap<String, StudyObject>();
	private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
	
	public Alignment() {}

	public void fromPivot(Pivot pivot) {
	    for (Pivot child : pivot.children) {
	        if (child.getField().equals("object_uri_str")) {
	            StudyObject studyObject = StudyObject.find(child.getValue());
	            if (studyObject != null) {
	                addObject(studyObject);
	            }
	        }
	        
	        if (child.getField().equals("characteristic_uri_str")) {
                Attribute attrib = Attribute.find(child.getValue());
                if (attrib != null) {
                    addAttribute(attrib);
                }
            }
	    }
	}
	
	public boolean containsObject(String uri) {
	    return objects.containsKey(uri);
	}
	
	public boolean containsAttribute(String uri) {
        return attributes.containsKey(uri);
    }
	
	public StudyObject getObject(String uri) {
	    return objects.get(uri);
	}
	
	public Attribute getAttribute(String uri) {
        return attributes.get(uri);
    }
	
	public List<StudyObject> getObjects() {
		return new ArrayList<StudyObject>(objects.values());
	}

	public List<Attribute> getAttributes() {
		return new ArrayList<Attribute>(attributes.values());
	}
	
	public void addObject(StudyObject obj) {
        objects.put(obj.getUri(), obj);
    }
	
	public void addAttribute(Attribute attribute) {
	    attributes.put(attribute.getUri(), attribute);
	}
}
