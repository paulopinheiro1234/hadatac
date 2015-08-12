
package models;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class FacetsWithCategories {

    //Maps categories of field facets (e.g "characteristic") to a list of the facets falling in that category (e.g pH)
    public TreeMap<String, TreeMap<String, Boolean>> facets = new TreeMap<String, TreeMap<String, Boolean>>();
    //List of categories
    public Map<String, Boolean> categories = new TreeMap<String, Boolean>();
    //Keep track of all facets regardless of category for use by the query form
    public ArrayList<String> all_facets = new ArrayList<String>();


    public FacetsWithCategories() {}
    
    //TODO update this to create facets Map
    public FacetsWithCategories(Map<String, Boolean> facet_categories, ArrayList<String> all_facets){
        this.categories = new TreeMap<String, Boolean>(facet_categories);
        this.all_facets = new ArrayList<String>(all_facets);
    }

    //Consider adding to map from categories to list of strings
    public boolean addFacet(String category, String facet){
        if (categories.get(category) == null) {
            categories.put(category, true);
            facets.put(category, new TreeMap<String, Boolean>());
        }
        //Note that this doesn't prevent the facet from being overwritten
        facets.get(category).put(facet, false);
        return true;
    }

}
