package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class DetectorType extends HADatAcClass implements Comparable<DetectorType> {

	static String className = "vstoi:Detector";
	
	private String url;

	public DetectorType () {
		super(className);
	}

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }
    
    public String getSuperLabel() {
    	DetectorType superInsType = DetectorType.find(getSuperUri());
    	if (superInsType == null || superInsType.getLabel() == null) {
    		return "";
    	}
    	return superInsType.getLabel();
    }


	public static List<DetectorType> find() {
		List<DetectorType> detectorTypes = new ArrayList<DetectorType>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				" ?uri rdfs:subClassOf* " + className + " . " + 
				"} ";

		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			DetectorType detectorType = find(soln.getResource("uri").getURI());
			detectorTypes.add(detectorType);
		}			

		java.util.Collections.sort((List<DetectorType>) detectorTypes);
		return detectorTypes;

	}

	public static Map<String,String> getMap() {
		List<DetectorType> list = find();
		Map<String,String> map = new HashMap<String,String>();
		for (DetectorType typ: list) 
			map.put(typ.getUri(),typ.getLabel());
		return map;
	}

	public static DetectorType find(String uri) {
		DetectorType detectorType = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
		model = qexec.execDescribe();

		detectorType = new DetectorType();
		StmtIterator stmtIterator = model.listStatements();

		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				detectorType.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#hasWebDocumentation")) {
				detectorType.setURL(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				detectorType.setSuperUri(object.asResource().getURI());
			}
		}
		
		detectorType.setUri(uri);
		detectorType.setLocalName(uri.substring(uri.indexOf('#') + 1));

		return detectorType;
	}

	@Override
	public int compareTo(DetectorType another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getLocalName().compareTo(another.getLocalName());
	}

}
