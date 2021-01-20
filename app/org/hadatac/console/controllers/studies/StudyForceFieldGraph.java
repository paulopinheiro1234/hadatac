package org.hadatac.console.controllers.studies;

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
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.console.controllers.objects.ObjectForceFieldGraph;

public class StudyForceFieldGraph {

    List<OCNode> nodes = null;
    Study study = null;
    List<ObjectCollection> socList = null;
    Map<String,OCNode> socs = null;
    Map<String,OCNode> objects = null;

    public StudyForceFieldGraph(String study_uri, int maxObjs) {
        if (study_uri == null || study_uri.equals("")) {
            return;
        }
        nodes = new ArrayList<OCNode>();
        socs = new HashMap<String, OCNode>();
        objects = new HashMap<String, OCNode>();
        study = Study.find(study_uri);
        if (study == null) {
            return;
        }
        addStudy(study, "", "", maxObjs);
    }

    private void addStudy(Study study, String toLabel, String fromLabel, int maxObjs) {

        // Verify if study has already been added
        if (getNodeWithUri(study.getUri()) != null) {
            return;
        }

        // add study itself and its attributes
        socList = ObjectCollection.findByStudyUri(study.getUri());
        String nameNode = "Study " + study.getId();

        OCNode studyNode = new OCNode(nameNode, study.getUri(), OCNode.STUDY, studyHtml(nameNode, study), new ArrayList<>());
        nodes.add(studyNode);
        for (ObjectCollection soc : socList) {
            if (soc.getHasScopeUri() == null || soc.getHasScopeUri().equals("")) {
                addStudySOC(soc, studyNode, maxObjs);
            }
        }

    }

    private void addStudySOC(ObjectCollection soc, OCNode dependedOn, int maxObjs) {
        String socLabel = "";
        // to add role label
        socLabel += soc.getRoleLabel();
        OCNode socNode = new OCNode(socLabel, soc.getUri(), OCNode.SOC, socHtml(soc), new ArrayList<>(Arrays.asList(dependedOn.getName())));
        nodes.add(socNode);
        List<StudyObject> objList = StudyObject.findByCollectionWithPages(soc, maxObjs, 0);
        for (StudyObject obj: objList) {
            addSOCObject(obj,socNode);
        }
        String scopeUri = "";
        if (soc != null && soc.getUri() != null) {
            scopeUri = soc.getUri();
        }
        for (ObjectCollection subSoc : socList) {
            if (subSoc.getHasScopeUri() != null && subSoc.getHasScopeUri().equals(scopeUri)) {
                addStudySOC(subSoc, socNode, maxObjs);
            }
        }
    }

    private void addSOCObject(StudyObject obj, OCNode dependedOn) {
        if (obj != null) {
            String objLabel = obj.getLabel();
            OCNode objNode = new OCNode(objLabel, obj.getUri(), OCNode.OBJECT, ObjectForceFieldGraph.objectHtml(obj), new ArrayList<>(Arrays.asList(dependedOn.getName())));
            nodes.add(objNode);
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

    private String studyHtml(String id, Study study) {
        String html = "";
        html += "<h3>Study Details</h3>";
        html += "<b>Id</b>: " + id + "<br>";
        html += "<b>Title</b>: " + study.getTitle() + "<br>";
        html += "<b>Description</b>: " + study.getComment() + "<br>";
        html += "<b>URI</b>: " + URIUtils.replaceNameSpace(study.getUri()) + "<br>";
        return html;
    }

    private String socHtml(ObjectCollection soc) {
        String html = "";
        html += "<h3>Semantic Object Collection Details</h3>";
        html += "<b>Role</b>: " + soc.getRoleLabel() + "<br>";
        if (soc.getGroundingLabel() != null && !soc.getGroundingLabel().equals("")) {
            html += "<b>Grounding Label</b>: " + soc.getGroundingLabel() + "<br>";
            html += "<b>Reference</b>: " + soc.getSOCReference() + "<br>";
        }
        return html;
    }

    @SuppressWarnings("unchecked")
    public String toJson() {

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
            } else if (tmpObject.getType() == OCNode.SOC) {
                nodeType = "soc";
            } else if (tmpObject.getType() == OCNode.OBJECT) {
                nodeType = "object";
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

}
