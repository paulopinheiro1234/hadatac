package org.hadatac.console.controllers.objects;

import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.console.controllers.objectcollections.OCNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.Attribute;
import org.hadatac.entity.pojo.Entity;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.InRelationToInstance;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Unit;
import org.hadatac.metadata.loader.URIUtils;

public class ObjectForceFieldGraph {
	
    List<OCNode> nodes = new ArrayList<OCNode>();
    List<StudyObject> obj = null;
    
    public ObjectForceFieldGraph(StudyObject obj) {
    	if (obj == null) {
    		return;
    	}
    	//System.out.println("ObjectForceFieldGraph: JSON=[" + toJson() + "]");
    	addObject(obj, "", "");
    }
        
    private void addObject(StudyObject obj, String toLabel, String fromLabel) {
    	
    	// Verify if object has already been added
    	if (getNodeWithUri(obj.getUri()) != null) {
    		return;
    	}

    	// add the object itself and its properties
    	List<String> scopeUris = StudyObject.retrieveScopeUris(obj.getUri());
    	ObjectCollection soc = ObjectCollection.find(obj.getIsMemberOf());
    	String nameNode = soc.getRoleLabel() + " " + obj.getOriginalId();
    	if (fromLabel != null && !fromLabel.equals("")) {
    		updateNodeDependency(fromLabel, nameNode);
    	}
    	List<String> toList = new ArrayList<String>();
    	if (toLabel != null && !toLabel.equals("")) {
    		toList.add(toLabel);
    	}

    	OCNode objNode = new OCNode(nameNode, obj.getUri(), OCNode.OBJECT, objectHtml(nameNode, obj), toList);
    	nodes.add(objNode);
    	addObjectProperties(obj, objNode);

    	// add objects in the scope of the current object
    	if (scopeUris != null) {
    		for (String scopeObjUri : scopeUris) {
    			StudyObject scopeObj = StudyObject.find(scopeObjUri);
    			if (scopeObj != null) {
    				addObject(scopeObj, nameNode, "");
    			}
    		}
    	}

       	// add objects that have the current in their scope 
    	List<String> urisScopedByThisUri = StudyObject.retrieveUrisScopedByThisUri(obj.getUri());
    	if (urisScopedByThisUri != null) {
    		for (String scopeObjUri : urisScopedByThisUri) {
    			StudyObject scopeObj = StudyObject.find(scopeObjUri);
    			if (scopeObj != null) {
    				addObject(scopeObj, "", nameNode);
    			}
    		}
    	}
    }
    
    private void addObjectProperties(StudyObject obj, OCNode dependedOn) {
    	List<Measurement> measurements = Measurement.findByObjectUri(obj.getUri()); 
    	//System.out.println("ObjectForceField: total_properties=" + measurements.size());
    	for (Measurement m : measurements) {
        	//System.out.println("ObjectForceField: label=" + m.getLabel());
        	String attrStr = "";
        	for (String attr_uri : m.getCharacteristicUris()) {
        		Attribute attr = Attribute.find(attr_uri);
        		if (attrStr.equals("")) {
        			attrStr = attr.getLabel();
        		} else {
        			attrStr += attrStr + "; " + attr.getLabel();
        		}
        	} 
        	String irtStr = HADatAcThing.getLabel(m.getInRelationToUri());
        	if (irtStr == null) {
        		irtStr = "";
        	}
        	if (irtStr.length() > 0) {
        		irtStr = irtStr + " ";
        	}
        	attrStr = "[" + irtStr + attrStr + "]";
        	String valueStr = m.getValue();
        	if (valueStr.startsWith("http")) {
        		valueStr = HADatAcThing.getLabel(valueStr);
        	}
        	nodes.add(new OCNode(valueStr + " " + attrStr, m.getUri(), OCNode.PROPERTY,  propertyHtml(m), new ArrayList<>(Arrays.asList(dependedOn.getName()))));
    	}
    }
    
    private OCNode getNodeWithUri(String uri) {
    	if (nodes.size() > 0) {
    		for (OCNode nd: nodes) {
    			if (nd.getURI() != null && nd.getURI().equals(uri)) {
    				return nd;
    			}
    		}
    	}
    	return null;
    }
    
    private void updateNodeDependency(String label, String dependency) {
    	if (nodes.size() > 0) {
    		for (OCNode nd: nodes) {
    			if (nd.getName() != null && nd.getName().equals(label)) {
    				List<String> depList = nd.getMemberOf();
    				depList.add(dependency);
    			}
    		}
    	}
    }
    
    private String objectHtml(String id, StudyObject obj) {
    	String html = "";
    	html += "<h3>Object Details</h3>";
    	html += "<b>Internal Id</b>: " + id + "<br>"; 
    	html += "<b>Original Id</b>: " + obj.getOriginalId() + "<br>";
    	Entity ent = Entity.find(obj.getTypeUri());
    	html += "<b>Entity</b>: " + ent.getLabel() + " (" + URIUtils.replaceNameSpace(obj.getTypeUri()) + ") <br>";
    	if (ent.getComment() != null && !ent.getComment().equals("")) {
    		html += "<ul><li><b>Description</b>: " + ent.getComment();
    		html += "</li></ul>";
    	}
    	html += "<b>URI</b>: " + URIUtils.replaceNameSpace(obj.getUri()) + "<br>";
    	return html;
    }
    
    private String propertyHtml(Measurement m) {
    	String html = "";
    	html += "<h3>Property Details</h3>";
    	html += "<b>Value</b>: " + m.getValue() + " <br>"; 
    	html += "<b>Original Value</b>: " + m.getOriginalValue() + " <br>"; 
		html += "<b>Attribute</b>: "; 
    	for (String attrStr : m.getCharacteristicUris()) {
    		Attribute attr = Attribute.find(attrStr);
    		html += attr.getLabel() + " ";
    	}
    	html += " (";
    	for (String attrUriStr : m.getCharacteristicUris()) {
    		html += URIUtils.replaceNameSpace(attrUriStr) + " ";
    	}
    	html += ") <br>";
    	if (m.getInRelationToUri() != null && !m.getInRelationToUri().equals("")) { 
    		html += "<b>InRelationTo</b>: " + HADatAcThing.getLabel(m.getInRelationToUri()) + " (" + URIUtils.replaceNameSpace(m.getInRelationToUri()) + ") <br>";
    	}
    	Unit unit = Unit.find(m.getUnitUri());
    	if (unit != null) {
    		html += "<b>Unit</b>: " + unit.getLabel() + " (" + URIUtils.replaceNameSpace(m.getUnitUri()) + ") <br>";
    	}
    	html += "<b>Source</b>: <br>"; 
    	return html;
    }
    
    @SuppressWarnings("unchecked")
	private String toJson() {
    	
    	JSONObject dag = new JSONObject();
	
    	JSONObject ndObj = new JSONObject();
    	Iterator<OCNode> objs = nodes.iterator();
    	while (objs.hasNext()) {
    		OCNode tmpObject = objs.next();
    		JSONObject object = new JSONObject();
    		//System.out.println(tmpObject.getName());
    		String nodeType = null;
    		if (tmpObject.getType() == OCNode.OBJECT) {
    			nodeType = "object";
    		} else if (tmpObject.getType() == OCNode.PROPERTY) {
    			nodeType = "property";
    		} else {
    			nodeType = "";
    		}
    		object.put("type", nodeType);
    		object.put("name", tmpObject.getName());
    		object.put("docs", tmpObject.getHtmlDoc());
    		JSONArray depList = new JSONArray();
    		if (tmpObject.getMemberOf() != null && tmpObject.getMemberOf().size() > 0) {
    			for (String str : tmpObject.getMemberOf()) {
    				depList.add(str);
    			}
    		}
    		object.put("depends", depList);
    		object.put("dependedOnBy", new JSONArray());
    		ndObj.put(tmpObject.getName(),object);
    	}
    	dag.put("data", ndObj);

    	dag.put("errors", new JSONArray());
    	
    	//System.out.println(dag.toJSONString());
	
    	return dag.toJSONString();
    }
    
    public String getQueryResult() {
    	if (nodes.size() == 0){
    		return "";
    	} else{
    		return toJson();
    	}	
    } 

}
