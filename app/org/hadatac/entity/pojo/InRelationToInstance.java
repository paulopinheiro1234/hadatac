package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.text.WordUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hadatac.console.controllers.dataacquisitionsearch.Facetable;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.utils.CollectionUtil;


public class InRelationToInstance extends HADatAcThing implements Comparable<InRelationToInstance> {

    public InRelationToInstance() {}

    @Override
    public boolean equals(Object o) {
        if((o instanceof InRelationToInstance) && (((InRelationToInstance)o).getUri().equals(this.getUri()))) {
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
    public Map<Facetable, List<Facetable>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromSolr(facet, facetHandler);
    }

    public Map<Facetable, List<Facetable>> getTargetFacetsFromSolr(
            Facet facet, FacetHandler facetHandler) {

        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ "
                + "in_relation_to_uri_str:{ "
                + "type: terms, "
                + "field: in_relation_to_uri_str, "
                + "limit: 1000}}");

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            return parsePivot(pivot, facet, query.toString());
        } catch (Exception e) {
            System.out.println("[ERROR] InRelationToInstance.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
        }

        return null;
    }

    private Map<Facetable, List<Facetable>> parsePivot(Pivot pivot, Facet facet, String query) {
        facet.clearFieldValues("in_relation_to_uri_str");

        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        for (Pivot pivot_ent : pivot.children) {
            InRelationToInstance object = new InRelationToInstance();
            object.setUri(pivot_ent.getValue());
            object.setLabel(WordUtils.capitalize(Entity.find(pivot_ent.getValue()).getLabel()));
            object.setCount(pivot_ent.getCount());
            object.setQuery(query);
            object.setField("in_relation_to_uri_str");

            if (!results.containsKey(object)) {
                List<Facetable> children = new ArrayList<Facetable>();
                results.put(object, children);
            }

            Facet subFacet = facet.getChildById(object.getUri());
            subFacet.putFacet("in_relation_to_uri_str", object.getUri());
        }

        return results;
    }

    @Override
    public int compareTo(InRelationToInstance another) {
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

