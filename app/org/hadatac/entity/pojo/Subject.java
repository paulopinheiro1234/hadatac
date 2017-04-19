package org.hadatac.entity.pojo;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;


public class Subject {
	public String uri = "";
	public String type = "";
	public String label = "";
	public String ofStudy = "";
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getOfStudy() {
		return ofStudy;
	}
	public void setOfStudy(String ofStudy) {
		this.ofStudy = ofStudy;
	}
	
	public static boolean isPlatform(String subject_uri) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?cohort WHERE {\n"
				+ "  <" + subject_uri + "> a hasco:SubjectPlatform . \n"
				+ "  <" + subject_uri + "> hasco:isSubjectOf ?cohort . \n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() >= 1) {
			return true;
		}
		
		return false;
	}
	
	public static Subject findSubject(String study_uri, String subject_id) {
		Subject subject = new Subject();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?uri WHERE {\n"
				+ "  ?uri hasco:originalID \"" + subject_id + "\" .\n"
				+ "  ?uri hasco:isSubjectOf ?cohort .\n"
				+ "  ?cohort hasco:isCohortOf " + study_uri + " .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			subject.setUri(soln.getResource("uri").getURI());
		}
		
		return subject;
	}
	
	public static String findSampleUri(String study_uri, String sample_id) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?sampleUri WHERE {\n"
				+ "  ?sampleUri hasco:originalID \"" + sample_id + "\" .\n"
				+ "  ?sampleUri hasco:isSampleOf ?subjectUri .\n"
				+ "  ?subjectUri hasco:isSubjectOf ?cohort .\n"
				+ "  ?cohort hasco:isCohortOf " + study_uri + " .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			return soln.getResource("sampleUri").getURI();
		}
		
		return null;
	}
	
	public static String findCodeValue(String attr_uri, String code) {
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?codeValue WHERE {"
                + " ?uri hasco:hasReference <" + attr_uri + ">."
                + " ?value hasco:isPossibleValueOf ?uri . "
                + " ?value hasco:hasCode \"" + code + "\" . "
                + " OPTIONAL { ?value hasco:hasCodeValue ?codeValue . }"        
                + " }";
        
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
        ResultSet results = qexec.execSelect();
        ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
        
        if (resultsrw.size() > 0) {
            QuerySolution soln = resultsrw.next();
            if (null != soln.getLiteral("codeValue")) {
            	String codeValue = soln.getLiteral("codeValue").toString();
            	if (!codeValue.equals("")) {
            		return codeValue;
            	}
            }
        }
        
        return null;
    }
	
	public static String checkObjectUri(String attr_uri, String obj_uri) {
		
		String objuri = obj_uri;
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?s ?o WHERE {"
                + " <" + attr_uri + "> rdfs:label ?l."
                + " ?s hasco:hasAssociatedObject ?o . "
                + " ?s rdfs:label ?l. "       
                + " }";
        
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
        ResultSet results = qexec.execSelect();
        ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
        
        if (resultsrw.size() > 0) {
            QuerySolution soln = resultsrw.next();
            if (null != soln.getLiteral("o")) {
            	String attributeAssociation = soln.getLiteral("o").toString();
            	if (attributeAssociation.equals("<http://hadatac.org/kb/chear#ObjectTypeMother>")) {
            		String motheruri = 	obj_uri+"-mother";
            		Model model = ModelFactory.createDefaultModel();
            		DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(Collections.METADATA_GRAPH);
            		Resource subject = model.createResource(motheruri);
            		Property predicate = model.createProperty("rdf:type");
            		Resource object = model.createResource("http://semanticscience.org/resource/Human");
            		Statement s = ResourceFactory.createStatement(subject, predicate, object);
            		Property predicate2 = model.createProperty("http://hadatac.org/ont/chear#Mother");
            		Resource object2 = model.createResource(obj_uri);
            		Statement s2 = ResourceFactory.createStatement(subject, predicate2, object2);            		
            		model.add(s);
            		model.add(s2);
            		accessor.add(model);
            		objuri = motheruri;
            	}
            }
        }
        return objuri;
    }
	
	
	Model model = ModelFactory.createDefaultModel();
	DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(Collections.getCollectionsName(Collections.METADATA_SPARQL));
	
}


