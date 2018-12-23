package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.text.WordUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class EntityRole extends HADatAcThing implements Comparable<EntityRole> {

    static String className = "sio:Object";

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
    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromTripleStore(facet, facetHandler);
    }

    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacetsFromTripleStore(
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
                + "?dasoSub sio:Relation ?relation . \n"
                + "?daso hasco:hasRole ?roleUri . \n"
                + "?dasa hasco:hasEntity ?entityUri . \n"
                + "?dasa hasco:hasAttribute ?attributeUri . \n"
                + "?attributeUri rdfs:label ?attributeLabel . \n"
                + "} \n"
                + "FILTER (?daso != ?dasoSub) \n"
                + "} \n";

        // System.out.println("EntityRole query: " + query);

        Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.size() == 0) {
                System.out.println("resultsrw.size() == 0!");

                EntityRole role = new EntityRole();

                /*
                role.setUri(URIUtils.replacePrefixEx("sio:Sample"));
                role.setLabel("Sample");
                role.setField("entity_role_uri_str");
                 */

                AttributeInstance attrib = new AttributeInstance();				
                if (!results.containsKey(role)) {
                    List<HADatAcThing> facets = new ArrayList<HADatAcThing>();
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

                while (resultsrw.hasNext()) {
                    QuerySolution soln = resultsrw.next();
                    EntityRole role = new EntityRole();
                    if (soln.get("roleUri") != null && !soln.get("roleUri").toString().isEmpty()) {
                        role.setUri(soln.get("roleUri").toString());
                        role.setLabel(WordUtils.capitalize(URIUtils.getBaseName(soln.get("roleUri").toString())));
                        role.setField("entity_role_uri_str");
                    } else {
                        System.out.println("soln.get(\"roleUri\") == null");
                        /*
                        role.setUri(URIUtils.replacePrefixEx("sio:Sample"));
                        role.setLabel("Sample");
                        role.setField("entity_role_uri_str");
                         */
                    }

                    AttributeInstance attrib = new AttributeInstance();
                    attrib.setUri(soln.get("attributeUri").toString());
                    attrib.setLabel(WordUtils.capitalize(soln.get("attributeLabel").toString()));
                    attrib.setField("characteristic_uri_str_multi");

                    if (!results.containsKey(role)) {
                        List<HADatAcThing> facets = new ArrayList<HADatAcThing>();
                        results.put(role, facets);
                    }
                    if (!results.get(role).contains(attrib)) {
                        results.get(role).add(attrib);
                    }

                    Facet subFacet = facet.getChildById(role.getUri());
                    subFacet.putFacet("entity_role_uri_str", role.getUri());
                    if (soln.get("dasoSub") != null) {
                        subFacet.putFacet("daso_uri_str", soln.get("dasoSub").toString());
                    } else {
                        subFacet.putFacet("daso_uri_str", soln.get("daso").toString());
                    }
                    subFacet.putFacet("dasa_uri_str", soln.get("dasa").toString());

                    /*
                    if (!role.getUri().equals(URIUtils.replacePrefixEx("sio:Sample"))) {
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

    public static Map<String, String> findObjRoleMappings(String studyUri) {
        Map<String, String> results = new HashMap<String, String>();        
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() 
                + " SELECT ?studyObj ?label WHERE { \n" 
                + " ?studyObj hasco:isMemberOf ?soc . \n" 
                + " ?soc hasco:hasRoleLabel ?label . \n" 
                + " ?soc hasco:isMemberOf <" + studyUri + "> . \n" 
                + " } \n";
        //    + "SELECT ?studyObj ?roleUri WHERE { \n"
        //    + "{ "
        //    + "?studyObj hasco:isMemberOf ?soc . \n"
        //    + "?soc a hasco:SubjectGroup . \n"
        //    + "?soc hasco:isMemberOf <" + studyUri + "> . \n"
        //    + "?studyObj hasco:hasRole ?roleUri . \n"
        //    + "} UNION { \n"
        //    + "?studyObj hasco:isMemberOf ?soc . \n"
        //    + "?soc a hasco:SampleCollection . \n"
        //    + "?soc hasco:isMemberOf <" + studyUri + "> . \n"
        //    + "?studyObj a ?roleUri . \n"
        //    + "}}";

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

    @Override
    public int saveToLabKey(String userName, String password) {
        return 0;
    }

    @Override
    public int deleteFromLabKey(String userName, String password) {
        return 0;
    }
}

