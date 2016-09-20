package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import play.Play;

public class Measurement {
	@Field("uri")
	private String uri;
	@Field("owner_uri")
	private String ownerUri;
	@Field("permission_uri")
	private String permissionUri;
	private DateTime timestamp;
	@Field("value")
	private String value;
	@Field("unit")
	private String unit;
	@Field("unit_uri")
	private String unitUri;
	@Field("entity")
	private String entity;
	@Field("entity_uri")
	private String entityUri;
	@Field("characteristic")
	private String characteristic;
	@Field("characteristic_uri")
	private String characteristicUri;
	@Field("instrument_model")
	private String instrumentModel;
	@Field("instrument_uri")
	private String instrumentUri;
	@Field("platform_name")
	private String platformName;
	@Field("platform_uri")
	private String platformUri;
	@Field("location")
	private String location;
	@Field("elevation")
	private double elevation;
	@Field("dataset_uri")
	private String datasetUri;
	
	public String getOwnerUri() {
		return ownerUri;
	}
	public void setOwnerUri(String ownerUri) {
		this.ownerUri = ownerUri;
	}
	public String getPermissionUri() {
		return permissionUri;
	}
	public void setPermissionUri(String permissionUri) {
		this.permissionUri = permissionUri;
	}
	public String getInstrumentModel() {
		return instrumentModel;
	}
	public void setInstrumentModel(String instrumentModel) {
		this.instrumentModel = instrumentModel;
	}
	public String getInstrumentUri() {
		return instrumentUri;
	}
	public void setInstrumentUri(String instrumentUri) {
		this.instrumentUri = instrumentUri;
	}
	public String getPlatformName() {
		return platformName;
	}
	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}
	public String getPlatformUri() {
		return platformUri;
	}
	public void setPlatformUri(String platformUri) {
		this.platformUri = platformUri;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getTimestamp() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(timestamp);
	}
	@Field("timestamp")
	public void setTimestamp(String timestamp) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.timestamp = formatter.parseDateTime(timestamp);
	}
	public void setTimestampXsd(String timestamp) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.timestamp = formatter.parseDateTime(timestamp);
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getUnitUri() {
		return unitUri;
	}
	public void setUnitUri(String unitUri) {
		this.unitUri = unitUri;
	}
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public String getEntityUri() {
		return entityUri;
	}
	public void setEntityUri(String entityUri) {
		this.entityUri = entityUri;
	}
	public String getCharacteristic() {
		return characteristic;
	}
	public void setCharacteristic(String characteristic) {
		this.characteristic = characteristic;
	}
	public String getCharacteristicUri() {
		return characteristicUri;
	}
	public void setCharacteristicUri(String characteristicUri) {
		this.characteristicUri = characteristicUri;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public double getElevation() {
		return elevation;
	}
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}
	public String getDatasetUri() {
		return datasetUri;
	}
	public void setDatasetUri(String datasetUri) {
		this.datasetUri = datasetUri;
	}
	
	public int save(SolrClient client) {
		try {
			int status = client.addBean(this).getStatus();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] Measurement.save - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public static int delete(String datasetUri) {
		SolrClient solr = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/measurement");
		try {
			UpdateResponse response = solr.deleteByQuery("dataset_uri:\"" + datasetUri + "\"");
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.delete - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.delete - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.delete - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public static AcquisitionQueryResult find(int page, int qtd, List<String> permissions, FacetHandler handler) {
		AcquisitionQueryResult result = new AcquisitionQueryResult();
		
		SolrClient solr = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/measurement");
		SolrQuery query = new SolrQuery();
		String permission_query = "";
		String facet_query = "";
		String q;
		
		permission_query += "permission_uri:\"" + "PUBLIC" + "\"";
		if (permissions != null) {
			Iterator<String> i = permissions.iterator();
			while (i.hasNext()) {
				permission_query += " OR ";
				permission_query += "permission_uri:\"" + i.next() + "\"";
			}
		}
		System.out.println(permission_query);
		
		if (handler != null) {
			Iterator<String> i = handler.facetsAnd.keySet().iterator();
			while (i.hasNext()) {
				String field = i.next();
				String value = handler.facetsAnd.get(field);
				facet_query += field + ":\"" + value + "\"";
				if (i.hasNext()) {
					facet_query += " AND ";
				}
			}
		}
		
		if (facet_query.trim().equals("")) {
			facet_query = "*:*";
		}
		
		//q =  "(" + permission_query + ") AND (" + facet_query + ")";
		q =  facet_query;
		System.out.println("!!! QUERY: " + q);
		query.setQuery(q);
		query.setStart((page-1)*qtd);
		query.setRows(qtd);
		query.setFacet(true);
		query.addFacetField("unit");
		query.addFacetPivotField("entity,characteristic");
		query.addFacetPivotField("platform_name,instrument_model");
		
		try {
			QueryResponse queryResponse = solr.query(query);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			System.out.println("SolrDocumentList: " + results.size());
			Iterator<SolrDocument> m = results.iterator();
			while (m.hasNext()) {
				System.out.println("Next");
				result.documents.add(convertFromSolr(m.next()));
			}
			
			if (queryResponse.getFacetFields() != null) {
				Iterator<FacetField> f = queryResponse.getFacetFields().iterator();
				while (f.hasNext()) {
					FacetField field = f.next();
					result.field_facets.put(field.getName(), new HashMap<String, Long>());
					Iterator<Count> v = field.getValues().iterator();
					while (v.hasNext()) {
						Count count = v.next();
						Map<String, Long> map = result.field_facets.get(field.getName());
						map.put(count.getName(), count.getCount());
					}
				}
			}
			
			if (queryResponse.getFacetPivot() != null) {
				Iterator<Entry<String, List<PivotField>>> i1 = queryResponse.getFacetPivot().iterator();
				
				while (i1.hasNext()) {
					Entry<String, List<PivotField>> entry = i1.next();
					
					if (entry.getKey().equals("entity,characteristic")) {
						
					} else if (entry.getKey().equals("platform_name,instrument_model")) {
						
					}
					
					List<Pivot> parents = new ArrayList<Pivot>();
					result.pivot_facets.put(entry.getKey(), parents);
					
					List<PivotField> list = entry.getValue();
					System.out.println("List<PivotField> size: " + list.size());
					Iterator<PivotField> i_parents = list.iterator();
					System.out.println("!!!!!!! PIVOT: " + entry.getKey());
					while (i_parents.hasNext()) {
						PivotField pivot = i_parents.next();
						Pivot parent = new Pivot();
						parent.field = pivot.getField();
						parent.value = pivot.getValue().toString();
						parent.count = pivot.getCount();
						parents.add(parent);
						System.out.println("!!! PIVOT FIELD: " + pivot.getField());
						System.out.println("!!! PIVOT VALUE: " + pivot.getValue().toString());
						System.out.println("!!! PIVOT COUNT: " + pivot.getCount());
						Iterator <PivotField> i_children = pivot.getPivot().iterator();
						while (i_children.hasNext()) {
							pivot = i_children.next();
							Pivot child = new Pivot();
							child.field = pivot.getField();
							child.value = pivot.getValue().toString();
							child.count = pivot.getCount();
							parent.children.add(child);
							System.out.println("!!! PIVOT FIELD: " + pivot.getField());
							System.out.println("!!! PIVOT VALUE: " + pivot.getValue().toString());
							System.out.println("!!! PIVOT COUNT: " + pivot.getCount());
						}
					}
				}
			}
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.find(int, int, List<String>, Map<String, String>) - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.find(int, int, List<String>, Map<String, String>) - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.find(int, int, List<String>, Map<String, String>) - Exception message: " + e.getMessage());
		}
		
		return result;
	}
	
	public static Measurement convertFromSolr(SolrDocument doc) {
		System.out.println("convertFromSolr is called");
		
		Measurement m = new Measurement();
		DateTime date;
		
		m.setUri(doc.getFieldValue("uri").toString());
		m.setOwnerUri(doc.getFieldValue("owner_uri").toString());
		m.setPermissionUri(doc.getFieldValue("permission_uri").toString());
        date = new DateTime((Date)doc.getFieldValue("timestamp"));
		if (doc.getFieldValue("timestamp") !=null) { 
			//m.setTimestamp(doc.getFieldValue("timestamp").toString());
		    m.setTimestamp(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
			System.out.println("timestamp != null");
		}
		//m.setValue(Double.parseDouble(doc.getFieldValue("value").toString()));
		m.setValue(doc.getFieldValue("value").toString());
		System.out.println(doc.getFieldValue("unit"));
		System.out.println(doc.getFieldValue("entity"));
		System.out.println(doc.getFieldValue("characteristic"));
		System.out.println(doc.getFieldValue("unit_uri"));
		System.out.println(doc.getFieldValue("entity_uri"));
		System.out.println(doc.getFieldValue("characteristic_uri"));
		m.setUnit(doc.getFieldValue("unit").toString());
		m.setUnitUri(doc.getFieldValue("unit_uri").toString());
		m.setEntity(doc.getFieldValue("entity").toString());
		m.setEntityUri(doc.getFieldValue("entity_uri").toString());
		m.setCharacteristic(doc.getFieldValue("characteristic").toString());
		m.setCharacteristicUri(doc.getFieldValue("characteristic_uri").toString());
		m.setInstrumentModel(doc.getFieldValue("instrument_model").toString());
		m.setInstrumentUri(doc.getFieldValue("instrument_uri").toString());
		m.setPlatformName(doc.getFieldValue("platform_name").toString());
		m.setPlatformUri(doc.getFieldValue("platform_uri").toString());
		if (doc.getFieldValue("location") !=null) { m.setLocation(doc.getFieldValue("location").toString()); }
		if (doc.getFieldValue("elevation") !=null) { m.setElevation(Double.parseDouble(doc.getFieldValue("elevation").toString())); }
		m.setDatasetUri(doc.getFieldValue("dataset_uri").toString());
		System.out.println("Finished convertFromSolr");
		
		return m;
	}
}