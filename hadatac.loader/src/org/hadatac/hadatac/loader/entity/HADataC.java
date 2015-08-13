package org.hadatac.hadatac.loader.entity;

import java.util.HashMap;

import com.hp.hpl.jena.rdf.model.Resource;

public class HADataC {
	private Resource node;
	private String hostname;
	
	private Dataset dataset;
	private DataCollection dataCollection;
	private Deployment deployment;
	
	private HashMap<String, MeasurementType> measurements;
	private HashMap<String, Characteristic> characteristics;
	private HashMap<String, Unit> units;
	private HashMap<String, Entity> entities;
	
	public HADataC(Resource node) {
		super();
		this.node = node;
		
		measurements = new HashMap<String, MeasurementType>();
		characteristics = new HashMap<String, Characteristic>();
		units = new HashMap<String, Unit>();
		entities = new HashMap<String, Entity>();
	}
	
	public void addMeasurement(String uri, MeasurementType measurement) {
		measurements.put(uri, measurement);
	}
	
	public boolean hasMeasurement(String uri) {
		return measurements.containsKey(uri);
	}
	
	public MeasurementType getMeasurement(String uri) {
		return measurements.get(uri);
	}
	
	public void addCharacteristic(String uri, Characteristic characteristic) {
		characteristics.put(uri, characteristic);
	}
	
	public boolean hasCharacteristic(String uri) {
		return characteristics.containsKey(uri);
	}
	
	public Characteristic getCharacteristic(String uri) {
		return characteristics.get(uri);
	}
	
	public void addUnit(String uri, Unit unit) {
		units.put(uri, unit);
	}
	
	public boolean hasUnit(String uri) {
		return units.containsKey(uri);
	}
	
	public Unit getUnit(String uri) {
		return units.get(uri);
	}
	
	public void addEntity(String uri, Entity entity) {
		entities.put(uri, entity);
	}
	
	public boolean hasEntity(String uri) {
		return entities.containsKey(uri);
	}
	
	public Entity getEntity(String uri) {
		return entities.get(uri);
	}
	
	public Entity getEntityFromMeasurement(String uri) {
		return entities.get(measurements.get(uri).getEntityURI());
	}
	
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
	public void setDataCollection(DataCollection dataCollection) {
		this.dataCollection = dataCollection;
	}
	
	public void setDeployment(Deployment deployment) {
		this.deployment = deployment;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		hostname = hostname.trim();
		if (hostname.endsWith("/")) {
			this.hostname = hostname.substring(0, hostname.length()-1); 
		} else {
			this.hostname = hostname;
		}
	}
	
	public String getURI() {
		return hostname + "/hadatac";
	}
	
	public String getMeasurementURI(String uri) {
		return hostname + "/hadatac/" + deployment.getLocalName() + "/" + dataCollection.getLocalName() + "/" + dataset.getLocalName() + "/" + measurements.get(uri).getLocalName();
	}
	
	public String getDatasetKbURI() {
		return hostname + "/hadatac/" + deployment.getLocalName() + "/" + dataCollection.getLocalName() + "/" + dataset.getLocalName();
	}
	
	public String getDatasetPreambleURI() {
		return dataset.getURI();
	}
	
	public String getDataCollectionKbURI() {
		return hostname + "/hadatac/" + deployment.getLocalName() + "/" + dataCollection.getLocalName();
	}
	
	public String getDataCollectionPreambleURI() {
		return dataCollection.getURI();
	}
	
	public String getDeploymentKbURI() {
		return hostname + "/hadatac/" + deployment.getLocalName();
	}
	
	public String getDeploymentPreambleURI() {
		return deployment.getURI();
	}
	
	public String getStaticMetadataSparqlURL() {
		return hostname + ":7574/solr/store/sparql";
	}
	
	public String getDynamicMetadataSelectURL() {
		return hostname + ":8983/solr/sdc/select";
	}
	
	public String getDynamicMetadataUpdateURL() {
		return hostname + ":8983/solr/sdc/update";
	}
	
	public String getMeasurementUpdateURL() {
		return hostname + ":8983/solr/measurement/update";
	}
}
