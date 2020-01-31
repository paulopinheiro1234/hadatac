package org.hadatac.entity.pojo;

import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.hadatac.utils.NameSpaces;

public class Dataset {
	private String localName;
	private String ccsvUri;
	private String uri;
	private List<MeasurementType> measurementTypes;
	
	public List<MeasurementType> getMeasurementTypes() {
		return measurementTypes;
	}
	public void setMeasurementTypes(List<MeasurementType> types) {
		this.measurementTypes = types;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public Dataset() {
	}

	public String getCcsvUri() {
		return ccsvUri;
	}

	public void setCcsvUri(String ccsvUri) {
		this.ccsvUri = ccsvUri;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}
	
	public static Dataset find(Model model) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT ?ds WHERE {\n"
				+ "  ?ds a vstoi:Dataset .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			Dataset dataset = new Dataset();
			dataset.setLocalName(soln.getResource("ds").getLocalName());
			dataset.setCcsvUri(soln.getResource("ds").getURI());
			return dataset;
		}

		return null;
	}
}
