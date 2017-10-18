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

public class AttributeInstance extends HADatAcThing implements Comparable<AttributeInstance> {

	static String className = "sio:Attribute";

	public AttributeInstance () {}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof AttributeInstance) && (((AttributeInstance)o).getUri() == this.getUri())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}

	public long getNumberFromSolr(List<String> values, FacetHandler facetHandler) {
		SolrQuery query = new SolrQuery();
		query.setQuery(facetHandler.getTempSolrQuery("CHAR_URI", "characteristic_uri_str", values));
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
			System.out.println("[ERROR] AttributeInstance.getNumberFromSolr() - Exception message: " + e.getMessage());
		}

		return -1;
	}

	@Override
	public int compareTo(AttributeInstance another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getUri().compareTo(another.getUri());
	}
}
