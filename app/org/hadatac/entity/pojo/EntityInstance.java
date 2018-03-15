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
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.utils.CollectionUtil;

import com.typesafe.config.ConfigFactory;

public class EntityInstance extends HADatAcThing implements Comparable<EntityInstance> {

    static String className = "sio:Object";

    public EntityInstance() {}

    @Override
    public boolean equals(Object o) {
        if((o instanceof EntityInstance) && (((EntityInstance)o).getUri().equals(this.getUri()))) {
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
        System.out.println("\nEntityInstance getTargetFacets facet: " + facet.toSolrQuery());

        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ "
                + "entity_uri_str:{ "
                + "type: terms, "
                + "field: entity_uri_str, "
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
            System.out.println("[ERROR] EntityInstance.getTargetFacets() - Exception message: " + e.getMessage());
        }

        return null;
    }

    private Map<HADatAcThing, List<HADatAcThing>> parsePivot(Pivot pivot, Facet facet) {
        facet.clearFieldValues("entity_uri_str");

        Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
        for (Pivot pivot_ent : pivot.children) {
            EntityInstance entity = new EntityInstance();
            entity.setUri(pivot_ent.getValue());
            entity.setLabel(WordUtils.capitalize(Entity.find(pivot_ent.getValue()).getLabel()));
            entity.setCount(pivot_ent.getCount());
            entity.setField("entity_uri_str");

            if (!results.containsKey(entity)) {
                List<HADatAcThing> children = new ArrayList<HADatAcThing>();
                results.put(entity, children);
            }

            Facet subFacet = facet.getChildById(entity.getUri());
            subFacet.putFacet("entity_uri_str", entity.getUri());
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

