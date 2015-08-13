package org.hadatac.hadatac.loader.entity;

import com.hp.hpl.jena.rdf.model.Resource;

public class DataCollection {
	private Resource node;
	
	public DataCollection(Resource node) {
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
