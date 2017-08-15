package org.hadatac.metadata.loader;

import java.util.Map;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.entity.pojo.Study;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.hadatac.utils.NameSpaces;

import play.Play;

public class MetadataContext implements RDFContext {

    String username = null;
    String password = null;
    String kbURL = null;
    boolean verbose = false;

    String processMessage = "";
    String loadFileMessage = "";
	
    public MetadataContext(String un, String pwd, String kb, boolean ver) {
    	username = un;
	    password = pwd;
	    kbURL = kb;
	    verbose = ver;
    }

    public static Long playTotalTriples() {
    	MetadataContext metadata = new MetadataContext(
    			"user", "password", 
    			Play.application().configuration().getString("hadatac.solr.triplestore"), 
    			false);
    	return metadata.totalTriples();
    }
    
    public Long totalTriples() {
    	try {
    		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    				"SELECT (COUNT(*) as ?tot) WHERE { ?s ?p ?o . }";
    		Query query = QueryFactory.create(queryString);
    			
    		QueryExecution qexec = QueryExecutionFactory.sparqlService(
    				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    		ResultSet results = qexec.execSelect();
    		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
    		qexec.close();
    			
    		QuerySolution soln = resultsrw.next();
    		
    		return Long.valueOf(soln.getLiteral("tot").getValue().toString()).longValue();
    	} catch (Exception e) {
			return (long) -1;
		}
    }
    
	public String clean(int mode) {	
	    String message = "";
        message += Feedback.println(mode,"   Triples before [clean]: " + totalTriples());
        message += Feedback.println(mode, " ");
        
        String queryString = "";
		queryString += NameSpaces.getInstance().printSparqlNameSpaceList();
		queryString += "DELETE WHERE { ?s ?p ?o . } ";
		UpdateRequest req = UpdateFactory.create(queryString);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(req, 
				Collections.getCollectionsName(Collections.METADATA_UPDATE));
		try {
			processor.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, " ");
		message += Feedback.print(mode, "   Triples after [clean]: " + totalTriples());
		
        return message; 
	}
	
	public String cleanStudy(int mode, String study) {	
	    String message = "";
        message += Feedback.println(mode,"   Triples before [clean]: " + totalTriples());
        message += Feedback.println(mode, " ");
        message += Feedback.println(mode, "      Deleted the following triples: ");
        Study studyObj = new Study();
        Model model = studyObj.findModel(study);
        StmtIterator iter = model.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			System.out.println(stmt.toString());
			message += Feedback.println(mode, stmt.toString());
		}
        
        String queryString = "";
		queryString += NameSpaces.getInstance().printSparqlNameSpaceList();
		queryString += "DELETE {?s ?p ?o } " +
		"WHERE " +
		"{  " +
		"  { " +
		"	{  " +
		// Study 
		"   ?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?s a ?subUri . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?s = " + study + ") " +
		"  	} " +
		"    MINUS " +
		"    { " +
		// Other Studies 
		"   ?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?s a ?subUri . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?s != " + study + ") " +
		"    }  " +
		"  } " +
		"  UNION " + 
		"  { " +
		"	{  " +
		//  Data Acquisitions, Cohort
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?s hasco:isDataAcquisitionOf|hasco:isCohortOf ?study . " + 
		"  	?s ?p ?o . " +
		"  	FILTER (?study = " + study + ") " +
		"  	} " +
		"    MINUS " +
		"    {  " +
		// Other Data Acquisitions, Cohort
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?s hasco:isDataAcquisitionOf|hasco:isCohortOf ?study . " + 
		"  	?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"  	} " +
		"  } " +
		"  UNION " + 
		"  { " +
		"	{  " +
		//  Cohort Subjects
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?cohort hasco:isCohortOf ?study . " +
		"	?s hasco:isSubjectOf ?cohort . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study = " + study + ") " +
		"  	} " +
		"    MINUS " +
		"    {  " +
		// Other Cohort Subjects
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?cohort hasco:isCohortOf ?study . " +
		"	?s hasco:isSubjectOf ?cohort . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"  	} " +
		"  } " +
		"  UNION " + 
		"  { " +
		"	{  " +
		//  Data Acquisition Schema and Deployment
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?da hasco:isDataAcquisitionOf ?study . " + 
		"   ?da hasco:hasSchema|hasco:hasDeployment ?s . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study = " + study + ") " +
		"  	} " +
		"    MINUS " +
		"    {  " +
		// Other Data Acquisition Schema and Deployment
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?da hasco:isDataAcquisitionOf ?study . " + 
		"   ?da hasco:hasSchema|hasco:hasDeployment ?s . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"  	} " +
		"  } " +
		"  UNION " + 
		"  { " +
		"    { " +
		// Sample Collections
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"   ?s hasco:isSampleCollectionOf ?study . " + 
		"   ?s ?p ?o . " +
		"  FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    { " +
		// Other Sample Collections
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"   ?s hasco:isSampleCollectionOf ?study . " + 
		"   ?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"    } " +
		"  } "  +
		"  UNION " + 
		"  { " +
		"    { " +
		// Sample Collection Samples
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"   ?sc hasco:isSampleCollectionOf ?study . " + 
		"   ?s hasco:isObjectOf ?sc .  " +
		"   ?s ?p ?o . " +
		"  FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    { " +
		// Other Sample Collection Samples
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"   ?sc hasco:isSampleCollectionOf ?study . " + 
		"   ?s hasco:isObjectOf ?sc .  " +
		"   ?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"    } " +
		"  } "  +
		"  UNION " + 
		"  { " +
		"    { " +
		// Deployment - Platform, Instrument, detector
		"  	?subUri rdfs:subClassOf* hasco:Study .  " + 
		"  	?study a ?subUri . " +
		"   ?da hasco:isDataAcquisitionOf ?study . " + 
		"  	?da hasco:hasDeployment ?deploy .  " +
		"	?deploy vstoi:hasPlatform|hasco:hasInstrument|hasco:hasDetector ?s . " +
		"  	?s ?p ?o . " +
		"  FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    { " +
		// Other Deployment - Platform, Instrument, detector
		"  	?subUri rdfs:subClassOf* hasco:Study .  " + 
		"  	?study a ?subUri . " +
		"   ?da hasco:isDataAcquisitionOf ?study . " + 
		"  	?da hasco:hasDeployment ?deploy .  " +
		"	?deploy vstoi:hasPlatform|hasco:hasInstrument|hasco:hasDetector ?s . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"    } " +
		"  } " +
		"  UNION " + 
		"  { " +
		"    { " +
		// DA Schema Attribute
		"  	?subUri rdfs:subClassOf* hasco:Study .  " + 
		"  	?study a ?subUri . " +
		"  	?da hasco:isDataAcquisitionOf ?study . " +
		"   ?da hasco:hasSchema ?schema . " +
		"   ?s hasco:partOfSchema ?schema . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    { " +
		// Other DA Schema Attribute
		"  	?subUri rdfs:subClassOf* hasco:Study .  " + 
		"  	?study a ?subUri . " +
		"  	?da hasco:isDataAcquisitionOf ?study . " +
		"   ?da hasco:hasSchema ?schema . " +
		"   ?s hasco:partOfSchema ?schema . " +
		"  	?s ?p ?o . " +
		"  FILTER (?study != " + study + ") " +
		"    } " +
		"  } " +
		"  UNION  " +
		"  { " +
		"  	 {  " +
		// Datasets
		"   ?subUri rdfs:subClassOf* hasco:Study . " + 
		"   ?study a ?subUri . " +
		"   ?s hasco:isDatasetOf ?study . " +
		"   ?s ?p ?o . " +
		"   FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    {  " +
		// Other Datasets
		"   ?subUri rdfs:subClassOf* hasco:Study . " + 
		"   ?study a ?subUri . " +
		"   ?s hasco:isDatasetOf ?study . " +
		"   ?s ?p ?o . " +
		"   FILTER (?study != " + study + ") " +
		"     } " +
		"   } " +
		"   UNION " + 
		"   { " +
		"  	  {  " +
		// Attribute References 
		"    ?subUri rdfs:subClassOf* hasco:Study . " + 
		"    ?study a ?subUri . " +
		"    ?data hasco:isDatasetOf ?study . " +
		"    ?s hasco:isAttributeReferenceOf ?data . " +
		"    ?s ?p ?o . " +
		"    FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    {  " +
		// Other Attribute References
		"    ?subUri rdfs:subClassOf* hasco:Study . " + 
		"    ?study a ?subUri . " +
		"    ?data hasco:isDatasetOf ?study . " +
		"     ?s hasco:isAttributeReferenceOf ?data . " +
		"    ?s ?p ?o . " +
		"    FILTER (?study != " + study + ") " +
		"    } " +
		"  } " +
		"} ";
		
		UpdateRequest req = UpdateFactory.create(queryString);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(req, 
				Collections.getCollectionsName(Collections.METADATA_UPDATE));
		try {
			processor.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, " ");
		message += Feedback.print(mode, "   Triples after [clean]: " + totalTriples());
		
        return message; 
	}
	
	
	public String getLang(String contentType) {
		if (contentType.contains("turtle")) {
			return "TTL";
		} else if (contentType.contains("rdf+xml")) {
			return "RDF/XML";
		} else {
			return "";
		}
	}
	
	/* 
	 *   contentType correspond to the mime type required for curl to process the data provided. For example, application/rdf+xml is
	 *   used to process rdf/xml content.
	 *   
	 */
	public Long loadLocalFile(int mode, String filePath, String contentType) {
		Model model = ModelFactory.createDefaultModel();
		DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(
				kbURL + Collections.METADATA_GRAPH);

		loadFileMessage = "";
		Long total = totalTriples();
		try {
			model.read(filePath, getLang(contentType));
			accessor.add(model);
		} catch (NotFoundException e) {
			System.out.println("NotFoundException: file " + filePath);
			System.out.println("NotFoundException: " + e.getMessage());
		} catch (RiotNotFoundException e) {
			System.out.println("RiotNotFoundException: file " + filePath);
			System.out.println("RiotNotFoundException: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Exception: file " + filePath);
			System.out.println("Exception: " + e.getMessage());
		}

		Long newTotal = totalTriples();
		return (newTotal - total);
	}
	
	/*
	 *  oper: "confirmed" cache ontologies from the web and load
	 *        "confrmedCache" load from cached ontologies
	 *        "cache" cache ontologies from the web
	 */
	public String loadOntologies(int mode, String oper) {
	    String message = "";
	    Long total = new Long(0);
	    if (!oper.equals("cache")) {
		    total = totalTriples();
		    message += Feedback.println(mode, "   Triples before [loadOntologies]: " + total);
		    message += Feedback.println(mode," ");
	    }
		if (!oper.equals("confirmedCache")) {
		    message += NameSpaces.getInstance().copyNameSpacesLocally(mode);
		}
		if (!oper.equals("cache")) {
		    for (Map.Entry<String, NameSpace> entry : NameSpaces.table.entrySet()) {
	    	    String abbrev = entry.getKey().toString();
	    	    String nsURL = entry.getValue().getURL();
	    	    if ((abbrev != null) && (nsURL != null) && (entry.getValue().getType() != null) && !nsURL.equals("")) {
	    		    String filePath = NameSpaces.CACHE_PATH + "copy" + "-" + abbrev.replace(":","");
	    		    message += Feedback.print(mode, "   Uploading " + filePath);
	    		    for (int i = filePath.length(); i < 50; i++) {
	    			    message += Feedback.print(mode, ".");
	    		    }
	    		    loadLocalFile(mode, filePath, entry.getValue().getType());
	    		    message += loadFileMessage;
	    		    Long newTotal = totalTriples();
	    		    message += Feedback.println(mode, "   Added " + (newTotal - total) + " triples.");
	    		
	    		    total = newTotal;
	    	    }	          
	         }
	    	 message += Feedback.println(mode," ");
		     message += Feedback.println(mode, "   Triples after [loadOntologies]: " + totalTriples());
		}
		return message;
	}
}	
	
