package org.hadatac.entity.pojo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.HashMap;

import org.apache.commons.text.WordUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;


public class TimeInstance extends HADatAcThing implements Comparable<TimeInstance> {

    public TimeInstance () {}

    @Override
    public boolean equals(Object o) {
        if((o instanceof TimeInstance) && (((TimeInstance)o).getLabel().equals(this.getLabel()))) {
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
        // System.out.println("TimeInstance strQuery: " + strQuery);
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
            System.out.println("[ERROR] TimeInstance.getNumberFromSolr() - Exception message: " + e.getMessage());
        }

        return -1;
    }
    
    @Override
    public Map<Facetable, List<Facetable>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
    	//System.out.println("TimeInstance: INSIDE getTargetFacets");
        return getTargetFacetsFromSolr(facet, facetHandler);
    }

    @Override
    public Map<Facetable, List<Facetable>> getTargetFacetsFromSolr(
            Facet facet, FacetHandler facetHandler) {        
        SolrQuery query = new SolrQuery();
        String queryString = facetHandler.getTempSolrQuery(facet);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date minTime = findMinTime("timestamp_date", queryString);
        //System.out.println("minTime: " + minTime);
        Date maxTime = findMaxTime("timestamp_date", queryString);
        //System.out.println("maxTime: " + maxTime);

        String gap = null;
        String param = "";
        if (minTime != null && maxTime != null) {
            gap = calculateTimeGap(minTime.toInstant(), maxTime.toInstant());
            //System.out.println("gap: " + gap);
            param = "{ "
                    + "named_time_str:{ "
                    + "type: terms, "
                    + "field: named_time_str, "
                    + "limit: 1000}, "
                    + "timestamp_date:{ "
                    + "type: range, "
                    + "field: timestamp_date, "
                    + "start: \"" + sdfDate.format(minTime) + "Z\", "
                    + "end: \"" + sdfDate.format(maxTime) + "Z\", "
                    + "gap: \"" + gap + "\" } "
                    + "}";
        } else {
            param = "{ "
                    + "named_time_str:{ "
                    + "type: terms, "
                    + "field: named_time_str, "
                    + "limit: 1000}}";
        }
        query.setQuery(queryString);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", param);

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            //for (Pivot pvt : pivot.children) {
            //	System.out.println("Value: " + pvt.getValue() + "   Field: " + pvt.getField());
            //}
            return parsePivot(pivot, query.toString());
        } catch (Exception e) {
            System.out.println("[ERROR] TimeInstance.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
        }

        return null;
    }

    public static Date findMinTime(String field, String q) {
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        query.setRows(1);
        query.addSort(field, SolrQuery.ORDER.asc);

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();

            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();

            SolrDocumentList results = queryResponse.getResults();
            if (results.size() == 1) {
                Measurement m = Measurement.convertFromSolr(results.get(0), null, new HashMap<>());
                return m.getTimestamp();
            }
        } catch (IOException e) {
            System.out.println("[ERROR] TimeInstance.findMinTime(String, String) - IOException message: " + e.getMessage());
        } catch (SolrServerException e) {
            System.out.println("[ERROR] TimeInstance.findMinTime(String, String) - SolrServerException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] TimeInstance.findMinTime(String, String) - Exception message: " + e.getMessage());
        }

        return null;
    }

    public static Date findMaxTime(String field, String q) {
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        query.setRows(1);
        query.set(field, "[* TO NOW]");
        query.addSort(field, SolrQuery.ORDER.desc);

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();

            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();

            SolrDocumentList results = queryResponse.getResults();
            if (results.size() == 1) {
                Measurement m = Measurement.convertFromSolr(results.get(0), null, new HashMap<>());
                return m.getTimestamp();
            }
        } catch (IOException e) {
            System.out.println("[ERROR] TimeInstance.findMaxTime(String, String) - IOException message: " + e.getMessage());
        } catch (SolrServerException e) {
            System.out.println("[ERROR] TimeInstance.findMaxTime(String, String) - SolrServerException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] TimeInstance.findMaxTime(String, String) - Exception message: " + e.getMessage());
        }

        return null;
    }

    public static String calculateTimeGap(Instant min, Instant max) {

        if (min == null || max == null) {
            return "";
        }

        Duration duration = Duration.between(min, max);
        //System.out.println("duration in days: " + duration.toDays());

        long days = duration.toDays();
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (years > 2) {
            return "+1YEAR";
        }
        if (months > 3) {
            return "+1MONTH";
        }
        if (weeks > 3) {
            return "+1WEEK";
        }
        if (days > 3) {
            return "+1DAY";
        }

        long hours = duration.toHours();

        if (hours > 4) {
            return "+1HOUR";
        }

        long minutes = duration.toMinutes();

        if (minutes > 4) {
            return "+1MINUTE";
        }

        return "+1MINUTE";
    }

    private Map<Facetable, List<Facetable>> parsePivot(Pivot pivot, String query) {   
        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        
        for (Pivot pivot_ent : pivot.children) {
            if (pivot_ent.getValue().isEmpty()) {
                continue;
            }

            TimeInstance time = new TimeInstance();
            if (pivot_ent.getValue().startsWith("http")) {
                time.setUri(pivot_ent.getValue());
                
                Entity entity = Entity.find(pivot_ent.getValue());
                if (entity != null) {
                    time.setLabel(WordUtils.capitalize(entity.getLabel()));
                }
                
                if (time.getLabel().isEmpty()) {
                    time.setLabel(WordUtils.capitalize(URIUtils.getBaseName(time.getUri())));
                }
            } else {
                time.setUri("");
                time.setLabel(WordUtils.capitalize(pivot_ent.getValue()));
            }
            time.setCount(pivot_ent.getCount());
            time.setQuery(query);
            time.setField(pivot_ent.getField());
            if (!results.containsKey(time)) {
                List<Facetable> attributes = new ArrayList<Facetable>();
                results.put(time, attributes);
            }
        }

        return results;
    }

    @Override
    public int compareTo(TimeInstance another) {
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
