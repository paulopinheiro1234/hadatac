package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Variable {

    private Entity ent;
    private String role;
    private List<Attribute> attrList;
    private Entity inRelationTo;
    private Unit unit;
    private Attribute timeAttr;

    public Variable(AlignmentEntityRole entRole, AttributeInRelationTo attrInRel) {
    	this(entRole, attrInRel, null, null);
    }

    public Variable(AlignmentEntityRole entRole, AttributeInRelationTo attrInRel, Unit unit) {
    	this(entRole, attrInRel, unit, null);
    }

    public Variable(AlignmentEntityRole entRole, AttributeInRelationTo attrInRel, Unit unit, Attribute timeAttr) {
    	this.ent = entRole.getEntity();
    	this.role = entRole.getRole();
    	this.attrList = attrInRel.getAttributeList();
    	this.inRelationTo = attrInRel.getInRelationTo();
    	this.unit = unit;
    	this.timeAttr = timeAttr;
    }

    public String getKey() {
    	return getRole() + getEntityStr() + getAttributeListStr() + getInRelationToStr() + getUnitStr() + getTimeStr();
    }

    public Entity getEntity() {
    	return ent;
    }

    public String getEntityStr() {
        if (ent == null || ent.getUri() == null || ent.getUri().isEmpty()) { 
        	return "";
        }
    	return ent.getUri();
    }

    public String getRole() {
    	if (role == null) {
    		return "";
    	}
    	return role;
    }

    public List<Attribute> getAttributeList() {
    	return attrList;
    }

    public String getAttributeListStr() {
    	if (attrList == null || attrList.isEmpty()) {
    		return "";
    	}
    	String resp = "";
    	for (Attribute attr : attrList) {
    		if (attr != null && attr.getUri() != null && !attr.getUri().isEmpty()) { 
        		resp = resp + attr.getUri();
    		}
    	}
        return resp;
    }

    public Entity getInRelationTo() {
    	return inRelationTo;
    }

    public String getInRelationToStr() {
        if (inRelationTo == null || inRelationTo.getUri() == null ||inRelationTo.getUri().isEmpty()) { 
            return "";
        }
    	return inRelationTo.getUri();
    }

    public Unit getUnit() {
    	return unit;
    }

    public String getUnitStr() {
        if (unit == null || unit.getUri() == null || unit.getUri().isEmpty()) { 
            return "";
        }
    	return unit.getUri();
    }

    public Attribute getTime() {
    	return timeAttr;
    }

    public String getTimeStr() {
        if (timeAttr == null || timeAttr.getUri() == null || timeAttr.getUri().isEmpty()) { 
            return "";
        }
    	return timeAttr.getUri();
    }

    public static String upperCase(String orig) {
    	String[] words = orig.split(" ");
    	StringBuffer sb = new StringBuffer();

    	for (int i = 0; i < words.length; i++) {
    		sb.append(Character.toUpperCase(words[i].charAt(0)))
    		.append(words[i].substring(1)).append(" ");
    	}          
    	return sb.toString().trim();
    }      

    public String prep(String orig) {
    	String aux = upperCase(orig);
    	return aux.replaceAll(" ","-").replaceAll("[()]","");
    }

    public String toString() {
    	//System.out.println("[" + attr.getLabel() + "]");
    	String str = "";
    	if (role != null && !role.isEmpty()) {
    		str += prep(role) + "-";
    	}
    	if (ent != null && ent.getLabel() != null && !ent.getLabel().isEmpty()) {
    		str += prep(ent.getLabel());
    	}
    	if (attrList != null && attrList.size() > 0) {
	    	for (Attribute attr : attrList) {
	    		if (attr != null && attr.getLabel() != null && !attr.getLabel().isEmpty()) {
	    			str += "-" + prep(attr.getLabel());
	    		}
	    	}
    	}
    	if (inRelationTo != null && !inRelationTo.getLabel().isEmpty()) {
    		str += "-" + prep(inRelationTo.getLabel());
    	}
    	if (unit != null && unit.getLabel() != null && !unit.getLabel().isEmpty()) {
    		str += "-" + prep(unit.getLabel());
    	}
    	if (timeAttr != null && timeAttr.getLabel() != null && !timeAttr.getLabel().isEmpty()) {
    		str += "-" + prep(timeAttr.getLabel());
    	}
    	return str;
    }

}
