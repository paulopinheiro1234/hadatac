package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeVariable {

    private StudyObject obj;
    private Entity ent;
    private String role;
    private List<Attribute> attrList;
    private Entity inRelationTo;
    private Unit unit;

    public TimeVariable(StudyObject obj, AlignmentEntityRole entRole, AttributeInRelationTo attrInRel) {
        this(obj, entRole, attrInRel, null);
    }

    public TimeVariable(StudyObject obj, AlignmentEntityRole entRole, AttributeInRelationTo attrInRel, Unit unit) {
        this.obj = obj;
    	this.ent = entRole.getEntity();
    	this.role = entRole.getRole();
    	this.attrList = attrInRel.getAttributeList();
    	this.inRelationTo = attrInRel.getInRelationTo();
    	this.unit = unit;
    }

    public String getKey() {
    	return getObject() + getRole() + getEntityStr() + getAttributeListStr() + getInRelationToStr() + getUnitStr();
    }

    public String getObject() {
        return obj.getOriginalId();
    }

    public Entity getEntity() {
        return ent;
    }

    public String getEntityStr() {
        if (ent != null && ent.getUri() != null && !ent.getUri().equals("")) {
            return ent.getUri();
        }
        return "";
    }

    public String getRole() {
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
    		if (attr == null || attr.getUri() == null || !attr.getUri().equals("")) { 
    			return "";
    		}
    		resp = resp + attr.getUri();
    	}
        return resp;
    }


    public Entity getInRelationTo() {
        return inRelationTo;
    }

    public String getInRelationToStr() {
        if (inRelationTo != null && inRelationTo.getUri() != null && !inRelationTo.getUri().equals("")) {
            return inRelationTo.getUri();
        }
        return "";
    }

    public Unit getUnit() {
        return unit;
    }

    public String getUnitStr() {
        if (unit != null && unit.getUri() != null && !unit.getUri().equals("")) {
            return unit.getUri();
        }
        return "";
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
    	if (obj.getOriginalId() != null && !obj.getOriginalId().equals("")) {
    		str += prep(obj.getOriginalId()) + "-";
    	}
    	if (role != null && !role.equals("")) {
    		str += prep(role) + "-";
    	}
    	str += prep(ent.getLabel());
    	for (Attribute attr : attrList) {
    		if (attr != null && attr.getLabel() != null) {
    			str += "-" + prep(attr.getLabel());
    		}
    	}
    	if (inRelationTo != null && !inRelationTo.getLabel().equals("")) {
    		str += "-" + prep(inRelationTo.getLabel());
    	}
    	if (unit != null) {
    		str += "-" + prep(unit.getLabel());
    	}
    	return str;
    }

}
