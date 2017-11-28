package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.labkey.remoteapi.CommandException;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.metadata.loader.LabkeyDataHandler;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class ObjectCollection extends HADatAcThing {

	public static String SUBJECT_COLLECTION = "http://hadatac.org/ont/hasco/SubjectGroup";
	public static String SAMPLE_COLLECTION = "http://hadatac.org/ont/hasco/SampleCollection";
	public static String LOCATION_COLLECTION = "http://hadatac.org/ont/hasco/LocationCollection";
	public static String TIME_COLLECTION = "http://hadatac.org/ont/hasco/TimeCollection";

	public static String INDENT1 = "   ";
	public static String INSERT_LINE1 = "INSERT DATA {  ";
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
	public static String LINE3 = INDENT1 + "a         hasco:ObjectCollection;  ";
	public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
	public static String LINE_LAST = "}  ";
	private String studyUri = "";
	private String hasScopeUri = "";    
	private List<String> spaceScopeUris = null;
	private List<String> timeScopeUris = null;
	private List<String> objectUris = null;

	public ObjectCollection() {
		this.uri = "";
		this.typeUri = "";
		this.label = "";
		this.comment = "";
		this.studyUri = "";
		this.hasScopeUri = "";
		this.spaceScopeUris = new ArrayList<String>();
		this.timeScopeUris = new ArrayList<String>();
		this.objectUris = new ArrayList<String>();
	}

	public ObjectCollection(String uri,
			String typeUri,
			String label,
			String comment,
			String studyUri,
			String hasScopeUri,
			List<String> spaceScopeUris,
			List<String> timeScopeUris) {
		this.setUri(uri);
		this.setTypeUri(typeUri);
		this.setLabel(label);
		this.setComment(comment);
		this.setStudyUri(studyUri);
		this.setHasScopeUri(hasScopeUri);
		this.setSpaceScopeUris(spaceScopeUris);
		this.setTimeScopeUris(timeScopeUris);
		this.objectUris = new ArrayList<String>();
	}

	public ObjectCollectionType getObjectCollectionType() {
		if (typeUri == null || typeUri.equals("")) {
			return null;
		}
		ObjectCollectionType ocType = ObjectCollectionType.find(typeUri);
		return ocType;    
	}

	public String getStudyUri() {
		return studyUri;
	}

	public Study getStudy() {
		if (studyUri == null || studyUri.equals("")) {
			return null;
		}
		return Study.find(studyUri);
	}

	public boolean isDomainCollection() {
		if (typeUri == null || typeUri.equals("")) {
			return false;
		}
		return (typeUri.equals(SUBJECT_COLLECTION) || typeUri.equals(SAMPLE_COLLECTION));
	}

	public boolean isLocationCollection() {
		if (typeUri == null || typeUri.equals("")) {
			return false;
		}
		return typeUri.equals(LOCATION_COLLECTION);
	}

	public boolean isTimeCollection() {
		if (typeUri == null || typeUri.equals("")) {
			return false;
		}
		return typeUri.equals(TIME_COLLECTION);
	}

	public void setStudyUri(String studyUri) {
		this.studyUri = studyUri;
	}

	public List<String> getObjectUris() {
		return objectUris;
	}

	public String getUriFromOriginalId(String originalId) {
		if (originalId == null || originalId.equals("")) {
			return "";
		}
		for (StudyObject obj : this.getObjects()) {
			if (originalId.equals(obj.getOriginalId())) {
				return obj.getUri();
			}
		}
		return "";
	}
	public List<StudyObject> getObjects() {
		List<StudyObject> resp = new ArrayList<StudyObject>();
		if (objectUris == null || objectUris.size() <=0) {
			return resp;
		}
		for (String uri : objectUris) {
			StudyObject obj = StudyObject.find(uri);
			if (obj != null) {
				resp.add(obj);
			}
		}
		return resp;
	}

	public void setObjectUris(List<String> objectUris) {
		this.objectUris = objectUris;
	}

	public String getHasScopeUri() {
		return hasScopeUri;
	}

	public ObjectCollection getHasScope() {
		if (hasScopeUri == null || hasScopeUri.equals("")) {
			return null;
		}
		return ObjectCollection.find(hasScopeUri);
	}

	public void setHasScopeUri(String hasScopeUri) {
		this.hasScopeUri = hasScopeUri;
	}

	public List<String> getSpaceScopeUris() {
		return spaceScopeUris;
	}

	public List<ObjectCollection> getSpaceScopes() {
		if (spaceScopeUris == null || spaceScopeUris.equals("")) {
			return null;
		}
		List<ObjectCollection> spaceScopes = new ArrayList<ObjectCollection>();
		for (String scopeUri : spaceScopeUris) {
			ObjectCollection oc = ObjectCollection.find(scopeUri);
			if (oc != null) {
				spaceScopes.add(oc);
			}
		}
		return spaceScopes;
	}

	public void setSpaceScopeUris(List<String> spaceScopeUris) {
		this.spaceScopeUris = spaceScopeUris;
	}

	public List<String> getTimeScopeUris() {
		return timeScopeUris;
	}

	public List<ObjectCollection> getTimeScopes() {
		if (timeScopeUris == null || timeScopeUris.equals("")) {
			return null;
		}
		List<ObjectCollection> timeScopes = new ArrayList<ObjectCollection>();
		for (String scopeUri : timeScopeUris) {
			ObjectCollection oc = ObjectCollection.find(scopeUri);
			if (oc != null) {
				timeScopes.add(oc);
			}
		}
		return timeScopes;
	}

	public void setTimeScopeUris(List<String> timeScopeUris) {
		this.timeScopeUris = timeScopeUris;
	}

	public boolean inUriList(List<String> selected) {
		String uriAdjusted = uri.replace("<","").replace(">","");
		for (String str : selected) {
			if (uriAdjusted.equals(str)) {
				return true;
			}
		}
		return false;
	}

	private static List<String> retrieveSpaceScope (String oc_uri) {
		List<String> scopeUris = new ArrayList<String>();
		String scopeUri = ""; 
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT  ?spaceScopeUri WHERE { " + 
				"    " + oc_uri + " hasco:hasSpaceScope ?spaceScopeUri . " + 
				"}";
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null) {
				try {
					if (soln.getResource("spaceScopeUri") != null && soln.getResource("spaceScopeUri").getURI() != null) {
						scopeUri = soln.getResource("spaceScopeUri").getURI();
						if (scopeUri != null && !scopeUri.equals("")) {
							scopeUris.add(scopeUri);
						}
					}
				} catch (Exception e1) {
				}
			}
		}
		
		return scopeUris;
	}

	private static List<String> retrieveTimeScope (String oc_uri) {
		List<String> scopeUris = new ArrayList<String>();
		String scopeUri = "";
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT  ?timeScopeUri WHERE { " + 
				"    " + oc_uri + " hasco:hasTimeScope ?timeScopeUri . " + 
				"}";
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null) {
				try {
					if (soln.getResource("timeScopeUri") != null && soln.getResource("timeScopeUri").getURI() != null) {
						scopeUri = soln.getResource("timeScopeUri").getURI();
						if (scopeUri != null && !scopeUri.equals("")) {
							scopeUris.add(scopeUri);
						}
					}
				} catch (Exception e1) {
				}
			}
		}
		
		return scopeUris;
	}

	public static ObjectCollection find(String oc_uri) {
		try {
			oc_uri = URLDecoder.decode(oc_uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		ObjectCollection oc = null;
		if (oc_uri.startsWith("http")) {
			oc_uri = "<" + oc_uri + ">";
		}
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT  ?ocType ?comment ?studyUri ?hasScopeUri ?spaceScopeUri ?timeScopeUri WHERE { " + 
				"    " + oc_uri + " a ?ocType . " + 
				"    " + oc_uri + " hasco:isMemberOf ?studyUri .  " + 
				"    OPTIONAL { " + oc_uri + " rdfs:comment ?comment } . " + 
				"    OPTIONAL { " + oc_uri + " hasco:hasScope ?hasScopeUri } . " + 
				"}";
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		if (!resultsrw.hasNext()) {
			System.out.println("[WARNING] ObjectCollection. Could not find OC with URI: " + oc_uri);
			return oc;
		}

		String typeStr = "";
		String labelStr = "";
		String studyUriStr = "";
		String commentStr = "";
		String hasScopeUriStr = "";
		List<String> spaceScopeUrisStr = new ArrayList<String>();
		List<String> timeScopeUrisStr = new ArrayList<String>();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null) {

				try {
					if (soln.getResource("ocType") != null && soln.getResource("ocType").getURI() != null) {
						typeStr = soln.getResource("ocType").getURI();
					}
				} catch (Exception e1) {
					typeStr = "";
				}

				labelStr = FirstLabel.getLabel(oc_uri);

				try {
					if (soln.getResource("studyUri") != null && soln.getResource("studyUri").getURI() != null) {
						studyUriStr = soln.getResource("studyUri").getURI();
					}
				} catch (Exception e1) {
					studyUriStr = "";
				}

				try {
					if (soln.getLiteral("comment") != null && soln.getLiteral("comment").getString() != null) {
						commentStr = soln.getLiteral("comment").getString();
					}
				} catch (Exception e1) {
					commentStr = "";
				}

				try {
					if (soln.getResource("hasScopeUri") != null && soln.getResource("hasScopeUri").getURI() != null) {
						hasScopeUriStr = soln.getResource("hasScopeUri").getURI();
					}
				} catch (Exception e1) {
					hasScopeUriStr = "";
				}

				spaceScopeUrisStr = retrieveSpaceScope(oc_uri);

				timeScopeUrisStr = retrieveTimeScope(oc_uri);

				oc = new ObjectCollection(oc_uri, typeStr, labelStr, commentStr, studyUriStr, hasScopeUriStr, spaceScopeUrisStr, timeScopeUrisStr);
			}
		}

		// retrieve URIs of objects that are member of the collection
		String queryMemberStr = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT  ?uriMember WHERE { " + 
				"    ?uriMember hasco:isMemberOf " + oc_uri + " .  " + 
				"}";

		Query queryMember = QueryFactory.create(queryMemberStr);
		QueryExecution qexecMember = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), queryMember);
		ResultSet resultsMember = qexecMember.execSelect();
		ResultSetRewindable resultsrwMember = ResultSetFactory.copyResults(resultsMember);
		qexecMember.close();

		if (resultsrwMember.hasNext()) {
			String uriMemberStr = "";

			while (resultsrwMember.hasNext()) {
				QuerySolution soln = resultsrwMember.next();
				if (soln != null) {
					try {
						if (soln.getResource("uriMember") != null && soln.getResource("uriMember").getURI() != null) {
							uriMemberStr = soln.getResource("uriMember").getURI();
							oc.getObjectUris().add(uriMemberStr);
						}
					} catch (Exception e1) {
						uriMemberStr = "";
					}
				}
			}
		}
		
		return oc;
	}

	public static List<ObjectCollection> findAll() {
		List<ObjectCollection> oc_list = new ArrayList<ObjectCollection>();

		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT ?uri WHERE { " + 
				"   ?ocType rdfs:subClassOf+ hasco:ObjectCollection . " +
				"   ?uri a ?ocType . } ";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null && soln.getResource("uri").getURI() != null) { 
				ObjectCollection sc = ObjectCollection.find(soln.getResource("uri").getURI());
				oc_list.add(sc);
			}
		}
		return oc_list;
	}

	public static List<ObjectCollection> findDomainByStudy(Study study) {
		List<ObjectCollection> ocList = new ArrayList<ObjectCollection>();
		for (ObjectCollection oc : ObjectCollection.findByStudy(study)) {
			if (oc.isDomainCollection()) {
				ocList.add(oc);
			}
		}
		return ocList;
	}

	public static List<ObjectCollection> findByStudy(Study study) {
		if (study == null) {
			return null;
		}
		List<ObjectCollection> ocList = new ArrayList<ObjectCollection>();

		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT ?uri WHERE { " + 
				"   ?ocType rdfs:subClassOf+ hasco:ObjectCollection . " +
				"   ?uri a ?ocType .  " +
				"   ?uri hasco:isMemberOf  <" + study.getUri() + "> . " +
				" } ";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null && soln.getResource("uri").getURI() != null) { 
				ObjectCollection oc = ObjectCollection.find(soln.getResource("uri").getURI());
				ocList.add(oc);
			}
		}
		return ocList;
	}

	public static String findByStudyJSON(Study study) {
		if (study == null) {
			return null;
		}
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT ?uri ?label WHERE { " + 
				"   ?ocType rdfs:subClassOf+ hasco:ObjectCollection . " +
				"   ?uri a ?ocType .  " +
				"   ?uri hasco:isMemberOf  <" + study.getUri() + "> . " +
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

	private void saveObjectUris(String oc_uri) {
		if (objectUris == null || objectUris.size() == 0) {
			return;
		}

		String insert = "";

		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		for (String uri : objectUris) {
			if (uri != null && !uri.equals("")) {
				if (uri.startsWith("http")) {
					insert += "  <" + uri + "> hasco:isMemberOf  " + oc_uri + " . ";
				} else {
					insert += "  " + uri + " hasco:isMemberOf  " + oc_uri + " . ";
				}
			}
		}
		insert += LINE_LAST;
		UpdateRequest request = UpdateFactory.create(insert);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
	}

	public void save() {
		String insert = "";

		String oc_uri = "";
		if (this.getUri().startsWith("<")) {
			oc_uri = this.getUri();
		} else {
			oc_uri = "<" + this.getUri() + ">";
		}

		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		insert += oc_uri + " a <" + typeUri + "> . ";
		insert += oc_uri + " rdfs:label  \"" + this.getLabel() + "\" . ";
		if (this.getStudyUri().startsWith("http")) {
			insert += oc_uri + " hasco:isMemberOf  <" + this.getStudyUri() + "> . ";
		} else {
			insert += oc_uri + " hasco:isMemberOf  " + this.getStudyUri() + " . ";
		}
		if (this.getComment() != null && !this.getComment().equals("")) {
			insert += oc_uri + " rdfs:comment  \"" + this.getComment() + "\" . ";
		}
		if (this.getHasScopeUri() != null && !this.getHasScopeUri().equals("")) {
			if (this.getHasScopeUri().startsWith("http")) {
				insert += oc_uri + " hasco:hasScope  <" + this.getHasScopeUri() + "> . ";
			} else {
				insert += oc_uri + " hasco:hasScope  " + this.getHasScopeUri() + " . ";
			}
		}
		if (this.getSpaceScopeUris() != null && !this.getSpaceScopeUris().equals("")) {
			for (String spaceScope : this.getSpaceScopeUris()) {
				if (spaceScope.startsWith("http")) {
					insert += oc_uri + " hasco:hasSpaceScope  <" + spaceScope + "> . ";
				} else {
					insert += oc_uri + " hasco:hasSpaceScope  " + spaceScope + " . ";
				}
			}
		}
		if (this.getTimeScopeUris() != null && !this.getTimeScopeUris().equals("")) {
			for (String timeScope : this.getTimeScopeUris()) {
				if (timeScope.startsWith("http")) {
					insert += oc_uri + " hasco:hasTimeScope  <" + timeScope + "> . ";
				} else {
					insert += oc_uri + " hasco:hasTimeScope  " + timeScope + " . ";
				}
			}
		}
		insert += LINE_LAST;
		UpdateRequest request = UpdateFactory.create(insert);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
		saveObjectUris(oc_uri);
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
		row.put("rdfs:label", getLabel());
		row.put("hasco:isMemberOf", getStudyUri());
		rows.add(row);

		int totalChanged = 0;
		try {
			totalChanged = loader.insertRows("ObjectCollection", rows);
		} catch (CommandException e) {
			try {
				totalChanged = loader.updateRows("ObjectCollection", rows);
			} catch (CommandException e2) {
				System.out.println("[ERROR] Could not insert or update ObjectCollection(s)");
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
		return loader.deleteRows("ObjectCollection", rows);
	}

	public void delete() {
		String query = "";

		String oc_uri = "";
		if (this.getUri().startsWith("<")) {
			oc_uri = this.getUri();
		} else {
			oc_uri = "<" + this.getUri() + ">";
		}

		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += DELETE_LINE1;
		query += " " + oc_uri + "  ";
		query += DELETE_LINE3;
		query += LINE_LAST;

		UpdateRequest request = UpdateFactory.create(query);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
	}
}
