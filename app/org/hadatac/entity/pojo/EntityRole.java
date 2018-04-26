package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
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
	public boolean equals(Object o) {;
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
	
	public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
			Facet facet, FacetHandler facetHandler) {
		
		String valueConstraint = "";
		if (!facet.getFacetValuesByField("entity_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?entityUri { " + stringify(
					facet.getFacetValuesByField("entity_uri_str"), true) + " } \n ";
		}
		
		if (!facet.getFacetValuesByField("entity_role_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?roleUri { " + stringify(
					facet.getFacetValuesByField("entity_role_uri_str"), true) + " } \n ";
		}
		
		if (!facet.getFacetValuesByField("characteristic_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?attributeUri { " + stringify(
					facet.getFacetValuesByField("characteristic_uri_str"), true) + " } \n ";
		}
		
		if (!facet.getFacetValuesByField("dasa_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?dasa { " + stringify(
					facet.getFacetValuesByField("dasa_uri_str"), true) + " } \n ";
		}
		
		String query = "";
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += "SELECT ?roleUri ?daso ?dasoSub ?dasa ?relation ?entityUri ?attributeUri ?attributeLabel WHERE { \n"
				+ valueConstraint
				+ "{ "
				+ "?dasa hasco:isAttributeOf ?daso . \n"
				+ "?daso hasco:hasRole ?roleUri . \n"
				+ "?daso sio:Relation ?relation . \n"
				+ "?dasa hasco:hasEntity ?entityUri . \n"
				+ "?dasa hasco:hasAttribute ?attributeUri . \n"
				+ "?attributeUri rdfs:label ?attributeLabel . \n"
				+ "} UNION { \n"
				+ "?dasa hasco:isAttributeOf ?dasoSub . \n"
				+ "?dasoSub sio:inRelationTo ?daso . \n"
				+ "?dasoSub sio:Relation ?relation . \n"
				+ "?daso hasco:hasRole ?roleUri . \n"
				+ "?dasa hasco:hasEntity ?entityUri . \n"
				+ "?dasa hasco:hasAttribute ?attributeUri . \n"
				+ "?attributeUri rdfs:label ?attributeLabel . \n"
				+ "}}";
		
		// System.out.println("EntityRole query: " + query);
		
		Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
		try {
		    ResultSetRewindable resultsrw = SPARQLUtils.select(
	                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), query);
			
			if (resultsrw.size() == 0) {
				EntityRole role = new EntityRole();
				role.setUri(URIUtils.replacePrefixEx("sio:Sample"));
				role.setLabel("Sample");
				role.setField("entity_role_uri_str");
				
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
				facet.clearFieldValues("characteristic_uri_str");
				facet.clearFieldValues("daso_uri_str");
				facet.clearFieldValues("dasa_uri_str");
				
				while (resultsrw.hasNext()) {
					QuerySolution soln = resultsrw.next();
					if (soln.get("roleUri") != null && !soln.get("roleUri").toString().isEmpty()) {
						EntityRole role = new EntityRole();
						role.setUri(soln.get("roleUri").toString());
						role.setLabel(WordUtils.capitalize(soln.get("roleUri").toString().split("#")[1]));
						role.setField("entity_role_uri_str");
						
						AttributeInstance attrib = new AttributeInstance();
						attrib.setUri(soln.get("attributeUri").toString());
						attrib.setLabel(WordUtils.capitalize(soln.get("attributeLabel").toString()));
						attrib.setField("characteristic_uri_str");
						
						if (!results.containsKey(role)) {
							List<HADatAcThing> facets = new ArrayList<HADatAcThing>();
							results.put(role, facets);
						}
						if (!results.get(role).contains(attrib)) {
							results.get(role).add(attrib);
						}
						
						Facet subFacet = facet.getChildById(role.getUri());
						subFacet.putFacet("entity_role_uri_str", role.getUri());
						subFacet.putFacet("entity_uri_str", soln.get("entityUri").toString());
						subFacet.putFacet("daso_uri_str", soln.get("daso").toString());
						subFacet.putFacet("dasa_uri_str", soln.get("dasa").toString());
					}
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
                + "SELECT ?studyObj ?roleUri WHERE { \n"
                + "{ "
                + "?studyObj hasco:isMemberOf ?soc . \n"
                + "?soc a hasco:SubjectGroup . \n"
                + "?soc hasco:isMemberOf <" + studyUri + "> . \n"
                + "?studyObj hasco:hasRole ?roleUri . \n"
                + "} UNION { \n"
                + "?studyObj hasco:isMemberOf ?soc . \n"
                + "?soc a hasco:SampleCollection . \n"
                + "?soc hasco:isMemberOf <" + studyUri + "> . \n"
                + "?studyObj a ?roleUri . \n"
                + "}}";
        
        // System.out.println("findObjRoleMappings query: " + queryString);
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

        try {
            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                results.put(soln.get("studyObj").toString(), soln.get("roleUri").toString());
            }
        } catch (Exception e) {
            System.out.println("EntityRole.findObjRoleMappings() Error: " + e.getMessage());
            e.printStackTrace();
        }

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

