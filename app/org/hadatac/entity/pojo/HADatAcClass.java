package org.hadatac.entity.pojo;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.query.ResultSetFormatter;
import org.hadatac.utils.NameSpaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hadatac.utils.CollectionUtil;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.TreeNode;

public class HADatAcClass extends HADatAcThing {

    private String className = "";
    public  String uri = "";
    public  String superUri = "";
    public  String localName = "";
    public  String label = "";
    public  String comment = "";

    public HADatAcClass (String currentClassName) {
        if (currentClassName == null) {
            currentClassName = "";
        }
        this.className = currentClassName; 
    }

    public String getClassName() {
        return className;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        if (uri == null) {
            uri = "";
        }
        this.uri = uri;
    }

    public String getSuperUri() {
        return superUri;
    }

    public void setSuperUri(String superUri) {
        if (superUri == null) {
            superUri = "";
        }
        this.superUri = superUri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (label == null) {
            label = "";
        }
        this.label = label;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        if (localName == null) {
            localName = "";
        }
        this.localName = localName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment == null) {
            comment = "";
        }
        this.comment = comment;
    }

    public static int getNumberClasses() {
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "select (COUNT(?categ) as ?tot) where " +  
                " { SELECT ?c (COUNT(?c) as ?categ) " +
                "     WHERE {" + 
                "             [] a ?c . " +
                "     } " +
                " GROUP BY ?c " + 
                " }";

        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                return Integer.parseInt(soln.getLiteral("tot").getString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public String getSuperClassLabel() {
    	HADatAcClass superType = findGeneric(this.getSuperUri());
    	if (superType == null || superType.getLabel() == null) {
    		return "";
    	}
    	return superType.getLabel();
    }


    public List<HADatAcClass> findGeneric() {
        List<HADatAcClass> typeClasses = new ArrayList<HADatAcClass>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf* " + className + " . " + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            HADatAcClass typeClass = findGeneric(soln.getResource("uri").getURI());
            typeClasses.add(typeClass);
        }			


        typeClasses.sort(Comparator.comparing(HADatAcClass::getLabel, (label1, label2) -> {
        	int compare = label1.compareTo(label2);  
        	return compare;
        }));
        
        return typeClasses;   
    }

    public HADatAcClass findGeneric(String uri) {
    	HADatAcClass typeClass = null;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);
        
        StmtIterator stmtIterator = model.listStatements();

        // returns null if not statement is found
        if (!stmtIterator.hasNext()) {
        	return typeClass;
        }
        
        typeClass = new HADatAcClass("");

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
            	typeClass.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
            	typeClass.setSuperUri(object.asResource().getURI());
            }
        }

        typeClass.setUri(uri);
        typeClass.setLocalName(uri.substring(uri.indexOf('#') + 1));

        return typeClass;
    }

    
    @JsonIgnore
    public String getHierarchyJson() {
        //System.out.println("Inside HADatAcClass's getHierarchyJson: [" + className + "]");
        String q = 
                "SELECT ?id ?superId ?label ?superLabel ?comment WHERE { " + 
                        "   ?id rdfs:subClassOf* " + className + " . " + 
                        "   ?id rdfs:subClassOf ?superId .  " + 
                        "   OPTIONAL { ?id rdfs:label ?label . } " + 
                        "   OPTIONAL { ?superId rdfs:label ?superLabel . } " + 
                        "   OPTIONAL { ?id rdfs:comment ?comment . } " +
                        "}";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + q;
            ResultSetRewindable resultsrw = SPARQLUtils.select(CollectionUtil.
                    getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
            ResultSetFormatter.outputAsJSON(outputStream, resultsrw);

            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @JsonIgnore
    public String getHierarchyJson2() {
        //System.out.println("Inside HADatAcClass's getHierarchyJson: [" + className + "]");
        String q = 
                "SELECT ?model ?superModel ?modelName ?superModelName ?comment WHERE { " + 
                        "   ?model rdfs:subClassOf* " + className + " . " + 
                        "   ?model rdfs:subClassOf ?superModel .  " + 
                        "   OPTIONAL { ?model rdfs:label ?modelName . } " + 
                        "   OPTIONAL { ?superModel rdfs:label ?superModelName . } " + 
                        "   OPTIONAL { ?model rdfs:comment ?comment . } " +
                        "}";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + q;
            ResultSetRewindable resultsrw = SPARQLUtils.select(CollectionUtil.
                    getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
            ResultSetFormatter.outputAsJSON(outputStream, resultsrw);

            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @JsonIgnore
    public TreeNode getHierarchy() {
        String node = null;
        String superNode = null;
        String nodeLabel = null;
        ArrayList<TreeNode> branchCollection = new ArrayList<TreeNode>();
        String q = 
                "SELECT ?id ?superId ?label WHERE { " + 
                        "   ?id rdfs:subClassOf* " + className + " . " + 
                        "   ?id rdfs:subClassOf ?superId .  " + 
                        "   ?id rdfs:label ?label .  " + 
                        "}";
        try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + q;

            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                uri = "";
                superUri = "";
                label = "";
                if (soln != null && soln.getResource("id") != null && soln.getResource("id").getURI() != null) {
                    node  = soln.getResource("id").getURI();
                }
                if (soln != null && soln.getResource("superId") != null && soln.getResource("superId").getURI() != null) {
                    superNode = soln.getResource("superId").getURI();
                }
                if (soln != null && soln.getLiteral("label") != null && soln.getLiteral("label").getString() != null) {
                    nodeLabel = soln.getLiteral("label").getString();
                }
                TreeNode currentBranch = new TreeNode(superNode);
                currentBranch.addChild(node);
                branchCollection.add(currentBranch);
            }

            TreeNode result = buildTree(branchCollection);
            if (result.getChildren() == null) {
                return null;
            }
            return result.getChildren().get(0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TreeNode("");
    }

    @JsonIgnore
    public String mapTypeLabelToUri() {
        String nodeUri = null;
        String nodeLabel = null;
        Map<String,String> resp = new HashMap<String,String>();
        String q = 
                "SELECT ?id ?label WHERE { " + 
                        "   ?id rdfs:subClassOf* " + className + " . " + 
                        "   ?id rdfs:label ?label .  " + 
                        "}";
        try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + q;

            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                if (soln != null && soln.getResource("id") != null && soln.getResource("id").getURI() != null) {
                    nodeUri  = soln.getResource("id").getURI();
                }
                if (soln != null && soln.getLiteral("label") != null && soln.getLiteral("label").getString() != null) {
                    nodeLabel = soln.getLiteral("label").getString();
                    nodeLabel = nodeLabel.replaceAll(" ", "");
                }
                resp.put(nodeLabel, nodeUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        String output = "{";
        boolean first = true;
        Iterator<Map.Entry<String, String>> itr = resp.entrySet().iterator(); 
        while(itr.hasNext()) {
        	if (first) {
        		first = false;
        	} else {
        		output = output + ",";
        	}
             Map.Entry<String, String> entry = itr.next(); 
             output = output + "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
        }         
        output = output + "}"; 
        //System.out.println("[" + output + "]");
        return output;
    }

    public static String getLabelByUri(String uri, Class<?> cls) {
        try {
            Method method = cls.getMethod("find", String.class);
            HADatAcClass instance = (HADatAcClass)method.invoke(null, uri);
            if (instance != null) {
                return instance.getLabel();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return "";
    }

    private TreeNode buildTree(List<TreeNode> inputTree){
        TreeNode resultsTree = new TreeNode("Empty");
        ArrayList<TreeNode> assignedBranches = new ArrayList<TreeNode>();
        int numIterations = 0;
        int maxIterations = 20;
        while (assignedBranches.size() != inputTree.size() && numIterations<maxIterations){
            numIterations++;
            for (TreeNode tn : inputTree){
                if (!assignedBranches.contains(tn)){
                    if (resultsTree.getName().equals("Empty")) {
                        resultsTree = new TreeNode(tn.getName());
                        resultsTree.addChild(tn.getChildren().get(0));
                        assignedBranches.add(tn);
                    } else {
                        if (resultsTree.hasValue(tn.getName())!=null){
                            TreeNode branchOfInterest = resultsTree.hasValue(tn.getName());
                            branchOfInterest.addChild(tn.getChildren().get(0));
                            assignedBranches.add(tn);
                        } else {
                            if (tn.hasValue(resultsTree.getName())!=null) {
                                TreeNode newBranch = new TreeNode(tn.getName());
                                newBranch.addChild(resultsTree);
                                resultsTree = newBranch;
                                assignedBranches.add(tn);
                            } 
                        }
                    }
                }
            }
        }

        return resultsTree;
    }

    public String toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String output =  mapper.writeValueAsString(this);
            return output;
        } catch (Exception e) {
            System.out.println("[ERROR] HADatAcClass.toJSON() - Exception message: " + e.getMessage());
        }
        return "";
    }

    @Override
    public boolean saveToTripleStore() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void deleteFromTripleStore() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean saveToSolr() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int deleteFromSolr() {
        // TODO Auto-generated method stub
        return 0;
    }

}

