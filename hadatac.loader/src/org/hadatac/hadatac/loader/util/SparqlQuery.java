package org.hadatac.hadatac.loader.util;

import com.hp.hpl.jena.rdf.model.Resource;

public class SparqlQuery {
	public static String getHADataCQuery() {
		return "" // query for measurement data
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX hadatac: <http://hadatac.org/ontology/hadatac.owl#>\n"
			+ "PREFIX oboe: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>\n"
			+ "PREFIX time: <http://www.w3.org/2006/time#>\n"
			+ "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vsto-instrument.owl#>\n"
			+ "PREFIX hasneto: <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#>\n"
			+ "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
			+ "PREFIX jp: <http://jefferson.tw.rpi.edu/ontology/jp-core.owl#>\n"
			+ "SELECT ?kb ?url WHERE {"
			+ "  ?kb a hadatac:KnowledgeBase ."
			+ "  ?kb hadatac:hasHost ?url ."
			+ "}";
	}
	
	public static String getDataCollectionQuery(String uri) {
		return "" // query for dataset metadata
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX oboe: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>\n"
			+ "PREFIX time: <http://www.w3.org/2006/time#>\n"
			+ "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vsto-instrument.owl#>\n"
			+ "PREFIX hasneto: <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#>\n"
			+ "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#>\n"
			+ "SELECT ?dc ?started_at ?ended_at\n"
			+ "WHERE {\n"
			+ "  <" + uri + "> prov:wasGeneratedBy ?dc .\n"
			+ "  OPTIONAL { ?dc prov:startedAtTime ?started_at . }\n"
			+ "  OPTIONAL { ?dc prov:endedAtTime ?ended_at . }\n"
			+ "}";
	}
	
	public static String getDatasetQuery() {
		return "" // query for dataset metadata
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX oboe: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>\n"
			+ "PREFIX time: <http://www.w3.org/2006/time#>\n"
			+ "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vsto-instrument.owl#>\n"
			+ "PREFIX hasneto: <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#>\n"
			+ "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#>\n"
			+ "SELECT ?ds\n"
			+ "WHERE {\n"
			+ "  ?ds rdf:type vstoi:Dataset .\n"
			+ "}";
	}
	
	public static String getMeasurementQuery() {
		return "" // query for measurement data
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX hadatac: <http://hadatac.org/ontology/hadatac.owl#>\n"
			+ "PREFIX oboe: <http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#>\n"
			+ "PREFIX time: <http://www.w3.org/2006/time#>\n"
			+ "PREFIX vstoi: <http://jefferson.tw.rpi.edu/ontology/vsto-instrument.owl#>\n"
			+ "PREFIX hasneto: <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#>\n"
			+ "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
			+ "PREFIX jp: <http://jefferson.tw.rpi.edu/ontology/jp-core.owl#>\n"
			+ "SELECT ?mt ?valueColumn ?characteristic ?unit ?ds ?ltColumn ?tsColumn ?dpColumn\n"
			//+ "SELECT ?mt ?ltColumn ?tsColumn ?valueColumn ?characteristic ?unit ?ds\n"
			//+ "SELECT ?mt ?ltColumn ?valueColumn ?characteristic ?unit ?ds\n"
			+ "WHERE {\n"
			+ "  ?mt rdf:type oboe:Measurement .\n"
			+ "  ?mt hadatac:atColumn ?valueColumn .\n"
			+ "  ?mt oboe:ofCharacteristic ?characteristic .\n"
			+ "  ?mt oboe:usesStandard ?unit .\n"
			+ "  OPTIONAL { SELECT ?mt ?tsColumn WHERE { ?mt rdf:type oboe:Measurement . ?mt time:inDateTime ?ts . ?ts hadatac:atColumn ?tsColumn . } }\n"
			+ "  OPTIONAL { SELECT ?mt ?ltColumn WHERE { ?mt rdf:type oboe:Measurement . ?mt geo:location ?lt . ?lt hadatac:atColumn ?ltColumn . } }\n"
			+ "  OPTIONAL { SELECT ?mt ?dpColumn WHERE { ?mt rdf:type oboe:Measurement . ?mt jp:atDepth ?dp . ?dp hadatac:atColumn ?dpColumn . } }\n"
			//+ "  OPTIONAL { ?ts ccsv:atColumn ?tsColumn . }\n"
			//+ "  OPTIONAL { ?mt time:inDateTime ?ts . }\n"
			//+ "  OPTIONAL { ?mt geo:location ?lt . }\n"
			//+ "  OPTIONAL { ?lt ccsv:atColumn ?ltColumn . }\n"
			+ "  ?ds hasneto:hasMeasurementType ?mt .\n"
			+ "}";
	}
	
	public static String getLabelQuery(String uri) {
		String query = "" // query for measurement data
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "SELECT ?label WHERE { "
			+ "  <" + uri + "> rdfs:label ?label . "
			+ "}";
		return query;
	}
	
	public static String getEntityQuery(String uri) {
		String query = "" // query for measurement data
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX hasneto: <http://jefferson.tw.rpi.edu/ontology/hasneto.owl#>\n"
			+ "SELECT ?entity ?label WHERE {"
			+ "  <" + uri + "> hasneto:ofEntity ?entity ."
			+ "  ?entity rdfs:label ?label . "
			+ "}";
		return query;
	}
}
