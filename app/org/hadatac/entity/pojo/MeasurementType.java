package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hadatac.data.loader.util.Sparql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class MeasurementType {
	private String uri;
	private String localName;
	private String characteristicUri;
	private String characteristicLabel;
	private String unitUri;
	private String unitLabel;
	private String entityUri;
	private String entityLabel;
	private int valueColumn;
	private int timestampColumn;
	private int elevationColumn;
	
	public int getValueColumn() {
		return valueColumn;
	}
	public void setValueColumn(int valueColumn) {
		this.valueColumn = valueColumn;
	}
	public int getTimestampColumn() {
		return timestampColumn;
	}
	public void setTimestampColumn(int timestampColumn) {
		this.timestampColumn = timestampColumn;
	}
	public int getElevationColumn() {
		return elevationColumn;
	}
	public void setElevationColumn(int elevationColumn) {
		this.elevationColumn = elevationColumn;
	}
	public String getEntityUri() {
		return entityUri;
	}
	public void setEntityUri(String entityUri) {
		this.entityUri = entityUri;
	}
	public String getEntityLabel() {
		return entityLabel;
	}
	public void setEntityLabel(String entityLabel) {
		this.entityLabel = entityLabel;
	}
	public String getCharacteristicUri() {
		return characteristicUri;
	}
	public void setCharacteristicUri(String characteristicUri) {
		this.characteristicUri = characteristicUri;
	}
	public String getUnitUri() {
		return unitUri;
	}
	public void setUnitUri(String unitUri) {
		this.unitUri = unitUri;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	
	public static List<MeasurementType> find(HADataC hadatac) {
		List<MeasurementType> measurementTypesKb = new ArrayList<MeasurementType>();
		MeasurementType measurementType;
		MeasurementType measurementTypeKb;
		boolean measurementTypeComplete;
		
		Iterator<MeasurementType> i = hadatac.dataset.measurementTypes.iterator();
		while (i.hasNext()) {
			measurementType = i.next();
			measurementTypeKb = new MeasurementType();
			measurementTypeComplete = true;
			
			String queryString = Sparql.prefix
					+ "SELECT ?c ?c_label ?e ?e_label WHERE {\n"
					+ "  <" + measurementType.getCharacteristicUri() + "> <http://hadatac.org/ont/hasneto#ofCharacteristic> ?c .\n"
					+ "  OPTIONAL { ?c rdfs:label ?c_label . }\n"
					+ "  <" + measurementType.getCharacteristicUri() + "> <http://hadatac.org/ont/hasneto#ofEntity> ?e .\n"
					+ "  OPTIONAL { ?e rdfs:label ?e_label . }\n"
					+ "}";
			
			System.out.println(queryString);
			
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			
			measurementTypeKb.setLocalName(measurementType.getLocalName());
			measurementTypeKb.setValueColumn(measurementType.getValueColumn());
			measurementTypeKb.setTimestampColumn(measurementType.getTimestampColumn());
			
			if (resultsrw.size() == 1) {
				QuerySolution soln = resultsrw.next();
				measurementTypeKb.setCharacteristicUri(soln.getResource("c").getURI());
				if (soln.getLiteral("c_label") != null) { measurementTypeKb.setCharacteristicLabel(soln.getLiteral("c_label").getString()); }
				else { measurementTypeKb.setCharacteristicLabel(soln.getResource("c").getLocalName()); }
				measurementTypeKb.setEntityUri(soln.getResource("e").getURI());
				if (soln.getLiteral("e_label") != null) { measurementTypeKb.setEntityLabel(soln.getLiteral("e_label").getString()); }
				else { measurementTypeKb.setEntityLabel(soln.getResource("e").getLocalName()); }
			} else {
				measurementTypeComplete = false;
			}
			
			queryString = Sparql.prefix
					+ "SELECT ?u_label WHERE {\n"
					+ "  <" + measurementType.getUnitUri() + "> a owl:Class .\n"
					+ "  OPTIONAL { <" + measurementType.getUnitUri() + "> rdfs:label ?u_label . }\n"
					+ "}";
			
			System.out.println(queryString);
			
			query = QueryFactory.create(queryString);
			qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
			results = qexec.execSelect();
			resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			
			if (resultsrw.size() == 1) {
				QuerySolution soln = resultsrw.next();
				measurementTypeKb.setUnitUri(measurementType.getUnitUri());
				if (soln.getLiteral("u_label") != null) {
					measurementTypeKb.setUnitLabel(soln.getLiteral("u_label").getString());
				}
				else {
					Resource unit = ResourceFactory.createResource(measurementType.getUnitUri());
					measurementTypeKb.setUnitLabel(unit.getLocalName());
				}
			} else {
				measurementTypeComplete = false;
			}
			
			if (measurementTypeComplete == true) {
				measurementTypesKb.add(measurementTypeKb);
				System.out.println("[OK] Measurement type <" + measurementType.getLocalName() + "> is defined in the knowledge base. Entity: \"" + measurementTypeKb.getEntityLabel() + "\"; Characteristic: \"" + measurementTypeKb.getCharacteristicLabel() + "\"; Unit: \"" + measurementTypeKb.getUnitLabel() + "\"");
			} else {
				System.out.println("[WARNING] Measurement type <" + measurementType.getLocalName() + "> is not defined in the knowledge base."); 
			}
		}
		
		return measurementTypesKb;
	}
	
	public String getCharacteristicLabel() {
		return characteristicLabel;
	}
	public void setCharacteristicLabel(String characteristicLabel) {
		this.characteristicLabel = characteristicLabel;
	}
	public String getUnitLabel() {
		return unitLabel;
	}
	public void setUnitLabel(String unitLabel) {
		this.unitLabel = unitLabel;
	}
	public static List<MeasurementType> find(Model model, Dataset dataset) {
		List<MeasurementType> list = new ArrayList<MeasurementType>();
		
		String queryString = Sparql.prefix
				+ "SELECT ?mt ?column ?char ?unit ?tsColumn WHERE {\n"
				+ "  <" + dataset.getCcsvUri() + "> hadatac:hasMeasurementType ?mt .\n"
				+ "  ?mt a hadatac:MeasurementType .\n"
				+ "  ?mt hadatac:atColumn ?column .\n"
				+ "  ?mt oboe:ofCharacteristic ?char .\n"
				+ "  ?mt oboe:usesStandard ?unit .\n"
				+ "  OPTIONAL { ?mt time:inDateTime ?ts . }\n"
				+ "  OPTIONAL { ?ts hadatac:atColumn ?tsColumn . }\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		
		ResultSetFormatter.outputAsCSV(System.out, resultsrw);
		resultsrw.reset();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			MeasurementType measurementType = new MeasurementType();
			measurementType.setUri(soln.getResource("mt").getURI());
			measurementType.setLocalName(soln.getResource("mt").getLocalName());
			measurementType.setCharacteristicUri(soln.getResource("char").getURI());
			measurementType.setUnitUri(soln.getResource("unit").getURI());
			measurementType.setValueColumn(soln.getLiteral("column").getInt());
			System.out.println("!! loading from preamble: " + soln.getLiteral("column").getInt());
			if (soln.getLiteral("tsColumn") != null) { measurementType.setTimestampColumn(soln.getLiteral("tsColumn").getInt()); }
			list.add(measurementType);
		}
		
		return list;
	}
}
