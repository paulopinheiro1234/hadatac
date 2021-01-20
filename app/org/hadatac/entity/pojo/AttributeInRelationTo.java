package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class AttributeInRelationTo {

    private List<Attribute> attrList;
    private Entity inRelationTo;
    
    public AttributeInRelationTo(List<Attribute> attrList, Entity inRelationTo) {
	   this.attrList = attrList;
	   this.inRelationTo = inRelationTo;
    }

    public String getKey() {
    	if (attrList == null) {
    		return "";
    	}
    	String attrUris = "";
    	for (Attribute attr : attrList) {
    		attrUris = attrUris + attr.getUri();
    	}
    	if (inRelationTo == null || inRelationTo.getUri().isEmpty()) {
    		return attrUris;
    	}
    	return attrUris + inRelationTo.getUri();
    }

    public List<Attribute> getAttributeList() {
    	return attrList;
    }

    public Entity getInRelationTo() {
    	return inRelationTo;
    }

    public String toString() {
    	if (attrList == null || attrList.size() <= 0) {
    		System.out.println("ERROR: AttributeInRelationTo: called toString() with null argument.");
    		return "";
    	}
    	String listLabel = "";
        Iterator<Attribute> iter = attrList.iterator();
        while (iter.hasNext()) {
            listLabel = listLabel + iter.next().getLabel();
            if (iter.hasNext()) {
            	listLabel = listLabel + "-";
            }
        }
    	if (inRelationTo == null || inRelationTo.equals("")) {
    		return listLabel;
    	} else {
    		return listLabel + "-" + inRelationTo;
    	}
    }

}
