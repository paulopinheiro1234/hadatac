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
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.utils.CollectionUtil;


public class Category extends HADatAcThing implements Comparable<Category> {

	public Map<String, String> categoryMap;
	
    public Category() {
    	categoryMap = new HashMap<String, String>();
    }

    @Override
    public boolean equals(Object o) {
        if((o instanceof Category) && (((Category)o).getUri().equals(this.getUri()))) {
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

    public Map<String, String> getCategoriesSolrFacetSearch(Facet facet, FacetHandler facetHandler) {

        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        //System.out.println("Inside Category.getTargerFacetsFromSolr(): facet query is " + facet.getFacetName() + "   query is: " + strQuery);;
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ "
                + " categorical_class_uri_str:{ "
                + " type: terms, "
                + " field: categorical_class_uri_str, "
                + " limit: 1000, "
                + " facet: { "
                + "     characteristic_uri_str_multi:{ "
                + "     type: terms, "
                + "     field: characteristic_uri_str_multi, "
                + "     limit: 1000} "
                + "     } "
                + "   } "
                + " }");

        Pivot pivot = null;
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            pivot = Pivot.parseQueryResponse(queryResponse);
            categoryMap.clear();
            traverseCategoriesFacetSearch(pivot, "");
            //System.out.println("Printing category pivot");
            //pivot.print("");
            //for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
            //    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            //}
        } catch (Exception e) {
            System.out.println("[ERROR] Category.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
            return null;
        }

        return categoryMap;

    }

    public Map<String, String> getCategoriesSolr(Facet facet, FacetHandler facetHandler) {

    	SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
    	//System.out.println("Inside Category.getTargerFacetsFromSolr(): facet query is " + facet.getFacetName() + "   query is: " + strQuery);;
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ " 
                + " categorical_class_uri_str:{ " 
                + " type: terms, " 
                + " field: categorical_class_uri_str, " 
                + " limit: 1000, " 
                + " facet: { "
                + "     characteristic_uri_str_multi:{ " 
                + "     type: terms, " 
                + "     field: characteristic_uri_str_multi, " 
                + "     limit: 1000} " 
                + "     } "
                + "   } "
                + " }"); 

        Pivot pivot = null;
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            pivot = Pivot.parseQueryResponse(queryResponse);
            categoryMap.clear();
            traverseCategories(pivot, "");
            //System.out.println("Printing category pivot");
            //pivot.print("");
            //for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
            //    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            //}
        } catch (Exception e) {
            System.out.println("[ERROR] Category.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
            return null;
        }
        
        return categoryMap;
       
    }
   
    public void traverseCategories(Pivot pivot, String category) {
    	//System.out.println("Field: " + pivot.getField() + "   Value: "  + pivot.getValue());
    	if (pivot.getField().equals("categorical_class_uri_str")) {
    		category = pivot.getValue();
    	}
    	if (category != null && !category.isEmpty() && pivot.getField().equals("characteristic_uri_str_multi")) {
            if (pivot.getValue().contains(":")) {
            	categoryMap.put(pivot.getValue(),WordUtils.capitalize(Entity.find(category).getLabel()));
            } else {
            	categoryMap.put(pivot.getValue(),category);
            }    	
        } else if (!pivot.children.isEmpty()) {
            for (Pivot child : pivot.children) {
            	traverseCategories(child, category);
            }
        }
        return;
    }

    public void traverseCategoriesFacetSearch(Pivot pivot, String category) {
        //System.out.println("Field: " + pivot.getField() + "   Value: "  + pivot.getValue());
        if (pivot.getField().equals("categorical_class_uri_str")) {
            category = pivot.getValue();
        }
        if (category != null && !category.isEmpty() && pivot.getField().equals("characteristic_uri_str_multi")) {
            if (pivot.getValue().contains(":")) {
                categoryMap.put(pivot.getValue(),WordUtils.capitalize(Entity.facetSearchFind(category).getLabel()));
            } else {
                categoryMap.put(pivot.getValue(),category);
            }
        } else if (!pivot.children.isEmpty()) {
            for (Pivot child : pivot.children) {
                traverseCategoriesFacetSearch(child, category);
            }
        }
        return;
    }

    @Override
    public int compareTo(Category another) {
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

