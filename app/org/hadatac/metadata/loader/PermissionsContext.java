package org.hadatac.metadata.loader;

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
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import play.Play;
import play.libs.ws.*;
import play.mvc.Call;

public class PermissionsContext implements RDFContext {

    String username = null;
    String password = null;   
    boolean verbose = false;

    String processMessage = "";
    String loadFileMessage = "";
	
    public PermissionsContext(String un, String pwd, String kb, boolean ver) {
	    username = un;
	    password = pwd;
	    verbose = ver;
    }

    public static Long playTotalTriples() {
    	PermissionsContext permissions = new PermissionsContext(
	    		 "user", "password", 
	    		 Play.application().configuration().getString("hadatac.solr.permissions"),
	    		 false);
    	return permissions.totalTriples();
    }
   
    public Long totalTriples() {
    	try {
    		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    				"SELECT (COUNT(*) as ?tot) WHERE { ?s ?p ?o . }";
    		
    		Query query = QueryFactory.create(queryString);
    			
    		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL), query);
    		ResultSet results = qexec.execSelect();
    		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
    		qexec.close();
    		
    		QuerySolution soln = resultsrw.next();
    		return Long.valueOf(soln.getLiteral("tot").getValue().toString()).longValue();
    	} catch (Exception e) {
			e.printStackTrace();
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
				Collections.getCollectionsName(Collections.PERMISSIONS_UPDATE));
		processor.execute();
		
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
				Collections.getCollectionsName(Collections.PERMISSIONS_GRAPH));		

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
	
}	
	
