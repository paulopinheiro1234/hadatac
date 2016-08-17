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
import org.hadatac.data.loader.util.Sparql;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import play.Play;

public class DataCollection {
	@Field("uri")
	private String uri;
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
	@Field("label")
	private List<String> label;
	@Field("comment")
    private List<String> comment;
	@Field("used_uri")
    private List<String> usedUri;
	@Field("was_associated_with_uri")
    private List<String> wasAssociatedWithUri;
	@Field("unit")
	private List<String> unit;
	@Field("unit_uri")
	private List<String> unitUri;
	@Field("entity")
	private List<String> entity;
	@Field("entity_uri")
	private List<String> entityUri;
	@Field("characteristic")
	private List<String> characteristic;
	@Field("characteristic_uri")
	private List<String> characteristicUri;
	@Field("type")
    private String type;
	@Field("type_uri")
    private String typeUri;
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
	 * 0 - DataCollection is a new one, its details on the preamble
	 * 		It should not exist inside the KB
	 * 		Preamble must contain deployment link and deployment must exists on the KB
	 * 1 - DataCollection already exists, only a reference present on the preamble
	 * 		It should exist inside the KB as not finished yet 
	 * 2 - DataCollection already exists, the preamble states its termination with endedAtTime information
	 * 		It should exist inside the KB as not finished yet
	 */
	
	public DataCollection() {
		startedAt = null;
		endedAt = null;
		numberDataPoints = 0;
		label = new ArrayList<String>();
		comment = new ArrayList<String>();
		usedUri = new ArrayList<String>();
		wasAssociatedWithUri = new ArrayList<String>();
		datasetUri = new ArrayList<String>();
		unit = new ArrayList<String>();
		unitUri = new ArrayList<String>();
		characteristic = new ArrayList<String>();
		characteristicUri = new ArrayList<String>();
		entity = new ArrayList<String>();
		entityUri = new ArrayList<String>();
	}
	
	public List<String> getLabel() {
        return label;
    }

    public void setLabel(List<String> label) {
        this.label = label;
    }
    
    public void addLabel(String label) {
        if (this.label.contains(label) == false) {
            this.label.add(label);
        }
    }

    public List<String> getComment() {
        return comment;
    }

    public void setComment(List<String> comment) {
        this.comment = comment;
    }
    
    public void addComment(String comment) {
        if (this.comment.contains(comment) == false) {
            this.comment.add(comment);
        }
    }

    public List<String> getUsedUri() {
        return usedUri;
    }

    public void setUsedUri(List<String> usedUri) {
        this.usedUri = usedUri;
    }
    
    public void addUsedUri(String usedUri) {
        if (this.usedUri.contains(usedUri) == false) {
            this.usedUri.add(usedUri);
        }
    }

    public List<String> getWasAssociatedWithUri() {
        return wasAssociatedWithUri;
    }

    public void setWasAssociatedWithUri(List<String> wasAssociatedWithUri) {
        this.wasAssociatedWithUri = wasAssociatedWithUri;
    }
    
    public void addWasAssociatedWithUri(String wasAssociatedWithUri) {
        if (this.wasAssociatedWithUri.contains(wasAssociatedWithUri) == false) {
            this.wasAssociatedWithUri.add(wasAssociatedWithUri);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeUri() {
        return typeUri;
    }

    public void setTypeUri(String typeUri) {
        this.typeUri = typeUri;
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
	public void addDatasetUri(String datasetUri) {
		this.datasetUri.add(datasetUri);
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
			SolrClient client = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/sdc");
			if (endedAt == null) {
				endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
			}
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] DataCollection.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public int save(SolrClient solr) {
		try {
			if (endedAt == null) {
				endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
			}
			int status = solr.addBean(this).getStatus();
			solr.commit();
			solr.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] DataCollection.save(SolrClient) - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public static DataCollection convertFromSolr(SolrDocument doc) {
		Iterator<Object> i;
		DateTime date;
		DataCollection dataCollection = new DataCollection();
		dataCollection.setUri(doc.getFieldValue("uri").toString());
		dataCollection.setOwnerUri(doc.getFieldValue("owner_uri").toString());
		dataCollection.setPermissionUri(doc.getFieldValue("permission_uri").toString());
		dataCollection.setTriggeringEvent(Integer.parseInt(doc.getFieldValue("triggering_event").toString()));
		dataCollection.setNumberDataPoints(Long.parseLong(doc.getFieldValue("nr_data_points").toString()));
		date = new DateTime((Date)doc.getFieldValue("started_at"));
		dataCollection.setStartedAt(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
		date = new DateTime((Date)doc.getFieldValue("ended_at"));
		dataCollection.setEndedAt(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
		if (doc.getFieldValues("label") != null) {
            i = doc.getFieldValues("label").iterator();
            while (i.hasNext()) {
                dataCollection.addLabel(i.next().toString());
            }
        }
		if (doc.getFieldValues("comment") != null) {
            i = doc.getFieldValues("comment").iterator();
            while (i.hasNext()) {
                dataCollection.addComment(i.next().toString());
            }
        }
		if (doc.getFieldValues("used_uri") != null) {
            i = doc.getFieldValues("used_uri").iterator();
            while (i.hasNext()) {
                dataCollection.addUsedUri(i.next().toString());
            }
        }
		if (doc.getFieldValues("was_associated_with_uri") != null) {
            i = doc.getFieldValues("was_associated_with_uri").iterator();
            while (i.hasNext()) {
                dataCollection.addWasAssociatedWithUri(i.next().toString());
            }
        }
		if (doc.getFieldValues("unit") != null) {
			i = doc.getFieldValues("unit").iterator();
			while (i.hasNext()) {
				dataCollection.addUnit(i.next().toString());
			}
		}
		if (doc.getFieldValues("unit_uri") != null) {
			i = doc.getFieldValues("unit_uri").iterator();
			while (i.hasNext()) {
				dataCollection.addUnitUri(i.next().toString());
			}
		}
		
		dataCollection.setType(doc.getFieldValue("type").toString());
		dataCollection.setTypeUri(doc.getFieldValue("type_uri").toString());
		
		if (doc.getFieldValues("entity") != null) {
			i = doc.getFieldValues("entity").iterator();
			while (i.hasNext()) {
				dataCollection.addEntity(i.next().toString());
			}
		}
		if (doc.getFieldValues("entity_uri") != null) {
			i = doc.getFieldValues("entity_uri").iterator();
			while (i.hasNext()) {
				dataCollection.addEntityUri(i.next().toString());
			}
		}
		if (doc.getFieldValues("characteristic") != null) {
			i = doc.getFieldValues("characteristic").iterator();
			while (i.hasNext()) {
				dataCollection.addCharacteristic(i.next().toString());
			}
		}
		if (doc.getFieldValues("characteristic_uri") != null) {
			i = doc.getFieldValues("characteristic_uri").iterator();
			while (i.hasNext()) {
				dataCollection.addCharacteristicUri(i.next().toString());
			}
		}
		
		dataCollection.setDeploymentUri(doc.getFieldValue("deployment_uri").toString());
		dataCollection.setInstrumentModel(doc.getFieldValue("instrument_model").toString());
		dataCollection.setInstrumentUri(doc.getFieldValue("instrument_uri").toString());
		dataCollection.setPlatformName(doc.getFieldValue("platform_name").toString());
		dataCollection.setPlatformUri(doc.getFieldValue("platform_uri").toString());
		
		if (doc.getFieldValues("dataset_uri") != null) {
			i = doc.getFieldValues("dataset_uri").iterator();
			while (i.hasNext()) {
				dataCollection.addDatasetUri(i.next().toString());
			}
		}
		
		return dataCollection;
	}
	
	public static List<DataCollection> find(String ownerUri, State state) {
		List<DataCollection> list = new ArrayList<DataCollection>();
		
		SolrClient solr = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/sdc");
		SolrQuery query = new SolrQuery();
		if (state.getCurrent() == State.ALL) {
			query.set("q", "owner_uri:\"" + ownerUri + "\"");
		} else { 
			if (state.getCurrent() == State.ACTIVE) {
		      query.set("q", "owner_uri:\"" + ownerUri + "\"" + " AND " + "ended_at:\"9999-12-31T23:59:59.999Z\"");
			} else {  // it is assumed that state is CLOSED
			      query.set("q", "owner_uri:\"" + ownerUri + "\"" + " AND " + "-ended_at:\"9999-12-31T23:59:59.999Z\"");
			}
		}
		query.set("sort", "started_at asc");
		
		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			Iterator<SolrDocument> i = results.iterator();
			while (i.hasNext()) {
				DataCollection dataCollection = convertFromSolr(i.next());
				list.add(dataCollection);
			}
		} catch (Exception e) {
			list.clear();
			System.out.println("[ERROR] DataCollection.find(String) - Exception message: " + e.getMessage());
		}
		
		return list;
	}
	
	public static List<DataCollection> find(String ownerUri) {
		List<DataCollection> list = new ArrayList<DataCollection>();
		
		SolrClient solr = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/sdc");
		SolrQuery query = new SolrQuery();
		query.set("q", "owner_uri:\"" + ownerUri + "\"");
		query.set("sort", "started_at asc");
		
		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			Iterator<SolrDocument> i = results.iterator();
			while (i.hasNext()) {
				DataCollection dataCollection = convertFromSolr(i.next());
				list.add(dataCollection);
			}
		} catch (Exception e) {
			list.clear();
			System.out.println("[ERROR] DataCollection.find(String) - Exception message: " + e.getMessage());
		}
		
		return list;
	}
	
	public static DataCollection findByUri(String dataCollectionUri) {
		
		SolrClient solr = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/sdc");
		SolrQuery query = new SolrQuery();
		query.set("q", "uri:\"" + dataCollectionUri + "\"");
		query.set("sort", "started_at asc");
		DataCollection dataCollection = null;
		
		try {
			QueryResponse queryResponse = solr.query(query);
			solr.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				dataCollection = convertFromSolr(list.get(0));
				//hadatac.deployment = Deployment.find(hadatac);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] DataCollection.findByUri(dataCollectionUri) - Exception message: " + e.getMessage());
		}
				
		return dataCollection;
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
			SolrClient solr = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/sdc");
			UpdateResponse response = solr.deleteById(this.uri);
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] DataCollection.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] DataCollection.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] DataCollection.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public static List<DataCollection> find(Deployment deployment, boolean active) {
		SolrClient solr = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.data") + "/sdc");
		SolrQuery query = new SolrQuery();
		query.set("q", "deployment_uri:\"" + deployment.getUri() + "\"");
		query.set("sort", "started_at desc");
		List<DataCollection> list = new ArrayList<DataCollection>();
		
		try {
			QueryResponse queryResponse = solr.query(query);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			Iterator<SolrDocument> i = results.iterator();
			if (active == true) {
				if (i.hasNext()) {
					DataCollection dataCollection = convertFromSolr(i.next());
					if (dataCollection.isFinished() == false) {
						list.add(dataCollection);
					}
				}
			} else {
				while (i.hasNext()) {
					list.add(convertFromSolr(i.next()));
				}
			}
		} catch (Exception e) {
			System.out.println("[ERROR] DataCollection.find(Deployment, boolean) - Exception message: " + e.getMessage());
		}
		
		return list;
	}
	
	public static DataCollection find(HADataC hadatac) {
		SolrClient solr = new HttpSolrClient(hadatac.getDynamicMetadataURL());
		SolrQuery query = new SolrQuery("uri:\"" + hadatac.getDataCollectionKbUri() + "\"");
		DataCollection dataCollection = null;
		
		try {
			QueryResponse queryResponse = solr.query(query);
			solr.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				dataCollection = convertFromSolr(list.get(0));
				//hadatac.deployment = Deployment.find(hadatac);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] DataCollection.find(HADataC) - Exception message: " + e.getMessage());
		}
		
		return dataCollection;
	}
	
	public static DataCollection find(Model model, Dataset dataset) {
		String queryString = Sparql.prefix
				+ "SELECT ?dc ?startedAt ?endedAt WHERE {\n"
				+ "  <" + dataset.getCcsvUri() + "> prov:wasGeneratedBy ?dc .\n"
				+ "  ?dc a hasneto:DataCollection .\n"
				+ "  ?dc prov:startedAtTime ?startedAt .\n"
				+ "  OPTIONAL { ?dc prov:endedAtTime ?endedAt } .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() == 1) {
			QuerySolution soln = resultsrw.next();
			DataCollection dataCollection = new DataCollection();
			dataCollection.setLocalName(soln.getResource("dc").getLocalName());
			dataCollection.setCcsvUri(soln.getResource("dc").getURI());
			dataCollection.setStartedAtXsd(soln.getLiteral("startedAt").getString());
			if (soln.getLiteral("endedAt") != null) { dataCollection.setEndedAtXsd(soln.getLiteral("endedAt").getString()); }
			dataCollection.setStatus(0);
			return dataCollection;
		}
		
		queryString = Sparql.prefix
				+ "SELECT ?dc ?endedAt WHERE {\n"
				+ "  <" + dataset.getCcsvUri() + "> prov:wasGeneratedBy ?dc .\n"
				+ "  ?dc prov:endedAtTime ?endedAt .\n"
				+ "}";
		
		query = QueryFactory.create(queryString);
		
		qexec = QueryExecutionFactory.create(query, model);
		results = qexec.execSelect();
		resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() == 1) {
			QuerySolution soln = resultsrw.next();
			DataCollection dataCollection = new DataCollection();
			dataCollection.setLocalName(soln.getResource("dc").getLocalName());
			dataCollection.setCcsvUri(soln.getResource("dc").getURI());
			dataCollection.setEndedAtXsd(soln.getLiteral("endedAt").getString());
			dataCollection.setStatus(2);
			return dataCollection;
		}
		
		queryString = Sparql.prefix
				+ "SELECT ?dc ?endedAt WHERE {\n"
				+ "  <" + dataset.getCcsvUri() + "> prov:wasGeneratedBy ?dc .\n"
				+ "}";
		
		query = QueryFactory.create(queryString);
		
		qexec = QueryExecutionFactory.create(query, model);
		results = qexec.execSelect();
		resultsrw = ResultSetFactory.copyResults(results);
		
		if (resultsrw.size() == 1) {
			QuerySolution soln = resultsrw.next();
			DataCollection dataCollection = new DataCollection();
			dataCollection.setLocalName(soln.getResource("dc").getLocalName());
			dataCollection.setCcsvUri(soln.getResource("dc").getURI());
			dataCollection.setStatus(1);
			return dataCollection;
		}
		
		return null;
	}
	
	public static DataCollection create(HADataC hadatacCcsv, HADataC hadatacKb) {
		DataCollection dataCollection = new DataCollection();
		
		dataCollection.setLocalName(hadatacCcsv.dataCollection.getLocalName());
		dataCollection.setUri(hadatacCcsv.getDataCollectionKbUri());
		dataCollection.setStartedAtXsd(hadatacCcsv.dataCollection.getStartedAtXsd());
		dataCollection.setEndedAtXsd(hadatacCcsv.dataCollection.getEndedAtXsd());
		Iterator<MeasurementType> i = hadatacKb.dataset.measurementTypes.iterator();
		while (i.hasNext()) {
			MeasurementType measurementType = i.next();
			dataCollection.addCharacteristic(measurementType.getCharacteristicLabel());
			dataCollection.addCharacteristicUri(measurementType.getCharacteristicUri());
			dataCollection.addEntity(measurementType.getEntityLabel());
			dataCollection.addEntityUri(measurementType.getEntityUri());
			dataCollection.addUnit(measurementType.getUnitLabel());
			dataCollection.addUnitUri(measurementType.getUnitUri());
		}
		dataCollection.setDeploymentUri(hadatacKb.getDeploymentUri());
		dataCollection.setInstrumentModel(hadatacKb.deployment.instrument.getLabel());
		dataCollection.setInstrumentUri(hadatacKb.deployment.instrument.getUri());
		dataCollection.setPlatformName(hadatacKb.deployment.platform.getLabel());
		dataCollection.setPlatformUri(hadatacKb.deployment.platform.getUri());
		dataCollection.setLocation(hadatacKb.deployment.platform.getLocation());
		dataCollection.setElevation(hadatacKb.deployment.platform.getElevation());
		dataCollection.addDatasetUri(hadatacCcsv.getDatasetKbUri());
		
		return dataCollection;
	}
	
	public int setPermission(String uri) {
		this.permissionUri = uri;
		return 0;
	}
	
	public void merge(DataCollection dataCollection) {
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
		builder.append("LocalName: " + this.localName + "\n");
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