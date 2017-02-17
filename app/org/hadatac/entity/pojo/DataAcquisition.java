package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import play.Play;

public class DataAcquisition {
	@Field("uri")
	private String uri;
	@Field("label")
	private String label;
	@Field("comment")
	private String comment;
	@Field("used_uri")
	private String used_uri;
	@Field("was_associated_with_uri")
	private String was_associated_with_uri;
	
	private DateTime startedAt;
	private DateTime endedAt;
	
	@Field("owner_uri")
	private String ownerUri;
	@Field("permission_uri")
	private String permissionUri;
	@Field("triggering_event")
	private int triggeringEvent;
	@Field("nr_data_points")
	private long numberDataPoints;
	@Field("unit")
	private List<String> unit;
	@Field("unit_uri")
	private List<String> unitUri;
	@Field("entity")
	private List<String> entity;
	@Field("entity_uri")
	private List<String> entityUri;
	@Field("type")
	private List<String> type;
	@Field("type_uri")
	private List<String> typeUri;
	@Field("characteristic")
	private List<String> characteristic;
	@Field("characteristic_uri")
	private List<String> characteristicUri;
	@Field("study_uri")
	private String studyUri;
	@Field("schema_uri")
	private String schemaUri;
	@Field("deployment_uri")
	private String deploymentUri;
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
	private String elevation;
	@Field("dataset_uri")
	private List<String> datasetUri;
	
	private String ccsvUri;
	private String localName;
	private int status;
	/*
	 * 0 - DataAcquisition is a new one, its details on the preamble
	 * 		It should not exist inside the KB
	 * 		Preamble must contain deployment link and deployment must exists on the KB
	 * 1 - DataAcquisition already exists, only a reference present on the preamble
	 * 		It should exist inside the KB as not finished yet 
	 * 2 - DataAcquisition already exists, the preamble states its termination with endedAtTime information
	 * 		It should exist inside the KB as not finished yet
	 */
	
	public DataAcquisition() {
		startedAt = null;
		endedAt = null;
		numberDataPoints = 0;
		datasetUri = new ArrayList<String>();
		unit = new ArrayList<String>();
		unitUri = new ArrayList<String>();
		characteristic = new ArrayList<String>();
		characteristicUri = new ArrayList<String>();
		entity = new ArrayList<String>();
		entityUri = new ArrayList<String>();
		type = new ArrayList<String>();
		typeUri = new ArrayList<String>();
	}

	public String getElevation() {
		return elevation;
	}

	public void setElevation(String elevation) {
		this.elevation = elevation;
	}
	
	public String getCcsvUri() {
		return ccsvUri;
	}

	public void setCcsvUri(String ccsvUri) {
		this.ccsvUri = ccsvUri;
	}
	
	public long getNumberDataPoints() {
		return numberDataPoints;
	}

	public void setNumberDataPoints(long numberDataPoints) {
		this.numberDataPoints = numberDataPoints;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getUsedUri() {
		return used_uri;
	}
	public void setUsedUri(String used_uri) {
		this.used_uri = used_uri;
	}
	
	public String getAssociatedUri() {
		return was_associated_with_uri;
	}
	public void setAssociatedUri(String uri) {
		this.was_associated_with_uri = uri;
	}
	
	public String getStudyUri() {
		return studyUri;
	}
	public void setStudyUri(String study_uri) {
		this.studyUri = study_uri;
	}
	
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
	
	public int getTriggeringEvent() {
		return triggeringEvent;
	}
	public void setTriggeringEvent(int triggeringEvent) {
		this.triggeringEvent = triggeringEvent;
	}
	
	public String getTriggeringEventName() {
		switch (triggeringEvent) {
			case TriggeringEvent.INITIAL_DEPLOYMENT:
				return TriggeringEvent.INITIAL_DEPLOYMENT_NAME;
			case TriggeringEvent.LEGACY_DEPLOYMENT:
				return TriggeringEvent.LEGACY_DEPLOYMENT_NAME;
			case TriggeringEvent.CHANGED_CONFIGURATION:
				return TriggeringEvent.CHANGED_CONFIGURATION_NAME;
			case TriggeringEvent.CHANGED_OWNERSHIP:
				return TriggeringEvent.CHANGED_OWNERSHIP_NAME;
			case TriggeringEvent.AUTO_CALIBRATION:
				return TriggeringEvent.AUTO_CALIBRATION_NAME;
			case TriggeringEvent.SUSPEND_DATA_ACQUISITION:
				return TriggeringEvent.SUSPEND_DATA_ACQUISITION_NAME;
			case TriggeringEvent.RESUME_DATA_ACQUISITION:
				return TriggeringEvent.RESUME_DATA_ACQUISITION_NAME;
		}
		return "";
	}
	
	public String getStartedAt() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(startedAt);
	}
	public String getStartedAtXsd() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		return formatter.withZone(DateTimeZone.UTC).print(startedAt);
	}
	@Field("started_at")
	public void setStartedAt(String startedAt) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	public void setStartedAtXsd(String startedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	public void setStartedAtXsdWithMillis(String startedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	public String getEndedAt() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(endedAt);
	}
	public String getEndedAtXsd() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		return formatter.withZone(DateTimeZone.UTC).print(endedAt);
	}
	@Field("ended_at")
	public void setEndedAt(String endedAt) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	public void setEndedAtXsd(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	public void setEndedAtXsdWithMillis(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	public List<String> getUnit() {
		return unit;
	}
	public void setUnit(List<String> unit) {
		this.unit = unit;
	}
	public void addUnit(String unit) {
		if (this.unit.contains(unit) == false) {
			this.unit.add(unit);
		}
	}
	public List<String> getUnitUri() {
		return unitUri;
	}
	public void setUnitUri(List<String> unitUri) {
		this.unitUri = unitUri;
	}
	public void addUnitUri(String unitUri) {
		if (this.unitUri.contains(unitUri) == false) {
			this.unitUri.add(unitUri);
		}
	}
	public List<String> getEntity() {
		return entity;
	}
	public void setEntity(List<String> entity) {
		this.entity = entity;
	}
	public void addEntity(String entity) {
		if (this.entity.contains(entity) == false) {
			this.entity.add(entity);
		}
	}
	public List<String> getEntityUri() {
		return entityUri;
	}
	public void setEntityUri(List<String> entityUri) {
		this.entityUri = entityUri;
	}
	public void addEntityUri(String entityUri) {
		if (this.entityUri.contains(entityUri) == false) {
			this.entityUri.add(entityUri);
		}
	}
	public List<String> getCharacteristic() {
		return characteristic;
	}
	public void setCharacteristic(List<String> characteristic) {
		this.characteristic = characteristic;
	}
	public void addCharacteristic(String characteristic) {
		if (this.characteristic.contains(characteristic) == false) {
			this.characteristic.add(characteristic);
		}
	}
	public List<String> getCharacteristicUri() {
		return characteristicUri;
	}
	public void setCharacteristicUri(List<String> characteristicUri) {
		this.characteristicUri = characteristicUri;
	}
	public void addCharacteristicUri(String characteristicUri) {
		if (this.characteristicUri.contains(characteristicUri) == false) {
			this.characteristicUri.add(characteristicUri);
		}
	}
	public String getSchemaUri() {
		return schemaUri;
	}
	public void setSchemaUri(String schemaUri) {
		this.schemaUri = schemaUri;
	}
	public String getDeploymentUri() {
		return deploymentUri;
	}
	public void setDeploymentUri(String deploymentUri) {
		this.deploymentUri = deploymentUri;
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public List<String> getDatasetUri() {
		return datasetUri;
	}
	public void setDatasetUri(List<String> datasetUri) {
		this.datasetUri = datasetUri;
	}
	public void addDatasetUri(String dataset_uri) {
		for(String uri : datasetUri){
			if(uri.equals(dataset_uri)){
				return;
			}
		}
		this.datasetUri.add(uri);
	}
	public boolean containsDataset(String uri) {
		return datasetUri.contains(uri);
	}
	public void addNumberDataPoints(long number) {
		numberDataPoints += number;
	}
	
	public boolean isFinished() {
		if (endedAt == null) {
			return false;
		} else {
			return endedAt.isBeforeNow();
		}
	}
	
	public int save() {
		try {
			SolrClient client = new HttpSolrClient(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_COLLECTION);
			if (null == endedAt) {
				endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
			}
			else if (endedAt.toString().startsWith("9999")) {
				endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
			}
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] DataAcquisition.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public static DataAcquisition convertFromSolr(SolrDocument doc) {
		Iterator<Object> i;
		DateTime date;
		DataAcquisition dataAcquisition = new DataAcquisition();
		dataAcquisition.setUri(doc.getFieldValue("uri").toString());
		dataAcquisition.setOwnerUri(doc.getFieldValue("owner_uri").toString());
		dataAcquisition.setPermissionUri(doc.getFieldValue("permission_uri").toString());
		dataAcquisition.setStudyUri(doc.getFieldValue("study_uri").toString());
		dataAcquisition.setTriggeringEvent(Integer.parseInt(doc.getFieldValue("triggering_event").toString()));
		dataAcquisition.setNumberDataPoints(Long.parseLong(doc.getFieldValue("nr_data_points").toString()));
		date = new DateTime((Date)doc.getFieldValue("started_at"));
		dataAcquisition.setStartedAt(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
		date = new DateTime((Date)doc.getFieldValue("ended_at"));
		dataAcquisition.setEndedAt(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
		if (doc.getFieldValues("schema_uri") != null) {
			dataAcquisition.setSchemaUri(doc.getFieldValue("schema_uri").toString());
		}
		if (doc.getFieldValues("unit") != null) {
			i = doc.getFieldValues("unit").iterator();
			while (i.hasNext()) {
				dataAcquisition.addUnit(i.next().toString());
			}
		}
		if (doc.getFieldValues("unit_uri") != null) {
			i = doc.getFieldValues("unit_uri").iterator();
			while (i.hasNext()) {
				dataAcquisition.addUnitUri(i.next().toString());
			}
		}
		if (doc.getFieldValues("entity") != null) {
			i = doc.getFieldValues("entity").iterator();
			while (i.hasNext()) {
				dataAcquisition.addEntity(i.next().toString());
			}
		}
		if (doc.getFieldValues("entity_uri") != null) {
			i = doc.getFieldValues("entity_uri").iterator();
			while (i.hasNext()) {
				dataAcquisition.addEntityUri(i.next().toString());
			}
		}
		if (doc.getFieldValues("characteristic") != null) {
			i = doc.getFieldValues("characteristic").iterator();
			while (i.hasNext()) {
				dataAcquisition.addCharacteristic(i.next().toString());
			}
		}
		if (doc.getFieldValues("characteristic_uri") != null) {
			i = doc.getFieldValues("characteristic_uri").iterator();
			while (i.hasNext()) {
				dataAcquisition.addCharacteristicUri(i.next().toString());
			}
		}
		
		dataAcquisition.setDeploymentUri(doc.getFieldValue("deployment_uri").toString());
		dataAcquisition.setInstrumentModel(doc.getFieldValue("instrument_model").toString());
		dataAcquisition.setInstrumentUri(doc.getFieldValue("instrument_uri").toString());
		dataAcquisition.setPlatformName(doc.getFieldValue("platform_name").toString());
		dataAcquisition.setPlatformUri(doc.getFieldValue("platform_uri").toString());
		
		if (doc.getFieldValues("dataset_uri") != null) {
			i = doc.getFieldValues("dataset_uri").iterator();
			while (i.hasNext()) {
				dataAcquisition.addDatasetUri(i.next().toString());
			}
		}
		
		return dataAcquisition;
	}
	
	public static List<DataAcquisition> find(String ownerUri, State state) {
		SolrQuery query = new SolrQuery();
		if (state.getCurrent() == State.ALL) {
			if (null == ownerUri) {
				query.set("q", "owner_uri:*");
			}
			else {
				query.set("q", "owner_uri:\"" + ownerUri + "\"");
			}
		} else if (state.getCurrent() == State.ACTIVE) {
			if (null == ownerUri) {
				query.set("q", "owner_uri:* AND " + "ended_at:\"9999-12-31T23:59:59.999Z\"");
			}
			else {
				query.set("q", "owner_uri:\"" + ownerUri + "\"" + " AND " + "ended_at:\"9999-12-31T23:59:59.999Z\"");
			}
		} else {  // it is assumed that state is CLOSED
			if (null == ownerUri) {
				query.set("q", "owner_uri:* AND " + "-ended_at:\"9999-12-31T23:59:59.999Z\"");
			}
			else {
				query.set("q", "owner_uri:\"" + ownerUri + "\"" + " AND " + "-ended_at:\"9999-12-31T23:59:59.999Z\"");
			}
		}
		query.set("sort", "started_at asc");
		query.set("rows", "10000000");
		
		return findByQuery(query);
	}
	
	public static List<String> findAllAccessibleDataAcquisition(String user_uri){
		List<String> listURI = new ArrayList<String>();
		
		String group_uri = "";
		if(user_uri.equals("Public")){
			group_uri = "Public";
		}
		else{
			User user = User.find(user_uri);
			if(null == user){
				return listURI;
			}
			else{
				group_uri = user.getImmediateGroupUri();
			}
		}
		
		for(DataAcquisition acquisition : findAll()) {
			if(acquisition.getPermissionUri().equals("Public")
			|| acquisition.getPermissionUri().equals(group_uri)
			|| acquisition.getPermissionUri().equals(user_uri)){
				listURI.add(acquisition.getUri());
			}
		}
		
		return listURI;
	}
	
	public static List<DataAcquisition> findAll() {
		SolrQuery query = new SolrQuery();
		query.set("q", "owner_uri:*");
		query.set("sort", "started_at asc");
		query.set("rows", "10000000");
		
		return findByQuery(query);
	}
	
	public static List<DataAcquisition> findAll(State state) {
		return find(null, state);
	}
	
	public static List<DataAcquisition> find(String ownerUri) {
		SolrQuery query = new SolrQuery();
		query.set("q", "owner_uri:\"" + ownerUri + "\"");
		query.set("sort", "started_at asc");
		query.set("rows", "10000000");
		
		return findByQuery(query);
	}
	
	public static DataAcquisition findDataAcquisition(SolrQuery query) {
		DataAcquisition dataAcquisition = null;
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_COLLECTION);
		
		try {
			QueryResponse queryResponse = solr.query(query);
			solr.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				dataAcquisition = convertFromSolr(list.get(0));
			}
		} catch (Exception e) {
			System.out.println("[ERROR] DataAcquisition.find(SolrQuery) - Exception message: " + e.getMessage());
		}
				
		return dataAcquisition;
	}
	
	public static DataAcquisition findByUri(String dataAcquisitionUri) {
		SolrQuery query = new SolrQuery();
		query.set("q", "uri:\"" + dataAcquisitionUri + "\"");
		query.set("sort", "started_at asc");
		query.set("rows", "10000000");
		
		List<DataAcquisition> results = findByQuery(query);
		if (!results.isEmpty()) {
			return results.get(0);
		}

		return null;
	}
	
	public int close(String endedAt) {
		this.setEndedAtXsd(endedAt);
		return this.save();
	}
	
	public int delete() {
		try {
			Iterator<String> i = datasetUri.iterator();
			while (i.hasNext()) {
				Measurement.delete(i.next());
			}
			SolrClient solr = new HttpSolrClient(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_COLLECTION);
			UpdateResponse response = solr.deleteById(this.uri);
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] DataAcquisition.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] DataAcquisition.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] DataAcquisition.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public static List<DataAcquisition> findByQuery(SolrQuery query) {
		List<DataAcquisition> list = new ArrayList<DataAcquisition>();
		
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_COLLECTION);

		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			Iterator<SolrDocument> i = results.iterator();
			while (i.hasNext()) {
				list.add(convertFromSolr(i.next()));
			}
		} catch (Exception e) {
			list.clear();
			System.out.println("[ERROR] DataAcquisition.find(SolrQuery) - Exception message: " + e.getMessage());
		}
		
		return list;
	}
	
	public static List<DataAcquisition> find(Deployment deployment, boolean active) {
		SolrQuery query = new SolrQuery();
		query.set("q", "deployment_uri:\"" + deployment.getUri() + "\"");
		query.set("sort", "started_at desc");
		query.set("rows", "10000000");
		List<DataAcquisition> listDA = findByQuery(query);
		
		if (active == true) {
			// Filter out inactive data acquisition
			Iterator<DataAcquisition> iterDA = listDA.iterator();
			while (iterDA.hasNext()) {
				DataAcquisition dataAcquisition = iterDA.next();
				if (dataAcquisition.isFinished() == false) {
					iterDA.remove();
				}
			}
		}

		return listDA;
	}
	
	public static DataAcquisition find(HADataC hadatac) {
		SolrQuery query = new SolrQuery("uri:\"" + hadatac.getDataAcquisitionKbUri() + "\"");
		return findDataAcquisition(query);
	}
	
	public static DataAcquisition find(Model model, Dataset dataset) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT ?dc ?startedAt ?endedAt WHERE {\n"
				+ "  <" + dataset.getCcsvUri() + "> prov:wasGeneratedBy ?dc .\n"
				+ "  ?dc a hasneto:DataAcquisition .\n"
				+ "  ?dc prov:startedAtTime ?startedAt .\n"
				+ "  OPTIONAL { ?dc prov:endedAtTime ?endedAt } .\n"
				+ "}";
		System.out.println(queryString);
		
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			DataAcquisition dataAcquisition = new DataAcquisition();
			dataAcquisition.setLocalName(soln.getResource("dc").getLocalName());
			dataAcquisition.setCcsvUri(soln.getResource("dc").getURI());
			dataAcquisition.setStartedAtXsd(soln.getLiteral("startedAt").getString());
			if (soln.getLiteral("endedAt") != null) {
				dataAcquisition.setEndedAtXsd(soln.getLiteral("endedAt").getString());
			}
			dataAcquisition.setStatus(0);
			return dataAcquisition;
		}
		
		queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?dc ?endedAt WHERE {\n"
				+ "  <" + dataset.getCcsvUri() + "> prov:wasGeneratedBy ?dc .\n"
				+ "  ?dc prov:endedAtTime ?endedAt .\n"
				+ "}";
		
		query = QueryFactory.create(queryString);
		
		qexec = QueryExecutionFactory.create(query, model);
		results = qexec.execSelect();
		resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			DataAcquisition dataAcquisition = new DataAcquisition();
			dataAcquisition.setLocalName(soln.getResource("dc").getLocalName());
			dataAcquisition.setCcsvUri(soln.getResource("dc").getURI());
			dataAcquisition.setEndedAtXsd(soln.getLiteral("endedAt").getString());
			dataAcquisition.setStatus(2);
			return dataAcquisition;
		}
		
		queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ "SELECT ?dc ?endedAt WHERE {\n"
				+ "  <" + dataset.getCcsvUri() + "> prov:wasGeneratedBy ?dc .\n"
				+ "}";
		
		query = QueryFactory.create(queryString);
		
		qexec = QueryExecutionFactory.create(query, model);
		results = qexec.execSelect();
		resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			DataAcquisition dataAcquisition = new DataAcquisition();
			dataAcquisition.setLocalName(soln.getResource("dc").getLocalName());
			dataAcquisition.setCcsvUri(soln.getResource("dc").getURI());
			dataAcquisition.setStatus(1);
			return dataAcquisition;
		}
		
		return null;
	}
	
	public static DataAcquisition create(HADataC hadatacCcsv, HADataC hadatacKb) {
		DataAcquisition dataAcquisition = new DataAcquisition();
		
		dataAcquisition.setLocalName(hadatacCcsv.getDataAcquisition().getLocalName());
		dataAcquisition.setUri(hadatacCcsv.getDataAcquisitionKbUri());
		dataAcquisition.setStudyUri(hadatacCcsv.getDataAcquisition().getStudyUri());
		dataAcquisition.setStartedAtXsd(hadatacCcsv.getDataAcquisition().getStartedAtXsd());
		dataAcquisition.setEndedAtXsd(hadatacCcsv.getDataAcquisition().getEndedAtXsd());
		Iterator<MeasurementType> i = hadatacKb.getDataset().getMeasurementTypes().iterator();
		while (i.hasNext()) {
			MeasurementType measurementType = i.next();
			dataAcquisition.addCharacteristic(measurementType.getCharacteristicLabel());
			dataAcquisition.addCharacteristicUri(measurementType.getCharacteristicUri());
			dataAcquisition.addEntity(measurementType.getEntityLabel());
			dataAcquisition.addEntityUri(measurementType.getEntityUri());
			dataAcquisition.addUnit(measurementType.getUnitLabel());
			dataAcquisition.addUnitUri(measurementType.getUnitUri());
		}
		dataAcquisition.setDeploymentUri(hadatacKb.getDeploymentUri());
		dataAcquisition.setInstrumentModel(hadatacKb.getDeployment().getInstrument().getLabel());
		dataAcquisition.setInstrumentUri(hadatacKb.getDeployment().getInstrument().getUri());
		dataAcquisition.setPlatformName(hadatacKb.getDeployment().getPlatform().getLabel());
		dataAcquisition.setPlatformUri(hadatacKb.getDeployment().getPlatform().getUri());
		dataAcquisition.setLocation(hadatacKb.getDeployment().getPlatform().getLocation());
		dataAcquisition.setElevation(hadatacKb.getDeployment().getPlatform().getElevation());
		dataAcquisition.addDatasetUri(hadatacCcsv.getDatasetKbUri());
		
		return dataAcquisition;
	}
	
	public void merge(DataAcquisition dataCollection) {
		Iterator<String> i;
		
		i = dataCollection.unit.iterator();
		while (i.hasNext()) {
			addUnit(i.next());
		}
		i = dataCollection.unitUri.iterator();
		while (i.hasNext()) {
			addUnitUri(i.next());
		}
		i = dataCollection.entity.iterator();
		while (i.hasNext()) {
			addEntity(i.next());
		}
		i = dataCollection.entityUri.iterator();
		while (i.hasNext()) {
			addEntityUri(i.next());
		}
		i = dataCollection.characteristic.iterator();
		while (i.hasNext()) {
			addCharacteristic(i.next());
		}
		i = dataCollection.characteristicUri.iterator();
		while (i.hasNext()) {
			addCharacteristicUri(i.next());
		}
		i = dataCollection.datasetUri.iterator();
		while (i.hasNext()) {
			addDatasetUri(i.next());
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		Iterator<String> i;
		builder.append("localName: " + this.localName + "\n");
		builder.append("uri: " + this.getUri() + "\n");
		builder.append("started_at: " + this.getStartedAt() + "\n");
		builder.append("ended_at: " + this.getEndedAt() + "\n");
		i = unit.iterator();
		while (i.hasNext()) {
			builder.append("unit: " + i.next() + "\n");
		}
		i = unitUri.iterator();
		while (i.hasNext()) {
			builder.append("unit_uri: " + i.next() + "\n");
		}
		i = characteristic.iterator();
		while (i.hasNext()) {
			builder.append("characteristic: " + i.next() + "\n");
		}
		i = characteristicUri.iterator();
		while (i.hasNext()) {
			builder.append("characteristic_uri: " + i.next() + "\n");
		}
		i = entity.iterator();
		while (i.hasNext()) {
			builder.append("entity: " + i.next() + "\n");
		}
		i = entityUri.iterator();
		while (i.hasNext()) {
			builder.append("entity_uri: " + i.next() + "\n");
		}
		builder.append("deployment_uri: " + this.deploymentUri + "\n");
		builder.append("instrument_model: " + this.instrumentModel + "\n");
		builder.append("instrument_uri: " + this.instrumentUri + "\n");
		builder.append("platform_name: " + this.platformName + "\n");
		builder.append("platform_uri: " + this.platformUri + "\n");
		builder.append("location: " + this.location + "\n");
		builder.append("elevation: " + this.elevation + "\n");
		i = datasetUri.iterator();
		while (i.hasNext()) {
			builder.append("dataset_uri: " + i.next() + "\n");
		}
		
		return builder.toString();
	}
}