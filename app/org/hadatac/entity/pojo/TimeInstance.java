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

public class TimeInstance extends HADatAcThing implements Comparable<TimeInstance> {

	public TimeInstance () {}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof TimeInstance) && (((TimeInstance)o).getUri().equals(this.getUri()))) {
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
		query.setQuery(facetHandler.getTempSolrQuery("TIME", "named_time_str", preValues));
		query.setRows(0);
		query.setFacet(true);
		query.setFacetLimit(-1);
		query.setParam("json.facet", "{ "
				+ "named_time_str:{ "
				+ "type: terms, "
				+ "field: named_time_str,"
				+ "limit: 1000}}");

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			Pivot pivot = Measurement.parseFacetResults(queryResponse);
			Map<HADatAcThing, List<HADatAcThing>> result = parsePivot(pivot);
			System.out.println("TimeInstance Parse Pivot: " + result);
			return parsePivot(pivot);
		} catch (Exception e) {
			System.out.println("[ERROR] TimeInstance.getTargetFacets() - Exception message: " + e.getMessage());
		}

		return null;
	}

	private Map<HADatAcThing, List<HADatAcThing>> parsePivot(Pivot pivot) {
		Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
		for (Pivot pivot_ent : pivot.children) {
			if (pivot_ent.value.isEmpty()) {
				continue;
			}
			
			TimeInstance time = new TimeInstance();
			if (pivot_ent.value.startsWith("http")) {
				time.setUri(pivot_ent.value);
				DataAcquisitionSchemaEvent dase = DataAcquisitionSchemaEvent.find(pivot_ent.value);
				if (dase != null) {
					time.setLabel(dase.getLabel());
				} else {
					time.setLabel(pivot_ent.value);
				}
			} else {
				time.setUri("");
				time.setLabel(pivot_ent.value);
			}
			time.setCount(pivot_ent.count);
			if (!results.containsKey(time)) {
				List<HADatAcThing> attributes = new ArrayList<HADatAcThing>();
				results.put(time, attributes);
			}
		}

		return results;
	}

	@Override
	public int compareTo(TimeInstance another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getUri().compareTo(another.getUri());
	}
}
