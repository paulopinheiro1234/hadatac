package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.WordUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.console.models.Facet;
import org.hadatac.utils.CollectionUtil;

import com.typesafe.config.ConfigFactory;

public class AttributeInstance extends HADatAcThing implements Comparable<AttributeInstance> {

    static String className = "sio:Attribute";

    public AttributeInstance () {}

    @Override
    public boolean equals(Object o) {
        if((o instanceof AttributeInstance) && (((AttributeInstance)o).getUri().equals(this.getUri()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }
    
    @Override
    public long getNumber(Facet facet, FacetHandler facetHandler) {
        return getNumberFromSolr(facet, facetHandler);
    }

    @Override
    public long getNumberFromSolr(Facet facet, FacetHandler facetHandler) {
        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(false);

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
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
    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromSolr(facet, facetHandler);
    }
    
    @Override
    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacetsFromSolr(
            Facet facet, FacetHandler facetHandler) {
        
        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ "
                + "characteristic_uri_str_multi:{ "
                + "type: terms, "
                + "field: characteristic_uri_str_multi, "
                + "limit: 1000}}");

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);            
            return parsePivot(pivot, facet);
        } catch (Exception e) {
            System.out.println("[ERROR] AttributeInstance.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
        }

        return null;
    }

    private Map<HADatAcThing, List<HADatAcThing>> parsePivot(Pivot pivot, Facet facet) {
        Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
        for (Pivot pivot_ent : pivot.children) {
            AttributeInstance attrib = new AttributeInstance();
            attrib.setUri(pivot_ent.getValue());
            attrib.setLabel(WordUtils.capitalize(Attribute.find(pivot_ent.getValue()).getLabel()));
            attrib.setCount(pivot_ent.getCount());
            attrib.setField("characteristic_uri_str_multi");

            if (!results.containsKey(attrib)) {
                List<HADatAcThing> children = new ArrayList<HADatAcThing>();
                results.put(attrib, children);
            }

            Facet subFacet = facet.getChildById(attrib.getUri());
            subFacet.putFacet("characteristic_uri_str_multi", attrib.getUri());
        }

        return results;
    }

    @Override
    public int compareTo(AttributeInstance another) {
        if (this.getLabel() != null && another.getLabel() != null) {
            return this.getLabel().compareTo(another.getLabel());
        }
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
