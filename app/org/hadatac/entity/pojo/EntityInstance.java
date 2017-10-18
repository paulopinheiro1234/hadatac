package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.utils.Collections;

import play.Play;

public class EntityInstance extends HADatAcThing implements Comparable<EntityInstance> {

	static String className = "sio:Object";

	public EntityInstance() {}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof EntityInstance) && (((EntityInstance)o).getUri() == this.getUri())) {
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
		SolrQuery query = new SolrQuery();
		query.setQuery(facetHandler.getTempSolrQuery("CHAR_URI", "characteristic_uri_str", preValues));
		query.setRows(0);
		query.setFacet(true);
		query.setFacetLimit(-1);
		query.setParam("json.facet", "{ "
				+ "entity_uri_str:{ "
				+ "type: terms, "
				+ "field: entity_uri_str, "
				+ "limit: 1000, "
				+ "facet:{ "
				+ "characteristic_uri_str: { "
				+ "type : terms, "
				+ "field: characteristic_uri_str, "
				+ "limit: 1000}}}}");

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			Pivot pivot = Measurement.parseFacetResults(queryResponse);
			Map<HADatAcThing, List<HADatAcThing>> result = parsePivot(pivot);
			System.out.println("Parse Pivot: " + result);
			return parsePivot(pivot);
		} catch (Exception e) {
			System.out.println("[ERROR] Entity.getNumberFromSolr() - Exception message: " + e.getMessage());
		}

		return null;
	}
	
	private Map<HADatAcThing, List<HADatAcThing>> parsePivot(Pivot pivot) {
		Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
		for (Pivot pivot_ent : pivot.children) {
			EntityInstance entity = new EntityInstance();
			entity.setUri(pivot_ent.value);
			entity.setLabel(Entity.find(pivot_ent.value).getLabel());
			entity.setCount(pivot_ent.count);
			for (Pivot pivot_attrib : pivot_ent.children) {
				AttributeInstance attrib = new AttributeInstance();
				attrib.setUri(pivot_attrib.value);
				attrib.setLabel(Attribute.find(pivot_attrib.value).getLabel());
				attrib.setCount(pivot_attrib.count);
				if (!results.containsKey(entity)) {
					List<HADatAcThing> attributes = new ArrayList<HADatAcThing>();
					results.put(entity, attributes);
				}
				if (!results.get(entity).contains(attrib)) {
					results.get(entity).add(attrib);
				}
			}
		}
		
		return results;
	}

	@Override
	public int compareTo(EntityInstance another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getUri().compareTo(another.getUri());
	}
}

