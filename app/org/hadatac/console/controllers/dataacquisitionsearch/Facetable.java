package org.hadatac.console.controllers.dataacquisitionsearch;

import java.util.List;
import java.util.Map;

import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;

public interface Facetable {
    
    public String getField();
    public String getLabel();
    public String getUri();
    public String getQuery();
    public int getCount();
    
    public Map<Facetable, List<Facetable>> getTargetFacets(
            Facet facet, FacetHandler facetHandler);

    public Map<Facetable, List<Facetable>> getTargetFacetsFromSolr(
            Facet facet, FacetHandler facetHandler);
    
    public Map<Facetable, List<Facetable>> getTargetFacetsFromTripleStore(
            Facet facet, FacetHandler facetHandler);

    public long getNumber(Facet facet, FacetHandler facetHandler);
    
    public long getNumberFromSolr(Facet facet, FacetHandler facetHandler);
    
    public long getNumberFromTripleStore(Facet facet, FacetHandler facetHandler);
}
