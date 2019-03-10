package org.hadatac.console.controllers.objectcollections;

import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class OCForceFieldGraph {

	public static String COMPLETE = "COMPLETE";
	public static String NO_TIME = "NO_TIME";
	public static String NO_SPACE = "NO_SPACE";
	public static String NO_TIME_SPACE = "NO_TIME_SPACE";

	Study study = null;
    List<OCNode> objects = new ArrayList<OCNode>();
    List<ObjectCollection> ocList = null;
    
    public OCForceFieldGraph(String mode, String std_uri) {
    	if (std_uri == null || std_uri.equals("")) {
    		return;
    	}
    	study = Study.find(std_uri);
    	if (study != null) {
    		objects.add(new OCNode(study.getUri(), study.getId(), OCNode.STUDY, "",  new ArrayList<>()));
    		addCollectionNodes(mode, study);
    	}
    }
    
    private void addCollectionNodes(String mode, Study study) {
    	ocList = ObjectCollection.findByStudyUri(study.getUri());
    	for (ObjectCollection oc : ocList) {			
    		int type = -1;
    		if ((mode.equals(NO_TIME) || mode.equals(NO_TIME_SPACE)) && oc.isTimeCollection()) {
    		} else if ((mode.equals("NO_SPACE") || mode.equals(NO_TIME_SPACE)) && oc.isLocationCollection()) {
    		} else {
    			if (oc.isLocationCollection()) {
    				type = OCNode.SPACECOLLECTION;
    				if (oc.getHasScopeUri() == null || oc.getHasScopeUri().equals("")) {
    					objects.add(new OCNode(oc.getUri(), oc.getRoleLabel() + " (" + oc.getNumOfObjects() + " objects)", type, "",  new ArrayList<>(Arrays.asList(study.getUri()))));
    					addCollectionNodes(mode,oc);
    				}
    			} else if (oc.isTimeCollection()) {
    				type = OCNode.TIMECOLLECTION;
    				if (oc.getHasScopeUri() == null || oc.getHasScopeUri().equals("")) {
    					objects.add(new OCNode(oc.getUri(), oc.getRoleLabel() + " (" + oc.getNumOfObjects() + " objects)", type,  "",  new ArrayList<>(Arrays.asList(study.getUri()))));
    					addCollectionNodes(mode,oc);
    				}
    			} else {
    				type = OCNode.COLLECTION;
    				if (oc.getHasScopeUri() == null || oc.getHasScopeUri().equals("")) {
    					objects.add(new OCNode(oc.getUri(), oc.getRoleLabel() + " (" + oc.getNumOfObjects() + " objects)", type,  "",  new ArrayList<>(Arrays.asList(study.getUri()))));
    					addCollectionNodes(mode,oc);
    				}
    			}
    		}
    	}
    }
    
   private void addCollectionNodes(String mode, ObjectCollection objCollection) {
       for (ObjectCollection oc : ocList) {			
    	   int type = -1;
    	   if ((mode.equals(NO_TIME) || mode.equals(NO_TIME_SPACE)) && oc.isTimeCollection()) {
    	   } else if ((mode.equals("NO_SPACE") || mode.equals(NO_TIME_SPACE)) && oc.isLocationCollection()) {
    	   } else {
    		   if (oc.isLocationCollection()) {
    			   type = OCNode.SPACECOLLECTION;
    			   if (oc.getHasScopeUri().equals(objCollection.getUri())) {
    				   objects.add(new OCNode(oc.getUri(), oc.getRoleLabel() + " (" + oc.getNumOfObjects() + " objects)", type,  "",  new ArrayList<>(Arrays.asList(objCollection.getUri()))));
    				   addCollectionNodes(mode,oc);
    			   }
    		   } else if (oc.isTimeCollection()) {
    			   type = OCNode.TIMECOLLECTION;
    			   if (oc.getHasScopeUri().equals(objCollection.getUri())) {
    				   objects.add(new OCNode(oc.getUri(), oc.getRoleLabel() + " (" + oc.getNumOfObjects() + " objects)", type,  "",  new ArrayList<>(Arrays.asList(objCollection.getUri()))));
    				   addCollectionNodes(mode,oc);
    			   }
    		   } else {
    			   type = OCNode.COLLECTION;
    			   if (oc.getHasScopeUri().equals(objCollection.getUri())) {
    				   objects.add(new OCNode(oc.getUri(), oc.getRoleLabel() + " (" + oc.getNumOfObjects() + " objects)", type,  "",  new ArrayList<>(Arrays.asList(objCollection.getUri()))));
    				   addCollectionNodes(mode,oc);
    			   }
    		   }
    	   }
       }
   }
 
    private int findObjectIndex(String uri) {
    	Iterator<OCNode> ag = objects.iterator();
    	if (uri.equals("Public")){
    		return 0;
    	}
    	while (ag.hasNext()) {
    		OCNode tmpObject = ag.next();
    		if (tmpObject.getURI().equals(uri)){
    			return objects.indexOf(tmpObject);
    		}
    	}
    	return -1;
    }
    
    @SuppressWarnings("unchecked")
	private String toJson() {
    	JSONObject tree = new JSONObject();
	
    	JSONArray nodes = new JSONArray();
    	Iterator<OCNode> ag = objects.iterator();
    	while (ag.hasNext()) {
    		OCNode tmpObject = ag.next();
    		JSONObject object = new JSONObject();
    		object.put("name", tmpObject.getName());
    		object.put("group", tmpObject.getType() + 1);
    		nodes.add(object);
    	}
    	tree.put("nodes", nodes);
		
    	JSONArray links = new JSONArray();
    	ag = objects.iterator();
    	while (ag.hasNext()) {
    		OCNode tmpObject = ag.next();
    		JSONObject edge = new JSONObject();
    		if (tmpObject.getMemberOf().size() > 0) {
    			int ind = findObjectIndex(tmpObject.getMemberOf().get(0));
    			if (ind == -1) {
    				System.out.println("Invalid memberOf info for " + tmpObject.getURI() + " under " + tmpObject.getMemberOf().get(0));
    			} else {
    				edge.put("source", objects.indexOf(tmpObject));
    				edge.put("target", ind);
    				edge.put("value", 4);
    				links.add(edge);
    			}
    		}
    	}
    	tree.put("links", links);
	
    	return tree.toJSONString();
    }
    
    public String getQueryResult() {
    	if (objects.size() == 0){
    		return "";
    	} else{
    		return toJson();
    	}	
    } 

	private String toTreeJson() {
    	String tree = "";
    	int i=1;
    	for (OCNode node : objects) {
    		if (node.getMemberOf() == null || node.getMemberOf().size() <= 0) {
    			tree += "[ { v:\'" + node.getName() + "\', f:\'" + node.getURI() + "\'},\'" + node.getName() + "\',\'" + node.getName()+ "\']";
    		} else {
    			tree += "[ { v:\'" + node.getName() + "\', f:\'" + node.getURI() + "\'},\'" + node.getMemberOf().get(0) + "\',\'" + node.getName() + "\']";
    		}
    		if (i++ < objects.size()) {
    			tree += ",";
    		}
    	}

    	return tree;
    }
    
    public String getTreeQueryResult() {
    	if (objects.size() == 0){
    		return "";
    	} else{
    		return toTreeJson();
    	}	
    } 
}
