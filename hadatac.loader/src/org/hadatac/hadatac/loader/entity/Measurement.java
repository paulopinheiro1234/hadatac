package org.hadatac.hadatac.loader.entity;

import org.apache.solr.client.solrj.beans.Field;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class Measurement {
	private String uri;
	private DateTime timestamp;
	private double value;
	private String unit;
	private String unit_uri;
	private String entity;
	private String entity_uri;
	private String characteristic;
	private String characteristic_uri;
	private String latlong;
	private double elevation;
	private String dataset_uri;
	
	public Measurement() {
		super();
	}
	
	public String getUri() {
		return uri;
	}
	@Field("uri")
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public DateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}
	public void setTimestamp(String timestamp) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.timestamp = formatter.withZone(DateTimeZone.UTC).parseDateTime(timestamp);
	}
	
	public String getTimestampSolr() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(timestamp);
	}
	@Field("timestamp")
	public void setTimestampSolr(String timestamp) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.timestamp = formatter.parseDateTime(timestamp);
	}
	
	public double getValue() {
		return value;
	}
	@Field("value")
	public void setValue(double value) {
		this.value = value;
	}
	
	public String getUnit() {
		return unit;
	}
	@Field("unit")
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public String getUnit_uri() {
		return unit_uri;
	}
	@Field("unit_uri")
	public void setUnit_uri(String unit_uri) {
		this.unit_uri = unit_uri;
	}
	
	public String getEntity() {
		return entity;
	}
	@Field("entity")
	public void setEntity(String entity) {
		this.entity = entity;
	}
	
	public String getEntity_uri() {
		return entity_uri;
	}
	@Field("entity_uri")
	public void setEntity_uri(String entity_uri) {
		this.entity_uri = entity_uri;
	}
	
	public String getCharacteristic() {
		return characteristic;
	}
	@Field("characteristic")
	public void setCharacteristic(String characteristic) {
		this.characteristic = characteristic;
	}
	
	public String getCharacteristic_uri() {
		return characteristic_uri;
	}
	@Field("characteristic_uri")
	public void setCharacteristic_uri(String characteristic_uri) {
		this.characteristic_uri = characteristic_uri;
	}
	
	public String getLatlong() {
		return latlong;
	}
	@Field("location")
	public void setLatlong(String latlong) {
		this.latlong = latlong;
	}
	
	public double getElevation() {
		return elevation;
	}
	@Field("elevation")
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
	
	public String getDataset_uri() {
		return dataset_uri;
	}
	@Field("dataset_uri")
	public void setDataset_uri(String dataset_uri) {
		this.dataset_uri = dataset_uri;
	}
}
