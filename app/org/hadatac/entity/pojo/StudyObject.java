package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.metadata.loader.URIUtils;

import org.hadatac.annotations.PropertyField;
import org.hadatac.annotations.PropertyValueType;

public class StudyObject extends HADatAcThing {

    public static String LOCATION = "http://semanticscience.org/resource/SIO_000317";
    public static String TIME = "http://semanticscience.org/resource/SIO_000417";

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:StudyObject;  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "OBJ-";
    
    // Within mapIdStudyObjects
    //    0 -> studyObject URI
    public static final String STUDY_OBJECT_URI = "STUDY_OBJECT_URI";
    //    1 -> originalID of object in the object’s scope (if any)
    public static final String SUBJECT_ID = "SUBJECT_ID";
    //    2 -> URI of object in the object’s scope (if any)
    public static final String SCOPE_OBJECT_URI = "SCOPE_OBJECT_URI";
    public static final String SCOPE_OBJECT_SOC_URI = "SCOPE_OBJECT_SOC_URI";
    //    3 -> studyObjectType
    public static final String STUDY_OBJECT_TYPE = "STUDY_OBJECT_TYPE";
    //    4 -> soc's type
    public static final String SOC_TYPE = "SOC_TYPE";
    //         soc's label
    public static final String SOC_LABEL = "SOC_LABEL";
    public static final String SOC_URI = "SOC_URI";
    public static final String OBJECT_ORIGINAL_ID = "OBJECT_ORIGINAL_ID";
    public static final String OBJECT_TIME = "OBJECT_TIME";
 
    @PropertyField(uri="hasco:originalID")
    String originalId;
    
    @PropertyField(uri="hasco:isMemberOf", valueType=PropertyValueType.URI)
    String isMemberOf;
    
    @PropertyField(uri="hasco:hasRole", valueType=PropertyValueType.URI)
    String roleUri = "";
    
    @PropertyField(uri="hasco:hasObjectScope", valueType=PropertyValueType.URI)
    List<String> scopeUris = new ArrayList<String>();
    
    @PropertyField(uri="hasco:hasTimeObjectScope", valueType=PropertyValueType.URI)
    List<String> timeScopeUris = new ArrayList<String>();
    
    @PropertyField(uri="hasco:hasSpaceObjectScope", valueType=PropertyValueType.URI)
    List<String> spaceScopeUris = new ArrayList<String>();

    public StudyObject() {
        this("", "");
    }

    public StudyObject(String uri, String isMemberOf) {
        setUri(uri);
        setTypeUri("");
        setOriginalId("");
        setLabel("");
        setIsMemberOf(isMemberOf);
        setComment("");
    }

    public StudyObject(String uri,
            String typeUri,
            String originalId,
            String label,
            String isMemberOf,
            String comment,
            List<String> scopeUris,
            List<String> timeScopeUris,
            List<String> spaceScopeUris) {
        setUri(uri);
        setTypeUri(typeUri);
        setOriginalId(originalId);
        setLabel(label);
        setIsMemberOf(isMemberOf);
        setComment(comment);
        setScopeUris(scopeUris);
        setTimeScopeUris(timeScopeUris);
        setSpaceScopeUris(spaceScopeUris);
    }

    public StudyObject(String uri,
            String typeUri,
            String originalId,
            String label,
            String isMemberOf,
            String comment) { 
        setUri(uri);
        setTypeUri(typeUri);
        setOriginalId(originalId);
        setLabel(label);
        setIsMemberOf(isMemberOf);
        setComment(comment);
    }

    public StudyObjectType getStudyObjectType() {
        if (typeUri == null || typeUri.equals("")) {
            return null;
        }
        return StudyObjectType.find(typeUri);
    }

    public boolean isLocation() {
        if (typeUri == null || typeUri.equals("")) {
            return false;
        }
        return (typeUri.equals(LOCATION));
    }

    public boolean isTime() {
        if (typeUri == null || typeUri.equals("")) {
            return false;
        }
        return (typeUri.equals(TIME));
    }

    public String getRoleUri() {
        return roleUri;
    }

    public void setRoleUri(String roleUri) {
        this.roleUri = roleUri;
    }

    public String getOriginalId() {
        return originalId;
    }

    public String getOriginalIdLabel() {
    	if (originalId != null && !originalId.isEmpty()) {
    		return originalId;
    	}
    	return uri;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public String getIsMemberOf() {
        return isMemberOf;
    }

    public void setIsMemberOf(String isMemberOf) {
        this.isMemberOf = isMemberOf;
    }	

    public List<String> getScopeUris() {
        return scopeUris;
    }

    public void setScopeUris(List<String> scopeUris) {
        this.scopeUris = scopeUris;
    }

    public void addScopeUri(String scopeUri) {
        this.scopeUris.add(scopeUri);
    }

    public List<String> getTimeScopeUris() {
        return timeScopeUris;
    }

    public void setTimeScopeUris(List<String> timeScopeUris) {
        this.timeScopeUris = timeScopeUris;
    }

    public void addTimeScopeUri(String timeScopeUri) {
        this.timeScopeUris.add(timeScopeUri);
    }

    public List<String> getSpaceScopeUris() {
        return spaceScopeUris;
    }

    public void setSpaceScopeUris(List<String> spaceScopeUris) {
        this.spaceScopeUris = spaceScopeUris;
    }

    public void addSpaceScopeUri(String spaceScopeUri) {
        this.spaceScopeUris.add(spaceScopeUri);
    }

    public String getGroupId() {
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?id WHERE { \n" + 
        		 " ?grpUri hasco:ofSOC ?soc . \n" + 
        		 " ?grpUri hasco:hasGroupId ?id . \n" + 
        		 " <" + uri + "> hasco:isMemberOf ?soc . " + 
        		 " <" + uri + "> hasco:isGroupMember ?grpUri . " + 
        		 "}";
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                return soln.getLiteral("id").getString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    	
    }
    
    public static int getNumberStudyObjects() {
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += " select (count(?obj) as ?tot) where " + 
                " { ?obj hasco:isMemberOf ?collection . ?obj a ?objType . " + 
                " FILTER NOT EXISTS { ?objType rdfs:subClassOf* hasco:ObjectCollection . } " + 
                "}";
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                return Integer.parseInt(soln.getLiteral("tot").getString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getNumberStudyObjectsByCollection(String oc_uri) {
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += " select (count(?obj) as ?tot) where " + 
                " { ?obj hasco:isMemberOf <" + oc_uri + "> . ?obj a ?objType . " + 
                " FILTER NOT EXISTS { ?objType rdfs:subClassOf* hasco:ObjectCollection . } " + 
                "}";
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                return Integer.parseInt(soln.getLiteral("tot").getString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static List<String> retrieveScopeUris(String obj_uri) {
        List<String> retrievedUris = new ArrayList<String>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?scopeUri WHERE { " + 
                " <" + obj_uri + "> hasco:hasObjectScope ?scopeUri . " + 
                "}";

        //System.out.println("Study.retrieveScopeUris() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            return retrievedUris;
        }
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                try {
                    if (soln.getResource("scopeUri") != null && soln.getResource("scopeUri").getURI() != null) {
                        retrievedUris.add(soln.getResource("scopeUri").getURI());
                    }
                } catch (Exception e1) {
                }
            }
        }
        return retrievedUris;
    }

    public static List<String> retrieveUrisScopedByThisUri(String obj_uri) {
        List<String> retrievedUris = new ArrayList<String>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?scopeUri WHERE { " + 
                " ?scopeUri hasco:hasObjectScope <" + obj_uri + "> . " + 
                "}";

        //System.out.println("Study.retrieveScopeUris() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            return retrievedUris;
        }
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                try {
                    if (soln.getResource("scopeUri") != null && soln.getResource("scopeUri").getURI() != null) {
                        retrievedUris.add(soln.getResource("scopeUri").getURI());
                    }
                } catch (Exception e1) {
                }
            }
        }
        return retrievedUris;
    }

    public static List<String> retrieveTimeScopeUris(String obj_uri) {
        List<String> retrievedUris = new ArrayList<String>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?timeScopeUri WHERE { " + 
                " <" + obj_uri + "> hasco:hasTimeObjectScope ?timeScopeUri . " + 
                "}";

        //System.out.println("Study.retrieveTimeScopeUris() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            return retrievedUris;
        }
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                try {
                    if (soln.getResource("timeScopeUri") != null && soln.getResource("timeScopeUri").getURI() != null) {
                        retrievedUris.add(soln.getResource("timeScopeUri").getURI());
                    }
                } catch (Exception e1) {
                }
            }
        }
        return retrievedUris;
    }

    public static List<String> retrieveTimeScopeTypeUris(String obj_uri) {
        List<String> retrievedUris = new ArrayList<String>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT DISTINCT ?timeScopeUri ?timeScopeTypeUri WHERE { " + 
                " <" + obj_uri + "> hasco:hasTimeObjectScope ?timeScopeUri . " + 
                " ?timeScopeUri a ?timeScopeTypeUri . " +
                "}";

        //System.out.println("Study.retrieveTimeScopeUris() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            return retrievedUris;
        }
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                try {
                    if (soln.getResource("timeScopeTypeUri") != null && soln.getResource("timeScopeTypeUri").getURI() != null) {
                        retrievedUris.add(soln.getResource("timeScopeTypeUri").getURI());
                    }
                } catch (Exception e1) {
                }
            }
        }
        return retrievedUris;
    }

    public static List<String> retrieveSpaceScopeUris(String obj_uri) {
        List<String> retrievedUris = new ArrayList<String>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?spaceScopeUri WHERE { " + 
                " <" + obj_uri + "> hasco:hasSpaceObjectScope ?spaceScopeUri . " + 
                "}";

        //System.out.println("Study.retrieveSpaceScopeUris() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            return retrievedUris;
        }
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                try {
                    if (soln.getResource("spaceScopeUri") != null && soln.getResource("spaceScopeUri").getURI() != null) {
                        retrievedUris.add(soln.getResource("spaceScopeUri").getURI());
                    }
                } catch (Exception e1) {
                }
            }
        }
        return retrievedUris;
    }

    public static StudyObject find(String obj_uri) {
        StudyObject obj = null;
        if (obj_uri == null || obj_uri.trim().equals("")) {
            return obj;
        }
        obj_uri = obj_uri.trim();

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?objType ?originalId ?isMemberOf ?hasLabel ?hasComment WHERE { \n" + 
                "    <" + obj_uri + "> a ?objType . \n" + 
                "    <" + obj_uri + "> hasco:isMemberOf ?isMemberOf . \n" + 
                "    OPTIONAL { <" + obj_uri + "> hasco:originalID ?originalId } . \n" + 
                "    OPTIONAL { <" + obj_uri + "> rdfs:label ?hasLabel } . \n" + 
                "    OPTIONAL { <" + obj_uri + "> rdfs:comment ?hasComment } . \n" + 
                "}";

        //System.out.println("StudyObject find() queryString:\n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            //System.out.println("[WARNING] StudyObject. Could not find OBJ with URI: <" + obj_uri + ">");
            return obj;
        }

        String typeStr = "";
        String originalIdStr = "";
        String isMemberOfStr = "";
        String commentStr = "";

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {

                try {
                    if (soln.getResource("objType") != null && soln.getResource("objType").getURI() != null) {
                        typeStr = soln.getResource("objType").getURI();
                    }
                } catch (Exception e1) {
                    typeStr = "";
                }

                try {
                    if (soln.getLiteral("originalId") != null && soln.getLiteral("originalId").getString() != null) {
                        originalIdStr = soln.getLiteral("originalId").getString();
                    }
                } catch (Exception e1) {
                    originalIdStr = "";
                }

                try {
                    if (soln.getResource("isMemberOf") != null && soln.getResource("isMemberOf").getURI() != null) {
                        isMemberOfStr = soln.getResource("isMemberOf").getURI();
                    }
                } catch (Exception e1) {
                    isMemberOfStr = "";
                }

                try {
                    if (soln.getLiteral("hasComment") != null && soln.getLiteral("hasComment").getString() != null) {
                        commentStr = soln.getLiteral("hasComment").getString();
                    }
                } catch (Exception e1) {
                    commentStr = "";
                }

                obj = new StudyObject(obj_uri,
                        typeStr,
                        originalIdStr,
                        FirstLabel.getLabel(obj_uri),
                        isMemberOfStr,
                        commentStr,
                        retrieveScopeUris(obj_uri),
                        retrieveTimeScopeUris(obj_uri),
                        retrieveSpaceScopeUris(obj_uri));
            }
        }

        return obj;
    }
    
    public static Map<String, String> buildCachedUriByOriginalId() {
        Map<String, String> cache = new HashMap<String, String>();
        
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT DISTINCT ?objUri ?originalId WHERE { \n" + 
                " ?objUri hasco:originalID ?originalId . \n" + 
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                if (soln.get("objUri") != null && soln.get("originalId") != null) {
                    cache.put(soln.get("originalId").toString(), soln.get("objUri").toString());
                }
            }
        }
        
        //System.out.println("buildCachedUriByOriginalId: " + cache.size());

        return cache;
    }

    public static String findUribyOriginalId(String original_id) {
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?objuri WHERE { " + 
                "	?objuri hasco:originalID \"" + original_id + "\" . " + 
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (resultsrw.size() >= 1) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                if (soln.getResource("objuri") != null) {
                    return soln.getResource("objuri").toString();
                }
            }
        } else {
            System.out.println("[WARNING] StudyObject. Could not find OBJ URI for: " + original_id);
            return "";
        }

        return "";
    }
    
    public static Map<String, String> buildCachedObjectBySocAndOriginalId() {
        Map<String, String> cache = new HashMap<String, String>();
        
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT DISTINCT ?objUri ?id ?socUri WHERE { " + 
                "   ?objUri hasco:originalID ?id . " + 
                "   ?objUri hasco:isMemberOf ?socUri . " +                  
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                if (soln.get("objUri") != null 
                        && soln.get("socUri") != null 
                        && soln.get("id") != null) {
                    String key = soln.get("socUri").toString() + ":" + soln.get("id").toString();
                    cache.put(key, soln.get("objUri").toString());
                    //System.out.println("buildCachedObjectBySocAndOriginalId: adding key : [" + key + "]");
                }
            }
        }
        
        //System.out.println("buildCachedObjectBySocAndOriginalId: " + cache.size());

        return cache;
    }

    public static String findUriBySocAndOriginalId(String socUri, String original_id) {
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?objuri WHERE { " + 
                "	?objuri hasco:originalID ?id . " + 
                "	?objuri hasco:isMemberOf <" + socUri + "> . " + 
      		    "   filter contains(?id,\"" + original_id + "\") " +                   
                "}";

        //System.out.println("StudyObject: findUriBySocAndOriginalId => SOC=[" + socUri + "]  originalId: [" + original_id + "]");
        //System.out.println("StudyObject: findUriBySocAndOriginalId => query=[" + queryString + "]");
        //System.out.println("StudyObject: findUriBySocAndOriginalId => CollectionUtil=[" + CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL) + "]");

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        //System.out.println("StudyObject: findUriBySocAndOriginalId => resultSize=[" + resultsrw.size() + "]");
        if (resultsrw.size() >= 1) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                if (soln != null && soln.getResource("objuri") != null) {
                    //System.out.println("StudyObject: findUriBySocAndOriginalId => objuri=[" + soln.getResource("objuri").toString() + "]");
                    return soln.getResource("objuri").toString();
                }
            }
        } else {
            System.out.println("[WARNING] StudyObject. Could not find OBJ URI for  SOCURI=[" + socUri + "] and original ID =[" + original_id+ "]");
            return "";
        }

        return "";
    }
    
    public static Map<String, String> buildCachedObjectBySocAndScopeUri() {
        Map<String, String> cache = new HashMap<String, String>();
        
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT DISTINCT ?objUri ?scopeUri ?socUri WHERE { \n" + 
                " ?objUri hasco:hasObjectScope ?scopeUri . \n" + 
                " ?objUri hasco:isMemberOf ?socUri . \n" +
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                if (soln.get("objUri") != null 
                        && soln.get("socUri") != null 
                        && soln.get("scopeUri") != null) {
                    String key = soln.get("socUri").toString() + ":" + soln.get("scopeUri").toString();
                    cache.put(key, soln.get("objUri").toString());
                    //System.out.println("buildCachedUriBySocAndScopeUri: [" + key + "][" + soln.get("objUri") + "]");
                }
            }
        }
        
        //System.out.println("buildCachedUriBySocAndScopeUri: " + cache.size());

        return cache;
    }

    public static String findUriBySocAndScopeUri(String socUri, String scopeUri) {
    	//System.out.println("StudyObject: findUriBySocAndScopeUri: SOCURI=[" + socUri + "]  SCOPEURI=[" + scopeUri + "]");
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?objuri WHERE { " + 
                "      VALUES ?scopeuri { <" + scopeUri + "> } . " + 
                "      ?objuri hasco:hasObjectScope ?scopeuri .  " + 
                "      ?objuri hasco:isMemberOf <" + socUri + "> . " +
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (resultsrw.size() >= 1) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                if (soln.getResource("objuri") != null) {
                    return soln.getResource("objuri").toString();
                }
            }
        } else {
            System.out.println("[WARNING] StudyObject. Could not find OBJ URI for SOCURI=[" + socUri + "] and Scope URI=[" + scopeUri + "]");
            return "";
        }

        return "";
    }
    
    public static Map<String, String> buildCachedScopeBySocAndObjectUri() {
        Map<String, String> cache = new HashMap<String, String>();
        
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT DISTINCT ?scopeUri ?socUri ?objUri WHERE { \n" + 
                "  ?objUri hasco:hasObjectScope ?scopeUri . \n" + 
                "  ?scopeUri hasco:isMemberOf ?socUri . \n" +
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                if (soln.get("scopeUri") != null 
                        && soln.get("socUri") != null 
                        && soln.get("objUri") != null) {
                    String key = soln.get("socUri").toString() + ":" + soln.get("objUri").toString();
                    cache.put(key, soln.get("scopeUri").toString());
                    //System.out.println("buildCachedScopeBySocAndObjectUri: [" + key + "][" + soln.get("scopeUri") + "]");
                }
            }
        }
        
        //System.out.println("buildCachedScopeBySocAndObjectUri: " + cache.size());

        return cache;
    }

    /*
     *    this query traverses the grounding path backwards because the isMemberOf is of the scopeUri 
     *    rather than the isMemberOf of the objUri
     */
    public static String findScopeBySocAndObjectUri(String socUri, String objUri) {
    	//System.out.println("StudyObject: findScopeBySocAndObjectUri: SOCURI=[" + socUri + "]  OBJURI=[" + objUri + "]");
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?scopeUri WHERE { " + 
                "      VALUES ?objUri { <" + objUri + "> } . " + 
                "      ?objUri hasco:hasObjectScope ?scopeUri .  " + 
                "      ?scopeUri hasco:isMemberOf <" + socUri + "> . " +
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (resultsrw.size() >= 1) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {
                if (soln.getResource("scopeUri") != null) {
                    return soln.getResource("scopeUri").toString();
                }
            }
        } else {
            System.out.println("[WARNING] StudyObject. Could not find OBJ URI for SOCURI=[" + socUri + "] and Object URI=[" + objUri + "]");
            return "";
        }

        return "";
    }

    public static List<StudyObject> findByCollection(ObjectCollection oc) {
        if (oc == null) {
            return null;
        }
        List<StudyObject> objects = new ArrayList<StudyObject>();

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri WHERE { " + 
                "   ?uri hasco:isMemberOf  <" + oc.getUri() + "> . " +
                " } ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) {
                StudyObject object = StudyObject.find(soln.getResource("uri").getURI());
                objects.add(object);
            }
        }
        return objects;
    }

    public static List<StudyObject> findByCollectionWithPages(ObjectCollection oc, int pageSize, int offset) {
        if (oc == null) {
            return null;
        }
        List<StudyObject> objects = new ArrayList<StudyObject>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
        		"SELECT ?uri WHERE { " + 
                "   ?uri hasco:isMemberOf  <" + oc.getUri() + "> . " +
                " } " + 
                " LIMIT " + pageSize + 
                " OFFSET " + offset;

        /* 
        "SELECT ?uri ?id WHERE { " + 
        "   ?uri hasco:isMemberOf  <" + oc.getUri() + "> . " +
        "   ?uri hasco:originalID  ?id . " +
        " } ORDER BY ASC (?id)" + 
        " LIMIT " + pageSize + 
        " OFFSET " + offset;
        */

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) {
                StudyObject object = StudyObject.find(soln.getResource("uri").getURI());
                objects.add(object);
            }
        }
        return objects;
    }

    public static String findByCollectionJSON(ObjectCollection oc) {
        if (oc == null) {
            return null;
        }
        
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri ?label WHERE { " + 
                "   ?uri hasco:isMemberOf  <" + oc.getUri() + "> . " +
                "   OPTIONAL { ?uri rdfs:label ?label } . " +
                " } ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        ResultSetFormatter.outputAsJSON(outputStream, resultsrw);

        try {
            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static Map<String, Map<String, String>> findIdUriMappings(String studyUri) {
        //System.out.println("findIdUriMappings is called!");

        Map<String, Map<String, String>> mapIdUriMappings = new HashMap<String, Map<String, String>>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?studyObject ?studyObjectType ?id ?obj ?subj_id ?soc ?socType WHERE { \n"
                + " { \n"
                + "     ?studyObject hasco:originalID ?id . \n"
                + "     ?studyObject rdf:type ?studyObjectType . \n"
                + "     ?studyObject hasco:isMemberOf ?soc . \n"
                + "     ?soc rdf:type ?socType . \n"
                + "     ?soc a hasco:SubjectGroup . \n"
                + "     ?soc hasco:isMemberOf* <" + studyUri + "> . \n"
                + " } UNION { \n"
                + "     ?studyObject hasco:originalID ?id . \n"
                + "     ?studyObject rdf:type ?studyObjectType . \n"
                + "     ?studyObject hasco:isMemberOf ?soc . \n"
                + "     ?soc rdf:type ?socType . \n"
                + "     ?soc a hasco:SampleCollection . \n"
                + "     ?soc hasco:isMemberOf* <" + studyUri + "> . \n"
                + "     ?studyObject hasco:hasObjectScope ?obj . \n"
                + "     ?obj hasco:originalID ?subj_id . \n"
                + " } \n"
                + " } \n";

        //System.out.println("findIdUriMappings() query: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        try {
            while (resultsrw.hasNext()) {           
                QuerySolution soln = resultsrw.next();
                Map<String, String> details = new HashMap<String, String>();
                if (soln.get("studyObject") != null) {
                    details.put(STUDY_OBJECT_URI, soln.get("studyObject").toString());
                } else {
                    details.put(STUDY_OBJECT_URI, "");
                }
                if (soln.get("subj_id") != null) {
                    details.put(SUBJECT_ID, soln.get("subj_id").toString());
                } else {
                    details.put(SUBJECT_ID, "");
                }
                if (soln.get("obj") != null) {
                    details.put(SCOPE_OBJECT_URI, soln.get("obj").toString());
                } else {
                    details.put(SCOPE_OBJECT_URI, "");
                }
                if (soln.get("studyObjectType") != null) {
                    details.put(STUDY_OBJECT_TYPE, soln.get("studyObjectType").toString());
                } else {
                    details.put(STUDY_OBJECT_TYPE, "");
                }
                if (soln.get("socType") != null) {
                    details.put(SOC_TYPE, soln.get("socType").toString());
                } else {
                    details.put(SOC_TYPE, "");
                }
                if (soln.get("soc") != null) {
                    details.put(SOC_URI, soln.get("soc").toString());
                } else {
                    details.put(SOC_URI, "");
                }
                mapIdUriMappings.put(soln.get("id").toString(), details);
            }
        } catch (Exception e) {
            System.out.println("Error in findIdUriMappings(): " + e.getMessage());
        }

        //System.out.println("mapIdUriMappings: " + mapIdUriMappings.keySet().size());
        
        return mapIdUriMappings;
    }
    
    @Override
    public long getNumber(Facet facet, FacetHandler facetHandler) {
        return getNumberFromSolr(facet, facetHandler);
    }

    @Override
    public long getNumberFromSolr(Facet facet, FacetHandler facetHandler) {
        //System.out.println("\nStudyObject facet: " + facet.toSolrQuery());

        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
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
            System.out.println("[ERROR] StudyObject.getNumberFromSolr() - Exception message: " + e.getMessage());
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

        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ "
                + "study_object_uri_str:{ "
                + "type: terms, "
                + "field: study_object_uri_str, "
                + "limit: 10000000}}");

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);            
            return parsePivot(pivot, facet);
        } catch (Exception e) {
            System.out.println("[ERROR] StudyObject.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
        }

        return null;
    }

    private Map<Facetable, List<Facetable>> parsePivot(Pivot pivot, Facet facet) {
        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        
        for (Pivot child : pivot.children) {
            StudyObject studyObject = new StudyObject();
            studyObject.setUri(child.getValue());
            studyObject.setLabel(child.getValue());
            studyObject.setCount(child.getCount());
            studyObject.setField("study_object_uri_str");

            if (!results.containsKey(studyObject)) {
                List<Facetable> children = new ArrayList<Facetable>();
                results.put(studyObject, children);
            }
        }

        return results;
    }

    @Override
    public void deleteFromTripleStore() {
        super.deleteFromTripleStore();
    }

    /*
    public void deleteFromStudy(String study_uri) {
        String query = "";

        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += " DELETE WHERE { \n";
        if (study_uri.startsWith("http")) {
            query += "<" + this.getUri() + ">";
        } else {
            query += this.getUri();
        }
        query += " ?p ?o . \n";
        query += " } ";

        UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
        processor.execute();

        //System.out.println("Deleted <" + getUri() + "> from triple store");

    }
    */
    
    @Override
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }
}
