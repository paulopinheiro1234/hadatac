package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.utils.CollectionUtil;

public class StudyObjectRole extends HADatAcThing implements Comparable<StudyObjectRole> {

	static String className = "sio:SIO_000776";

	public StudyObjectRole() {}
	
	@Override
	public boolean equals(Object o) {;
		if((o instanceof StudyObjectRole) && (((StudyObjectRole)o).getUri().equals(this.getUri()))) {
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
        
        // System.out.println("StudyObjectRole getNumberFromSolr: " + strQuery);
        
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
            System.out.println("[ERROR] StudyObjectRole.getNumberFromSolr() - Exception message: " + e.getMessage());
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
	    //System.out.println("getTargetFacetsFromSolr() is called");
	    
        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        
        // System.out.println("StudyObjectRole getTargetFacetsFromSolr: " + strQuery);
        
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ "
                + "role_str:{ "
                + "type: terms, "
                + "field: role_str, "
                + "limit: 1000}}");

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            return parsePivot(pivot, facet, query.toString());
        } catch (Exception e) {
            System.out.println("[ERROR] StudyObjectRole.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
        }

        return null;
    }

    private Map<Facetable, List<Facetable>> parsePivot(Pivot pivot, Facet facet, String query) {
        facet.clearFieldValues("role_uri_str");
        
        // System.out.println("StudyObjectRole: Query is [" + query + "]");

        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        for (Pivot pivot_ent : pivot.children) {
            StudyObjectRole role = new StudyObjectRole();
            role.setUri(pivot_ent.getValue());
            //role.setLabel(WordUtils.capitalize(Entity.find(pivot_ent.getValue()).getLabel()));
            //Comment from PP: this is a temporary hack since role_uri has changed to be the label itself
            role.setLabel(pivot_ent.getValue());
            role.setCount(pivot_ent.getCount());
            role.setQuery(query);
            role.setField("role_str");
            
            // Ignore blank roles
            if (role.getUri().isEmpty()) {
                continue;
            }

            if (!results.containsKey(role)) {
                List<Facetable> children = new ArrayList<Facetable>();
                results.put(role, children);
            }

            Facet subFacet = facet.getChildById(role.getUri());
            subFacet.putFacet("role_str", role.getUri());
        }

        return results;
    }

	@Override
	public int compareTo(StudyObjectRole another) {
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

