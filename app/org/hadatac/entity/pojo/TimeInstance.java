package org.hadatac.entity.pojo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.utils.Collections;

import play.Play;

public class TimeInstance extends HADatAcThing implements Comparable<TimeInstance> {

	public TimeInstance () {}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof TimeInstance) && (((TimeInstance)o).getLabel().equals(this.getLabel()))) {
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
		String queryString = facetHandler.getTempSolrQuery("TIME", "named_time_str", preValues);
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		Date minTime = findMinTime("timestamp_date", queryString);
		System.out.println("minTime: " + minTime);
		Date maxTime = findMaxTime("timestamp_date", queryString);
		System.out.println("maxTime: " + maxTime);
		
		String gap = null;
		String param = "";
		if (minTime != null && maxTime != null) {
			gap = calculateTimeGap(minTime.toInstant(), maxTime.toInstant());
			param = "{ "
					+ "named_time_str:{ "
					+ "type: terms, "
					+ "field: named_time_str, "
					+ "limit: 1000}, "
					+ "timestamp_date:{ "
					+ "type: range, "
					+ "field: timestamp_date, "
					+ "start: \"" + sdfDate.format(minTime) + "Z\", "
					+ "end: \"" + sdfDate.format(maxTime) + "Z\", "
					+ "gap: \"" + gap + "\" } "
					+ "}";
		} else {
			param = "{ "
					+ "named_time_str:{ "
					+ "type: terms, "
					+ "field: named_time_str, "
					+ "limit: 1000}}";
		}
		query.setQuery(queryString);
		query.setRows(0);
		query.setFacet(true);
		query.setFacetLimit(-1);
		query.setParam("json.facet", param);
		
		//System.out.println("query.getParam(): " + query.getParams("json.facet")[0]);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			//System.out.println("response.getResponse(): " + queryResponse.getResponse());
			Pivot pivot = Measurement.parseFacetResults(queryResponse);
			return parsePivot(pivot);
		} catch (Exception e) {
			System.out.println("[ERROR] TimeInstance.getTargetFacets() - Exception message: " + e.getMessage());
		}

		return null;
	}
	
	public static Date findMinTime(String field, String q) {
		SolrQuery query = new SolrQuery();
		query.setQuery(q);
		query.setRows(1);
		query.addSort(field, SolrQuery.ORDER.asc);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();

			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();

			SolrDocumentList results = queryResponse.getResults();
			if (results.size() == 1) {
				Measurement m = Measurement.convertFromSolr(results.get(0), null);
				return m.getTimestamp();
			}
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.findMinTime(String, String) - IOException message: " + e.getMessage());
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.findMinTime(String, String) - SolrServerException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.findMinTime(String, String) - Exception message: " + e.getMessage());
		}

		return null;
	}

	public static Date findMaxTime(String field, String q) {
		SolrQuery query = new SolrQuery();
		query.setQuery(q);
		query.setRows(1);
		query.set(field, "[* TO NOW]");
		query.addSort(field, SolrQuery.ORDER.desc);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();

			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();

			SolrDocumentList results = queryResponse.getResults();
			if (results.size() == 1) {
				Measurement m = Measurement.convertFromSolr(results.get(0), null);
				return m.getTimestamp();
			}
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.findMaxTime(String, String) - IOException message: " + e.getMessage());
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.findMaxTime(String, String) - SolrServerException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.findMaxTime(String, String) - Exception message: " + e.getMessage());
		}

		return null;
	}

	public static String calculateTimeGap(Instant min, Instant max) {

		if (min == null || max == null) {
			return "";
		}

		Duration duration = Duration.between(min, max);

		long days = duration.toDays();
		long weeks = days / 7;
		long months = days / 30;
		long years = days / 365;

		if (years > 2) {
			return "+1YEAR";
		}
		if (months > 4) {
			return "+1MONTH";
		}
		if (weeks > 4) {
			return "+1WEEK";
		}
		if (days > 4) {
			return "+1DAY";
		}

		long hours = duration.toHours();

		if (hours > 4) {
			return "+1HOUR";
		}

		long minutes = duration.toMinutes();

		if (minutes > 4) {
			return "+1MINUTE";
		}

		return "+1MINUTE";
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
			time.setField(pivot_ent.field);
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
