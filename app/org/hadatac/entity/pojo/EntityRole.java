package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;


public class EntityRole extends HADatAcThing implements Comparable<EntityRole> {

	static String className = "sio:Object";

	public EntityRole() {}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof EntityRole) && (((EntityRole)o).getUri() == this.getUri())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}
	
	public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
			List<String> preValues, FacetHandler facetHandler) {
		String query = "";
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += "SELECT ?roleUri ?dasoSub ?relation ?attributeUri ?attributeLabel WHERE { "
				+ "{ "
				+ "?dasa hasco:isAttributeOf ?daso . "
				+ "?daso hasco:hasRole ?roleUri . "
				+ "?daso sio:Relation ?relation . "
				+ "?dasa hasco:hasEntity ?entityUri . "
				+ "?dasa hasco:hasAttribute ?attributeUri . "
				+ "?attributeUri rdfs:label ?attributeLabel . "
				+ "} UNION { "
				+ "?dasa hasco:isAttributeOf ?dasoSub . "
				+ "?dasoSub sio:isPartOf ?daso . "
				+ "?dasoSub sio:Relation ?relation . "
				+ "?daso hasco:hasRole ?roleUri . "
				+ "?dasa hasco:hasEntity ?entityUri . "
				+ "?dasa hasco:hasAttribute ?attributeUri . "
				+ "?attributeUri rdfs:label ?attributeLabel . "
				+ "}}";

		Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
		try {
			QueryExecution qe = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
			ResultSet resultSet = qe.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(resultSet);
			qe.close();
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				EntityRole role = new EntityRole();
				if (soln.get("roleUri") != null && !soln.get("roleUri").toString().isEmpty()) {
					role.setUri(soln.get("roleUri").toString());
					role.setLabel(soln.get("roleUri").toString().split("#")[1]);
				} else if (soln.get("relation") != null || soln.get("relation").toString().isEmpty()) {
					role.setUri("http://semanticscience.org/resource/Sample");
					role.setLabel("Sample");
				}
								
				AttributeInstance attrib = new AttributeInstance();
				if (!preValues.isEmpty() && !preValues.contains(soln.get("attributeUri").toString())) {
					continue;
				}
				attrib.setUri(soln.get("attributeUri").toString());
				attrib.setLabel(soln.get("attributeLabel").toString());
				if (!results.containsKey(role)) {
					List<HADatAcThing> facets = new ArrayList<HADatAcThing>();
					results.put(role, facets);
				}
				if (!results.get(role).contains(attrib)) {
					results.get(role).add(attrib);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}

	@Override
	public int compareTo(EntityRole another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getUri().compareTo(another.getUri());
	}
}

