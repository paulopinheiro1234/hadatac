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
import org.hadatac.entity.pojo.StudyObjectMatching;
import org.hadatac.console.http.SPARQLUtils;

public class StudyObjectMatching extends StudyObject {

    public static String className = "hasco:StudyObjectMatching";

    public static String INDENT1 = "   ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String LINE_LAST = "}  ";

    private List<String> hasMemberUris = null;

    public StudyObjectMatching(String isMemberOf, String id) {
    	this.setIsMemberOf(isMemberOf);
    	this.setOriginalId(id);
    	this.uri = isMemberOf.replace("SOC", "MATCHING") + "-" + id;
        this.hasMemberUris = new ArrayList<String>();
    }

    public void addMemberUri(String uri) {
    	hasMemberUris.add(uri);;
    }

    public static List<String> findMatches(String memberUri) {
     	if (memberUri == null || memberUri.isEmpty()) {
    		return null;
    	}
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?targetUri WHERE { " + 
                " <" + memberUri + "> hasco:isMatchingMember  ?matchingUri . " +
                " ?targetUri hasco:isMatchingMember ?matchingUri . " +
                "}";

    	//System.out.println(queryString);
    	
    	ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            return null;
        }
        List<String> resp = new ArrayList<String>();
        while (resultsrw.hasNext()) {
        	QuerySolution soln = resultsrw.next();
        	if (soln != null) {
        		try {
        			if (soln.getResource("targetUri") != null && soln.getResource("targetUri").getURI() != null) {
        				 resp.add(soln.getResource("targetUri").getURI());
        			}
        		} catch (Exception e1) {
        			System.out.println("StudyObjectMatching [ERROR]: error retrieving Study Object ID.");
        		}
        	}
        }
        return resp;
    }

    public static StudyObjectMatching find(String socUri, String id) {
     	if (socUri == null || socUri.isEmpty()) {
    		return null;
    	}
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?matchingUri WHERE { " + 
                "   <" + socUri +"> a hasco:StudyObjectMatching . " +
                "   <" + socUri +"> hasco:isMemberOf ?matchingUri . " +
                "   ?matchingUri hasco:originalId \"" + id + "\" . " +
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            return null;
        }
        QuerySolution soln = resultsrw.next();
        if (soln != null) {
        	try {
        		if (soln.getResource("matchingUri") != null && soln.getResource("matchingUri").getURI() != null) {
        			return StudyObjectMatching.find(soln.getResource("matchingUri").getURI());
                } 
        	} catch (Exception e1) {
        		System.out.println("StudyObjectMatching [ERROR]: error retrieving matching content from graph database.");
        	}
        }
        return null;    	
    }
    
    public static StudyObjectMatching findByMemberUri(String memberUri) {
     	if (memberUri == null || memberUri.isEmpty()) {
    		return null;
    	}
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?matchingUri WHERE { " + 
                "   <" + memberUri +"> hasco:isMatchingMember ?matchingUri . " +
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            return null;
        }
        QuerySolution soln = resultsrw.next();
        if (soln != null) {
        	try {
        		if (soln.getResource("matchingUri") != null && soln.getResource("matchingUri").getURI() != null) {
        			return StudyObjectMatching.find(soln.getResource("matchingUri").getURI());
                } 
        	} catch (Exception e1) {
        		System.out.println("StudyObjectMatching [ERROR]: error retrieving matching content from graph database.");
        	}
        }
        return null;    	
    }
    
    public static StudyObjectMatching find(String matchingUri) {
     	if (matchingUri == null || matchingUri.isEmpty()) {
    		return null;
    	}

     	String soc = "";
     	String id = "";
     	StudyObjectMatching matching = null;
     	
     	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?memberUri ?matchingId ?isMemberOf WHERE { " + 
                " ?memberUri hasco:isMatchingMember <" + matchingUri + "> . " +
                " <" + matchingUri + "> hasco:originalID ?matchingId . " +
                " <" + matchingUri + "> hasco:isMemberOf ?isMemberOf . " +
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            return null;
        }
        QuerySolution soln = resultsrw.next();
        if (soln != null) {
            try {
                if (soln.getResource("isMemberOf") != null && soln.getResource("isMemberOf").getURI() != null) {
                	soc = soln.getResource("isMemberOf").getURI();
                }
                if (soln.getLiteral("matchingId") != null && soln.getLiteral("matchingId").getString() != null) {
                	id = soln.getLiteral("matchingId").getString();
                	matching = new StudyObjectMatching(soc, id);
                }
                if (soln.getResource("memberUri") != null && soln.getResource("memberUri").getURI() != null) {
                	String objUri = soln.getResource("memberUri").getURI();
                	matching.addMemberUri(objUri);
                }
            } catch (Exception e1) {
            	System.out.println("StudyObjectMatching [ERROR]: error retrieving matching content from graph database.");
            }
        }
        while (resultsrw.hasNext()) {
            soln = resultsrw.next();
            if (soln != null) {
                try {
                    if (soln.getResource("memberUri") != null && soln.getResource("memberUri").getURI() != null) {
                    	String objUri = soln.getResource("memberUri").getURI();
                    	matching.addMemberUri(objUri);
                    }
                } catch (Exception e1) {
                	System.out.println("StudyObjectMatching [ERROR]: error retrieving matching content from graph database.");
                }
            }
        }
        return matching;
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

        insert += uriStr + " a hasco:StudyObjectMatching . ";
        insert += uriStr + " hasco:isMemberOf <" + isMemberOf + "> . ";
        insert += uriStr + " hasco:originalID \"" + originalId + "\" . ";
        if (this.hasMemberUris != null && this.hasMemberUris.size() > 0) {
            for (String memberUri : this.hasMemberUris) {
                if (memberUri.startsWith("http")) {
                	insert += " <" + memberUri + "> hasco:isMatchingMember " + uriStr + "  . ";
                } else {
                	insert += " " + memberUri + " hasco:isMatchingMember " + uriStr + "  . ";
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

    public boolean saveLastMemberTripleStore() {
    	if (hasMemberUris.size() <= 0) {
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

        String lastUri = hasMemberUris.get(hasMemberUris.size() - 1);
        if (lastUri.startsWith("http")) {
        	insert += " <" + lastUri + "> hasco:isMatchingMember " + uriStr + "  . ";
        } else {
        	insert += " " + lastUri + " hasco:isMatchingMember " + uriStr + "  . ";
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
