package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.console.models.Facet;
import org.hadatac.utils.CollectionUtil;


public class AttributeInstance extends HADatAcThing implements Comparable<AttributeInstance> {

    static String className = "sio:SIO_000614";

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
    public Map<Facetable, List<Facetable>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromSolr(facet, facetHandler);
    }
    
    @Override
    public Map<Facetable, List<Facetable>> getTargetFacetsFromSolr(
            Facet facet, FacetHandler facetHandler) {
        
        SolrQuery query = new SolrQuery();
        QueryResponse queryResponse = null;
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
            queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
        } catch (Exception e) {
            System.out.println("[ERROR] AttributeInstance.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
            return null;
        }

        Pivot pivot = Pivot.parseQueryResponse(queryResponse);            
        return parsePivot(pivot, facet, query.toString());

    }

    private Map<Facetable, List<Facetable>> parsePivot(Pivot pivot, Facet facet, String query) {
        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        
        for (Pivot pivot_ent : pivot.children) {
            AttributeInstance attrib = new AttributeInstance();
            attrib.setUri(pivot_ent.getValue());
            if (pivot_ent.getValue().contains("; ")) {
                List<String> uris = Arrays.asList(pivot_ent.getValue().split("; "));
                String label = String.join(" ", uris.stream()
                        .map(s -> WordUtils.capitalize(Attribute.find(s).getLabel()))
                        .collect(Collectors.toList()));
                attrib.setLabel(label);
            } else {
                attrib.setLabel(WordUtils.capitalize(Attribute.find(pivot_ent.getValue()).getLabel()));
            }
            attrib.setCount(pivot_ent.getCount());
            attrib.setQuery(query);
            attrib.setField("characteristic_uri_str_multi");

            if (!results.containsKey(attrib)) {
                List<Facetable> children = new ArrayList<Facetable>();
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
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }

}
