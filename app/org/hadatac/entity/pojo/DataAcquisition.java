package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;

public class DataAcquisition extends HADatAcThing {
	private static String className = "hasco:DataAcquisition";

	@Field("uri")
	private String uri;
	@Field("label")
	private String label;
	@Field("comment")
	private String comment;
	@Field("used_uri")
	private String used_uri;

	private DateTime startedAt;
	private DateTime endedAt;

	@Field("owner_uri")
	private String ownerUri;
	@Field("permission_uri")
	private String permissionUri;
	@Field("parameter")
	private String parameter;
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
	private List<String> types;
	@Field("type_uri")
	private List<String> typeURIs;
	@Field("associated_uri")
	private List<String> associatedURIs;
	@Field("characteristic")
	private List<String> characteristic;
	@Field("characteristic_uri")
	private List<String> characteristicUri;
	@Field("study_uri")
	private String studyUri;
	@Field("method_uri")
	private String methodUri;
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
	private List<String> datasetURIs;
	@Field("globalscope_uri")
	private String globalScopeUri;
	@Field("globalscope_name")
	private String globalScopeName;
	@Field("localscope_uri")
	private List<String> localScopeUri;
	@Field("localscope_name")
	private List<String> localScopeName;
	@Field("status")
	private int status;
	/*
	 * 0 - DataAcquisition is a new one, its details on the preamble
	 * 		It should not exist inside the KB
	 * 		Preamble must contain deployment link and deployment must exists on the KB
	 * 1 - DataAcquisition already exists, only a reference present on the preamble
	 * 		It should exist inside the KB as not finished yet 
	 * 2 - DataAcquisition already exists, the preamble states its termination with endedAtTime information
	 * 		It should exist inside the KB as not finished yet
	 *
	 * 9999 - Data Acquisition spec is complete (anything else diferent than 9999 is considered incomplete
	 *
	 */

	private boolean isComplete;
	private String ccsvUri;
	private String localName;
	private Deployment deployment;
	public DataAcquisition() {
		startedAt = null;
		endedAt = null;
		numberDataPoints = 0;
		isComplete = false;
		datasetURIs = new ArrayList<String>();
		unit = new ArrayList<String>();
		unitUri = new ArrayList<String>();
		characteristic = new ArrayList<String>();
		characteristicUri = new ArrayList<String>();
		entity = new ArrayList<String>();
		entityUri = new ArrayList<String>();
		types = new ArrayList<String>();
		typeURIs = new ArrayList<String>();
		associatedURIs = new ArrayList<String>();
		deployment = null;
		globalScopeUri = null;
		globalScopeName = null;
		localScopeUri = new ArrayList<String>();
		localScopeName = new ArrayList<String>();
	}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof DataAcquisition) && (((DataAcquisition)o).getUri() == this.getUri())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
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

	public boolean isComplete() {
		return status == 9999;
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		if (uri == null || uri.equals("")) {
			this.uri = "";
			return;
		}
		this.uri = ValueCellProcessing.replacePrefixEx(uri);
	}

	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
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

	public String getStudyUri() {
		return studyUri;
	}
	public Study getStudy() {
		if (studyUri == null || studyUri.equals(""))  
			return null;
		Study study = Study.find(studyUri);
		return study;
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

	public boolean getIsComplete() {
		return isComplete;
	}
	public void setIsComplete(boolean isComplete) {
		this.isComplete = isComplete;
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
	public int getTriggeringEventByName(String name) {
		switch (name) {
		case TriggeringEvent.INITIAL_DEPLOYMENT_NAME:
			return TriggeringEvent.INITIAL_DEPLOYMENT;
		case TriggeringEvent.LEGACY_DEPLOYMENT_NAME:
			return TriggeringEvent.LEGACY_DEPLOYMENT;
		case TriggeringEvent.CHANGED_CONFIGURATION_NAME:
			return TriggeringEvent.CHANGED_CONFIGURATION;
		case TriggeringEvent.CHANGED_OWNERSHIP_NAME:
			return TriggeringEvent.CHANGED_OWNERSHIP;
		case TriggeringEvent.AUTO_CALIBRATION_NAME:
			return TriggeringEvent.AUTO_CALIBRATION;
		case TriggeringEvent.SUSPEND_DATA_ACQUISITION_NAME:
			return TriggeringEvent.SUSPEND_DATA_ACQUISITION;
		case TriggeringEvent.RESUME_DATA_ACQUISITION_NAME:
			return TriggeringEvent.RESUME_DATA_ACQUISITION;
		}

		return -1;
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
	public void setStartedAt(DateTime startedAt) {
		this.startedAt = startedAt;
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
	public void setEndedAt(DateTime endedAt) {
		this.endedAt = endedAt;
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
	public String getMethodUri() {
		return methodUri;
	}
	public void setMethodUri(String methodUri) {
		this.methodUri = methodUri;
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
	public Deployment getDeployment() {
		if (deploymentUri == null || deploymentUri.equals("")) {
			return null;
		}
		if (deployment != null) {
			if (deployment.getUri().equals(deploymentUri)) {
				return deployment;
			}
		}
		return deployment = Deployment.find(deploymentUri);
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

	public boolean hasScope() {
		if (globalScopeUri != null && !globalScopeUri.equals("")) {
			return true;
		}
		if (localScopeUri != null && localScopeUri.size() > 0) {
			for (String tmpUri : localScopeUri) {
				if (tmpUri != null && !tmpUri.equals("")) {
					return true;
				}
			}
		}
		return false;
	}

	public String getGlobalScopeUri() {
		return globalScopeUri;
	}
	public void setGlobalScopeUri(String globalScopeUri) {
		this.globalScopeUri = globalScopeUri;
		if (globalScopeUri == null || globalScopeUri.equals("")) {
			return;
		}
		ObjectCollection oc = ObjectCollection.find(globalScopeUri);
		if (oc != null) {
			if (oc.getUri().equals(globalScopeUri)) {
				globalScopeName = oc.getLabel();
				return;
			}
		} else {
			StudyObject obj = StudyObject.find(globalScopeUri);
			if (obj.getUri().equals(globalScopeUri)) {
				globalScopeName = obj.getLabel();
				return;
			}
		}
	}
	public String getGlobalScopeName() {
		return globalScopeName;
	}
	public void setGlobalScopeName(String globalScopeName) {
		this.globalScopeName = globalScopeName;
	}

	public List<String> getLocalScopeUri() {
		return localScopeUri;
	}
	public void setLocalScopeUri(List<String> localScopeUri) {
		this.localScopeUri = localScopeUri;
		if (localScopeUri == null || localScopeUri.size() == 0) {
			return;
		}
		localScopeName = new ArrayList<String>();
		for (String objUri : localScopeUri) {
			StudyObject obj = StudyObject.find(objUri);
			if (obj != null && obj.getUri().equals(objUri)) {
				localScopeName.add(obj.getLabel());
			} else {
				localScopeName.add("");
			}
		}
	}
	public void addLocalScopeUri(String localScopeUri) {
		this.localScopeUri.add(localScopeUri);
	}
	public List<String> getLocalScopeName() {
		return localScopeName;
	}
	public void setLocalScopeName(List<String> localScopeName) {
		this.localScopeName = localScopeName;
	}
	public void addLocalScopeName(String localScopeName) {
		this.localScopeName.add(localScopeName);
	}
	public List<String> getDatasetUri() {
		return datasetURIs;
	}
	public void setDatasetUri(List<String> datasetURIs) {
		this.datasetURIs = datasetURIs;
	}
	public void addDatasetUri(String dataset_uri) {
		if (!datasetURIs.contains(dataset_uri)) {
			datasetURIs.add(dataset_uri);
		}
	}
	public void deleteDatasetUri(String dataset_uri) {
		Iterator<String> iter = datasetURIs.iterator();
		while (iter.hasNext()){
			if (iter.next().equals(dataset_uri)) {
				iter.remove();
			}
		}
	}
	public void deleteAllDatasetURIs() {
		datasetURIs.clear();
	}
	public boolean containsDataset(String uri) {
		return datasetURIs.contains(uri);
	}

	public List<String> getTypeURIs() {
		return typeURIs;
	}
	public void setTypeURIs(List<String> typeURIs) {
		this.typeURIs = typeURIs;
	}
	public void addTypeUri(String type_uri) {
		if (!typeURIs.contains(type_uri)) {
			typeURIs.add(type_uri);
		}
	}

	public List<String> getAssociatedURIs() {
		return associatedURIs;
	}

	public void setAssociatedURIs(List<String> associatedURIs) {
		this.associatedURIs = associatedURIs;
	}
	public void addAssociatedUri(String associated_uri) {
		if (!associatedURIs.contains(associated_uri)) {
			associatedURIs.add(associated_uri);
		}
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
			SolrClient client = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_COLLECTION).build();
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
	
	public long getNumberFromSolr(List<String> values, FacetHandler facetHandler) {
		SolrQuery query = new SolrQuery();
		query.setQuery(facetHandler.getTempSolrQuery("ACQUISITION_URI", "acquisition_uri_str", values));
		query.setRows(0);
		query.setFacet(false);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			return results.getNumFound();
		} catch (Exception e) {
			System.out.println("[ERROR] DataAcquisition.getNumberFromSolr() - Exception message: " + e.getMessage());
		}

		return -1;
	}

	public static DataAcquisition convertFromSolr(SolrDocument doc) {
		Iterator<Object> i;
		DateTime date;

		DataAcquisition dataAcquisition = new DataAcquisition();
		try {
			if (doc.getFieldValue("uri") != null) {
				dataAcquisition.setUri(doc.getFieldValue("uri").toString());
			}
			if (doc.getFieldValue("owner_uri") != null) {
				dataAcquisition.setOwnerUri(doc.getFieldValue("owner_uri").toString());
			}
			if (doc.getFieldValue("permission_uri") != null) {
				dataAcquisition.setPermissionUri(doc.getFieldValue("permission_uri").toString());
			}
			if (doc.getFieldValue("parameter") != null) {
				dataAcquisition.setParameter(doc.getFieldValue("parameter").toString());
			}
			if (doc.getFieldValue("study_uri") != null) {
				dataAcquisition.setStudyUri(doc.getFieldValue("study_uri").toString());
			}
			if (doc.getFieldValue("triggering_event") != null) {
				dataAcquisition.setTriggeringEvent(Integer.parseInt(doc.getFieldValue("triggering_event").toString()));
			}
			if (doc.getFieldValue("nr_data_points") != null) {
				dataAcquisition.setNumberDataPoints(Long.parseLong(doc.getFieldValue("nr_data_points").toString()));
			}
			if (doc.getFieldValue("started_at") != null) {
				date = new DateTime((Date)doc.getFieldValue("started_at"));
				dataAcquisition.setStartedAt(date.withZone(DateTimeZone.UTC));
			}
			if (doc.getFieldValue("ended_at") != null) {
				date = new DateTime((Date)doc.getFieldValue("ended_at"));
				dataAcquisition.setEndedAt(date.withZone(DateTimeZone.UTC));
			}
			if (doc.getFieldValues("method_uri") != null) {
				dataAcquisition.setMethodUri(doc.getFieldValue("method_uri").toString());
			}
			if (doc.getFieldValues("schema_uri") != null) {
				dataAcquisition.setSchemaUri(doc.getFieldValue("schema_uri").toString());
			}
			if (doc.getFieldValue("label") != null) {
				dataAcquisition.setLabel(doc.getFieldValue("label").toString());
			}
			if (doc.getFieldValue("comment") != null) {
				dataAcquisition.setComment(doc.getFieldValue("comment").toString());
			}
			if (doc.getFieldValues("associated_uri") != null) {
				i = doc.getFieldValues("associated_uri").iterator();
				while (i.hasNext()) {
					dataAcquisition.addAssociatedUri(i.next().toString());
				}
			}
			if (doc.getFieldValues("type_uri") != null) {
				i = doc.getFieldValues("type_uri").iterator();
				while (i.hasNext()) {
					dataAcquisition.addTypeUri(i.next().toString());
				}
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
			if (doc.getFieldValue("deployment_uri") != null) {
				dataAcquisition.setDeploymentUri(doc.getFieldValue("deployment_uri").toString());
			}
			if (doc.getFieldValue("instrument_model") != null) {
				dataAcquisition.setInstrumentModel(doc.getFieldValue("instrument_model").toString());
			}
			if (doc.getFieldValue("instrument_uri") != null) {
				dataAcquisition.setInstrumentUri(doc.getFieldValue("instrument_uri").toString());
			}
			if (doc.getFieldValue("platform_name") != null) {
				dataAcquisition.setPlatformName(doc.getFieldValue("platform_name").toString());
			}
			if (doc.getFieldValue("platform_uri") != null) {
				dataAcquisition.setPlatformUri(doc.getFieldValue("platform_uri").toString());
			}
			if (doc.getFieldValues("dataset_uri") != null) {
				i = doc.getFieldValues("dataset_uri").iterator();
				while (i.hasNext()) {
					dataAcquisition.addDatasetUri(i.next().toString());
				}
			}
			if (doc.getFieldValue("globalscope_uri") != null) {
				dataAcquisition.setGlobalScopeUri(doc.getFieldValue("globalscope_uri").toString());
			}
			if (doc.getFieldValue("globalscope_name") != null) {
				dataAcquisition.setGlobalScopeName(doc.getFieldValue("globalscope_name").toString());
			}
			if (doc.getFieldValues("localscope_uri") != null) {
				i = doc.getFieldValues("localscope_uri").iterator();
				while (i.hasNext()) {
					dataAcquisition.addLocalScopeUri(i.next().toString());
				}
			}
			if (doc.getFieldValues("localscope_name") != null) {
				i = doc.getFieldValues("localscope_name").iterator();
				while (i.hasNext()) {
					dataAcquisition.addLocalScopeName(i.next().toString());
				}
			}
			if (doc.getFieldValue("status") != null) {
				dataAcquisition.setStatus(Integer.parseInt(doc.getFieldValue("status").toString()));
			}
		} catch (Exception e) {
			System.out.println("[ERROR] DataAcquisition.convertFromSolr(SolrDocument) - e.Message: " + e.getMessage());
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
				query.set("q", "owner_uri:* AND ended_at:\"9999-12-31T23:59:59.999Z\"");
			}
			else {
				query.set("q", "owner_uri:\"" + ownerUri + "\" AND ended_at:\"9999-12-31T23:59:59.999Z\"");
			}
		} else {  // it is assumed that state is CLOSED
			if (null == ownerUri) {
				query.set("q", "owner_uri:* AND -ended_at:\"9999-12-31T23:59:59.999Z\"");
			}
			else {
				query.set("q", "owner_uri:\"" + ownerUri + "\" AND -ended_at:\"9999-12-31T23:59:59.999Z\"");
			}
		}
		query.set("sort", "started_at asc");
		query.set("rows", "10000000");

		return findByQuery(query);
	}

	public static List<String> findAllAccessibleDataAcquisition(String user_uri) {
		List<String> results = new ArrayList<String>();
		List<String> accessLevels = new ArrayList<String>();

		User user = User.find(user_uri);
		if (null != user) {
			user.getGroupNames(accessLevels);
		}

		for(DataAcquisition acquisition : findAll()) {
			if(acquisition.getPermissionUri().equals("Public")
					|| acquisition.getPermissionUri().equals(user_uri)
					|| acquisition.getOwnerUri().equals(user_uri)){
				results.add(acquisition.getUri());
				continue;
			}

			for (String level : accessLevels) {
				if (acquisition.getPermissionUri().equals(level)) {
					results.add(acquisition.getUri());
				}
			}
		}

		return results;
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
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_COLLECTION).build();

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
		//System.out.println("inside findByUri: <" + dataAcquisitionUri + ">");
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
			deleteMeasurementData();

			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_COLLECTION).build();
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

	public boolean deleteMeasurementData() {
		Iterator<String> iter = datasetURIs.iterator();
		while (iter.hasNext()) {
			if (Measurement.delete(iter.next()) == 0) {
				iter.remove();
			}
		}

		return datasetURIs.isEmpty();
	}

	public static List<DataAcquisition> findByQuery(SolrQuery query) {
		List<DataAcquisition> results = new ArrayList<DataAcquisition>();

		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_COLLECTION).build();

		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList docs = response.getResults();
			Iterator<SolrDocument> i = docs.iterator();
			while (i.hasNext()) {
				results.add(convertFromSolr(i.next()));
			}
		} catch (Exception e) {
			results.clear();
			System.out.println("[ERROR] DataAcquisition.findByQuery(SolrQuery) - Exception message: " + e.getMessage());
		}

		return results;
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
				+ "  ?dc a hasco:DataAcquisition .\n"
				+ "  ?dc prov:startedAtTime ?startedAt .\n"
				+ "  OPTIONAL { ?dc prov:endedAtTime ?endedAt } .\n"
				+ "}";

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
		DataAcquisitionSchema schema = DataAcquisitionSchema.find(hadatacKb.getDataAcquisitionKbUri());

		dataAcquisition.setLocalName(hadatacCcsv.getDataAcquisition().getLocalName());
		dataAcquisition.setUri(hadatacCcsv.getDataAcquisitionKbUri());
		dataAcquisition.setStudyUri(hadatacCcsv.getDataAcquisition().getStudyUri());
		dataAcquisition.setStartedAtXsd(hadatacCcsv.getDataAcquisition().getStartedAtXsd());
		dataAcquisition.setEndedAtXsd(hadatacCcsv.getDataAcquisition().getEndedAtXsd());
		if (schema != null && schema.getAttributes() != null) {
			Iterator<DataAcquisitionSchemaAttribute> i = schema.getAttributes().iterator();
			while (i.hasNext()) {
				DataAcquisitionSchemaAttribute dasa = i.next();
				dataAcquisition.addCharacteristic(dasa.getAttributeLabel());
				dataAcquisition.addCharacteristicUri(dasa.getAttribute());
				dataAcquisition.addEntity(dasa.getEntityLabel());
				dataAcquisition.addEntityUri(dasa.getEntity());
				dataAcquisition.addUnit(dasa.getUnitLabel());
				dataAcquisition.addUnitUri(dasa.getUnit());
			}
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
		i = dataCollection.datasetURIs.iterator();
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
		builder.append("globalScopeUri: " + this.globalScopeUri + "\n");
		builder.append("globalScopeName: " + this.globalScopeName + "\n");
		for (String localUri : localScopeUri) {
			builder.append("localScopeUri: " + localUri + "\n");
		}
		for (String localName : localScopeName) {
			builder.append("localScopeName: " + localName + "\n");
		}
		i = datasetURIs.iterator();
		while (i.hasNext()) {
			builder.append("dataset_uri: " + i.next() + "\n");
		}

		return builder.toString();
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public int saveToLabKey(String user_name, String password) throws CommandException {

		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");

		LabkeyDataHandler loader = new LabkeyDataHandler(
				site, user_name, password, path);

		List<String> abbrevTypeURIs = new ArrayList<String>();
		for (String uri : getTypeURIs()) {
			abbrevTypeURIs.add(ValueCellProcessing.replaceNameSpaceEx(uri));
		}
		List<String> abbrevAssociatedURIs = new ArrayList<String>();
		for (String uri : getAssociatedURIs()) {
			abbrevAssociatedURIs.add(ValueCellProcessing.replaceNameSpaceEx(uri));
		}

		String localUri = "";
		int totalChanged = 0;
		Iterator<String> i = getLocalScopeUri().iterator();
		while (i.hasNext()) {
			localUri += ValueCellProcessing.replaceNameSpaceEx(i.next());
			if (i.hasNext()) {
				localUri += " , ";
			}
		}
		List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("a", String.join(", ", abbrevTypeURIs));
		row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
		row.put("rdfs:label", getLabel());
		row.put("rdfs:comment", getComment());
		row.put("prov:startedAtTime", getStartedAt());
		row.put("prov:used", getParameter());
		row.put("prov:wasAssociatedWith", String.join(", ", abbrevAssociatedURIs));
		row.put("hasco:hasDeployment", ValueCellProcessing.replaceNameSpaceEx(getDeploymentUri()));
		row.put("hasco:isDataAcquisitionOf", ValueCellProcessing.replaceNameSpaceEx(getStudyUri()));
		row.put("hasco:hasSchema", ValueCellProcessing.replaceNameSpaceEx(getSchemaUri()));
		row.put("hasco:hasGlobalScope", ValueCellProcessing.replaceNameSpaceEx(getGlobalScopeUri()));
		row.put("hasco:hasLocalScope", localUri); 
		row.put("hasco:hasTriggeringEvent", getTriggeringEventName());
		row.put("prov:endedAtTime", getEndedAt().startsWith("9999")? "" : getEndedAt());
		rows.add(row);

		try {
			totalChanged = loader.insertRows("DataAcquisition", rows);
		} catch (CommandException e) {
			try {
				totalChanged = loader.updateRows("DataAcquisition", rows);
			} catch (CommandException e2) {
				System.out.println("[ERROR] Could not insert or update Data Acquisition");
			}
		}

		return totalChanged;
	}
}

