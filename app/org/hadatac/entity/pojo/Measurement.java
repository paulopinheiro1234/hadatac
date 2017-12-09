package org.hadatac.entity.pojo;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.hadatac.console.controllers.dataacquisitionsearch.FacetTree;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.utils.Collections;

import play.Play;
import scala.annotation.meta.field;

public class Measurement {
	@Field("uri")
	private String uri;
	@Field("owner_uri_str")
	private String ownerUri;
	@Field("acquisition_uri_str")
	private String acquisitionUri;
	@Field("study_uri_str")
	private String studyUri;
	@Field("object_uri_str")
	private String objectUri;
	@Field("timestamp_date")
	private Date timestamp;
	@Field("named_time_str")
	private String abstractTime;
	@Field("value_str")
	private String value;
	@Field("pid_str")
	private String pid;
	@Field("sid_str")
	private String sid;	
	@Field("unit_uri_str")
	private String unitUri;
	@Field("dasa_uri_str")
	private String schemaAttributeUri;
	@Field("entity_uri_str")
	private String entityUri;	
	@Field("characteristic_uri_str")
	private String characteristicUri;	
	@Field("location_latlong")
	private String location;
	@Field("elevation_double")
	private double elevation;
	@Field("dataset_uri_str")
	private String datasetUri;
	
	// Variables that are not stored in Solr
	private String entity;
	private String characteristic;
	private String unit;
	private String platformName;
	private String platformUri;
	private String instrumentModel;
	private String instrumentUri;
	private String strTimestamp;

	public String getOwnerUri() {
		return ownerUri;
	}

	public void setOwnerUri(String ownerUri) {
		this.ownerUri = ownerUri;
	}

	public String getAcquisitionUri() {
		return acquisitionUri;
	}

	public void setAcquisitionUri(String acquisitionUri) {
		this.acquisitionUri = acquisitionUri;
	}

	public String getStudyUri() {
		return studyUri;
	}

	public void setStudyUri(String studyUri) {
		this.studyUri = studyUri;
	}

	public String getObjectUri() {
		return objectUri;
	}

	public void setPID(String objectUri) {
		this.pid = objectUri;
	}

	public void setSID(String objectUri) {
		this.sid = objectUri;
	}

	public String getObjectPID() {
		return this.pid;
	}

	public String getObjectSID() {
		return this.sid;
	}

	public void setObjectUri(String objectUri) {
		this.objectUri = objectUri;
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

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		if (timestamp != null) {
			this.strTimestamp = timestamp.toString();
		}
	}
	
	public String getTimestampString() {
		return strTimestamp;
	}

	public void setTimestampString(String strTimestamp) {
		this.strTimestamp = strTimestamp;
	}
	
	public void setTimestamp(Instant timestamp) {
		this.timestamp = Date.from(timestamp);
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = Date.from(Instant.parse(timestamp));
	}

	public String getAbstractTime() {
		return abstractTime;
	}

	public void setAbstractTime(String abstractTime) {
		this.abstractTime = abstractTime;
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
	
	public String getSchemaAttributeUri() {
		return schemaAttributeUri;
	}
	
	public void setSchemaAttributeUri(String schemaAttributeUri) {
		this.schemaAttributeUri = schemaAttributeUri;
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

	public int save() {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		try {
			int status = solr.addBean(this).getStatus();
			solr.commit();
			solr.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] Measurement.save - e.Message: " + e.getMessage());
			return -1;
		}
	}

	public static int delete(String datasetUri) {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		try {
			UpdateResponse response = solr.deleteByQuery("dataset_uri_str:\"" + datasetUri + "\"");
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

	public static String buildQuery(String user_uri, String study_uri, String subject_uri, String char_uri) {
		String acquisition_query = "";
		String facet_query = "";
		String q = "";

		List<String> listURI = DataAcquisition.findAllAccessibleDataAcquisition(user_uri);
		Iterator<String> iter_uri = listURI.iterator();
		while (iter_uri.hasNext()) {
			String uri = iter_uri.next();
			acquisition_query += "acquisition_uri_str" + ":\"" + uri + "\"";
			if (iter_uri.hasNext()) {
				acquisition_query += " OR ";
			}
		}

		if (acquisition_query.equals("")) {
			return "";
		}

		if (!study_uri.equals("")) {
			facet_query += "study_uri_str" + ":\"" + study_uri + "\"";
		}
		if (!subject_uri.equals("")) {
			if (!study_uri.equals("")) {
				facet_query += " AND ";
			}
			facet_query += "object_uri_str" + ":\"" + subject_uri + "\"";
		}

		if (!char_uri.equals("")) {
			if (!study_uri.equals("") || !subject_uri.equals("")) {
				facet_query += " AND ";
			}
			facet_query += "characteristic_uri_str" + ":\"" + char_uri + "\"";
		}

		if (facet_query.trim().equals("")) {
			q = acquisition_query;
		} else {
			q = "(" + acquisition_query + ") AND (" + facet_query + ")";
		}

		return q;
	}

	public static String buildQuery(String user_uri, int page, int qtd, FacetHandler handler) {
		String acquisition_query = "";
		String facet_query = "";
		String q = "";

		List<String> listURI = DataAcquisition.findAllAccessibleDataAcquisition(user_uri);
		Iterator<String> iter_uri = listURI.iterator();
		while (iter_uri.hasNext()) {
			acquisition_query += "acquisition_uri_str:\"" + iter_uri.next() + "\"";
			if (iter_uri.hasNext()) {
				acquisition_query += " OR ";
			}
		}

		if (acquisition_query.equals("")) {
			return "";
		}

		if (handler != null) {
			facet_query = handler.toSolrQuery();
		}

		if (facet_query.trim().equals("") || facet_query.trim().equals("*:*")) {
			q = acquisition_query;
		} else {
			q = "(" + acquisition_query + ") AND (" + facet_query + ")";
		}

		return q;
	}

	public static AcquisitionQueryResult findForViews(String user_uri, String study_uri, 
			String subject_uri, String char_uri, boolean bNumberOfResultsOnly) {
		AcquisitionQueryResult result = new AcquisitionQueryResult();

		String q = buildQuery(user_uri, study_uri, subject_uri, char_uri);
		/*
		 * an empty query happens when current user is not allowed to see any
		 * data acquisition
		 */
		if (q.equals("")) {
			return result;
		}

		SolrQuery query = new SolrQuery();
		query.setQuery(q);
		if (bNumberOfResultsOnly) {
			query.setRows(0);
		}
		else {
			query.setRows(10000000);
		}
		query.setFacet(false);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			if (bNumberOfResultsOnly) {
				result.setDocumentSize(results.getNumFound());
			} else {
				Iterator<SolrDocument> m = results.iterator();
				while (m.hasNext()) {
					result.documents.add(convertFromSolr(m.next()));
				}
			}
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.findForViews() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.findForViews() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.findForViews() - Exception message: " + e.getMessage());
		}

		return result;
	}

	public static AcquisitionQueryResult find(String user_uri, int page, int qtd, FacetHandler handler) {
		AcquisitionQueryResult result = new AcquisitionQueryResult();

		String q = buildQuery(user_uri, page, qtd, handler);
		/*
		 * an empty query happens when current user is not allowed to see any
		 * data acquisition
		 */
		if (q.equals("")) {
			System.out.println("q is empty");
			return result;
		}
		
		int docSize = 0;
		SolrQuery query = new SolrQuery();
		query.setQuery(q);
		if (page != -1) {
			query.setStart((page - 1) * qtd + 1);
			query.setRows(qtd);
		} else {
			query.setRows(99999999);
		}
		query.setFacet(true);
		query.setFacetLimit(-1);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			System.out.println("!!!! QUERY: " + query.toQueryString());
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			System.out.println("Num of results: " + results.getNumFound());
			Iterator<SolrDocument> m = results.iterator();
			while (m.hasNext()) {
				result.addDocument(convertFromSolr(m.next()));
			}
			
			FacetTree fTree = new FacetTree();
			fTree.setTargetFacet(DataAcquisition.class);
			fTree.addUpperFacet(Study.class);
			Pivot pivot = getFacetStats(fTree, handler, false);
			for (Pivot p : pivot.children) {
				docSize += p.count;
			}
			result.extra_facets.put(FacetHandler.STUDY_FACET, pivot);
			
			fTree = new FacetTree();
			fTree.setTargetFacet(AttributeInstance.class);
			fTree.addUpperFacet(Indicator.class);
			fTree.addUpperFacet(EntityRole.class);
			fTree.addUpperFacet(EntityInstance.class);
			pivot = getFacetStats(fTree, handler, true);
			fTree.mergeFacetTree(1, 0, new ArrayList<Integer>(), null, pivot, "", new ArrayList<Pivot>());
			result.extra_facets.put(FacetHandler.ENTITY_CHARACTERISTIC_FACET, pivot);
			
			fTree = new FacetTree();
			fTree.setTargetFacet(UnitInstance.class);
			result.extra_facets.put(FacetHandler.UNIT_FACET, getFacetStats(fTree, handler, false));
			
			fTree = new FacetTree();
			fTree.setTargetFacet(TimeInstance.class);
			result.extra_facets.put(FacetHandler.TIME_FACET, getFacetStats(fTree, handler, false));
			
			fTree = new FacetTree();
			fTree.setTargetFacet(DataAcquisition.class);
			fTree.addUpperFacet(Platform.class);
			fTree.addUpperFacet(Instrument.class);
			result.extra_facets.put(FacetHandler.PLATFORM_INSTRUMENT_FACET, getFacetStats(fTree, handler, false));

		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.find() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.find() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.find() - Exception message: " + e.getMessage());
			e.printStackTrace();
		}

		result.setDocumentSize((long) docSize);

		return result;
	}
	
	private static Pivot getFacetStats(FacetTree fTree, FacetHandler facetHandler, 
			boolean bStatsFromSecondLastLevel) {
		Pivot pivot = new Pivot();
		fTree.retrieveFacetData(0, facetHandler, pivot, new ArrayList<String>(), bStatsFromSecondLastLevel);
		pivot.setNullParent();
		
		return pivot;
	}

	@SuppressWarnings("unchecked")
	public static Pivot parseFacetResults(QueryResponse response) {
		if (response.getResponse() != null) {
			if (response.getResponse().get("facets") instanceof NamedList) {
				return parsePivot(((NamedList<Object>)response.getResponse().get("facets")), "");
			}
		}

		return null;
	}

	private static Pivot parsePivot(NamedList<Object> objects, String field) {
		Pivot pivot = new Pivot();
		objects.forEach(new BiConsumer<String, Object>() {

			@SuppressWarnings("unchecked")
			@Override
			public void accept(String t, Object u) {
				if (t.equals("val")) {
					if (u instanceof String) {
						pivot.value = (String)u;
					} else {
						pivot.value = u.toString();
					}
				} else if (t.equals("count")) {
					pivot.field = field;
					pivot.count = (int)u;
				} else {
					if (u instanceof ArrayList<?>) {
						for (NamedList<Object> nl : (ArrayList<NamedList<Object>>) u) {
							Pivot child = parsePivot((NamedList<Object>)nl, field);
							pivot.addChild(child);
						}
					} else if (u instanceof NamedList<?>) {
						for (NamedList<Object> nl : ((ArrayList<NamedList<Object>>)((NamedList<Object>)u).get("buckets"))) {
							Pivot child = parsePivot((NamedList<Object>)nl, t);
							pivot.addChild(child);
						}
					}
				}
			}
		});

		return pivot;
	}

	public static long getNumByDataAcquisition(DataAcquisition dataAcquisition) {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		SolrQuery query = new SolrQuery();
		query.set("q", "acquisition_uri_str:\"" + dataAcquisition.getUri() + "\"");
		query.set("rows", "10000000");

		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			// Update the data set URI list
			dataAcquisition.deleteAllDatasetURIs();
			Iterator<SolrDocument> iter = results.iterator();
			while (iter.hasNext()) {
				SolrDocument doc = iter.next();
				dataAcquisition.addDatasetUri(SolrUtils.getFieldValue(doc, "dataset_uri_str"));
			}
			return results.getNumFound();
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.findByDataAcquisitionUri(acquisition_uri) - Exception message: "
					+ e.getMessage());
		}

		return 0;
	}

	public static List<Measurement> findByDataAcquisitionUri(String acquisition_uri) {
		List<Measurement> listMeasurement = new ArrayList<Measurement>();

		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		SolrQuery query = new SolrQuery();
		query.set("q", "acquisition_uri_str:\"" + acquisition_uri + "\"");
		query.set("rows", "10000000");

		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			Iterator<SolrDocument> i = results.iterator();
			while (i.hasNext()) {
				Measurement measurement = convertFromSolr(i.next());
				listMeasurement.add(measurement);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.findByDataAcquisitionUri(acquisition_uri) - Exception message: "
					+ e.getMessage());
		}

		return listMeasurement;
	}

	public static Measurement convertFromSolr(SolrDocument doc) {
		Measurement m = new Measurement();
		m.setUri(SolrUtils.getFieldValue(doc, "uri"));
		m.setOwnerUri(SolrUtils.getFieldValue(doc, "owner_uri_str"));
		m.setDatasetUri(SolrUtils.getFieldValue(doc, "dataset_uri_str"));
		m.setAcquisitionUri(SolrUtils.getFieldValue(doc, "acquisition_uri_str"));
		m.setStudyUri(SolrUtils.getFieldValue(doc, "study_uri_str"));
		m.setSchemaAttributeUri(SolrUtils.getFieldValue(doc, "dasa_uri_str"));
		m.setObjectUri(SolrUtils.getFieldValue(doc, "object_uri_str"));
		m.setPID(SolrUtils.getFieldValue(doc, "pid_str"));
		m.setSID(SolrUtils.getFieldValue(doc, "sid_str"));
		m.setAbstractTime(SolrUtils.getFieldValue(doc, "named_time_str"));
		m.setValue(SolrUtils.getFieldValue(doc, "value_str"));
		
		m.setEntityUri(SolrUtils.getFieldValue(doc, "entity_uri_str"));
		m.setEntity(HADatAcClass.getLabelByUri(m.getEntityUri(), Entity.class));
		
		m.setCharacteristicUri(SolrUtils.getFieldValue(doc, "characteristic_uri_str"));
		m.setCharacteristic(HADatAcClass.getLabelByUri(m.getCharacteristicUri(), Attribute.class));
		
		m.setUnitUri(SolrUtils.getFieldValue(doc, "unit_uri_str"));
		m.setUnit(HADatAcClass.getLabelByUri(m.getUnitUri(), Unit.class));
		
		DataAcquisition da = DataAcquisition.findByUri(m.getAcquisitionUri());
		if (da != null) {
			m.setPlatformUri(da.getPlatformUri());
			m.setPlatformName(da.getPlatformName());
			m.setInstrumentUri(da.getInstrumentUri());
			m.setInstrumentModel(da.getInstrumentModel());
			m.setLocation(da.getLocation());
			//m.setElevation(Double.parseDouble(da.getElevation()));
		}

		if (doc.getFieldValue("timestamp_date") != null) {
			if (((Date)doc.getFieldValue("timestamp_date")).equals(new Date(0))) {
				m.setTimestamp((Date)null);
			} else {
				m.setTimestamp((Date)doc.getFieldValue("timestamp_date"));
			}
		}
		
		return m;
	}
	
	public static List<String> getFieldNames() {
		List<String> results = new ArrayList<String>();
		java.lang.reflect.Field[] fields = Measurement.class.getDeclaredFields();
		for (java.lang.reflect.Field field : fields) {
			// Not include dates
			if (field.getType() != Date.class) {
				results.add(field.getName());
			}
		}
		
		return results;
	}
	
	public static String outputAsCSV(List<Measurement> measurements, List<String> fieldNames) {
		String result = "";		
		// Create headers
		result += String.join(",", fieldNames) + "\n";
		
		// Create rows
		for (Measurement m : measurements) {
			result += m.toCSVRow(fieldNames) + "\n";
		}
		
		return result;
	}
	
	public String toCSVRow(List<String> fieldNames) {
		List<String> values = new ArrayList<String>();
		for (String name : fieldNames) {
			Object obj = null;
			try {
				obj = Measurement.class.getDeclaredField(name).get(this);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			if (obj != null) {
				values.add(obj.toString());
			} else {
				values.add("");
			}
		}
		
		return String.join(",", values);
	}
}
