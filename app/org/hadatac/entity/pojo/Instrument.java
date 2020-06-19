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
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class Instrument extends HADatAcThing implements Comparable<Instrument> {

	private String serialNumber;
	private String image;
	
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    
    public String getTypeLabel() {
    	InstrumentType insType = InstrumentType.find(getTypeUri());
    	if (insType == null || insType.getLabel() == null) {
    		return "";
    	}
    	return insType.getLabel();
    }

    public String getTypeURL() {
    	InstrumentType insType = InstrumentType.find(getTypeUri());
    	if (insType == null || insType.getLabel() == null) {
    		return "";
    	}
    	return insType.getURL();
    }

    public List<Detector> getAttachments() {
    	List<Detector> dets = new ArrayList<Detector>();
    	if (uri == null || uri.isEmpty()) {
    		return dets;
    	}
    	String iUri = uri;
    	if (uri.startsWith("http")) {
    		iUri = "<" + uri + ">";
    	}
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			    "SELECT ?detUri WHERE { " +
			    "   ?detUri vstoi:isInstrumentAttachment " + iUri + " . " + 
			    "} ";
			
		ResultSetRewindable resultsrw = SPARQLUtils.select(
				CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
				
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Detector det = Detector.find(soln.getResource("detUri").getURI());
			dets.add(det);
		}			
    	return dets;
    }
    
	@Override
	public boolean equals(Object o) {
		if((o instanceof Instrument) && (((Instrument)o).getUri().equals(this.getUri()))) {
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
				Instrument instrument = new Instrument();
				instrument.setUri(soln.get("instrumentUri").toString());
				instrument.setLabel(soln.get("instrumentLabel").toString());
				instrument.setQuery(query);
				instrument.setField("instrument_uri_str");
				
				STR da = new STR();
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
	
	public static List<Instrument> find() {
		List<Instrument> instruments = new ArrayList<Instrument>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		    " SELECT ?uri WHERE { " +
		    " ?instModel rdfs:subClassOf+ vstoi:Instrument . " + 
		    " ?uri a ?instModel ." + 
		    "} ";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
			
		while (resultsrw.hasNext()) {
		    QuerySolution soln = resultsrw.next();
		    Instrument instrument = find(soln.getResource("uri").getURI());
		    instruments.add(instrument);
		}			
		
		java.util.Collections.sort((List<Instrument>) instruments);
		return instruments;
	}
	
	public static List<Instrument> findAvailable() {
		List<Instrument> instruments = new ArrayList<Instrument>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		    " SELECT ?uri WHERE { " +
		    "   { ?instModel rdfs:subClassOf+ vstoi:Instrument . " + 
		    "     ?uri a ?instModel ." + 
		    "   } MINUS { " + 
		    "     ?dep_uri a vstoi:Deployment . " + 
		    "     ?dep_uri hasco:hasInstrument ?uri .  " +
		    "     FILTER NOT EXISTS { ?dep_uri prov:endedAtTime ?enddatetime . } " + 
		    "    } " + 
		    "} " + 
		    "ORDER BY DESC(?datetime) ";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
		
		while (resultsrw.hasNext()) {
		    QuerySolution soln = resultsrw.next();
		    Instrument instrument = find(soln.getResource("uri").getURI().trim());
			instruments.add(instrument);
		}			
		
		java.util.Collections.sort((List<Instrument>) instruments);
		return instruments;
	}
	
	public static List<Instrument> findDeployed() {
		List<Instrument> instruments = new ArrayList<Instrument>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		    " SELECT ?uri WHERE { " +
		    "   ?instModel rdfs:subClassOf+ vstoi:Instrument . " + 
		    "   ?uri a ?instModel ." + 
		    "   ?dep_uri a vstoi:Deployment . " + 
		    "   ?dep_uri hasco:hasInstrument ?uri .  " +
		    "   FILTER NOT EXISTS { ?dep_uri prov:endedAtTime ?enddatetime . } " + 
		    "} " + 
		    "ORDER BY DESC(?datetime) ";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
		
		while (resultsrw.hasNext()) {
		    QuerySolution soln = resultsrw.next();
		    Instrument instrument = find(soln.getResource("uri").getURI().trim());
		    instruments.add(instrument);
		}			

		java.util.Collections.sort((List<Instrument>) instruments);
		return instruments;
	}
	
	public static Instrument find(String uri) {
	    Instrument instrument = null;
	    Statement statement;
	    RDFNode object;
	    
	    String queryString = "DESCRIBE <" + uri + ">";
	    Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);
		
		instrument = new Instrument();
		StmtIterator stmtIterator = model.listStatements();
		
		while (stmtIterator.hasNext()) {
		    statement = stmtIterator.next();
		    object = statement.getObject();
		    if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
		    	instrument.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                instrument.setTypeUri(object.asResource().getURI());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#hasSerialNumber")) {
		    	instrument.setSerialNumber(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasImage")) {
                instrument.setImage(object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
		    	instrument.setComment(object.asLiteral().getString());
		    }
		}
		
		instrument.setUri(uri);
		
		return instrument;
	}
	
    @Override
    public int compareTo(Instrument another) {
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
    
}
