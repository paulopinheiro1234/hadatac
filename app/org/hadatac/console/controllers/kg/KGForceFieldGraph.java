package org.hadatac.console.controllers.kg;

import org.hadatac.console.controllers.objectcollections.OCNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.Attribute;
import org.hadatac.metadata.loader.URIUtils;

public class KGForceFieldGraph {
	
    List<OCNode> nodes = new ArrayList<OCNode>();
    List<Study> studies = null;
    Map<String,OCNode> attributes = new HashMap<String, OCNode>();
    
    public KGForceFieldGraph() {
    	System.out.println("HERE MAIN 1");
    	studies = Study.find();
    	if (studies == null) {
    		return;
    	}
    	for (Study study: studies) { 
    		//System.out.println("StudiesForceFieldGraph: JSON=[" + toJson() + "]");
    		addStudy(study, "", "");
    	}
    	System.out.println("HERE MAIN 2");
    }
        
    private void addStudy(Study study, String toLabel, String fromLabel) {
    	
    	// Verify if study has already been added
    	if (getNodeWithUri(study.getUri()) != null) {
    		return;
    	}

    	System.out.println("HERE ADD STUDY 1");

    	// add study itself and its attributes
    	List<Attribute> attributes = Attribute.findByStudy(study.getUri());
    	String nameNode = "Study " + study.getId();

    	OCNode studyNode = new OCNode(nameNode, study.getUri(), OCNode.STUDY, studyHtml(nameNode, study), new ArrayList<>());
    	nodes.add(studyNode);
    	int i = 0;
    	for (Attribute att : attributes) {
    		addStudyAttribute(att, studyNode);
        	System.out.println("HERE ADD STUDY 2 [" + i + "]");
    	}
    	System.out.println("HERE ADD STUDY 3");

    }
    
    private void addStudyAttribute(Attribute att, OCNode dependedOn) {
    	System.out.println("HERE ADD ATTRIBUTE 1");
    	nodes.add(new OCNode(att.getLabel(), att.getUri(), OCNode.ATTRIBUTE, attributeHtml(att), new ArrayList<>(Arrays.asList(dependedOn.getName()))));   	
    	System.out.println("HERE ADD ATTRIBUTE 2");
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
    
    private String studyHtml(String id, Study study) {
    	String html = "";
    	html += "<h3>Study Details</h3>";
    	html += "<b>Id</b>: " + id + "<br>"; 
    	html += "<b>Title</b>: " + study.getTitle() + "<br>";
    	html += "<b>Description</b>: " + study.getComment() + "<br>"; 
    	html += "<b>URI</b>: " + URIUtils.replaceNameSpace(study.getUri()) + "<br>";
    	return html;
    }
    
    private String attributeHtml(Attribute att) {
    	String html = "";
    	html += "<h3>Attribute Details</h3>";
		html += "<b>Label</b>: " + att.getClassName() + "<br>"; 
    	html += "<b>Source</b>: <br>"; 
    	return html;
    }
    
    @SuppressWarnings("unchecked")
	private String toJson() {
    	
    	JSONObject dag = new JSONObject();
	
    	JSONObject ndstudy = new JSONObject();
    	Iterator<OCNode> studys = nodes.iterator();
    	while (studys.hasNext()) {
    		OCNode tmpObject = studys.next();
    		JSONObject object = new JSONObject();
    		//System.out.println(tmpstudyect.getName());
    		String nodeType = null;
    		if (tmpObject.getType() == OCNode.STUDY) {
    			nodeType = "study";
    		} else if (tmpObject.getType() == OCNode.ATTRIBUTE) {
    			nodeType = "attribute";
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
    		ndstudy.put(tmpObject.getName(),object);
    	}
    	dag.put("data", ndstudy);

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
