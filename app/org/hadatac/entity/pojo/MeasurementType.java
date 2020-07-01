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
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
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
			if(measurementType.getCharacteristicUri().equals(URIUtils.replacePrefixEx("hasco:TimeStamp"))){
				measurementType.setTimestampColumn(soln.getLiteral("column").getInt());
				System.out.println("TimeStampColumn: " + soln.getLiteral("column").getInt());
			}
			if(measurementType.getCharacteristicUri().equals(URIUtils.replacePrefixEx("sio:SIO_000418"))){
				measurementType.setTimeInstantColumn(soln.getLiteral("column").getInt());
				System.out.println("TimeInstantColumn: " + soln.getLiteral("column").getInt());
			}
			if(measurementType.getCharacteristicUri().equals(URIUtils.replacePrefixEx("hasco:originalID"))){
				measurementType.setIdColumn(soln.getLiteral("column").getInt());
				System.out.println("IdColumn: " + measurementType.getIdColumn());
			}
			list.add(measurementType);
		}
		
		return list;
	}
}
