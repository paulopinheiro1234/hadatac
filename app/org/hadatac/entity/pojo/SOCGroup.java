package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.entity.pojo.SOCGroup;
import org.hadatac.console.http.SPARQLUtils;

public class SOCGroup extends HADatAcThing {

    public static String INDENT1 = "   ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String LINE_LAST = "}  ";

    private String uri = "";
    private String id = "";
    private String ofSOC = "";
    private List<String> hasMemberUris = null;

    public SOCGroup(String soc, String id) {
        this.id = id;
        this.ofSOC = soc;
        this.uri = soc.replace("SOC", "GRP") + "-" + id;
        this.hasMemberUris = new ArrayList<String>();
    }

    public String getUri() {
    	return uri;
    }

    public void setUri(String uri) {
    	this.uri = uri;
    }

    public String getId() {
    	return id;
    }

    public void setId(String id) {
    	this.id = id;
    }

    public String getOfSOC() {
    	return ofSOC;
    }

    public void setOfSOC(String soc) {
    	this.ofSOC = soc;
    }

    public List<String> getMemberUris() {
    	return hasMemberUris;
    }

    public boolean hasMember(String memberUri) {
    	return hasMemberUris.contains(memberUri);
    }
    
    public void addMemberUri(String uri) {
    	if (hasMemberUris != null) {
    		if (!hasMemberUris.contains(uri)) {
    			hasMemberUris.add(uri);
    		}
    	}
    }

    public void addMemberUris(List<String> listUri) {
    	hasMemberUris.addAll(listUri);
    }

    public static SOCGroup find(String grpUri) {
        SOCGroup grp = null;

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?soc ?id ?member WHERE { \n" + 
                "    <" + grpUri + "> a hasco:SOCGroup . \n" + 
                "    <" + grpUri + "> hasco:ofSOC ?soc . \n" + 
                "    <" + grpUri + "> hasco:hasGroupId ?id . \n" + 
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            System.out.println("[WARNING] SOCGroup. Could not find group with URI: <" + grpUri + ">");
            return grp;
        }

        String idStr = "";
        String socStr = "";
        List<String> memberUrisStr = new ArrayList<String>();

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {

                try {
                    if (soln.getResource("soc") != null && soln.getResource("soc").getURI() != null) {
                        socStr = soln.getResource("soc").getURI();
                    }
                } catch (Exception e1) {
                    socStr = "";
                }

                try {
                    if (soln.getLiteral("id") != null && soln.getLiteral("id").getString() != null) {
                        idStr = soln.getLiteral("id").getString();
                    }
                } catch (Exception e1) {
                    idStr = "";
                }

                memberUrisStr = retrieveMembers(grpUri);

                grp = new SOCGroup(socStr, idStr);
                if (memberUrisStr != null && memberUrisStr.size() > 0) {
                	grp.addMemberUris(memberUrisStr);
                }
            }
        }

        return grp;
    }

    public static List<String> retrieveMembers(String grpUri) {
        List<String> retrievedUris = new ArrayList<String>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?memberUri WHERE { " + 
                " ?memberUri hasco:isGroupMember <" + grpUri + "> . " + 
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
                    if (soln.getResource("memberUri") != null && soln.getResource("memberUri").getURI() != null) {
                        retrievedUris.add(soln.getResource("memberUri").getURI());
                    }
                } catch (Exception e1) {
                }
            }
        }
        return retrievedUris;
    }

    public static List<SOCGroup> findAll() {
        List<SOCGroup> grpList = new ArrayList<SOCGroup>();

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri WHERE { " + 
                "   ?uri a hasco:SOCGroup . } ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) { 
                SOCGroup grp = SOCGroup.find(soln.getResource("uri").getURI());
                grpList.add(grp);
            }
        }

        return grpList;
    }

    public static List<SOCGroup> findBySOCUri(String SOCUri) {
        if (SOCUri == null) {
            return null;
        }
        List<SOCGroup> grpList = new ArrayList<SOCGroup>();

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri WHERE { \n" + 
                "   ?uri a hasco:SOCGroup . \n" +
                "   ?uri hasco:ofSOC <" + SOCUri + "> . \n" +
                " } ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) { 
                SOCGroup grp = SOCGroup.find(soln.getResource("uri").getURI());
                grpList.add(grp);
            }
        }
        return grpList;
    }

    public static SOCGroup findBySOCUriAndId(String SOCUri, String id) {
        if (SOCUri == null || id == null) {
            return null;
        }

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri WHERE { \n" + 
                "   ?uri a hasco:SOCGroup . \n" +
                "   ?uri hasco:ofSOC <" + SOCUri + "> . \n" +
                "   ?uri hasco:hasGroupId \"" + id + "\" . \n" +
                " } ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        SOCGroup grp = null;
        if (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) { 
                grp = SOCGroup.find(soln.getResource("uri").getURI());
            }
        }
        return grp;
    }

    @Override
    public boolean saveToTripleStore() {
        String insert = "";

        String uriStr = "";
        if (this.getUri().startsWith("<")) {
            uriStr = this.getUri();
        } else {
            uriStr = "<" + this.getUri() + ">";
        }

        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;

        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }

        insert += uriStr + " a hasco:SOCGroup . ";
        insert += uriStr + " hasco:hasGroupId  \"" + this.getId() + "\" . ";
        if (this.getOfSOC().startsWith("http")) {
            insert += uriStr + " hasco:ofSOC  <" + this.getOfSOC() + "> . ";
        } else {
            insert += uriStr + " hasco:ofSOC  " + this.getOfSOC() + " . ";
        }
        if (this.getMemberUris() != null && this.getMemberUris().size() > 0) {
            for (String memberUri : this.getMemberUris()) {
                if (memberUri.startsWith("http")) {
                	insert += " <" + memberUri + "> hasco:isGroupMember " + uriStr + "  . ";
                } else {
                	insert += " " + memberUri + "  hasco:isGroupMember " + uriStr + "  . ";
                }
            }
        }

        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }

        insert += LINE_LAST;

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
            processor.execute();
        } catch (QueryParseException e) {
            System.out.println("QueryParseException due to update query: " + insert);
            throw e;
        }

        return true;
    }

    public boolean saveMemberToTripleStore(String memberUri) {
    	if (memberUri == null || memberUri.isEmpty()) {
    		return false;
    	}

    	String insert = "";

        String uriStr = "";
        if (this.getUri().startsWith("<")) {
            uriStr = this.getUri();
        } else {
            uriStr = "<" + this.getUri() + ">";
        }

        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;

        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }

        if (memberUri.startsWith("http")) {
        	insert += " <" + memberUri + "> hasco:isGroupMember " + uriStr + "  . ";
        } else {
        	insert += " " + memberUri + "  hasco:isGroupMember " + uriStr + "  . ";
        }

        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }

        insert += LINE_LAST;

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
            processor.execute();
        } catch (QueryParseException e) {
            System.out.println("QueryParseException due to update query: " + insert);
            throw e;
        }

        return true;
    }

    @Override
    public void deleteFromTripleStore() {
    	super.deleteFromTripleStore();
    }

    @Override
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }

    public String toString() {
        return this.getUri();
    } 

}
