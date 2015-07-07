package models;

import views.formdata.FacetFormData;

public class Query {
    public String subject;
    public String predicate; 
    public FacetsWithCategories field_facets = new FacetsWithCategories();
    public FacetsWithCategories query_facets = new FacetsWithCategories();
    public FacetsWithCategories pivot_facets = new FacetsWithCategories();
    public FacetsWithCategories range_facets = new FacetsWithCategories();
    public FacetsWithCategories cluster_facets = new FacetsWithCategories();
    
    public Query() {}
    
    public Query(String named_geographic_location, String spatial_predicate,
                         FacetsWithCategories field_facets, FacetsWithCategories query_facets, FacetsWithCategories pivot_facets, FacetsWithCategories range_facets,
                         FacetsWithCategories cluster_facets){
        this.subject = named_geographic_location;
        this.predicate = spatial_predicate;


        for (String category : field_facets.facets.keySet() ) {
            for (String facet : field_facets.facets.get(category).keySet()){
                this.field_facets.addFacet(category, facet);
            }
        }

        for (String category : query_facets.facets.keySet() ) {
            for (String facet : query_facets.facets.get(category).keySet()){
                this.query_facets.addFacet(category, facet);
            }
        }

        for (String category : pivot_facets.facets.keySet() ) {
            for (String facet : pivot_facets.facets.get(category).keySet()){
                this.pivot_facets.addFacet(category, facet);
            }
        }

        for (String category : range_facets.facets.keySet() ) {
            for (String facet : range_facets.facets.get(category).keySet()){
                this.range_facets.addFacet(category, facet);
            }
        }

        for (String category : cluster_facets.facets.keySet() ) {
            for (String facet : cluster_facets.facets.get(category).keySet()){
                this.cluster_facets.addFacet(category, facet);
            }
        }
    }
    
    public static Query makeInstance(FacetFormData formData) {
        Query query = new Query(formData.subject, formData.predicate,
                                formData.field_facets, formData.query_facets, formData.pivot_facets,
                                formData.range_facets, formData.cluster_facets);
        return query;
    }
}
