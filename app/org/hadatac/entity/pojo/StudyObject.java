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
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
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
import org.hadatac.console.models.Pivot;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.labkey.remoteapi.CommandException;

import com.typesafe.config.ConfigFactory;

public class StudyObject extends HADatAcThing {

    public static String LOCATION = "http://semanticscience.org/resource/Location";
    public static String TIME = "http://semanticscience.org/resource/TimeInterval";

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:StudyObject;  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "OBJ-";

    String originalId;
    String isMemberOf;
    String roleUri = "";
    List<String> scopeUris = new ArrayList<String>();

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
            List<String> scopeUris) {
        setUri(uri);
        setTypeUri(typeUri);
        setOriginalId(originalId);
        setLabel(label);
        setIsMemberOf(isMemberOf);
        setComment(comment);
        setScopeUris(scopeUris);
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

    public static List<String> retrieveScopeUris(String obj_uri) {
        List<String> retrievedUris = new ArrayList<String>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?scopeUri WHERE { " + 
                " <" + obj_uri + "> hasco:hasObjectScope ?scopeUri . " + 
                "}";

        //System.out.println("Study.retrieveScopeUris() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);
        
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
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            System.out.println("[WARNING] StudyObject. Could not find OBJ with URI: <" + obj_uri + ">");
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
                        retrieveScopeUris(obj_uri));
            }
        }

        return obj;
    }

    public static String findUribyOriginalId(String original_id) {
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?objuri WHERE { " + 
                "	?objuri hasco:originalID \"" + original_id + "\" . " + 
                "}";
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

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
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);
        
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
                "SELECT ?uri ?id WHERE { " + 
                "   ?uri hasco:isMemberOf  <" + oc.getUri() + "> . " +
                "   ?uri hasco:originalID  ?id . " +
                " } ORDER BY ASC (?id)" + 
                " LIMIT " + pageSize + 
                " OFFSET " + offset;
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);
        
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
        Query query = QueryFactory.create(queryString);

        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), query);
        ResultSet results = qexec.execSelect();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        qexec.close();

        try {
            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public long getNumberFromSolr(Facet facet, FacetHandler facetHandler) {
        System.out.println("\nStudyObject facet: " + facet.toSolrQuery());

        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(false);

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + CollectionUtil.DATA_ACQUISITION).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            SolrDocumentList results = queryResponse.getResults();
            return results.getNumFound();
        } catch (Exception e) {
            System.out.println("[ERROR] StudyObject.getNumberFromSolr() - Exception message: " + e.getMessage());
        }

        return -1;
    }

    public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
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
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + CollectionUtil.DATA_ACQUISITION).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);            
            return parsePivot(pivot, facet);
        } catch (Exception e) {
            System.out.println("[ERROR] StudyObject.getTargetFacets() - Exception message: " + e.getMessage());
        }

        return null;
    }

    private Map<HADatAcThing, List<HADatAcThing>> parsePivot(Pivot pivot, Facet facet) {
        Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
        for (Pivot child : pivot.children) {
            StudyObject studyObject = new StudyObject();
            studyObject.setUri(child.getValue());
            studyObject.setLabel(child.getValue());
            studyObject.setCount(child.getCount());
            studyObject.setField("study_object_uri_str");

            if (!results.containsKey(studyObject)) {
                List<HADatAcThing> children = new ArrayList<HADatAcThing>();
                results.put(studyObject, children);
            }
        }

        return results;
    }

    @Override
    public boolean saveToTripleStore() {
        System.out.println("Saving study object " + getUri() + " to triple store");

        if (uri == null || uri.equals("")) {
            System.out.println("[ERROR] Trying to save OBJ without assigning an URI");
            return false;
        }
        if (isMemberOf == null || isMemberOf.equals("")) {
            System.out.println("[ERROR] Trying to save OBJ without assigning DAS's URI");
            return false;
        }
        String insert = "";

        String obj_uri = "<" + getUri() + ">";

        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;
        if (typeUri.startsWith("http")) {
            insert += obj_uri + " a <" + typeUri + "> . ";
        } else {
            insert += obj_uri + " a " + typeUri + " . ";
        }
        if(!roleUri.isEmpty()){
            if (roleUri.startsWith("http")) {
                insert += obj_uri + " hasco:hasRole <" + roleUri + "> . ";
            } else {
                insert += obj_uri + " hasco:hasRole " + roleUri + " . ";
            }	
        }
        if (!originalId.equals("")) {
            insert += obj_uri + " hasco:originalID \""  + originalId + "\" .  ";
        }   
        if (!label.equals("")) {
            insert += obj_uri + " rdfs:label  \"" + label + "\" . ";
        }
        if (!isMemberOf.equals("")) {
            if (isMemberOf.startsWith("http")) {
                insert += obj_uri + " hasco:isMemberOf <" + isMemberOf + "> .  "; 
            } else {
                insert += obj_uri + " hasco:isMemberOf " + isMemberOf + " .  "; 
            } 
        }
        if (!comment.equals("")) {
            insert += obj_uri + " hasco:hasComment \""  + comment + "\" .  ";
        }
        if (scopeUris != null && scopeUris.size() > 0) {
            for (String scope : scopeUris) {
                if (!scope.equals("")) {
                    if (scope.startsWith("http")) {
                        insert += obj_uri + " hasco:hasObjectScope <" + scope + "> .  "; 
                    } else {
                        insert += obj_uri + " hasco:hasObjectScope " + scope + " .  "; 
                    }
                }
            } 
        }

        insert += LINE_LAST;
        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionsName(CollectionUtil.METADATA_UPDATE));
            processor.execute();
        } catch (QueryParseException e) {
            System.out.println("QueryParseException due to update query: " + insert);
            throw e;
        }

        return true;
    }

    @Override
    public int saveToLabKey(String user_name, String password) {
        System.out.println("Saving study object " + getUri() + " to LabKey");

        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        row.put("a", URIUtils.replaceNameSpaceEx(getTypeUri()));
        row.put("hasco:hasRole", URIUtils.replaceNameSpaceEx(getRoleUri()));
        row.put("hasco:originalID", getOriginalId());
        row.put("rdfs:label", getLabel());
        row.put("hasco:isMemberOf", URIUtils.replaceNameSpaceEx(getIsMemberOf()));
        row.put("rdfs:comment", getComment());
        String scopeStr = "";
        for (int i=0; i <  scopeUris.size(); i++) {
            String scope = scopeUris.get(i);
            scopeStr += URIUtils.replaceNameSpaceEx(scope);
            if (i < scopeUris.size() - 1) {
                scopeStr += " , ";
            }
        }
        row.put("hasco:hasObjectScope",scopeStr);
        rows.add(row);
        int totalChanged = 0;
        try {
            totalChanged = loader.insertRows("StudyObject", rows);
        } catch (CommandException e) {
            System.out.println(e);
            try {
                totalChanged = loader.updateRows("StudyObject", rows);
            } catch (CommandException e2) {
                System.out.println(e2);
                System.out.println("[ERROR] Could not insert or update Study Object(s)");
            }
        }

        return totalChanged;
    }

    @Override
    public int deleteFromLabKey(String user_name, String password) {
        System.out.println("Deleting study object " + getUri() + " from LabKey");

        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        rows.add(row);

        try {
            return loader.deleteRows("StudyObject", rows);
        } catch (CommandException e) {
            System.out.println("[ERROR] Could not delete Study Object(s)");
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void deleteFromTripleStore() {
        System.out.println("Deleting study object " + getUri() + " from triple store");

        String query = "";
        if (this.getUri() == null || this.getUri().equals("")) {
            return;
        }
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += DELETE_LINE1;
        if (this.getUri().startsWith("http")) {
            query += "<" + this.getUri() + ">";
        } else {
            query += this.getUri();
        }
        query += DELETE_LINE3;
        query += LINE_LAST;

        UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                request, CollectionUtil.getCollectionsName(CollectionUtil.METADATA_UPDATE));
        processor.execute();
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
