package org.hadatac.console.models;

import java.util.List;
import java.util.Map;

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
