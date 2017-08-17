package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.ConfigProp;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.labkey.remoteapi.CommandException;
import play.Play;

public class Platform  extends HADatAcThing implements Comparable<Platform> {

        public static String INSERT_LINE1 = "INSERT DATA {  ";
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
	public static String DELETE_LINE3 = " ?p ?o . ";
	public static String LINE_LAST = "}  ";
	public static String PREFIX = "PLT-";
	
	private String location;
	private String firstCoordinate;
	private String secondCoordinate;
	private String thirdCoordinate;
	private String elevation;
	private String serialNumber;
	
	// Constructer
	public Platform(String uri,
			String typeUri,
			String label,
			String comment) {
	    this.uri = uri;
	    this.typeUri = typeUri;
	    this.label = label;
	    this.comment = comment;
	}
	
	public Platform() {
	    this.uri = "";
	    this.typeUri = "";
	    this.label = "";
	    this.comment = "";
	}
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getElevation() {
		return elevation;
	}
	public void setElevation(String elevation) {
		this.elevation = elevation;
	}
	public String getFirstCoordinate() {
		return firstCoordinate;
	}
	public void setFirstCoordinate(String firstCoordinate) {
		this.firstCoordinate = firstCoordinate;
	}

	public String getSecondCoordinate() {
		return secondCoordinate;
	}

	public void setSecondCoordinate(String secondCoordinate) {
		this.secondCoordinate = secondCoordinate;
	}
	
	public String getThirdCoordinate() {
		return thirdCoordinate;
	}

	public void setThirdCoordinate(String thirdCoordinate) {
		this.thirdCoordinate = thirdCoordinate;
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	public static Platform find(String uri) {
		Platform platform = null;
		Model model;
		Statement statement;
		RDFNode object;
		
		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Play.application().configuration().getString("hadatac.solr.triplestore") 
				+ Collections.METADATA_SPARQL, query);
		model = qexec.execDescribe();
		
		platform = new Platform();
		StmtIterator stmtIterator = model.listStatements();
		
		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				platform.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
				platform.setTypeUri(object.asResource().getURI());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
				platform.setComment(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#hasSerialNumber")) {
				platform.setSerialNumber(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasFirstCoordinate")) {
				platform.setFirstCoordinate(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSecondCoordinate")) {
				platform.setSecondCoordinate(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasThirdCoordinate")) {
				platform.setThirdCoordinate(object.asLiteral().getString());
			}
		}
		
		platform.setUri(uri);
		
		return platform;
	}
	
	public static List<Platform> find() {
	    List<Platform> platforms = new ArrayList<Platform>();
	    String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		" SELECT ?uri WHERE { " +
		" ?platModel rdfs:subClassOf* vstoi:Platform . " + 
		" ?uri a ?platModel ." + 
		"} ";
		
	    Query query = QueryFactory.create(queryString);
	    
	    QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	    ResultSet results = qexec.execSelect();
	    ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	    qexec.close();
	    
	    System.out.println("Query: " + queryString);
	    System.out.println("HERE1 " + resultsrw.hasNext());
	    while (resultsrw.hasNext()) {
		QuerySolution soln = resultsrw.next();
		System.out.println("HERE2: " + soln.getResource("uri").getURI());
		Platform platform = find(soln.getResource("uri").getURI());
		platforms.add(platform);
	    }			
	    
	    java.util.Collections.sort((List<Platform>) platforms);
	    
	    return platforms;
	}
	
	public static Platform find(HADataC hadatac) {
		Platform platform = null;
		
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?platform ?label ?lat ?lon ?ele WHERE {\n"
				+ "  <" + hadatac.getDeploymentUri() + "> vstoi:hasPlatform ?platform .\n"
				+ "  OPTIONAL { ?platform rdfs:label ?label . }\n"
				+ "  OPTIONAL { ?platform rdfs:comment ?comment . }\n"
				+ "  OPTIONAL { ?platform <http://hadatac.org/ont/hasco/hasFirstCoordinate> ?lat . }\n"
				+ "  OPTIONAL { ?platform <http://hadatac.org/ont/hasco/hasSecondCoordinate> ?lon . }\n"
				+ "  OPTIONAL { ?platform <http://hadatac.org/ont/hasco/hasThirdCoordinate> ?ele . }\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(hadatac.getStaticMetadataSparqlURL(), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			platform = new Platform();
			platform.setUri(soln.getResource("platform").getURI());
			if (soln.getLiteral("label") != null) {
				platform.setLabel(soln.getLiteral("label").getString());
			}
			if(soln.getLiteral("comment") != null) {
				platform.setComment(soln.getLiteral("comment").getString());
			}
			if(soln.getLiteral("lat") != null) {
				platform.setFirstCoordinate(soln.getLiteral("lat").getString());
			}
			if(soln.getLiteral("lon") != null) {
				platform.setSecondCoordinate(soln.getLiteral("long").getString());
			}
			if(soln.getLiteral("ele") != null) {
				platform.setThirdCoordinate(soln.getLiteral("ele").getString());
				platform.setLocation("(" + platform.getFirstCoordinate() + ", " 
						 				 + platform.getSecondCoordinate() + ", "
						 				 + platform.getThirdCoordinate() + ")");
			}
			if (soln.getLiteral("ele") != null) {
				platform.setElevation(soln.getLiteral("ele").getString());
			}
		}
		
		return platform;
	}

	public void save() {
	    //System.out.println("Saving <" + uri + ">");
	    if (uri == null || uri.equals("")) {
		System.out.println("[ERROR] Trying to save Platform without assigning an URI");
		return;
	    }
	    
	    delete();  // delete any existing triple for the current platform
	    
	    String insert = "";
	    String plt_uri = "";
	    
	    if (this.getUri().startsWith("<")) {
		plt_uri = this.getUri();
	    } else {
		plt_uri = "<" + this.getUri() + ">";
	    }
	    insert += NameSpaces.getInstance().printSparqlNameSpaceList();
	    insert += INSERT_LINE1;
	    if (typeUri.startsWith("<")) {
		insert += plt_uri + " a " + typeUri + " . ";
	    } else {
		insert += plt_uri + " a <" + typeUri + "> . ";
	    }
	    insert += plt_uri + " rdfs:label  \"" + label + "\" . ";
	    if (comment != null && !comment.equals("")) {
		insert += plt_uri + " rdfs:comment \"" + comment + "\" .  ";
	    }
	    insert += LINE_LAST;
	    //System.out.println("Platform insert query (pojo's save): <" + insert + ">");
	    UpdateRequest request = UpdateFactory.create(insert);
	    UpdateProcessor processor = UpdateExecutionFactory.createRemote(
					request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
	    processor.execute();
	    
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	    public int saveToLabKey(String user_name, String password) {
	    String site = ConfigProp.getPropertyValue("labkey.config", "site");
	    String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
	    LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
	    List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
	    Map<String, Object> row = new HashMap<String, Object>();
	    row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
	    row.put("a", ValueCellProcessing.replaceNameSpaceEx(typeUri));
	    row.put("rdfs:label", getLabel());
	    row.put("rdfs:comment", getComment());
	    rows.add(row);
	    
	    int totalChanged = 0;
	    try {
		totalChanged = loader.insertRows("Platform", rows);
	    } catch (CommandException e) {
		try {
		    totalChanged = loader.updateRows("Platform", rows);
		} catch (CommandException e2) {
		    System.out.println("[ERROR] Could not insert or update Platform(ies)");
		}
	    }
	    return totalChanged;
	}
	
	public void delete() {
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
	    UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
	    processor.execute();
	}
	
	@Override
	    public int compareTo(Platform another) {
	    return this.getLabel().compareTo(another.getLabel());
	}
	
}
