package org.hadatac.console.controllers.objectcollections;

import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.ObjectCollection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class OCForceFieldGraph {

	Study study = null;
	List<OCNode> objects = new ArrayList<OCNode>();

	public OCForceFieldGraph(String mode, String filename, String da_uri, String std_uri) {
		if (std_uri == null || std_uri.equals("")) {
			return;
		}
		study = Study.find(std_uri);
		objects.add(new OCNode(study.getUri(), study.getLabel(), OCNode.STUDY, ""));
		addCollectionNodes(mode, study);
	}

	private void addCollectionNodes(String mode, Study study){

		List<ObjectCollection> ocList = ObjectCollection.findByStudy(study);
		for (ObjectCollection oc : ocList) {			
			int type = -1;
			if ((mode.equals("full")  || mode.equals("space") || mode.equals("collections")) && (oc.isLocationCollection())) {
				type = OCNode.SPACECOLLECTION;
				objects.add(new OCNode(oc.getUri(), oc.getLabel(), type, study.getUri()));
				if (!mode.equals("collections")) {
					addObjectNodes(oc);
				}
			} else if ((mode.equals("full")  || mode.equals("time") || mode.equals("collections")) && oc.isTimeCollection()) {
				type = OCNode.TIMECOLLECTION;
				objects.add(new OCNode(oc.getUri(), oc.getLabel(), type, study.getUri()));
				if (!mode.equals("collections")) {
					addObjectNodes(oc);
				}
			} else if (mode.equals("full") || mode.equals("collections")) {
				type = OCNode.COLLECTION;
				objects.add(new OCNode(oc.getUri(), oc.getLabel(), type, study.getUri()));
				if (!mode.equals("collections")) {
					addObjectNodes(oc);
				}
			}
		}
	}

	private void addObjectNodes(ObjectCollection oc){
		List<StudyObject> soList = StudyObject.findByCollection(oc);
		for (StudyObject so : soList) {
			int type = -1;
			if (so.isLocation()) {
				type = OCNode.SPACEOBJECT;
			} else if (so.isTime()) {
				type = OCNode.TIMEOBJECT;
			} else {
				type = OCNode.OBJECT;
			}
			objects.add(new OCNode(so.getUri(), so.getLabel(), type, oc.getUri()));
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

	private String toJson() {
		JSONObject tree = new JSONObject();

		JSONArray nodes = new JSONArray();
		Iterator<OCNode> ag = objects.iterator();
		while (ag.hasNext()) {
			OCNode tmpObject = ag.next();
			JSONObject object = new JSONObject();
			System.out.println(tmpObject.getName());
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
			System.out.println(tmpObject.getName() + "=====");
			System.out.println(tmpObject.getMemberOf() + "!!!!!");
			if (!tmpObject.getMemberOf().equals("")) {
				int ind = findObjectIndex(tmpObject.getMemberOf());
				if (ind == -1) {
					System.out.println("Invalid memberOf info for " + tmpObject.getURI() + " under " + tmpObject.getMemberOf());
				}
				else {
					edge.put("source", objects.indexOf(tmpObject));
					edge.put("target", ind);
					edge.put("value", 4);
					links.add(edge);

				}
			}
		}
		tree.put("links", links);
		System.out.println(tree.toJSONString());

		return tree.toJSONString();
	}

	public String getQueryResult() {
		if (objects.size() == 0){
			return "";
		}
		else{
			return toJson();
		}
	} 
}
