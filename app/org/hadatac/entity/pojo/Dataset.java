package org.hadatac.entity.pojo;

import java.util.List;

import org.hadatac.data.loader.util.Sparql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;

public class Dataset {
	private String localName;
	private String ccsvUri;
	private String uri;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public List<MeasurementType> measurementTypes;
	
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
		String queryString = Sparql.prefix
				+ "SELECT ?ds WHERE {\n"
				+ "  ?ds a vstoi:Dataset .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() == 1) {
			QuerySolution soln = resultsrw.next();
			Dataset dataset = new Dataset();
			dataset.setLocalName(soln.getResource("ds").getLocalName());
			dataset.setCcsvUri(soln.getResource("ds").getURI());
			return dataset;
		} else

		return null;
	}
}
