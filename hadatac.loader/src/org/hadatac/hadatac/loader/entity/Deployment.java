package org.hadatac.hadatac.loader.entity;

import com.hp.hpl.jena.rdf.model.Resource;

public class Deployment {
	private Resource node;
	
	public Deployment(Resource node) {
		super();
		this.node = node;
	}
	
	public String getLocalName() {
		return node.getLocalName();
	}
	
	public String getURI() {
		return node.getURI();
	}
}
