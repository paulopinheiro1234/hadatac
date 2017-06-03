package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.Iterator;
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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.NameSpaces;

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
	private int timeInstantColumn;
	private int idColumn;
	private int elevationColumn;
	
	public MeasurementType() {
		this.timestampColumn = -1;
		this.timeInstantColumn = -1;
		this.elevationColumn = -1;
		this.idColumn = -1;
	}
	
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
	public int getTimeInstantColumn() {
		return timeInstantColumn;
	}
	public void setTimeInstantColumn(int timeInstantColumn) {
		this.timeInstantColumn = timeInstantColumn;
	}
	public int getIdColumn() {
		return idColumn;
	}
	public void setIdColumn(int idColumn) {
		this.idColumn = idColumn;
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
		List<MeasurementType> results = new ArrayList<MeasurementType>();
		MeasurementType measurementTypeKb;
		boolean measurementTypeComplete;
		
		Iterator<MeasurementType> iter = hadatac.getDataset().getMeasurementTypes().iterator();
		while (iter.hasNext()) {
			MeasurementType measurementType = iter.next();
			measurementTypeKb = new MeasurementType();
			measurementTypeComplete = true;

			String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
					+ "SELECT ?dc ?c_label ?e_label WHERE {\n"
					+ " ?dc " + "hasco:hasAttribute" + " <" + measurementType.getCharacteristicUri() + "> .\n"
					+ "  OPTIONAL { " + "<" + measurementType.getCharacteristicUri() + "> " + "rdfs:label ?c_label . }\n"
					+ " ?dc " + "hasco:hasEntity" + " <" + measurementType.getEntityUri() + "> .\n"
					+ "  OPTIONAL { " + "<" + measurementType.getEntityUri() + "> " + "rdfs:label ?e_label . }\n"
					+ "}";
			
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					hadatac.getStaticMetadataSparqlURL(), query);
			ResultSet resultset = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(resultset);
			qexec.close();
			
			measurementTypeKb.setLocalName(measurementType.getLocalName());
			measurementTypeKb.setValueColumn(measurementType.getValueColumn());
			measurementTypeKb.setTimestampColumn(measurementType.getTimestampColumn());
			measurementTypeKb.setTimeInstantColumn(measurementType.getTimeInstantColumn());
			measurementTypeKb.setIdColumn(measurementType.getIdColumn());
			
			if (resultsrw.size() >= 1) {
				QuerySolution soln = resultsrw.next();
				measurementTypeKb.setCharacteristicUri(measurementType.getCharacteristicUri());
				if (soln.getLiteral("c_label") != null) {
					measurementTypeKb.setCharacteristicLabel(soln.getLiteral("c_label").getString()); 
				}
				else {
				    measurementTypeKb.setCharacteristicLabel(measurementType.getCharacteristicUri().replace("http://hadatac.org/ont/chear#","").replace("http://semanticscience.org/resource/",""));
					//measurementTypeKb.setCharacteristicLabel("non-label characteristic");
				}
				measurementTypeKb.setEntityUri(measurementType.getEntityUri());
				if (soln.getLiteral("e_label") != null) {
					measurementTypeKb.setEntityLabel(soln.getLiteral("e_label").getString()); 
				}
				else {
					measurementTypeKb.setEntityLabel("Subject");
				}
			} else {
				measurementTypeComplete = false;
			}
			
			queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
					+ "SELECT ?u_label WHERE {\n"
					+ "  <" + measurementType.getUnitUri() + "> rdfs:label ?u_label . \n"
					+ "}";
			
			query = QueryFactory.create(queryString);
			qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
			resultset = qexec.execSelect();
			resultsrw = ResultSetFactory.copyResults(resultset);
			qexec.close();
			
			if (resultsrw.size() >= 1) {
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
				results.add(measurementTypeKb);
				System.out.println("[OK] Measurement type <" + measurementType.getLocalName() + "> is defined in the knowledge base. Entity: \"" + measurementTypeKb.getEntityLabel() + "\"; Characteristic: \"" + measurementTypeKb.getCharacteristicLabel() + "\"; Unit: \"" + measurementTypeKb.getUnitLabel() + "\"");
			} else {
				System.out.println("[WARNING] Measurement type <" + measurementType.getLocalName() + "> is not defined in the knowledge base."); 
			}
		}
		
		return results;
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
		
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT ?mt ?column ?ent ?char ?unit WHERE {\n"
				+ "  <" + dataset.getCcsvUri() + "> hadatac:hasMeasurementType ?mt .\n"
				+ "  ?mt a hadatac:MeasurementType .\n"
				+ "  ?mt hadatac:atColumn ?column .\n"
				+ "  ?mt hasco:hasEntity ?ent .\n"
				+ "  ?mt hasco:hasAttribute ?char .\n"
				+ "  ?mt hasco:hasUnit ?unit .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		
		resultsrw.reset();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			MeasurementType measurementType = new MeasurementType();
			measurementType.setUri(soln.getResource("mt").getURI());
			measurementType.setLocalName(soln.getResource("mt").getLocalName());
			measurementType.setEntityUri(soln.getResource("ent").getURI());
			measurementType.setCharacteristicUri(soln.getResource("char").getURI());
			measurementType.setUnitUri(soln.getResource("unit").getURI());
			measurementType.setValueColumn(soln.getLiteral("column").getInt());
			if(measurementType.getCharacteristicUri().equals(ValueCellProcessing.replacePrefixEx("sio:TimeStamp"))){
				measurementType.setTimestampColumn(soln.getLiteral("column").getInt());
				System.out.println("TimeStampColumn: " + soln.getLiteral("column").getInt());
			}
			if(measurementType.getCharacteristicUri().equals(ValueCellProcessing.replacePrefixEx("sio:TimeInstant"))){
				measurementType.setTimeInstantColumn(soln.getLiteral("column").getInt());
				System.out.println("TimeInstantColumn: " + soln.getLiteral("column").getInt());
			}
			if(measurementType.getCharacteristicUri().equals(ValueCellProcessing.replacePrefixEx("hasco:originalID"))){
				measurementType.setIdColumn(soln.getLiteral("column").getInt());
				System.out.println("IdColumn: " + measurementType.getIdColumn());
			}
			list.add(measurementType);
		}
		
		return list;
	}
}
