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
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Entity;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpace;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;

public class KGForceFieldGraph {

    List<OCNode> nodes = null;
    List<Study> studies = null;
    List<NameSpace> ontologies = null;
    List<Deployment> deployments = null;
    List<DataAcquisitionSchema> sdds = null;
    List<STR> daspecs = null;
    Map<String,OCNode> variables = null;
    boolean includeOntologies;
    boolean includeIndicators;
    boolean includeDeployments;
    boolean includeSDDs;
    boolean includeDASpecs;

    public KGForceFieldGraph(boolean includeOntologies, boolean includeIndicators, boolean includeDeployments, boolean includeSDDs, boolean includeDASpecs) {
        this.includeOntologies = includeOntologies;
        this.includeIndicators = includeIndicators;
        this.includeDeployments = includeDeployments;
        this.includeSDDs = includeDeployments;
        this.includeDASpecs = includeDASpecs;
        nodes = new ArrayList<OCNode>();
        variables = new HashMap<String, OCNode>();

        // Ontologies need to be added before than studies
        if (includeOntologies) {
            //System.out.println("=========================================== ONTOLOGIES");
            ontologies = NameSpaces.getInstance().getOrderedNamespacesAsList();
            if (ontologies != null && ontologies.size() > 0) {
                for (NameSpace ont: ontologies) {
                    addOntology(ont);
                }
            }
        }

        if (includeDeployments) {
            //System.out.println("=========================================== DEPLOYMENTS");
            State state = new State(State.ACTIVE);
            deployments = Deployment.find(state);
            if (deployments != null && deployments.size() > 0) {
                for (Deployment dpl: deployments) {
                    addDeployment(dpl);
                }
            }
        }

        if (includeSDDs) {
            //System.out.println("=========================================== SDDs");
            sdds = DataAcquisitionSchema.findAll();
            if (sdds != null && sdds.size() > 0) {
                for (DataAcquisitionSchema sdd: sdds) {
                    addSDD(sdd);
                }
            }
        }

        //System.out.println("=========================================== STUDIES");
        studies = Study.find();
        if (studies != null && studies.size() > 0) {
            for (Study study: studies) {
                addStudy(study);
            }
        }

        // DASpecs need to be added after studies
        if (includeDASpecs) {
            //System.out.println("=========================================== DASpecs");
            daspecs = STR.findAll() ;
            if (daspecs != null && daspecs.size() > 0) {
                for (STR daspec: daspecs) {
                    addDASpec(daspec);
                }
            }
        }

        if (includeOntologies) {
            setHAScOHierarchy();
        }

        //System.out.println("=========================================== JSON");
        //System.out.println("JSON: [" + toJson() + "]");

    }

    private void addStudy(Study study) {

        if (study == null) {
            return;
        }

        //System.out.println("addStudy: " + study.getUri());
        // Verify if study has already been added
        if (getNodeWithUri(study.getUri()) != null) {
            return;
        }

        // add study itself and its attributes
        List<DataAcquisitionSchemaAttribute> variables = DataAcquisitionSchemaAttribute.findByStudy(study.getUri());
        String nameNode = "Study " + study.getId();
        OCNode studyNode = new OCNode(nameNode, study.getUri(), OCNode.STUDY, studyHtml(nameNode, study), new ArrayList<>());
        nodes.add(studyNode);
        for (DataAcquisitionSchemaAttribute variable : variables) {
            addStudyVariable(variable, studyNode);
        }
        if (includeOntologies) {
            OCNode domain = getNodeStartsWithName(ConfigProp.getBasePrefix() + " ");
            if (domain != null) {
                domain.addMember(nameNode);
            }
        }
    }

    private void addStudyVariable(DataAcquisitionSchemaAttribute variable, OCNode dependedOn) {
        if (variable == null || dependedOn == null || variable.getConcatAttributeLabel() == null || variable.getConcatAttributeLabel().equals("")) {
            return;
        }
        //System.out.println("addStudyVariable: " + variable.getConcatAttributeLabel());
        String varLabel = "";
        // to add role label
        varLabel += variable.getConcatAttributeLabel();
        if (variable.getInRelationToUri() != null && !variable.getInRelationToUri().equals("")) {
            DataAcquisitionSchemaObject irt = DataAcquisitionSchemaObject.find(variable.getInRelationToUri());
            if (irt == null || irt.getLabel() == null) {
                varLabel += " null";
            } else {
                varLabel += " " + irt.getLabel();
            }
        }
        List<String> listNS = new ArrayList<String>();
        if (variable.getEntityNamespace() != null) {
            int pos = variable.getEntityNamespace().indexOf(':');
            if (pos > 0) {
                listNS.add(variable.getEntityNamespace().substring(0, pos));
            }
        }
        if (variable.getAttributeNamespace() != null && variable.getAttributeNamespace().size() > 0) {
            for (String attStr : variable.getAttributeNamespace()) {
                if (attStr != null) {
                    int pos = attStr.indexOf(':');
                    if (pos > 0) {
                        String attNS = attStr.substring(0, pos);
                        if (!listNS.contains(attNS)) {
                            listNS.add(attNS);
                        }
                    }
                }
            }
        }
        if (includeOntologies) {
            for (String ns : listNS) {
                OCNode tmpNode = this.getNodeStartsWithName(ns);
                if (tmpNode != null) {
                    tmpNode.addMember(varLabel);
                }
            }
        }
        nodes.add(new OCNode(varLabel, variable.getUri(), OCNode.VARIABLE, variableHtml(variable), new ArrayList<String>(Arrays.asList(dependedOn.getName()))));
    }

    private void addOntology(NameSpace ont) {
        //System.out.println("Add ontology " + ont.getAbbreviation());
        if (ont == null) {
            return;
        }

        // add study itself and its attributes
        String nameNode = ont.getAbbreviation();
        if (ont.getURL() != null && !ont.getURL().equals("")) {
            if (ont.getNumberOfLoadedTriples() == 0) {
                return;
            } else {
                nameNode += " (" + ont.getNumberOfLoadedTriples() + " triples)";
            }
        }

        OCNode ontNode = new OCNode(nameNode, ont.getURL(), OCNode.ONTOLOGY, ontologyHtml(ont), new ArrayList<>());
        nodes.add(ontNode);
    }

    private void addDeployment(Deployment dpl) {
        //System.out.println("Add deployment " + dpl.getLabel());
        if (dpl == null) {
            return;
        }

        // add study itself and its attributes
        String nameNode = dpl.getInstrument().getLabel();

        OCNode ontNode = new OCNode(nameNode, dpl.getUri(), OCNode.DEPLOYMENT, deploymentHtml(dpl), new ArrayList<>());
        nodes.add(ontNode);
    }

    private void addSDD(DataAcquisitionSchema sdd) {
        //System.out.println("Add SDD " + sdd.getLabel());
        if (sdd == null) {
            return;
        }

        // add study itself and its attributes
        String nameNode = sdd.getLabel();

        OCNode ontNode = new OCNode(nameNode, sdd.getUri(), OCNode.SDD, sddHtml(sdd), new ArrayList<>());
        nodes.add(ontNode);
    }

    private void addDASpec(STR daspec) {
        //System.out.println("Add DASpec " + daspec.getLabel());
        if (daspec == null) {
            return;
        }

        // add study itself and its attributes
        String nameNode = daspec.getLabel();
        nameNode += " (" + daspec.getNumberDataPoints() + " data values)";

        OCNode daspecNode = new OCNode(nameNode, daspec.getUri(), OCNode.DASPEC, daspecHtml(daspec), new ArrayList<>());
        nodes.add(daspecNode);

        if (includeSDDs) {
            OCNode targetNode = getNodeWithUri(daspec.getSchemaUri());
            if (targetNode != null) {
                daspecNode.addMember(targetNode.getName());
            }
        }
        if (includeDeployments) {
            OCNode targetNode = getNodeWithUri(daspec.getDeploymentUri());
            if (targetNode != null) {
                daspecNode.addMember(targetNode.getName());
            }
        }
        OCNode studyNode = getNodeWithUri(daspec.getStudyUri());
        if (studyNode != null) {
            studyNode.addMember(daspecNode.getName());
        }
    }

    private String studyHtml(String id, Study study) {
        String html = "";
        html += "<h3>Study Details</h3>";
        html += "<b>Id</b>: " + id;
        html += " (see ";
        html += "<input type=\"button\" value=\"graph in new window\" onclick=\"msg('" + study.getUri() + "')\">";
        //html += "<form><input type=\"button\" value=\"" + study.getUri() + "\" onclick=\"msg('" + study.getUri() + "')\"></form>";
        //html += "<button id=\"callstudy\" class=\"study\" value=\"" + study.getUri() + "\">graph in new window</button>";
        html += ")<br>";
        html += "<b>Title</b>: " + study.getTitle() + "<br>";
        html += "<b>Description</b>: " + study.getComment() + "<br>";
        html += "<b>URI</b>: " + URIUtils.replaceNameSpace(study.getUri()) + "<br>";
        return html;
    }

    private String variableHtml(DataAcquisitionSchemaAttribute variable) {
        String html = "";
        html += "<h3>Variable Details</h3>";
        html += "<b>Entity</b>: " + variable.getEntityLabel() + "<br>";
        html += "<b>Attribute</b>: " + variable.getConcatAttributeLabel() + "<br>";
        if (variable.getInRelationToLabel() != null && !variable.getInRelationToLabel().equals("")) {
            html += "<b>In relation to</b>: " + variable.getInRelationToLabel() + "<br>";
        }
        if (variable.getUnitLabel() != null && !variable.getUnitLabel().equals("")) {
            html += "<b>Unit</b>: " + variable.getUnitLabel() + "<br>";
        }
        html += "<b>Source</b>: <br>";
        return html;
    }

    private String ontologyHtml(NameSpace ont) {
        String html = "";
        html += "<h3>Ontology</h3>";
        html += "<b>Abbreviation</b>: " + ont.getAbbreviation() + "<br>";
        html += "<b>Name</b>: " + ont.getName() + "<br>";
        if (ont.getURL() == null || ont.getURL().equals("")) {
            html += "<b># loaded triples</b>: this ontology is not configured to be loaded in this HADatAc<br>";
        } else {
            html += "<b>URL</b>: " + ont.getURL() + "<br>";
            html += "<b># loaded triples</b>: " + ont.getNumberOfLoadedTriples() + "<br>";
            html += "<b>Type</b>: " + ont.getType() + "<br>";
        }
        return html;
    }

    private String deploymentHtml(Deployment dpl) {
        String html = "";
        html += "<h3>Deployment</h3>";
        html += "<b>URI</b>: " + dpl.getUri() + "<br>";
        Platform plt = dpl.getPlatform();
        Instrument inst = dpl.getInstrument();
        html += "<b>Platform</b>: " + plt.getLabel() + "<br>";
        html += "<b>Instrument</b>: " + inst.getLabel() + "<br>";
        return html;
    }

    private String sddHtml(DataAcquisitionSchema sdd) {
        String html = "";
        html += "<h3>Semanic Data Dictionary</h3>";
        html += "<b>URI</b>: " + sdd.getUri() + "<br>";
        html += "<b>Name</b>: " + sdd.getLabel() + "<br>";
        html += "<b># of attributes</b>: " + sdd.getTotalDASA() + "<br>";
        html += "<b># of objects</b>: " + sdd.getTotalDASO() + "<br>";
        return html;
    }

    private String daspecHtml(STR daspec) {
        String html = "";
        html += "<h3>Data Acquisition Specification</h3>";
        html += "<b>URI</b>: " + daspec.getUri() + "<br>";
        html += "<b>Name</b>: " + daspec.getLabel() + "<br>";
        return html;
    }

    private OCNode getNodeWithUri(String uri) {
        if (nodes != null && nodes.size() > 0) {
            for (OCNode nd: nodes) {
                if (nd != null && nd.getURI() != null && nd.getURI().equals(uri)) {
                    return nd;
                }
            }
        }
        return null;
    }

    private OCNode getNodeStartsWithName(String name) {
        if (nodes != null && nodes.size() > 0) {
            for (OCNode nd: nodes) {
                if (nd != null && nd.getName() != null && nd.getName().startsWith(name)) {
                    return nd;
                }
            }
        }
        return null;
    }

    private void setHAScOHierarchy() {
        OCNode node = getNodeStartsWithName("sio ");
        OCNode targetNode = getNodeStartsWithName(ConfigProp.getBasePrefix() + " ");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("hasco ");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        targetNode = getNodeStartsWithName("hasco ");
        node = getNodeStartsWithName("vstoi ");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("prov ");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("uo ");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("rdfs ");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("owl ");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("rdf ");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("skos");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("foaf");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("xsd");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
        node = getNodeStartsWithName("dcterms");
        if (node != null && targetNode != null) {
            node.addMember(targetNode.getName());
        }
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
            } else if (tmpObject.getType() == OCNode.VARIABLE) {
                nodeType = "variable";
            } else if (tmpObject.getType() == OCNode.ONTOLOGY) {
                nodeType = "ontology";
            } else if (tmpObject.getType() == OCNode.DEPLOYMENT) {
                nodeType = "deployment";
            } else if (tmpObject.getType() == OCNode.SDD) {
                nodeType = "sdd";
            } else if (tmpObject.getType() == OCNode.DASPEC) {
                nodeType = "daspec";
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