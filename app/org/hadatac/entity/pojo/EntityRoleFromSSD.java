package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.utils.CollectionUtil;

import com.typesafe.config.ConfigFactory;

public class EntityRoleFromSSD extends HADatAcThing implements Comparable<EntityRoleFromSSD> {

	static String className = "sio:Object";

	public EntityRoleFromSSD() {}
	
	@Override
	public boolean equals(Object o) {;
		if((o instanceof EntityRoleFromSSD) && (((EntityRoleFromSSD)o).getUri().equals(this.getUri()))) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}
	
	public long getNumberFromSolr(Facet facet, FacetHandler facetHandler) {
        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(false);

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + CollectionUtil.DATA_ACQUISITION).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            SolrDocumentList results = queryResponse.getResults();
            return results.getNumFound();
        } catch (Exception e) {
            System.out.println("[ERROR] EntityRoleFromSSD.getNumberFromSolr() - Exception message: " + e.getMessage());
        }

        return -1;
    }
	
	public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {

        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ "
                + "role_uri_str:{ "
                + "type: terms, "
                + "field: role_uri_str, "
                + "limit: 1000}}");

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + CollectionUtil.DATA_ACQUISITION).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            return parsePivot(pivot, facet);
        } catch (Exception e) {
            System.out.println("[ERROR] EntityRoleFromSSD.getTargetFacets() - Exception message: " + e.getMessage());
        }

        return null;
    }

    private Map<HADatAcThing, List<HADatAcThing>> parsePivot(Pivot pivot, Facet facet) {
        facet.clearFieldValues("role_uri_str");

        Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
        for (Pivot pivot_ent : pivot.children) {
            EntityRoleFromSSD role = new EntityRoleFromSSD();
            role.setUri(pivot_ent.getValue());
            role.setLabel(WordUtils.capitalize(Entity.find(pivot_ent.getValue()).getLabel()));
            role.setCount(pivot_ent.getCount());
            role.setField("role_uri_str");

            if (!results.containsKey(role)) {
                List<HADatAcThing> children = new ArrayList<HADatAcThing>();
                results.put(role, children);
            }

            Facet subFacet = facet.getChildById(role.getUri());
            subFacet.putFacet("role_uri_str", role.getUri());
        }

        return results;
    }

	@Override
	public int compareTo(EntityRoleFromSSD another) {
		return this.getUri().compareTo(another.getUri());
	}

    @Override
    public boolean saveToTripleStore() {
        return false;
    }

    @Override
    public void deleteFromTripleStore() {
    }

    @Override
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }

    @Override
    public int saveToLabKey(String userName, String password) {
        return 0;
    }

    @Override
    public int deleteFromLabKey(String userName, String password) {
        return 0;
    }
}

