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

}
