package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.solr.client.solrj.beans.Field;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class MessageStream extends HADatAcThing implements Comparable<MessageStream> {

	private String ip = "";
	private String port = "";
	private String status = "";
	private String protocol = "";
	
	public String getIP() {
		return ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}
	
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
    
    public String getStatus() {
    	if (status == null || status.isEmpty()) {
    		status = "off";
    	}
    	return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
	@Override
	public boolean equals(Object o) {
		if((o instanceof MessageStream) && (((MessageStream)o).getUri().equals(this.getUri()))) {
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
        return getTargetFacetsFromTripleStore(facet, facetHandler);
    }
	
	public Map<Facetable, List<Facetable>> getTargetFacetsFromTripleStore(
			Facet facet, FacetHandler facetHandler) {
		String valueConstraint = "";
		if (!facet.getFacetValuesByField("platform_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?platformUri { " + stringify(
					facet.getFacetValuesByField("platform_uri_str")) + " } \n ";
		}
		
		String query = "";
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += "SELECT ?platformUri ?instrumentUri ?dataAcquisitionUri ?instrumentLabel ?dataAcquisitionLabel WHERE { \n"
				+ valueConstraint
				+ " ?dataAcquisitionUri hasco:hasDeployment ?deploymentUri . \n"
				+ " ?deploymentUri vstoi:hasPlatform ?platformUri . \n"
				+ " ?deploymentUri hasco:hasInstrument ?instrumentUri . \n"
				+ " ?instrumentUri rdfs:label ?instrumentLabel . \n"
				+ " ?dataAcquisitionUri rdfs:label ?dataAcquisitionLabel . \n"
				+ " } \n";

		//System.out.println("Instrument getTargetFacets query: " + query);
		
		facet.clearFieldValues("acquisition_uri_str");
		
		Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
		try {
		    ResultSetRewindable resultsrw = SPARQLUtils.select(
	                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
		    
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				MessageStream instrument = new MessageStream();
				instrument.setUri(soln.get("instrumentUri").toString());
				instrument.setLabel(soln.get("instrumentLabel").toString());
				instrument.setQuery(query);
				instrument.setField("instrument_uri_str");
				
				ObjectAccessSpec da = new ObjectAccessSpec();
				da.setUri(soln.get("dataAcquisitionUri").toString());
				da.setLabel(soln.get("dataAcquisitionLabel").toString());
				da.setField("acquisition_uri_str");
				
				if (!results.containsKey(instrument)) {
					List<Facetable> facets = new ArrayList<Facetable>();
					results.put(instrument, facets);
				}
				if (!results.get(instrument).contains(da)) {
					results.get(instrument).add(da);
				}
				
				Facet subFacet = facet.getChildById(instrument.getUri());
				subFacet.putFacet("instrument_uri_str", instrument.getUri());
				subFacet.putFacet("acquisition_uri_str", da.getUri());
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}

		return results;
	}
	
	public static List<MessageStream> find() {
		List<MessageStream> streams = new ArrayList<MessageStream>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		    " SELECT ?uri WHERE { " +
		    " ?uri a hasco:MessageStream ." + 
		    "} ";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
			
		while (resultsrw.hasNext()) {
		    QuerySolution soln = resultsrw.next();
		    MessageStream stream = find(soln.getResource("uri").getURI());
		    streams.add(stream);
		}			
		
		java.util.Collections.sort((List<MessageStream>) streams);
		return streams;
	}
	
	public static MessageStream find(String uri) {
	    MessageStream stream = null;
	    Statement statement;
	    RDFNode object;
	    
	    String queryString = "DESCRIBE <" + uri + ">";
	    Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);
		
		stream = new MessageStream();
		StmtIterator stmtIterator = model.listStatements();
		
		if (!stmtIterator.hasNext()) {
			return null;
		}
		
		while (stmtIterator.hasNext()) {
		    statement = stmtIterator.next();
		    object = statement.getObject();
		    if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
		    	stream.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                stream.setTypeUri(object.asResource().getURI());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasIP")) {
		    	stream.setIP(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasPort")) {
                stream.setPort(object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasStatus")) {
		    	stream.setStatus(object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasProtocol")) {
		    	stream.setProtocol(object.asLiteral().getString());
		    }
		}
		
		stream.setUri(uri);
		
		return stream;
	}
	
    @Override
    public int compareTo(MessageStream another) {
        return this.getLabel().compareTo(another.getLabel());
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
