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
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;

import com.typesafe.config.ConfigFactory;

public class UnitInstance extends HADatAcThing implements Comparable<UnitInstance> {

    static String className = "uo:0000000";

    public UnitInstance () {}

    @Override
    public boolean equals(Object o) {
        if((o instanceof UnitInstance) && (((UnitInstance)o).getUri().equals(this.getUri()))) {
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

    @Override
    public Map<Facetable, List<Facetable>> getTargetFacetsFromSolr(
            Facet facet, FacetHandler facetHandler) {  
        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ "
                + "unit_uri_str:{ "
                + "type: terms, "
                + "field: unit_uri_str,"
                + "limit: 1000}}");

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            return parsePivot(pivot, query.toString());
        } catch (Exception e) {
            System.out.println("[ERROR] Unit.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
        }

        return null;
    }

    private Map<Facetable, List<Facetable>> parsePivot(Pivot pivot, String query) {
        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        
        for (Pivot pivot_ent : pivot.children) {
            UnitInstance unit = new UnitInstance();
            unit.setUri(pivot_ent.getValue());
            if (URIUtils.isValidURI(pivot_ent.getValue())) {
                Unit unit_temp = Unit.find(pivot_ent.getValue());
                if (unit_temp != null) {
                    unit.setLabel(WordUtils.capitalize(unit_temp.getLabel()));
                } else {
                    unit.setLabel(pivot_ent.getValue());
                }
            } else {
                if (pivot_ent.getValue().isEmpty()) {
                    // Skip empty unit
                    continue;
                } else {
                    unit.setLabel(WordUtils.capitalize(pivot_ent.getValue()));
                }
            }
            unit.setCount(pivot_ent.getCount());
            unit.setQuery(query);
            unit.setField("unit_uri_str");
            if (!results.containsKey(unit)) {
                List<Facetable> attributes = new ArrayList<Facetable>();
                results.put(unit, attributes);
            }
        }

        return results;
    }

    @Override
    public int compareTo(UnitInstance another) {
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

}
