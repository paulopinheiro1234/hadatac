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
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.utils.ConfigProp;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.labkey.remoteapi.CommandException;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;

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
	List<String> scopeUris;

	public StudyObject() {
		this("", "");
	}

	public StudyObject(String uri, String isMemberOf) {
		this.setUri(uri);
		this.setTypeUri("");
		this.setOriginalId("");
		this.setLabel("");
		this.setIsMemberOf(isMemberOf);
		this.setComment("");
		this.setScopeUris(new ArrayList<String>());
	}

	public StudyObject(String uri,
			String typeUri,
			String originalId,
			String label,
			String isMemberOf,
			String comment,
			List<String> scopeUris) { 
		this.setUri(uri);
		this.setTypeUri(typeUri);
		this.setOriginalId(originalId);
		this.setLabel(label);
		this.setIsMemberOf(isMemberOf);
		this.setComment(comment);
		this.setScopeUris(scopeUris);
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

	private static List<String> retrieveScopeUris(String obj_uri) {
		List<String> retrievedUris = new ArrayList<String>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT  ?scopeUri WHERE { " + 
				"    " + obj_uri + " hasco:hasObjectScope ?scopeUri . " + 
				"}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
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
		//System.out.println("Looking for object with URI " + obj_uri);
		if (obj_uri.startsWith("http")) {
			obj_uri = "<" + obj_uri + ">";
		}
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT  ?objType ?originalId ?isMemberOf ?hasLabel " + 
				" ?hasComment ?isFrom ?atLocation ?atTime WHERE { " + 
				"    " + obj_uri + " a ?objType . " + 
				"    " + obj_uri + " hasco:isMemberOf ?isMemberOf .  " + 
				"    OPTIONAL { " + obj_uri + " hasco:originalID ?originalId } . " + 
				"    OPTIONAL { " + obj_uri + " rdfs:label ?hasLabel } . " + 
				"    OPTIONAL { " + obj_uri + " rdfs:comment ?hasComment } . " + 
				"}";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		if (!resultsrw.hasNext()) {
			System.out.println("[WARNING] StudyObject. Could not find OBJ with URI: " + obj_uri);
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
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

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
		//System.out.println("StudyObject findByCollection: " + queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null && soln.getResource("uri").getURI() != null) { 
				//System.out.println("URI: [" + soln.getResource("uri").getURI() + "]");
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

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
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

	public void save() {
		delete();  // delete any existing triple for the current OBJ
		//System.out.println("Saving <" + uri + ">");
		if (uri == null || uri.equals("")) {
			System.out.println("[ERROR] Trying to save OBJ without assigning an URI");
			return;
		}
		if (isMemberOf == null || isMemberOf.equals("")) {
			System.out.println("[ERROR] Trying to save OBJ without assigning DAS's URI");
			return;
		}
		String insert = "";

		String obj_uri = "";
		if (this.getUri().startsWith("<")) {
			obj_uri = this.getUri();
		} else {
			obj_uri = "<" + this.getUri() + ">";
		}

		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		if (typeUri.startsWith("http")) {
			insert += obj_uri + " a <" + typeUri + "> . ";
		} else {
			insert += obj_uri + " a " + typeUri + " . ";
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
		//insert += this.getUri() + " hasco:hasSource " + " .  "; 
		//insert += this.getUri() + " hasco:isPIConfirmed " + " .  "; 
		insert += LINE_LAST;
		//System.out.println("OBJ insert query (pojo's save): <" + insert + ">");
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
		row.put("a", ValueCellProcessing.replaceNameSpaceEx(getTypeUri()));
		row.put("hasco:originalID", getOriginalId());
		row.put("rdfs:label", getLabel());
		row.put("hasco:isMemberOf", ValueCellProcessing.replaceNameSpaceEx(getIsMemberOf()));
		row.put("rdfs:comment", getComment());
		String scopeStr = "";
		for (int i=0; i <  scopeUris.size(); i++) {
			String scope = scopeUris.get(i);
			scopeStr += ValueCellProcessing.replaceNameSpaceEx(scope);
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
				System.out.println("[ERROR] Could not insert or update Object(s)");
			}
		}
		return totalChanged;
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public int deleteFromLabKey(String user_name, String password) throws CommandException {
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
		LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
		List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri().replace("<","").replace(">","")));
		rows.add(row);
		for (Map<String,Object> str : rows) {
			System.out.println("deleting object " + row.get("hasURI"));
		}
		return loader.deleteRows("StudyObject", rows);
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
		//System.out.println("SPARQL query inside obj poho's delete: " + query);
		UpdateRequest request = UpdateFactory.create(query);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
	}

}
