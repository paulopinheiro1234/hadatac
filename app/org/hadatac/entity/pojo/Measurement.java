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
import org.apache.solr.client.solrj.SolrRequest;
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
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.utils.Collections;

import play.Play;

public class Measurement {
	@Field("uri")
	private String uri;
	@Field("owner_uri")
	private String ownerUri;
	@Field("acquisition_uri")
	private String acquisitionUri;
	@Field("study_uri")
	private String studyUri;
	@Field("object_uri")
	private String objectUri;
	@Field("timestamp")
	private String timestamp;
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
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public void setTimestampXsd(String timestamp) {
		this.timestamp = timestamp;
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
	
	public int save() {
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION);
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
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION);
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
	
	public static String buildQuery(String user_uri, String study_uri, 
									String subject_uri, String char_uri) {
	    String acquisition_query = "";
        String facet_query = "";
        String q = "";
        
        List<String> listURI = DataAcquisition.findAllAccessibleDataAcquisition(user_uri);
        Iterator<String> iter_uri = listURI.iterator();
        while(iter_uri.hasNext()){
            String uri = iter_uri.next();
            acquisition_query += "acquisition_uri" + ":\"" + uri + "\"";
            if(iter_uri.hasNext()){
                acquisition_query += " OR ";
            }
        }
        
        if(!study_uri.equals("")){
            facet_query += "study_uri" + ":\"" + study_uri + "\"";
        }
        if(!subject_uri.equals("")){
            if(!study_uri.equals("")){
                facet_query += " AND ";
            }
            facet_query += "object_uri" + ":\"" + subject_uri + "\"";
        }
        if(!char_uri.equals("")){
            if(!study_uri.equals("") || !subject_uri.equals("")){
                facet_query += " AND ";
            }
            facet_query += "characteristic_uri" + ":\"" + char_uri + "\"";
        }
        
        if (facet_query.trim().equals("")) {
            q = acquisition_query;
        }
        else {
            q = "(" + acquisition_query + ") AND (" + facet_query + ")";
        }
	    
	    return q;
	}
	
	public static AcquisitionQueryResult findForViews(String user_uri, String study_uri, 
													  String subject_uri, String char_uri) {
		AcquisitionQueryResult result = new AcquisitionQueryResult();
		
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION);
		SolrQuery query = new SolrQuery();
		
		String q = buildQuery(user_uri, study_uri, subject_uri, char_uri);
		query.setQuery(q);
		query.setRows(10000000);
		query.setFacet(false);
		
		try {
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			System.out.println("SolrDocumentList: " + results.size());
			Iterator<SolrDocument> m = results.iterator();
			while (m.hasNext()) {
				result.documents.add(convertFromSolr(m.next()));
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
	
	public static String buildQuery(String user_uri, int page, int qtd, FacetHandler handler) {
	    String acquisition_query = "";
        String facet_query = "";
        String q = "";
        
        List<String> listURI = DataAcquisition.findAllAccessibleDataAcquisition(user_uri);
        Iterator<String> iter_uri = listURI.iterator();
        while(iter_uri.hasNext()){
            String uri = iter_uri.next();
            acquisition_query += "acquisition_uri" + ":\"" + uri + "\"";
            if(iter_uri.hasNext()){
                acquisition_query += " OR ";
            }
        }
        
        if (handler != null) {
        	facet_query = handler.toSolrQuery();
        }
        
        if (acquisition_query.equals("")) {
        	if (facet_query.trim().equals("")) {
        		q = "*:*";
            }
        	else {
        		q = facet_query;
        	}
        }
        else {
            if (facet_query.trim().equals("") || facet_query.trim().equals("*:*")) {
                q = acquisition_query;
            }
            else {
            	q = "(" + acquisition_query + ") AND (" + facet_query + ")";
            }
        }
        
        return q;
	}
	
	public static AcquisitionQueryResult find(String user_uri, int page, int qtd, FacetHandler handler) {
		AcquisitionQueryResult result = new AcquisitionQueryResult();
		int docSize = 0;
		
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.DATA_ACQUISITION);
		SolrQuery query = new SolrQuery();
		
		String q = buildQuery(user_uri, page, qtd, handler);
		System.out.println("q: " + q);
		query.setQuery(q);
		query.setStart((page - 1) * qtd + 1);
		System.out.println("Starting at: " + ((page - 1)* qtd + 1) + "    page: " + page + "     qtd: " + qtd);
		query.setRows(qtd);
		query.setFacet(true);
		query.setFacetLimit(-1);
		query.addFacetField("unit");
		query.addFacetPivotField("study_uri,acquisition_uri");
		query.addFacetPivotField("entity,characteristic");
		query.addFacetPivotField("platform_name,instrument_model");
		
		try {
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			System.out.println("SolrDocumentList: " + results.size());
			Iterator<SolrDocument> m = results.iterator();
			while (m.hasNext()) {
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
				Iterator<Entry<String, List<PivotField>>> iter = queryResponse.getFacetPivot().iterator();
				
				while (iter.hasNext()) {
					Entry<String, List<PivotField>> entry = iter.next();
					
					List<Pivot> parents = new ArrayList<Pivot>();
					result.pivot_facets.put(entry.getKey(), parents);
					System.out.println("PIVOT: " + entry.getKey());
					
					List<PivotField> listPivotField = entry.getValue();
					System.out.println("List<PivotField> size: " + listPivotField.size());
					Iterator<PivotField> iterParents = listPivotField.iterator();
					
					while (iterParents.hasNext()) {
						PivotField pivot = iterParents.next();
						
						Pivot parent = new Pivot();
						if (pivot.getField().equals("study_uri")) {
							docSize += pivot.getCount();
						}
						parent.field = pivot.getField();
						parent.value = pivot.getValue().toString();
						parent.count = pivot.getCount();
						parents.add(parent);
						System.out.println("PIVOT FIELD: " + pivot.getField());
						System.out.println("PIVOT VALUE: " + pivot.getValue().toString());
						System.out.println("PIVOT COUNT: " + pivot.getCount());
						
						List<PivotField> subPivotFiled = pivot.getPivot();
						if(null != subPivotFiled){
							Iterator<PivotField> iterChildren = subPivotFiled.iterator();
							while (iterChildren.hasNext()) {
								pivot = iterChildren.next();
								Pivot child = new Pivot();
								child.field = pivot.getField();
								child.value = pivot.getValue().toString();
								child.count = pivot.getCount();
								parent.children.add(child);
								System.out.println("PIVOT FIELD: " + pivot.getField());
								System.out.println("PIVOT VALUE: " + pivot.getValue().toString());
								System.out.println("PIVOT COUNT: " + pivot.getCount());
							}
						}
					}
				}
			}
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.find() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.find() - IOException message: " + e.getMessage());
		} catch (Exception e) {
		        e.printStackTrace();
			System.out.println("[ERROR] Measurement.find() - Exception message: " + e.getMessage());
		}
		
		result.setDocumentSize((long)docSize);
		
		return result;
	}
	
	public static long getNumByDataAcquisition(DataAcquisition dataAcquisition) {		
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION);
		SolrQuery query = new SolrQuery();
		query.set("q", "acquisition_uri:\"" + dataAcquisition.getUri() + "\"");
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
				if (doc.getFieldValue("dataset_uri") != null) {
					dataAcquisition.addDatasetUri(doc.getFieldValue("dataset_uri").toString());
				}
			}
			return results.getNumFound();
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.findByDataAcquisitionUri(acquisition_uri) - Exception message: " + e.getMessage());
		}
				
		return 0;
	}
	
	public static List<Measurement> findByDataAcquisitionUri(String acquisition_uri) {
		List<Measurement> listMeasurement = new ArrayList<Measurement>();
		
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION);
		SolrQuery query = new SolrQuery();
		query.set("q", "acquisition_uri:\"" + acquisition_uri + "\"");
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
			System.out.println("[ERROR] Measurement.findByDataAcquisitionUri(acquisition_uri) - Exception message: " + e.getMessage());
		}
				
		return listMeasurement;
	}
	
	public static Measurement convertFromSolr(SolrDocument doc) {		
		Measurement m = new Measurement();
		m.setUri(doc.getFieldValue("uri").toString());
		m.setOwnerUri(doc.getFieldValue("owner_uri").toString());
		m.setAcquisitionUri(doc.getFieldValue("acquisition_uri").toString());
		m.setStudyUri(doc.getFieldValue("study_uri").toString());
		if (doc.getFieldValue("object_uri") != null) {
  		    m.setObjectUri(doc.getFieldValue("object_uri").toString());
		}
		if (doc.getFieldValue("timestamp") != null) {
			m.setTimestamp(doc.getFieldValue("timestamp").toString());
		}
		m.setValue(doc.getFieldValue("value").toString());
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
		if (doc.getFieldValue("location") !=null) {
			m.setLocation(doc.getFieldValue("location").toString());
		}
		if (doc.getFieldValue("elevation") !=null) {
			m.setElevation(Double.parseDouble(doc.getFieldValue("elevation").toString()));
		}
		m.setDatasetUri(doc.getFieldValue("dataset_uri").toString());
		
		return m;
	}
}