package org.hadatac.utils;

import org.apache.jena.rdf.model.Resource;

public class OntologyTerm {
	public final Resource classIRI;
	public final String classTerm;

	public OntologyTerm(Resource classIRI, String classTerm) {
		this.classIRI = classIRI;
		this.classTerm = classTerm;
	}

	@Override
	public String toString() {
		return classTerm + "(" + classIRI.getURI() + ")";
	}

	@Override
	public boolean equals(Object o) {
		// If the object is compared with itself then return true
		if (o == this) {
			return true;
		}

		OntologyTerm measurable = (OntologyTerm) o;
		// Compare the data members and return accordingly
		return this.classTerm.equals(measurable.classTerm)
				&& this.classIRI.getURI().equals(measurable.classIRI.getURI());
	}
}