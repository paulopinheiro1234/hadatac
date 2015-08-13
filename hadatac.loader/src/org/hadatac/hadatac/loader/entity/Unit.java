package org.hadatac.hadatac.loader.entity;

import com.hp.hpl.jena.rdf.model.Resource;

public class Unit {
	private Resource node;
	private String uri;
	private String label;
	
	public Unit(Resource node) {
		super();
		this.node = node;
	}
	
	public String getUri() {
		return node.getURI();
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
	
	public String getLocalName() {
		return node.getLocalName();
	}
}
