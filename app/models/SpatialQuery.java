package models;

import views.formdata.FacetFormData;

public class SpatialQuery extends Query {

    public String named_geographic_location;
    public String spatial_predicate; 

    //These are just lists of strings
    //because when the query gets submitted the "category" (e.g "characteristic")
    //they fall into is irrelevant
    /*
    public ArrayList<String> field_facets; 
    public ArrayList<String> query_facets;
    public ArrayList<String> pivot_facets;
    public ArrayList<String> range_facets;
    public ArrayList<String> cluster_facets;
    */
    public FacetsWithCategories field_facets = new FacetsWithCategories();
    public FacetsWithCategories query_facets = new FacetsWithCategories();
    public FacetsWithCategories pivot_facets = new FacetsWithCategories();
    public FacetsWithCategories range_facets = new FacetsWithCategories();
    public FacetsWithCategories cluster_facets = new FacetsWithCategories();

    public SpatialQuery() {}

    /*
    public Query(String named_geographic_location, String spatial_predicate,
                         FacetsWithCategories field_facets, FacetsWithCategories query_facets, FacetsWithCategories pivot_facets, FacetsWithCategories range_facets,
                         FacetsWithCategories cluster_facets){
        
        this.named_geographic_location = named_geographic_location;
        this.spatial_predicate = spatial_predicate;

        for (String subfacet : field_facets.all_facets) {
            this.field_facets.add(subfacet);
        } 

        for (String subfacet : query_facets.all_facets) {
            this.query_facets.add(subfacet);
        } 

        for (String subfacet : pivot_facets.all_facets) {
            this.pivot_facets.add(subfacet);
        } 

        for (String subfacet : range_facets.all_facets) {
            this.range_facets.add(subfacet);
        } 

        for (String subfacet : cluster_facets.all_facets) {
            this.cluster_facets.add(subfacet);
        } 
        
    }
    */

    
    public SpatialQuery(String named_geographic_location, String spatial_predicate,
                         FacetsWithCategories field_facets, FacetsWithCategories query_facets, FacetsWithCategories pivot_facets, FacetsWithCategories range_facets,
                         FacetsWithCategories cluster_facets){
        this.named_geographic_location = named_geographic_location;
        this.spatial_predicate = spatial_predicate;


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
    /*
    public Query(String named_geographic_location, String spatial_predicate,
                 ArrayList<String> field_facets, ArrayList<String> query_facets, ArrayList<String> pivot_facets, ArrayList<String> range_facets,
                 ArrayList<String> cluster_facets){
        this.named_geographic_location = named_geographic_location;
        this.spatial_predicate = spatial_predicate;
        this.field_facets = field_facets;
        this.query_facets = query_facets;
        this.pivot_facets = pivot_facets;
        this.range_facets = range_facets;
        this.cluster_facets = cluster_facets;
    }
    */

    public static SpatialQuery makeInstance(FacetFormData formData) {
        SpatialQuery query = new SpatialQuery(formData.subject, formData.predicate,
                                formData.field_facets, formData.query_facets, formData.pivot_facets,
                                formData.range_facets, formData.cluster_facets);
        return query;
    }
}
