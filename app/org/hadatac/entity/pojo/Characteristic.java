package org.hadatac.entity.pojo;

import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.utils.Collections;

import play.Play;


public class Characteristic {
	private String uri;
	private String label;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public static Characteristic find(String uri) {
		return null;
	}
	
	public static List<Characteristic> find(List<MeasurementType> list) {
		return null;
	}
	
	public long getNumberFromSolr(List<String> charURIs, FacetHandler facetHandler) {
		for (String uri : charURIs) {
			facetHandler.putFacet("CHAR_URI", "characteristic_uri", uri);
		}
		SolrQuery query = new SolrQuery();
		query.setQuery(facetHandler.toSolrQuery());
		query.setRows(0);
		query.setFacet(false);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			return results.getNumFound();
		} catch (Exception e) {
			System.out.println("[ERROR] Characteristic.getNumberFromSolr() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
}
