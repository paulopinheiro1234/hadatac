package org.hadatac.hadatac.loader.entity;

import com.hp.hpl.jena.rdf.model.Resource;

public class MeasurementType {

	private Resource node;
	
	private String characteristicURI;
	private String unitURI;
	private String entityURI;

	public MeasurementType(Resource node) {
		super();
		this.node = node;
	}

	public String getLocalName() {
		return node.getLocalName();
	}

	public String getCharacteristicURI() {
		return characteristicURI;
	}

	public void setCharacteristicURI(String characteristicURI) {
		this.characteristicURI = characteristicURI;
	}

	public String getUnitURI() {
		return unitURI;
	}

	public void setUnitURI(String unitURI) {
		this.unitURI = unitURI;
	}

	public String getEntityURI() {
		return entityURI;
	}

	public void setEntityURI(String entityURI) {
		this.entityURI = entityURI;
	}
}
