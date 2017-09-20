package org.hadatac.entity.pojo;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.query.ResultSetFormatter;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.Collections;
import org.hadatac.console.models.TreeNode;

public class HADatAcClass {

	private String className = "";
	public  String uri;
	public  String superUri;
	public  String localName;
	public  String label;
	public  String comment;
	
	public HADatAcClass (String currentClassName) {
		this.className = currentClassName; 
	}
	
	public String getClassName() {
		return className;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSuperUri() {
		return superUri;
	}

	public void setSuperUri(String superUri) {
		this.superUri = superUri;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getHierarchyJson() {
		//System.out.println("Inside HADatAcClass's getHierarchyJson: [" + className + "]");
		String collection = "";
		String q = 
				"SELECT ?id ?superId ?label ?comment WHERE { " + 
						"   ?id rdfs:subClassOf* " + className + " . " + 
						"   ?id rdfs:subClassOf ?superId .  " + 
						"   OPTIONAL { ?id rdfs:label ?label . } " + 
						"   OPTIONAL { ?id rdfs:comment ?comment . } " +
						"}";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + q;
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.
					getCollectionsName(Collections.METADATA_SPARQL), query);
			ResultSet results = qexec.execSelect();
			ResultSetFormatter.outputAsJSON(outputStream, results);
			qexec.close();

			//System.out.println(outputStream.toString("UTF-8"));

			return outputStream.toString("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public TreeNode getHierarchy() {
		String collection = "";
		TreeNode newTree = null;
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
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + q;
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.
					getCollectionsName(Collections.METADATA_SPARQL), query);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();

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
			qexec.close();

			TreeNode result = buildTree(branchCollection);
			return result.getChildren().get(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new TreeNode("");
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

}

