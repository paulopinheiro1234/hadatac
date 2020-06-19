package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.text.WordUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class EntityRole extends HADatAcThing implements Comparable<EntityRole> {

    static String className = "sio:SIO_000776";

    public EntityRole() {}

    @Override
    public boolean equals(Object o) {
        if((o instanceof EntityRole) && (((EntityRole)o).getUri().equals(this.getUri()))) {
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

    public Map<Facetable, List<Facetable>> getTargetFacetsFromTripleStore(
            Facet facet, FacetHandler facetHandler) {

        String valueConstraint = "";
        if (!facet.getFacetValuesByField("entity_uri_str").isEmpty()) {
            valueConstraint += " VALUES ?entityUri { " + stringify(
                    facet.getFacetValuesByField("entity_uri_str")) + " } \n ";
        }

        if (!facet.getFacetValuesByField("entity_role_uri_str").isEmpty()) {
            valueConstraint += " VALUES ?roleUri { " + stringify(
                    facet.getFacetValuesByField("entity_role_uri_str")) + " } \n ";
        }

        if (!facet.getFacetValuesByField("characteristic_uri_str_multi").isEmpty()) {
            valueConstraint += " VALUES ?attributeUri { " + stringify(
                    facet.getFacetValuesByField("characteristic_uri_str_multi")) + " } \n ";
        }

        if (!facet.getFacetValuesByField("dasa_uri_str").isEmpty()) {
            valueConstraint += " VALUES ?dasa { " + stringify(
                    facet.getFacetValuesByField("dasa_uri_str")) + " } \n ";
        }

        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT DISTINCT ?roleUri ?daso ?dasoSub ?dasa ?relation ?entityUri ?attributeUri ?attributeLabel WHERE { \n"
                + valueConstraint
                + "{ \n"
                + "?dasa hasco:isAttributeOf ?daso . \n"
                + "?daso hasco:hasRole ?roleUri . \n"
                + "?dasa hasco:hasEntity ?entityUri . \n"
                + "?dasa hasco:hasAttribute ?attributeUri . \n"
                + "?attributeUri rdfs:label ?attributeLabel . \n"
                + "} UNION { \n"
                + "?dasa hasco:isAttributeOf ?dasoSub . \n"
                + "?dasoSub ?relation ?daso . \n"
                + "?dasoSub hasco:Relation ?relation . \n"
                + "?daso hasco:hasRole ?roleUri . \n"
                + "?dasa hasco:hasEntity ?entityUri . \n"
                + "?dasa hasco:hasAttribute ?attributeUri . \n"
                + "?attributeUri rdfs:label ?attributeLabel . \n"
                + "FILTER (?daso != ?dasoSub) \n"
                + "} \n"
                + "} \n";

        // System.out.println("EntityRole query: \n" + query);

        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.size() == 0) {
                System.out.println("resultsrw.size() == 0!");

                EntityRole role = new EntityRole();

                /*
                role.setUri(URIUtils.replacePrefixEx("sio:SIO_001050"));
                role.setLabel("Sample");
                role.setField("entity_role_uri_str");
                 */

                AttributeInstance attrib = new AttributeInstance();				
                if (!results.containsKey(role)) {
                    List<Facetable> facets = new ArrayList<Facetable>();
                    results.put(role, facets);
                }
                if (!results.get(role).contains(attrib)) {
                    results.get(role).add(attrib);
                }

                facet.getChildById(role.getUri());
            } else {
                facet.clearFieldValues("entity_uri_str");
                facet.clearFieldValues("characteristic_uri_str_multi");
                facet.clearFieldValues("daso_uri_str");
                facet.clearFieldValues("dasa_uri_str");

                boolean hasEmptyDasoSub = false;
                while (resultsrw.hasNext()) {
                    QuerySolution soln = resultsrw.next();
                    if (soln.get("dasoSub") == null) {
                        hasEmptyDasoSub = true;
                    }
                }
                
                boolean allowRoleWithEmptyUri = true;
                resultsrw.reset();
                while (resultsrw.hasNext()) {
                    QuerySolution soln = resultsrw.next();
                    if (soln.get("roleUri") != null && !soln.get("roleUri").toString().isEmpty()) {
                        if ((hasEmptyDasoSub && soln.get("dasoSub") == null) || 
                                (!hasEmptyDasoSub && soln.get("dasoSub") != null)) {
                            allowRoleWithEmptyUri = false;
                        }
                    }
                }
                
                resultsrw.reset();
                while (resultsrw.hasNext()) {
                    QuerySolution soln = resultsrw.next();
                    EntityRole role = new EntityRole();
                    if (soln.get("roleUri") != null && !soln.get("roleUri").toString().isEmpty()) {
                        if ((hasEmptyDasoSub && soln.get("dasoSub") == null) || 
                                (!hasEmptyDasoSub && soln.get("dasoSub") != null)) {
                            role.setUri(soln.get("roleUri").toString());
                            role.setLabel(WordUtils.capitalize(URIUtils.getBaseName(soln.get("roleUri").toString())));
                            role.setField("entity_role_uri_str");
                            role.setQuery(query);
                        }
                    } else {
                        System.out.println("soln.get(\"roleUri\") == null");
                        /*
                        role.setUri(URIUtils.replacePrefixEx("sio:SIO_001050"));
                        role.setLabel("Sample");
                        role.setField("entity_role_uri_str");
                         */
                    }

                    AttributeInstance attrib = new AttributeInstance();
                    attrib.setUri(soln.get("attributeUri").toString());
                    attrib.setLabel(WordUtils.capitalize(soln.get("attributeLabel").toString()));
                    attrib.setField("characteristic_uri_str_multi");

                    if (!results.containsKey(role) && (allowRoleWithEmptyUri || (!allowRoleWithEmptyUri && !role.getUri().isEmpty()))) {
                        List<Facetable> facets = new ArrayList<Facetable>();
                        results.put(role, facets);
                    }
                    
                    if (results.containsKey(role)) {
                        if (!results.get(role).contains(attrib)) {
                            results.get(role).add(attrib);
                        }
                    }

                    Facet subFacet = facet.getChildById(role.getUri());
                    subFacet.putFacet("entity_role_uri_str", role.getUri());
                    if (hasEmptyDasoSub) {
                        if (soln.get("dasoSub") == null) {
                            subFacet.putFacet("daso_uri_str", soln.get("daso").toString());
                        }
                    } else {
                        if (soln.get("dasoSub") != null) {
                            subFacet.putFacet("daso_uri_str", soln.get("dasoSub").toString());
                        }
                    }
                    subFacet.putFacet("dasa_uri_str", soln.get("dasa").toString());

                    /*
                    if (!role.getUri().equals(URIUtils.replacePrefixEx("sio:SIO_001050"))) {
                        subFacet.putFacet("entity_uri_str", soln.get("entityUri").toString());
                    }
                     */
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
    
    public Map<Facetable, List<Facetable>> getTargetFacetsFromSolr(
            Facet facet, FacetHandler facetHandler) {
        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
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
            return parsePivot(pivot, facet);
        } catch (Exception e) {
            System.out.println("[ERROR] EntityRole.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
        }

        return null;
    }
    
    private Map<Facetable, List<Facetable>> parsePivot(Pivot pivot, Facet facet) {
        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        
        for (Pivot child : pivot.children) {
            if (child.getValue().isEmpty()) {
                continue;
            }

            EntityRole role = new EntityRole();
            role.setUri(child.getValue());
            
            role.setLabel(WordUtils.capitalize(child.getValue()));
            role.setCount(child.getCount());
            role.setField("role_str");

            if (!results.containsKey(role)) {
                List<Facetable> children = new ArrayList<Facetable>();
                results.put(role, children);
            }

            Facet subFacet = facet.getChildById(role.getUri());
            subFacet.putFacet("role_str", role.getUri());
        }

        return results;
    }

    public static Map<String, String> findObjRoleMappings(String studyUri) {
        Map<String, String> results = new HashMap<String, String>();        
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
                + " SELECT ?studyObj ?label WHERE { \n" 
                + " ?studyObj hasco:isMemberOf ?soc . \n" 
                + " ?soc hasco:hasRoleLabel ?label . \n" 
                + " ?soc hasco:isMemberOf <" + studyUri + "> . \n" 
                + " } \n";

        // System.out.println("findObjRoleMappings query: " + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        try {
            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                results.put(soln.get("studyObj").toString(), soln.get("label").toString());
            }
        } catch (Exception e) {
            System.out.println("EntityRole.findObjRoleMappings() Error: " + e.getMessage());
            e.printStackTrace();
        }

        // System.out.println("findObjRoleMappings results: " + results);

        return results;
    }

    @Override
    public int compareTo(EntityRole another) {
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

