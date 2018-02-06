package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class EntityRole extends HADatAcThing implements Comparable<EntityRole> {

	static String className = "sio:Object";

	public EntityRole() {}
	
	@Override
	public boolean equals(Object o) {;
		if((o instanceof EntityRole) && (((EntityRole)o).getUri().equals(this.getUri()))) {
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
			Facet facet, FacetHandler facetHandler) {
		System.out.println("\nEntityRole facet: " + facet.toSolrQuery());
		
		String valueConstraint = "";
		if (!facet.getFacetValuesByField("entity_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?entityUri { " + stringify(
					facet.getFacetValuesByField("entity_uri_str"), true) + " } \n ";
		}
		
		if (!facet.getFacetValuesByField("entity_role_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?roleUri { " + stringify(
					facet.getFacetValuesByField("entity_role_uri_str"), true) + " } \n ";
		}
		
		if (!facet.getFacetValuesByField("characteristic_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?attributeUri { " + stringify(
					facet.getFacetValuesByField("characteristic_uri_str"), true) + " } \n ";
		}
		
		if (!facet.getFacetValuesByField("dasa_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?dasa { " + stringify(
					facet.getFacetValuesByField("dasa_uri_str"), true) + " } \n ";
		}
		
		String query = "";
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += "SELECT ?roleUri ?dasoSub ?dasa ?relation ?entityUri ?attributeUri ?attributeLabel WHERE { \n"
				+ valueConstraint
				+ "{ "
				+ "?dasa hasco:isAttributeOf ?daso . \n"
				+ "?daso hasco:hasRole ?roleUri . \n"
				+ "?daso sio:Relation ?relation . \n"
				+ "?dasa hasco:hasEntity ?entityUri . \n"
				+ "?dasa hasco:hasAttribute ?attributeUri . \n"
				+ "?attributeUri rdfs:label ?attributeLabel . \n"
				+ "} UNION { \n"
				+ "?dasa hasco:isAttributeOf ?dasoSub . \n"
				+ "?dasoSub sio:isPartOf ?daso . \n"
				+ "?dasoSub sio:Relation ?relation . \n"
				+ "?daso hasco:hasRole ?roleUri . \n"
				+ "?dasa hasco:hasEntity ?entityUri . \n"
				+ "?dasa hasco:hasAttribute ?attributeUri . \n"
				+ "?attributeUri rdfs:label ?attributeLabel . \n"
				+ "}}";
		
		System.out.println("EntityRole query: " + query);
		
		Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
		try {
			QueryExecution qe = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
			ResultSet resultSet = qe.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(resultSet);
			qe.close();
			
			if (resultsrw.size() == 0) {
				for (String value : facet.getFacetValuesByField("characteristic_uri_str")) {
					EntityRole role = new EntityRole();
					role.setUri(ValueCellProcessing.replacePrefix("sio:Sample"));
					role.setLabel("Sample");
					role.setField("entity_role_uri_str");
					
					AttributeInstance attrib = new AttributeInstance();
					attrib.setUri(value);
					attrib.setField("characteristic_uri_str");
					Attribute temp_attrib = Attribute.find(value);
					if (temp_attrib != null) {
						attrib.setLabel(WordUtils.capitalize(temp_attrib.getLabel()));
					} else {
						attrib.setLabel(WordUtils.capitalize(value.split("#")[1]));
					}
					
					if (!results.containsKey(role)) {
						List<HADatAcThing> facets = new ArrayList<HADatAcThing>();
						results.put(role, facets);
					}
					if (!results.get(role).contains(attrib)) {
						results.get(role).add(attrib);
					}
					
					Facet subFacet = facet.getChildById(role.getUri());
					subFacet.putFacet("entity_role_uri_str", role.getUri());
				}
			} else {
				while (resultsrw.hasNext()) {
					QuerySolution soln = resultsrw.next();
					EntityRole role = new EntityRole();
					if (soln.get("roleUri") != null && !soln.get("roleUri").toString().isEmpty()) {
						role.setUri(soln.get("roleUri").toString());
						role.setLabel(WordUtils.capitalize(soln.get("roleUri").toString().split("#")[1]));
						role.setField("entity_role_uri_str");
					} else if (soln.get("relation") != null || soln.get("relation").toString().isEmpty()) {
						role.setUri(ValueCellProcessing.replacePrefix("sio:Human"));
						role.setLabel("Human");
						role.setField("entity_role_uri_str");
					}
									
					AttributeInstance attrib = new AttributeInstance();
					attrib.setUri(soln.get("attributeUri").toString());
					attrib.setLabel(WordUtils.capitalize(soln.get("attributeLabel").toString()));
					attrib.setField("characteristic_uri_str");
					if (!results.containsKey(role)) {
						List<HADatAcThing> facets = new ArrayList<HADatAcThing>();
						results.put(role, facets);
					}
					if (!results.get(role).contains(attrib)) {
						results.get(role).add(attrib);
					}
					
					Facet subFacet = facet.getChildById(role.getUri());
					subFacet.putFacet("entity_role_uri_str", role.getUri());
					subFacet.putFacet("entity_uri_str", soln.get("entityUri").toString());
					subFacet.putFacet("dasa_uri_str", soln.get("dasa").toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}

	@Override
	public int compareTo(EntityRole another) {
		return this.getUri().compareTo(another.getUri());
	}
}

