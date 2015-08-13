package org.hadatac.hadatac.loader.entity;

import com.hp.hpl.jena.rdf.model.Resource;

public class Dataset {
	private Resource node;
	
	public Dataset(Resource node) {
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
